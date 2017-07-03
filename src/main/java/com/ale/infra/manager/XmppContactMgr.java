package com.ale.infra.manager;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.contact.IContactSearchListener;
import com.ale.infra.contact.RainbowPresence;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.directory.IDirectoryProxy;
import com.ale.infra.proxy.users.IUserProxy;
import com.ale.infra.xmpp.AbstractRainbowXMPPConnection;
import com.ale.infra.xmpp.xep.message.UserVcardUpdateEvent;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.util.DateTimeUtil;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.iqlast.LastActivityManager;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * XMPP contact manager.
 */
public class XmppContactMgr implements RosterListener {
    final static String LOG_TAG = "XmppContactMgr";

    private final IContactCacheMgr m_contactCacheMgr;
    private final Set<XmppContactMgrListener> m_changeListeners = new HashSet<>();
    private final IDirectoryProxy m_directoryProxy;
    private final IPlatformServices m_platformServices;
    private final IUserProxy m_userProxy;
    private AbstractXMPPConnection m_connection;
    private Roster m_roster;
    private RainbowPresence userSettingsPresence;
    private Contact m_user;
    private final StanzaListener m_presencePacketListener = new StanzaListener() {
        @Override
        public synchronized void processPacket(final Stanza packet) throws SmackException.NotConnectedException {
            UserVcardUpdateEvent userVcardUpdate = packet.getExtension(UserVcardUpdateEvent.ELEMENT, UserVcardUpdateEvent.NAMESPACE);
            if (userVcardUpdate != null) {
                m_contactCacheMgr.refreshUser();
            }

            if (m_user != null && packet.getFrom() != null && packet.getFrom().startsWith(m_user.getImJabberId())) {
                presenceChanged((Presence) packet);
            }
        }
    };
    private LastActivityManager m_lastActivityManager;

    public XmppContactMgr(AbstractRainbowXMPPConnection rainbowXMPPConnection,
                          IPlatformServices platformServices, IContactCacheMgr contactCacheMgr,
                          IDirectoryProxy directoryProxy, IUserProxy userProxy) {
        m_contactCacheMgr = contactCacheMgr;
        if (m_contactCacheMgr != null) {
            m_user = m_contactCacheMgr.getUser();
        }

        m_connection = rainbowXMPPConnection;
        m_roster = Roster.getInstanceFor(m_connection);
        // make roster query right after login (get them in "entriesAdded" callback)
        // NB : set this before XMPP connect
        m_roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
        m_roster.addRosterListener(this);

        m_directoryProxy = directoryProxy;
        m_platformServices = platformServices;
        m_userProxy = userProxy;
        m_lastActivityManager = LastActivityManager.getInstanceFor(m_connection);

        StanzaFilter presenceFilter = new StanzaTypeFilter(Presence.class);
        m_connection.addSyncStanzaListener(m_presencePacketListener, presenceFilter);

        ProviderManager.addExtensionProvider(UserVcardUpdateEvent.ELEMENT, UserVcardUpdateEvent.NAMESPACE, new UserVcardUpdateEvent.Provider());
    }



    private Presence parsePresence(String presencePacket, Contact contact) {
        boolean isPresenceAvailable = true; // No presence info in Stanza;
        Presence presence = null;
        try {
            String to = "";
            String from = "";
            String type = null;


            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();

            parser.setInput(new ByteArrayInputStream(presencePacket.getBytes()), null);

            parser.next();


            String elt;
            String value;
            for (int i = 0; i < parser.getAttributeCount(); i++) {
                elt = parser.getAttributeName(i);
                value = parser.getAttributeValue(i);
                switch (elt) {
                    case "type":
                        type = value;
                        break;
                    case "to":
                        to = value;
                        break;
                    case "from":
                        from = value;
                        break;

                    default:
                        break;
                }
            }


            Presence.Type presType = Presence.Type.available;
            if (type != null && !type.isEmpty()) {
                presType = Presence.Type.fromString(type);
            }
            presence = new Presence(presType);
            presence.setTo(to);
            presence.setFrom(from);


            // Parse sub-elements
            int eventType = parser.next();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String elementName = parser.getName();

                        switch (elementName) {
                            case "status":
                                presence.setStatus(parser.nextText());
                                break;
                            case "priority":
                                int priority = Integer.parseInt(parser.nextText());
                                presence.setPriority(priority);
                                break;
                            case "show":
                                String modeText = parser.nextText();
                                if (StringUtils.isNotEmpty(modeText)) {
                                    presence.setMode(Presence.Mode.fromString(modeText));
                                } else {
                                    // Some implementations send presence stanzas with a
                                    // '<show />' element, which is a invalid XMPP presence
                                    // stanza according to RFC 6121 4.7.2.1
                                    Log.getLogger().warn(LOG_TAG, "Empty or null mode text in presence show element form " + presence.getFrom() + " with id '" + presence.getStanzaId() + "' which is invalid according to RFC6121 4.7.2.1");
                                }
                                break;
                            case "x":
                                isPresenceAvailable = false;
                                break;
                            case "photo":
                                isPresenceAvailable = false;
                                break;
                            case "avatar":
                                isPresenceAvailable = false;
                                Date now = new Date();
                                if (contact.getDirectoryContact() != null) {
                                    contact.getDirectoryContact().setLastAvatarUpdateDate(DateTimeUtil.getStringStampFromDate(now));
                                }
                                m_contactCacheMgr.downloadContactAvatar(contact, true);
                                break;
                            case "data":
                                if (m_user == contact) {
                                    loadUserInfos();
                                } else {
                                    resolveRosterContacts();
                                }
                                break;
                            default:
                                break;
                        }
                }
                eventType = parser.next();
            }
        }
        catch (XmlPullParserException e)
        {
            Log.getLogger().error(LOG_TAG, "XmlPullParserException: ", e);
        }
        catch (IOException e)
        {
            Log.getLogger().error(LOG_TAG, "IOException: ", e);
        }

        if (isPresenceAvailable)
            return presence;
        else
            return null;
    }

    private String getResourceIdFromPresence(String userJid) {
        String resourceId = StringsUtil.EMPTY;

        if ((userJid != null) && (userJid.contains("/"))) {
            resourceId = userJid.substring(userJid.indexOf("/") + 1);
        }

        return resourceId;
    }

    //////////////////////////////////////RosterListener//////////////////////////////////////
    @Override
    public void entriesAdded(final Collection<String> addresses) {
        Log.getLogger().info(LOG_TAG, ">>>> entriesAdded; " + addresses.size());

        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (String rosterId : addresses) {
                    Log.getLogger().verbose(LOG_TAG, ">entriesAdded; ROSTERID = " + rosterId);
                    if (rosterId.startsWith(StringsUtil.JID_TEL_PREFIX)) {
                        Log.getLogger().verbose(LOG_TAG, "Skipping tel Id; " + rosterId);
                        continue;
                    }
                    if (m_user != null && rosterId.equals(m_user.getImJabberId())) {
                        Log.getLogger().verbose(LOG_TAG, "Skipping OWN Id; " + rosterId);
                        continue;
                    }


                    updateContactWithSubscribeState(rosterId);
                }
                resolveRosterContacts();
            }
        });
        myThread.start();
    }

    private void resolveRosterContacts() {
        Log.getLogger().verbose(LOG_TAG, ">resolveRosterContacts;");

        m_directoryProxy.searchNetwork(m_roster.getEntryCount(), new IDirectoryProxy.IDirectoryListener() {
            @Override
            public void onCorporateSearchSuccess(List<DirectoryContact> searchResults) {
                Log.getLogger().verbose(LOG_TAG, ">resolveRosterContacts; searchNetwork onCorporateSearchSuccess");

                if (searchResults.size() > 0) {
                    for (DirectoryContact dirContact : searchResults) {
                        m_contactCacheMgr.createContactIfNotExistOrUpdate(dirContact);
                    }

                    m_contactCacheMgr.updateListAndSortIt();
                    notifyRostersChanged();
                }
            }

            @Override
            public void onFailure() {
                Log.getLogger().warn(LOG_TAG, ">resolveRosterContacts; searchNetwork onFailure");
            }
        });
    }

    @Override
    public void entriesUpdated(final Collection<String> addresses) {
        Log.getLogger().info(LOG_TAG, ">>>> entriesUpdated");

        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (String rosterId : addresses) {
                    Log.getLogger().verbose(LOG_TAG, ">entriesUpdated; ROSTERID = " + rosterId);
                    if (rosterId.startsWith(StringsUtil.JID_TEL_PREFIX)) {
                        Log.getLogger().verbose(LOG_TAG, "Skipping tel Id; " + rosterId);
                        continue;
                    }
                    if (m_user != null && rosterId.equals(m_user.getImJabberId())) {
                        Log.getLogger().verbose(LOG_TAG, "Skipping OWN Id; " + rosterId);
                        continue;
                    }

                    updateContactWithSubscribeState(rosterId);
                }

                resolveRosterContacts();
            }
        });
        myThread.start();
    }

    private void updateContactWithSubscribeState(String rosterId) {
        Log.getLogger().verbose(LOG_TAG, ">updateContactWithSubscribeState; "+rosterId);
        Contact foundContact = m_contactCacheMgr.getContactFromJid(rosterId);
        if (foundContact == null) {
            Log.getLogger().verbose(LOG_TAG, "Create new Contact for roster");
            DirectoryContact dirContact = new DirectoryContact();
            dirContact.setImJabberId(rosterId);

            foundContact = m_contactCacheMgr.createContactIfNotExistOrUpdate(dirContact);
        }
        foundContact.setIsRoster(true);
        foundContact.setRosterInProgress(false);
        foundContact.setPresence(null, RainbowPresence.OFFLINE);

        RosterEntry rosterEntry = m_roster.getEntry(foundContact.getImJabberId());
        if (rosterEntry != null) {
            RosterPacket.ItemStatus status = rosterEntry.getStatus();
            RosterPacket.ItemType type = rosterEntry.getType();
            if (type != null && (type == RosterPacket.ItemType.none || type == RosterPacket.ItemType.remove) && status == null) {//removed by toher side
                Log.getLogger().info(LOG_TAG, ">updateContactWithSubscribeState type(susbcription):" + type + " status(ask):" + status + " contact:" + foundContact.getDisplayName4Log(""));
                try {
                    deleteContactFromRoster(foundContact.getImJabberId());
                } catch (SmackException.NotLoggedInException e) {
                    Log.getLogger().error(LOG_TAG, "NotLoggedInException : " + e.getMessage());
                } catch (SmackException.NoResponseException e) {
                    Log.getLogger().error(LOG_TAG, "NoResponseException : " + e.getMessage());
                } catch (XMPPException.XMPPErrorException e) {
                    Log.getLogger().error(LOG_TAG, "XMPPErrorException : " + e.getMessage());
                } catch (SmackException.NotConnectedException e) {
                    Log.getLogger().error(LOG_TAG, "NotConnectedException");
                }
            }
        }
    }

    @Override
    public synchronized void entriesDeleted(final Collection<String> addresses) {
        Log.getLogger().info(LOG_TAG, ">entriesDeleted");
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (String rosterId : addresses) {
                    Contact contact = m_contactCacheMgr.getContactFromJid(rosterId);
                    if (contact != null) {
                        contact.setIsRoster(false);
                        contact.setPresence(getResourceIdFromPresence(contact.getImJabberId()), RainbowPresence.UNSUBSCRIBED);
                    }
                }
                notifyRostersChanged();
                m_contactCacheMgr.updateListAndSortIt();
            }
        });
        myThread.start();
    }

    @Override
    public void presenceChanged(Presence presence) {
        if( presence == null) {
            Log.getLogger().warn(LOG_TAG, ">presenceChanged: presence is NULL");
            return;
        }

        Log.getLogger().verbose(LOG_TAG, ">presenceChanged; " + String.valueOf(presence));
        if (!m_connection.isConnected()) {
            //Do not take into account presence changed while not connected smack force unavailable when connection is down
            Log.getLogger().verbose(LOG_TAG, ">presenceChanged while not connected = NTIA");
            return;
        }
        String jabberId = presence.getFrom();

        Contact contact = m_contactCacheMgr.getContactFromJid(jabberId);
        if (contact == null) {
            Log.getLogger().verbose(LOG_TAG, " No contact for Jid; " + jabberId);
            DirectoryContact dirContact = new DirectoryContact();
            dirContact.setImJabberId(jabberId);
            contact = m_contactCacheMgr.createContactIfNotExistOrUpdate(dirContact);
        }
        Log.getLogger().verbose(LOG_TAG, ">>>> OTHER PRESENCE RECEIVED ; " + contact.getDisplayName4Log(""));
        Presence pres = parsePresence(presence.toString(), contact);
        if (pres != null) {

            String resourceId = getResourceIdFromPresence(jabberId);
            contact.setPresence(resourceId, RainbowPresence.getPresenceFrom(pres, m_contactCacheMgr.getUser() == contact));
            //<delay xmlns='urn:xmpp:delay' stamp='2017-01-06T15:54:00.045+00:00' from='0807c64963814a0a846b3f16387e7a4a@demo-all-in-one-dev-1.opentouch.cloud/web_win_1.18.7_7HOFCpSp'></delay>
            DelayInformation delayInfo = null;
            try {
                delayInfo = presence.getExtension(DelayInformation.ELEMENT,DelayInformation.NAMESPACE);
            } catch (Exception e) {
                Log.getLogger().error(LOG_TAG, "Presence has NO Delay TAG: "+e.getMessage());
            }

            if(!jabberId.startsWith(StringsUtil.JID_TEL_PREFIX))
            {
                // get message timestamp
                if (delayInfo != null)
                {
                    Date date = delayInfo.getStamp();
                    Log.getLogger().verbose(LOG_TAG, "Presence has Delay TAG: " + date);
                    contact.getDirectoryContact().setLastPresenceReceivedDate(date);
                }
                else
                {
                    contact.getDirectoryContact().setLastPresenceReceivedDate(new Date());
                }
            }
        }
    }

    private synchronized void notifyUserLoaded() {
        for (XmppContactMgrListener listener : m_changeListeners.toArray(new XmppContactMgrListener[m_changeListeners.size()])) {
            listener.onUserLoaded();
        }
    }

    private void notifyRostersChanged() {
        Log.getLogger().verbose(LOG_TAG, ">notifyRostersChanged");

        for (XmppContactMgrListener listener : m_changeListeners.toArray(new XmppContactMgrListener[m_changeListeners.size()])) {
            listener.rostersChanged();
        }
    }

    public void loadUserInfos() {
        Log.getLogger().verbose(LOG_TAG, ">loadUserInfos");

        if (m_user != null) {

            RainbowSdk.instance().contacts().searchByJid(m_user.getImJabberId(), new IContactSearchListener() {
                @Override
                public void searchStarted() {

                }

                @Override
                public void searchFinished(List<DirectoryContact> contactsFounded) {
                    Log.getLogger().verbose(LOG_TAG, ">loadUserInfos; searchByJid onCorporateSearchSuccess");

                    try {
                        if (contactsFounded.size() > 0) {
                            Log.getLogger().verbose(LOG_TAG, " fill User with Search Result");
                            DirectoryContact userDirectoryContact = contactsFounded.get(0);

                            userDirectoryContact.setPresence(null, RainbowPresence.ONLINE);
                            m_contactCacheMgr.setDirectoryContact(m_user, userDirectoryContact);
                        } else {
                            Log.getLogger().warn(LOG_TAG, " NOTHING to fill for User with Search Result");
                        }

                        //Reload Roster when user is initialized
                        m_roster.reload();

                        notifyUserLoaded();
                    } catch (Exception e) {
                        Log.getLogger().error(LOG_TAG, "error while getting user vcard : " + e);
                        m_user = null;
                    }
                }

                @Override
                public void searchError() {
                    Log.getLogger().warn(LOG_TAG, ">loadUserInfos; searchByJid onFailure");
                    notifyUserLoaded(); //even if search fails, connexion is established so trig all managers that connexion is OK.
                }
            });
        }
    }

    public void sendPresence(Presence.Type presenceType, Presence.Mode presenceMode, String moodMsg) {
        Log.getLogger().verbose(LOG_TAG, ">sendPresence; " + presenceType.toString());

        Presence p = new Presence(presenceType, moodMsg, 5, presenceMode);
        p.setStatus(moodMsg);

        try {
            m_connection.sendStanza(p);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    public void sendPresence(RainbowPresence presence) {
        sendPresence (presence.getXmppType(), presence.getXmppMode(), presence.getXmppStatus());
        String status = "online";
        if (presence.isDND())
            status ="dnd";
        else if (presence.isAway() || presence.isManualAway())
            status = "away";
        else if (presence.isOnline())
            status = "online";
        else if (presence.isXA())
            status = "invisible";

        if (m_user != null && m_userProxy != null) {
            m_userProxy.setUserSettings(m_user.getCorporateId(), "presence", status, null);
        }
        userSettingsPresence = presence;
    }

    public Presence getSettingsPresence(){
        if (userSettingsPresence == null) {
            return new Presence(Presence.Type.unavailable);
        } else {
            return  new Presence(userSettingsPresence.getXmppType(), userSettingsPresence.getXmppStatus(), 5, userSettingsPresence.getXmppMode());
        }
    }

    public void deleteContactFromRoster(String contactJid) throws SmackException.NotLoggedInException, XMPPException.XMPPErrorException, SmackException.NotConnectedException, SmackException.NoResponseException {
        Log.getLogger().verbose(LOG_TAG, ">deleteContactFromRoster");

        Contact contact = RainbowContext.getInfrastructure().getContactCacheMgr().getContactFromJid(contactJid);

        if (contactJid == null) {
            Log.getLogger().verbose(LOG_TAG, "Contact to delete is null");
            return;
        }

        if( contact.getPresence().isSubscribe()) {
            Log.getLogger().verbose(LOG_TAG, "  Send Automatic Ack to false");
        }

        contact.setPresence(null,RainbowPresence.UNSUBSCRIBED);
        contact.setIsRoster(false);
        contact.setRosterInProgress(false);
        m_contactCacheMgr.updateListAndSortIt();

        RosterEntry entry = m_roster.getEntry(contact.getImJabberId());
        if (entry != null) {
            m_roster.removeEntry(entry);
        }
    }

    public void reloadRosters(final IReloadRostersListener listener) {
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (m_roster != null) {
                        m_roster.reloadAndWait();
                    }

                    Log.getLogger().info(LOG_TAG, "ReloadRosters SUCCESS : ");

                    if (listener != null)
                        listener.onReloadRostersSuccess();
                } catch (Exception e) {
                    Log.getLogger().error(LOG_TAG, "ReloadRosters FAILED : ", e);

                    if (listener != null)
                        listener.onReloadRostersFailed();
                }
            }
        });
        myThread.start();
    }

    public synchronized void registerChangeListener(XmppContactMgrListener changeListener) {
        m_changeListeners.add(changeListener);
    }

    public synchronized void unregisterChangeListener(XmppContactMgrListener changeListener) {
        m_changeListeners.remove(changeListener);
    }

    public Roster getRoster() {
        return m_roster;
    }

    public long getLastActivityIdleTimeOfContact(String jid) {
        if( !m_connection.isConnected() || !m_connection.isAuthenticated()) {
            Log.getLogger().warn(LOG_TAG, ">getLastActivityOfUser: No connection available");
            return -1;
        }
        Log.getLogger().verbose(LOG_TAG, ">getLastActivityOfUser");

        try {
            if (m_lastActivityManager != null) {
                // TODO check with domain instead of jid
                //null is used to query server
                if (m_lastActivityManager.isLastActivitySupported(null)) {
                    Log.getLogger().warn(LOG_TAG, ">getLastActivityOfUser; not supported by Server");
                    return -1;
                }
                return m_lastActivityManager.getLastActivity(jid).getIdleTime();
            }
        } catch (SmackException e) {
            Log.getLogger().error(LOG_TAG, ">getLastActivityOfUser SmackException ; " + e.getMessage());
        } catch (XMPPException.XMPPErrorException e) {
            Log.getLogger().error(LOG_TAG, ">getLastActivityOfUser XMPPException ; " + e.getMessage());
        }
        return -1;
    }

    public void disconnect()
    {
        if(m_roster != null)
            m_roster.removeRosterListener(this);

        if( m_connection != null)
            m_connection.removeSyncStanzaListener(m_presencePacketListener);

        ProviderManager.removeExtensionProvider(UserVcardUpdateEvent.ELEMENT, UserVcardUpdateEvent.NAMESPACE);

        synchronized (this)
        {
            m_changeListeners.clear();
        }
    }

    public interface XmppContactMgrListener {
        void onUserLoaded();

        void rostersChanged();
    }

    public interface IReloadRostersListener {
        void onReloadRostersSuccess();

        void onReloadRostersFailed();
    }

}

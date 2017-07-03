package com.ale.infra.contact;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.database.ContactDataSource;
import com.ale.infra.database.IDatabaseMgr;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.invitation.CompanyContact;
import com.ale.infra.invitation.Invitation;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.manager.Conversation;
import com.ale.infra.manager.ICompanyInvitationMgr;
import com.ale.infra.manager.IInvitationMgr;
import com.ale.infra.manager.XmppContactMgr;
import com.ale.infra.manager.room.Room;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.EnduserBots.IEnduserBotsProxy;
import com.ale.infra.proxy.avatar.IAvatarProxy;
import com.ale.infra.proxy.company.ICompanyProxy;
import com.ale.infra.proxy.directory.IDirectoryProxy;
import com.ale.infra.proxy.users.GetUserSettingsResponse;
import com.ale.infra.proxy.users.IUserProxy;
import com.ale.infra.searcher.ContactCacheSearcher;
import com.ale.infra.searcher.ConversationSearcher;
import com.ale.infra.searcher.GroupSearcher;
import com.ale.infra.searcher.IDisplayable;
import com.ale.infra.searcher.RoomSearcher;
import com.ale.infra.searcher.SearcherFactory;
import com.ale.infra.searcher.localContact.ILocalContactSearcher;
import com.ale.infra.user.IUserPreferences;
import com.ale.infra.xmpp.XmppConnection;
import com.ale.infra.xmpp.xep.ILastActivityNotification;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.util.DateTimeUtil;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by georges on 21/04/16.
 */
public class ContactCacheMgr implements IContactCacheMgr, XmppContactMgr.XmppContactMgrListener, Contact.ContactListener {

    private static final String LOG_TAG = "ContactCacheMgr";

    private final SearcherFactory m_contactSearcherFactory;
    private final IDirectoryProxy m_directory;
    private final IAvatarProxy m_avatarProxy;
    private final IUserProxy m_usersProxy;
    private final IEnduserBotsProxy m_botProxy;
    private final ContactCacheFileMgr m_contactsCacheFile;
    private final IUserPreferences m_userPreferences;
    private ILocalContactSearcher m_localContactSearcher;
    private ContactCacheSearcher m_contactCacheSearcher;

    private Contact m_user;
    private ArrayItemList<Contact> m_contactsCache;

    // ArrayItemList for GUI ;
    private ArrayItemList<Contact> m_rosters;
    private ArrayItemList<Contact> m_rosterAndLocal;
    private ArrayItemList<Contact> m_connectedContacts;
    private ArrayItemList<IDisplayable> m_searchedDisplayable;

    private Date m_lastSearchDate;
    private Timer m_searchTimer;
    private ContactDataSource contactDataSource;
    private XmppContactMgr m_xmppContactMgr;
    private IInvitationMgr m_invitationMgr;

    public ContactCacheMgr(IPlatformServices platformServices, IDirectoryProxy directory, IAvatarProxy avatarProxy,
                           IUserProxy userProxy, IEnduserBotsProxy botProxy, IDatabaseMgr databaseMgr) {

        m_contactSearcherFactory = new SearcherFactory();
        m_directory = directory;
        init();
        m_avatarProxy = avatarProxy;

        m_usersProxy = userProxy;
        m_contactsCacheFile = new ContactCacheFileMgr(null);

        m_botProxy = botProxy;
        m_userPreferences = platformServices.getUserPreferences();

        if (databaseMgr != null)
            contactDataSource = databaseMgr.getContactDataSource();
        else
            contactDataSource = null;

    }

    private void init() {
        m_user = new Contact();

        m_contactsCache = new ArrayItemList<>();
        m_rosters = new ArrayItemList<>();
        m_rosterAndLocal = new ArrayItemList<>();
        m_connectedContacts = new ArrayItemList<>();
        m_searchedDisplayable = new ArrayItemList<>();

        m_lastSearchDate = null;
        m_searchTimer = null;
    }

/*--------------------------------------------------------------------------------------*/
// XmppContactMgrListener Interface
/*--------------------------------------------------------------------------------------*/
    @Override
    public void onUserLoaded() {
        Log.getLogger().info(LOG_TAG, ">onUserLoaded");
        refreshUser();
    }

    @Override
    public void rostersChanged() {
        Log.getLogger().verbose(LOG_TAG, ">rostersChanged");

        updateListAndSortIt();
    }

    @Override
    public void getBots() {
        m_botProxy.getAllBots(100, 0, new IEnduserBotsProxy.IGetAllBotsListener() {
            @Override
            public void onGetAllBotsSuccess(List<DirectoryContact> dirContacts) {
                Log.getLogger().verbose(LOG_TAG, ">onGetAllBotsSuccess; "+dirContacts.size());
                for (DirectoryContact dirContact : dirContacts) {
                    Contact contact = createContactIfNotExistOrUpdate(dirContact);
                    downloadContactAvatar(contact, true);
                    contact.setIsRoster(true);
                }
                updateRostersCache();
            }

            @Override
            public void onGetAllBotsFailure() {
                Log.getLogger().warn(LOG_TAG, ">onGetAllBotsFailure");
            }
        });
    }


    @Override
    public void getUserSettings() {
        Log.getLogger().verbose(LOG_TAG, ">getUserSettings");

        m_usersProxy.getUserSettings(getUser().getCorporateId(), new IUserProxy.IGetUserSettingsListener() {
            @Override
            public void onSuccess(GetUserSettingsResponse settings) {
                if( settings == null || settings.getPresence() == null) {
                    Log.getLogger().warn(LOG_TAG, "settings is NULL / skip result");
                    return;
                }

                if (!settings.getPresence().isEmpty() && !settings.getPresence().equals(getUser().getDirectoryContact().getPresenceSettings())) {
                    RainbowPresence presence;
                    switch (settings.getPresence()) {
                        case "dnd":
                            presence = RainbowPresence.DND;
                            break;
                        case "online":
                            presence = RainbowPresence.ONLINE;
                            break;
                        case "away":
                            presence = RainbowPresence.AWAY;
                            break;
                        case "invisible":
                            presence = RainbowPresence.XA;
                            break;
                        default:
                            presence = RainbowPresence.ONLINE;
                            break;
                    }
                    Log.getLogger().verbose(LOG_TAG, ">getUserSettings presence is" + presence.toString());
                    //Remind presence from serve

                    if (!presence.equals(getUser().getDirectoryContact().getPresenceSettings())) {
                        getUser().setPresenceSettings(presence);
                        getUser().setPresence(null, presence);
                        if (m_xmppContactMgr != null)
                            m_xmppContactMgr.sendPresence(presence);
                    }
                }
            }

            @Override
            public void onFailure(RainbowServiceException exception) {
                Log.getLogger().verbose(LOG_TAG, "> getUserSettings failure with code : " + exception.getStatusCode());
            }
        });
    }


/*--------------------------------------------------------------------------------------*/

    private void updateRostersCache() {
        List<Contact> rosterTemp = new ArrayList<>();
        List<Contact> rosterAndLocalTemp = new ArrayList<>();
        List<Contact> connectedTemp = new ArrayList<>();

        for (Contact c : m_rosters.getCopyOfDataList())
            c.unregisterChangeListener(this);

        for (Contact contact : m_contactsCache.getCopyOfDataList()) {
            if( contact == null )
                continue;


            if ( contact.isNative() || (contact.isRoster() && contact.getDirectoryContact() != null  && !contact.getDirectoryContact().isEmpty()) ) {
                rosterAndLocalTemp.add(contact);
            }
            if (contact.isRoster() && contact.getDirectoryContact() != null && !contact.getDirectoryContact().isEmpty()) {
                rosterTemp.add(contact);
                contact.registerChangeListener(this);
            }
            if ( (contact.isRoster() && contact.getDirectoryContact() != null  && !contact.getDirectoryContact().isEmpty()) && !contact.getPresence().isOffline()) {
                connectedTemp.add(contact);
            }
        }

        m_rosters.replaceAll(rosterTemp);
        m_rosterAndLocal.replaceAll(rosterAndLocalTemp);
        // TODO order connected contacts
        m_connectedContacts.replaceAll(connectedTemp);
    }

    private void updateConnectedCache() {
        List<Contact> connectedTemp = new ArrayList<>();
        for (Contact contact : m_contactsCache.getCopyOfDataList()) {
            if (contact.isRoster() && !contact.getPresence().isOffline()) {
                connectedTemp.add(contact);
            }
        }

        // TODO order connected contacts
        m_connectedContacts.replaceAll(connectedTemp);
    }

    public void initContext(Context context) {
        m_contactSearcherFactory.createContactObserver(context);
        m_contactsCacheFile.initContext(context);
        m_localContactSearcher = m_contactSearcherFactory.createContactSearcher(context);
        m_contactCacheSearcher = m_contactSearcherFactory.createContactCacheSearcher();
    }

    @Override
    public void setObserver(XmppConnection connection) {
        if( connection != null && connection.getXmppContactMgr() != null) {
            m_xmppContactMgr = connection.getXmppContactMgr();
            m_xmppContactMgr.registerChangeListener(this);
        }
    }

    public void updateListAndSortIt() {
        Log.getLogger().verbose(LOG_TAG, ">updateListAndSortIt; count=" + m_contactsCache.getCount());

        sortContactsAlphabetically(m_contactsCache.getCopyOfDataList());

        // update roster list:
        updateRostersCache();
    }

    private void sortContactsAlphabetically(List<Contact> allContacts) {
        Log.getLogger().verbose(LOG_TAG, ">sortContactsAlphabetically; count=" + allContacts.size());

        Collections.sort(allContacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact contact1, Contact contact2) {
                String nameContact1 = contact1.getDisplayName(null);
                String nameContact2 = contact2.getDisplayName(null);

                if (nameContact1 == null && nameContact2 == null)
                    return 0;
                if (nameContact1 == null)
                    return 1;
                if (nameContact2 == null)
                    return -1;

                return nameContact1.toLowerCase().compareTo(nameContact2.toLowerCase());
            }
        });

        m_contactsCache.replaceAll(allContacts);
    }

    @Override
    public ArrayItemList<Contact> getContacts() {
        return m_contactsCache;
    }

    @Override
    public ArrayItemList<Contact> getRosters() {
        return m_rosters;
    }

    @Override
    public ArrayItemList<Contact> getRosterAndLocal() {
        return m_rosterAndLocal;
    }

    @Override
    public ArrayItemList<Contact> getConnectedContacts() {
        return m_connectedContacts;
    }

    @Override
    public ArrayItemList<Contact> getRostersWithoutBot() {
        ArrayItemList<Contact> contactList = new ArrayItemList<>();
        for ( Contact contact: m_rosters.getCopyOfDataList()) {
            if (contact.isRoster() &&
                    !contact.isBot()) {
                contactList.add(contact);
            }
        }
        return contactList;
    }



    @Override
    public Contact getContactFromJid(String jabberId) {
        if ( StringsUtil.isNullOrEmpty(jabberId) )
            return null;

        String id = StringsUtil.getJidWithoutDevicePartAndTelPart(jabberId);

        if (id.equals(m_user.getImJabberId()))
            return m_user;
        for (Contact contact : m_contactsCache.getCopyOfDataList()) {
            String contactJid = contact.getImJabberId();
            if (id.equals(contactJid)) {
                return contact;
            }
        }

        return null;
    }

    @Override
    public Contact getContactFromCorporateId(String corporateId) {
        if ( StringsUtil.isNullOrEmpty(corporateId) )
            return null;

        if (corporateId.equals(m_user.getCorporateId()))
            return m_user;
        for (Contact contact : m_contactsCache.getCopyOfDataList()) {
            if (corporateId.equals(contact.getCorporateId())) {
                return contact;
            }
        }

        return null;
    }


    @Override
    public Contact getContactFromId(String contactId) {
        if ( StringsUtil.isNullOrEmpty(contactId) )
            return null;

        for (Contact contact : m_contactsCache.getCopyOfDataList()) {
            if (contactId.equals(contact.getContactId())) {
                return contact;
            }
        }
        return null;
    }

    @Override
    public Contact getContactFromEmail(String email) {
        for (Contact cacheContact : m_contactsCache.getCopyOfDataList()) {
            for (EmailAddress contactCacheEmailAddress : cacheContact.getEmailAddresses()) {
                if (contactCacheEmailAddress.getValue().equalsIgnoreCase(email)) {
                    return cacheContact;
                }
            }
        }
        return null;
    }

    @Override
    public synchronized Contact createContactIfNotExistOrUpdate(DirectoryContact dirContact) {
        Log.getLogger().verbose(LOG_TAG, ">createContactIfNotExistOrUpdate; " + dirContact.getDisplayName4Log(""));

        Contact contact = getContactFromJid(dirContact.getImJabberId());
        if (contact != null) {
            Log.getLogger().verbose(LOG_TAG, " Contact " + dirContact.getDisplayName4Log("") + " found by JID in ContactCacheMgr");
            setDirectoryContact(contact, dirContact);
        } else {
            contact = getContactFromEmail(dirContact.getMainEmailAddress());
            if (contact != null) {
                Log.getLogger().verbose(LOG_TAG, " Contact " + dirContact.getDisplayName4Log("") + " found by email in ContactCacheMgr");
                setDirectoryContact(contact, dirContact);
            } else {//try all emails available
                contact = getContactFromEmail(dirContact.getFirstEmailAddress());
                if (contact != null) {
                    Log.getLogger().verbose(LOG_TAG, " Contact " + dirContact.getDisplayName4Log("") + " found by first email in ContactCacheMgr");
                    setDirectoryContact(contact, dirContact);
                } else {
                    contact = getContactFromCorporateId(dirContact.getCorporateId());
                    if (contact != null) {
                        Log.getLogger().verbose(LOG_TAG, " Contact " + dirContact.getDisplayName4Log("") + " found by CorporateId in ContactCacheMgr");
                        setDirectoryContact(contact, dirContact);
                    } else {
                        Log.getLogger().verbose(LOG_TAG, " Create New Contact " + dirContact.getDisplayName4Log("") + " in ContactCacheMgr");
                        contact = new Contact();
                        setDirectoryContact(contact, dirContact);
                        addNewContact(contact);
                        updateRostersCache();
                    }
                }
            }
        }
        return contact;
    }

    @Override
    public void resolveContactFromDB(DirectoryContact dirContact) {

        Contact contact = getContactFromJid(dirContact.getImJabberId());
        if (contact != null) {
            Log.getLogger().verbose(LOG_TAG, " Contact " + dirContact.getDisplayName4Log("") + " found by JID in ContactCacheMgr");
            setDirectoryContactFromDB(contact, dirContact);
        } else {
            contact = getContactFromCorporateId(dirContact.getCorporateId());
            if (contact != null) {
                Log.getLogger().verbose(LOG_TAG, " Contact " + dirContact.getDisplayName4Log("") + " found by CorporateId in ContactCacheMgr");
                setDirectoryContactFromDB(contact, dirContact);
            } else {
                Log.getLogger().verbose(LOG_TAG, " Create New Contact " + dirContact.getDisplayName4Log("") + " in ContactCacheMgr");
                contact = new Contact();
                setDirectoryContactFromDB(contact, dirContact);
                addNewContact(contact);
                updateRostersCache();
            }
        }
    }

    @Override
    public void setUser() {
        String userJid = RainbowContext.getPlatformServices().getApplicationData().getUserId();
        Contact user = getContactFromCorporateId(userJid);
        if (user != null)
            m_user = user;
    }

    @Override
    public void removeObserver(XmppConnection m_connection)
    {
        if(m_xmppContactMgr != null)
        {
            m_xmppContactMgr.unregisterChangeListener(this);
            m_xmppContactMgr = null;
        }

        m_contactsCache.clear();
        m_rosters.clear();
        m_rosterAndLocal.clear();
        m_connectedContacts.clear();
        m_searchedDisplayable.clear();
    }

    private void setDirectoryContactFromDB(final Contact contact, DirectoryContact directoryContact) {

        if(contact.getDirectoryContact() == directoryContact)
            return;

        if (contact.isCorporate()) {
            contact.getDirectoryContact().fillEmptyFieldsWithContact(directoryContact);
        } else {
            contact.setDirectoryContact(directoryContact);
        }
        downloadContactAvatar(contact, false);
    }

    @Override
    public Contact getContactFromEmail(Set<EmailAddress> addresses) {

        for (Contact cacheContact : m_contactsCache.getCopyOfDataList()) {
            for (EmailAddress contactCacheEmailAddress : cacheContact.getEmailAddresses()) {
                if( contactCacheEmailAddress != null) {
                    for (EmailAddress contactEmailAddress : addresses) {
                        if (contactCacheEmailAddress.equals(contactEmailAddress)) {
                            return cacheContact;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void abortSearchDelayed() {
        if (m_searchTimer != null)
            m_searchTimer.cancel();
    }


    @Override
    public void searchDelayedByFullName(final String fullName, final boolean searchLocalContacts,
                                        final boolean searchConversations, final boolean searchGroups,
                                        final boolean searchCompanies, final boolean searchRooms,
                                        final IContactSearchListener listener) {
        //Log.getLogger().verbose(LOG_TAG, ">searchDelayedByFullName; " + fullName);

        if (StringsUtil.isNullOrEmpty(fullName)) {
            Log.getLogger().warn(LOG_TAG, "No text given for search delayed");
            return;
        }

        if (m_lastSearchDate == null) {
            Log.getLogger().debug(LOG_TAG, "Any Last Time on searchDelay");
            m_lastSearchDate = new Date();
        }
        int timeFromLastSearch = DateTimeUtil.getNumberOfMilliSecondsBetweenDates(m_lastSearchDate, new Date());
        Log.getLogger().debug(LOG_TAG, "time = " + String.valueOf(timeFromLastSearch) + "ms");
        if (timeFromLastSearch < 1000) {
            Log.getLogger().debug(LOG_TAG, "Abort because timeout not reached ; " + String.valueOf(timeFromLastSearch) + "ms");
            m_directory.abortSearch();
            m_lastSearchDate = new Date();
            if (m_searchTimer != null)
                m_searchTimer.cancel();

            m_searchTimer = new Timer();
            m_searchTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ContactCacheMgr.this.searchDelayedByFullName(fullName, searchLocalContacts,
                            searchConversations, searchGroups, searchCompanies, searchRooms, listener);
                }
            }, 1000);
            return;
        }

        m_lastSearchDate = null;
        if (m_searchTimer != null)
            m_searchTimer.cancel();

        m_searchedDisplayable.clear();
        if (listener != null)
            listener.searchStarted();
        //Log.getLogger().verbose(LOG_TAG, "directory searchByName with : " + fullName);
        Thread iContactsSearch = new Thread(new Runnable() {
            public void run() {
                Log.getLogger().info(LOG_TAG, "thread to retrieve local contacts is running...");
                Log.getLogger().verbose(LOG_TAG, "size of the fullname to search + " + fullName.length());

                if (fullName.length() >= 2) {
                    RainbowSdk.instance().contacts().searchByName(fullName, new IContactSearchListener() {
                        @Override
                        public void searchStarted() {

                        }

                        @Override
                        public void searchFinished(List<DirectoryContact> contactsFounded) {
                            {
                                Log.getLogger().verbose(LOG_TAG, ">searchDelayedByFullName; onCorporateSearchSuccess");

                                if( searchLocalContacts ) {
                                    ConversationSearcher convSearcher = m_contactSearcherFactory.createConversationSearcher();
                                    addDisplayablesInSearchList(m_contactCacheSearcher.searchByNameOnRosterAndLocalWithConversationFilter(fullName, convSearcher.searchByName(fullName)));
                                }
                                if( searchConversations ) {
                                    ConversationSearcher convSearcher = m_contactSearcherFactory.createConversationSearcher();
                                    addDisplayablesInSearchList(convSearcher.searchByName(fullName));
                                }
                                if( searchGroups ) {
                                    GroupSearcher groupSearcher = m_contactSearcherFactory.createGroupSearcher();
                                    addDisplayablesInSearchList(groupSearcher.searchByName(fullName));
                                }
                                if( searchCompanies ) {
                                    ICompanyInvitationMgr companyMgr = RainbowContext.getInfrastructure().getCompanyInvitationMgr();
                                    companyMgr.searchByName(fullName, new ICompanyProxy.IGetCompanyDataListener() {
                                        @Override
                                        public void onSuccess(List<CompanyContact> requestList) {
                                            List<IDisplayable> companyDisplayableList = new ArrayList<>();
                                            for(CompanyContact company : requestList) {
                                                companyDisplayableList.add(company);
                                            }

                                            addDisplayablesInSearchList(companyDisplayableList);
                                        }

                                        @Override
                                        public void onFailure(RainbowServiceException exception) {
                                        }
                                    });
                                }
                                if( searchRooms ) {
                                    RoomSearcher roomSearcher = m_contactSearcherFactory.createRoomSearcher();
                                    ConversationSearcher convSearcher = m_contactSearcherFactory.createConversationSearcher();
                                    addDisplayablesInSearchList(roomSearcher.searchByNameWithConversationFilterAndResultAsConversation(fullName, convSearcher.searchByName(fullName)));
                                }

                                //add directory contact search result
                                computeSearchListAndFilterOwnContact(fullName, contactsFounded);

                                if (listener != null)
                                    listener.searchFinished(contactsFounded);
                            }
                        }

                        @Override
                        public void searchError() {
                            Log.getLogger().warn(LOG_TAG, ">searchDelayedByFullName; onCorporateSearchFailure");

                            if (listener != null)
                                listener.searchError();
                        }
                    });
                } else if (fullName.length() == 1) {
                    if (listener != null) {
                        if( searchLocalContacts ) {
                            m_searchedDisplayable.addAll(m_contactCacheSearcher.searchByNameOnRosterAndLocal(fullName));
                        }
                        // TODO : change it
                        listener.searchFinished(null);
                    }
                }
            }
        });
        iContactsSearch.start();
        m_lastSearchDate = null;
    }


    private void addDisplayablesInSearchList(List<IDisplayable> contactList) {
        Log.getLogger().verbose(LOG_TAG, ">addDisplayablesInSearchList");
        List<IDisplayable> displayableToAdd = new ArrayList<>();

        for (IDisplayable contact : contactList) {
            if( !m_searchedDisplayable.getCopyOfDataList().contains(contact)) {
                Log.getLogger().verbose(LOG_TAG, " adding contact ; "+contact.getDisplayName(""));
                displayableToAdd.add(contact);
            }
        }
        m_searchedDisplayable.addAll(displayableToAdd);
    }

    @Override
    public void setDirectoryContact(final Contact contact, DirectoryContact directoryContact) {

        if(contact.getDirectoryContact() == directoryContact)
            return;

        if (contact.isCorporate()) {
            contact.getDirectoryContact().fillEmptyFieldsWithContact(directoryContact);

            contact.notifyContactUpdated();
        } else {
            contact.setDirectoryContact(directoryContact);
        }

        if (contactDataSource != null)
            contactDataSource.createOrUpdateContact(contact);
        downloadContactAvatar(contact, false);
    }



    private void computeSearchListAndFilterOwnContact(String query, List<DirectoryContact> dirContacts) {
        List<IDisplayable> directoryFound = new ArrayList<>();

        for (DirectoryContact dirContact : dirContacts) {
            if( dirContact.getImJabberId().equals(m_user.getImJabberId())) {
                Log.getLogger().warn(LOG_TAG, "Skip own user from Search Result");
                continue;
            }

            Contact contact = createContactIfNotExistOrUpdate(dirContact);
            if (!m_searchedDisplayable.getCopyOfDataList().contains(contact)) {
                directoryFound.add(contact);
            }
        }

        for(IDisplayable displayable: m_contactSearcherFactory.createConversationSearcher().searchByName(query)) {
            if (displayable instanceof Conversation) {
                Conversation conversation = (Conversation) displayable;
                if (m_searchedDisplayable.getCopyOfDataList().contains(conversation) && conversation.isChatType() && directoryFound.contains(conversation.getContact()) ) {
                    directoryFound.remove(conversation.getContact());
                }
            }
        }

        m_searchedDisplayable.addAll(directoryFound);
    }

    @Override
    public synchronized ArrayItemList<IDisplayable> getSearchedContacts() {
        return m_searchedDisplayable;
    }

    @Override
    public synchronized void clearSearchedContacts() {
        m_searchedDisplayable.clear();
    }

    @Override
    public synchronized void retrieveMobileLocalContacts() {
        if (m_localContactSearcher == null) {
            Log.getLogger().warn(LOG_TAG, "retrieveMobileLocalContacts; localContactSearcher is NULL");
            return;
        }

        Thread iContactsSearch = new Thread(new Runnable() {
            public void run() {
                Log.getLogger().info(LOG_TAG, "thread to retrieve local contacts is running...");

                m_localContactSearcher.retrieveAllVisibleGroups();

                if (m_userPreferences.isUseContactFilteringMode())
                    updateContactCacheWithLocalContacts(m_localContactSearcher.searchAndroidFilteredAndWithEmail());
                else
                    updateContactCacheWithLocalContacts(m_localContactSearcher.searchAllWithEmail());

                searchMatchingBusinessContactInServer();

                updateListAndSortIt();

            }
        });
        iContactsSearch.start();

    }


    private void searchMatchingBusinessContactInServer() {
        Log.getLogger().verbose(LOG_TAG, ">searchMatchingBusinessContactInServer");

        List<String> emails = new ArrayList<>();
        for (Contact contact : m_contactsCache.getCopyOfDataList()) {
            if (contact.isNative()) {
                if (contact.isCorporate()) {
                    continue;
                }

                for (EmailAddress emailAddress : contact.getEmailAddresses()) {
                    emails.add(emailAddress.getValue());
                }
            }
        }
        if (emails.size() > 0) {
            searchContactByEmailsOnServer(emails);
        }
    }


    private void searchContactByEmailsOnServer(final List<String> contactEmail) {
        //Log.getLogger().verbose(LOG_TAG, ">searchContactByEmailsOnServer; " + contactEmail);

        m_directory.searchByMails(contactEmail, new IDirectoryProxy.IDirectoryListener() {
            @Override
            public void onCorporateSearchSuccess(List<DirectoryContact> searchResults) {
                Log.getLogger().verbose(LOG_TAG, ">onCorporateSearchSuccess");
                for (DirectoryContact dirContact : searchResults) {
                    Contact contact = ContactCacheMgr.this.getContactFromEmail(dirContact.getEmailAddresses());
                    if (contact != null) {
                        setDirectoryContact(contact, dirContact);
                    } else {
                        ContactCacheMgr.this.createContactIfNotExistOrUpdate(dirContact);
                    }
                }
            }

            @Override
            public void onFailure() {
                Log.getLogger().warn(LOG_TAG, ">onAvatarFailure");
            }
        });
    }

    private void updateContactCacheWithLocalContacts(List<IContact> iContacts) {

        for (IContact iContact : iContacts) {
            Contact contact = getContactFromEmail(iContact.getEmailAddresses());
            if (contact == null) {
                contact = new Contact();
                // add contact into contact cache
                addNewContact(contact);
            }

            if (iContact.isCorporate()) {
                setDirectoryContact(contact, (DirectoryContact) iContact);
            } else if (iContact.isNative()) {
                contact.setLocalContact((LocalContact) iContact);
            }
        }
    }




    @Override
    public void downloadContactAvatar(final Contact contact, boolean forceReload) {
        if( StringsUtil.isNullOrEmpty(contact.getImJabberId()) ) {
            Log.getLogger().warn(LOG_TAG, ">downloadContactAvatar; contact has no JID - skip");
            return;
        }
        String contactLogInfo = contact.getDisplayName4Log("");
        if( StringsUtil.isNullOrEmpty(contactLogInfo) )
            contactLogInfo = contact.getImJabberId();


        Log.getLogger().verbose(LOG_TAG, "Getting Avatar for Contact; " + contactLogInfo);
        //search photo on server side
        // 288 = 96*3

        final DirectoryContact directoryContact = contact.getDirectoryContact();
        if( !forceReload) {
            Log.getLogger().verbose(LOG_TAG, "Check if photo needs to be downloaded");
            if ( contact.getPhoto() != null ) {
                Log.getLogger().verbose(LOG_TAG, "Contact " +contact.getDisplayName4Log("") + " has already a Photo");
                return;
            }
        }
        if( contact.getDirectoryContact() != null &&
                !contact.isBot() &&
                StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(contact.getDirectoryContact().getLastAvatarUpdateDate()) ) {
            Log.getLogger().verbose(LOG_TAG, "Contact " + contact.getDisplayName4Log("") + " has no Photo in Server");
            return;
        }

        //float scale = m_context.getResources().getDisplayMetrics().density;

        final String simpleJidNameWithoutDomain = StringsUtil.getJidWithoutDomain(contact.getImJabberId());
        File photoFromCache = m_contactsCacheFile.findFileStartingBy(simpleJidNameWithoutDomain);
        if( photoFromCache != null ) {
            Log.getLogger().verbose(LOG_TAG, "Photo From Cache detected for " + contactLogInfo);
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(photoFromCache), null, options);

                if( checkPhotoCacheDateValidity(photoFromCache.getName(),directoryContact.getLastAvatarUpdateDate()) ) {
                    Log.getLogger().verbose(LOG_TAG, "Using Cache photo for "+contactLogInfo);
                    updatePhoto(contact, bitmap);
                    return;
                } else {
                    Log.getLogger().verbose(LOG_TAG, "Cached Photo of "+contactLogInfo+" is obsolete / delete it");
                    m_contactsCacheFile.deleteFile(photoFromCache.getName());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }


        Log.getLogger().verbose(LOG_TAG, "Asking Server photo for "+ contactLogInfo);
        if( m_avatarProxy != null) {
            m_avatarProxy.getAvatar(contact, 288, new IAvatarProxy.IAvatarListener() {
                @Override
                public void onAvatarSuccess(Bitmap bmp) {
                    Log.getLogger().verbose(LOG_TAG, ">onAvatarSuccess");
                    updatePhoto(contact, bmp);

                    StringBuilder fileName = new StringBuilder();
                    fileName.append(simpleJidNameWithoutDomain);
                    fileName.append("_");
                    fileName.append(directoryContact.getLastAvatarUpdateDate());
                    Log.getLogger().verbose(LOG_TAG, "Contact ImageName; " + fileName.toString());

                    m_contactsCacheFile.save(fileName.toString(), bmp);
                }

                @Override
                public void onAvatarFailure() {
                    Log.getLogger().warn(LOG_TAG, ">onAvatarFailure; "+ contact.getDisplayName4Log(""));
                }
            });
        }
    }

    public boolean checkPhotoCacheDateValidity(String photoFileName, String lastAvatarUpdateDateStrg) {
        if( StringsUtil.isNullOrEmpty(photoFileName) )
            return false;
        if( StringsUtil.isNullOrEmpty(lastAvatarUpdateDateStrg) )
            return true;
        if( !photoFileName.contains("_") )
            return false;

        String fileCachedStrg = photoFileName.substring(photoFileName.lastIndexOf("_")+1,photoFileName.length());
        Date fileCachedDate = DateTimeUtil.getDateFromStringStamp(fileCachedStrg);
        if( fileCachedDate == null) {
            Log.getLogger().warn(LOG_TAG, "Error while computing fileCachedDate");
            return false;
        }
        Date lastAvatarUpdateDate = DateTimeUtil.getDateFromStringStamp(lastAvatarUpdateDateStrg);
        if( lastAvatarUpdateDate == null) {
            Log.getLogger().warn(LOG_TAG, "Error while computing lastAvatarUpdateDate");
            return false;
        }

        long dateDiffMs = fileCachedDate.getTime() - lastAvatarUpdateDate.getTime();
        return dateDiffMs >= 0;

    }

    @Override
    public void clearCachePhoto(boolean refreshContactPhotos) {
        Log.getLogger().verbose(LOG_TAG, ">clearCachePhoto");

        Log.getLogger().verbose(LOG_TAG, "1) Deleting all Files stored");
        m_contactsCacheFile.deleteAllFiles();
        for(String fileName: m_contactsCacheFile.listInternalFiles()) {
            Log.getLogger().verbose(LOG_TAG, "File detected: "+fileName);
        }

        Log.getLogger().verbose(LOG_TAG, "2) Clean All Directory Contacts with Photo And Reload All Photos from Server");
        for(Contact contact : m_contactsCache.getCopyOfDataList()) {
            if( contact.isCorporate() ) {
                Log.getLogger().verbose(LOG_TAG, " Clearing photo for "+contact.getDisplayName4Log(""));
                contact.getDirectoryContact().setPhoto(null);
                if( refreshContactPhotos ) {
                    Log.getLogger().verbose(LOG_TAG, " Reloading photo for "+contact.getDisplayName4Log(""));
                    downloadContactAvatar(contact, true);
                }
            }
        }
    }

    @Override
    public synchronized void clearMobileLocalContacts() {

        List<Contact> contactsToRemove = findContactWithOnlyLocalAndClearLocalFromDirectory();

        if (contactsToRemove.size() > 0) {
            m_contactsCache.delete(contactsToRemove);
        }
    }

    @Override
    public void resolveDirectoryContacts(List<Contact> contacts) {
        Log.getLogger().verbose(LOG_TAG, ">resolveDirectoryContacts");

        List<String> unresolvedContactJids = new ArrayList<>();
        for(Contact contact : contacts) {
            if( contact.isCorporate() && StringsUtil.isNullOrEmpty(contact.getCompanyId()))
                unresolvedContactJids.add(contact.getImJabberId());
        }

        if( unresolvedContactJids.size() > 0) {
            m_directory.searchByJids(unresolvedContactJids, new IDirectoryProxy.IDirectoryListener() {
                @Override
                public void onCorporateSearchSuccess(List<DirectoryContact> searchResults) {
                    Log.getLogger().verbose(LOG_TAG, ">onCorporateSearchSuccess");

                    if (searchResults.size() > 0) {
                        for (DirectoryContact dirContact : searchResults) {
                            createContactIfNotExistOrUpdate(dirContact);
                        }
                    }
                }

                @Override
                public void onFailure() {
                    Log.getLogger().warn(LOG_TAG, ">onFailure");
                }
            });
        }
    }

    @Override
    public void resolveDirectoryContactById(final String roomId, final String contactId, final Room.IRoomParticipantListener roomParticipantListener) {
        Log.getLogger().verbose(LOG_TAG, ">resolveDirectoryContactById");

        Contact contact = getContactFromCorporateId(contactId);

        if (contact == null || !contact.isValid() ) {
            m_usersProxy.getUserData(contactId, new IUserProxy.IGetUserDataListener() {
                @Override
                public void onSuccess(Contact contact) {
                    Log.getLogger().verbose(LOG_TAG, ">resolveDirectoryContactById Success");
                    Contact contactCreated = createContactIfNotExistOrUpdate(contact.getDirectoryContact());
                    Log.getLogger().verbose(LOG_TAG, ">resolveDirectoryContactById contact = " + contactCreated.getDisplayName4Log(""));
                    if (roomParticipantListener != null)
                        roomParticipantListener.roomParticipantFoundSuccess(contactCreated);
                }

                @Override
                public void onFailure(RainbowServiceException exception) {
                    Log.getLogger().warn(LOG_TAG, "> resolveDirectoryContactById onFailure");
                    if (roomParticipantListener != null)
                        roomParticipantListener.roomParticipantFoundFailed(roomId, contactId);
                }
            });
        } else {
            Log.getLogger().verbose(LOG_TAG, ">resolveDirectoryContactById contact already found in cache= " + contact.getDisplayName4Log(""));
            if (roomParticipantListener != null)
                roomParticipantListener.roomParticipantFoundSuccess(contact);
        }
    }

    private List<Contact> findContactWithOnlyLocalAndClearLocalFromDirectory() {
        List<Contact> localContacts = new ArrayList<>();

        for (Contact contact : m_contactsCache.getCopyOfDataList()) {
            if (contact.isNative()) {
                if (contact.isCorporate()) {
                    // Contact has native and Corporate; Clear Native info inside
                    contact.setLocalContact(null);
                } else {
                    localContacts.add(contact);
                }
            }
        }

        return localContacts;
    }

    private void updatePhoto(Contact contact, Bitmap photo) {
        Log.getLogger().verbose(LOG_TAG, "Updating Photo for contact; " + contact.getDisplayName4Log(""));
        contact.setPhoto(photo);
        contact.notifyContactUpdated();
    }

    public void setLocalContactSearcher(ILocalContactSearcher m_localContactSearcher) {
        this.m_localContactSearcher = m_localContactSearcher;
    }

    @Override
    public Contact getUser() {
        return m_user;
    }

    @Override
    public ContactCacheFileMgr getContactsCacheFile() {
        return m_contactsCacheFile;
    }

    @Override
    public synchronized void addNewContact(Contact contact) {
        if (contact != null) {
            m_contactsCache.add(contact);
        }
    }

    @Override
    public void contactUpdated(Contact updatedContact) {

    }

    @Override
    public void onPresenceChanged(Contact contact, RainbowPresence presence) {
        Log.getLogger().verbose(LOG_TAG, ">onPresenceChanged");
        updateConnectedCache();
    }

    @Override
    public void onActionInProgress(boolean clickActionInProgress) {

    }

    @Override
    public List<Contact> getRostersWithoutSubscription() {
        List<Contact> contactList = m_rosters.getCopyOfDataList();

        List<Invitation> subscribingList = null;
        if (m_invitationMgr != null)
            subscribingList = m_invitationMgr.getPendingReceivedUserInvitationList();

        for (Invitation subscribingInvitation : subscribingList) {
            Contact invitingContact = subscribingInvitation.getInvitingContact();
            if (invitingContact!= null && contactList.contains(invitingContact)) {
                contactList.remove(invitingContact);
            }
        }

        return contactList;
    }

    @Override
    public boolean isLoggedInUser(Contact contact) {
        if (m_user != null && m_user.equals(contact)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isContactInformationVisible(Contact contact) {
        if (contact == null)
            return false;
        if (contact.isRoster() || contact.isNative()) {
            return true;
        }
        if (m_user == null)
            return false;

        if (contact.isCorporate() && contact.getDirectoryContact().getCompanyName() != null &&
                contact.getDirectoryContact().getCompanyName().equalsIgnoreCase(m_user.getDirectoryContact().getCompanyName()))
            return true;

        return false;
    }

    @Override
    public void setInvitationMgr(IInvitationMgr invitationMgr) {
        if( invitationMgr != null){
            m_invitationMgr = invitationMgr;
        }
    }

    @Override
    public void refreshUser(){

        m_usersProxy.getUserData(getUser().getCorporateId(), new IUserProxy.IGetUserDataListener() {
            @Override
            public void onSuccess(Contact contact) {
                Log.getLogger().verbose(LOG_TAG, ">refreshUser Success");
                Contact contactCreated = createContactIfNotExistOrUpdate(contact.getDirectoryContact());
                Log.getLogger().verbose(LOG_TAG, ">refreshUser contact = " + contactCreated.getDisplayName4Log(""));
            }

            @Override
            public void onFailure(RainbowServiceException exception) {
                Log.getLogger().warn(LOG_TAG, "> refreshUser onFailure");
            }
        });
        getUserSettings();
    }

    @Override
    public void refreshContactLastActivityDate(final Contact contact, final ILastActivityNotification lastActivityNotification) {
        Log.getLogger().verbose(LOG_TAG, ">refreshUserLastActivityDate");

        if (contact == null) {
            Log.getLogger().info(LOG_TAG, ">refreshUserLastActivityDate contact is null");
            return;
        }

        if (!contact.isRoster()) {
            Log.getLogger().info(LOG_TAG, ">refreshUserLastActivityDate contact is not roster");
            return;
        }

        if (contact.isBot()) {
            Log.getLogger().info(LOG_TAG, ">refreshUserLastActivityDate contact is Bot");
            return;
        }

        Thread myThread = new Thread() {
            public void run() {
                long idleTime = -1;

                Log.getLogger().info(LOG_TAG, ">refreshUserLastActivityDate inside Thread");
                idleTime = m_xmppContactMgr.getLastActivityIdleTimeOfContact(contact.getImJabberId());
                if (idleTime > 0) {
                    Log.getLogger().info(LOG_TAG, String.format(Locale.getDefault(), "LastActivity date for user with jid %s: %d", contact.getImJabberId(), idleTime));

                    Date now = new Date();
                    Date lastPresenceChangeDate = DateTimeUtil.dateByAddingDuration(now, Calendar.SECOND, (int) -idleTime);
                    Log.getLogger().info(LOG_TAG, " lastPresenceChangeDate=" + lastPresenceChangeDate);

                    contact.getDirectoryContact().setLastPresenceReceivedDate(lastPresenceChangeDate);
                }

                if (lastActivityNotification != null) {
                    Log.getLogger().verbose(LOG_TAG, ">refreshUserLastActivityDate; lastActivityNotification is not NULL");
                    if (idleTime >= 0)
                        lastActivityNotification.complete(idleTime);
                    else
                        lastActivityNotification.error();
                }
            }
        };
        myThread.start();
    }

}

package com.ale.rainbowsdk;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.IContactSearchListener;
import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.contact.RainbowPresence;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.invitation.Invitation;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.list.IItemListChangeListener;
import com.ale.infra.proxy.directory.IDirectoryProxy;
import com.ale.infra.proxy.notifications.INotificationProxy;
import com.ale.infra.proxy.users.IUserProxy;
import com.ale.listener.IRainbowContactManagementListener;
import com.ale.listener.IRainbowContactsListener;
import com.ale.listener.IRainbowInvitationManagementListener;
import com.ale.listener.IRainbowSentInvitationListener;
import com.ale.util.log.Log;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.List;

/**
 * This module is used to manage the contacts of the current session.
 */

public class Contacts
{
    private static final String LOG_TAG = "Contacts";

    private ArrayItemList<IRainbowContact> m_contacts;
    private ArrayItemList<IRainbowContact> m_sentInvitations;
    private ArrayItemList<IRainbowContact> m_receivedInvitations;
    private ArrayList<IRainbowContactsListener> m_listeners;
    private ArrayList<IRainbowInvitationManagementListener> m_receivedInvitationsListener;

    private IItemListChangeListener m_contactsListener = new IItemListChangeListener() {
        @Override
        public void dataChanged() {
            ArrayItemList<Contact> contacts = RainbowContext.getInfrastructure().getContactCacheMgr().getRosters();
            m_contacts.clear();
            for(Contact contact : contacts.getCopyOfDataList()) {
                m_contacts.add(contact);
            }
        }
    };

    private IItemListChangeListener m_receivedInvitationListener =new IItemListChangeListener() {
        @Override
        public void dataChanged() {
            List<IRainbowContact> contacts = getInvitationsReceivedWIthIRainbowContact(
                    RainbowContext.getInfrastructure().getInvitationMgr().getReceivedUserInvitationList().getCopyOfDataList()).getCopyOfDataList();
            m_receivedInvitations.replaceAll(contacts);
            notifyReceivedInvitationChenged(contacts);
        }
    };

    private IItemListChangeListener m_sentInvitationListener =new IItemListChangeListener() {
        @Override
        public void dataChanged() {
            List<IRainbowContact> contacts = getInvitationsSentWIthIRainbowContact(
                    RainbowContext.getInfrastructure().getInvitationMgr().getSentUserInvitationList().getCopyOfDataList()).getCopyOfDataList();
            m_sentInvitations.replaceAll(contacts);
        }
    };

    private Contact.ContactListener m_contactListener = new Contact.ContactListener() {
        @Override
        public void contactUpdated(Contact updatedContact) {
            notifyContactUpdated(updatedContact);
        }

        @Override
        public void onPresenceChanged(Contact contact, RainbowPresence presence) {
            notifyPresenceChanged(contact, presence);
        }

        @Override
        public void onActionInProgress(boolean clickActionInProgress) {

        }
    };

    Contacts() {
        m_contacts = new ArrayItemList<>();
        if (RainbowContext.getInfrastructure() != null
                && RainbowContext.getInfrastructure().getContactCacheMgr() != null
                && RainbowContext.getInfrastructure().getContactCacheMgr().getRosters() != null) {
            ArrayItemList<Contact> contacts = RainbowContext.getInfrastructure().getContactCacheMgr().getRosters();
            m_contacts.clear();
            for(Contact contact : contacts.getCopyOfDataList()) {
                m_contacts.add(contact);
            }
        }

        m_sentInvitations = new ArrayItemList<>();

        if (RainbowContext.getInfrastructure() != null
                && RainbowContext.getInfrastructure().getInvitationMgr() != null
                && RainbowContext.getInfrastructure().getInvitationMgr().getSentUserInvitationList() != null) {
            List<IRainbowContact> contacts = getInvitationsSentWIthIRainbowContact(
                    RainbowContext.getInfrastructure().getInvitationMgr().getSentUserInvitationList().getCopyOfDataList()).getCopyOfDataList();
            m_sentInvitations.replaceAll(contacts);

        }

        m_receivedInvitations = new ArrayItemList<>();

        if (RainbowContext.getInfrastructure() != null
                && RainbowContext.getInfrastructure().getInvitationMgr() != null
                && RainbowContext.getInfrastructure().getInvitationMgr().getReceivedUserInvitationList() != null) {
            List<IRainbowContact> contacts = getInvitationsReceivedWIthIRainbowContact(
                    RainbowContext.getInfrastructure().getInvitationMgr().getReceivedUserInvitationList().getCopyOfDataList()).getCopyOfDataList();
            m_receivedInvitations.replaceAll(contacts);

        }

        m_listeners = new ArrayList<>();
        m_receivedInvitationsListener = new ArrayList<>();
    }

    private ArrayItemList<IRainbowContact> getInvitationsReceivedWIthIRainbowContact(List<Invitation> invitations) {
        ArrayItemList<IRainbowContact> contactsInvited = new ArrayItemList<>();
        for(Invitation invitation : invitations) {
            Contact contact = RainbowContext.getInfrastructure().getContactCacheMgr().getContactFromCorporateId(invitation.getInvitingContact().getCorporateId());
            if (contact == null) {
                contact = new Contact();
                contact = RainbowContext.getInfrastructure().getContactCacheMgr().createContactIfNotExistOrUpdate(new DirectoryContact());
                contact.setLoginEmail(invitation.getInvitedUserEmail());
            }
            contact.setInvitationId(invitation.getId());
            contactsInvited.add((IRainbowContact) contact);
        }
        return contactsInvited;

    }

    private ArrayItemList<IRainbowContact> getInvitationsSentWIthIRainbowContact(List<Invitation> invitations) {
        ArrayItemList<IRainbowContact> contactsInvited = new ArrayItemList<>();
        for(Invitation invitation : invitations) {
            Contact contact = RainbowContext.getInfrastructure().getContactCacheMgr().getContactFromCorporateId(invitation.getInvitedContact().getCorporateId());
            if (contact == null) {
                contact = new Contact();
                contact = RainbowContext.getInfrastructure().getContactCacheMgr().createContactIfNotExistOrUpdate(new DirectoryContact());
                contact.setLoginEmail(invitation.getInvitedUserEmail());
            }
            contactsInvited.add((IRainbowContact) contact);
        }
        return contactsInvited;

    }

    public void registerListener(IRainbowContactsListener listener) {
        if (!m_listeners.contains(listener)) {
            m_listeners.add(listener);
        }
    }

    public void unregisterListener(IRainbowContactsListener listener) {
        if (m_listeners.contains(listener)) {
            m_listeners.remove(listener);
        }
    }

    public void registerInvitationsListener(IRainbowInvitationManagementListener listener) {
        if (!m_receivedInvitationsListener.contains(listener)) {
            m_receivedInvitationsListener.add(listener);
        }
    }

    public void unregisInvitationsterListener(IRainbowInvitationManagementListener listener) {
        if (m_receivedInvitationsListener.contains(listener)) {
            m_receivedInvitationsListener.remove(listener);
        }
    }

    private void notifyContactUpdated(IRainbowContact rainbowContact) {
        for (IRainbowContactsListener listener : m_listeners) {
            listener.onContactUpdated(rainbowContact);
        }
    }

    private void notifyPresenceChanged(IRainbowContact rainbowContact, RainbowPresence presence) {
        for (IRainbowContactsListener listener : m_listeners) {
            listener.onPresenceChanged(rainbowContact, presence);
        }
    }

    private void notifyReceivedInvitationChenged(List<IRainbowContact> contactsInviting) {
        for (IRainbowInvitationManagementListener listener : m_receivedInvitationsListener) {
            listener.onNewReceivedInvitation(contactsInviting);
        }
    }

    /**
     * Add the contact to the list
     *
     * @param contactCorporateId Contact to add
     * @param listener callbacks to prevent that the add contact has been done or not
     */
    public void addRainbowContactToRoster(final String contactCorporateId, final String contactEmail, final IRainbowSentInvitationListener listener)
    {
        if (RainbowContext.getInfrastructure() != null && RainbowContext.getInfrastructure().getInvitationMgr() != null) {
            RainbowContext.getInfrastructure().getInvitationMgr().sendUserVisibilityInvitation(contactCorporateId, new INotificationProxy.ISendEMailListener() {
                @Override
                public void onSuccess() {
                    Log.getLogger().verbose(LOG_TAG, ">addRainbowContactToRoster : the inviaition has been sent to the contact with email : " + contactEmail);
                    if (listener != null) {
                        listener.onInvitationSentSuccess(contactEmail);
                    }
                }

                @Override
                public void onFailure(RainbowServiceException exception) {
                    Log.getLogger().error(LOG_TAG, ">addRainbowContactToRoster : exception : " + exception.getDetailsMessage());
                    if (listener != null) {
                        listener.onInvitationSentError(exception);
                    }
                }
            });
        } else {
            Log.getLogger().error(LOG_TAG, ">addRainbowContactToRoster : getInfrasture or invitationMgr is null");
            if (listener != null) {
                listener.onInvitationError();
            }
        }

    }

    /**
     * Add the contact to the list
     *
     * @param contactFirstEmail Contact first email of the contact to add
     * @param contactMainEmail Contact first email of the contact to add
     * @param listener callbacks to prevent that the add contact has been done or not
     */
    public void inviteUserNotRegisterToRainbow(String contactCorporateId, final String contactFirstEmail, final String contactMainEmail, final IRainbowSentInvitationListener listener)
    {
        if (RainbowContext.getInfrastructure() != null && RainbowContext.getInfrastructure().getInvitationMgr() != null) {
            RainbowContext.getInfrastructure().getInvitationMgr().sendEmailUserInvitation(contactCorporateId, contactFirstEmail, contactMainEmail, new INotificationProxy.ISendEMailListener() {
                @Override
                public void onSuccess() {
                    Log.getLogger().verbose(LOG_TAG, ">inviteUserNotRegisterToRainbow : the inviaition has been sent to the contact with email : " + contactMainEmail);
                    if (listener != null) {
                        listener.onInvitationSentSuccess(contactMainEmail);
                    }
                }

                @Override
                public void onFailure(RainbowServiceException exception) {
                    Log.getLogger().error(LOG_TAG, ">inviteUserNotRegisterToRainbow : exception : " + exception.getDetailsMessage());
                    if (listener != null) {
                        listener.onInvitationSentError(exception);
                    }
                }
            });
        } else {
            Log.getLogger().error(LOG_TAG, ">inviteUserNotRegisterToRainbow : getInfrasture or invitationMgr is null");
            if (listener != null) {
                listener.onInvitationError();
            }
        }

    }

    /**
     *
     * @param invitationId : id of the onvitation object
     * @param listener : callbacks
     */
    public void acceptInvitation(String invitationId, final IRainbowInvitationManagementListener listener) {
        if (RainbowContext.getInfrastructure() != null
                && RainbowContext.getInfrastructure().getInvitationMgr() != null) {
            RainbowContext.getInfrastructure().getInvitationMgr().acceptUserInvitation(invitationId, new IUserProxy.IGetUserInvitationsListener() {
                @Override
                public void onSuccess(List<Invitation> invitationList) {
                    if (listener != null) {
                        listener.onAcceptSuccess();
                    }
                }

                @Override
                public void onFailure(RainbowServiceException exception) {
                    if (listener != null) {
                        listener.onError();
                    }
                }
            });
        }
    }


    /**
     *
     * @param invitationId : id of the onvitation object
     * @param listener ; callback
     */
    public void declineInvitation(String invitationId, final IRainbowInvitationManagementListener listener) {
        if (RainbowContext.getInfrastructure() != null
                && RainbowContext.getInfrastructure().getInvitationMgr() != null) {
            RainbowContext.getInfrastructure().getInvitationMgr().declineUserInvitation(invitationId, new IUserProxy.IGetUserInvitationsListener() {
                @Override
                public void onSuccess(List<Invitation> invitationList) {
                    if (listener != null) {
                        listener.onDeclineSuccess();
                    }
                }

                @Override
                public void onFailure(RainbowServiceException exception) {
                    if (listener != null) {
                        listener.onError();
                    }
                }
            });
        }
    }
    /**
     * Remove the contact from the list
     *
     * @param contactJid Contact jid of the contact to remove
     * @param contactEmail Contact email of the contact to remove
     * @param listener callbacks to prevent that the remove contact has been done or not
     */
    public void removeContactFromRoster(String contactJid, String contactEmail, IRainbowContactManagementListener listener)
    {
        if (RainbowContext.getInfrastructure() != null && RainbowContext.getInfrastructure().getXmppContactMgr() != null) {
            try {
                RainbowContext.getInfrastructure().getXmppContactMgr().deleteContactFromRoster(contactJid);
                if (listener != null) {
                    listener.OnContactRemoveSuccess(contactEmail);
                }
            } catch (SmackException.NotLoggedInException | XMPPException.XMPPErrorException | SmackException.NotConnectedException | SmackException.NoResponseException e1) {
                if (listener != null) {
                    listener.onContactRemovedError(e1);
                }
            }
        }
    }

    /**
     * Retrieve the list of contacts (no local contacts)
     *
     * @return A list of Contact
     */
    public ArrayItemList<IRainbowContact> getRainbowContacts()
    {
        return m_contacts;
    }

    /**
     * get IRainbowContactobject from corporateId
     * @param contactCorporateId : corporateId of the contact
     * @return the IrainbowContact object.
     */
    public IRainbowContact getContactFromId(String contactCorporateId) {
        return RainbowContext.getInfrastructure().getContactCacheMgr().
                getContactFromCorporateId(contactCorporateId);
    }

    /**
     * ger sent invitations for logged user
     * @return the list of sent onvitations
     */
    public  ArrayItemList<IRainbowContact> getSentInvitations() {
        return m_sentInvitations;
    }

    /**
     * ger received invitations for logged user
     * @return the list of received onvitations
     */
    public  ArrayItemList<IRainbowContact> getReceivedInvitations() {
        return m_receivedInvitations;
    }

    /**
     * ger pending received invitations for logged user
     * @return the list of received onvitations
     */
    public  List<IRainbowContact> getPendingReceivedInvitations() {
        if (RainbowContext.getInfrastructure() != null
                && RainbowContext.getInfrastructure().getInvitationMgr() != null
                && RainbowContext.getInfrastructure().getInvitationMgr().getPendingReceivedUserInvitationList() != null) {
            return getInvitationsReceivedWIthIRainbowContact(
                    RainbowContext.getInfrastructure().getInvitationMgr().getPendingReceivedUserInvitationList()).getCopyOfDataList();
        }
        return null;
    }

    /**
     * ger pending received invitations for logged user
     * @return the list of received onvitations
     */
    public  List<IRainbowContact> getPendingSentInvitations() {
        if (RainbowContext.getInfrastructure() != null
                && RainbowContext.getInfrastructure().getInvitationMgr() != null
                && RainbowContext.getInfrastructure().getInvitationMgr().getRegistrationPendingSentUserInvitationList() != null) {
            return getInvitationsSentWIthIRainbowContact(
                    RainbowContext.getInfrastructure().getInvitationMgr().getRegistrationPendingSentUserInvitationList()).getCopyOfDataList();
        }
        return null;
    }

    /**
     * get the onvitation objetc if needed
     * @param invitationId ; id of the invitation
     * @return the invitation object if exist
     */
    public Invitation getInvitationById(String invitationId) {
        return RainbowContext.getInfrastructure().getInvitationMgr().findReceivedUserInvitationWithInvitationId(invitationId);
    }

    /**
     * Retrieve the last user login saved in cache
     *
     * @return      The user login in cache
     */
    public String getUserLoginInCache() {
        if (RainbowContext.getPlatformServices().getApplicationData().getUserLogin() == null) {
            return "";
        } else {
            return RainbowContext.getPlatformServices().getApplicationData().getUserLogin();
        }
    }

    /**
     * Retrieve the last user password saved in cache
     *
     * @return      The user password in cache
     */
    public String getUserPasswordInCache() {
        if (RainbowContext.getPlatformServices().getApplicationData().getUserPassword() == null) {
            return "";
        } else {
            return RainbowContext.getPlatformServices().getApplicationData().getUserPassword();
        }
    }

    /**
     * Search a contact by his login
     *
     * @param login
     * @return
     */
    public IRainbowContact searchByLogin(String login)
    {
        return new Contact();
    }

    /**
     * Search a contact by his first or last name
     *
     * @param name : string used for searching user
     * @param listener : callback calledd after search result.
     */
    public void searchByName(String name, final IContactSearchListener listener)
    {
        if ((RainbowContext.getInfrastructure() != null) && (RainbowContext.getInfrastructure().getDirectoryProxy() != null)) {
            RainbowContext.getInfrastructure().getDirectoryProxy().searchByName(name, new IDirectoryProxy.IDirectoryListener() {
                @Override
                public void onCorporateSearchSuccess(List<DirectoryContact> searchResults) {
                    if (listener != null) {
                        listener.searchFinished(searchResults);
                    }

                }

                @Override
                public void onFailure() {
                    if (listener != null) {
                        listener.searchError();
                    }

                }
            });
        } else {
            if (listener != null) {
                listener.searchError();
            }
        }

            /*
        RainbowContext.getInfrastructure().getContactCacheMgr().searchDelayedByFullName(name, true, false, false, false, false, new IContactSearchListener() {
            @Override
            public void searchStarted() {
                listener.searchStarted();
            }

            @Override
            public void searchFinished() {
                // For the moment, only get IRainbowContact (no IRainbowConversation, no Room, ...)
                m_searchedContacts.clear();
                for (IDisplayable displayable : RainbowContext.getInfrastructure().getContactCacheMgr().getSearchedContacts().getCopyOfDataList()) {
                    if (displayable instanceof IRainbowContact) {
                        m_searchedContacts.add((IRainbowContact)displayable);
                    }
                }
                listener.searchFinished();
            }

            @Override
            public void searchError() {
                listener.searchError();
            }
        });*/
    }


    /**
     * Search onformation about user founded using his jid
     * @param imJabberId : rainbow contact which is searched to know more information of it
     * @param listener interface listener used to have method result.
     */
    public void searchByJid(String imJabberId, final IContactSearchListener listener) {
        if (RainbowContext.getInfrastructure() != null && RainbowContext.getInfrastructure().getDirectoryProxy() != null) {
            RainbowContext.getInfrastructure().getDirectoryProxy().searchByJid(RainbowSdk.instance().myProfile().getConnectedUser().getCorporateId(), imJabberId, new IDirectoryProxy.IDirectoryListener() {
                @Override
                public void onCorporateSearchSuccess(List<DirectoryContact> searchResults) {
                    if (listener != null) {
                        listener.searchFinished(searchResults);
                    }
                }

                @Override
                public void onFailure() {
                    if (listener != null) {
                        listener.searchError();
                    }
                }
            });
        }
    }

    protected void registerChangeListener() {
        RainbowContext.getInfrastructure().getContactCacheMgr().getRosters().registerChangeListener(m_contactsListener);
    }

    protected void unregisterChangeListener() {
        RainbowContext.getInfrastructure().getContactCacheMgr().getRosters().unregisterChangeListener(m_contactsListener);
    }

    void registerInvitationChangeListener() {
        RainbowContext.getInfrastructure().getInvitationMgr().getSentUserInvitationList().registerChangeListener(m_sentInvitationListener);
        RainbowContext.getInfrastructure().getInvitationMgr().getReceivedUserInvitationList().registerChangeListener(m_receivedInvitationListener);
    }

    void unregisterInvitationChangeListener() {
        RainbowContext.getInfrastructure().getInvitationMgr().getSentUserInvitationList().unregisterChangeListener(m_sentInvitationListener);
        RainbowContext.getInfrastructure().getInvitationMgr().getReceivedUserInvitationList().unregisterChangeListener(m_receivedInvitationListener);
    }


}

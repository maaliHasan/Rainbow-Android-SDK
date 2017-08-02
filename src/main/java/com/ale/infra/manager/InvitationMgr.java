package com.ale.infra.manager;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.EmailAddress;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.contact.RainbowPresence;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.invitation.Invitation;
import com.ale.infra.proxy.notifications.INotificationProxy;
import com.ale.infra.proxy.users.IUserProxy;
import com.ale.infra.xmpp.XmppConnection;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by trunk1 on 15/11/2016.
 */

public class InvitationMgr implements IInvitationMgr, XmppContactMgr.XmppContactMgrListener {
    private static final String LOG_TAG = InvitationMgr.class.getSimpleName();

    private final IContactCacheMgr m_contactCacheMgr;
    private final IPlatformServices m_platformServices;
    private IUserProxy m_usersProxy;
    private ArrayItemList<Invitation> m_sentEmailInvitationList;
    private ArrayItemList<Invitation> m_receivedInvitationList;
    private INotificationProxy m_notificationProxy;
    private XmppContactMgr m_xmppContactMgr;

    public InvitationMgr(IContactCacheMgr contactCacheMgr, IPlatformServices platformServices, IUserProxy userProxy, INotificationProxy notificationProxy) {
        m_contactCacheMgr = contactCacheMgr;
        m_platformServices = platformServices;
        m_usersProxy = userProxy;
        m_notificationProxy = notificationProxy;

        m_sentEmailInvitationList = new ArrayItemList<>();
        m_receivedInvitationList = new ArrayItemList<>();

        m_contactCacheMgr.setInvitationMgr(this);
    }

    @Override
    public ArrayItemList<Invitation> getReceivedUserInvitationList() {
        return m_receivedInvitationList;
    }

    @Override
    public ArrayItemList<Invitation> getSentUserInvitationList() {
        return m_sentEmailInvitationList;
    }

    @Override
    public void refreshReceivedUserInvitationList() {
        Log.getLogger().verbose(LOG_TAG, ">refreshReceivedUserInvitationList");
        Thread myThread = new Thread() {
            public void run() {
                m_usersProxy.getUserReceivedInvitations(m_platformServices.getApplicationData().getUserId(), new IUserProxy.IGetUserInvitationsListener() {
                    @Override
                    public void onSuccess(final List<Invitation> invitationList) {
                        Log.getLogger().verbose(LOG_TAG, ">refreshReceivedUserInvitationList received invitation Success :" + invitationList.size());
                        setReceivedUserInvitationList(invitationList);

                        for (Invitation invitation : invitationList) {
                            if (Invitation.InvitationStatus.PENDING == invitation.getStatus()) {
                                autoAcceptSameCompanyUserInvitation(invitation);
                            }
                        }
                    }

                    @Override
                    public void onFailure(RainbowServiceException exception) {
                        Log.getLogger().warn(LOG_TAG, "> refreshReceivedUserInvitationList received invitation onFailure");
                    }
                });
            }
        };
        myThread.start();
    }

    @Override
    public void refreshSentUserInvitationList() {
        Log.getLogger().verbose(LOG_TAG, ">refreshSentUserInvitationList");

        Thread myThread = new Thread() {
            public void run() {
                m_usersProxy.getUserSentInvitations(m_platformServices.getApplicationData().getUserId(), new IUserProxy.IGetUserInvitationsListener() {
                    @Override
                    public void onSuccess(final List<Invitation> invitationList) {
                        Log.getLogger().verbose(LOG_TAG, ">refreshSentUserInvitationList sent invitation Success : " + invitationList.size());
                        setSentUserInvitationList(invitationList);
                    }

                    @Override
                    public void onFailure(RainbowServiceException exception) {
                        Log.getLogger().warn(LOG_TAG, "> refreshSentUserInvitationList sent invitation onFailure");
                    }
                });
            }
        };
        myThread.start();
    }

    private synchronized void setReceivedUserInvitationList(List<Invitation> invitationList) {
        m_receivedInvitationList.clear();
        for (Invitation invitation : invitationList) {
            getReceivedUserContactInfo(invitation);
        }
        m_receivedInvitationList.addAll(invitationList);
    }

    private synchronized void setSentUserInvitationList(List<Invitation> invitationList) {
        m_sentEmailInvitationList.clear();
        for (Invitation invitation : invitationList) {
            getSentUserContactInfo(invitation);
        }
        m_sentEmailInvitationList.addAll(invitationList);
    }

    private void getSentUserContactInfo(final Invitation invitation) {
        //Inviting is always me
        Contact me = m_contactCacheMgr.getUser();
        Contact invitedContact = null;
        invitation.setInvitingContact(me);

        DirectoryContact dirContact = new DirectoryContact();
        if (!StringsUtil.isNullOrEmpty(invitation.getInvitedUserId())) {

            dirContact.setCorporateId(invitation.getInvitedUserId());
            invitedContact = m_contactCacheMgr.createContactIfNotExistOrUpdate(dirContact);
            invitation.setInvitedContact(invitedContact);
            if (StringsUtil.isNullOrEmpty(invitedContact.getImJabberId())) {

                Thread myThread = new Thread() {
                    public void run() {
                        m_usersProxy.getUserData(invitation.getInvitedUserId(), new IUserProxy.IGetUserDataListener() {
                            @Override
                            public synchronized void onSuccess(Contact foundUserContact) {
                                if (foundUserContact != null) {
                                    Contact updateContact = m_contactCacheMgr.createContactIfNotExistOrUpdate(foundUserContact.getDirectoryContact());
                                    invitation.setInvitedContact(updateContact);
                                    m_sentEmailInvitationList.fireDataChanged();
                                }
                            }

                            @Override
                            public void onFailure(RainbowServiceException exception) {
                                Log.getLogger().warn(LOG_TAG, ">getSentUserContactInfo invited onFailure");
                            }
                        });
                    }
                };
                myThread.start();
            }
        } else if (!StringsUtil.isNullOrEmpty(invitation.getInvitedUserEmail())) {

            dirContact.addEmailAddress(invitation.getInvitedUserEmail(), EmailAddress.EmailType.OTHER);
            invitedContact = m_contactCacheMgr.getContactFromEmail(dirContact.getEmailAddresses());
            if (invitedContact == null) {
                invitedContact = new Contact();
                invitedContact.setDirectoryContact(dirContact);
            }
            invitation.setInvitedContact(invitedContact);
        }
    }

    private void getReceivedUserContactInfo(final Invitation invitation) {
        //Invited is always me
        Contact me = m_contactCacheMgr.getUser();
        Contact invitingContact = null;
        invitation.setInvitedContact(me);

        DirectoryContact dirContact = new DirectoryContact();
        if (!StringsUtil.isNullOrEmpty(invitation.getInvitingUserId())) {

            dirContact.setCorporateId(invitation.getInvitingUserId());
            invitingContact = m_contactCacheMgr.createContactIfNotExistOrUpdate(dirContact);
            invitation.setInvitingContact(invitingContact);
            if (StringsUtil.isNullOrEmpty(invitingContact.getImJabberId())) {

                Thread myThread = new Thread() {
                    public void run() {
                        m_usersProxy.getUserData(invitation.getInvitingUserId(), new IUserProxy.IGetUserDataListener() {
                            @Override
                            public void onSuccess(Contact foundUserContact) {
                                if (foundUserContact != null) {
                                    Contact updateContact = m_contactCacheMgr.createContactIfNotExistOrUpdate(foundUserContact.getDirectoryContact());
                                    invitation.setInvitingContact(updateContact);
                                    m_receivedInvitationList.fireDataChanged();
                                }
                            }

                            @Override
                            public void onFailure(RainbowServiceException exception) {
                                Log.getLogger().warn(LOG_TAG, ">getReceivedUserContactInfo invited onFailure");
                            }
                        });
                    }
                };
                myThread.start();
            }
        } else if (!StringsUtil.isNullOrEmpty(invitation.getInvitingUserEmail())) {
            dirContact.addEmailAddress(invitation.getInvitedUserEmail(), EmailAddress.EmailType.OTHER);
            invitingContact.setDirectoryContact(dirContact);
            invitation.setInvitingContact(invitingContact);


            dirContact.addEmailAddress(invitation.getInvitedUserEmail(), EmailAddress.EmailType.OTHER);
            invitingContact = m_contactCacheMgr.getContactFromEmail(dirContact.getEmailAddresses());
            if (invitingContact == null) {
                invitingContact = new Contact();
                invitingContact.setDirectoryContact(dirContact);
            }
            invitation.setInvitedContact(invitingContact);
        }

    }

    /*----------------------------------------------------------------------*/
    @Override
    public void acceptUserInvitation(final String invitationId, final IUserProxy.IGetUserInvitationsListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">acceptUserInvitation");

        final Invitation invitation = findReceivedUserInvitationWithInvitationId(invitationId);

        if (invitation == null || Invitation.InvitationStatus.PENDING != invitation.getStatus()) {
            Log.getLogger().verbose(LOG_TAG, ">acceptUserInvitation no invitation");
            refreshReceivedUserInvitationList();
            if (listener != null) {
                listener.onFailure(new RainbowServiceException("invitation is null or not pending"));
            }
            return;
        }

        invitation.setClickActionInProgress(true);//action in progress
        Thread myThread = new Thread() {
            public void run() {
                m_usersProxy.postAcceptUserInvitation(m_platformServices.getApplicationData().getUserId(), invitation.getId(), new IUserProxy.IGetUserInvitationsListener() {
                    @Override
                    public void onSuccess(final List<Invitation> invitationList) {
                        refreshReceivedUserInvitationList();
                        if (listener != null)
                            listener.onSuccess(invitationList);
                        invitation.setClickActionInProgress(false);//end action in progress
                    }

                    @Override
                    public void onFailure(RainbowServiceException exception) {
                        Log.getLogger().warn(LOG_TAG, "> acceptUserInvitation invitation onFailure");
                        if (listener != null)
                            listener.onFailure(exception);
                        invitation.setClickActionInProgress(false);//end action in progress
                    }
                });
            }
        };
        myThread.start();
    }

    @Override
    public void declineUserInvitation(final String invitationId, final IUserProxy.IGetUserInvitationsListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">declineUserInvitation");

        final Invitation invitation = findReceivedUserInvitationWithInvitationId(invitationId);

        if (invitation == null || Invitation.InvitationStatus.PENDING != invitation.getStatus()) {
            Log.getLogger().verbose(LOG_TAG, ">declineUserInvitation no invitation");
            refreshReceivedUserInvitationList();
            if (listener != null) {
                listener.onFailure(new RainbowServiceException("invitation is null or not pending"));
            }
            return;
        }

        invitation.setClickActionInProgress(true);//action in progress
        Thread myThread = new Thread() {
            public void run() {
                m_usersProxy.postDeclineUserInvitation(m_platformServices.getApplicationData().getUserId(), invitation.getId(), new IUserProxy.IGetUserInvitationsListener() {
                    @Override
                    public void onSuccess(final List<Invitation> invitationList) {
                        refreshReceivedUserInvitationList();
                        if (listener != null)
                            listener.onSuccess(invitationList);
                        resetPresenceInProgressVisibilityPendingSentUser(invitation.getInvitedContact());
                        invitation.setClickActionInProgress(false);//end action in progress
                    }

                    @Override
                    public void onFailure(RainbowServiceException exception) {
                        Log.getLogger().warn(LOG_TAG, "> declineUserInvitation invitation onFailure");
                        if (listener != null)
                            listener.onFailure(exception);
                        resetPresenceInProgressVisibilityPendingSentUser(invitation.getInvitedContact());
                        invitation.setClickActionInProgress(false);//end action in progress
                    }
                });
            }
        };
        myThread.start();
    }

    @Override
    public void cancelUserInvitation(final String InvitedUserId, final IUserProxy.IGetUserInvitationsListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">cancelUserInvitation");

        final Invitation invitation = findPendingSentUserInvitationWithContact(InvitedUserId);
        if (invitation == null || Invitation.InvitationStatus.PENDING != invitation.getStatus()) {
            Log.getLogger().verbose(LOG_TAG, ">cancelUserInvitation no invitation");
            refreshSentUserInvitationList();
            if (listener != null) {
                listener.onFailure(new RainbowServiceException("invitation is null or not pending"));
            }
            return;
        }

        invitation.setClickActionInProgress(true);//action in progress
        Thread myThread = new Thread() {
            public void run() {
                m_usersProxy.cancelUserInvitation(m_platformServices.getApplicationData().getUserId(), invitation.getId(), new IUserProxy.IGetUserInvitationsListener() {
                    @Override
                    public void onSuccess(final List<Invitation> invitationList) {
                        refreshSentUserInvitationList();
                        if (listener != null)
                            listener.onSuccess(invitationList);
                        resetPresenceInProgressVisibilityPendingSentUser(invitation.getInvitedContact());
                        invitation.setClickActionInProgress(false);//end action in progress
                    }

                    @Override
                    public void onFailure(RainbowServiceException exception) {
                        Log.getLogger().warn(LOG_TAG, "> cancelUserInvitation invitation onFailure");
                        refreshSentUserInvitationList();
                        if (listener != null)
                            listener.onFailure(exception);
                        resetPresenceInProgressVisibilityPendingSentUser(invitation.getInvitedContact());
                        invitation.setClickActionInProgress(false);//end action in progress
                    }
                });
            }
        };
        myThread.start();
    }


    @Override
    public void sendUserVisibilityInvitation(final String contactCorporateId, final INotificationProxy.ISendEMailListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">sendUserVisibilityInvitation");

        if(contactCorporateId == null)
            return;

        Invitation invitation = findPendingSentUserInvitationWithContact(contactCorporateId);
        if ( invitation == null ){
            Log.getLogger().verbose(LOG_TAG, ">sendUserVisibilityInvitation");
            createUserInvitation(contactCorporateId, listener);
        } else {
            Log.getLogger().verbose(LOG_TAG, ">sendUserVisibilityInvitation re-invite");
            resendUserInvitation(invitation, listener);
        }
    }

    private void sendUserInvitation(final String contactCorporateId, String contactFirstEmail, String contactMainEmail, final INotificationProxy.ISendEMailListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">sendUserInvitation");

        if (contactFirstEmail == null && contactMainEmail == null) {
            Log.getLogger().verbose(LOG_TAG, ">sendUserInvitation no contact");
            refreshSentUserInvitationList();
            listener.onFailure(new RainbowServiceException("No contact provided"));
            return;
        }

        String emailAddress;
        if (StringsUtil.isNullOrEmpty(contactFirstEmail)) {
            Log.getLogger().verbose(LOG_TAG, ">sendUserInvitation no Email available. cancel");
            refreshSentUserInvitationList();
            listener.onFailure(new RainbowServiceException("No Email available"));
            return;
        } else {
            //check if Main address exists
            emailAddress = contactMainEmail;
            if (StringsUtil.isNullOrEmpty(emailAddress)) {
                Log.getLogger().verbose(LOG_TAG, "sendUserInvitation No MainEmail available > use one emailAddress");
                emailAddress = contactFirstEmail;
            }
        }

        final String finalEmailAddress = emailAddress;
        if (!StringsUtil.isNullOrEmpty(contactCorporateId)) {
            Contact contact = m_contactCacheMgr.getContactFromCorporateId(contactCorporateId);
            contact.setClickActionInProgress(true);
        } else {
            Contact contact = m_contactCacheMgr.getContactFromEmail(finalEmailAddress);
            contact.setClickActionInProgress(true);
        }
        Thread myThread = new Thread() {
            public void run() {
                m_notificationProxy.sendEmailInvite(m_platformServices.getApplicationData().getUserId(), finalEmailAddress, new INotificationProxy.ISendEMailListener() {
                    @Override
                    public synchronized void onSuccess() {
                        if (listener != null)
                            listener.onSuccess();
                        if (!StringsUtil.isNullOrEmpty(contactCorporateId)) {
                            Contact contact = m_contactCacheMgr.getContactFromCorporateId(contactCorporateId);
                            contact.setRosterInProgress(true);
                            contact.setPresence(null, RainbowPresence.SUBSCRIBE);
                            contact.setClickActionInProgress(false);
                        } else {
                            Contact contact = m_contactCacheMgr.getContactFromEmail(finalEmailAddress);
                            if (contact != null) {
                                contact.setRosterInProgress(true);
                                contact.setPresence(null, RainbowPresence.SUBSCRIBE);
                                contact.setClickActionInProgress(false);
                            }
                        }
                    }

                    @Override
                    public synchronized void onFailure(RainbowServiceException exception) {
                        if (listener != null)
                            listener.onFailure(exception);
                        if (contactCorporateId != null) {
                            Contact contact = m_contactCacheMgr.getContactFromCorporateId(contactCorporateId);
                            contact.setClickActionInProgress(false);//hide progressBar
                        }

                    }
                });
            }
        };
        myThread.start();

    }

    private void resendUserInvitation(final Invitation invitation, final INotificationProxy.ISendEMailListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">resendUserInvitation");

        if (invitation == null || Invitation.InvitationStatus.PENDING != invitation.getStatus()) {
            Log.getLogger().verbose(LOG_TAG, ">resendUserInvitation no invitation");
            refreshSentUserInvitationList();
            listener.onFailure(new RainbowServiceException("No invitation provided"));
            return;
        }

        invitation.setClickActionInProgress(true);//diplay progressBar
        Thread myThread = new Thread() {
            public void run() {
                m_notificationProxy.resendEmailInvite(m_platformServices.getApplicationData().getUserId(), invitation.getId(), new INotificationProxy.ISendEMailListener() {
                    @Override
                    public synchronized void onSuccess() {
                        if (listener != null)
                            listener.onSuccess();
                        invitation.setClickActionInProgress(false);//hide progressBar
                    }

                    @Override
                    public synchronized void onFailure(RainbowServiceException exception) {
                        if (listener != null)
                            listener.onFailure(exception);
                        invitation.setClickActionInProgress(false);//hide progressBar
                    }
                });
            }
        };
        myThread.start();
    }

    private void createUserInvitation(final String contactCorporateId, final INotificationProxy.ISendEMailListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">createUserInvitation");

        if (contactCorporateId == null) {
            Log.getLogger().verbose(LOG_TAG, ">createUserInvitation no contact");
            refreshSentUserInvitationList();
            listener.onFailure(new RainbowServiceException("No contact provided"));
            return;
        }

        final Contact contact = RainbowContext.getInfrastructure().getContactCacheMgr().getContactFromCorporateId(contactCorporateId);
        if (contact != null)
            contact.setClickActionInProgress(true);//diplay progressBar
        Thread myThread = new Thread() {
            public void run() {
                m_usersProxy.createUserInvitation(m_platformServices.getApplicationData().getUserId(), contactCorporateId, null, new IUserProxy.IGetUserInvitationsListener() {
                    @Override
                    public void onSuccess(List<Invitation> invitationList) {
                        if (listener != null)
                            listener.onSuccess();
                        setPresenceInProgressVisibilityPendingSentUser(contactCorporateId);
                        if (contact != null)
                            contact.setClickActionInProgress(false);//hide progressBar
                    }

                    @Override
                    public void onFailure(RainbowServiceException exception) {
                        if (listener != null)
                            listener.onFailure(exception);
                        if (contact != null)
                            contact.setClickActionInProgress(false);//hide progressBar
                    }
                });
            }
        };
        myThread.start();
    }

    private void autoAcceptSameCompanyUserInvitation(Invitation invitation) {
        Log.getLogger().info(LOG_TAG, ">autoAcceptSameCompanyUserInvitation");

        if (invitation == null || Invitation.InvitationStatus.PENDING != invitation.getStatus()) {
            Log.getLogger().verbose(LOG_TAG, ">autoAcceptSameCompanyUserInvitation no invitation");
            return;
        }

        if (invitation.getInvitingContact() == null) {
            Log.getLogger().verbose(LOG_TAG, ">autoAcceptSameCompanyUserInvitation no invitation contact");
            return;
        }

        if (invitation.getInvitingContact().getCompanyId().equals(m_contactCacheMgr.getUser().getCompanyId()) && !invitation.isClickActionInProgress()) { //if same company as me
            Log.getLogger().info(LOG_TAG, ">autoAcceptSameCompanyUserInvitation send auto acceptation");
            acceptUserInvitation(invitation.getId(), null);
        }
    }
    /*----------------------------------------------------------------------*/


    @Override
    public List<Invitation> getPendingReceivedUserInvitationList() {
        List<Invitation> pendingInvitationList = new ArrayList<>();

        for (Invitation invitation : m_receivedInvitationList.getCopyOfDataList()) {
            if ((invitation.getAcceptationDate() == null) && (invitation.getDeclinationDate() == null) &&
                    Invitation.InvitationStatus.PENDING == invitation.getStatus()) {
                pendingInvitationList.add(invitation);
            }
        }

        return pendingInvitationList;
    }

    @Override
    public List<Invitation> getRegistrationPendingSentUserInvitationList() {
        List<Invitation> pendingInvitationList = new ArrayList<>();

        for (Invitation invitation : m_sentEmailInvitationList.getCopyOfDataList()) {
            if ((invitation.getAcceptationDate() == null) && (invitation.getDeclinationDate() == null) &&
                    Invitation.InvitationType.REGISTRATION == invitation.getInvitationType() && Invitation.InvitationStatus.PENDING == invitation.getStatus()) {
                pendingInvitationList.add(invitation);
            }
        }

        return sortInvitationByLastNotificationDate(pendingInvitationList);
    }

    @Override
    public List<Invitation> getVisibilityPendingSentUserInvitationList() {
        List<Invitation> pendingInvitationList = new ArrayList<>();

        for (Invitation invitation : m_sentEmailInvitationList.getCopyOfDataList()) {
            if ((invitation.getAcceptationDate() == null) && (invitation.getDeclinationDate() == null) &&
                    Invitation.InvitationType.VISIBILITY == invitation.getInvitationType() && Invitation.InvitationStatus.PENDING == invitation.getStatus()) {
                setPresenceInProgressVisibilityPendingSentUser(invitation.getInvitedContact().getCorporateId());
                pendingInvitationList.add(invitation);
            }
        }

        return sortInvitationByLastNotificationDate(pendingInvitationList);
    }

    @Override
    public void sendEmailUserInvitation(String contactCorporateId, String contactFirstEmail, String contactMainEmail, INotificationProxy.ISendEMailListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">sendEmailUserInvitation");

        if(contactFirstEmail == null && contactMainEmail == null)
            return;

        //check if Main address exists
        String emailAddress = contactMainEmail;
        if (StringsUtil.isNullOrEmpty(emailAddress)) {
            Log.getLogger().verbose(LOG_TAG, "No MainEmail available > use one emailAddress");
            emailAddress = contactFirstEmail;
        }

        Invitation invitation = findPendingSentUserInvitationWithInvitedEmail(emailAddress);
        if ( invitation == null ){
            Log.getLogger().verbose(LOG_TAG, ">sendEmailUserInvitation");
            sendUserInvitation(contactCorporateId, contactFirstEmail, contactMainEmail, listener);
        } else {
            Log.getLogger().verbose(LOG_TAG, ">sendEmailUserInvitation re-invite");
            resendUserInvitation(invitation, listener);
        }
    }



    private void setPresenceInProgressVisibilityPendingSentUser(String contactCorporateId){
        Contact contact = RainbowContext.getInfrastructure().getContactCacheMgr().getContactFromCorporateId(contactCorporateId);
        if (contact != null) {
            contact.setRosterInProgress(true);
            contact.setPresence(null, RainbowPresence.SUBSCRIBE);
        }
    }

    private void resetPresenceInProgressVisibilityPendingSentUser(Contact contact){
        if (contact != null) {
            contact.setRosterInProgress(false);
            contact.setPresence(null, RainbowPresence.UNSUBSCRIBED);
        }
    }

    private List<Invitation> sortInvitationByLastNotificationDate(List<Invitation> invitationList) {
        ArrayList<Invitation> sortedInvitationList = new ArrayList<>();
        sortedInvitationList.addAll(invitationList);

        Collections.sort(sortedInvitationList, new Comparator<Invitation>() {
            @Override
            public int compare(Invitation invitation1, Invitation invitation2) {
                if (invitation1.getLastNotificationDate() == null || invitation2.getLastNotificationDate() == null)
                    return 0;
                return invitation2.getLastNotificationDate().compareTo(invitation1.getLastNotificationDate());
            }
        });

        return sortedInvitationList;
    }

    private Invitation findPendingSentUserInvitationWithInvitedEmail(String emailAddress) {
        for (Invitation invitation : m_sentEmailInvitationList.getCopyOfDataList()){
            if ( emailAddress.equals(invitation.getInvitedUserEmail()) && Invitation.InvitationStatus.PENDING == invitation.getStatus() ){
                return invitation;
            }
        }
        return null;
    }

    private Invitation findPendingSentUserInvitationWithContact(String  contactCorporateId) {
        for (Invitation invitation : m_sentEmailInvitationList.getCopyOfDataList()){
            if ( contactCorporateId.equals(invitation.getInvitedUserId())  && Invitation.InvitationStatus.PENDING == invitation.getStatus() ) {
                return invitation;
            }
        }
        return null;
    }

//    @Override
//    public Invitation findPendingReceivedUserInvitationWithContact(Contact contact) {
//        if (contact == null) return null;
//
//        for (Invitation invitation : m_receivedInvitationList.getCopyOfDataList()) {
//            if ( contact.getCorporateId().equals(invitation.getInvitingUserId())  && Invitation.InvitationStatus.PENDING == invitation.getStatus() ) {
//                return invitation;
//            }
//        }
//        return null;
//    }
//
//    @Override
//    public Invitation findSentUserInvitationWithInvitedCorporateId(String id) {
//        if (StringsUtil.isNullOrEmpty(id)) return null;
//
//        for (Invitation invitation : m_sentEmailInvitationList.getCopyOfDataList()){
//            if ( !StringsUtil.isNullOrEmpty(invitation.getInvitedUserId()) && invitation.getInvitedUserId().equals(id)){
//                return invitation;
//            }
//        }
//        return null;
//    }

    @Override
    public Invitation findReceivedUserInvitationWithInvitationId(String invitationId) {
        if (StringsUtil.isNullOrEmpty(invitationId)) return null;

        for (Invitation invitation : m_receivedInvitationList.getCopyOfDataList()) {
            if ( invitation.getId().equalsIgnoreCase(invitationId)){
                return invitation;
            }
        }
        return null;
    }


    public boolean isInvited(Contact contact) {
        if (!StringsUtil.isNullOrEmpty(contact.getEmailAddressForType(EmailAddress.EmailType.WORK)))
            return false;
        for (Invitation invit: m_sentEmailInvitationList.getCopyOfDataList()) {
            if (Invitation.InvitationStatus.PENDING == invit.getStatus()){
                if (contact.hasEmailAddress(invit.getInvitedUserEmail()))
                    return true;
            }
        }
        return false;

    }

    @Override
    public void setObserver(XmppConnection connection) {
        if( connection != null && connection.getXmppContactMgr() != null) {
            m_xmppContactMgr = connection.getXmppContactMgr();
            m_xmppContactMgr.registerChangeListener(this);
        }
    }

    @Override
    public void removeObserver(XmppConnection connection)
    {
        if(m_xmppContactMgr != null) {
            m_xmppContactMgr.unregisterChangeListener(this);
            m_xmppContactMgr = null;
        }
    }

    @Override
    public void onUserLoaded() {
        Log.getLogger().verbose(LOG_TAG, ">onUserLoaded");
        refreshReceivedUserInvitationList();
        refreshSentUserInvitationList();
    }

    @Override
    public void rostersChanged() {
    }
}

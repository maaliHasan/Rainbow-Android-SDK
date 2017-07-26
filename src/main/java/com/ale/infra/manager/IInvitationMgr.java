package com.ale.infra.manager;

import com.ale.infra.contact.Contact;
import com.ale.infra.invitation.Invitation;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.proxy.notifications.INotificationProxy;
import com.ale.infra.proxy.users.IUserProxy;
import com.ale.infra.xmpp.XmppConnection;

import java.util.List;

/**
 * Created by trunk1 on 15/11/2016.
 */

public interface IInvitationMgr
{
    void refreshReceivedUserInvitationList();

    void refreshSentUserInvitationList();

    void acceptUserInvitation(String invitationId, IUserProxy.IGetUserInvitationsListener listener);

    void declineUserInvitation(String invitationId, IUserProxy.IGetUserInvitationsListener listener);

    void cancelUserInvitation(String InvitedUserId, IUserProxy.IGetUserInvitationsListener listener);

    ArrayItemList<Invitation> getReceivedUserInvitationList();

    ArrayItemList<Invitation> getSentUserInvitationList();

    List<Invitation> getPendingReceivedUserInvitationList();

    List<Invitation> getRegistrationPendingSentUserInvitationList();

    List<Invitation> getVisibilityPendingSentUserInvitationList();

    Invitation findReceivedUserInvitationWithInvitationId(String invitationId);

    void sendEmailUserInvitation(String contactCorporateId, String contactFirstEmail, String contactMainEmail, INotificationProxy.ISendEMailListener listener);

    void sendUserVisibilityInvitation(String contactCorporateId, INotificationProxy.ISendEMailListener listener);

    boolean isInvited(Contact contact);

    void setObserver(XmppConnection connection);

    void removeObserver(XmppConnection m_connection);
}



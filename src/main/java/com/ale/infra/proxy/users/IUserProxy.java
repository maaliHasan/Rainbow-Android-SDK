package com.ale.infra.proxy.users;

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.invitation.CompanyInvitation;
import com.ale.infra.invitation.CompanyJoinRequest;
import com.ale.infra.invitation.Invitation;

import java.util.List;

/**
 * Created by grobert on 09/11/15.
 */
public interface IUserProxy
{
    void sendSelfRegisterPasswords(String email, String password, String temporaryToken, String firstname,
                                   String lastname, String country,String language,
                                   String invitationId, String joinCompanyInvitationId,
                                   boolean isInitialized, IUsersPasswordListener listener);

    void updateUser(String userId, String firstName, String lastName, String loginEmail, String nickName,
                    String title, String jobTitle, String emailWork, String emailHome, String officePhone,
                    String mobilePhone, String personalPhone,String personalMobilePhone, String country,String language,
                    boolean isInitialized, final IUsersListener listener);
    void createUserInvitation(String userId, String invitedUserId, String invitedEmailLogin,
                              final IGetUserInvitationsListener listener);
    void getUserData(String userId, final IGetUserDataListener listener);
    void getUserSettings(String userId, final IGetUserSettingsListener listener);
    void setUserSettings(String userId, String settings, String value, final IUsersListener listener);
    void getUserReceivedInvitations(String userId, final IGetUserInvitationsListener listener);
    void getUserSentInvitations(String userId, final IGetUserInvitationsListener listener);
    void postAcceptUserInvitation(String userId, String invitationId, final IGetUserInvitationsListener listener);
    void postDeclineUserInvitation(String userId, String invitationId, final IGetUserInvitationsListener listener);
    void cancelUserInvitation(String userId, String invitationId, final IGetUserInvitationsListener listener);
    void deleteDeleteUserInvitation(String userId, String invitationId, final IGetUserInvitationsListener listener);
    void getReceivedCompanyInvitations(String userId, final IGetCompanyInvitationsListener listener);
    void postAcceptCompanyInvitation(String userId, String companyInvitationId, final IGetCompanyInvitationsListener listener);
    void postDeclineCompanyInvitation(String userId, String companyInvitationId, final IGetCompanyInvitationsListener listener);
    void getJoinCompanyRequests(String userId, final IGetCompanyJoinRequestListener listener);
    void cancelJoinCompanyRequest(String userId, String joinCompanyRequestId, final IGetCompanyJoinRequestListener listener);
    void sendJoinCompanyRequest(String userId, String requestedCompanyId, String requestedCompanyAdminId , String requestedCompanyLinkId, final IGetCompanyJoinRequestListener listener);
    void resendJoinCompanyRequest(String userId, String joinCompanyRequestId, final IGetCompanyJoinRequestListener listener);

    void setContactCacheMgr(IContactCacheMgr contactCacheMgr);

    interface IUsersListener
    {
        void onSuccess();

        void onFailure(RainbowServiceException exception);
    }

    interface IUsersPasswordListener
    {
        void onSuccess(GetSelfRegisterUserPasswordResponse response);

        void onFailure(RainbowServiceException exception);
    }

    interface IGetUserDataListener
    {
        void onSuccess(Contact contact);

        void onFailure(RainbowServiceException exception);
    }

    interface IGetUserSettingsListener
    {
        void onSuccess(GetUserSettingsResponse settings);

        void onFailure(RainbowServiceException exception);
    }

    interface IGetUserInvitationsListener
    {
        void onSuccess(List<Invitation> invitationList);

        void onFailure(RainbowServiceException exception);
    }

    interface IGetCompanyInvitationsListener
    {
        void onSuccess(List<CompanyInvitation> invitationList);

        void onFailure(RainbowServiceException exception);
    }

    interface IGetCompanyJoinRequestListener
    {
        void onSuccess(List<CompanyJoinRequest> requestList);

        void onFailure(RainbowServiceException exception);
    }
}

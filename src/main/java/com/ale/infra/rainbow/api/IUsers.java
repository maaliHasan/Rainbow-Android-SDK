package com.ale.infra.rainbow.api;

import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.proxy.users.GetCompanyInvitationsResponse;
import com.ale.infra.proxy.company.GetCompanyJoinRequestResponse;
import com.ale.infra.proxy.users.GetSelfRegisterUserPasswordResponse;
import com.ale.infra.proxy.users.GetUserDataResponse;
import com.ale.infra.proxy.users.GetUserInvitationResponse;
import com.ale.infra.proxy.users.GetUserInvitationsResponse;
import com.ale.infra.proxy.users.GetUserSettingsResponse;

/**
 * Created by grobert on 09/11/15.
 */
public interface IUsers extends IRainbowService
{
    void sendSelfRegisterPasswords(String email, String password, String temporaryToken, String firstname, String lastname, String country,
                                   String language, String invitationId, String joinCompanyInvitationId, boolean isInitialized, IAsyncServiceResultCallback<GetSelfRegisterUserPasswordResponse> callback);

    void updateUser(String userId, String firstName, String lastName, String loginEmail, String nickName,
                    String title, String jobTitle, String emailWork, String emailHome, String officePhone,
                    String mobilePhone, String personalPhone,String personalMobilePhone,String country,String language,
                    boolean isInitialized, IAsyncServiceResultCallback<GetUserDataResponse> callback);

    void getUserData(String userId, IAsyncServiceResultCallback<GetUserDataResponse> callback);

    void createUserInvitation(String userId, String invitedUserId, String invitedEmailLogin, IAsyncServiceResultCallback<GetUserInvitationResponse> callback);

    void getUserSettings(String userId, IAsyncServiceResultCallback<GetUserSettingsResponse> callback);

    void setUserSettings(String userId, String settings, String value, IAsyncServiceVoidCallback callback);

    void getUserReceivedInvitations(String userId, IAsyncServiceResultCallback<GetUserInvitationsResponse> callback);
    void getUserSentInvitations(String userId, IAsyncServiceResultCallback<GetUserInvitationsResponse> callback);
    void postAcceptUserInvitation(String userId, String invitationId, IAsyncServiceResultCallback<GetUserInvitationsResponse> callback);
    void postDeclineUserInvitation(String userId, String invitationId, IAsyncServiceResultCallback<GetUserInvitationsResponse> callback);
    void deleteDeleteUserInvitation(String userId, String invitationId, IAsyncServiceResultCallback<GetUserInvitationsResponse> callback);
    void cancelUserInvitation(String userId, String invitationId, IAsyncServiceResultCallback<GetUserInvitationsResponse> callback);

    void getReceivedCompanyInvitations(String userId, IAsyncServiceResultCallback<GetCompanyInvitationsResponse> callback);
    void postAcceptCompanyInvitation(String userId, String companyInvitationId, IAsyncServiceResultCallback<GetCompanyInvitationsResponse> callback);
    void postDeclineCompanyInvitation(String userId, String companyInvitationId, IAsyncServiceResultCallback<GetCompanyInvitationsResponse> callback);

    void getJoinCompanyRequests(String userId, IAsyncServiceResultCallback<GetCompanyJoinRequestResponse> callback);
    void cancelJoinCompanyRequest(String userId, String joinCompanyRequestId, IAsyncServiceResultCallback<GetCompanyJoinRequestResponse> callback);
    void sendJoinCompanyRequest(String userId, String requestedCompanyId, String requestedCompanyAdminId , String requestedCompanyLinkId , IAsyncServiceResultCallback<GetCompanyJoinRequestResponse> callback);
    void resendJoinCompanyRequest(String userId, String joinCompanyRequestId, IAsyncServiceResultCallback<GetCompanyJoinRequestResponse> callback);
}

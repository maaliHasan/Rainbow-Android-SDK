package com.ale.infra.proxy.users;

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseVoid;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.invitation.CompanyJoinRequest;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.invitation.CompanyInvitation;
import com.ale.infra.invitation.Invitation;
import com.ale.infra.proxy.company.GetCompanyJoinRequestResponse;
import com.ale.infra.rainbow.api.IServicesFactory;
import com.ale.infra.rainbow.api.IUsers;
import com.ale.util.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by grobert on 09/11/15.
 */
public class UsersProxy implements IUserProxy
{
    private static final String LOG_TAG = "UsersProxy";

    private final IUsers m_usersService;
    private IContactCacheMgr m_contactCacheMgr;

    public UsersProxy(IServicesFactory servicesFactory, IRESTAsyncRequest httpClientFactory, IPlatformServices platformService)
    {
        Log.getLogger().info(LOG_TAG, "initialization");
        m_usersService = servicesFactory.createUsersService(httpClientFactory, platformService);
    }

    @Override
    public void setContactCacheMgr(IContactCacheMgr contactCacheMgr) {
        m_contactCacheMgr = contactCacheMgr;
    }

    @Override
    public void sendSelfRegisterPasswords(String email, String password, String temporaryToken,
                                          String firstName, String lastName, String country, String language,
                                          String invitationId, String joinCompanyInvitationId, boolean isInitialized, final IUsersPasswordListener listener) {
        Log.getLogger().verbose(LOG_TAG, "sendSelfRegisterPasswords with : " + email);

        m_usersService.sendSelfRegisterPasswords(email, password, temporaryToken, firstName, lastName,
                country, language, invitationId, joinCompanyInvitationId, isInitialized, new IAsyncServiceResultCallback<GetSelfRegisterUserPasswordResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetSelfRegisterUserPasswordResponse> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "Error while trying to sendSelfRegisterPasswords :" + asyncResult.getException().getMessage());

                    if (listener != null) {
                        listener.onFailure(asyncResult.getException());
                    }
                } else {
                    if (listener != null)
                        listener.onSuccess(asyncResult.getResult());
                }
            }
        });
    }


    @Override
    public void updateUser(String userId, String firstName, String lastName, String loginEmail, String nickName,
                           String title, String jobTitle, String emailWork, String emailHome, String officePhone,
                           String mobilePhone, String personalPhone, String personalMobilePhone, String country, String language,
                           boolean isInitialized, final IUsersListener listener)
    {
        Log.getLogger().verbose(LOG_TAG, ">updateUser");

        m_usersService.updateUser(userId, firstName, lastName, loginEmail, nickName, title, jobTitle,
                emailWork, emailHome,  officePhone, mobilePhone,  personalPhone, personalMobilePhone ,
                country, language, isInitialized, new IAsyncServiceResultCallback<GetUserDataResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetUserDataResponse> asyncResult) {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().info(LOG_TAG, "updateUser SUCCESS");
                    Contact contact = asyncResult.getResult().getContact();
                    m_contactCacheMgr.createContactIfNotExistOrUpdate(contact.getDirectoryContact());
                    if (listener != null)
                        listener.onSuccess();
                }
                else
                {
                    Log.getLogger().error(LOG_TAG, "Error while trying to updateUser :" + asyncResult.getException().getMessage());
                    if (listener != null) {
                        listener.onFailure(asyncResult.getException());
                    }
                }
            }
        });
    }

    @Override
    public void getUserData(String userId, final IGetUserDataListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">getUserData");

        m_usersService.getUserData(userId, new IAsyncServiceResultCallback<GetUserDataResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetUserDataResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().info(LOG_TAG, "getUserData SUCCESS");
                    Contact contact = asyncResult.getResult().getContact();
                    if (listener != null)
                        listener.onSuccess(contact);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "getUserData FAILURE", asyncResult.getException());
                    if (listener != null) {
                        listener.onFailure(asyncResult.getException());
                    }
                }
            }
        });
    }
    @Override
    public void getUserSettings(String userId, final IGetUserSettingsListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">getUserSettings");

        m_usersService.getUserSettings(userId, new IAsyncServiceResultCallback<GetUserSettingsResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetUserSettingsResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().info(LOG_TAG, "getUserSettings SUCCESS");
                    GetUserSettingsResponse settings = asyncResult.getResult();
                    if (listener != null)
                        listener.onSuccess(settings);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "getUserSettings FAILURE", asyncResult.getException());
                    if (listener != null) {
                        listener.onFailure(asyncResult.getException());
                    }
                }
            }
        });
    }

    @Override
    public void createUserInvitation(String userId, String invitedUserId, String invitedEmailLogin,
                              final IGetUserInvitationsListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">createUserInvitation");

        m_usersService.createUserInvitation(userId, invitedUserId, invitedEmailLogin, new IAsyncServiceResultCallback<GetUserInvitationResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetUserInvitationResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().info(LOG_TAG, "createUserInvitation SUCCESS");
                    List<Invitation> list = new ArrayList<Invitation>();
                    GetUserInvitationResponse result = asyncResult.getResult();
                    list.add(result.getInvitation());
                    if (listener != null)
                        listener.onSuccess(list);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "createUserInvitation FAILURE", asyncResult.getException());
                    if (listener != null) {
                        listener.onFailure(asyncResult.getException());
                    }
                }
            }
        });
    }

    @Override
    public void setUserSettings(String userId, String settings, String value, final IUsersListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">setUserSettings");

        m_usersService.setUserSettings(userId,settings,value,
                new IAsyncServiceVoidCallback() {
                    @Override
                    public void handleResult(AsyncServiceResponseVoid asyncResult) {
                        if (asyncResult.exceptionRaised()) {
                            Log.getLogger().error(LOG_TAG, "Error while trying to setUserSettings :" + asyncResult.getException().getMessage());

                            if (listener != null) {
                                listener.onFailure(asyncResult.getException());
                            }
                        } else {
                            if (listener != null)
                                listener.onSuccess();
                        }
                    }
                });
    }

    @Override
    public void getUserReceivedInvitations(String userId, final IGetUserInvitationsListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">getUserReceivedInvitations");

        m_usersService.getUserReceivedInvitations(userId, new IAsyncServiceResultCallback<GetUserInvitationsResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetUserInvitationsResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().info(LOG_TAG, "getUserReceivedInvitations SUCCESS");
                    List<Invitation> invitationList = asyncResult.getResult().getInvitationList();
                    if (listener != null)
                        listener.onSuccess(invitationList);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "getUserReceivedInvitations FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onFailure(asyncResult.getException());
                }
            }
        });
    }

    @Override
    public void getUserSentInvitations(String userId, final IGetUserInvitationsListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">getUserSentInvitations");

        m_usersService.getUserSentInvitations(userId, new IAsyncServiceResultCallback<GetUserInvitationsResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetUserInvitationsResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().info(LOG_TAG, "getUserSentInvitations SUCCESS");
                    List<Invitation> invitationList = asyncResult.getResult().getInvitationList();
                    if (listener != null)
                        listener.onSuccess(invitationList);
                } else {
                    Log.getLogger().info(LOG_TAG, "getUserSentInvitations FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onFailure(asyncResult.getException());
                }
            }
        });
    }

    @Override
    public void postAcceptUserInvitation(String userId, String invitationId, final IGetUserInvitationsListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">acceptUserInvitation");

        m_usersService.postAcceptUserInvitation(userId, invitationId, new IAsyncServiceResultCallback<GetUserInvitationsResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetUserInvitationsResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().info(LOG_TAG, "acceptUserInvitation SUCCESS");
                    List<Invitation> invitationList = asyncResult.getResult().getInvitationList();
                    if (listener != null)
                        listener.onSuccess(invitationList);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "acceptUserInvitation FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onFailure(asyncResult.getException());
                }
            }
        });
    }

    @Override
    public void postDeclineUserInvitation(String userId, String invitationId, final IGetUserInvitationsListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">declineUserInvitation");

        m_usersService.postDeclineUserInvitation(userId, invitationId, new IAsyncServiceResultCallback<GetUserInvitationsResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetUserInvitationsResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().info(LOG_TAG, "declineUserInvitation SUCCESS");
                    List<Invitation> invitationList = asyncResult.getResult().getInvitationList();
                    if (listener != null)
                        listener.onSuccess(invitationList);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "declineUserInvitation FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onFailure(asyncResult.getException());
                }
            }
        });
    }

    @Override
    public void cancelUserInvitation(String userId, String invitationId, final IGetUserInvitationsListener listener){
        Log.getLogger().verbose(LOG_TAG, ">cancelUserInvitation");

        m_usersService.cancelUserInvitation(userId, invitationId, new IAsyncServiceResultCallback<GetUserInvitationsResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetUserInvitationsResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().info(LOG_TAG, "cancelUserInvitation SUCCESS");
                    List<Invitation> invitationList = asyncResult.getResult().getInvitationList();
                    if (listener != null)
                        listener.onSuccess(invitationList);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "cancelUserInvitation FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onFailure(asyncResult.getException());
                }
            }
        });
    }

    @Override
    public void deleteDeleteUserInvitation(String userId, String invitationId, final IGetUserInvitationsListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">deleteDeleteUserInvitation");

        m_usersService.deleteDeleteUserInvitation(userId, invitationId, new IAsyncServiceResultCallback<GetUserInvitationsResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetUserInvitationsResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().info(LOG_TAG, "deleteDeleteUserInvitation SUCCESS");
                    List<Invitation> invitationList = asyncResult.getResult().getInvitationList();
                    if (listener != null)
                        listener.onSuccess(invitationList);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "deleteDeleteUserInvitation FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onFailure(asyncResult.getException());
                }
            }
        });
    }

    @Override
    public void getReceivedCompanyInvitations(String userId, final IGetCompanyInvitationsListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">getUserReceivedInvitations");

        m_usersService.getReceivedCompanyInvitations(userId, new IAsyncServiceResultCallback<GetCompanyInvitationsResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetCompanyInvitationsResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().info(LOG_TAG, "getReceivedCompanyInvitations SUCCESS");
                    List<CompanyInvitation> invitationList = asyncResult.getResult().getInvitationList();
                    if (listener != null)
                        listener.onSuccess(invitationList);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "getReceivedCompanyInvitations FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onFailure(asyncResult.getException());
                }
            }
        });
    }

    @Override
    public void postAcceptCompanyInvitation(String userId, String companyInvitationId, final IGetCompanyInvitationsListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">acceptUserInvitation");

        m_usersService.postAcceptCompanyInvitation(userId, companyInvitationId, new IAsyncServiceResultCallback<GetCompanyInvitationsResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetCompanyInvitationsResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().info(LOG_TAG, "postAcceptCompanyInvitation SUCCESS");
                    List<CompanyInvitation> invitationList = asyncResult.getResult().getInvitationList();
                    if (listener != null)
                        listener.onSuccess(invitationList);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "postAcceptCompanyInvitation FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onFailure(asyncResult.getException());
                }
            }
        });
    }

    @Override
    public void postDeclineCompanyInvitation(String userId, String companyInvitationId, final IGetCompanyInvitationsListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">declineUserInvitation");

        m_usersService.postDeclineCompanyInvitation(userId, companyInvitationId, new IAsyncServiceResultCallback<GetCompanyInvitationsResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetCompanyInvitationsResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().info(LOG_TAG, "postDeclineCompanyInvitation SUCCESS");
                    List<CompanyInvitation> invitationList = asyncResult.getResult().getInvitationList();
                    if (listener != null)
                        listener.onSuccess(invitationList);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "postDeclineCompanyInvitation FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onFailure(asyncResult.getException());
                }
            }
        });
    }

    @Override
    public void getJoinCompanyRequests(String userId, final IGetCompanyJoinRequestListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">getJoinCompanyRequests");

        m_usersService.getJoinCompanyRequests(userId, new IAsyncServiceResultCallback<GetCompanyJoinRequestResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetCompanyJoinRequestResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().info(LOG_TAG, "getJoinCompanyRequests SUCCESS");
                    List<CompanyJoinRequest> requestList = asyncResult.getResult().getRequestList();
                    if (listener != null)
                        listener.onSuccess(requestList);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "getJoinCompanyRequests FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onFailure(asyncResult.getException());
                }
            }
        });
    }

    @Override
    public void cancelJoinCompanyRequest(String userId, String joinCompanyRequestId, final IGetCompanyJoinRequestListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">cancelJoinCompanyRequests");

        m_usersService.cancelJoinCompanyRequest(userId, joinCompanyRequestId, new IAsyncServiceResultCallback<GetCompanyJoinRequestResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetCompanyJoinRequestResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().info(LOG_TAG, "cancelJoinCompanyRequests SUCCESS");
                    List<CompanyJoinRequest> requestList = asyncResult.getResult().getRequestList();
                    if (listener != null)
                        listener.onSuccess(requestList);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "cancelJoinCompanyRequests FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onFailure(asyncResult.getException());
                }
            }
        });
    }

    @Override
    public void sendJoinCompanyRequest(String userId, String requestedCompanyId, String requestedCompanyAdminId , String requestedCompanyLinkId, final IGetCompanyJoinRequestListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">getJoinCompanyRequests");

        m_usersService.sendJoinCompanyRequest(userId, requestedCompanyId, requestedCompanyAdminId, requestedCompanyLinkId, new IAsyncServiceResultCallback<GetCompanyJoinRequestResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetCompanyJoinRequestResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().info(LOG_TAG, "getJoinCompanyRequests SUCCESS");
                    List<CompanyJoinRequest> requestList = asyncResult.getResult().getRequestList();
                    if (listener != null)
                        listener.onSuccess(requestList);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "getJoinCompanyRequests FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onFailure(asyncResult.getException());
                }
            }
        });
    }

    @Override
    public void resendJoinCompanyRequest(String userId, String joinCompanyRequestId, final IGetCompanyJoinRequestListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">getJoinCompanyRequests");

        m_usersService.resendJoinCompanyRequest(userId, joinCompanyRequestId, new IAsyncServiceResultCallback<GetCompanyJoinRequestResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetCompanyJoinRequestResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().info(LOG_TAG, "getJoinCompanyRequests SUCCESS");
                    List<CompanyJoinRequest> requestList = asyncResult.getResult().getRequestList();
                    if (listener != null)
                        listener.onSuccess(requestList);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "getJoinCompanyRequests FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onFailure(asyncResult.getException());
                }
            }
        });
    }
}

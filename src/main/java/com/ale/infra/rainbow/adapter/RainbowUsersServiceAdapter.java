package com.ale.infra.rainbow.adapter;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.RESTResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseVoid;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.framework.RainbowServiceTag;

import com.ale.infra.proxy.users.GetCompanyInvitationsResponse;
import com.ale.infra.proxy.company.GetCompanyJoinRequestResponse;
import com.ale.infra.proxy.users.GetSelfRegisterUserPasswordResponse;
import com.ale.infra.proxy.users.GetUserDataResponse;
import com.ale.infra.proxy.users.GetUserInvitationResponse;
import com.ale.infra.proxy.users.GetUserInvitationsResponse;
import com.ale.infra.proxy.users.GetUserSettingsResponse;
import com.ale.infra.rainbow.api.ApisConstants;
import com.ale.infra.rainbow.api.IUsers;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by grobert on 09/11/15.
 */
public class RainbowUsersServiceAdapter implements IUsers
{
    private static final String LOG_TAG = "RainbowUsersServiceAdapter";

    private final IRESTAsyncRequest m_restAsyncRequest;
    private IPlatformServices m_platformServices;

    public RainbowUsersServiceAdapter(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices)
    {
        m_restAsyncRequest = restAsyncRequest;
        m_platformServices = platformServices;
    }

    private String getUrl()
    {
        String url = RainbowContext.getPlatformServices().getApplicationData().getServerUrl();
        if (url == null)
        {
            url = StringsUtil.EMPTY;
        }
        return url;
    }

    @Override
    public RainbowServiceTag getTag()
    {
        return RainbowServiceTag.USERS;
    }

    @Override
    public void sendSelfRegisterPasswords(String email, String password, String temporaryToken,
                                          String firstName, String lastName, String country,
                                          String language, String invitationId, String joinCompanyInvitationId, boolean isInitialized,
                                          final IAsyncServiceResultCallback<GetSelfRegisterUserPasswordResponse> callback)
    {
        Log.getLogger().verbose(LOG_TAG, ">sendSelfRegisterPasswords");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.SELF_REGISTER_USER);

        JSONObject restBody = new JSONObject();
        try
        {
            restBody.put("loginEmail", email);
            restBody.put("password", password);
            restBody.put("temporaryToken", temporaryToken);
            restBody.put("firstName", firstName);
            restBody.put("lastName", lastName);
            if( !StringsUtil.isNullOrEmpty(country))
                restBody.put("country", country);
            if( !StringsUtil.isNullOrEmpty(invitationId))
                restBody.put("invitationId", invitationId);
            if( !StringsUtil.isNullOrEmpty(joinCompanyInvitationId))
                restBody.put("joinCompanyInvitationId", joinCompanyInvitationId);

            if( !StringsUtil.isNullOrEmpty(language))
                restBody.put("language", language);
            restBody.put("isInitialized", isInitialized);
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "sendSelfRegisterPasswords failed.");
                    Log.getLogger().verbose(LOG_TAG, ">sendSelfRegisterPasswords : " + asyncResult.getException().getDetailsMessage());
                    notifySendSelfRegisterPasswordResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "sendSelfRegisterPasswords success.");
                    try {
                        notifySendSelfRegisterPasswordResult(callback, null, new GetSelfRegisterUserPasswordResponse(asyncResult.getResult().getResponse()));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST Conversation result");
                        notifySendSelfRegisterPasswordResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void updateUser(String userId, String firstName, String lastName, String loginEmail, String nickName,
                           String title, String jobTitle, String emailWork, String emailHome, String officePhone,
                           String mobilePhone, String personalPhone,String personalMobilePhone,String country, String language,
                           boolean isInitialized, final IAsyncServiceResultCallback<GetUserDataResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">updateUser");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(userId);

        JSONObject restBody = new JSONObject();
        try
        {
            if(!StringsUtil.isNullOrEmpty(firstName))
                restBody.put("firstName", firstName);
            if(!StringsUtil.isNullOrEmpty(lastName))
                restBody.put("lastName", lastName);
            if(nickName != null)
                restBody.put("nickName", nickName);
            if(title != null)
                restBody.put("title", title);
            if(jobTitle != null)
                restBody.put("jobTitle", jobTitle);
            if( emailHome !=  null || !StringsUtil.isNullOrEmpty(emailWork) )
            {
                JSONArray emails = new JSONArray();

                JSONObject email = addEmailInfoInJson(emailHome, "home");
                if(email != null)
                    emails.put(email);
                email = addEmailInfoInJson(emailWork, "work");
                if(email != null)
                    emails.put(email);

                restBody.put("emails", emails);
            }
            if( !StringsUtil.isNullOrEmpty(loginEmail))
                restBody.put("loginEmail", loginEmail);


            JSONArray phones = new JSONArray();
            JSONObject phone = null;
            if (!StringsUtil.isNullOrEmpty(officePhone)){
                phone = addPhoneInfoInJson(officePhone, "work", "landline");
                if (phone != null)
                    phones.put(phone);
            }

            if (!StringsUtil.isNullOrEmpty(mobilePhone)) {
                phone = addPhoneInfoInJson(mobilePhone, "work", "mobile");
                if (phone != null)
                    phones.put(phone);
            }

            if (!StringsUtil.isNullOrEmpty(personalPhone)) {
                phone = addPhoneInfoInJson(personalPhone, "home", "landline");
                if (phone != null)
                    phones.put(phone);
            }

            if (!StringsUtil.isNullOrEmpty(personalMobilePhone)) {
                phone = addPhoneInfoInJson(personalMobilePhone, "home", "mobile");
                if (phone != null)
                    phones.put(phone);
            }
            restBody.put("phoneNumbers", phones);

            if( !StringsUtil.isNullOrEmpty(country))
                restBody.put("country", country);

            if( !StringsUtil.isNullOrEmpty(language))
                restBody.put("language", language);

            restBody.put("isInitialized", isInitialized);
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }
        Log.getLogger().verbose(LOG_TAG, "Filling User with ; "+restBody);

        m_restAsyncRequest.sendPutRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "updateUser failed.");
                    notifyGetUserDataResult(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "updateUser success : ");
                        notifyGetUserDataResult(callback, null, new GetUserDataResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "updateUser Impossible to parse REST Conversation result");
                        notifyGetUserDataResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void getUserData(String userId, final  IAsyncServiceResultCallback<GetUserDataResponse> callback) {
        if( StringsUtil.isNullOrEmpty(userId)) {
            Log.getLogger().warn(LOG_TAG, "getUserData: userId is Empty");
            return;
        }
        Log.getLogger().verbose(LOG_TAG, ">getUserData : " + userId);

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(userId);

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "getUserData failed" + asyncResult.getException());
                    notifyGetUserDataResult(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "getUserData success");
                        notifyGetUserDataResult(callback, null, new GetUserDataResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "getUserData Impossible to parse REST Conversation result");
                        notifyGetUserDataResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void getUserSettings(String userId,final  IAsyncServiceResultCallback<GetUserSettingsResponse> callback) {
        if( StringsUtil.isNullOrEmpty(userId)) {
            Log.getLogger().warn(LOG_TAG, "getUserSettings: userId is Empty");
            return;
        }
        Log.getLogger().verbose(LOG_TAG, ">getUserSettings : " + userId);

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(userId);
        restUrl.append(ApisConstants.SETTINGS);

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "getUserSettings failed" + asyncResult.getException());
                    notifyGetUserSettingsResult(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "getUserSettings success");
                        notifyGetUserSettingsResult(callback, null, new GetUserSettingsResponse(m_platformServices, asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "getUserSettings Impossible to parse REST result");
                        notifyGetUserSettingsResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void setUserSettings(String userId, String settings, String value , final  IAsyncServiceVoidCallback callback) {
        if( StringsUtil.isNullOrEmpty(userId)) {
            Log.getLogger().warn(LOG_TAG, "setUserSettings: userId is Empty");
            return;
        }
        Log.getLogger().verbose(LOG_TAG, ">setUserSettings : " + userId);

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(userId);
        restUrl.append(ApisConstants.SETTINGS);

        JSONObject restBody = new JSONObject();
        try {
            restBody.put(settings, value);
        } catch (Exception ex) {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }
        Log.getLogger().verbose(LOG_TAG, "Filling User with ; " + restBody);

        m_restAsyncRequest.sendPutRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "setUserSettings failed.");
                    notifyVoidResult(callback, asyncResult.getException());
                } else {
                    Log.getLogger().info(LOG_TAG, "setUserSettings success.");
                    notifyVoidResult(callback, null);
                }
            }
        });
    }


    @Override
    public void getUserReceivedInvitations(String userId, final IAsyncServiceResultCallback<GetUserInvitationsResponse> callback) {
        if( StringsUtil.isNullOrEmpty(userId)) {
            Log.getLogger().warn(LOG_TAG, "getUserReceivedInvitations: userId is Empty");
            return;
        }
        Log.getLogger().verbose(LOG_TAG, ">getUserReceivedInvitations : " + userId);

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(userId);
        restUrl.append(ApisConstants.RECEIVED_INVITATIONS);

        try
        {
            restUrl.append("?format=full");
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "getUserReceivedInvitations Error while filling JSON Object");
        }

        Log.getLogger().info(LOG_TAG, "getUserReceivedInvitations URL :" + restUrl.toString());

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "getUserReceivedInvitations failed : " + asyncResult.getException());
                    notifyGetUserReceivedInvitations(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "getUserReceivedInvitations success");
                        notifyGetUserReceivedInvitations(callback, null, new GetUserInvitationsResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "getUserReceivedInvitations Impossible to parse REST Conversation result");
                        notifyGetUserReceivedInvitations(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void getUserSentInvitations(String userId, final IAsyncServiceResultCallback<GetUserInvitationsResponse> callback) {
        if( StringsUtil.isNullOrEmpty(userId)) {
            Log.getLogger().warn(LOG_TAG, "getUserSentInvitations: userId is Empty");
            return;
        }
        Log.getLogger().verbose(LOG_TAG, ">getUserSentInvitations : " + userId);

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(userId);
        restUrl.append(ApisConstants.SENT_INVITATIONS);
        try
        {
            restUrl.append("?format=full");
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "getUserSentInvitations Error while filling JSON Object");
        }

        Log.getLogger().info(LOG_TAG, "getUserSentInvitations URL :" + restUrl.toString());

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "getUserSentInvitations failed : " + asyncResult.getException());
                    notifyGetUserReceivedInvitations(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "getUserSentInvitations success");
                        notifyGetUserReceivedInvitations(callback, null, new GetUserInvitationsResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "getUserSentInvitations Impossible to parse REST Conversation result");
                        notifyGetUserReceivedInvitations(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void postAcceptUserInvitation(String userId, String invitationId, final IAsyncServiceResultCallback<GetUserInvitationsResponse> callback) {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(userId);
        restUrl.append(ApisConstants.INVITATIONS);
        restUrl.append(invitationId);
        restUrl.append(ApisConstants.ACCEPT);

        JSONObject restBody = new JSONObject();

        Log.getLogger().info(LOG_TAG, "acceptUserInvitation URL :" + restUrl.toString());

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "acceptUserInvitation failed : " + asyncResult.getException());
                    notifyAcceptedUserInvitation(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "acceptUserInvitation success");
                        notifyAcceptedUserInvitation(callback, null, new GetUserInvitationsResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "acceptUserInvitation Impossible to parse REST Conversation result");
                        notifyAcceptedUserInvitation(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void postDeclineUserInvitation(String userId, String invitationId, final IAsyncServiceResultCallback<GetUserInvitationsResponse> callback) {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(userId);
        restUrl.append(ApisConstants.INVITATIONS);
        restUrl.append(invitationId);
        restUrl.append(ApisConstants.DECLINE);

        JSONObject restBody = new JSONObject();

        Log.getLogger().info(LOG_TAG, "declineUserInvitation URL :" + restUrl.toString());

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody , new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "declineUserInvitation failed : " + asyncResult.getException());
                    notifyDeclinedUserInvitation(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "declineUserInvitation success");
                        notifyDeclinedUserInvitation(callback, null, new GetUserInvitationsResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "declineUserInvitation Impossible to parse REST Conversation result");
                        notifyDeclinedUserInvitation(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }


    @Override
    public void cancelUserInvitation(String userId, String invitationId, final IAsyncServiceResultCallback<GetUserInvitationsResponse> callback) {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(userId);
        restUrl.append(ApisConstants.INVITATIONS);
        restUrl.append(invitationId);
        restUrl.append(ApisConstants.CANCEL);

        JSONObject restBody = new JSONObject();

        Log.getLogger().info(LOG_TAG, "cancelUserInvitation URL :" + restUrl.toString());

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody , new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "cancelUserInvitation failed : " + asyncResult.getException());
                    notifyDeclinedUserInvitation(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "cancelUserInvitation success");
                        notifyDeclinedUserInvitation(callback, null, new GetUserInvitationsResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "cancelUserInvitation Impossible to parse REST Conversation result");
                        notifyDeclinedUserInvitation(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void deleteDeleteUserInvitation(String userId, String invitationId,  final IAsyncServiceResultCallback<GetUserInvitationsResponse> callback) {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(userId);
        restUrl.append(ApisConstants.INVITATIONS);
        restUrl.append(invitationId);

        JSONObject restBody = new JSONObject();

        Log.getLogger().info(LOG_TAG, "deleteDeleteUserInvitation URL :" + restUrl.toString());

        m_restAsyncRequest.sendDeleteRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "deleteDeleteUserInvitation failed : " + asyncResult.getException());
                    notifyDeleteUserInvitation(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "deleteDeleteUserInvitation success");
                        notifyDeleteUserInvitation(callback, null, new GetUserInvitationsResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "deleteDeleteUserInvitation Impossible to parse REST Conversation result");
                        notifyDeleteUserInvitation(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void getReceivedCompanyInvitations(String userId, final IAsyncServiceResultCallback<GetCompanyInvitationsResponse> callback) {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(userId);
        restUrl.append(ApisConstants.JOIN_COMPANY_RECEIVED_INVITATIONS);

        try
        {
            restUrl.append("?format=full");
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "getReceivedCompanyInvitations Error while filling JSON Object");
        }

        Log.getLogger().info(LOG_TAG, "getReceivedCompanyInvitations URL :" + restUrl.toString());

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "getReceivedCompanyInvitations failed : " + asyncResult.getException());
                    notifyGetReceivedCompanyInvitations(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "getReceivedCompanyInvitations success");
                        notifyGetReceivedCompanyInvitations(callback, null, new GetCompanyInvitationsResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "getReceivedCompanyInvitations Impossible to parse REST Conversation result");
                        notifyGetReceivedCompanyInvitations(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void postAcceptCompanyInvitation(String userId, String companyInvitationId, final IAsyncServiceResultCallback<GetCompanyInvitationsResponse> callback) {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(userId);
        restUrl.append(ApisConstants.JOIN_COMPANY_RECEIVED_INVITATIONS);
        restUrl.append("/" + companyInvitationId);
        restUrl.append(ApisConstants.ACCEPT);

        JSONObject restBody = new JSONObject();

        Log.getLogger().info(LOG_TAG, "postAcceptCompanyInvitation URL :" + restUrl.toString());

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "postAcceptCompanyInvitation failed : " + asyncResult.getException());
                    notifyAcceptedCompanyInvitation(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "postAcceptCompanyInvitation success");
                        notifyAcceptedCompanyInvitation(callback, null, new GetCompanyInvitationsResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "postAcceptCompanyInvitation Impossible to parse REST Conversation result");
                        notifyAcceptedCompanyInvitation(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void postDeclineCompanyInvitation(String userId, String companyInvitationId, final IAsyncServiceResultCallback<GetCompanyInvitationsResponse> callback) {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(userId);
        restUrl.append(ApisConstants.JOIN_COMPANY_RECEIVED_INVITATIONS);
        restUrl.append("/" + companyInvitationId);
        restUrl.append(ApisConstants.DECLINE);

        JSONObject restBody = new JSONObject();

        Log.getLogger().info(LOG_TAG, "postDeclineCompanyInvitation URL :" + restUrl.toString());

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody , new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "postDeclineCompanyInvitation failed : " + asyncResult.getException());
                    notifyDeclinedCompanyInvitation(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "postDeclineCompanyInvitation success");
                        notifyDeclinedCompanyInvitation(callback, null, new GetCompanyInvitationsResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "postDeclineCompanyInvitation Impossible to parse REST Conversation result");
                        notifyDeclinedCompanyInvitation(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void createUserInvitation(String userId, String invitedUserId, String invitedEmailLogin,
                                     final IAsyncServiceResultCallback<GetUserInvitationResponse> callback){
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(userId);
        restUrl.append("/invitations");
        JSONObject restBody = new JSONObject();
        try
        {
            if (!StringsUtil.isNullOrEmpty(invitedEmailLogin))
                restBody.put("email", invitedEmailLogin);

            if (!StringsUtil.isNullOrEmpty(invitedUserId))
                restBody.put("invitedUserId", invitedUserId);
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody , new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "createUserInvitation failed : " + asyncResult.getException() + " " + asyncResult.getException().getDetailsMessage());
                    notifyUserInvitation(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "createUserInvitation success");
                        notifyUserInvitation(callback, null, new GetUserInvitationResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "createUserInvitation Impossible to parse REST Conversation result");
                        notifyUserInvitation(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void getJoinCompanyRequests(String userId, final IAsyncServiceResultCallback<GetCompanyJoinRequestResponse> callback) {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(userId);
        restUrl.append(ApisConstants.JOIN_COMPANY_REQUESTED_INVITATIONS);

        try
        {
            restUrl.append("?format=full");
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "getReceivedCompanyInvitations Error while filling JSON Object");
        }
        //api/rainbow/enduser/v1.0/users/{userId}/join-companies/requests
        Log.getLogger().info(LOG_TAG, "getJoinCompanyRequests URL :" + restUrl.toString());

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "postAcceptCompanyInvitation failed : " + asyncResult.getException());
                    notifyResponse_GetCompanyJoinRequestResponse(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "postAcceptCompanyInvitation success");
                        notifyResponse_GetCompanyJoinRequestResponse(callback, null, new GetCompanyJoinRequestResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "postAcceptCompanyInvitation Impossible to parse REST Conversation result");
                        notifyResponse_GetCompanyJoinRequestResponse(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void cancelJoinCompanyRequest(String userId, String joinCompanyRequestId, final IAsyncServiceResultCallback<GetCompanyJoinRequestResponse> callback) {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(userId);
        restUrl.append(ApisConstants.JOIN_COMPANY_REQUESTED_INVITATIONS);
        restUrl.append("/" + joinCompanyRequestId);
        restUrl.append(ApisConstants.CANCEL);

        JSONObject restBody = new JSONObject();
//        /api/rainbow/enduser/v1.0/users/{userId}/join-companies/requests/{joinCompanyRequestId}/cancel
        Log.getLogger().info(LOG_TAG, "cancelJoinCompanyRequest URL :" + restUrl.toString());

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "cancelJoinCompanyRequest failed : " + asyncResult.getException());
                    notifyResponse_GetCompanyJoinRequestResponse(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "cancelJoinCompanyRequest success");
                        notifyResponse_GetCompanyJoinRequestResponse(callback, null, new GetCompanyJoinRequestResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "cancelJoinCompanyRequest Impossible to parse REST Conversation result");
                        notifyResponse_GetCompanyJoinRequestResponse(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void sendJoinCompanyRequest(String userId, String requestedCompanyId, String requestedCompanyAdminId , String requestedCompanyLinkId , final IAsyncServiceResultCallback<GetCompanyJoinRequestResponse> callback) {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(userId);
        restUrl.append(ApisConstants.JOIN_COMPANY_REQUESTED_INVITATIONS);

        JSONObject restBody = new JSONObject();
        try
        {
            if (!StringsUtil.isNullOrEmpty(requestedCompanyId))
                restBody.put("requestedCompanyId", requestedCompanyId);

            if (!StringsUtil.isNullOrEmpty(requestedCompanyAdminId ))
                restBody.put("requestedCompanyAdminId ", requestedCompanyAdminId );

            if (!StringsUtil.isNullOrEmpty(requestedCompanyLinkId  ))
                restBody.put("requestedCompanyLinkId  ", requestedCompanyLinkId  );
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }
//        /api/rainbow/enduser/v1.0/users/{userId}/join-companies/requests
        Log.getLogger().info(LOG_TAG, "sendJoinCompanyRequest URL :" + restUrl.toString());

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "sendJoinCompanyRequest failed : " + asyncResult.getException());
                    notifyResponse_GetCompanyJoinRequestResponse(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "sendJoinCompanyRequest success");
                        notifyResponse_GetCompanyJoinRequestResponse(callback, null, new GetCompanyJoinRequestResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "sendJoinCompanyRequest Impossible to parse REST Conversation result");
                        notifyResponse_GetCompanyJoinRequestResponse(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void resendJoinCompanyRequest(String userId, String joinCompanyRequestId, final IAsyncServiceResultCallback<GetCompanyJoinRequestResponse> callback) {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(userId);
        restUrl.append(ApisConstants.JOIN_COMPANY_REQUESTED_INVITATIONS);
        restUrl.append("/");
        restUrl.append(joinCompanyRequestId);
        restUrl.append(ApisConstants.RE_SEND);

        JSONObject restBody = new JSONObject();
//        /api/rainbow/enduser/v1.0/users/{userId}/join-companies/requests/{joinCompanyRequestId}/re-send
        Log.getLogger().info(LOG_TAG, "resendJoinCompanyRequest URL :" + restUrl.toString());

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "resendJoinCompanyRequest failed : " + asyncResult.getException());
                    notifyResponse_GetCompanyJoinRequestResponse(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "resendJoinCompanyRequest success");
                        notifyResponse_GetCompanyJoinRequestResponse(callback, null, new GetCompanyJoinRequestResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "resendJoinCompanyRequest Impossible to parse REST Conversation result");
                        notifyResponse_GetCompanyJoinRequestResponse(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    private JSONObject addEmailInfoInJson(String emailStrg,String type) throws JSONException {
        if( StringsUtil.isNullOrEmpty(emailStrg) ) {
            return null;
        }

        JSONObject email = new JSONObject();
        email.put("email", emailStrg);
        email.put("type", type);

        return email;
    }

    private JSONObject addPhoneInfoInJson(String phone,String type, String deviceType) throws JSONException {
        if( StringsUtil.isNullOrEmpty(phone) ) {
            return null;
        }

        JSONObject obj = new JSONObject();
        obj.put("number", phone);
        obj.put("type", type);
        obj.put("deviceType", deviceType);

        return obj;
    }

    private void notifyVoidResult(IAsyncServiceVoidCallback callback, RainbowServiceException alcServiceException)
    {
        AsyncServiceResponseVoid asyncResult = new AsyncServiceResponseVoid(alcServiceException);
        callback.handleResult(asyncResult);
    }

    private void notifyGetUserDataResult(IAsyncServiceResultCallback<GetUserDataResponse> callback, RainbowServiceException alcServiceException, GetUserDataResponse response)
    {
        AsyncServiceResponseResult<GetUserDataResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyGetUserSettingsResult(IAsyncServiceResultCallback<GetUserSettingsResponse> callback, RainbowServiceException alcServiceException, GetUserSettingsResponse response)
    {
        AsyncServiceResponseResult<GetUserSettingsResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyGetUserReceivedInvitations(IAsyncServiceResultCallback<GetUserInvitationsResponse> callback, RainbowServiceException alcServiceException, GetUserInvitationsResponse response)
    {
        AsyncServiceResponseResult<GetUserInvitationsResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }


    private void notifySendSelfRegisterPasswordResult(IAsyncServiceResultCallback<GetSelfRegisterUserPasswordResponse> callback, RainbowServiceException alcServiceException, GetSelfRegisterUserPasswordResponse response)
    {
        AsyncServiceResponseResult<GetSelfRegisterUserPasswordResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyAcceptedUserInvitation(IAsyncServiceResultCallback<GetUserInvitationsResponse> callback, RainbowServiceException alcServiceException, GetUserInvitationsResponse response)
    {
        AsyncServiceResponseResult<GetUserInvitationsResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyDeclinedUserInvitation(IAsyncServiceResultCallback<GetUserInvitationsResponse> callback, RainbowServiceException alcServiceException, GetUserInvitationsResponse response)
    {
        AsyncServiceResponseResult<GetUserInvitationsResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyDeleteUserInvitation(IAsyncServiceResultCallback<GetUserInvitationsResponse> callback, RainbowServiceException alcServiceException, GetUserInvitationsResponse response)
    {
        AsyncServiceResponseResult<GetUserInvitationsResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyGetReceivedCompanyInvitations(IAsyncServiceResultCallback<GetCompanyInvitationsResponse> callback, RainbowServiceException alcServiceException, GetCompanyInvitationsResponse response)
    {
        AsyncServiceResponseResult<GetCompanyInvitationsResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyAcceptedCompanyInvitation(IAsyncServiceResultCallback<GetCompanyInvitationsResponse> callback, RainbowServiceException alcServiceException, GetCompanyInvitationsResponse response)
    {
        AsyncServiceResponseResult<GetCompanyInvitationsResponse> asyncResult = new AsyncServiceResponseResult<GetCompanyInvitationsResponse>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyDeclinedCompanyInvitation(IAsyncServiceResultCallback<GetCompanyInvitationsResponse> callback, RainbowServiceException alcServiceException, GetCompanyInvitationsResponse response)
    {
        AsyncServiceResponseResult<GetCompanyInvitationsResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyUserInvitation(IAsyncServiceResultCallback<GetUserInvitationResponse> callback, RainbowServiceException alcServiceException, GetUserInvitationResponse response)
    {
        AsyncServiceResponseResult<GetUserInvitationResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyResponse_GetCompanyJoinRequestResponse(IAsyncServiceResultCallback<GetCompanyJoinRequestResponse> callback, RainbowServiceException alcServiceException, GetCompanyJoinRequestResponse response)
    {
        AsyncServiceResponseResult asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }
}

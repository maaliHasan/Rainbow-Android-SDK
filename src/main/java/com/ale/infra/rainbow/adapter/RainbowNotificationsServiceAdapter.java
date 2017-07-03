package com.ale.infra.rainbow.adapter;

import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.RESTResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseVoid;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.framework.RainbowServiceTag;
import com.ale.infra.rainbow.api.ApisConstants;
import com.ale.infra.rainbow.api.IRainbowNotifications;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * Created by grobert on 09/11/15.
 */
public class RainbowNotificationsServiceAdapter implements IRainbowNotifications
{
    private static final String LOG_TAG = "RainbowNotificationsServiceAdapter";

    private final IRESTAsyncRequest m_restAsyncRequest;
    private final IPlatformServices m_platformServices;


    public RainbowNotificationsServiceAdapter(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices)
    {
        m_restAsyncRequest = restAsyncRequest;
        m_platformServices = platformServices;
    }

    private String getUrl()
    {
        String url = m_platformServices.getApplicationData().getServerUrl();
        if (url == null)
        {
            url = StringsUtil.EMPTY;
        }
        return url;
    }

    @Override
    public RainbowServiceTag getTag()
    {
        return RainbowServiceTag.NOTIFCATIONS;
    }

    @Override
    public void sendInvitation(String userId, String email, final IAsyncServiceVoidCallback callback)
    {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        try {
            restUrl.append(URLEncoder.encode(userId, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.getLogger().error(LOG_TAG, "Exception : " + e.getMessage());
        }
        restUrl.append(ApisConstants.SEND_USER_INVITE);

        JSONObject restBody = new JSONObject();
        try
        {
            restBody.put("email", email);
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "sendInvitation failed.");
                    notifyVoidResult(callback, asyncResult.getException());
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "sendInvitation success.");
                    notifyVoidResult(callback, null);
                }
            }
        });
    }

    @Override
    public void resendEmailInvite(String userId, String invitationId, final IAsyncServiceVoidCallback callback)
    {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        try {
            restUrl.append(URLEncoder.encode(userId, "UTF-8"));
            restUrl.append(ApisConstants.SEND_USER_INVITE);
            restUrl.append("/");
            restUrl.append(URLEncoder.encode(invitationId, "UTF-8"));
            restUrl.append(ApisConstants.RE_SEND);
        } catch (UnsupportedEncodingException e) {
            Log.getLogger().error(LOG_TAG, "Exception : " + e.getMessage());
        }


        JSONObject restBody = new JSONObject();

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "resendEmailInvite failed.");
                    notifyVoidResult(callback, asyncResult.getException());
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "resendEmailInvite success.");
                    notifyVoidResult(callback, null);
                }
            }
        });
    }

    @Override
    public void sendSelfRegisterInvite(String email, final IAsyncServiceVoidCallback callback)
    {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.SELF_REGISTER_INVITE);

        JSONObject restBody = new JSONObject();
        String language = Locale.getDefault().getLanguage();
        try
        {
            restBody.put("email", email);
            restBody.put("lang", language);
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        Log.getLogger().info(LOG_TAG, "sendSelfRegisterInvite rest body :" + restBody.toString());
        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "sendSelfRegisterInvite failed.");
                    notifyVoidResult(callback, asyncResult.getException());
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "sendSelfRegisterInvite success.");
                    notifyVoidResult(callback, null);
                }
            }
        });
    }

    @Override
    public void sendResetPwdEmail(String email, final IAsyncServiceVoidCallback callback) {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.SELF_RESET_PWD_EMAIL);

        JSONObject restBody = new JSONObject();
        try
        {
            restBody.put("email", email);
            restBody.put("lang", Locale.getDefault().getLanguage());
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "sendResetPwdEmail failed.");
                    notifyVoidResult(callback, asyncResult.getException());
                } else {
                    Log.getLogger().info(LOG_TAG, "sendResetPwdEmail success.");
                    notifyVoidResult(callback, null);
                }
            }
        });
    }

    @Override
    public void sendResetUserPwd(String email, String newPassword, String tempToken, final IAsyncServiceVoidCallback callback) {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.SELF_RESET_USER_PWD);

        JSONObject restBody = new JSONObject();
        try
        {
            restBody.put("loginEmail", email);
            restBody.put("newPassword", newPassword);
            restBody.put("temporaryToken", tempToken);
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        m_restAsyncRequest.sendPutRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "sendResetUserPwd failed.");
                    notifyVoidResult(callback, asyncResult.getException());
                } else {
                    Log.getLogger().info(LOG_TAG, "sendResetUserPwd success.");
                    notifyVoidResult(callback, null);
                }
            }
        });
    }

    private void notifyVoidResult(IAsyncServiceVoidCallback callback, RainbowServiceException alcServiceException)
    {
        AsyncServiceResponseVoid asyncResult = new AsyncServiceResponseVoid(alcServiceException);
        callback.handleResult(asyncResult);
    }
}

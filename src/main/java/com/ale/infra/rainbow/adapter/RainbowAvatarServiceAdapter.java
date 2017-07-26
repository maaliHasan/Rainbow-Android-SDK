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
import com.ale.infra.proxy.avatar.GetAvatarResponse;
import com.ale.infra.proxy.framework.RainbowServiceTag;
import com.ale.infra.rainbow.api.ApisConstants;
import com.ale.infra.rainbow.api.IRainbowAvatarService;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by wilsius on 25/05/16.
 */
public class RainbowAvatarServiceAdapter implements IRainbowAvatarService
{
    private static final String LOG_TAG = "RainbowAvatarServiceAdapter";

    private final IRESTAsyncRequest m_restAsyncRequest;
    private final IPlatformServices m_platformServices;


    public RainbowAvatarServiceAdapter(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices)
    {
        m_restAsyncRequest = restAsyncRequest;
        m_platformServices = platformServices;
    }

    @Override
    public RainbowServiceTag getTag()
    {
        return RainbowServiceTag.AVATAR;
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
    public void deleteMyAvatar(String userId, final IAsyncServiceVoidCallback callback)
    {
        Log.getLogger().verbose(LOG_TAG, ">deleteMyAvatar");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);

        // sending token through header or parameters does not work (see https://tools.ietf.org/html/rfc6750)
        // but it works with URI
        try
        {
            restUrl.append(URLEncoder.encode(userId, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to delete my avatar");
        }
        restUrl.append("/avatar");

        m_restAsyncRequest.sendDeleteRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "deleteConversation failed");
                    notifyVoidResult(callback, asyncResult.getException());
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "deleteConversation success");
                    notifyVoidResult(callback, null);
                }
            }
        });
    }

    @Override
    public void getAvatar(final String userId, String hash , final int size, final IAsyncServiceResultCallback<GetAvatarResponse> callback)
    {
        Log.getLogger().verbose(LOG_TAG, ">getAvatar");
        StringBuilder restUrl = new StringBuilder(RainbowContext.getPlatformServices().getApplicationData().getServerUrlForCDN());
        restUrl.append(ApisConstants.AVATAR);
        restUrl.append("/");

        // sending token through header or parameters does not work (see https://tools.ietf.org/html/rfc6750)
        // but it works with URI
        try
        {
            restUrl.append(URLEncoder.encode(userId, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to getAvatar");
        }
        restUrl.append("?size=");
        restUrl.append(String.valueOf(size));
        if (hash != null) {
            restUrl.append("?update=");
            restUrl.append(hash);
        }
        Log.getLogger().verbose(LOG_TAG, ">getAvatar resturl:" + restUrl);

        final String s = restUrl.toString();
        try
        {
            m_restAsyncRequest.getAvatarFile(restUrl.toString(), new IAsyncServiceResultCallback<GetAvatarResponse>() {
                @Override
                public void handleResult(AsyncServiceResponseResult<GetAvatarResponse> asyncResult) {
                    if (asyncResult.exceptionRaised())
                    {
                        Log.getLogger().error(LOG_TAG, "getAvatar failed");
                        notifyGetAvatarResult(callback, asyncResult.getException(), null);
                    }
                    else
                    {
                        Log.getLogger().info(LOG_TAG, "getAvatar success:" +  s);
                        try {
                            notifyGetAvatarResult(callback, null, asyncResult.getResult());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        catch (Exception e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to parse REST Avatar result" + e.toString());
            notifyGetAvatarResult(callback, new RainbowServiceException(e), null);
        }
    }

    private void notifyGetAvatarResult(IAsyncServiceResultCallback<GetAvatarResponse> callback, RainbowServiceException alcServiceException, GetAvatarResponse response)
    {
        AsyncServiceResponseResult<GetAvatarResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    @Override
    public void uploadAvatar(String userId, File photoFile, final IAsyncServiceVoidCallback callback)
    {
        Log.getLogger().verbose(LOG_TAG, ">uploadAvatar");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);

        // sending token through header or parameters does not work (see https://tools.ietf.org/html/rfc6750)
        // but it works with URI
        try
        {
            restUrl.append(URLEncoder.encode(userId, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to delete my avatar");
        }
        restUrl.append("/avatar");

        m_restAsyncRequest.uploadPhoto(restUrl.toString(), photoFile, new IAsyncServiceResultCallback<RESTResult>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "uploadAvatar failed");
                    notifyVoidResult(callback, asyncResult.getException());
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "uploadAvatar success");
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

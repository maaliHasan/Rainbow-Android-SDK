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
import com.ale.infra.proxy.authentication.AuthenticationResponse;
import com.ale.infra.proxy.framework.RainbowServiceTag;
import com.ale.infra.rainbow.api.ApisConstants;
import com.ale.infra.rainbow.api.IRainbowAuthentication;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

/**
 * Created by grobert on 26/10/15.
 */
public class RainbowAuthenticationServiceAdapter implements IRainbowAuthentication
{
    private static final String LOG_TAG = "RainbowAuthenticationServiceAdapter";

    private final IRESTAsyncRequest m_restAsyncRequest;


    public RainbowAuthenticationServiceAdapter(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService)
    {
        m_restAsyncRequest = restAsyncRequest;
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
        return RainbowServiceTag.AUTHENTICATE;
    }

    @Override
    public void authenticate(String login, String password, final IAsyncServiceResultCallback<AuthenticationResponse> callback)
    {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.LOGIN_API);

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "authentication to Rainbow failed.");
                    notifyAuthenticationResult(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "Rainbow authentication success.");
                        notifyAuthenticationResult(callback, null, new AuthenticationResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST authentication credentials");
                        notifyAuthenticationResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void authenticateApplication(final IAsyncServiceResultCallback<AuthenticationResponse> callback) {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.LOGIN_APPLICATION);

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "authentication application failed.");
                    notifyAuthenticationResult(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "application authentication success.");
                        notifyAuthenticationResult(callback, null, new AuthenticationResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST authentication application: " + error);
                        notifyAuthenticationResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void disconnectOfRainbowServer(String token, final IAsyncServiceVoidCallback callback)
    {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.LOGOUT_API);

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "The disconnection of the Rainbow server is failed.");
                    notifyDisconnectionResult(callback, asyncResult.getException());
                } else {
                    Log.getLogger().info(LOG_TAG, "Rainbow disconnection success.");
                    notifyDisconnectionResult(callback, null);
                }
            }
        });
    }

    private void notifyAuthenticationResult(IAsyncServiceResultCallback<AuthenticationResponse> callback, RainbowServiceException alcServiceException, AuthenticationResponse response)
    {
        AsyncServiceResponseResult<AuthenticationResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyDisconnectionResult(IAsyncServiceVoidCallback callback, RainbowServiceException alcServiceException)
    {
        callback.handleResult(new AsyncServiceResponseVoid(alcServiceException));
    }
}

package com.ale.infra.rainbow.adapter;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.RESTResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.framework.RainbowServiceTag;
import com.ale.infra.proxy.portal.GetVersionResponse;
import com.ale.infra.rainbow.api.ApisConstants;
import com.ale.infra.rainbow.api.IPortalVersionService;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

/**
 * Created by georges on 29/08/2016.
 */
public class PortalVersionServiceAdapter implements IPortalVersionService
{
    private static final String LOG_TAG = "PortalVersionServiceAdapter";

    private final IRESTAsyncRequest m_restAsyncRequest;


    public PortalVersionServiceAdapter(IRESTAsyncRequest restAsyncRequest, IPlatformServices p)
    {
        m_restAsyncRequest = restAsyncRequest;
    }

    @Override
    public RainbowServiceTag getTag() {
        return RainbowServiceTag.BOTS;
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
    public void getEndUserVersion(final IAsyncServiceResultCallback<GetVersionResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">getEndUserVersion");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS_ABOUT);

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "getEndUserVersion failed");
                    notifyGetVersionResult(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "getEndUserVersion success");
                        notifyGetVersionResult(callback, null, new GetVersionResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST getEndUserVersion result");
                        notifyGetVersionResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    private void notifyGetVersionResult(IAsyncServiceResultCallback<GetVersionResponse> callback, RainbowServiceException alcServiceException, GetVersionResponse response) {
        AsyncServiceResponseResult<GetVersionResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }
}

package com.ale.infra.rainbow.adapter;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.RESTResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.framework.RainbowServiceTag;
import com.ale.infra.proxy.profile.FeatureResponse;
import com.ale.infra.rainbow.api.ApisConstants;
import com.ale.infra.rainbow.api.IProfileService;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;


public class RainbowProfileServiceAdapter implements IProfileService
{
    private static final String LOG_TAG = "RainbowProfileServiceAdapter";

    private final IRESTAsyncRequest m_restAsyncRequest;
    private IPlatformServices m_platformServices;

    public RainbowProfileServiceAdapter(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices)
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
    public void getUserFeatures(String userId, final IAsyncServiceResultCallback<FeatureResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">getUserFeatures");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(userId);
        restUrl.append(ApisConstants.FEATURES);

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "getUserFeatures FAILURE");
                    Log.getLogger().verbose(LOG_TAG, ">getUserFeatures : " + asyncResult.getException().getDetailsMessage());
                    notifyFeatureResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "getUserFeatures SUCCESS");
                    try {
                        notifyFeatureResult(callback, null, new FeatureResponse(asyncResult.getResult().getResponse()));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST PgiConferenceResponse result");
                        notifyFeatureResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    private void notifyFeatureResult(IAsyncServiceResultCallback<FeatureResponse> callback, RainbowServiceException alcServiceException, FeatureResponse response)
    {
        AsyncServiceResponseResult<FeatureResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }
}

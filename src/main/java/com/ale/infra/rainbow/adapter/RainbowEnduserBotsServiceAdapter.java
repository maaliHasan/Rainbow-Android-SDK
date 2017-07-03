package com.ale.infra.rainbow.adapter;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.RESTResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.EnduserBots.GetAllBotsResponse;
import com.ale.infra.proxy.EnduserBots.GetBotDataResponse;
import com.ale.infra.proxy.framework.RainbowServiceTag;
import com.ale.infra.rainbow.api.ApisConstants;
import com.ale.infra.rainbow.api.IRainbowEnduserBotsService;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by wilsius on 08/08/16.
 */
public class RainbowEnduserBotsServiceAdapter implements IRainbowEnduserBotsService
{
    private static final String LOG_TAG = "RainbowEnduserBotsServiceAdapter";

    private final IRESTAsyncRequest m_restAsyncRequest;


    public RainbowEnduserBotsServiceAdapter(IRESTAsyncRequest restAsyncRequest, IPlatformServices p)
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
    public void getAllBots(int limit, int offset, final IAsyncServiceResultCallback<GetAllBotsResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">getAllBots");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.BOTS);

        try
        {
            restUrl.append("?format=full" );

            if (limit != 0)
                restUrl.append("&limit=" + limit);

            restUrl.append("&offset=" + offset);

        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "getAllBots failed");
                    notifyGetAllBotsResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    try
                    {
                        Log.getLogger().info(LOG_TAG, "getAllBots success");
                        notifyGetAllBotsResult(callback, null, new GetAllBotsResponse(asyncResult.getResult().getResponse()));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST bots result");
                        notifyGetAllBotsResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void getBotData(String botId, final IAsyncServiceResultCallback<GetBotDataResponse> callback) {

        Log.getLogger().verbose(LOG_TAG, ">getBotData");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.BOTS);

        restUrl.append("/");

        // sending token through header or parameters does not work (see https://tools.ietf.org/html/rfc6750)
        // but it works with URI
        try
        {
            restUrl.append(URLEncoder.encode(botId, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to create conversation");
        }

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "getAllBots failed");
                    notifyGetBotDataResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    try
                    {
                        Log.getLogger().info(LOG_TAG, "getAllBots success");
                        notifyGetBotDataResult(callback, null, new GetBotDataResponse(asyncResult.getResult().getResponse()));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST bots result");
                        notifyGetBotDataResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });

    }

    private void notifyGetAllBotsResult(IAsyncServiceResultCallback<GetAllBotsResponse> callback, RainbowServiceException alcServiceException, GetAllBotsResponse response) {
        AsyncServiceResponseResult<GetAllBotsResponse> asyncResult = new AsyncServiceResponseResult<GetAllBotsResponse>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyGetBotDataResult(IAsyncServiceResultCallback<GetBotDataResponse> callback, RainbowServiceException alcServiceException, GetBotDataResponse response) {
        AsyncServiceResponseResult<GetBotDataResponse> asyncResult = new AsyncServiceResponseResult<GetBotDataResponse>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }
}

package com.ale.infra.proxy.EnduserBots;

import com.ale.infra.application.IApplicationData;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.rainbow.api.IRainbowEnduserBotsService;
import com.ale.infra.rainbow.api.IServicesFactory;
import com.ale.util.log.Log;

/**
 * Created by wilsius on 08/08/16.
 */
public class EnduserBotsProxy implements IEnduserBotsProxy
{
    private static final String LOG_TAG = "EnduserBotsProxy";
    private IApplicationData m_applicationData;
    private IRainbowEnduserBotsService m_botsService;

    public EnduserBotsProxy(IServicesFactory servicesFactory, IRESTAsyncRequest httpClientFactory, IPlatformServices platformService)
    {
        Log.getLogger().info(LOG_TAG, "initialization");
        m_botsService = servicesFactory.createEnduserBotsService(httpClientFactory, platformService);
        m_applicationData = platformService.getApplicationData();

    }

    @Override
    public void getAllBots(int limit, int offset, final IGetAllBotsListener listener) {
        Log.getLogger().info(LOG_TAG, ">getAllBots");

        m_botsService.getAllBots(limit, offset, new IAsyncServiceResultCallback<GetAllBotsResponse>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetAllBotsResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "getAllBots SUCCESS");
                    if (listener != null)
                        listener.onGetAllBotsSuccess(asyncResult.getResult().getDirContacts());
                } else {
                    Log.getLogger().warn(LOG_TAG, "getAllBots FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onGetAllBotsFailure();
                }
            }
        });
    }

    @Override
    public void getBotData(String botId, final IGetBotDataListener listener) {
        Log.getLogger().info(LOG_TAG, ">getBotData for id: " + botId);

        m_botsService.getBotData(botId, new IAsyncServiceResultCallback<GetBotDataResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetBotDataResponse> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "getBotData SUCCESS");
                    if (listener != null)
                        listener.onGetBotDataSuccess(asyncResult.getResult().getContact());
                } else {
                    Log.getLogger().warn(LOG_TAG, "getBotData FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onGetBotDataFailure();
                }
            }
        });
    }
}

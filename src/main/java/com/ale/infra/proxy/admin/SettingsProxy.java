package com.ale.infra.proxy.admin;

import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.rainbow.api.IRainbowSettingsService;
import com.ale.infra.rainbow.api.IServicesFactory;
import com.ale.util.log.Log;

import org.webrtc.PeerConnection;

import java.util.List;

/**
 * Created by cebruckn on 13/02/2017.
 */

public class SettingsProxy implements ISettings
{
    private static final String LOG_TAG = "SettingsProxy";
    private IRainbowSettingsService m_adminService;

    public SettingsProxy(IServicesFactory servicesFactory, IRESTAsyncRequest httpClientFactory, IPlatformServices platformService)
    {
        m_adminService = servicesFactory.createSettingsService(httpClientFactory, platformService);
    }

    @Override
    public void getIceServers(final IIceServersListener listener)
    {
        m_adminService.getIceServers(new IAsyncServiceResultCallback<List<PeerConnection.IceServer>>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<List<PeerConnection.IceServer>> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "Impossible to get Ice Servers: ", asyncResult.getException());

                    if (listener != null)
                        listener.onGetIceServersFailure();
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "Ice Servers download success");

                    if (listener != null)
                        listener.onGetIceServersSuccess(asyncResult.getResult());
                }
            }
        });
    }
}

package com.ale.infra.rainbow.adapter;

import android.location.LocationManager;

import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.RESTResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.framework.RainbowServiceTag;
import com.ale.infra.rainbow.api.ApisConstants;
import com.ale.infra.rainbow.api.IRainbowSettingsService;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.PeerConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wilsius on 25/05/16.
 */
public class RainbowSettingsServiceAdapter implements IRainbowSettingsService
{
    private static final String LOG_TAG = "RainbowSettingsServiceAdapter";

    private final IRESTAsyncRequest m_restAsyncRequest;
    private final IPlatformServices m_platformServices;


    public RainbowSettingsServiceAdapter(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices)
    {
        m_restAsyncRequest = restAsyncRequest;
        m_platformServices = platformServices;
    }

    @Override
    public RainbowServiceTag getTag()
    {
        return RainbowServiceTag.SETTINGS;
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
    public void getIceServers(final IAsyncServiceResultCallback<List<PeerConnection.IceServer>> callback)
    {
        Log.getLogger().verbose(LOG_TAG, ">getIceServers");


        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.ICE_SERVERS);

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "getIceServers failed");
                    notifyIceServersResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "getIceServers success");

                    List<PeerConnection.IceServer> iceServers = new ArrayList<PeerConnection.IceServer>();

                    try
                    {
                        JSONObject servers = new JSONObject(asyncResult.getResult().getResponse());
                        JSONArray arrayServers = servers.getJSONArray("data");

                        for (int index = 0; index < arrayServers.length(); index++)
                        {
                            JSONObject server = arrayServers.getJSONObject(index);

                            String credential = "";
                            String urls = "";
                            String username = "";

                            if (server.has("credential"))
                                credential = server.getString("credential");

                            if (server.has("urls"))
                                urls = server.getString("urls");

                            if (server.has("username"))
                                username = server.getString("username");

                            if(!StringsUtil.isNullOrEmpty(urls))
                            {
                                PeerConnection.IceServer iceServer = new PeerConnection.IceServer(urls, username, credential);
                                iceServers.add(iceServer);
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        Log.getLogger().error(LOG_TAG, "Error while parsing Ice Servers: ", e);
                    }

                    notifyIceServersResult(callback, null, iceServers);
                }
            }
        });
    }

    private void notifyIceServersResult(IAsyncServiceResultCallback<List<PeerConnection.IceServer>> callback, RainbowServiceException alcServiceException, List<PeerConnection.IceServer> servers)
    {
        AsyncServiceResponseResult<List<PeerConnection.IceServer>> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, servers);
        callback.handleResult(asyncResult);
    }
}

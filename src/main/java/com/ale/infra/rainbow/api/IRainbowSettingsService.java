package com.ale.infra.rainbow.api;

import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;

import org.webrtc.PeerConnection;

import java.util.List;

/**
 * Created by cebruckn on 13/02/2017.
 */
public interface IRainbowSettingsService extends IRainbowService
{
    void getIceServers(final IAsyncServiceResultCallback<List<PeerConnection.IceServer>> callback);
}

package com.ale.infra.proxy.admin;

import org.webrtc.PeerConnection;

import java.util.List;

/**
 * Created by cebruckn on 13/02/2017.
 */

public interface ISettings
{
    void getIceServers(IIceServersListener listener);

    interface IIceServersListener
    {
        void onGetIceServersSuccess(List<PeerConnection.IceServer> servers);

        void onGetIceServersFailure();
    }
}

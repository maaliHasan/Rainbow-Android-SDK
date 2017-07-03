package com.ale.infra.manager.call;

/**
 * Created by cebruckn on 14/02/2017.
 */

public interface ITelephonyListener
{
    void onCallAdded(WebRTCCall call);

    void onCallModified(WebRTCCall call);

    void onCallRemoved(WebRTCCall call);
}

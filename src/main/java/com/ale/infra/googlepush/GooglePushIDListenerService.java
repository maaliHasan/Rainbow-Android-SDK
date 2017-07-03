package com.ale.infra.googlepush;


import android.content.Intent;

import com.ale.util.log.Log;
import com.google.android.gms.iid.InstanceIDListenerService;

public class GooglePushIDListenerService extends InstanceIDListenerService {

    private static final String LOG_TAG = "GooglePushIDListenerService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        Log.getLogger().verbose(LOG_TAG, ">onTokenRefresh");
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(this, GooglePushRegService.class);
        startService(intent);
    }
    // [END refresh_token]
}







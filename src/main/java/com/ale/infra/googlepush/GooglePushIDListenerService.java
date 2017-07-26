package com.ale.infra.googlepush;


import com.ale.infra.application.RainbowContext;
import com.ale.util.log.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class GooglePushIDListenerService extends FirebaseInstanceIdService
{

    private static final String LOG_TAG = "GooglePushIDListenerService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh()
    {
        Log.getLogger().verbose(LOG_TAG, ">onTokenRefresh");

        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        RainbowContext.getPlatformServices().getApplicationData().setGooglePushToken(refreshedToken);
    }
    // [END refresh_token]
}







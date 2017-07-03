package com.ale.infra.googlepush;


import android.app.IntentService;
import android.content.Intent;

import com.ale.infra.application.RainbowContext;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.util.log.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;


public class GooglePushRegService extends IntentService {

    private static final String LOG_TAG = "GooglePushRegService";

    public GooglePushRegService() {
        super(LOG_TAG);
    }

    public void onHandleIntent(Intent intent) {
        Log.getLogger().verbose(LOG_TAG, ">onHandleIntent");

        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                InstanceID instanceID = InstanceID.getInstance(GooglePushRegService.this);

                if (RainbowContext.getInfrastructure().isInDebugMode(getApplicationContext())) {
                    try {
                        instanceID.deleteInstanceID();
                    } catch (IOException e) {
                        Log.getLogger().error(LOG_TAG, "get Instance failed" + e.toString());
                    }
                }

                instanceID = InstanceID.getInstance(GooglePushRegService.this);
                String token = null;
                try {
                    token = instanceID.getToken(RainbowSdk.instance().getPushGoogleSenderId(),
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                    RainbowContext.getPlatformServices().getApplicationData().setGooglePushToken(token);
                } catch (IOException e) {
                    Log.getLogger().error(LOG_TAG,"get token failed" + e.toString());
                }
                Log.getLogger().verbose(LOG_TAG, "GCM Registration Token: " + token);
            }
        });
        myThread.start();
    }
}

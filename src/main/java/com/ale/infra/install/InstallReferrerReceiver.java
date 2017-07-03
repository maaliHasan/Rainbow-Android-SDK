package com.ale.infra.install;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ale.infra.application.IApplicationData;
import com.ale.infra.application.RainbowContext;
import com.ale.infra.application.RainbowIntent;
import com.ale.util.StringsUtil;

/**
 * Created by georges on 29/11/2016.
 */

public class InstallReferrerReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "InstallReferrerReceiver";

    private static final String KEY_EMAIL = "loginEmail";
    private static final String KEY_INVITATIONID = "invitationId";
    private static final String KEY_COMPANY = "companyName";


    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG,"Referral Received");
        try {
            String referrer = intent.getStringExtra("referrer");
            if (referrer != null && !referrer.equals("")) {
                Log.d(LOG_TAG,"Referral Received - " + referrer);

                String[] referrerParts = referrer.split("&");
                String loginEmail = getData(KEY_EMAIL, referrerParts);
                String invitationId = getData(KEY_INVITATIONID, referrerParts);
                String companyName = getData(KEY_COMPANY, referrerParts);

                IApplicationData applicationData = RainbowContext.getPlatformServices().getApplicationData();
                applicationData.setReferrerLogin(loginEmail);
                applicationData.setReferrerInvitationId(invitationId);
                applicationData.setReferrerCompanyName(companyName);

                Log.d(LOG_TAG,"keyEmail = " + loginEmail);
                Log.d(LOG_TAG,"keyInvitationId = " + invitationId);
                if( !StringsUtil.isNullOrEmpty(companyName) ) {
                    Log.d(LOG_TAG, "keyCompanyName = " + companyName);
                }

                context.sendBroadcast(new Intent(RainbowIntent.ACTION_REFERRER_RECEIVED));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getData(String key, String[] allData) {
        for (String selected : allData)
            if (selected.contains(key)) {
                return selected.split("=")[1];
            }
        return null;
    }
}

package com.ale.infra.proxy.users;

import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.util.log.Log;

import org.json.JSONObject;

/**
 * Created by wilsius on 08/11/16.
 */

public class GetUserSettingsResponse extends RestResponse {

    private static final String LOG_TAG = "GetUserSettingsResponse";

    private String presence = null;
    private boolean displayNameOrderFirstNameFirst = false;

    public GetUserSettingsResponse(IPlatformServices platformServices, String data) throws Exception {
        if (platformServices.getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, ">GetUserSettingsResponse; " + data);

        JSONObject obj = new JSONObject(data);

        if( obj.has("data")) {
            JSONObject json = obj.getJSONObject("data");
            presence = json.optString("presence", null);
            displayNameOrderFirstNameFirst = json.optBoolean("displayNameOrderFirstNameFirst", false);
        }
    }

    public String getPresence() {
        return presence;
    }

    public boolean isDisplayNameOrderFirstNameFirst() {
        return displayNameOrderFirstNameFirst;
    }
}


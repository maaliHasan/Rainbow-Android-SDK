package com.ale.infra.proxy.users;

import com.ale.infra.application.IApplicationData;
import com.ale.infra.application.RainbowContext;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.util.log.Log;

import org.json.JSONObject;


public class GetSelfRegisterUserPasswordResponse extends RestResponse {
    private static final String LOG_TAG = "GetSelfRegisterUserPasswordResponse";
    private boolean initialized = false;

    public GetSelfRegisterUserPasswordResponse(String data) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, ">GetSelfRegisterUserPasswordResponse; " + data);

        JSONObject obj = new JSONObject(data);

        JSONObject json = obj.getJSONObject("data");
        initialized = json.optBoolean("isInitialized", false);

        IApplicationData applicationData = RainbowContext.getPlatformServices().getApplicationData();
        applicationData.setUserId(json.getString("id"));
    }

    public boolean isInitialized (){
        return initialized;
    }

}

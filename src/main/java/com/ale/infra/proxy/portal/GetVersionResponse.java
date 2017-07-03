package com.ale.infra.proxy.portal;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.util.log.Log;

import org.json.JSONObject;

/**
 * Created by wilsius on 08/08/16.
 */
public class GetVersionResponse extends RestResponse
{
    private static final String LOG_TAG = "GetVersionResponse";

    private final String m_version;

    public GetVersionResponse(String data) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, "Parsing version data; " + data);

        JSONObject about = new JSONObject(data);

        m_version = about.getString(RestResponse.VERSION);
    }

    public String getVersion() {
        return m_version;
    }
}

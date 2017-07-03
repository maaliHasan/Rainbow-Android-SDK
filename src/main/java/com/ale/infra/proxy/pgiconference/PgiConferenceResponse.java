package com.ale.infra.proxy.pgiconference;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.manager.pgiconference.PgiConference;
import com.ale.util.log.Log;

import org.json.JSONException;
import org.json.JSONObject;

import static com.ale.infra.proxy.framework.RestResponse.COMPANYID;
import static com.ale.infra.proxy.framework.RestResponse.CONFERENCE_USERID;
import static com.ale.infra.proxy.framework.RestResponse.ID;
import static com.ale.infra.proxy.framework.RestResponse.MEDIATYPE;
import static com.ale.infra.proxy.framework.RestResponse.PROVIDER_CONFID;
import static com.ale.infra.proxy.framework.RestResponse.PROVIDER_TYPE;
import static com.ale.infra.proxy.framework.RestResponse.PROVIDER_USERID;
import static com.ale.infra.proxy.framework.RestResponse.USERID;

/**
 * Created by georges on 20/02/2017.
 */

public class PgiConferenceResponse {
    private static final String LOG_TAG = "PgiConferenceResponse";

    private PgiConference m_pgiConference = new PgiConference();

    public PgiConferenceResponse() {
    }

    public PgiConferenceResponse(String response) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, ">PgiConferenceResponse; "+response);

        JSONObject confObj = new JSONObject(response);
        parseJson(confObj);
    }

    public PgiConference parseJson(JSONObject confObj) throws JSONException {

        if (confObj.has(ID))
            m_pgiConference.setId(confObj.getString(ID));
        if (confObj.has(PROVIDER_USERID))
            m_pgiConference.setProviderUserId(confObj.getString(PROVIDER_USERID));
        if (confObj.has(PROVIDER_CONFID))
            m_pgiConference.setProviderConfId(confObj.getString(PROVIDER_CONFID));
        if (confObj.has(PROVIDER_TYPE))
            m_pgiConference.setProviderType(confObj.getString(PROVIDER_TYPE));
        if (confObj.has(CONFERENCE_USERID))
            m_pgiConference.setConfUserId(confObj.getString(CONFERENCE_USERID));
        if (confObj.has(USERID))
            m_pgiConference.setUserId(confObj.getString(USERID));
        if (confObj.has(COMPANYID))
            m_pgiConference.setCompanyId(confObj.getString(COMPANYID));
        if (confObj.has(MEDIATYPE))
            m_pgiConference.setMediaType(confObj.getString(MEDIATYPE));

        return m_pgiConference;
    }

    public PgiConference getConference() {
        return m_pgiConference;
    }
}

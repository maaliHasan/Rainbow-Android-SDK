package com.ale.infra.proxy.pgiconference;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.manager.pgiconference.PgiConference;
import com.ale.util.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by georges on 27/04/2017.
 */

public class PgiGetAllConferencesResponse {
    private static final String LOG_TAG = "PgiGetAllConferencesResponse";

    private List<PgiConference> m_confs = new ArrayList<>();

    public PgiGetAllConferencesResponse(String response) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, ">PgiGetAllConferencesResponse; "+response);

        m_confs = new ArrayList<>();

        JSONObject pgiConfsObj = new JSONObject(response);
        JSONArray arrayUsersGroups = pgiConfsObj.getJSONArray("data");


        for (int index = 0; index < arrayUsersGroups.length(); index++) {
            JSONObject jsonObj = arrayUsersGroups.getJSONObject(index);

            PgiConferenceResponse pgiConfResp = new PgiConferenceResponse();

            m_confs.add(pgiConfResp.parseJson(jsonObj));
        }
    }

    public List<PgiConference> getConferenceList() {
        return m_confs;
    }
}

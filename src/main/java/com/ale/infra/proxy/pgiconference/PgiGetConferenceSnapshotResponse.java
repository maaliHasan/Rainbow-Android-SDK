package com.ale.infra.proxy.pgiconference;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.manager.pgiconference.PgiConference;
import com.ale.infra.manager.pgiconference.PgiConferenceParticipant;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static com.ale.infra.proxy.framework.RestResponse.CONFERENCE_STARTDATE;
import static com.ale.infra.proxy.framework.RestResponse.JIDIM;
import static com.ale.infra.proxy.framework.RestResponse.JIDTEL;
import static com.ale.infra.proxy.framework.RestResponse.PARTICIPANT_HOLD;
import static com.ale.infra.proxy.framework.RestResponse.PARTICIPANT_MUTE;
import static com.ale.infra.proxy.framework.RestResponse.PARTICIPANT_ROLE;
import static com.ale.infra.proxy.framework.RestResponse.PHONENUMBER;
import static com.ale.infra.proxy.framework.RestResponse.STARTDATE;
import static com.ale.infra.proxy.framework.RestResponse.STATE;
import static com.ale.infra.proxy.framework.RestResponse.USERID;

/**
 * Created by georges on 03/05/2017.
 */

public class PgiGetConferenceSnapshotResponse{
    private static final String LOG_TAG = "PgiGetConferenceSnapshotResponse";

    private PgiConference m_pgiConf = null;

    public PgiGetConferenceSnapshotResponse(String response) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, ">PgiGetConferenceSnapshotResponse; "+response);


        m_pgiConf = new PgiConference();

        JSONObject pgiConfsObj = new JSONObject(response);
        JSONArray arrayUsersGroups = pgiConfsObj.getJSONArray("data");


        for (int index = 0; index < arrayUsersGroups.length(); index++) {
            JSONObject jsonObj = arrayUsersGroups.getJSONObject(index);

            m_pgiConf.addParticipant(parseConfParticipantJson(jsonObj));
        }

    }

    public PgiConferenceParticipant parseConfParticipantJson(JSONObject jsonObj) throws JSONException, ParseException {

        PgiConferenceParticipant participant = new PgiConferenceParticipant();

        if (jsonObj.has(USERID))
            participant.setUserId(jsonObj.getString(USERID));
        if (jsonObj.has(JIDIM))
            participant.setJidIm(jsonObj.getString(JIDIM));
        if (jsonObj.has(JIDTEL))
            participant.setJidTel(jsonObj.getString(JIDTEL));
        if (jsonObj.has(PARTICIPANT_ROLE))
            participant.setRole(jsonObj.getString(PARTICIPANT_ROLE));
        if (jsonObj.has(PARTICIPANT_MUTE))
            participant.setMuted(jsonObj.getBoolean(PARTICIPANT_MUTE));
        if (jsonObj.has(PARTICIPANT_HOLD))
            participant.setHold(jsonObj.getBoolean(PARTICIPANT_HOLD));
        if (jsonObj.has(PHONENUMBER))
            participant.setPhoneNumber(jsonObj.getString(PHONENUMBER));
        if (jsonObj.has(STATE))
            participant.setState(jsonObj.getString(STATE));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));

        if (jsonObj.has(CONFERENCE_STARTDATE)) {
            String dateString = jsonObj.getString(CONFERENCE_STARTDATE);
            if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(dateString))
                participant.setConfStartDate(df.parse(dateString));
        }
        if (jsonObj.has(STARTDATE)) {
            String dateString = jsonObj.getString(STARTDATE);
            if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(dateString))
                participant.setStartDate(df.parse(dateString));
        }

        return participant;
    }

    public PgiConference getConferenceSnapshot() {
        return m_pgiConf;
    }
}

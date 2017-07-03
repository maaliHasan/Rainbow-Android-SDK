package com.ale.infra.proxy.room;

import android.support.annotation.NonNull;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.manager.room.RoomConfEndPoint;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.util.log.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by georges on 28/04/2017.
 */

public class GetConfEndPointDataResponse extends RestResponse {
    private static final String LOG_TAG = "GetConfEndPointDataResponse";

    private final SimpleDateFormat m_dateFormat;
    private final RoomConfEndPoint m_confEndPoint;


    public GetConfEndPointDataResponse(String data) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, "Parsing ConfEndPointData; " + data);

        m_dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        m_dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        JSONObject obj1 = new JSONObject(data);
        JSONObject jsonObject =  obj1.getJSONObject("data");

        m_confEndPoint = parseConfEndPoint(jsonObject);
    }

    @NonNull
    private RoomConfEndPoint parseConfEndPoint(JSONObject jsonObject) throws JSONException, ParseException {
        RoomConfEndPoint confEndPoint = new RoomConfEndPoint();

        if( jsonObject.has("userId"))
            confEndPoint.setUserId(jsonObject.getString("userId"));

        if( jsonObject.has("additionDate")) {
            Date date = m_dateFormat.parse(jsonObject.getString("additionDate"));
            confEndPoint.setAdditionDate(date);
        }

        if( jsonObject.has("privilege"))
            confEndPoint.setPrivilege(jsonObject.getString("privilege"));

        return confEndPoint;
    }

    public RoomConfEndPoint getConfEndPoint() { return m_confEndPoint; }
}

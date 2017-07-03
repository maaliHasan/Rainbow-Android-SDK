package com.ale.infra.proxy.fileserver;

import com.ale.infra.http.RESTResult;
import com.ale.util.log.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cebruckn on 13/04/2017.
 */

public class Consumption
{
    private static final String LOG_TAG = "Consumption";
    private long m_quota = 0;
    private long m_consumption = 0;
    private String m_unit = "octet";


    public Consumption(RESTResult result)
    {
        try
        {
            JSONObject obj = new JSONObject(result.getResponse());
            JSONObject data = obj.getJSONObject("data");

            m_quota = data.getLong("maxValue");
            m_consumption = data.getLong("currentValue");
            m_unit = data.getString("unit");
        }
        catch (JSONException e)
        {
            Log.getLogger().error(LOG_TAG,"Error while parsing consumption data: ",e);
        }
    }

    public long getQuota()
    {
        return m_quota;
    }

    public long getConsumption()
    {
        return m_consumption;
    }

    public String getUnit()
    {
        return m_unit;
    }
}

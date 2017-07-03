package com.ale.infra.proxy.profile;

import com.ale.infra.application.RainbowContext;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import static com.ale.infra.proxy.framework.RestResponse.FEATURES_ADDED_DATE;
import static com.ale.infra.proxy.framework.RestResponse.FEATURES_DATA;
import static com.ale.infra.proxy.framework.RestResponse.FEATURES_ID;
import static com.ale.infra.proxy.framework.RestResponse.FEATURES_ISENABLED;
import static com.ale.infra.proxy.framework.RestResponse.FEATURES_LASTUPDATEDATE;
import static com.ale.infra.proxy.framework.RestResponse.FEATURES_LIMIT_MAX;
import static com.ale.infra.proxy.framework.RestResponse.FEATURES_LIMIT_MIN;
import static com.ale.infra.proxy.framework.RestResponse.FEATURES_NAME;
import static com.ale.infra.proxy.framework.RestResponse.FEATURES_TYPE;
import static com.ale.infra.proxy.framework.RestResponse.FEATURES_UNIQUEREF;

/**
 * Created by georges on 20/02/2017.
 */

public class FeatureResponse {
    private static final String LOG_TAG = "FeatureResponse";


    private List<Feature> m_features = new ArrayList<>();

    public FeatureResponse(String response) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, ">FeatureResponse; "+response);

        JSONObject json = new JSONObject(response);
        JSONArray features = json.getJSONArray(FEATURES_DATA);
        if ( features != null) {
            for (int i = 0; i < features.length(); i++) {
                Feature featureResponse = parseFeature(features.get(i).toString());

                m_features.add(featureResponse);
            }
        }
    }

    private Feature parseFeature(String data) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, ">parseFeature; "+data);

        Feature feature = new Feature();

        JSONObject obj = new JSONObject(data);
        if (obj.has(FEATURES_ID))
            feature.setId(obj.getString(FEATURES_ID));

        if (obj.has(FEATURES_UNIQUEREF))
            feature.setUniqueRef(obj.getString(FEATURES_UNIQUEREF));

        if (obj.has(FEATURES_NAME))
            feature.setName(obj.getString(FEATURES_NAME));

        if (obj.has(FEATURES_TYPE))
            feature.setType(obj.getString(FEATURES_TYPE));

        if (obj.has(FEATURES_ISENABLED))
            feature.setIsEnabled(obj.getBoolean(FEATURES_ISENABLED));

        if (obj.has(FEATURES_LIMIT_MIN))
            feature.setLimitMin(obj.getInt(FEATURES_LIMIT_MIN));

        if (obj.has(FEATURES_LIMIT_MAX))
            feature.setLimitMax(obj.getInt(FEATURES_LIMIT_MAX));


        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));

        if (obj.has(FEATURES_ADDED_DATE)) {
            String dateString = obj.getString(FEATURES_ADDED_DATE);
            if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(dateString))
            feature.setAddedDate(df.parse(dateString));
        }

        if (obj.has(FEATURES_LASTUPDATEDATE)) {
            String dateString = obj.getString(FEATURES_LASTUPDATEDATE);
            if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(dateString))
                feature.setLastUpdateDate(df.parse(dateString));
        }

        return feature;
    }

    public List<Feature> getFeatures() {
        return m_features;
    }
}

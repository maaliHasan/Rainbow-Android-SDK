package com.ale.infra.proxy.authentication;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Profile;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.util.log.Log;

import org.json.JSONObject;


public class ProfileResponse extends RestResponse {
    private static final String LOG_TAG = "ProfileResponse";

    private Profile m_profile;


    public ProfileResponse(String data) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, ">ProfileResponse; "+data);

        m_profile = new Profile();

        JSONObject obj = new JSONObject(data);
        JSONObject dataObj = obj.optJSONObject(FILE_DESCRIPTOR_DATA);
        if( dataObj == null) {
            dataObj = obj;
        }
        if ( dataObj != null) {
            if (dataObj.has(SUBSCRIPTION_ID))
                m_profile.setSubscriptionId(dataObj.getString(SUBSCRIPTION_ID));

            if (dataObj.has(OFFER_ID))
                m_profile.setOfferId(dataObj.getString(OFFER_ID));

            if (dataObj.has(OFFER_NAME))
                m_profile.setOfferName(dataObj.getString(OFFER_NAME));

            if (dataObj.has(PROFILE_ID))
                m_profile.setProfileId(dataObj.getString(PROFILE_ID));

            if (dataObj.has(PROFILE_NAME))
                m_profile.setProfileName(dataObj.getString(PROFILE_NAME));

            if (dataObj.has(ASSIGNATION_DATE))
                m_profile.setAssignationDate(dataObj.getString(ASSIGNATION_DATE));

            if (dataObj.has(PROFILE_STATUS))
                m_profile.setProfileStatus(dataObj.getString(PROFILE_STATUS));

            if (dataObj.has(ISDEFAULT))
                m_profile.setIsDefault(dataObj.getBoolean(ISDEFAULT));
        }
    }


    public Profile getProfile() {
        return m_profile;
    }
}

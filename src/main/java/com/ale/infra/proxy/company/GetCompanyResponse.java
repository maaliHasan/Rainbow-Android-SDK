package com.ale.infra.proxy.company;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.invitation.CompanyContact;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by trunk1 on 13/02/2017.
 */

public class GetCompanyResponse  extends RestResponse {

    private static final String LOG_TAG = "GetCompanyResponse";

    private List<CompanyContact> companyList;

    public GetCompanyResponse(String data) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, "Parsing company response; ");

        companyList = new ArrayList<>();

        JSONObject obj = new JSONObject(data);
        JSONArray array = obj.optJSONArray("data");
        if (array != null) {//is an array
            for (int i= 0; i < array.length() ; i++){
                CompanyContact request =  getCompanyDataResponse(array.get(i).toString());
                companyList.add(request);
            }
        } else {//is an object
            JSONObject object = obj.getJSONObject("data");
            CompanyContact request =  getCompanyDataResponse(object.toString());
            companyList.add(request);
        }
    }

    private synchronized CompanyContact getCompanyDataResponse(String data) throws Exception{

        JSONObject obj = new JSONObject(data);
        CompanyContact request = new CompanyContact();

        if (obj.has(ID))
            request.setId(obj.getString(ID));

        if (obj.has(NAME))
            request.setName(obj.getString(NAME));

        if (obj.has(DESCRITPION))
            request.setDescription(obj.getString(DESCRITPION));

        if (obj.has(OFFER_TYPE))
            request.setOfferType(CompanyContact.CompanyContactOfferType.fromString(obj.getString(OFFER_TYPE)));

        if (obj.has(STATUS))
            request.setStatus(CompanyContact.CompanyContactStatus.fromString(obj.getString(STATUS)));

        if (obj.has(WEB_SITE))
            request.setWebSite(obj.getString(WEB_SITE));

        if (obj.has(RestResponse.COUNTRY))
            request.setCountry(obj.getString(RestResponse.COUNTRY));

        if (obj.has(ORGANISATION_ID))
            request.setOrganisationId(obj.getString(ORGANISATION_ID));

        if (obj.has(VISIBILITY))
            request.setVisibility(CompanyContact.CompanyContactVisibility.fromString(obj.getString(VISIBILITY)));

//        if (obj.has(VISIBILITY_BY))
//            request.setVisibleBy(obj.getString(VISIBILITY_BY));
//
//        if (obj.has(VISIBILITY_REQUESTS))
//            request.setVisibilityRequests(obj.getString(VISIBILITY_REQUESTS));

        if (obj.has(FORCE_HANDSHAKE))
            request.setForceHandshake(Boolean.valueOf(obj.getString(FORCE_HANDSHAKE)));

        if (obj.has(ADMIN_EMAIL))
            request.setAdminEmail(obj.getString(ADMIN_EMAIL));

        if (obj.has(SUPPORT_EMAIL))
            request.setSupportEmail(obj.getString(SUPPORT_EMAIL));

        if (obj.has(NUMBER_USERS))
            request.setNumberUsers(Integer.valueOf(obj.getString(NUMBER_USERS)));

        if (obj.has(COMPANY_SIZE))
            request.setCompanySize(obj.getString(COMPANY_SIZE));

        if (obj.has(SLOGAN))
            request.setSlogan(obj.getString(SLOGAN));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));

        if (obj.has(CREATION_DATE)) {
            String dateString = obj.getString(CREATION_DATE);
            if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(dateString))
                request.setCreationDate(df.parse(dateString));
        }

        if (obj.has(STATUS_UPDATE_DATE)) {
            String dateString = obj.getString(STATUS_UPDATE_DATE);
            if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(dateString))
                request.setStatusUpdateDate(df.parse(dateString));
        }

        if (obj.has(LAST_AVATAR_UPDATE_DATE)) {
            String dateString = obj.getString(LAST_AVATAR_UPDATE_DATE);
            if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(dateString)) {
                request.setLastAvatarUpdateDate(df.parse(dateString));
                request.seLastAvatarUpdateDateString(dateString);
            }

        }

        if (obj.has(LAST_BANNER_UPDATE_DATE)) {
            String dateString = obj.getString(LAST_BANNER_UPDATE_DATE);
            if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(dateString))
                request.setLastBannerUpdateDate(df.parse(dateString));
        }

        return request;
    }

    public List<CompanyContact> getCompanyList () { return companyList;}

}

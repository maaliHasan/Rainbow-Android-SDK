package com.ale.infra.proxy.company;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.invitation.CompanyContact;
import com.ale.infra.invitation.CompanyJoinRequest;
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
 * Created by trunk1 on 10/02/2017.
 */

public class GetCompanyJoinRequestResponse extends RestResponse {

    private static final String LOG_TAG = "GetCompanyInvitationsResponse";

    private List<CompanyJoinRequest> requestList;

    public GetCompanyJoinRequestResponse(String data) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, "Parsing company join request response; "+data);

        requestList = new ArrayList<>();

        JSONObject obj = new JSONObject(data);
        JSONArray array = obj.optJSONArray("data");
        if (array != null) {//is an array
            for (int i= 0; i < array.length() ; i++){
                CompanyJoinRequest request =  getCompanyJoinRequestDataResponse(array.get(i).toString());
                requestList.add(request);
            }
        } else {//is an object
            JSONObject object = obj.getJSONObject("data");
            CompanyJoinRequest request =  getCompanyJoinRequestDataResponse(object.toString());
            requestList.add(request);
        }
    }

    private synchronized CompanyJoinRequest getCompanyJoinRequestDataResponse(String data) throws Exception{

        JSONObject obj = new JSONObject(data);
        CompanyJoinRequest request = new CompanyJoinRequest();

        if (obj.has(ID))
            request.setId(obj.getString(ID));

        if (obj.has(JOIN_REQUEST_USER_ID))
            request.setRequestingUserId(obj.getString(JOIN_REQUEST_USER_ID));

        if (obj.has(JOIN_REQUEST_USER_LOGIN_EMAIL))
            request.setRequestingUserLoginEmail(obj.getString(JOIN_REQUEST_USER_LOGIN_EMAIL));

        if (obj.has(JOIN_REQUEST_COMPANY_ID))
            request.setRequestedCompanyId(obj.getString(JOIN_REQUEST_COMPANY_ID));

        if (obj.has(JOIN_REQUEST_COMPANY_NAME))
            request.setRequestedCompanyName(obj.getString(JOIN_REQUEST_COMPANY_NAME));

        if (obj.has(JOIN_REQUEST_TO_COMPANY_ADMIN))
            request.setRequestedToCompanyAdmin(obj.getString(JOIN_REQUEST_TO_COMPANY_ADMIN));

        if (obj.has(JOIN_REQUEST_COMPANY_ADMIN_ID))
            request.setCompanyAdminId(obj.getString(JOIN_REQUEST_COMPANY_ADMIN_ID));

        if (obj.has(JOIN_REQUEST_COMPANY_ADMIN_LOGIN_EMAIL))
            request.setCompanyAdminLoginEmail(obj.getString(JOIN_REQUEST_COMPANY_ADMIN_LOGIN_EMAIL));

        if (obj.has(INVITATION_STATUS))
            request.setStatus(CompanyJoinRequest.CompanyJoinRequestStatus.fromString(obj.getString(INVITATION_STATUS)));

        if (obj.has(INVITATION_REQUESTED_NOTIFICATION_LANGUAGE))
            request.setRequestedNotificationLanguage(obj.getString(INVITATION_REQUESTED_NOTIFICATION_LANGUAGE));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));

        if (obj.has(JOIN_REQUEST_REQUESTING_DATE)) {
            String dateString = obj.getString(JOIN_REQUEST_REQUESTING_DATE);
            if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(dateString))
                request.setRequestingDate(df.parse(dateString));
        }

        if (obj.has(INVITATION_LAST_NOTIFICATION_DATE)) {
            String dateString = obj.getString(INVITATION_LAST_NOTIFICATION_DATE);
            if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(dateString))
                request.setLastNotificationDate(df.parse(dateString));
        }

        return request;
    }

    public List<CompanyJoinRequest> getRequestList () { return requestList;}

}

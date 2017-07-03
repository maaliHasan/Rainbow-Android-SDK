package com.ale.infra.proxy.users;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.invitation.Invitation;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.infra.invitation.CompanyInvitation;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by trunk1 on 01/12/2016.
 */

public class GetCompanyInvitationsResponse extends RestResponse {
    private static final String LOG_TAG = "GetCompanyInvitationsResponse";

    private List<CompanyInvitation> invitationList;

    public GetCompanyInvitationsResponse(String data) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, "Parsing company invitation; "+data);

        invitationList = new ArrayList<>();

        JSONObject obj = new JSONObject(data);
        JSONArray array = obj.optJSONArray("data");
        if (array != null) {//is an array
            for (int i= 0; i < array.length() ; i++){
                CompanyInvitation invitation =  getCompanyInvitationDataResponse(array.get(i).toString());
                invitationList.add(invitation);
            }
        } else {//is an object
            JSONObject object = obj.getJSONObject("data");
            CompanyInvitation invitation =  getCompanyInvitationDataResponse(object.toString());
            invitationList.add(invitation);
        }
    }

    private CompanyInvitation getCompanyInvitationDataResponse(String data) throws Exception{

        JSONObject obj = new JSONObject(data);
        CompanyInvitation invitation = new CompanyInvitation();

        if (obj.has(ID))
            invitation.setId(obj.getString(ID));

        if (obj.has(INVITED_USER_ID))
            invitation.setInvitedUserId(obj.getString(INVITED_USER_ID));

        if (obj.has(INVITING_ADMIN_ID))
            invitation.setInvitingUserId(obj.getString(INVITING_ADMIN_ID));

        if (obj.has(INVITED_USER_EMAIL))
            invitation.setInvitedUserEmail(obj.getString(INVITED_USER_EMAIL));

        if (obj.has(INVITING_ADMIN_LOGIN_EMAIL))
            invitation.setInvitingUserEmail(obj.getString(INVITING_ADMIN_LOGIN_EMAIL));

        if (obj.has(INVITATION_REQUESTED_NOTIFICATION_LANGUAGE))
            invitation.setRequestedNotificationLanguage(obj.getString(INVITATION_REQUESTED_NOTIFICATION_LANGUAGE));

        if (obj.has(INVITATION_STATUS))
            invitation.setStatus(Invitation.InvitationStatus.fromString(obj.getString(INVITATION_STATUS)));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));

        if (obj.has(INVITATION_DATE)) {
            String dateString = obj.getString(INVITATION_DATE);
            if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(dateString))
                invitation.setInvitingDate(df.parse(dateString));
        }

        if (obj.has(INVITATION_LAST_NOTIFICATION_DATE)) {
            String dateString = obj.getString(INVITATION_LAST_NOTIFICATION_DATE);
            if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(dateString))
                invitation.setLastNotificationDate(df.parse(dateString));
        }

        if (obj.has(INVITATION_ACCEPTANCE_DATE)) {
            String dateString = obj.getString(INVITATION_ACCEPTANCE_DATE);
            if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(dateString))
                invitation.setAcceptationDate(df.parse(dateString));
        }

        if (obj.has(INVITATION_DECLINATION_DATE)) {
            String dateString = obj.getString(INVITATION_DECLINATION_DATE);
            if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(dateString))
                invitation.setDeclinationDate(df.parse(dateString));
        }

        if (obj.has(COMPANY_ID))
            invitation.setCompanyId(obj.getString(COMPANY_ID));

        return invitation;
    }

    public List<CompanyInvitation> getInvitationList () { return invitationList;}
}

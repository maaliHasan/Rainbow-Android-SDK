package com.ale.infra.proxy.users;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.infra.invitation.Invitation;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by trunk1 on 13/10/2016.
 */

public class GetUserInvitationsResponse extends RestResponse {
    private static final String LOG_TAG = "GetUserInvitationsResponse";

    private List<Invitation> invitationList;

    public GetUserInvitationsResponse (String data) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, "Parsing User invitations; "+ data);

        invitationList = new ArrayList<>();

        JSONObject obj = new JSONObject(data);
        JSONArray array = obj.optJSONArray("data");
        if (array != null) {//is an array
            for (int i= 0; i < array.length() ; i++){
                Invitation invitation =  getInvitationDataResponse(array.get(i).toString());
                invitationList.add(invitation);
            }
        } else {//is an object
            JSONObject object = obj.getJSONObject("data");
            Invitation invitation =  getInvitationDataResponse(object.toString());
            invitationList.add(invitation);
        }
    }

    public Invitation getInvitationDataResponse(String data) throws Exception{
        JSONObject obj = new JSONObject(data);
        Invitation invitation = new Invitation();

        if (obj.has(ID))
            invitation.setId(obj.getString(ID));

        if (obj.has(INVITED_USER_ID))
            invitation.setInvitedUserId(obj.getString(INVITED_USER_ID));

        if (obj.has(INVITING_USER_ID))
            invitation.setInvitingUserId(obj.getString(INVITING_USER_ID));

        if (obj.has(INVITED_USER_EMAIL))
            invitation.setInvitedUserEmail(obj.getString(INVITED_USER_EMAIL));

        if (obj.has(INVITING_USER_EMAIL))
            invitation.setInvitingUserEmail(obj.getString(INVITING_USER_EMAIL));

        if (obj.has(INVITATION_REQUESTED_NOTIFICATION_LANGUAGE))
            invitation.setRequestedNotificationLanguage(obj.getString(INVITATION_REQUESTED_NOTIFICATION_LANGUAGE));

        if (obj.has(INVITATION_STATUS))
            invitation.setStatus(Invitation.InvitationStatus.fromString(obj.getString(INVITATION_STATUS)));

        if (obj.has(INVITATION_TYPE))
            invitation.setInvitationType(Invitation.InvitationType.fromString(obj.getString(INVITATION_TYPE)));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));

        if (obj.has(INVITING_DATE)) {
            String dateString = obj.getString(INVITING_DATE);
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

        return invitation;
    }

    public List<Invitation> getInvitationList () { return invitationList;}

}

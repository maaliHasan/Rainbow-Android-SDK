package com.ale.infra.proxy.users;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.invitation.Invitation;
import com.ale.infra.proxy.framework.RestResponse;

import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by wilsius on 16/12/16.
 */

public class GetUserInvitationResponse extends RestResponse {
    private static final String LOG_TAG = "GetUserInvitationResponse";



    private Invitation invitation;

    public GetUserInvitationResponse (String data) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, ">GetUserInvitationResponse; "+data);

        Invitation invitation = new Invitation();
        JSONObject obj = new JSONObject(data);
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

    }

    public Invitation getInvitation() {
        return invitation;
    }
}

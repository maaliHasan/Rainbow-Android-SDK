package com.ale.infra.contact;

import android.graphics.Bitmap;

/**
 * Created by letrongh on 27/03/2017.
 */

public interface IRainbowContact {
    String getCorporateId();
    String getContactId();
    String getImJabberId();
    Bitmap getPhoto();
    String getFirstName();
    String getLastName();
    String getLoginEmail();
    RainbowPresence getPresence();
    String getCompanyName();
    boolean isBot();
    String getMainEmailAddress();
    String getFirstEmailAddress();
    String getJobTitle();
    String getNickName();
    void setInvitationId(String invitationId);
    String getInvitationId();
    void registerChangeListener(Contact.ContactListener contactListener);
    void unregisterChangeListener(Contact.ContactListener contactListener);
}

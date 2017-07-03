package com.ale.rainbowsdk;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.contact.RainbowPresence;
import com.ale.infra.proxy.avatar.IAvatarProxy;
import com.ale.infra.proxy.users.IUserProxy;
import com.ale.util.log.Log;

import java.io.File;


public class MyProfile {

    private static final String LOG_TAG = "MyProfile";

    /**
     * Retrieve the connected user information
     *
     * @return the connected user
     */
    public IRainbowContact getConnectedUser() {
        return RainbowContext.getInfrastructure().getContactCacheMgr().getUser();
    }

    /**
     * Change the presence of the connected user
     *
     * @param presence : updated presence for the connected user
     */
    public void setPresenceTo(RainbowPresence presence) {
        Log.getLogger().verbose(LOG_TAG, "presence clicked; " + presence.getPresence());

        if (getConnectedUser() != null && getConnectedUser().getPresence() != null && !getConnectedUser().getPresence().equals(presence)) {
            RainbowContext.getInfrastructure().getXmppContactMgr().sendPresence(presence);
        }
    }

    /**
     * Update the last name of connected user
     *
     * @param lastName : new lastname for myself
     * @param listener : listener used to know the update result
     */
    public void updateLastName(String lastName, IUserProxy.IUsersListener listener) {
        RainbowContext.getInfrastructure().getUsersProxy().updateUser(getConnectedUser().getCorporateId(), null, lastName, null, null, null, null, null, null, null, null, null, null, null, null, true, listener);
    }

    /**
     * Update the first name of connected user
     *
     * @param firstName : new firstname for myself
     * @param listener : listener used to know the update result
     */
    public void updateFirstName(String firstName, IUserProxy.IUsersListener listener) {
        RainbowContext.getInfrastructure().getUsersProxy().updateUser(getConnectedUser().getCorporateId(), firstName, null, null, null, null, null, null, null, null, null, null, null, null, null, true, listener);
    }

    /**
     * Update the nick name of connected user
     *
     * @param nickName : new nickname for myself
     * @param listener : listener used to know the update result
     */
    public void updateNickName(String nickName, IUserProxy.IUsersListener listener) {
        RainbowContext.getInfrastructure().getUsersProxy().updateUser(getConnectedUser().getCorporateId(), null, null, null, nickName, null, null, null, null, null, null, null, null, null, null, true, listener);
    }

    /**
     * Update the job title (function) of connected user
     *
     * @param jobTitle : new job title for myself
     * @param listener : listener used to know the update result
     */
    public void updateJobTitle(String jobTitle, IUserProxy.IUsersListener listener) {
        RainbowContext.getInfrastructure().getUsersProxy().updateUser(getConnectedUser().getCorporateId(), null, null, null, null, null, jobTitle, null, null, null, null, null, null, null, null, true, listener);
    }

    /**
     * Update the photo of connected user
     *
     * @param photo    : new photo for myself
     * @param listener : listener used to know the update result
     */
    public void updatePhoto(File photo, IAvatarProxy.IAvatarListener listener) {
        RainbowContext.getInfrastructure().getAvatarProxy().uploadAvatar((Contact)getConnectedUser(), photo, listener);
    }
}

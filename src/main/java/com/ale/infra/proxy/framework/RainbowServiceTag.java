package com.ale.infra.proxy.framework;

/**
 * Created by grobert on 26/10/15.
 */
public enum RainbowServiceTag
{
    AUTHENTICATE("authenticate"),
    DIRECTORY("directory"),
    CONVERSATION("conversation"),
    NOTIFCATIONS("notifications"),
    AVATAR("avatar"),
    ROOMS("rooms"),
    BOTS("bots"),
    USERS("users"),
    PGI("pgi"),
    SETTINGS("settings");

    protected String tag;

    RainbowServiceTag(String t)
    {
        this.tag = t;
    }

    @Override
    public String toString()
    {
        return tag;
    }
}

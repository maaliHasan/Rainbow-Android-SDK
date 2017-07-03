package com.ale.infra.contact;

import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.jivesoftware.smack.packet.Presence;

/**
 * Created by georges on 28/09/2016.
 */

public enum RainbowPresence {
    OFFLINE("offline"),
    ONLINE("online"),
    MOBILE_ONLINE("mobile_online"),
    AWAY("away"),
    MANUAL_AWAY("manual_away"),
    XA("xa"),
    DND("DoNotDisturb"),
    DND_PRESENTATION("DND_presentation"),
    BUSY("busy"),
    BUSY_AUDIO("busy_audio"),
    BUSY_VIDEO("busy_video"),
    BUSY_PHONE("busy_phone"),
    SUBSCRIBE("subscribe"),
    UNSUBSCRIBED("unsubscribed");

    private static final String LOG_TAG = "RainbowPresence";

    protected String presence;

    RainbowPresence(String presence)
    {
        this.presence = presence;
    }

    @Override
    public String toString()
    {
        return presence;
    }

    public static RainbowPresence fromString(String presence) {
        if (presence != null) {
            for (RainbowPresence status : RainbowPresence.values()) {
                if (presence.equalsIgnoreCase(status.presence)) {
                    return status;
                }
            }
        }
        return null;
    }

    public boolean isOffline() {
        return this.presence.equals(OFFLINE.toString());
    }

    public boolean isOnline() {
        return this.presence.equals(ONLINE.toString());
    }

    public boolean isAway() {
        return this.presence.equals(AWAY.toString()) || this.presence.equals(MANUAL_AWAY.toString());
    }

    public boolean isManualAway() {
        return this.presence.equals(MANUAL_AWAY.toString());
    }

    public boolean isXA() {
        return this.presence.equals(XA.toString());
    }

    public boolean isDNDonly() {
        return this.presence.equals(DND.toString());
    }

    public boolean isDND() {
        return isDNDonly() || isDNDPresentation() ;
    }

    public boolean isDNDPresentation() {
        return this.presence.equals(DND_PRESENTATION.toString());
    }

    public boolean isSubscribe() {
        return this.presence.equals(SUBSCRIBE.toString());
    }

    public boolean isMobileOnline() {
        return this.presence.equals(MOBILE_ONLINE.toString());
    }

    public boolean isBusyOnly() {
        return this.presence.equals(BUSY.toString());
    }

    public boolean isBusy() {
        return isBusyOnly()|| isBusyAudio() || isBusyPhone()  || isBusyVideo() ;
    }

    public boolean isBusyPhone() {
        return this.presence.equals(BUSY_PHONE.toString());
    }


    public boolean isBusyAudio() {
        return this.presence.equals(BUSY_AUDIO.toString());
    }
    public boolean isBusyVideo() {
        return this.presence.equals(BUSY_VIDEO.toString());
    }
    public boolean isUnsubscribed() {
        return this.presence.equals(UNSUBSCRIBED.toString());
    }

    public boolean isSubscribed() {
        return !this.presence.equals(UNSUBSCRIBED.toString()) && !this.presence.equals(SUBSCRIBE.toString());
    }


    public static RainbowPresence getPresenceFrom(Presence presence, boolean isUser) {
        Presence.Type presenceType = presence.getType();
        Presence.Mode presenceMode = presence.getMode();
        String status = presence.getStatus();
        if(StringsUtil.isNullOrEmpty(status))
            status = "";

        if ("EVT_SERVICE_INITIATED".equals(status) || "EVT_ESTABLISHED".equals(status)) {
            return BUSY_PHONE;
        } else if( presenceType.equals(Presence.Type.unavailable)) {
            return OFFLINE;
        } else if( presenceType.equals(Presence.Type.subscribe)) {
            return SUBSCRIBE;
        } else if( presenceType.equals(Presence.Type.subscribed)) {
            return OFFLINE;
        } else if (status.equals("mode=auto")) {
            return ONLINE;
        } else if( presenceType.equals(Presence.Type.available)) {
            if( presenceMode.equals(Presence.Mode.available)) {
                return ONLINE;
            } else if( presenceMode.equals(Presence.Mode.away) &&
                    status.equals("")) {
                return AWAY;
            } else if( presenceMode.equals(Presence.Mode.xa) &&
                    status.equals("")) {
                if(isUser)
                    return XA;
                else
                    return OFFLINE;
            } else if( presenceMode.equals(Presence.Mode.xa) &&
                    status.equals("away")) {
                return MANUAL_AWAY;
            } else if( presenceMode.equals(Presence.Mode.dnd) ) {
                if (status.equals("audio"))
                    return BUSY_AUDIO;
                else if (status.equals("video"))
                    return BUSY_VIDEO;
                else if (status.equals("sharing") || status.equals("presentation"))
                    return DND_PRESENTATION;
                else
                    return DND;
            }
        }

        Log.getLogger().warn(LOG_TAG, "Presence translation not found");
        return OFFLINE;
    }

    public Presence.Type getXmppType() {
        Presence.Type type = Presence.Type.available;
        if( isOffline())
            type = Presence.Type.unavailable;
        else if( isSubscribe())
            type = Presence.Type.subscribe;

        return type;
    }

    public Presence.Mode getXmppMode() {
        Presence.Mode mode = Presence.Mode.available;

        if( isAway() || isXA() )
            mode = Presence.Mode.xa;
        else if( isDND())
            mode = Presence.Mode.dnd;

        return mode;
    }

    public String getXmppStatus() {
        String status = "";

        if(isAway()) {
            status = "away";
        }
        else if (isOnline()) {
            status = "mode=auto";
        }

        return status;
    }

    public String getPresence()
    {
        return presence;
    }
}

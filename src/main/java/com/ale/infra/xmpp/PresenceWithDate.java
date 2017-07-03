package com.ale.infra.xmpp;

import com.ale.infra.contact.RainbowPresence;

import org.jivesoftware.smack.packet.Presence;

import java.security.acl.LastOwnerException;
import java.util.Date;

public class PresenceWithDate {

    private RainbowPresence m_presence;
    private long m_date;
    private boolean m_hasJidTel = false;

    public PresenceWithDate(RainbowPresence pres,long date, boolean hasJidTel) {
        m_presence = pres;
        m_date = date;
        m_hasJidTel = hasJidTel;
    }

    public RainbowPresence getPresence() {
        return m_presence;
    }

    public void setPresence(RainbowPresence presence) {
        this.m_presence = presence;
    }

    public long getDate() {
        return m_date;
    }

    public void setDate(long date) {
        this.m_date = date;
    }

    public boolean hasJidTel() {
        return m_hasJidTel;
    }

    public void setHasJidTel(boolean hasJidTel) {
        m_hasJidTel = hasJidTel;
    }
}

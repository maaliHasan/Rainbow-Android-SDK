package com.ale.infra.manager.pgiconference;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.xmpp.xep.pgiconference.ConferenceParticipant;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.util.Date;

/**
 * Created by georges on 03/05/2017.
 */

public class PgiConferenceParticipant {

    private static final String LOG_TAG = "PgiConferenceParticipant";

    private static final String ROLE_LEADER = "leader";
    private static final String ROLE_MODERATOR = "moderator";

    private static final String STATE_RINGING = "ringing";
    private static final String STATE_CONNECTED = "connected";



    private final IContactCacheMgr m_contactCache;
    private Contact m_contact;


    private String userId;
    private String jidIm;
    private String jidTel;
    private String role;
    private boolean muted;
    private boolean hold;
    private Date startDate;
    private Date confStartDate;
    private String phoneNumber;
    private String state;
    private boolean talking;

    private boolean m_showActions = false;

    public PgiConferenceParticipant() {
        m_contactCache = RainbowContext.getInfrastructure().getContactCacheMgr();
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setJidIm(String jidIm) {
        this.jidIm = jidIm;
    }

    public String getJidIm() {
        return jidIm;
    }

    public void setJidTel(String jidTel) {
        this.jidTel = jidTel;
    }

    public String getJidTel() {
        return jidTel;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setHold(boolean hold) {
        this.hold = hold;
    }

    public boolean isHold() {
        return hold;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setConfStartDate(Date confStartDate) {
        this.confStartDate = confStartDate;
    }

    public Date getConfStartDate() {
        return confStartDate;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public boolean isRinging() {
        return !StringsUtil.isNullOrEmpty(this.state) && this.state.equals(STATE_RINGING);
    }

    public boolean isConnected() {
        return !StringsUtil.isNullOrEmpty(this.state) && this.state.equals(STATE_CONNECTED);
    }

    public void dumpInLog(String dumpLogTag) {
        if( userId != null ) {
            Log.getLogger().info(dumpLogTag, "    userId="+userId);
        }
        if( jidIm != null ) {
            Log.getLogger().info(dumpLogTag, "    jidIm="+jidIm);
        }
        if( jidTel != null ) {
            Log.getLogger().info(dumpLogTag, "    jidTel="+jidTel);
        }
        if( role != null ) {
            Log.getLogger().info(dumpLogTag, "    role="+role);
        }
        Log.getLogger().info(dumpLogTag, "    muted="+muted);
        Log.getLogger().info(dumpLogTag, "    hold="+hold);

        if( startDate != null ) {
            Log.getLogger().info(dumpLogTag, "    startDate="+startDate.toString());
        }
        if( confStartDate != null ) {
            Log.getLogger().info(dumpLogTag, "    confStartDate="+confStartDate.toString());
        }
        if( phoneNumber != null ) {
            Log.getLogger().info(dumpLogTag, "    phoneNumber="+phoneNumber);
        }
        if( state != null ) {
            Log.getLogger().info(dumpLogTag, "    state="+state);
        }
    }

    public void update(ConferenceParticipant confPart) {
        Log.getLogger().verbose(LOG_TAG, ">update");

        setState(confPart.getCnxState());
        setHold(confPart.isHold());
        setMuted(confPart.isMuted());
        setRole(confPart.getRole());
        if(confPart.getParticipantId() != null)
            setUserId(confPart.getParticipantId());
        if(confPart.getJidIm() != null)
            setJidIm(confPart.getJidIm());
        if(confPart.getPhoneNumber() != null)
            setPhoneNumber(confPart.getPhoneNumber());
        // notify ???
    }

    public void setTalking(boolean talking) {
        this.talking = talking;
    }

    public boolean isTalking() {
        return talking;
    }

    public String getName() {
        if( m_contact == null) {
            getMatchingContact();
        }
        if( m_contact != null) {
            return m_contact.getDisplayName("");
        } else if( phoneNumber != null) {
            return phoneNumber;
        }
        return "";
    }

    public Contact getMatchingContact() {
        if( jidIm != null) {
            m_contact = m_contactCache.getContactFromJid(jidIm);
        }
        return m_contact;
    }

    public boolean isLeader() {

        if( !StringsUtil.isNullOrEmpty(role) &&
                (role.equals(ROLE_LEADER) || role.equals(ROLE_MODERATOR)))
        return true;

        return false;
    }

    public void showActions(boolean showActions) {
        m_showActions = showActions;
    }

    public boolean doesShowActions() {
        return m_showActions;
    }

    public void toggleShowActions() {
        m_showActions = !m_showActions;
    }
}

package com.ale.infra.manager.pgiconference;

import android.support.annotation.Nullable;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.manager.room.RoomConfEndPoint;
import com.ale.infra.xmpp.xep.pgiconference.ConferenceParticipant;
import com.ale.infra.xmpp.xep.pgiconference.ConferenceState;
import com.ale.infra.xmpp.xep.pgiconference.PgiConferenceInfoExtension;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by georges on 26/04/2017.
 */

public class PgiConference {
    private static final String LOG_TAG = "PgiConference";


    private String m_providerUserId;
    private String m_id;
    private String m_providerConfId;
    private String m_providerType;
    private String m_confUserId;
    private String m_userId;
    private String m_companyId;
    private String m_mediaType;
    private List<PgiConferenceParticipant> m_participants = new ArrayList<>();
    private ConferenceState confState;

    private Set<PgiConferenceListener> m_changeListeners = new HashSet<>();


    public PgiConference() {
    }

    public PgiConference(RoomConfEndPoint confEndpoint) {
        m_id = confEndpoint.getConfEndpointId();
        m_mediaType = confEndpoint.getMediaType();
        m_userId = confEndpoint.getUserId();

        //confEndpoint.getPrivilege();
        //confEndpoint.getAdditionDate();
    }


    public String getProviderUserId() {
        return m_providerUserId;
    }

    public void setProviderUserId(String providerUserId) {
        m_providerUserId = providerUserId;
    }

    public String getId() {
        return m_id;
    }

    public void setId(String id) {
        m_id = id;
    }

    public String getProviderConfId() {
        return m_providerConfId;
    }

    public void setProviderConfId(String providerConfId) {
        m_providerConfId = providerConfId;
    }

    public String getProviderType() {
        return m_providerType;
    }

    public void setProviderType(String providerType) {
        m_providerType = providerType;
    }

    public String getConfUserId() {
        return m_confUserId;
    }

    public void setConfUserId(String confUserId) {
        m_confUserId = confUserId;
    }

    public String getUserId() {
        return m_userId;
    }

    public void setUserId(String userId) {
        m_userId = userId;
    }

    public String getCompanyId() {
        return m_companyId;
    }

    public void setCompanyId(String companyId) {
        m_companyId = companyId;
    }

    public String getMediaType() {
        return m_mediaType;
    }

    public void setMediaType(String mediaType) {
        m_mediaType = mediaType;
    }

    public List<PgiConferenceParticipant> getAllParticipants() {
        return m_participants;
    }

    public void setParticipants(List<PgiConferenceParticipant> participants) {
        this.m_participants = participants;
    }

    public void addParticipant(PgiConferenceParticipant participant) {
        this.m_participants.add(participant);
    }

    public void setConfState(ConferenceState confState) {
        this.confState = confState;
    }

    public ConferenceState getConfState() {
        return confState;
    }

    public PgiConferenceParticipant getParticipant(ConferenceParticipant currentConfPart) {
        Log.getLogger().verbose(LOG_TAG, ">getParticipant");
        if( currentConfPart == null) {
            Log.getLogger().warn(LOG_TAG, "confPart is NULL");
            return null;
        }

        PgiConferenceParticipant pgiConferencePart = null;
        if( !StringsUtil.isNullOrEmpty(currentConfPart.getJidIm())) {
            pgiConferencePart = getPgiConferenceParticipantByJid(currentConfPart.getJidIm());
            if( pgiConferencePart != null)
                return pgiConferencePart;
        }
        if( !StringsUtil.isNullOrEmpty(currentConfPart.getPhoneNumber())) {
            pgiConferencePart = getPgiConferenceParticipantByPhoneNumber(currentConfPart.getPhoneNumber());
            if( pgiConferencePart != null)
                return pgiConferencePart;
        }
        if( !StringsUtil.isNullOrEmpty(currentConfPart.getParticipantId())) {
            pgiConferencePart = getPgiConferenceParticipantByUserId(currentConfPart.getParticipantId());
            if( pgiConferencePart != null)
                return pgiConferencePart;
        }

        return null;
    }


    @Nullable
    private PgiConferenceParticipant getPgiConferenceParticipantByJid(String jid) {
        Log.getLogger().verbose(LOG_TAG, ">getPgiConferenceParticipantByJid");
        for(PgiConferenceParticipant pgiConfPart : m_participants) {
            if (!StringsUtil.isNullOrEmpty(pgiConfPart.getJidIm()) && pgiConfPart.getJidIm().equals(jid)) {
                Log.getLogger().verbose(LOG_TAG, "participant found");
                return pgiConfPart;
            }
        }
        return null;
    }

    @Nullable
    private PgiConferenceParticipant getPgiConferenceParticipantByPhoneNumber(String phoneNb) {
        Log.getLogger().verbose(LOG_TAG, ">getPgiConferenceParticipantByPhoneNumber");
        for(PgiConferenceParticipant pgiConfPart : m_participants) {
            if (!StringsUtil.isNullOrEmpty(pgiConfPart.getPhoneNumber()) && pgiConfPart.getPhoneNumber().equals(phoneNb)) {
                Log.getLogger().verbose(LOG_TAG, "participant found");
                return pgiConfPart;
            }
        }
        return null;
    }

    @Nullable
    private PgiConferenceParticipant getPgiConferenceParticipantByUserId(String participantId) {
        Log.getLogger().verbose(LOG_TAG, ">getPgiConferenceParticipantByUserId");
        for(PgiConferenceParticipant pgiConfPart : m_participants) {
            if (!StringsUtil.isNullOrEmpty(pgiConfPart.getUserId()) && pgiConfPart.getUserId().equals(participantId)) {
                Log.getLogger().verbose(LOG_TAG, "participant found");
                return pgiConfPart;
            }
        }
        return null;
    }


    public synchronized void notifyConferenceUpdated()
    {
        Log.getLogger().verbose(LOG_TAG, ">notifyConferenceUpdated");

        for (PgiConferenceListener listener : m_changeListeners.toArray(new PgiConferenceListener[m_changeListeners.size()]))
        {
            listener.conferenceUpdated(PgiConference.this);
        }
    }

    public synchronized void registerChangeListener(PgiConferenceListener changeListener)
    {
        if( !m_changeListeners.contains(changeListener)) {
            m_changeListeners.add(changeListener);
        }
    }

    public synchronized void unregisterChangeListener(PgiConferenceListener changeListener)
    {
        m_changeListeners.remove(changeListener);
    }

    public PgiConferenceParticipant getUserFromJid(String imJabberId) {
        if( StringsUtil.isNullOrEmpty(imJabberId))
            return null;

        for(PgiConferenceParticipant part : m_participants) {
            if( !StringsUtil.isNullOrEmpty(part.getJidIm()) && part.getJidIm().equals(imJabberId)) {
                return  part;
            }
        }
        return null;
    }


    public void dumpInLog(String dumpLogTag) {
        if( m_providerUserId != null ) {
            Log.getLogger().info(dumpLogTag, "    providerUserId="+m_providerUserId);
        }
        if( m_id != null ) {
            Log.getLogger().info(dumpLogTag, "    id="+m_id);
        }
        if( m_providerConfId != null ) {
            Log.getLogger().info(dumpLogTag, "    providerConfId="+m_providerConfId);
        }
        if( m_providerType != null ) {
            Log.getLogger().info(dumpLogTag, "    providerType="+m_providerType);
        }
        if( m_confUserId != null ) {
            Log.getLogger().info(dumpLogTag, "    confUserId="+m_confUserId);
        }
        if( m_userId != null ) {
            Log.getLogger().info(dumpLogTag, "    userId="+m_userId);
        }
        if( m_companyId != null ) {
            Log.getLogger().info(dumpLogTag, "    companyId="+m_companyId);
        }
        if( m_mediaType != null ) {
            Log.getLogger().info(dumpLogTag, "    mediaType="+m_mediaType);
        }
        if( confState != null ) {
            Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
            Log.getLogger().info(dumpLogTag, "    ConfState");
            confState.dumpInLog(dumpLogTag);
        }
        if( m_participants != null && m_participants.size() > 0 ) {
            Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
            Log.getLogger().info(dumpLogTag, "    participants=" + m_participants.size());
            for(PgiConferenceParticipant roomPart: m_participants) {
                roomPart.dumpInLog(dumpLogTag);
            }
        } else {
            Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
            Log.getLogger().info(dumpLogTag, "    participants Empty");
        }
    }

    public void update(PgiConferenceInfoExtension pgiConfInfoExt) {
        if( pgiConfInfoExt.getState() != null)
            setConfState(pgiConfInfoExt.getState());

        // Loop on Updated Participants :
        if( pgiConfInfoExt.getUpdatedParticipants() != null) {
            for (ConferenceParticipant currentConfPart : pgiConfInfoExt.getUpdatedParticipants()) {
                PgiConferenceParticipant pgiConfPart = getParticipant(currentConfPart);
                if (pgiConfPart == null) {
                    Log.getLogger().verbose(LOG_TAG, "Create New Participant");
                    pgiConfPart = new PgiConferenceParticipant();
                    addParticipant(pgiConfPart);
                }
                Log.getLogger().verbose(LOG_TAG, "Updating Participant state: " + pgiConfPart.getUserId());
                pgiConfPart.update(currentConfPart);
            }
        }
        // Loop on Removed Participants :
        if( pgiConfInfoExt.getRemovedParticipants() != null) {
            IContactCacheMgr contactCacheMgr = RainbowContext.getInfrastructure().getContactCacheMgr();
            Contact me = null;
            if( contactCacheMgr != null)
                me = contactCacheMgr.getUser();

            for (String currentRemovedPartId : pgiConfInfoExt.getRemovedParticipants()) {
                PgiConferenceParticipant pgiConfPart = getPgiConferenceParticipantByUserId(currentRemovedPartId);
                if (pgiConfPart != null) {
                    Log.getLogger().verbose(LOG_TAG, "Delete Participant");
                    m_participants.remove(pgiConfPart);

                    if( me != null && pgiConfPart.getJidIm().equals(me.getImJabberId())) {
                        m_participants.clear();

                        if( this.confState != null )
                            this.confState.setConfActive(false);
                    }
                }
            }
        }
        // Loop on Talkers Participants :
        if( pgiConfInfoExt.getTalkersParticipants() != null) {
            clearParticipantsTalkingState();
            for (String currentTalkerId : pgiConfInfoExt.getTalkersParticipants()) {
                PgiConferenceParticipant pgiConfPart = getPgiConferenceParticipantByUserId(currentTalkerId);
                if (pgiConfPart != null) {
                    Log.getLogger().verbose(LOG_TAG, "Update Participant");
                    pgiConfPart.setTalking(true);
                }
            }
        }

        //dumpInLog("PGICONFINFO");
        notifyConferenceUpdated();
    }

    private void clearParticipantsTalkingState() {
        for (PgiConferenceParticipant currentPart : m_participants) {
            currentPart.setTalking(false);
        }
    }

    public List<PgiConferenceParticipant> getLeaders() {

        List<PgiConferenceParticipant> parts = new ArrayList<>();
        for(PgiConferenceParticipant part : m_participants) {
            if( part.isLeader() )
                parts.add(part);
        }

        return parts;
    }

    public List<PgiConferenceParticipant> getParticipantsWithoutJid(String filterJid) {

        List<PgiConferenceParticipant> parts = new ArrayList<>();
        for(PgiConferenceParticipant part : m_participants) {
            if( !part.isLeader() ) {
                if( !StringsUtil.isNullOrEmpty(part.getJidIm()) && part.getJidIm().equals(filterJid))
                    continue;

                parts.add(part);
            }
        }

        return parts;
    }

    public List<PgiConferenceParticipant> getTalkingUsers() {

        List<PgiConferenceParticipant> parts = new ArrayList<>();
        for(PgiConferenceParticipant part : m_participants) {
            if( part.isTalking() ) {
                parts.add(part);
            }
        }

        return parts;
    }

    public boolean areAllUsersMuted() {

        for(PgiConferenceParticipant part : m_participants) {
            if( !part.isMuted() )
                return false;
        }

        return true;
    }

    public boolean isRecording() {
        if( confState == null)
            return false;

        return confState.isRecordingStarted();
    }

    public boolean isConfActive() {
        if( confState == null)
            return false;

        return confState.isConfActive();
    }

    public boolean isUserConnected(String imJabberId) {
        PgiConferenceParticipant user = getPgiConferenceParticipantByJid(imJabberId);
        if( user == null)
            return false;

        return( user.isConnected() );
    }

    public interface PgiConferenceListener
    {
        /**
         * called when Conference has been updated
         */
        void conferenceUpdated(PgiConference updatedConf);
    }

}

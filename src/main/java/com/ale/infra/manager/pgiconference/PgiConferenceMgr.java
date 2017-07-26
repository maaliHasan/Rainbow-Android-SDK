package com.ale.infra.manager.pgiconference;

import android.content.Context;
import android.content.Intent;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.application.RainbowIntent;
import com.ale.infra.contact.Contact;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.manager.room.RoomConfEndPoint;
import com.ale.infra.manager.room.RoomMgr;
import com.ale.infra.xmpp.xep.pgiconference.ConferenceParticipant;
import com.ale.infra.xmpp.xep.pgiconference.ConferenceState;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by georges on 28/04/2017.
 */

public class PgiConferenceMgr implements IPgiConferenceMgr {
    private static final String LOG_TAG = "PgiConferenceMgr";

    private IPgiConferenceProxy m_pgiConferenceProxy;

    private List<PgiConference> m_myConferences = new ArrayList<>();
    private List<PgiConference> m_conferenceCache = new ArrayList<>();
    private Context m_applicationContext;


    public PgiConferenceMgr(IPgiConferenceProxy pgiConferenceProxy) {
        m_pgiConferenceProxy = pgiConferenceProxy;
    }

    @Override
    public void retrieveConferences() {
        Log.getLogger().verbose(LOG_TAG, ">retrieveConferences");

        if (RainbowContext.getInfrastructure().getCapabilities()== null)
            return;

        if (RainbowContext.getInfrastructure().getCapabilities().isConferenceAllowed()) {

            m_myConferences.clear();
            m_pgiConferenceProxy.getAllConferences(new IPgiConferenceProxy.IGetAllConferencesListener() {
                @Override
                public void onGetAllConfSuccess(List<PgiConference> conferences) {
                    Log.getLogger().verbose(LOG_TAG, ">onGetAllConfSuccess");
                    m_myConferences.addAll(conferences);
                    m_conferenceCache.addAll(conferences);
                    RainbowContext.getInfrastructure().getRoomMgr().refreshRoomConferenceInfo();
                }

                @Override
                public void onGetAllConfFailed() {
                    Log.getLogger().warn(LOG_TAG, ">onGetAllConfFailed");
                }
            });
        }
    }

    @Override
    public void retrieveConference(String confEndPointId, final IPgiConferenceProxy.IGetConferenceListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">retrieveConference");

        m_myConferences.clear();

        if (RainbowContext.getInfrastructure().getCapabilities()== null)
            return;

        if (RainbowContext.getInfrastructure().getCapabilities().isConferenceAllowed()) {
            m_pgiConferenceProxy.retrieveConference(confEndPointId, new IPgiConferenceProxy.IGetConferenceListener() {
                @Override
                public void onGetConferenceSuccess(PgiConference conference) {
                    Log.getLogger().verbose(LOG_TAG, ">onGetConferenceSuccess");
                    if (listener != null)
                        listener.onGetConferenceSuccess(conference);
                }

                @Override
                public void onGetConferenceFailed() {
                    Log.getLogger().warn(LOG_TAG, ">onGetConferenceFailed");
                    if (listener != null)
                        listener.onGetConferenceFailed();
                }
            });
        } else {
            if (listener != null)
                listener.onGetConferenceSuccess(null);
        }
    }

    @Override
    public List<PgiConference> getConferencesCache() {
        return m_conferenceCache;
    }

    @Override
    public List<PgiConference> getMyConferences() {
        return m_myConferences;
    }

    @Override
    public PgiConference getConferenceFromEndPoint(RoomConfEndPoint confEndpoint) {
        Log.getLogger().verbose(LOG_TAG, ">getConferenceFromEndPoint");
        if( confEndpoint == null)
            return null;


        for(PgiConference pgiConf : m_myConferences) {
            if( pgiConf.getId().equals(confEndpoint.getConfEndpointId())) {
                Log.getLogger().verbose(LOG_TAG, " conf EndPointId found");
                return pgiConf;
            }
        }
        final PgiConference pgiConf = new PgiConference(confEndpoint);
        m_conferenceCache.add(pgiConf);
        m_pgiConferenceProxy.getConferenceSnapshot(confEndpoint.getConfEndpointId(), new IPgiConferenceProxy.IGetConferenceSnapshotListener() {
            @Override
            public void onGetConferenceSnapshotSuccess(PgiConference conferenceSnapshot) {
                pgiConf.setParticipants(conferenceSnapshot.getAllParticipants());

                refreshConfState (pgiConf);
            }

            @Override
            public void onGetConferenceSnapshotFailed(RainbowServiceException exception) {
                if (exception.getDetailsCode() != 403300) {
                    // Conference is not found
                    RainbowContext.getInfrastructure().getRoomMgr().dissociatePgiConference(pgiConf.getId());
                }
            }
        });

        return pgiConf;
    }

    @Override
    public PgiConference getConferenceFromId(String confId) {
        Log.getLogger().verbose(LOG_TAG, ">getConferenceFromId: " + confId);


        for(PgiConference pgiConf : m_myConferences) {
            if( pgiConf.getId().equals(confId)) {
                Log.getLogger().verbose(LOG_TAG, " conf EndPointId found");
                return pgiConf;
            }
        }

        return null;
    }

    public boolean isAnotherConferenceActive(String confId) {
        for(PgiConference pgiConf : m_myConferences) {
            if( !pgiConf.getId().equals(confId)) {
                if (pgiConf.isConfActive()) {
                    Log.getLogger().verbose(LOG_TAG, " Theris another active conference");
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public void initiateConfAndCall(final String confId, final String phoneNumber, final IPgiConferenceProxy.IJoinAudioCallListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">startAudioConference");
        if(m_pgiConferenceProxy == null) {
            Log.getLogger().warn(LOG_TAG, ">startAudioConference : no PgiConfProxy");
            if (listener != null)
                listener.onJoinAudioCallFailed();
            return;
        }

        m_pgiConferenceProxy.getConferenceSnapshot(confId, new IPgiConferenceProxy.IGetConferenceSnapshotListener() {
            @Override
            public void onGetConferenceSnapshotSuccess(PgiConference conferenceSnapshot) {
                initConfAndCall(confId, phoneNumber, false,  listener);
            }

            @Override
            public void onGetConferenceSnapshotFailed(RainbowServiceException exception) {
                if( exception.getStatusCode() == 404) {
                    Log.getLogger().verbose(LOG_TAG, "Conference is not started = 404");
                    initConfAndCall(confId, phoneNumber, true, listener);
                } else if( exception.getDetailsCode() == 403300 ) {
                    Log.getLogger().verbose(LOG_TAG, "Conference is started but we are not inside = 403");
                    initConfAndCall(confId, phoneNumber,false,  listener);
                } else {
                    if (listener != null)
                        listener.onJoinAudioCallFailed();
                }
            }
        });
    }

    private void initConfAndCall(final String confId, final String phoneNumber, boolean needStart , final IPgiConferenceProxy.IJoinAudioCallListener listener) {

        if( isOurConf(confId) && needStart) {
            m_pgiConferenceProxy.startAudioConference(confId, new IPgiConferenceProxy.IStartAudioConfListener() {
                @Override
                public void onStartAudioConfSuccess() {
                    Log.getLogger().verbose(LOG_TAG, ">onStartAudioConfSuccess");
                    joinCallConference(confId, phoneNumber, listener);
                }

                @Override
                public void onStartAudioConfFailed() {
                    Log.getLogger().warn(LOG_TAG, ">onStartAudioConfFailed");
                    //m_pgiConferenceProxy.stopAudioConference(confId, null);
                    if (listener != null)
                        listener.onJoinAudioCallFailed();
                }
            });
        } else {
            joinCallConference(confId, phoneNumber, listener);
        }
    }

    private boolean isOurConf(String confid) {
    Contact user =  RainbowContext.getInfrastructure().getContactCacheMgr().getUser();
        for(PgiConference pgiConf : m_conferenceCache) {
            if( pgiConf.getId().equals(confid) &&  pgiConf.getUserId().equals(user.getCorporateId())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void stopAudioConference(final PgiConference conf, final IPgiConferenceProxy.IStopAudioConfListener listener) {
        if (conf == null || StringsUtil.isNullOrEmpty(conf.getId())) {
            Log.getLogger().warn(LOG_TAG, "confId is NULL");
            if (listener != null)
                listener.onStopAudioConfFailed();
            return;
        }
        Log.getLogger().verbose(LOG_TAG, ">stopAudioConference");

        if (conf.isMyConference()) {
            //unassociate
            RainbowContext.getInfrastructure().getRoomMgr().dissociatePgiConference(conf.getId());


            m_pgiConferenceProxy.stopAudioConference(conf.getId(), new IPgiConferenceProxy.IStopAudioConfListener() {
                @Override
                public void onStopAudioConfSuccess() {
                    Log.getLogger().verbose(LOG_TAG, ">stopAudioConference SUCCESS");
                    conf.getAllParticipants().clear();
                    conf.setConfState(null);
                    conf.notifyConferenceUpdated();

                    if (listener != null)
                        listener.onStopAudioConfSuccess();
                }

                @Override
                public void onStopAudioConfFailed() {
                    Log.getLogger().warn(LOG_TAG, ">stopAudioConference FAILURE");
                    conf.getAllParticipants().clear();
                    conf.setConfState(null);
                    conf.notifyConferenceUpdated();
                    if (listener != null)
                        listener.onStopAudioConfFailed();
                }
            });

        }
    }

    @Override
    public void toggleMuteStateParticipant(final PgiConference conf, final PgiConferenceParticipant participant, final IPgiConferenceProxy.IToggleMuteStateParticipantListener listener) {
        if( conf == null || StringsUtil.isNullOrEmpty(conf.getId())) {
            Log.getLogger().warn(LOG_TAG, "confId is NULL");
            if (listener != null)
                listener.onToggleMuteStateParticipantFailed();
            return;
        }
        if( participant == null) {
            Log.getLogger().warn(LOG_TAG, "participant is NULL");
            if (listener != null)
                listener.onToggleMuteStateParticipantFailed();
            return;
        }

        Log.getLogger().verbose(LOG_TAG, ">toggleMuteStateParticipant");

        final boolean muteState = participant.isMuted();

        m_pgiConferenceProxy.muteParticipant(conf.getId(), participant.getUserId(), !muteState, new IPgiConferenceProxy.IToggleMuteStateParticipantListener() {
            @Override
            public void onToggleMuteStateParticipantSuccess() {
                Log.getLogger().verbose(LOG_TAG, ">MuteParticipant SUCCESS");

                participant.setMuted(!muteState);
                conf.notifyConferenceUpdated();
                if (listener != null)
                    listener.onToggleMuteStateParticipantSuccess();
            }

            @Override
            public void onToggleMuteStateParticipantFailed() {
                Log.getLogger().warn(LOG_TAG, ">MuteParticipant FAILURE");
                if (listener != null)
                    listener.onToggleMuteStateParticipantFailed();
            }
        });
    }

    @Override
    public void muteAllParticipants(final PgiConference conf, final boolean muteState, final IPgiConferenceProxy.IToggleMuteStateParticipantListener listener) {
        if( conf == null || StringsUtil.isNullOrEmpty(conf.getId())) {
            Log.getLogger().warn(LOG_TAG, "confId is NULL");
            if (listener != null)
                listener.onToggleMuteStateParticipantFailed();
            return;
        }

        for(final PgiConferenceParticipant participant : conf.getAllParticipants()) {
            participant.setMuted(muteState);
        }
        conf.notifyConferenceUpdated();

        Log.getLogger().verbose(LOG_TAG, ">muteAllParticipants");

        m_pgiConferenceProxy.muteAllParticipant(conf.getId(), muteState, new IPgiConferenceProxy.IToggleMuteStateParticipantListener() {
            @Override
            public void onToggleMuteStateParticipantSuccess() {
                Log.getLogger().verbose(LOG_TAG, ">MuteAllParticipant SUCCESS");

                if (listener != null) {
                    listener.onToggleMuteStateParticipantSuccess();
                }

            }

            @Override
            public void onToggleMuteStateParticipantFailed() {
                Log.getLogger().warn(LOG_TAG, ">MuteParticipant FAILURE");
                if (listener != null) {
                    listener.onToggleMuteStateParticipantSuccess();
                }
            }
        });

//        final int[] nbPartMuted = {0};
//        for(final PgiConferenceParticipant participant : conf.getAllParticipants()) {
//
//            m_pgiConferenceProxy.muteParticipant(conf.getId(), participant.getUserId(), muteState, new IPgiConferenceProxy.IToggleMuteStateParticipantListener() {
//                @Override
//                public void onToggleMuteStateParticipantSuccess() {
//                    Log.getLogger().verbose(LOG_TAG, ">MuteParticipant SUCCESS");
//
//                    participant.setMuted(muteState);
//                    conf.notifyConferenceUpdated();
//
//                    nbPartMuted[0]++;
//                    if( nbPartMuted[0] >= conf.getAllParticipants().size()) {
//                        Log.getLogger().verbose(LOG_TAG, "All Participants are (un)muted now");
//                        if (listener != null)
//                            listener.onToggleMuteStateParticipantSuccess();
//                    }
//                }
//
//                @Override
//                public void onToggleMuteStateParticipantFailed() {
//                    Log.getLogger().warn(LOG_TAG, ">MuteParticipant FAILURE");
//                    if (listener != null)
//                        listener.onToggleMuteStateParticipantFailed();
//                }
//            });
//        }

    }

    @Override
    public void startRecording(PgiConference conf, final IPgiConferenceProxy.IStartRecordListener listener) {
        if( conf == null || StringsUtil.isNullOrEmpty(conf.getId())) {
            Log.getLogger().warn(LOG_TAG, "confId is NULL");
            if (listener != null)
                listener.onStartRecordFailed();
            return;
        }

        Log.getLogger().verbose(LOG_TAG, ">startRecording");

        m_pgiConferenceProxy.startRecording(conf.getId(), new IPgiConferenceProxy.IStartRecordListener() {
            @Override
            public void onStartRecordSuccess() {
                Log.getLogger().verbose(LOG_TAG, ">onStartRecordSuccess");
                if (listener != null)
                    listener.onStartRecordSuccess();
            }

            @Override
            public void onStartRecordFailed() {
                Log.getLogger().warn(LOG_TAG, ">onStartRecordFailed");
                if (listener != null)
                    listener.onStartRecordFailed();
            }
        });
    }

    @Override
    public void stopRecording(PgiConference conf, final IPgiConferenceProxy.IStopRecordListener listener) {
        if( conf == null || StringsUtil.isNullOrEmpty(conf.getId())) {
            Log.getLogger().warn(LOG_TAG, "confId is NULL");
            if (listener != null)
                listener.onStopRecordFailed();
            return;
        }

        Log.getLogger().verbose(LOG_TAG, ">stopRecording");

        m_pgiConferenceProxy.stopRecording(conf.getId(), new IPgiConferenceProxy.IStopRecordListener() {
            @Override
            public void onStopRecordSuccess() {
                Log.getLogger().verbose(LOG_TAG, ">onStopRecordSuccess");
                if (listener != null)
                    listener.onStopRecordSuccess();
            }

            @Override
            public void onStopRecordFailed() {
                Log.getLogger().warn(LOG_TAG, ">onStopRecordFailed");
                if (listener != null)
                    listener.onStopRecordFailed();
            }
        });
    }

    @Override
    public void hangUpParticipant(final PgiConference conf, PgiConferenceParticipant participant, final IPgiConferenceProxy.IHangUpParticipantListener listener) {
        if( conf == null || StringsUtil.isNullOrEmpty(conf.getId())) {
            Log.getLogger().warn(LOG_TAG, "confId is NULL");
            if (listener != null)
                listener.onHangUpParticipantFailed();
            return;
        }
        if( participant == null) {
            Log.getLogger().warn(LOG_TAG, "participant is NULL");
            if (listener != null)
                listener.onHangUpParticipantFailed();
            return;
        }

        Log.getLogger().verbose(LOG_TAG, ">HangUpParticipant");

        m_pgiConferenceProxy.hangUpParticipant(conf.getId(), participant.getUserId(), new IPgiConferenceProxy.IHangUpParticipantListener() {
            @Override
            public void onHangUpParticipantSuccess() {
                Log.getLogger().verbose(LOG_TAG, ">HangUpParticipant SUCCESS");

                conf.getAllParticipants().clear();
                conf.setConfState(null);
                conf.notifyConferenceUpdated();
                if (listener != null)
                    listener.onHangUpParticipantSuccess();
            }

            @Override
            public void onHangUpParticipantFailed() {
                Log.getLogger().warn(LOG_TAG, ">HangUpParticipant FAILURE");
                if (listener != null)
                    listener.onHangUpParticipantFailed();
            }
        });
    }

    @Override
    public void clearConferences() {
        m_myConferences.clear();
    }

    private void joinCallConference(final String confId, String phoneNumber, final IPgiConferenceProxy.IJoinAudioCallListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">joinCallConference");
        if(m_pgiConferenceProxy == null) {
            Log.getLogger().warn(LOG_TAG, ">joinCallConference : no PgiConfProxy");
            if (listener != null)
                listener.onJoinAudioCallFailed();
            return;
        }
        if(StringsUtil.isNullOrEmpty(phoneNumber)) {
            Log.getLogger().warn(LOG_TAG, ">joinCallConference : no PhoneNumber given");
            return;
        }

        m_pgiConferenceProxy.joinCallConference(confId, phoneNumber, new IPgiConferenceProxy.IJoinAudioCallListener() {
            @Override
            public void onJoinAudioCallSuccess() {
                Log.getLogger().verbose(LOG_TAG, ">onJoinAudioCallSuccess");

                Intent intent = new Intent(RainbowIntent.ACTION_RAINBOW_PGI_JOIN_SUCCESS);
                m_applicationContext.sendBroadcast(intent);

                if (listener != null)
                    listener.onJoinAudioCallSuccess();
            }

            @Override
            public void onJoinAudioCallFailed() {
                Log.getLogger().warn(LOG_TAG, ">onJoinAudioCallFailed");
                m_pgiConferenceProxy.stopAudioConference(confId, null);
                if (listener != null)
                    listener.onJoinAudioCallFailed();
            }
        });
    }

    public void setContext(Context context) {
        m_applicationContext = context.getApplicationContext();
    }

    private void refreshConfState (PgiConference pgiConference) {
        if (pgiConference.getConfState() != null)
            return;
        else {
            ConferenceState confState = new ConferenceState();

            for (PgiConferenceParticipant participant : pgiConference.getAllParticipants()) {
                if ( participant.isConnected()  || participant.isHold() || participant.isMuted()) {
                    confState.setConfActive(true);
                    confState.setParticipantCount(pgiConference.getAllParticipants().size());
                    pgiConference.setConfState(confState);
                    return;
                }
            }
        }
    }



}

package com.ale.infra.manager.pgiconference;

import com.ale.infra.manager.room.RoomConfEndPoint;

import java.util.List;

/**
 * Created by georges on 28/04/2017.
 */

public interface IPgiConferenceMgr {
    void retrieveConferences();

    void retrieveConference(String confEndPointId, IPgiConferenceProxy.IGetConferenceListener listener);

    List<PgiConference> getConferencesCache();

    List<PgiConference> getMyConferences();

    PgiConference getConferenceFromEndPoint(RoomConfEndPoint confEndpointId);

    PgiConference getConferenceFromId(String confId);

    void initiateConfAndCall(String id, String phoneNumber, IPgiConferenceProxy.IJoinAudioCallListener listener);

    void stopAudioConference(PgiConference confId, IPgiConferenceProxy.IStopAudioConfListener listener);

    void toggleMuteStateParticipant(PgiConference conf, PgiConferenceParticipant participant, IPgiConferenceProxy.IToggleMuteStateParticipantListener listener);

    void hangUpParticipant(PgiConference conf, PgiConferenceParticipant participant, IPgiConferenceProxy.IHangUpParticipantListener listener);

    void clearConferences();

    void muteAllParticipants(PgiConference conf, boolean mute, IPgiConferenceProxy.IToggleMuteStateParticipantListener listener);

    void startRecording(PgiConference conf, IPgiConferenceProxy.IStartRecordListener listener);

    void stopRecording(PgiConference conf, IPgiConferenceProxy.IStopRecordListener listener);
}

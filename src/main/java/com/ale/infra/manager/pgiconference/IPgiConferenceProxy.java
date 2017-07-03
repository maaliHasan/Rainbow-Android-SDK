package com.ale.infra.manager.pgiconference;

import com.ale.infra.http.adapter.concurrent.RainbowServiceException;

import java.util.List;

/**
 * Created by georges on 26/04/2017.
 */

public interface IPgiConferenceProxy {

    void getAllConferences(IGetAllConferencesListener listener);

    void retrieveConference(String confEndPointId, IGetConferenceListener listener);

    void createConference(String ownerId, String confName, ICreateConferenceListener listener);

    void startAudioConference(String confEndpointId, IStartAudioConfListener listener);

    void stopAudioConference(String confId, IStopAudioConfListener listener);

    void getConferenceSnapshot(String confId, IGetConferenceSnapshotListener listener);

    void joinCallConference(String confId, String phoneNumber, IJoinAudioCallListener listener);

    void muteParticipant(String confId, String userId, boolean muteState, IToggleMuteStateParticipantListener listener);

    void hangUpParticipant(String confId, String userId, IHangUpParticipantListener listener);

    void startRecording(String confId, IStartRecordListener listener);

    void stopRecording(String confId, IStopRecordListener listener);


    interface IGetAllConferencesListener
    {
        void onGetAllConfSuccess(List<PgiConference> conferences);

        void onGetAllConfFailed();
    }

    interface IGetConferenceListener
    {
        void onGetConferenceSuccess(PgiConference conference);

        void onGetConferenceFailed();
    }

    interface ICreateConferenceListener
    {
        void onCreateSuccess(PgiConference conference);

        void onCreateFailed();
    }

    interface IStartAudioConfListener {
        void onStartAudioConfSuccess();
        void onStartAudioConfFailed();
    }

    interface IStopAudioConfListener {
        void onStopAudioConfSuccess();
        void onStopAudioConfFailed();
    }

    interface IGetConferenceSnapshotListener {
        void onGetConferenceSnapshotSuccess(PgiConference conferenceSnapshot);
        void onGetConferenceSnapshotFailed(RainbowServiceException exception);
    }

    interface IJoinAudioCallListener {
        void onJoinAudioCallSuccess();
        void onJoinAudioCallFailed();
    }

     interface IToggleMuteStateParticipantListener {
        void onToggleMuteStateParticipantSuccess();
        void onToggleMuteStateParticipantFailed();
    }

    interface IHangUpParticipantListener {
        void onHangUpParticipantSuccess();
        void onHangUpParticipantFailed();
    }

    interface IStartRecordListener {
        void onStartRecordSuccess();
        void onStartRecordFailed();
    }

    interface IStopRecordListener {
        void onStopRecordSuccess();
        void onStopRecordFailed();
    }
}

package com.ale.infra.rainbow.api;

import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.proxy.pgiconference.PgiConferenceResponse;
import com.ale.infra.proxy.pgiconference.PgiGetAllConferencesResponse;
import com.ale.infra.proxy.pgiconference.PgiGetConferenceSnapshotResponse;

/**
 * Created by georges on 26/04/2017.
 */

public interface IPgiConferenceService extends IRainbowService  {

    void createConference(String ownerId,String confName,IAsyncServiceResultCallback<PgiConferenceResponse> callback);

    void getAllConferences(IAsyncServiceResultCallback<PgiGetAllConferencesResponse> callback);

    void retrieveConference(String confEndPointId, IAsyncServiceResultCallback<PgiConferenceResponse> callback);

    void startAudioConference(String confEndpointId, IAsyncServiceVoidCallback callback);

    void stopAudioConference(String confId, IAsyncServiceVoidCallback callback);

    void initiateCall(String confId, String phoneNumber, IAsyncServiceVoidCallback callback);

    void getConferenceSnapshot(String confId, IAsyncServiceResultCallback<PgiGetConferenceSnapshotResponse> callback);

    void muteParticipant(String confId, String participantId, boolean muteState, IAsyncServiceVoidCallback callback);

    void hangUpParticipant(String confId, String participantId, IAsyncServiceVoidCallback callback);

    void startRecording(String confId, IAsyncServiceVoidCallback callback);

    void stopRecording(String confId, IAsyncServiceVoidCallback callback);
}

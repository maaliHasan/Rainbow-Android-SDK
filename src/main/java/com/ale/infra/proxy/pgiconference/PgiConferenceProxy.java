package com.ale.infra.proxy.pgiconference;


import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseVoid;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.manager.pgiconference.IPgiConferenceProxy;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.rainbow.api.IPgiConferenceService;
import com.ale.infra.rainbow.api.IServicesFactory;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

/**
 * Created by georges on 10/02/2017.
 */

public class PgiConferenceProxy implements IPgiConferenceProxy {

    private static final String LOG_TAG = "PgiConferenceProxy";


    private IPgiConferenceService m_pgiConferenceService;

    public PgiConferenceProxy(IServicesFactory servicesFactory, IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService)
    {
        Log.getLogger().info(LOG_TAG, "initialization");
        m_pgiConferenceService = servicesFactory.createPgiConferenceService(restAsyncRequest, platformService);
    }

    @Override
    public void getAllConferences(final IGetAllConferencesListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">retrieveConferences");

        m_pgiConferenceService.getAllConferences(new IAsyncServiceResultCallback<PgiGetAllConferencesResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<PgiGetAllConferencesResponse> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "retrieveConferences SUCCESS");

                    if (listener != null) {
                        PgiGetAllConferencesResponse result = asyncResult.getResult();
                        listener.onGetAllConfSuccess(result.getConferenceList());
                    }
                } else {
                    Log.getLogger().info(LOG_TAG, "retrieveConferences FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onGetAllConfFailed();
                }
            }
        });
    }

    @Override
    public void retrieveConference(String confEndPointId, final IGetConferenceListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">retrieveConference");

        m_pgiConferenceService.retrieveConference(confEndPointId, new IAsyncServiceResultCallback<PgiConferenceResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<PgiConferenceResponse> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "retrieveConference SUCCESS");

                    if (listener != null) {
                        PgiConferenceResponse result = asyncResult.getResult();
                        listener.onGetConferenceSuccess(result.getConference());
                    }
                } else {
                    Log.getLogger().info(LOG_TAG, "retrieveConference FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onGetConferenceFailed();
                }
            }
        });
    }

    @Override
    public void createConference(String ownerId,String confName,final ICreateConferenceListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">createConference");

        m_pgiConferenceService.createConference(ownerId,confName,new IAsyncServiceResultCallback<PgiConferenceResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<PgiConferenceResponse> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "createConference SUCCESS");

                    if (listener != null) {
                        PgiConferenceResponse result = asyncResult.getResult();
                        listener.onCreateSuccess(result.getConference());
                    }
                } else {
                    Log.getLogger().info(LOG_TAG, "createConference FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onCreateFailed();
                }
            }
        });
    }

    @Override
    public void startAudioConference(String confId, final IStartAudioConfListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">startAudioConference");

        m_pgiConferenceService.startAudioConference(confId,new IAsyncServiceVoidCallback() {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "startAudioConference SUCCESS");

                    if (listener != null)
                        listener.onStartAudioConfSuccess();
                } else {
                    Log.getLogger().info(LOG_TAG, "startAudioConference FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onStartAudioConfFailed();
                }
            }
        });
    }

    @Override
    public void stopAudioConference(String confId, final IStopAudioConfListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">stopAudioConference");

        m_pgiConferenceService.stopAudioConference(confId,new IAsyncServiceVoidCallback() {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "stopAudioConference SUCCESS");

                    if (listener != null)
                        listener.onStopAudioConfSuccess();
                } else {
                    Log.getLogger().info(LOG_TAG, "stopAudioConference FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onStopAudioConfFailed();
                }
            }
        });
    }

    @Override
    public void getConferenceSnapshot(String confId, final IGetConferenceSnapshotListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">getConferenceSnapshot");

        if(StringsUtil.isNullOrEmpty(confId)) {
            Log.getLogger().warn(LOG_TAG, "getConferenceSnapshot FAILURE: no ConfId given");
            if (listener != null)
                listener.onGetConferenceSnapshotFailed(new RainbowServiceException("no ConfId given"));
            return;
        }

        m_pgiConferenceService.getConferenceSnapshot(confId,new IAsyncServiceResultCallback<PgiGetConferenceSnapshotResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<PgiGetConferenceSnapshotResponse> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "getConferenceSnapshot SUCCESS");

                    if (listener != null) {
                        PgiGetConferenceSnapshotResponse result = asyncResult.getResult();
                        listener.onGetConferenceSnapshotSuccess(result.getConferenceSnapshot());
                    }
                } else {
                    Log.getLogger().warn(LOG_TAG, "getConferenceSnapshot FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onGetConferenceSnapshotFailed(asyncResult.getException());
                }
            }
        });
    }


    @Override
    public void joinCallConference(String confId, String phoneNumber, final IJoinAudioCallListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">joinCallConference: "+phoneNumber);

        m_pgiConferenceService.initiateCall(confId, phoneNumber, new IAsyncServiceVoidCallback() {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "joinCallConference SUCCESS");

                    if (listener != null)
                        listener.onJoinAudioCallSuccess();
                } else {
                    Log.getLogger().info(LOG_TAG, "joinCallConference FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onJoinAudioCallFailed();
                }
            }
        });
    }

    @Override
    public void muteParticipant(String confId, String participantId, boolean muteState, final IToggleMuteStateParticipantListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">toggleMuteStateParticipant: "+muteState);

        m_pgiConferenceService.muteParticipant(confId, participantId, muteState, new IAsyncServiceVoidCallback() {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "toggleMuteStateParticipant SUCCESS");

                    if (listener != null)
                        listener.onToggleMuteStateParticipantSuccess();
                } else {
                    Log.getLogger().info(LOG_TAG, "toggleMuteStateParticipant FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onToggleMuteStateParticipantFailed();
                }
            }
        });
    }

    @Override
    public void hangUpParticipant(String confId, String participantId, final IHangUpParticipantListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">HangUpParticipant");

        m_pgiConferenceService.hangUpParticipant(confId, participantId, new IAsyncServiceVoidCallback() {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "HangUpParticipant SUCCESS");

                    if (listener != null)
                        listener.onHangUpParticipantSuccess();
                } else {
                    Log.getLogger().info(LOG_TAG, "HangUpParticipant FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onHangUpParticipantFailed();
                }
            }
        });
    }

    @Override
    public void startRecording(String confId, final IStartRecordListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">startRecording");

        m_pgiConferenceService.startRecording(confId, new IAsyncServiceVoidCallback() {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "startRecording SUCCESS");

                    if (listener != null)
                        listener.onStartRecordSuccess();
                } else {
                    Log.getLogger().info(LOG_TAG, "startRecording FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onStartRecordFailed();
                }
            }
        });
    }

    @Override
    public void stopRecording(String confId, final IStopRecordListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">stopRecording");

        m_pgiConferenceService.stopRecording(confId, new IAsyncServiceVoidCallback() {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "stopRecording SUCCESS");

                    if (listener != null)
                        listener.onStopRecordSuccess();
                } else {
                    Log.getLogger().info(LOG_TAG, "stopRecording FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onStopRecordFailed();
                }
            }
        });
    }
}

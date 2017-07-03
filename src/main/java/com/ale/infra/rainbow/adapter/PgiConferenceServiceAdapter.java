package com.ale.infra.rainbow.adapter;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.RESTResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseVoid;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.framework.RainbowServiceTag;
import com.ale.infra.proxy.pgiconference.PgiConferenceResponse;
import com.ale.infra.proxy.pgiconference.PgiGetAllConferencesResponse;
import com.ale.infra.proxy.pgiconference.PgiGetConferenceSnapshotResponse;
import com.ale.infra.rainbow.api.ApisConstants;
import com.ale.infra.rainbow.api.IPgiConferenceService;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by georges on 26/04/2017.
 */

public class PgiConferenceServiceAdapter implements IPgiConferenceService
{
    private static final String LOG_TAG = "PgiConferenceServiceAdapter";

    private final IRESTAsyncRequest m_restAsyncRequest;
    private IPlatformServices m_platformServices;

    public PgiConferenceServiceAdapter(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices)
    {
        m_restAsyncRequest = restAsyncRequest;
        m_platformServices = platformServices;
    }

    private String getUrl()
    {
        String url = RainbowContext.getPlatformServices().getApplicationData().getServerUrl();
        if (url == null)
        {
            url = StringsUtil.EMPTY;
        }
        return url;
    }

    @Override
    public RainbowServiceTag getTag()
    {
        return RainbowServiceTag.PGI;
    }

    @Override
    public void createConference(String ownerId,String confName,final IAsyncServiceResultCallback<PgiConferenceResponse> callback) {

        Log.getLogger().verbose(LOG_TAG, ">createConference");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.PGI_CONFPROVISIONNING);

        JSONObject restBody = new JSONObject();
        try
        {
            restBody.put("confUserId", ownerId);
            if( !StringsUtil.isNullOrEmpty(confName)) {
                JSONObject reservationObject= new JSONObject();
                JSONObject confNameObject= new JSONObject();
                confNameObject.put("ConferenceName", confName);
                reservationObject.put("Reservation", confNameObject);

                restBody.put("providerSpecific", reservationObject);
            }
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "createConference FAILURE");
                    Log.getLogger().verbose(LOG_TAG, ">createConference : " + asyncResult.getException().getDetailsMessage());
                    notifyCreateConferenceResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "createConference SUCCESS");
                    try {
                        notifyCreateConferenceResult(callback, null, new PgiConferenceResponse(asyncResult.getResult().getResponse()));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST PgiConferenceResponse result");
                        notifyCreateConferenceResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    private void notifyCreateConferenceResult(IAsyncServiceResultCallback<PgiConferenceResponse> callback, RainbowServiceException alcServiceException, PgiConferenceResponse response)
    {
        AsyncServiceResponseResult<PgiConferenceResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    @Override
    public void getAllConferences(final IAsyncServiceResultCallback<PgiGetAllConferencesResponse> callback) {

        Log.getLogger().verbose(LOG_TAG, ">retrieveConferences");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.PGI_CONFPROVISIONNING);

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "retrieveConferences FAILURE");
                    Log.getLogger().verbose(LOG_TAG, ">retrieveConferences : " + asyncResult.getException().getDetailsMessage());
                    notifyGetAllConferencesResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "retrieveConferences SUCCESS");
                    try {
                        notifyGetAllConferencesResult(callback, null, new PgiGetAllConferencesResponse(asyncResult.getResult().getResponse()));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST PgiGetAllConferencesResponse result");
                        notifyGetAllConferencesResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void retrieveConference(String confEndPointId, final IAsyncServiceResultCallback<PgiConferenceResponse> callback) {

        Log.getLogger().verbose(LOG_TAG, ">retrieveConference");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.PGI_CONFPROVISIONNING);
        try {
            restUrl.append("/");
            restUrl.append(URLEncoder.encode(confEndPointId, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to retrieveConference");
        }

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "retrieveConference FAILURE");
                    Log.getLogger().verbose(LOG_TAG, ">retrieveConference : " + asyncResult.getException().getDetailsMessage());
                    notifyGetConferenceResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "retrieveConference SUCCESS");
                    try {
                        notifyGetConferenceResult(callback, null, new PgiConferenceResponse(asyncResult.getResult().getResponse()));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST retrieveConference result");
                        notifyGetConferenceResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void startAudioConference(String confId, final IAsyncServiceVoidCallback callback) {
        Log.getLogger().verbose(LOG_TAG, ">startAudioConference");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.PGI_CONFERENCES);
        try {
            restUrl.append(URLEncoder.encode(confId, "UTF-8"));
            restUrl.append("/");
            restUrl.append(ApisConstants.START_PGICONFERENCE);
        } catch (UnsupportedEncodingException e) {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to startAudioConference");
        }

        m_restAsyncRequest.sendPutRequest(restUrl.toString(), null, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "startAudioConference FAILURE");
                    Log.getLogger().verbose(LOG_TAG, ">startAudioConference : " + asyncResult.getException().getDetailsMessage());
                    notifyVoidResult(callback, asyncResult.getException());
                } else {
                    Log.getLogger().info(LOG_TAG, "startAudioConference SUCCESS");
                    notifyVoidResult(callback, null);
                }
            }
        });
    }

    @Override
    public void stopAudioConference(String confId, final IAsyncServiceVoidCallback callback) {
        Log.getLogger().verbose(LOG_TAG, ">stopAudioConference");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.PGI_CONFERENCES);
        try {
            restUrl.append(URLEncoder.encode(confId, "UTF-8"));
            restUrl.append("/");
            restUrl.append(ApisConstants.STOP_PGICONFERENCE);
        } catch (UnsupportedEncodingException e) {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to stopAudioConference");
        }

        m_restAsyncRequest.sendPutRequest(restUrl.toString(), null, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "stopAudioConference FAILURE");
                    Log.getLogger().verbose(LOG_TAG, ">stopAudioConference : " + asyncResult.getException().getDetailsMessage());
                    notifyVoidResult(callback, asyncResult.getException());
                } else {
                    Log.getLogger().info(LOG_TAG, "stopAudioConference SUCCESS");
                    notifyVoidResult(callback, null);
                }
            }
        });
    }

    @Override
    public void initiateCall(String confId, String phoneNumber, final IAsyncServiceVoidCallback callback) {
        Log.getLogger().verbose(LOG_TAG, ">startAudioConference");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.PGI_CONFERENCES);
        try {
            restUrl.append(URLEncoder.encode(confId, "UTF-8"));
            restUrl.append("/");
            restUrl.append(ApisConstants.JOIN_PGICONFERENCE);
        } catch (UnsupportedEncodingException e) {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to startAudioConference");
        }

        JSONObject restBody = new JSONObject();
        try
        {
            restBody.put("participantPhoneNumber", phoneNumber);

            JSONObject participantObject= new JSONObject();
            participantObject.put("role", "moderator");
            participantObject.put("type", "unmuted");
            restBody.put("participant", participantObject);

            restBody.put("country", "FRA");
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "startAudioConference FAILURE");
                    notifyVoidResult(callback, asyncResult.getException());
                } else {
                    Log.getLogger().info(LOG_TAG, "startAudioConference SUCCESS");
                    notifyVoidResult(callback, null);
                }
            }
        });
    }

    @Override
    public void getConferenceSnapshot(String confId, final IAsyncServiceResultCallback<PgiGetConferenceSnapshotResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">getConferenceSnapshot");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.PGI_CONFERENCES);
        try {
            restUrl.append(URLEncoder.encode(confId, "UTF-8"));
            restUrl.append("/");
            restUrl.append(ApisConstants.SNAPSHOT_PGICONFERENCE);
        } catch (UnsupportedEncodingException e) {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to getConferenceSnapshot");
        }

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "getConferenceSnapshot FAILURE");
                    notifyGetConferenceSnapshotResult(callback, asyncResult.getException(), null);
                } else {
                    Log.getLogger().info(LOG_TAG, "getConferenceSnapshot SUCCESS");
                    try {
                        notifyGetConferenceSnapshotResult(callback, null, new PgiGetConferenceSnapshotResponse(asyncResult.getResult().getResponse()));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST getConferenceSnapshot result");
                        notifyGetConferenceSnapshotResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void muteParticipant(String confId, String participantId, boolean muteState, final IAsyncServiceVoidCallback callback) {
        Log.getLogger().verbose(LOG_TAG, ">toggleMuteStateParticipant");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.PGI_CONFERENCES);
        try {
            restUrl.append(URLEncoder.encode(confId, "UTF-8"));
            restUrl.append("/");
            restUrl.append(ApisConstants.PGI_PARTICIPANTS);
            restUrl.append("/");
            restUrl.append(URLEncoder.encode(participantId, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to toggleMuteStateParticipant");
        }

        JSONObject restBody = new JSONObject();
        try
        {
            if( muteState )
                restBody.put("option", "mute");
            else
                restBody.put("option", "unmute");
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        m_restAsyncRequest.sendPutRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "toggleMuteStateParticipant FAILURE");
                    notifyVoidResult(callback, asyncResult.getException());
                } else {
                    Log.getLogger().info(LOG_TAG, "toggleMuteStateParticipant SUCCESS");
                    try {
                        notifyVoidResult(callback, null);
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST toggleMuteStateParticipant result");
                        notifyVoidResult(callback, new RainbowServiceException(error));
                    }
                }
            }
        });
    }

    @Override
    public void hangUpParticipant(String confId, String participantId, final IAsyncServiceVoidCallback callback) {
        Log.getLogger().verbose(LOG_TAG, ">hangUpParticipant");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.PGI_CONFERENCES);
        try {
            restUrl.append(URLEncoder.encode(confId, "UTF-8"));
            restUrl.append("/");
            restUrl.append(ApisConstants.PGI_PARTICIPANTS);
            restUrl.append("/");
            restUrl.append(URLEncoder.encode(participantId, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to toggleMuteStateParticipant");
        }

        m_restAsyncRequest.sendDeleteRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "hangUpParticipant FAILURE");
                    notifyVoidResult(callback, asyncResult.getException());
                } else {
                    Log.getLogger().info(LOG_TAG, "hangUpParticipant SUCCESS");
                    try {
                        notifyVoidResult(callback, null);
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST hangUpParticipant result");
                        notifyVoidResult(callback, new RainbowServiceException(error));
                    }
                }
            }
        });
    }

    // TODO manage participantId later when it will work
    @Override
    public void startRecording(String confId, final IAsyncServiceVoidCallback callback) {
        Log.getLogger().verbose(LOG_TAG, ">startRecording");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.PGI_CONFERENCES);
        try {
            restUrl.append(URLEncoder.encode(confId, "UTF-8"));
            restUrl.append("/");
            restUrl.append(ApisConstants.RESUME_RECORDING);
        } catch (UnsupportedEncodingException e) {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to startRecording");
        }

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), null, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "startRecording FAILURE");
                    notifyVoidResult(callback, asyncResult.getException());
                } else {
                    Log.getLogger().info(LOG_TAG, "startRecording SUCCESS");
                    try {
                        notifyVoidResult(callback, null);
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST startRecording result");
                        notifyVoidResult(callback, new RainbowServiceException(error));
                    }
                }
            }
        });

    }

    // TODO manage participantId later when it will work
    @Override
    public void stopRecording(String confId, final IAsyncServiceVoidCallback callback) {
        Log.getLogger().verbose(LOG_TAG, ">startRecording");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.PGI_CONFERENCES);
        try {
            restUrl.append(URLEncoder.encode(confId, "UTF-8"));
            restUrl.append("/");
            restUrl.append(ApisConstants.RESUME_RECORDING);
        } catch (UnsupportedEncodingException e) {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to startRecording");
        }

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), null, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "startRecording FAILURE");
                    notifyVoidResult(callback, asyncResult.getException());
                } else {
                    Log.getLogger().info(LOG_TAG, "startRecording SUCCESS");
                    try {
                        notifyVoidResult(callback, null);
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST startRecording result");
                        notifyVoidResult(callback, new RainbowServiceException(error));
                    }
                }
            }
        });

    }

    private void notifyGetConferenceSnapshotResult(IAsyncServiceResultCallback<PgiGetConferenceSnapshotResponse> callback, RainbowServiceException alcServiceException, PgiGetConferenceSnapshotResponse response)
    {
        AsyncServiceResponseResult<PgiGetConferenceSnapshotResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyGetConferenceResult(IAsyncServiceResultCallback<PgiConferenceResponse> callback, RainbowServiceException alcServiceException, PgiConferenceResponse response) {
        AsyncServiceResponseResult<PgiConferenceResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyGetAllConferencesResult(IAsyncServiceResultCallback<PgiGetAllConferencesResponse> callback, RainbowServiceException alcServiceException, PgiGetAllConferencesResponse response)
    {
        AsyncServiceResponseResult<PgiGetAllConferencesResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyVoidResult(IAsyncServiceVoidCallback callback, RainbowServiceException alcServiceException)
    {
        AsyncServiceResponseVoid asyncResult = new AsyncServiceResponseVoid(alcServiceException);
        callback.handleResult(asyncResult);
    }

}

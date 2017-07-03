package com.ale.infra.rainbow.adapter;

import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.RESTResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseVoid;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.framework.RainbowServiceTag;
import com.ale.infra.proxy.room.GetAllRoomDataResponse;
import com.ale.infra.proxy.room.GetConfEndPointDataResponse;
import com.ale.infra.proxy.room.GetRoomDataResponse;
import com.ale.infra.proxy.room.GetUserRoomDataResponse;
import com.ale.infra.rainbow.api.ApisConstants;
import com.ale.infra.rainbow.api.IRainbowRoomService;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by wilsius on 29/07/16.
 */
public class RainbowRoomServiceAdapter implements IRainbowRoomService {
    private static final String LOG_TAG = "RainbowRoomServiceAdapter";

    private final IRESTAsyncRequest m_restAsyncRequest;
    private final IContactCacheMgr m_contactCacheMgr;
    private final IPlatformServices m_platformServices;


    public RainbowRoomServiceAdapter(IContactCacheMgr contactCacheMgr, IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices)
    {
        m_restAsyncRequest = restAsyncRequest;
        m_contactCacheMgr = contactCacheMgr;
        m_platformServices = platformServices;
    }

    @Override
    public RainbowServiceTag getTag()
    {
        return RainbowServiceTag.ROOMS;
    }

    private String getUrl()
    {
        String url = m_platformServices.getApplicationData().getServerUrl();
        if (url == null)
        {
            url = StringsUtil.EMPTY;
        }
        return url;
    }


    @Override
    public void createRoom(String roomName, String roomTopic, boolean visibility, final IAsyncServiceResultCallback<GetRoomDataResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">createRoom");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.ROOMS);

        JSONObject restBody = new JSONObject();
        try
        {
            restBody.put("name", roomName);
            restBody.put("visibility", visibility ? "public": "private");
            restBody.put("topic", roomTopic);

        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "createRoom failed");
                    notifyGetRoomDataResult(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "createRoom success");
                        notifyGetRoomDataResult(callback, null, new GetRoomDataResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST createRoom result");
                        notifyGetRoomDataResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });

    }

    @Override
    public void deleteRoom(String roomUniqueIdentifier, final IAsyncServiceVoidCallback callback) {
        Log.getLogger().verbose(LOG_TAG, ">deleteRoom");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.ROOMS);
        restUrl.append("/");

        // sending token through header or parameters does not work (see https://tools.ietf.org/html/rfc6750)
        // but it works with URI
        try
        {
            restUrl.append(URLEncoder.encode(roomUniqueIdentifier, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to deleteRoom");
        }

        m_restAsyncRequest.sendDeleteRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "deleteRoom failed");
                    notifyVoidResult(callback, asyncResult.getException());
                } else {
                    Log.getLogger().info(LOG_TAG, "deleteRoom success");
                    notifyVoidResult(callback, null);
                }
            }
        });

    }

    @Override
    public void getRoomData(String roomUniqueIdentifier, final IAsyncServiceResultCallback<GetRoomDataResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">getRoomData for room :" + roomUniqueIdentifier);

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.ROOMS);
        restUrl.append("/");

        // sending token through header or parameters does not work (see https://tools.ietf.org/html/rfc6750)
        // but it works with URI
        try
        {
            restUrl.append(URLEncoder.encode(roomUniqueIdentifier, "UTF-8"));
            restUrl.append("?format=full");
        }
        catch (UnsupportedEncodingException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to getRoomData");
        }

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "getRoomData failed");
                    notifyGetRoomDataResult(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "getRoomData success");
                        notifyGetRoomDataResult(callback, null, new GetRoomDataResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST getRoomData result");
                        notifyGetRoomDataResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void getAllRoomData(String roomUniqueIdentifier, int offset, int limit, final IAsyncServiceResultCallback<GetAllRoomDataResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">getAllRoomData");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.ROOMS);

        try
        {
            restUrl.append("?format=full");
            if (!StringsUtil.isNullOrEmpty(roomUniqueIdentifier))
                restUrl.append("&userId="  + roomUniqueIdentifier);
            if (limit != 0)
                restUrl.append("&limit=" + limit);

            restUrl.append("&offset=" + offset);

        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "getAllRoomData failed");
                    notifyGetAllRoomDataResult(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "getAllRoomData success");
                        notifyGetAllRoomDataResult(callback, null, new GetAllRoomDataResponse(m_platformServices, m_contactCacheMgr, asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST getAllRoomData result");
                        notifyGetAllRoomDataResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });

    }

    @Override
    public void changeRoomData(String roomUniqueIdentifier, String topic, boolean visibility, final IAsyncServiceResultCallback<GetRoomDataResponse> callback) {

        Log.getLogger().verbose(LOG_TAG, ">changeRoomData");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.ROOMS);
        restUrl.append("/");

        // sending token through header or parameters does not work (see https://tools.ietf.org/html/rfc6750)
        // but it works with URI
        try
        {
            restUrl.append(URLEncoder.encode(roomUniqueIdentifier, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to changeRoomData");
        }

        JSONObject restBody = new JSONObject();
        try
        {
            restBody.put("topic", topic);
            restBody.put("visibility", visibility ? "public" : "private");
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        m_restAsyncRequest.sendPutRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "changeRoomData failed");
                    notifyGetRoomDataResult(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "changeRoomData success");
                        notifyGetRoomDataResult(callback, null, new GetRoomDataResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST changeRoomData result");
                        notifyGetRoomDataResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });

    }

    @Override
    public void changeUserRoomData(String roomUniqueIdentifier, String userId, String privilege, String status, final IAsyncServiceResultCallback<GetUserRoomDataResponse> callback) {

        Log.getLogger().verbose(LOG_TAG, ">changeUserRoomData");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.ROOMS);
        restUrl.append("/");

        // sending token through header or parameters does not work (see https://tools.ietf.org/html/rfc6750)
        // but it works with URI
        try
        {
            restUrl.append(URLEncoder.encode(roomUniqueIdentifier, "UTF-8"));
            restUrl.append("/users/");
            restUrl.append(URLEncoder.encode(userId, "UTF-8"));

        }
        catch (UnsupportedEncodingException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to changeUserRoomData");
        }

        JSONObject restBody = new JSONObject();
        try
        {
            if( !StringsUtil.isNullOrEmpty(privilege)) {
                restBody.put("privilege", privilege);
            }
            if( !StringsUtil.isNullOrEmpty(status)) {
                restBody.put("status", status);
            }
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        m_restAsyncRequest.sendPutRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "changeUserRoomData failed");
                    notifyGetUserRoomDataResult(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "changeUserRoomData success");
                        notifyGetUserRoomDataResult(callback, null, new GetUserRoomDataResponse(m_platformServices, m_contactCacheMgr, asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST changeUserRoomData result");
                        notifyGetUserRoomDataResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });

    }

    @Override
    public void deleteUserFromARoom(String roomUniqueIdentifier, String participantIdToDelete, final IAsyncServiceVoidCallback callback) {
        Log.getLogger().verbose(LOG_TAG, "");
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.ROOMS);
        restUrl.append("/");
        try
        {
            restUrl.append(URLEncoder.encode(roomUniqueIdentifier, "UTF-8"));
            restUrl.append(ApisConstants.USERSROOM);
            restUrl.append("/");
            restUrl.append(URLEncoder.encode(participantIdToDelete, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to deleteRoom");
        }

        Log.getLogger().verbose(LOG_TAG, "delete participant in room request : " + restUrl.toString());
        m_restAsyncRequest.sendDeleteRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "deleteParticipantFromRoom failed");
                    notifyVoidResult(callback, asyncResult.getException());
                } else {
                    Log.getLogger().info(LOG_TAG, "deleteParticipantFromRoom success");
                    notifyVoidResult(callback, null);
                }
            }
        });
    }

    @Override
    public void addParticipantToRoom(String roomUniqueIdentifier, String participantIdToAdd, final IAsyncServiceResultCallback<GetUserRoomDataResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">addParticipantToRoom");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.ROOMS);
        restUrl.append("/");
        try
        {
            restUrl.append(URLEncoder.encode(roomUniqueIdentifier, "UTF-8"));
            restUrl.append(ApisConstants.USERSROOM);
        }
        catch (UnsupportedEncodingException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to deleteRoom");
        }

        JSONObject restBody = new JSONObject();
        try
        {
            restBody.put("userId", participantIdToAdd);
            restBody.put("reason", "invitation");
            restBody.put("status", "invited");
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "addParticipantToRoom FAILURE");
                    notifyGetUserRoomDataResult(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "addParticipantToRoom SUCCESS");
                        notifyGetUserRoomDataResult(callback, null, new GetUserRoomDataResponse(m_platformServices, m_contactCacheMgr, asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST addParticipantToRoom result");
                        notifyGetUserRoomDataResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void associateConfToRoom(String roomId, String confId, final IAsyncServiceResultCallback<GetConfEndPointDataResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">associateConfToRoom");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.ROOMS);
        restUrl.append("/");
        try {
            restUrl.append(URLEncoder.encode(roomId, "UTF-8"));
            restUrl.append(ApisConstants.CONFERENCESROOM);
        } catch (UnsupportedEncodingException e) {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to deleteRoom");
        }

        JSONObject restBody = new JSONObject();
        try {
            restBody.put("confId", confId);
        } catch (Exception ex) {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "associateConfToRoom FAILURE");
                    notifyAssociateConfToRoomResult(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "associateConfToRoom SUCCESS");
                        GetConfEndPointDataResponse getConfEndPointDataResponse = new GetConfEndPointDataResponse(asyncResult.getResult().getResponse());
                        notifyAssociateConfToRoomResult(callback, null, getConfEndPointDataResponse);
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST associateConfToRoom result");
                        notifyAssociateConfToRoomResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void dissociateConfToRoom(String roomId, String confId, final IAsyncServiceVoidCallback callback) {
        Log.getLogger().verbose(LOG_TAG, ">dissociateConfToRoom");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.ROOMS);
        restUrl.append("/");
        try {
            restUrl.append(URLEncoder.encode(roomId, "UTF-8"));
            restUrl.append(ApisConstants.CONFERENCESROOM);
            restUrl.append("/");
            restUrl.append(URLEncoder.encode(confId, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to dissociateConfToRoom");
        }

        m_restAsyncRequest.sendDeleteRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "dissociateConfToRoom FAILURE");
                    notifyVoidResult(callback, asyncResult.getException());
                } else {
                    Log.getLogger().info(LOG_TAG, "dissociateConfToRoom SUCCESS");
                    notifyVoidResult(callback, null);
                }
            }
        });
    }

    private void notifyAssociateConfToRoomResult(IAsyncServiceResultCallback<GetConfEndPointDataResponse> callback, RainbowServiceException alcServiceException, GetConfEndPointDataResponse response) {
        AsyncServiceResponseResult<GetConfEndPointDataResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyGetUserRoomDataResult(IAsyncServiceResultCallback<GetUserRoomDataResponse> callback, RainbowServiceException alcServiceException, GetUserRoomDataResponse response)
    {
        AsyncServiceResponseResult<GetUserRoomDataResponse> asyncResult = new AsyncServiceResponseResult<GetUserRoomDataResponse>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyGetRoomDataResult(IAsyncServiceResultCallback<GetRoomDataResponse> callback, RainbowServiceException alcServiceException, GetRoomDataResponse response)
    {
        AsyncServiceResponseResult<GetRoomDataResponse> asyncResult = new AsyncServiceResponseResult<GetRoomDataResponse>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyGetAllRoomDataResult(IAsyncServiceResultCallback<GetAllRoomDataResponse> callback, RainbowServiceException alcServiceException, GetAllRoomDataResponse response)
    {
        AsyncServiceResponseResult<GetAllRoomDataResponse> asyncResult = new AsyncServiceResponseResult<GetAllRoomDataResponse>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyVoidResult(IAsyncServiceVoidCallback callback, RainbowServiceException alcServiceException)
    {
        AsyncServiceResponseVoid asyncResult = new AsyncServiceResponseVoid(alcServiceException);
        callback.handleResult(asyncResult);
    }


}

package com.ale.infra.rainbow.api;

import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.proxy.room.GetAllRoomDataResponse;
import com.ale.infra.proxy.room.GetConfEndPointDataResponse;
import com.ale.infra.proxy.room.GetRoomDataResponse;
import com.ale.infra.proxy.room.GetUserRoomDataResponse;

/**
 * Created by wilsius on 29/07/16.
 */
public interface IRainbowRoomService extends IRainbowService {

    void createRoom (String roomName, String roomTopic, boolean visibility, IAsyncServiceResultCallback<GetRoomDataResponse> callback);
    void deleteRoom (String roomUniqueIdentifier, IAsyncServiceVoidCallback callback);
    void getRoomData (String roomUniqueIdentifier, IAsyncServiceResultCallback<GetRoomDataResponse> callback);
    void getAllRoomData (String roomUniqueIdentifier, int offset, int limit, IAsyncServiceResultCallback<GetAllRoomDataResponse> callback);
    void changeRoomData (String roomUniqueIdentifier, String topic ,boolean visibility, IAsyncServiceResultCallback<GetRoomDataResponse> callback);
    void deleteUserFromARoom(String roomUniqueIdentifier, String participantIdToDelete, IAsyncServiceVoidCallback callback);
    void changeUserRoomData(String roomUniqueIdentifier, String userId, String privilege, String status, IAsyncServiceResultCallback<GetUserRoomDataResponse> callback);
    void addParticipantToRoom(String roomUniqueIdentifier, String participantIdToDelete, IAsyncServiceResultCallback<GetUserRoomDataResponse> callback);
    void associateConfToRoom(String roomId, String confId, IAsyncServiceResultCallback<GetConfEndPointDataResponse> callback);
    void dissociateConfToRoom(String id, String confId, IAsyncServiceVoidCallback callback);
}

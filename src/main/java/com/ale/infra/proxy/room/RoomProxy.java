package com.ale.infra.proxy.room;

import com.ale.infra.application.IApplicationData;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseVoid;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.manager.room.Room;
import com.ale.infra.manager.room.RoomParticipant;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.rainbow.api.IRainbowRoomService;
import com.ale.infra.rainbow.api.IServicesFactory;
import com.ale.util.log.Log;

import java.util.List;

/**
 * Created by wilsius on 29/07/16.
 */
public class RoomProxy implements IRoomProxy {
    private static final String LOG_TAG = "RoomProxy";

    public static final String UNSUBSCRIBED = "unsubscribed";
    public static final String ACCEPTED = "accepted";
    public static final String REJECTED = "rejected";

    private final IContactCacheMgr m_contactCacheMgr;
    private IApplicationData m_applicationData;
    private IRainbowRoomService m_roomService;

    public RoomProxy(IServicesFactory servicesFactory, IContactCacheMgr contactCacheMgr, IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService)
    {
        Log.getLogger().info(LOG_TAG, "initialization");
        m_contactCacheMgr = contactCacheMgr;
        m_roomService = servicesFactory.createRoomService(contactCacheMgr, restAsyncRequest, platformService);
        m_applicationData = platformService.getApplicationData();
    }

    @Override
    public void createRoom(String roomName, String roomTopic, boolean visibility, final IRoomCreationListener listener) {
        Log.getLogger().info(LOG_TAG, ">createRoom");

        m_roomService.createRoom(roomName, roomTopic, visibility, new IAsyncServiceResultCallback<GetRoomDataResponse>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetRoomDataResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().verbose(LOG_TAG, "createRoom SUCCESS");

                    Room room = asyncResult.getResult().getRoom();
                    if (listener != null)
                        listener.onCreationSuccess(room);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "createRoom FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onCreationFailed();
                }
            }
        });

    }

    @Override
    public void deleteRoom(String roomUniqueIdentifier, final IDeleteRoomListener listener) {
        Log.getLogger().info(LOG_TAG, ">deleteRoom");

        m_roomService.deleteRoom(roomUniqueIdentifier, new IAsyncServiceVoidCallback() {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().verbose(LOG_TAG, "deleteRoom SUCCESS");

                    if (listener != null)
                        listener.onRoomDeletedSuccess();
                } else {
                    Log.getLogger().info(LOG_TAG, "deleteRoom FAILURE", asyncResult.getException());
                    if (listener != null) {
                        listener.onRoomDeletedFailed();
                    }
                }

            }
        });
    }

    @Override
    public void deleteParticipant(final String roomUniqueIdentifier, final String participantIdToDelete, final IDeleteParticipantListener listener) {
        Log.getLogger().debug(LOG_TAG, "deleteParticipant");
        m_roomService.deleteUserFromARoom(roomUniqueIdentifier, participantIdToDelete, new IAsyncServiceVoidCallback() {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().verbose(LOG_TAG, "deleteParticipant SUCCESS");

                    if (listener != null)
                        listener.onDeleteParticipantSuccess(roomUniqueIdentifier, participantIdToDelete);
                } else {
                    Log.getLogger().info(LOG_TAG, "deleteParticipant FAILURE", asyncResult.getException());

                    if (listener != null)
                        listener.onDeleteParticipantFailure();
                }
            }
        });
    }

    @Override
    public void addParticipantsToRoom(final Room room, final List<Contact> contactsToAdd, final IAddParticipantsListener listener) {
        Log.getLogger().info(LOG_TAG, ">addParticipantsToRoom");

        final int[] contactsNbAdded = {0};
        for(final Contact contact : contactsToAdd ) {

            m_roomService.addParticipantToRoom(room.getId(), contact.getCorporateId(), new IAsyncServiceResultCallback<GetUserRoomDataResponse>() {
                @Override
                public void handleResult(AsyncServiceResponseResult<GetUserRoomDataResponse> asyncResult) {
                    if (!asyncResult.exceptionRaised()) {
                        Log.getLogger().verbose(LOG_TAG, "addParticipantToRoom SUCCESS");

                        RoomParticipant roomParticipant = asyncResult.getResult().getRoomParticipant();
                        room.addParticipant(roomParticipant);
                        contactsNbAdded[0]++;
                        if( contactsNbAdded[0] >= contactsToAdd.size() && listener != null) {
                            listener.onAddParticipantsSuccess();
                        }
                    } else {
                        Log.getLogger().info(LOG_TAG, "addParticipantToRoom FAILURE", asyncResult.getException());
                        if( listener != null)
                            listener.onAddParticipantFailed(null);
                    }
                }
            });
        }
    }

    @Override
    public void associateConfToRoom(final Room room, String confId, final IAssociateConfListener listener) {
        Log.getLogger().info(LOG_TAG, ">associateConfToRoom");

        m_roomService.associateConfToRoom(room.getId(), confId, new IAsyncServiceResultCallback<GetConfEndPointDataResponse>() {
                @Override
                public void handleResult(AsyncServiceResponseResult<GetConfEndPointDataResponse> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().verbose(LOG_TAG, "associateConfToRoom SUCCESS");

                    room.setEndPoint(asyncResult.getResult().getConfEndPoint());
                    if( listener != null)
                        listener.onAssociateConfSuccess();
                } else {
                    Log.getLogger().info(LOG_TAG, "associateConfToRoom FAILURE", asyncResult.getException());
                    if( listener != null)
                        listener.onAssociateConfFailed();
                }
            }
        });
    }

    @Override
    public void dissociateConfToRoom(final Room room, String confId, final IDissociateConfListener listener) {
        Log.getLogger().info(LOG_TAG, ">dissociateConfToRoom");

        m_roomService.dissociateConfToRoom(room.getId(), confId, new IAsyncServiceVoidCallback() {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().verbose(LOG_TAG, "dissociateConfToRoom SUCCESS");

                    room.clearRoomConfEndPoints();
                    if( listener != null)
                        listener.onDissociateConfSuccess();
                } else {
                    Log.getLogger().info(LOG_TAG, "dissociateConfToRoom FAILURE", asyncResult.getException());
                    if( listener != null)
                        listener.onDissociateConfFailed();
                }
            }
        });
    }

    @Override
    public void getRoomData(final String roomUniqueIdentifier, final IGetRoomDataListener listener) {
        Log.getLogger().info(LOG_TAG, ">getRoomData for room :" + roomUniqueIdentifier);

        m_roomService.getRoomData(roomUniqueIdentifier, new IAsyncServiceResultCallback<GetRoomDataResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetRoomDataResponse> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().verbose(LOG_TAG, "getRoomData SUCCESS");

                    GetRoomDataResponse roomDataResponse = asyncResult.getResult();
                    Room room = roomDataResponse.getRoom();

                    m_contactCacheMgr.resolveDirectoryContacts(roomDataResponse.getUnresolvedContacts());

                    if (listener != null)
                        listener.onGetRoomDataSuccess(room);
                } else {
                    Log.getLogger().info(LOG_TAG, "getRoomData FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onGetRoomDataFailure(roomUniqueIdentifier);
                }
            }
        });
    }

    @Override
    public void getAllRoomData(String roomUniqueIdentifier, int offset, int limit, final IGetAllRoomDataListener listener) {
        Log.getLogger().info(LOG_TAG, ">getAllRoomData");

        m_roomService.getAllRoomData(roomUniqueIdentifier, offset, limit, new IAsyncServiceResultCallback<GetAllRoomDataResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetAllRoomDataResponse> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().verbose(LOG_TAG, "getAllRoomData SUCCESS");

                    GetAllRoomDataResponse roomDataResponse = asyncResult.getResult();
                    List<Room> room = roomDataResponse.getRooms();

                    m_contactCacheMgr.resolveDirectoryContacts(roomDataResponse.getUnresolvedContacts());

                    if (listener != null)
                        listener.onGetAllRoomsSuccess(room);
                } else {
                    Log.getLogger().info(LOG_TAG, "getAllRoomData FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onGetAllRoomsFailed();
                }
            }
        });
    }

    @Override
    public void changeRoomData(String roomUniqueIdentifier, String topic, boolean visibility, final IChangeRoomDataListener listener) {
        Log.getLogger().info(LOG_TAG, ">changeRoomData");
        m_roomService.changeRoomData(roomUniqueIdentifier, topic, visibility, new IAsyncServiceResultCallback<GetRoomDataResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetRoomDataResponse> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().verbose(LOG_TAG, "changeRoomData SUCCESS");

                    Room room = asyncResult.getResult().getRoom();
                    if (listener != null)
                        listener.onChangeRoomDataSuccess(room);
                } else {
                    Log.getLogger().info(LOG_TAG, "changeRoomData FAILURE", asyncResult.getException());
                    listener.onChangeRoomDataFailed(asyncResult.getResult().getRoom());
                }
            }
        });
    }

    @Override
    public void changeUserRoomData(String roomUniqueIdentifier, String userId, String privilege, String status, final IChangeUserRoomDataListener listener) {
        Log.getLogger().info(LOG_TAG, ">changeUserRoomData");

        m_roomService.changeUserRoomData(roomUniqueIdentifier, userId, privilege, status, new IAsyncServiceResultCallback<GetUserRoomDataResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetUserRoomDataResponse> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().verbose(LOG_TAG, "changeUserRoomData SUCCESS");

                    RoomParticipant roomParticipant = asyncResult.getResult().getRoomParticipant();
                    if (listener != null)
                        listener.onChangeUserRoomDataSuccess(roomParticipant);
                } else {
                    Log.getLogger().info(LOG_TAG, "changeUserRoomData FAILURE", asyncResult.getException());
                    listener.onChangeUserRoomDataFailed();
                }
            }
        });
    }
}

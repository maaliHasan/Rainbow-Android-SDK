package com.ale.infra.proxy.room;

import com.ale.infra.contact.Contact;
import com.ale.infra.manager.room.Room;
import com.ale.infra.manager.room.RoomParticipant;

import java.util.List;

/**
 * Created by wilsius on 29/07/16.
 */
public interface IRoomProxy {
    void createRoom (String roomName, String roomTopic, boolean visibility, IRoomCreationListener listener);
    void deleteRoom (String RoomUniqueIdentifier, IDeleteRoomListener listener);
    void deleteParticipant(String roomUniqueIdentifier, String participantIdToDelete, IDeleteParticipantListener listener);
    void getRoomData (String RoomUniqueIdentifier, IGetRoomDataListener listener);
    void getAllRoomData (String RoomUniqueIdentifier, int offset, int limit, IGetAllRoomDataListener listener);
    void changeRoomData (String RoomUniqueIdentifier, String topic ,boolean visibility, IChangeRoomDataListener listener);
    void changeUserRoomData(String roomUniqueIdentifier, String userId, String privilege, String status, IChangeUserRoomDataListener listener);
    void addParticipantsToRoom(Room room, List<Contact> m_contactsAlreadySelected, IAddParticipantsListener listener);

    void associateConfToRoom(Room room, String confId, IAssociateConfListener listener);
    void dissociateConfToRoom(Room room, String confId, IDissociateConfListener listener);

    interface IRoomCreationListener {
        void onCreationSuccess(Room room);

        void onCreationFailed();
    }

    interface IDeleteRoomListener
    {
        void onRoomDeletedSuccess();
        void onRoomDeletedFailed();
    }

    interface IDeleteParticipantListener
    {
        void onDeleteParticipantSuccess(String roomId, String participantIdDeleted);

        void onDeleteParticipantFailure();
    }

    interface IChangeRoomDataListener
    {
        void onChangeRoomDataSuccess(Room room);
        void onChangeRoomDataFailed(Room room);
    }

    interface IChangeUserRoomDataListener
    {
        void onChangeUserRoomDataSuccess(RoomParticipant roomParticipant);
        void onChangeUserRoomDataFailed();
    }

    interface IGetRoomDataListener
    {
        void onGetRoomDataSuccess(Room room);

        void onGetRoomDataFailure(String roomId);
    }

    interface IGetAllRoomDataListener
    {
        void onGetAllRoomsSuccess(List<Room> rooms);

        void onGetAllRoomsFailed();
    }

    interface IAddParticipantsListener
    {
        void onAddParticipantsSuccess();

        void onMaxParticipantsReached();

        void onAddParticipantFailed(Contact contactFailed);
    }

    interface IAssociateConfListener
    {
        void onAssociateConfSuccess();
        void onAssociateConfFailed();
    }

    public interface IDissociateConfListener {
        void onDissociateConfSuccess();
        void onDissociateConfFailed();
    }
}

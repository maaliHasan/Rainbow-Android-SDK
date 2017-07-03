package com.ale.infra.manager.room;

import com.ale.infra.contact.Contact;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.manager.MultiUserChatMgr;
import com.ale.infra.proxy.room.IRoomProxy;
import com.ale.infra.xmpp.XmppConnection;

import java.util.List;

/**
 * Created by trunk1 on 17/01/2017.
 */

public interface IRoomMgr {
    ArrayItemList<Room> getAllRooms();

    void setObserver(XmppConnection connection);
    void getRoomsFromDB();
    Room getRoomById(String roomId);
    Room getRoomByJid(String roomJid);
//    void archiveRoom(Room room);
    void updateRoom(Room room);

    void createRoom(String roomName, String roomTopic, IRoomProxy.IRoomCreationListener listener);
    void addParticipantsToRoom(Room room, List<Contact> contactList, IRoomProxy.IAddParticipantsListener listener);
    void refreshAllRooms(IRoomRefreshListener listener);
    void deleteRoom(Room room, IRoomProxy.IDeleteRoomListener listener);
    void deleteParticipantFromRoom(Room room, Contact contactToDelete, IRoomProxy.IDeleteParticipantListener listener);
    void changeUserRoomData(Room room, String contactCorpId, RoomStatus status, final IRoomProxy.IChangeUserRoomDataListener listener);
    void getRoomData(Room room, final IRoomProxy.IGetRoomDataListener listener);
    void changeRoomData(Room room, String topic, boolean visibility, final IRoomProxy.IChangeRoomDataListener listener);


    void acceptInvitation(Room room, IRoomProxy.IChangeUserRoomDataListener listener);
    void rejectInvitation(Room room, IRoomProxy.IChangeUserRoomDataListener listener);
    void leaveRoom(Room room, IRoomProxy.IDeleteParticipantListener listener);

    void addNewRoom(Room room);
    void deleteRoom(Room room);
    void roomParticipantChange(RoomChange roomChange, Room room);

    void onRoomChange(RoomChange roomChange);
    void setMultiUserChatMgr(MultiUserChatMgr multiUserChatMgr);

    List<Room> getPendingRoomList();
    List<Room> getAllRoomList();
    List<Room> getMyRoomList();
    List<Room> getArchivedRoomList();

    void displayPendingRoomNotification(Room room);

    String getFirstEscalationRoomNameAvailable(String unknown_name, String roomNameTrad);

    Room findRoomStrictlyWithParticipants(List<Contact> contacts);

    void associateConfToRoom(Room room, String confId, IRoomProxy.IAssociateConfListener listener);
    void dissociateConfToRoom(Room room, String confEndpointId, IRoomProxy.IDissociateConfListener listener);

    interface IRoomRefreshListener {
        void onRoomRefreshSuccess();
        void onRoomRefreshFailed();
    }


}

package com.ale.infra.manager.room;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.capabilities.ICapabilities;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.database.IDatabaseMgr;
import com.ale.infra.database.RoomDataSource;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.manager.ChatMgr;
import com.ale.infra.manager.Conversation;
import com.ale.infra.manager.MultiUserChatMgr;
import com.ale.infra.manager.XmppContactMgr;
import com.ale.infra.manager.pgiconference.IPgiConferenceMgr;
import com.ale.infra.manager.pgiconference.PgiConference;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.room.IRoomProxy;
import com.ale.infra.xmpp.XmppConnection;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by georges on 12/09/2016.
 */
public class RoomMgr implements IRoomMgr, XmppContactMgr.XmppContactMgrListener{
    private static final String LOG_TAG = "RoomMgr";

    private static final int ROOM_MAX_NUMBER = 200;
    private final ICapabilities m_capabilities;

    private final IPgiConferenceMgr m_pgiMgr;
    private IContactCacheMgr m_contactCache;
    private ArrayItemList<Room> m_allRooms;
    private RoomDataSource m_roomDataSource;
    private IRoomProxy m_roomProxy;
    private MultiUserChatMgr m_multiUserChatMgr;
    private ChatMgr m_chatMgr;

    public void setChatMgr(ChatMgr chatMgr) {
        m_chatMgr = chatMgr;
    }


    public RoomMgr(IContactCacheMgr contactCacheMgr, IPgiConferenceMgr pgiMgr, IPlatformServices platformServices, IDatabaseMgr dataBaseMgr, IRoomProxy roomProxy, ICapabilities capabilities) {
        m_allRooms = new ArrayItemList<>();
        m_contactCache = contactCacheMgr;
        m_pgiMgr = pgiMgr;
        m_roomProxy = roomProxy;
        m_capabilities = capabilities;

        if (dataBaseMgr != null) {
            m_roomDataSource = dataBaseMgr.getRoomDataSource();
        }
    }

    public void getRoomsFromDB() {
        if (m_roomDataSource != null)
            setRooms (m_roomDataSource.getAllRooms(), false);
    }

    public ArrayItemList<Room> getAllRooms() {
        return m_allRooms;
    }

    public synchronized int getRoomIndex(Room room) {
        for (int i = 0; i < m_allRooms.getCount();i++ ) {
            if (m_allRooms.get(i).getId().equalsIgnoreCase(room.getId())) {
                return i;
            }
        }
        return -1;
    }

    public synchronized Room getRoomById(String roomId) {
        for (Room room : m_allRooms.getCopyOfDataList()) {
            if (room.getId().equals(roomId)) {
                Log.getLogger().debug(LOG_TAG, ">getRoomById room id: " + roomId + " found, name is: " + room.getName());
                return room;
            }
        }
        Log.getLogger().debug(LOG_TAG, ">getRoomById room not found " + roomId );
        return null;
    }

    public synchronized Room getRoomByJid(String roomJid) {
        for (Room room : m_allRooms.getCopyOfDataList()) {
            if (room.getJid().equals(roomJid)) {
                Log.getLogger().debug(LOG_TAG, ">getRoomByJid room Jid: " + roomJid + " found, name is: " + room.getName());
                return room;
            }
        }
        Log.getLogger().debug(LOG_TAG, ">getRoomByJid room not found " + roomJid );
        return null;
    }

    public Room getRoomByName(String roomName, String roomTopic) {
        Log.getLogger().verbose(LOG_TAG, ">getRoomByName" );

        for (Room room : m_allRooms.getCopyOfDataList()) {
            if (room.getName().equals(roomName)) {
                if( roomTopic!= null && !room.getTopic().equals(roomTopic))
                    continue;

                return room;
            }
        }
        return null;
    }

    @Override
    public synchronized void deleteRoom(Room room) {
        Log.getLogger().verbose(LOG_TAG, ">deleteRoom");

        if ((m_allRooms.getCopyOfDataList()).contains(room))
        {
            m_allRooms.delete(room);
        }
        if (m_roomDataSource != null)
            m_roomDataSource.deleteRoom(room);
    }

    private void deleteConversation(Room room) {
        Log.getLogger().verbose(LOG_TAG, ">deleteRoom");

        Conversation conv = m_chatMgr.getConversationFromJid(room.getJid());
        if( conv != null) {
            m_chatMgr.deleteConversation(conv);
        }
    }

    @Override
    public synchronized void roomParticipantChange(RoomChange roomChange, Room room) {
        room.changeParticipantStatus(roomChange);
        updateDb(room);
    }

    public synchronized void setRooms(List<Room> rooms, boolean updateDB) {
        Log.getLogger().verbose(LOG_TAG, ">setRooms");
        List<Room> newRooms = new ArrayList<>();

        for (Room room: rooms) {
            Room resolvedRoom = getRoomById(room.getId());
            checkPgiConfInRoom(room);
            if (resolvedRoom != null) {
                //New room
                resolvedRoom.update(room);
            } else {
                resolvedRoom = room;
            }
            newRooms.add(resolvedRoom);
        }
        m_allRooms.replaceAll(newRooms);

        if (updateDB)
            refreshDB();
    }

    private void checkPgiConfInRoom(final Room room) {
        Log.getLogger().verbose(LOG_TAG, ">checkPgiConfInRoom");

        if( room.getEndPoints().size() > 0 && room.getPgiConference() == null) {
            RoomConfEndPoint confEndPoint = room.getEndPoints().get(0);

            PgiConference pgiConf = m_pgiMgr.getConferenceFromEndPoint(confEndPoint);
            room.setPgiConference(pgiConf);
        }
    }

    private synchronized void refreshDB() {
        for (Room currentRoom : m_allRooms.getCopyOfDataList()) {
            if (!StringsUtil.isNullOrEmpty(currentRoom.getId())) {
                updateDb(currentRoom);
            }
        }
    }

    @Override
    public synchronized void addNewRoom(Room room) {
        if(!room.isRoomVisibleForGui())
            return;

        checkPgiConfInRoom(room);

        Room mergedRoom = getRoomById(room.getId());
        if (mergedRoom != null) {
            mergedRoom.update(room);
        } else {
            mergedRoom = room;
            m_allRooms.uniqueAdd(room);
        }

        updateDb(mergedRoom);
    }

    @Override
    public synchronized void updateRoom(Room room) {
        checkPgiConfInRoom(room);

        int index = getRoomIndex(room);
        if (index >= 0) {
            Room existingRoom = getAllRooms().get(index);
            existingRoom.update(room);
            room = existingRoom;
        } else {
            addNewRoom(room);
        }
        room.resolveParticipants();

        m_allRooms.fireDataChanged();
        updateDb(room);
    }

    public void updateDb(Room room) {
        if (m_roomDataSource != null)
            m_roomDataSource.createOrUpdateRoom(room);
    }

    @Override
    public void setObserver(XmppConnection connection) {
        if( connection != null && connection.getXmppContactMgr() != null) {
            connection.getXmppContactMgr().registerChangeListener(this);
        }
    }

    public synchronized void refreshAllRooms(final IRoomRefreshListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">refreshAllRooms");

        if(m_roomProxy == null) {
            Log.getLogger().warn(LOG_TAG, ">createRoom : no room proxy");
            return;
        }

        m_roomProxy.getAllRoomData(m_contactCache.getUser().getCorporateId(), 0, ROOM_MAX_NUMBER, new IRoomProxy.IGetAllRoomDataListener() {
            @Override
            public synchronized void onGetAllRoomsSuccess(List<Room> rooms) {
                Log.getLogger().debug(LOG_TAG, ">onGetAllRoomsSuccess;" + rooms.size());
                setAllRoomDataList(rooms);
                if (listener != null)
                    listener.onRoomRefreshSuccess();
            }

            @Override
            public synchronized void onGetAllRoomsFailed() {
                Log.getLogger().debug(LOG_TAG, ">onGetAllRoomsFailed");
                if (listener != null)
                    listener.onRoomRefreshFailed();
            }
        });
    }

    private synchronized void setAllRoomDataList(List<Room> roomList) {
        List<Room> rooms = new ArrayList<>();
        for (Room room: roomList) {
            if (room.isRoomVisibleForGui())
                rooms.add(room);
        }
        setRooms(rooms, true);
    }


    @Override
    public synchronized void createRoom(String roomName, String roomTopic, final IRoomProxy.IRoomCreationListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">createRoom");

        if(m_roomProxy == null) {
            Log.getLogger().warn(LOG_TAG, ">createRoom : no room proxy");
            if (listener != null)
                listener.onCreationFailed();
            return;
        }

        //check if room already exists
        Room existingRoom = getRoomByName(roomName, roomTopic);
        if (existingRoom != null) {
            Log.getLogger().warn(LOG_TAG, ">createRoom : room already exists");
            if (listener != null)
                listener.onCreationSuccess(existingRoom);
        }


        m_roomProxy.createRoom(roomName, roomTopic, true, new IRoomProxy.IRoomCreationListener() {
            @Override
            public synchronized void onCreationSuccess(Room room) {
                Log.getLogger().verbose(LOG_TAG, ">onCreationSuccess");
                addNewRoom(room);

                if( m_multiUserChatMgr != null)
                    m_multiUserChatMgr.joinRoomIfNeeded(room);

                if (listener != null)
                    listener.onCreationSuccess(room);
            }

            @Override
            public synchronized void onCreationFailed() {
                Log.getLogger().warn(LOG_TAG, ">onCreationFailed");
                refreshAllRooms(null);
                if (listener != null)
                    listener.onCreationFailed();
            }
        });
    }

    @Override
    public synchronized void deleteRoom(final Room room, final IRoomProxy.IDeleteRoomListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">deleteRoom");
        if(m_roomProxy == null) {
            Log.getLogger().warn(LOG_TAG, ">deleteRoom : no room proxy");
            if (listener != null)
                listener.onRoomDeletedFailed();
            return;
        }

        if (room == null) {
            Log.getLogger().warn(LOG_TAG, "Room not available");
            return;
        }

        m_roomProxy.deleteRoom(room.getId(), new IRoomProxy.IDeleteRoomListener() {
            @Override
            public synchronized void onRoomDeletedSuccess() {
                Log.getLogger().verbose(LOG_TAG, ">onRoomDeletedSuccess");
                deleteRoom(room);
                deleteConversation(room);
                if (listener != null)
                    listener.onRoomDeletedSuccess();
            }

            @Override
            public synchronized void onRoomDeletedFailed() {
                Log.getLogger().warn(LOG_TAG, ">onRoomDeletedFailed");
                refreshAllRooms(null);
                if (listener != null)
                    listener.onRoomDeletedFailed();
            }
        });
    }

    @Override
    public synchronized void addParticipantsToRoom(final Room room, List<Contact> contactList, final IRoomProxy.IAddParticipantsListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">addUserInGroup");

        if(m_roomProxy == null) {
            Log.getLogger().warn(LOG_TAG, ">addParticipantsToRoom : no room proxy");
            if (listener != null)
                listener.onAddParticipantFailed(null);
            return;
        }

        if (room == null) {
            Log.getLogger().warn(LOG_TAG, "Room not available");
            if (listener != null)
                listener.onAddParticipantFailed(null);
            return;
        }

        if (contactList.size() == 0) {
            Log.getLogger().warn(LOG_TAG, "No contact to add in Room available");
            if (listener != null)
                listener.onAddParticipantFailed(null);
            return;
        }

        if (room.getParticipants().getCount() + contactList.size() > m_capabilities.getMaxBubbleParticipants()) {
            Log.getLogger().warn(LOG_TAG, "Max participants reached");
            if (listener != null)
                listener.onMaxParticipantsReached();
            return;
        }

        m_roomProxy.addParticipantsToRoom(room, contactList, new IRoomProxy.IAddParticipantsListener() {
            @Override
            public synchronized void onAddParticipantsSuccess() {
                Log.getLogger().verbose(LOG_TAG, ">onAddParticipantsSuccess");
                getRoomData(room, null);
                if (listener != null)
                    listener.onAddParticipantsSuccess();
            }

            @Override
            public void onMaxParticipantsReached()
            {
                // already tested
            }

            @Override
            public synchronized void onAddParticipantFailed(Contact contactFailed) {
                Log.getLogger().warn(LOG_TAG, ">onAddParticipantFailed");
                if (contactFailed == null) {
                    Log.getLogger().warn(LOG_TAG, ">onAddParticipantFailed : contactFailed = null" );
                } else {
                    Log.getLogger().warn(LOG_TAG, ">onAddParticipantFailed" + contactFailed.getDisplayName(""));
                }
                getRoomData(room, null);
                if (listener != null)
                    listener.onAddParticipantFailed(contactFailed);
            }
        });
    }

    @Override
    public synchronized void deleteParticipantFromRoom(final Room room, final Contact contactToDelete, final IRoomProxy.IDeleteParticipantListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">deleteParticipantFromRoom");
        if(m_roomProxy == null) {
            Log.getLogger().warn(LOG_TAG, ">deleteParticipantFromRoom : no room proxy");
            if (listener != null)
                listener.onDeleteParticipantFailure();
            return;
        }

        if (room == null) {
            Log.getLogger().warn(LOG_TAG, "Room not available");
            return;
        }

        if (contactToDelete == null) {
            Log.getLogger().warn(LOG_TAG, "No contact to delete in Room available");
            return;
        }

        m_roomProxy.deleteParticipant(room.getId(), contactToDelete.getCorporateId(), new IRoomProxy.IDeleteParticipantListener() {
            @Override
            public synchronized void onDeleteParticipantSuccess(String roomId, String participantIdDeleted) {
                Log.getLogger().verbose(LOG_TAG, "Delete User in room Success");

                if (room != null && room.getId().equalsIgnoreCase(roomId)) {
                    RoomParticipant participant = room.getParticipantWithId(participantIdDeleted);
                    if (participant != null) {
                        Log.getLogger().verbose(LOG_TAG, "Deleting Participant from Room in local");
                        participant.setStatus(RoomStatus.DELETED);

                        updateRoom(room);
                    }
                }

                if (listener != null)
                    listener.onDeleteParticipantSuccess(roomId, participantIdDeleted);
            }

            @Override
            public synchronized void onDeleteParticipantFailure() {
                Log.getLogger().warn(LOG_TAG, "Delete User in room not possible");
                getRoomData(room, null);
                if (listener != null)
                    listener.onDeleteParticipantFailure();
            }
        });
    }

    @Override
    public synchronized void getRoomData(Room room, final IRoomProxy.IGetRoomDataListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">getRoomData");
        if(m_roomProxy == null) {
            Log.getLogger().warn(LOG_TAG, ">getRoomData : no room proxy");
            if (listener != null)
                listener.onGetRoomDataFailure(room.getId());
            return;
        }

        if (room == null) {
            Log.getLogger().warn(LOG_TAG, "Room not available");
            return;
        }

        m_roomProxy.getRoomData(room.getId(), new IRoomProxy.IGetRoomDataListener() {
            @Override
            public synchronized void onGetRoomDataSuccess(Room foundRoom) {
                Log.getLogger().verbose(LOG_TAG, ">onGetRoomDataSuccess");
                updateRoom(foundRoom);
                if (listener != null)
                    listener.onGetRoomDataSuccess(foundRoom);
            }

            @Override
            public synchronized void onGetRoomDataFailure(String roomUniqueIdentifier) {
                Log.getLogger().warn(LOG_TAG, ">onGetRoomDataFailure");
                if (listener != null)
                    listener.onGetRoomDataFailure(roomUniqueIdentifier);
            }
        });
    }

    @Override
    public synchronized void changeUserRoomData(final Room room, String contactCorpId, RoomStatus status, final IRoomProxy.IChangeUserRoomDataListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">changeUserRoomData");
        if(m_roomProxy == null) {
            Log.getLogger().warn(LOG_TAG, ">changeUserRoomData : no room proxy");
            if (listener != null)
                listener.onChangeUserRoomDataFailed();
            return;
        }

        if (room == null) {
            Log.getLogger().warn(LOG_TAG, "Room not available");
            if (listener != null)
                listener.onChangeUserRoomDataFailed();
            return;
        }

        if (StringsUtil.isNullOrEmpty(contactCorpId)) {
            Log.getLogger().warn(LOG_TAG, "No contact to delete in Room available");
            if (listener != null)
                listener.onChangeUserRoomDataFailed();
            return;
        }

        m_roomProxy.changeUserRoomData(room.getId(), contactCorpId, null, status.toString(), new IRoomProxy.IChangeUserRoomDataListener() {
            @Override
            public synchronized void onChangeUserRoomDataSuccess(RoomParticipant roomParticipant) {
                Log.getLogger().debug(LOG_TAG, ">onChangeUserRoomDataSuccess");
                RoomParticipant participant = room.getParticipantWithId(roomParticipant.getId());
                if (participant != null) {
                    Log.getLogger().verbose(LOG_TAG, "Update Participant from Room in local");
                    participant.setStatus(roomParticipant.getStatus());
                    participant.setRole(roomParticipant.getRole());

                    updateRoom(room);
                }
                if (listener != null)
                    listener.onChangeUserRoomDataSuccess(roomParticipant);
            }

            @Override
            public synchronized void onChangeUserRoomDataFailed() {
                Log.getLogger().verbose(LOG_TAG, ">onChangeUserRoomDataFailed");
                getRoomData(room, null);
                if (listener != null)
                    listener.onChangeUserRoomDataFailed();
            }
        });
    }

    @Override
    public synchronized void changeRoomData(Room room, String topic, boolean visibility, final IRoomProxy.IChangeRoomDataListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">changeRoomData");
        if(m_roomProxy == null) {
            Log.getLogger().warn(LOG_TAG, ">changeRoomData : no room proxy");
            if (listener != null)
                listener.onChangeRoomDataFailed(room);
            return;
        }

        if (room == null) {
            Log.getLogger().warn(LOG_TAG, "Room not available");
            return;
        }

        m_roomProxy.changeRoomData(room.getId(), topic, visibility, new IRoomProxy.IChangeRoomDataListener() {
            @Override
            public synchronized void onChangeRoomDataSuccess(Room foundRoom) {
                updateRoom(foundRoom);
                if (listener != null)
                    listener.onChangeRoomDataSuccess(foundRoom);
            }

            @Override
            public synchronized void onChangeRoomDataFailed(Room foundRoom) {
                Log.getLogger().warn(LOG_TAG, ">onChangeRoomDataFailed");
                getRoomData(foundRoom, null);
                if (listener != null)
                    listener.onChangeRoomDataFailed(foundRoom);
            }
        });
    }

    @Override
    public void acceptInvitation(final Room room, final IRoomProxy.IChangeUserRoomDataListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">acceptInvitation");
        changeUserRoomData(room, m_contactCache.getUser().getCorporateId(), RoomStatus.ACCEPTED, new IRoomProxy.IChangeUserRoomDataListener() {
            @Override
            public void onChangeUserRoomDataSuccess(RoomParticipant roomParticipant) {
                //room participant updated by changeUserRoomData
                //cancel notification
                m_allRooms.fireDataChanged();//to trig RoomsListAdpater of changed whithout loosing Muc participantsListener
                if (listener != null)
                    listener.onChangeUserRoomDataSuccess(roomParticipant);
            }

            @Override
            public void onChangeUserRoomDataFailed() {
                getRoomData(room, null);
                m_allRooms.fireDataChanged();//to trig RoomsListAdpater of changed whithout loosing Muc participantsListener
                if (listener != null)
                    listener.onChangeUserRoomDataFailed();
            }
        });
    }

    @Override
    public void rejectInvitation(final Room room, final IRoomProxy.IChangeUserRoomDataListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">rejectInvitation");
        changeUserRoomData(room, m_contactCache.getUser().getCorporateId(), RoomStatus.REJECTED, new IRoomProxy.IChangeUserRoomDataListener() {
            @Override
            public void onChangeUserRoomDataSuccess(RoomParticipant roomParticipant) {
                //room participant updated by changeUserRoomData
                //cancel notification
                m_allRooms.fireDataChanged();//to trig RoomsListAdpater of changed whithout loosing Muc participantsListener
                if (listener != null)
                    listener.onChangeUserRoomDataSuccess(roomParticipant);
            }

            @Override
            public void onChangeUserRoomDataFailed() {
                getRoomData(room, null);
                m_allRooms.fireDataChanged(); //to trig RoomsListAdpater of changed whithout loosing Muc participantsListener
                if (listener != null)
                    listener.onChangeUserRoomDataFailed();
            }
        });
    }

    @Override
    public void leaveRoom(Room room, IRoomProxy.IDeleteParticipantListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">leaveRoom");
        deleteParticipantFromRoom(room, m_contactCache.getUser(), listener);
    }

    //deleteRoom : see above

    @Override
    public void onUserLoaded() {
        refreshAllRooms(null);
    }

    @Override
    public void rostersChanged() {

    }

    @Override
    public void setMultiUserChatMgr(MultiUserChatMgr multiUserChatMgr){
        m_multiUserChatMgr = multiUserChatMgr;
    }

    //triggered by chatMgr
    @Override
    public void onRoomChange(RoomChange roomChange) {
        Log.getLogger().verbose(LOG_TAG, ">onRoomChange");
        //Status is ACCEPTED, REJECTED, DELETED, PENDING

        if (roomChange.isTopicChange()) {
            Room room = getRoomById(roomChange.getRoomId());
            if (room != null) {
                room.setTopic(roomChange.getTopic());
                updateRoom(room);
            } else {
                Log.getLogger().error(LOG_TAG, "room topic changed on unknown room");
            }
            return;
        }

        Room room = getRoomById(roomChange.getRoomId());
        if (room == null ) {
            if (roomChange.getStatus().equals("deleted")) {
                Log.getLogger().warn(LOG_TAG, "RoomChange deleted; room not found");
                return;
            }

            // Maybe the room is created by myself on another device
            if (m_contactCache.getUser().getImJabberId().equalsIgnoreCase((roomChange.getUserJid()))) {
                Room newRoom = new Room();
                newRoom.setId(roomChange.getRoomId());

                getRoomData(newRoom, null);
                return;
            }
        } else {
            Contact ownUser = m_contactCache.getUser();
            switch (roomChange.getStatus()) {
                case DELETED:
                    if (roomChange.getUserJid().equals(ownUser.getImJabberId())) {
                        leaveRoom(room, null);
                        roomParticipantChange(roomChange, room);
                        deleteRoom(room);
                    } else {
                        roomParticipantChange(roomChange, room);
                    }
                    //check if participant delete or room delete
//                    m_chatMgr.deleteConversation(m_chatMgr.getConversationFromJid(roomChange.getRoomJid()));
//                    m_multiUserChatMgr.deleteConversation(room);
                    break;
                case ACCEPTED:
                    roomParticipantChange(roomChange, room);

                    break;
                case UNSUBSCRIBED:
                    roomParticipantChange(roomChange, room);
//                    if (roomChange.getUserJid().equals(ownUser.getImJabberId())) {
//                        archiveRoom(room);
//                    }
                    break;
//                case REJECTED:
//                    break;
//                case PENDING:
//                    break;
                default:
                    Log.getLogger().warn(LOG_TAG, ">No specific action for room status =" + roomChange.getStatus());
                    break;
            }
            if (!roomChange.getStatus().equals(RoomStatus.DELETED)) {
                getRoomData(room, null);
            }
        }
    }

    public List<Room> getPendingRoomList() {
        List<Room> pendingRoomList = new ArrayList<>();

        for (Room currentRoom : m_allRooms.getCopyOfDataList()) {
            if (currentRoom.isUserInvited()) {
                pendingRoomList.add(currentRoom);
            }
        }
        return pendingRoomList;
    }

    public List<Room> getAllRoomList() { //MyOwn + all other - archived - pending
        List<Room> pendingRoomList = new ArrayList<>();

        for (Room currentRoom : m_allRooms.getCopyOfDataList()) {
            if (!currentRoom.isUserInvited() && !currentRoom.isRoomArchived()) {
                pendingRoomList.add(currentRoom);
            }
        }
        return pendingRoomList;
    }

    public List<Room> getMyRoomList() {
        List<Room> pendingRoomList = new ArrayList<>();

        for (Room currentRoom : m_allRooms.getCopyOfDataList()) {
            if (currentRoom.isUserOwner() && !currentRoom.isRoomArchived()) {
                pendingRoomList.add(currentRoom);
            }
        }
        return pendingRoomList;
    }

    public List<Room> getArchivedRoomList() {
        List<Room> pendingRoomList = new ArrayList<>();

        for (Room currentRoom : m_allRooms.getCopyOfDataList()) {
            if (currentRoom.isRoomArchived()) {
                pendingRoomList.add(currentRoom);
            }
        }
        return pendingRoomList;
    }

    public void displayPendingRoomNotification(Room room){
        m_allRooms.fireDataChanged();
    }

    private String getEscalationRoomNameBegin(String unknown_name, String roomNameTrad) {
        StringBuilder roomName = new StringBuilder();

        Contact user = m_contactCache.getUser();
        if( user != null) {
            roomName.append(user.getInitials(unknown_name));
            roomName.append(" ");
        }
        roomName.append(roomNameTrad);
        roomName.append(" ");

        return roomName.toString();
    }

    private int getLastEscalationRoomNameIndex(String escalationNameBegin) {

        for(int index=0;index<50;index++) {
            StringBuilder roomName = new StringBuilder();
            roomName.append(escalationNameBegin);
            roomName.append(String.valueOf(index));

            if( getRoomByName(roomName.toString(), null) == null ) {
                Log.getLogger().verbose(LOG_TAG, "Room with name: "+roomName.toString()+" not found");
                return index;
            }
        }

        return -1;
    }

    @Override
    public String getFirstEscalationRoomNameAvailable(String unknown_name, String roomNameTrad) {
        StringBuilder roomName = new StringBuilder();
        String escalationNameBegin = getEscalationRoomNameBegin(unknown_name, roomNameTrad);
        roomName.append(escalationNameBegin);
        roomName.append( getLastEscalationRoomNameIndex(escalationNameBegin) );

        return roomName.toString();
    }

    @Override
    public Room findRoomStrictlyWithParticipants(List<Contact> contacts) {

        for(Room room : m_allRooms.getCopyOfDataList()) {
            if( room.doesContainOnlyParticipants(contacts))
                return room;
        }

        return null;
    }

    @Override
    public void associateConfToRoom(final Room room, final String confId, final IRoomProxy.IAssociateConfListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">associateConfToRoom");
        if(m_roomProxy == null) {
            Log.getLogger().warn(LOG_TAG, ">associateConfToRoom : no room proxy");
            if (listener != null)
                listener.onAssociateConfFailed();
            return;
        }

        if (room == null) {
            Log.getLogger().warn(LOG_TAG, "Room not available");
            return;
        }
        if (StringsUtil.isNullOrEmpty(confId)) {
            Log.getLogger().warn(LOG_TAG, "confId not available");
            return;
        }

        m_roomProxy.associateConfToRoom(room, confId, new IRoomProxy.IAssociateConfListener() {
            @Override
            public void onAssociateConfSuccess() {
                Log.getLogger().verbose(LOG_TAG, ">onAssociateConfSuccess");

                PgiConference pgiConf = m_pgiMgr.getConferenceFromId(confId);
                room.setPgiConference(pgiConf);
                if (listener != null)
                    listener.onAssociateConfSuccess();
            }

            @Override
            public void onAssociateConfFailed() {
                Log.getLogger().warn(LOG_TAG, ">onAssociateConfFailed");
                if (listener != null)
                    listener.onAssociateConfFailed();
            }
        });
    }

    @Override
    public void dissociateConfToRoom(final Room room, String confId, final IRoomProxy.IDissociateConfListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">dissociateConfToRoom");
        if(m_roomProxy == null) {
            Log.getLogger().warn(LOG_TAG, ">dissociateConfToRoom : no room proxy");
            if (listener != null)
                listener.onDissociateConfFailed();
            return;
        }

        if (room == null) {
            Log.getLogger().warn(LOG_TAG, "Room not available");
            return;
        }
        if (StringsUtil.isNullOrEmpty(confId)) {
            Log.getLogger().warn(LOG_TAG, "confId not available");
            return;
        }
        room.setPgiConference(null);
        m_roomProxy.dissociateConfToRoom(room, confId, new IRoomProxy.IDissociateConfListener() {
            @Override
            public void onDissociateConfSuccess() {
                Log.getLogger().verbose(LOG_TAG, ">onDissociateConfSuccess");

                if (listener != null)
                    listener.onDissociateConfSuccess();
            }

            @Override
            public void onDissociateConfFailed() {
                Log.getLogger().warn(LOG_TAG, ">onDissociateConfFailed");
                if (listener != null)
                    listener.onDissociateConfFailed();
            }
        });
    }

    @Override
    public void dissociatePgiConference(String confId) {
        for (Room room : getAllRooms().getCopyOfDataList()) {
            if (room.getPgiConference() != null && room.getPgiConference().getId().equals(confId)) {
                Log.getLogger().warn(LOG_TAG, "Dissociate Old Room" + room.getId());
                if (room.getPgiConference().isMyConference()) {
                    dissociateConfToRoom(room, confId, new IRoomProxy.IDissociateConfListener() {
                        @Override
                        public void onDissociateConfSuccess() {

                        }

                        @Override
                        public void onDissociateConfFailed() {

                        }
                    });
                } else {
                    //remove conf / room association locally
                    room.setInactiveConference(true);
                }

            }
        }
    }

 public void  dissociateOtherRoomFromConference(Room roomToAssociate) {

     if (roomToAssociate.getPgiConference() == null)
         return;

     String confId = roomToAssociate.getPgiConference().getId();
        for (Room room : getMyRoomList()) {

            if (!room.getId().equals(roomToAssociate.getId())) {
                if (room.getPgiConference() != null && room.getPgiConference().getId().equals(confId)) {
                    //Old association need to be destroyed

                    Log.getLogger().warn(LOG_TAG, "Dissociate Old Room" + room.getId());
                    if (room.getPgiConference() .isMyConference()) {
                        dissociateConfToRoom(room, confId, new IRoomProxy.IDissociateConfListener() {
                            @Override
                            public void onDissociateConfSuccess() {

                            }

                            @Override
                            public void onDissociateConfFailed() {

                            }
                        });
                    } else {
                        //remove conf / room association locally
                        room.setPgiConference(null);
                    }
                }

            }

        }
    }


    @Override
    public void refreshRoomConferenceInfo(){

        for (Room room: m_allRooms.getCopyOfDataList()) {
            checkPgiConfInRoom(room);
        }

    }
    public void removeObserver(XmppConnection connection)
    {
        if( connection != null && connection.getXmppContactMgr() != null) {
            connection.getXmppContactMgr().unregisterChangeListener(this);
        }
    }
}

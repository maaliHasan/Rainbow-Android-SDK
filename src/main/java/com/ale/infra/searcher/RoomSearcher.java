package com.ale.infra.searcher;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Group;
import com.ale.infra.manager.Conversation;
import com.ale.infra.manager.IGroupMgr;
import com.ale.infra.manager.room.IRoomMgr;
import com.ale.infra.manager.room.Room;
import com.ale.infra.manager.room.RoomStatus;
import com.ale.util.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by georges on 24/11/2016.
 */
public class RoomSearcher extends AbstractSearcher {

    private static final String LOG_TAG = "RoomSearcher";
    private IRoomMgr m_roomMgr;

    public RoomSearcher() {
        m_roomMgr = RainbowContext.getInfrastructure().getRoomMgr();
    }

    public List<IDisplayable> searchByName(String query)
    {
        Log.getLogger().verbose(LOG_TAG, ">searchByName: " + query);

        List<IDisplayable> roomsFound = new ArrayList<>();

        List<Room> roomList = m_roomMgr.getAllRooms().getCopyOfDataList();
        for(Room room : roomList) {
            if ( !(room.isRoomArchived() || RoomStatus.INVITED == room.getUserStatus()) ) {
                if (isMatchingQuery(room, query)) {
                    roomsFound.add(room);
                }
            }
        }

        return roomsFound;
    }

    public List<IDisplayable> searchByNameWithConversationFilter(String query, List<IDisplayable> conversationList)
    {
        Log.getLogger().verbose(LOG_TAG, ">searchByNameWithConversationFilter: " + query);

        List<IDisplayable> roomsFound = searchByName(query);

        for(IDisplayable displayable: conversationList) {
            if (displayable instanceof Conversation) {
                Conversation conversation = (Conversation) displayable;
                if (conversation.isRoomType() && roomsFound.contains(conversation.getRoom())) {
                    roomsFound.remove(conversation.getRoom());
                }
            }
        }

        return roomsFound;
    }

    public List<IDisplayable> searchByNameWithConversationFilterAndResultAsConversation(String query, List<IDisplayable> conversationList)
    {
        Log.getLogger().verbose(LOG_TAG, ">searchByNameWithConversationFilter: " + query);

        List<IDisplayable> roomAsConveration = new ArrayList<>();

        List<IDisplayable> roomsFound = searchByNameWithConversationFilter(query, conversationList);
        for(IDisplayable room: roomsFound) {
            roomAsConveration.add(new Conversation((Room) room));
        }

        return roomAsConveration;
    }
}

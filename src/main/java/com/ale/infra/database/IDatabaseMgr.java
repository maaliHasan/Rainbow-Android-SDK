package com.ale.infra.database;

/**
 * Created by wilsius on 24/11/16.
 */

public interface IDatabaseMgr {
     void shutdown();
     ConversationDataSource getConversationDataSource();
     ContactDataSource getContactDataSource();
     RoomDataSource getRoomDataSource();
     ChatDataSource getChatDataSource();
     void resetDatabase();
     void loadContacts();
     boolean isEmpty();
}

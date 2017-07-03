package com.ale.infra.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.manager.Conversation;
import com.ale.infra.manager.IMMessage;
import com.ale.infra.manager.room.Room;
import com.ale.infra.rainbow.api.ConversationType;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by wilsius on 24/11/16.
 */

public class ConversationDataSource {

    private static final String LOG_TAG = "ConversationDataSource";
    // Database fields
    private SQLiteDatabase database;
    private Object syncObject;
    private DatabaseMgr databaseMgr;


    public ConversationDataSource(SQLiteDatabase database, DatabaseMgr databaseMgr, Object syncObject) {
        this.syncObject = syncObject;
        this.database = database;
        this.databaseMgr =databaseMgr;
    }


    private Conversation getConversation(Cursor cursor) {
        if (cursor.getCount() == 0)
            return null;

        Conversation conversation = new Conversation();
        conversation.setId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CONVERSATION_ID)));
        String value = cursor.getString(cursor.getColumnIndex(DatabaseHelper.CONVERSATION_TYPE));
        if (value.equals("room"))
            conversation.setType(ConversationType.ROOM);
        else if (value.equals("bot"))
        conversation.setType(ConversationType.BOT);
        else
            conversation.setType(ConversationType.USER);
        conversation.setPeerId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CONVERSATION_PEER_ID)));
        conversation.setTopic(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CONVERSATION_TOPIC)));
        conversation.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CONVERSATION_NAME)));

        if (conversation.getType() == ConversationType.ROOM) {
            Room room = RainbowContext.getInfrastructure().getRoomMgr().getRoomById(conversation.getPeerId());
            conversation.setRoom(room);
        } else {
            Contact contact;
            contact = RainbowContext.getInfrastructure().getContactCacheMgr().getContactFromJid(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CONVERSATION_JID)));
            conversation.setContact(contact);
        }

        String lastMessageSender = cursor.getString(cursor.getColumnIndex(DatabaseHelper.CONVERSATION_LAST_MESSAGE_SENDERJID));
        if (!StringsUtil.isNullOrEmpty(lastMessageSender)) {
            IMMessage lastMessage = new IMMessage(lastMessageSender, cursor.getString(cursor.getColumnIndex(DatabaseHelper.CONVERSATION_LAST_MESSAGE_CONTENT)), false);
            lastMessage.setTimeStamp(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.CONVERSATION_LAST_MESSAGE_TIMESTAMP)));
            lastMessage.setMsgSent(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CONVERSATION_LAST_MESSAGE_SENT)) > 0);
            conversation.setLastMessage(lastMessage);
        }

        List<IMMessage> list = databaseMgr.getChatDataSource().getChats(conversation.getJid());

        conversation.getMessages().addAll(list);

        return conversation;
    }

    public void createOrUpdateConversation(Conversation conversation) {
        synchronized (syncObject) {
            if (!database.isOpen()) return;

            ContentValues values = new ContentValues();

            values.put(DatabaseHelper.CONVERSATION_ID,
                    StringsUtil.isNullOrEmpty(conversation.getId()) ? "" : conversation.getId());
            values.put(DatabaseHelper.CONVERSATION_JID,
                    StringsUtil.isNullOrEmpty(conversation.getJid()) ? "" : conversation.getJid());
            values.put(DatabaseHelper.CONVERSATION_PEER_ID,
                    StringsUtil.isNullOrEmpty(conversation.getPeerId()) ? "" : conversation.getPeerId());
            String val;
            if (conversation.getType() == ConversationType.ROOM)
                val = "room";
            else if (conversation.getType() == ConversationType.BOT)
                val = "bot";
            else
                val = "user";
            values.put(DatabaseHelper.CONVERSATION_TYPE,
                    val);
            values.put(DatabaseHelper.CONVERSATION_TOPIC,
                    StringsUtil.isNullOrEmpty(conversation.getTopic()) ? "" : conversation.getTopic());
            values.put(DatabaseHelper.CONVERSATION_NAME,
                    StringsUtil.isNullOrEmpty(conversation.getName()) ? "" : conversation.getName());

            if (conversation.getLastMessage() != null) {
                values.put(DatabaseHelper.CONVERSATION_LAST_MESSAGE_CONTENT, conversation.getLastMessage().getMessageContent());
                values.put(DatabaseHelper.CONVERSATION_LAST_MESSAGE_SENDERJID, conversation.getLastMessage().getContactJid());
                values.put(DatabaseHelper.CONVERSATION_LAST_MESSAGE_TIMESTAMP, conversation.getLastMessage().getTimeStamp());
                values.put(DatabaseHelper.CONVERSATION_LAST_MESSAGE_SENT, conversation.getLastMessage().isMsgSent());
            } else {
                values.put(DatabaseHelper.CONVERSATION_LAST_MESSAGE_CONTENT, "");
                values.put(DatabaseHelper.CONVERSATION_LAST_MESSAGE_SENDERJID, "");
                values.put(DatabaseHelper.CONVERSATION_LAST_MESSAGE_TIMESTAMP, 0L);
                values.put(DatabaseHelper.CONVERSATION_LAST_MESSAGE_SENT, false);
            }

            values.put(DatabaseHelper.LAST_UPDATED, new Date().getTime());

            try {

                Cursor cursor = getConversationIndex(conversation.getId(), conversation.getJid());
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                        String[] whereArgs = new String[]{
                                String.valueOf(id),
                        };
                        Log.getLogger().verbose(LOG_TAG, "createConversation conversation exists");
                        database.update(DatabaseHelper.TABLE_CONVERSATION, values,
                                DatabaseHelper.COLUMN_ID + " = ?", whereArgs);
                    } else {
                        database.insert(DatabaseHelper.TABLE_CONVERSATION, null, values);
                    }
                    values.clear();
                    cursor.close();
                }
            } catch (Exception e) {
                Log.getLogger().error(LOG_TAG, " createConversation exception " + e.toString());
            }
        }
    }


    private Cursor getConversationIndex(String id, String jid) {
        if( StringsUtil.isNullOrEmpty(id) && StringsUtil.isNullOrEmpty(jid)) {
            return null;
        }

        Cursor cursor = null;
        List<String> whereArgs = new ArrayList<>();
        StringBuilder selectionClause = new StringBuilder();
        String selectionId = "";
        String selectionJid = "";

        try {
            if (!database.isOpen())
                return null;

            if (!StringsUtil.isNullOrEmpty((id))) {
                selectionId = DatabaseHelper.CONVERSATION_ID + " = ?";
                whereArgs.add(id.toString());
            }

            if (!StringsUtil.isNullOrEmpty((jid))) {
                selectionJid = DatabaseHelper.CONVERSATION_JID + " = ?";
                whereArgs.add(jid.toString());
            }

            if (!StringsUtil.isNullOrEmpty((id)) && !StringsUtil.isNullOrEmpty((jid))) {
                selectionClause.append("( ");
                selectionClause.append(selectionId);
                selectionClause.append(" ) OR ( ");
                selectionClause.append(selectionJid);
                selectionClause.append(" )");
            } else {
                selectionClause.append(selectionId);
                selectionClause.append(selectionJid);
            }

            cursor = database.query(DatabaseHelper.TABLE_CONVERSATION, null,
                    selectionClause.toString(),
                    whereArgs.toArray(new String[whereArgs.size()]),
                    null, null, null);

            return cursor;
        } catch (Exception e) {
            Log.getLogger().error(LOG_TAG, "getConversationIndex exception " + e.toString());
            return null;
        }
    }

    public List<Conversation> getAllConversationFromDatabase() {
        List<Conversation> conversations = new ArrayList<Conversation>();
        synchronized (syncObject) {
            String selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_CONVERSATION;
            try {

                if (!database.isOpen())
                    return conversations;

                Cursor cursor = database.rawQuery(selectQuery, null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        Conversation conv;
                        do {
                            conv = getConversation(cursor);
                            if (conv != null) {
                                conversations.add(conv);
                            }
                        } while (cursor.moveToNext());
                    }
                    // make sure to disconnect the cursor
                    cursor.close();
                }
                Log.getLogger().info(LOG_TAG, " database contains :" + conversations.size());
                return conversations;
            } catch (Exception e) {
                Log.getLogger().error(LOG_TAG, " getAllConversationFromDatabase exception " + e.toString());
                return null;
            }
        }
    }

    public void deleteConversation(Conversation conversation) {
        synchronized (syncObject) {
            Cursor cursor = getConversationIndex(conversation.getId(), conversation.getJid());
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    try {
                        int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                        String[] whereArgs = new String[]{
                                String.valueOf(id),
                        };
                        database.delete(DatabaseHelper.TABLE_CONVERSATION,
                                DatabaseHelper.COLUMN_ID
                                        + " = ?", whereArgs);
                        databaseMgr.getChatDataSource().deleteChat(conversation);
                    } catch (Exception e) {
                        Log.getLogger().error(LOG_TAG, " deleteConversation exception " + e.toString());
                    }
                }
                cursor.close();
            }
        }
    }

    public void deleteMessages(Conversation conversation) {
        databaseMgr.getChatDataSource().deleteChat(conversation);
    }

    public void synchroDB(Conversation conversation) {
        databaseMgr.getChatDataSource().synchroDB( conversation);
    }
}

package com.ale.infra.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ale.infra.manager.Conversation;
import com.ale.infra.manager.IMMessage;
import com.ale.infra.xmpp.xep.calllog.CallLogPacketExtension;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by wilsius on 28/11/16.
 */

public class ChatDataSource {
    private static final String LOG_TAG = "ChatDataSource";
    // Database fields
    private SQLiteDatabase database;
    Object syncObject;


    public ChatDataSource(SQLiteDatabase database, Object syncObject) {
        this.database = database;
        this.syncObject = syncObject;
    }

    public void createOrUpdateChat(String ownerJid, IMMessage message) {
        synchronized (syncObject) {

            if (!database.isOpen()) return;

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.CHAT_OWNER_JID,
                    ownerJid);

            values.put(DatabaseHelper.CHAT_CONTACT_JID,
                    StringsUtil.isNullOrEmpty(message.getContactJid()) ? "" : message.getContactJid());
            values.put(DatabaseHelper.CHAT_CONTENT,
                    StringsUtil.isNullOrEmpty(message.getMessageContent()) ? "" : message.getMessageContent());
            values.put(DatabaseHelper.CHAT_SENT,
                    message.isMsgSent() ? "true" : "false");

            values.put(DatabaseHelper.CHAT_MESSAGE_ID,
                    StringsUtil.isNullOrEmpty(message.getMessageId()) ? "" : message.getMessageId());
            values.put(DatabaseHelper.CHAT_MAM_MESSAGE_ID,
                    StringsUtil.isNullOrEmpty(message.getMamMessageId()) ? "" : message.getMamMessageId());

            String deliveryState = "";
            if (message.getDeliveryState() == IMMessage.DeliveryState.SENT)
                deliveryState = "SENT";
            else if (message.getDeliveryState() == IMMessage.DeliveryState.SENT_SERVER_RECEIVED)
                deliveryState = "SENT_SERVER_RECEIVED";
            else if (message.getDeliveryState() == IMMessage.DeliveryState.SENT_CLIENT_RECEIVED)
                deliveryState = "SENT_CLIENT_RECEIVED";
            else if (message.getDeliveryState() == IMMessage.DeliveryState.SENT_CLIENT_READ)
                deliveryState = "SENT_CLIENT_READ";
            else if (message.getDeliveryState() == IMMessage.DeliveryState.RECEIVED)
                deliveryState = "RECEIVED";
            else if (message.getDeliveryState() == IMMessage.DeliveryState.READ)
                deliveryState = "READ";
            else
                deliveryState = "UNKNOWN";

            values.put(DatabaseHelper.CHAT_DELIVERY_STATE, deliveryState);
            values.put(DatabaseHelper.CHAT_TIMESTAMP, message.getTimeStamp());
            if (message.getMessageDateRead() != null) {
                values.put(DatabaseHelper.CHAT_TIMESTAMP_READ, message.getMessageDateRead().getTime());
            } else {
                values.put(DatabaseHelper.CHAT_TIMESTAMP_READ, 0L);
            }

            if (message.getCallLogEvent() != null) {
                values.put(DatabaseHelper.CHAT_CALL_LOG_TYPE, 1);
                CallLogPacketExtension callLogEvent = message.getCallLogEvent();
                values.put(DatabaseHelper.CHAT_CALL_LOG_CALLER, callLogEvent.getCallerJid());
                values.put(DatabaseHelper.CHAT_CALL_LOG_CALLEE, callLogEvent.getCalleeJid());
                values.put(DatabaseHelper.CHAT_CALL_LOG_STATE, callLogEvent.getState());
                values.put(DatabaseHelper.CHAT_CALL_LOG_DURATION, callLogEvent.getDuration());
            } else {
                values.put(DatabaseHelper.CHAT_CALL_LOG_TYPE, 0);
            }

            values.put(DatabaseHelper.LAST_UPDATED, new Date().getTime());

            try {
                Cursor cursor = getChatIndex(message.getMessageId());
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                        String[] whereArgs = new String[]{
                                String.valueOf(id),
                        };
                        Log.getLogger().verbose(LOG_TAG, "create Chat chat exists");
                        database.update(DatabaseHelper.TABLE_CHAT, values,
                                DatabaseHelper.COLUMN_ID + " = ?", whereArgs);
                    } else {
                        database.insert(DatabaseHelper.TABLE_CHAT, null, values);
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                Log.getLogger().error(LOG_TAG, " createChat exception " + e.toString());
            }
        }
    }

    private Cursor getChatIndex(String messageId) {
        Cursor cursor;
        String[] whereArgs = new String[]{
                messageId.toString()
        };
        try {
            if (!database.isOpen())
                return null;

            cursor = database.query(DatabaseHelper.TABLE_CHAT, null,
                    DatabaseHelper.CHAT_MESSAGE_ID + " = ?",
                    whereArgs,
                    null, null, null);
            return cursor;

        } catch (Exception e) {
            Log.getLogger().error(LOG_TAG, " getChatIndex exception " + e.toString());
            return null;
        }
    }

    private IMMessage getChat(Cursor cursor) {
        if (cursor.getCount() == 0) return null;

        IMMessage message = new IMMessage();
        message.setContactJid(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CHAT_CONTACT_JID)));
        message.setMamMessageId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CHAT_MAM_MESSAGE_ID)));
        message.setMessageContent(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CHAT_CONTENT)));
        message.setMessageId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CHAT_MESSAGE_ID)));
        message.setMsgSent(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CHAT_SENT)).equals("true") ? true : false);

        message.setFromMaM(false);
        long millisecond = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.CHAT_TIMESTAMP));
        Date date = new Date();
        date.setTime(millisecond);
        message.setTimeStamp(millisecond);

        millisecond = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.CHAT_TIMESTAMP_READ));
        if (millisecond == 0){
            message.setMessageDateRead(null);
        }else {
            date.setTime(millisecond);
            message.setMessageDateRead(date);
        }


        IMMessage.DeliveryState state;
        String delState = cursor.getString(cursor.getColumnIndex(DatabaseHelper.CHAT_DELIVERY_STATE));
        if ("SENT".equals(delState))
            state = IMMessage.DeliveryState.SENT;
        else if ("SENT_SERVER_RECEIVED".equals(delState))
            state = IMMessage.DeliveryState.SENT_SERVER_RECEIVED;
        else if ("SENT_CLIENT_RECEIVED".equals(delState))
            state = IMMessage.DeliveryState.SENT_CLIENT_RECEIVED;
        else if ("SENT_CLIENT_READ".equals(delState))
            state = IMMessage.DeliveryState.SENT_CLIENT_READ;
        else if ("RECEIVED".equals(delState))
            state = IMMessage.DeliveryState.RECEIVED;
        else if ("READ".equals(delState))
            state = IMMessage.DeliveryState.READ;
        else
            state = IMMessage.DeliveryState.UNKNOWN;

        message.setDeliveryState(state);

        boolean isCallLogType = (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CHAT_CALL_LOG_TYPE)) > 0);
        if( isCallLogType ) {
            CallLogPacketExtension callLogPacketExtension = new CallLogPacketExtension();
            callLogPacketExtension.setCallerJid(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CHAT_CALL_LOG_CALLER)));
            callLogPacketExtension.setCalleeJid(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CHAT_CALL_LOG_CALLEE)));
            callLogPacketExtension.setState(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CHAT_CALL_LOG_STATE)));
            callLogPacketExtension.setDuration(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CHAT_CALL_LOG_DURATION)));
            message.setCallLogEvent(callLogPacketExtension, null);
        }

        return message;
    }

    public void updateDeliveryState(String ownerJid, IMMessage message) {
        synchronized (syncObject) {
            if (message == null || message.getMessageId() == null) {
                Log.getLogger().error(LOG_TAG, " no message or message id - skip");
                return;
            }
            String[] whereArgs = new String[]{
                    message.getMessageId().toString(),
                    ownerJid.toString(),
            };
            if (!database.isOpen())
                return;

            ContentValues values = new ContentValues();

            String deliveryState = "";
            if (message.getDeliveryState() == IMMessage.DeliveryState.SENT)
                deliveryState = "SENT";
            else if (message.getDeliveryState() == IMMessage.DeliveryState.SENT_SERVER_RECEIVED)
                deliveryState = "SENT_SERVER_RECEIVED";
            else if (message.getDeliveryState() == IMMessage.DeliveryState.SENT_CLIENT_RECEIVED)
                deliveryState = "SENT_CLIENT_RECEIVED";
            else if (message.getDeliveryState() == IMMessage.DeliveryState.SENT_CLIENT_READ)
                deliveryState = "SENT_CLIENT_READ";
            else if (message.getDeliveryState() == IMMessage.DeliveryState.RECEIVED)
                deliveryState = "RECEIVED";
            else if (message.getDeliveryState() == IMMessage.DeliveryState.READ)
                deliveryState = "READ";
            else
                deliveryState = "UNKNOWN";
            values.put(DatabaseHelper.CHAT_DELIVERY_STATE,
                    deliveryState);
            try {
                database.update(DatabaseHelper.TABLE_CHAT, values,
                        DatabaseHelper.CHAT_MESSAGE_ID + " = ?" + " AND " + DatabaseHelper.CHAT_OWNER_JID + " = ?"
                        , whereArgs);
            } catch (Exception e) {
                Log.getLogger().error(LOG_TAG, " updateDeliveryState exception " + e.toString());
            }
        }
    }


    public List<IMMessage> getChats(String ownerJid) {
        List<IMMessage> messages = new ArrayList<IMMessage>();
        if (StringsUtil.isNullOrEmpty(ownerJid))
            return messages;

        String[] whereArgs = new String[]{
                ownerJid.toString(),
        };
        synchronized (syncObject) {
            try {
                if (!database.isOpen())
                    return messages;

                Cursor cursor = database.query(DatabaseHelper.TABLE_CHAT, null,
                        DatabaseHelper.CHAT_OWNER_JID + " = ?",
                        whereArgs,
                        null, null, null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        IMMessage message;
                        do {
                            message = getChat(cursor);
                            messages.add(message);
                        } while (cursor.moveToNext());
                    }
                    // make sure to disconnect the cursor
                    cursor.close();
                }
                Log.getLogger().info(LOG_TAG, "database for chat: " + ownerJid + "  contains :" + messages.size());

                if (messages.size() > 1) {
                    Collections.sort(messages, new Comparator<IMMessage>() {
                        @Override
                        public int compare(IMMessage msg1, IMMessage msg2) {
                            if (msg1.getTimeStamp() > msg2.getTimeStamp())
                                return 1;
                            else if (msg1.getTimeStamp() < msg2.getTimeStamp())
                                return -1;
                            else
                                return 0;

                        }
                    });
                }
                return messages;
            } catch (Exception e) {
                Log.getLogger().error(LOG_TAG, " getChats exception " + e.toString());
                return messages;
            }
        }
    }

    public void deleteChat(String ownerJid, IMMessage message) {
        synchronized (syncObject) {
            if (!database.isOpen())
                return;
            try {
                String[] whereArgs = new String[]{
                        message.getMessageId().toString(),
                        ownerJid.toString(),
                };
                database.delete(DatabaseHelper.TABLE_CHAT,
                        DatabaseHelper.CHAT_MESSAGE_ID + " = ?" + " AND " + DatabaseHelper.CHAT_OWNER_JID + " = ?", whereArgs);
            } catch (Exception e) {
                Log.getLogger().error(LOG_TAG, " deleteChat message exception " + e.toString());
            }
        }
    }

    public void deleteChat(String ownerJid) {

        synchronized (syncObject) {
            try {
                String[] whereArgs = new String[]{
                        ownerJid.toString(),
                };
                if (!database.isOpen())
                    return;
                database.delete(DatabaseHelper.TABLE_CHAT,
                        DatabaseHelper.CHAT_OWNER_JID
                                + " = ?", whereArgs);
            } catch (Exception e) {
                Log.getLogger().error(LOG_TAG, " deleteChat owner exception " + e.toString());
            }
        }
    }

    public void deleteChat(Conversation conversation) {
        deleteChat(conversation.getJid());
    }

    public void synchroDB (Conversation conversation) {
        IMMessage message;
        if ( !conversation.isFirstMamDone() ) {
            for (int i = conversation.getMessages().getCount() -1 ;  i >= 0; i--) {

                message = conversation.getMessages().get(i);
                if (!message.isFromMaM()) {
                    //Not from MaM remove
                    deleteChat(conversation.getJid(), message);
                    conversation.getMessages().delete(message);
                }
            }
        }
    }

}


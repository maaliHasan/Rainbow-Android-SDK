package com.ale.infra.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ale.infra.application.RainbowContext;
import com.ale.util.log.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.RunnableFuture;

/**
 * Created by wilsius on 24/11/16.
 */

public class DatabaseMgr implements  IDatabaseMgr{


    private DatabaseHelper databaseHelper;
    private SQLiteDatabase database;
    private ConversationDataSource conversationDataSource;
    private RoomDataSource roomDataSource;
    private ChatDataSource chatDataSource;
    private DateFormat df;
    private long openTimestamp = 0;
    private static  long DAY_15 = 15 * 1000 * 60 * 60 * 24;

    private ContactDataSource contactDataSource;
    private static final String LOG_TAG = "DatabaseMgr";

    private Object syncObject;

    public DatabaseMgr(Context context)  {

        this.syncObject = context;
        databaseHelper = new DatabaseHelper(context,syncObject);
        database = databaseHelper.getWritableDatabase();
        chatDataSource = new ChatDataSource(database,syncObject);
        conversationDataSource = new ConversationDataSource(database, this, syncObject);

        contactDataSource = new ContactDataSource(database,syncObject);
        roomDataSource = new RoomDataSource(database,syncObject);

         df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        openTimestamp = new Date().getTime();
    }
    @Override
    public void shutdown(){

        synchronized (syncObject) {
            deleteOldTableEntry(DatabaseHelper.TABLE_CHAT);
            deleteOldTableEntry(DatabaseHelper.TABLE_CONTACT);
            deleteOldTableEntry(DatabaseHelper.TABLE_CONVERSATION);
            deleteOldTableEntry(DatabaseHelper.TABLE_ROOM);
            deleteOldTableEntry(DatabaseHelper.TABLE_ROOM_CONTACT);

            database.close();
        }
        databaseHelper = null;
        chatDataSource = null;
        roomDataSource = null;
        conversationDataSource = null;
        contactDataSource = null;
        df = null;

    }
    @Override
    public ConversationDataSource getConversationDataSource() {
        return conversationDataSource;
    }
    @Override
    public ContactDataSource getContactDataSource() {
        return contactDataSource;
    }

    @Override
    public RoomDataSource getRoomDataSource() {
        return roomDataSource;
    }

    @Override
    public ChatDataSource getChatDataSource() {
        return chatDataSource;
    }

    @Override
    public synchronized void resetDatabase() {
        Log.getLogger().info(LOG_TAG,"Reset database");
            databaseHelper.clearTables(database);
    }
    @Override
    public void loadContacts() {
        if (contactDataSource != null) {
            contactDataSource.getAllContacts(true,true);
        }
    }

    @Override
    public boolean isEmpty() {
        if (contactDataSource != null)
            return contactDataSource.isEmpty();
        else
            return true;
    }

    public Date getDateFromStringStamp(String stamp) {
        String stampWithoutMs = stamp;
        if (stamp.indexOf('.') > 0)
            stampWithoutMs = stamp.substring(0, stamp.lastIndexOf('.'));
        TimeZone tz = TimeZone.getDefault();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        df.setTimeZone(tz);
        try {
            return df.parse(stampWithoutMs);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getStringStampFromDate(Date date) {
        DateFormat stampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        return stampFormat.format(date);
    }


    public void deleteOldTableEntry( String table) {
        // Delete all entry oldest than open date minus 15 days
        if (!database.isOpen())
            return;

        long oldest = openTimestamp - DAY_15 ;
        String[] selectionArgs = {String.valueOf(oldest)};
        String[] columns = {DatabaseHelper.COLUMN_ID};
        String selection = DatabaseHelper.LAST_UPDATED + "< ?";
        try {
            database.beginTransaction();
            Cursor cursor = database.query(table, columns, selection, selectionArgs, null, null, null);
            if (cursor != null) {
                Log.getLogger().info(LOG_TAG, "deleteOldTableEntry from table :" + table + " count: " + cursor.getCount());
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                        String[] whereArgs = new String[]{
                                String.valueOf(id)};

                        database.delete(table,
                                DatabaseHelper.COLUMN_ID + " = ?", whereArgs);
                    } while (cursor.moveToNext());

                    cursor.close();
                }
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.getLogger().error(LOG_TAG, " deleteOldContact exception " + e.toString());
        } finally {
            database.endTransaction();
        }
    }


}

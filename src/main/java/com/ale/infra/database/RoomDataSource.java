package com.ale.infra.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.manager.room.Room;
import com.ale.infra.manager.room.RoomParticipant;
import com.ale.infra.manager.room.RoomStatus;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.jivesoftware.smackx.muc.MUCRole;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by wilsius on 28/11/16.
 */

public class RoomDataSource {

    private static final String LOG_TAG = "RoomDataSource";
    // Database fields
    private SQLiteDatabase database;
    private Object syncObject;


    public RoomDataSource(SQLiteDatabase database, Object syncObj) {
        this.database = database;
        this.syncObject = syncObj;
    }

    public void createOrUpdateRoom(Room room) {
        if (StringsUtil.isNullOrEmpty(room.getJid())) return;

        synchronized (syncObject) {
            if (!database.isOpen()) return;

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.ROOM_TOPIC,
                    StringsUtil.isNullOrEmpty(room.getTopic()) ? "" : room.getTopic());
            values.put(DatabaseHelper.ROOM_NAME,
                    StringsUtil.isNullOrEmpty(room.getName()) ? "" : room.getName());
            values.put(DatabaseHelper.ROOM_JID,
                    StringsUtil.isNullOrEmpty(room.getJid()) ? "" : room.getJid());
            values.put(DatabaseHelper.ROOM_ID,
                    StringsUtil.isNullOrEmpty(room.getId()) ? "" : room.getId());
            values.put(DatabaseHelper.ROOM_CREATOR_ID,
                    StringsUtil.isNullOrEmpty(room.getCreatorId()) ? "" : room.getCreatorId());
            values.put(DatabaseHelper.LAST_UPDATED, new Date().getTime());

            deleteRoomContact(room);

            for (int i = 0; i < room.getParticipants().getCount(); i++) {
                createOrUpdateRoomContact(room, room.getParticipants().get(i));
            }
            try {
                Cursor cursor = getRoomIndex(room.getId());
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                        String[] whereArgs = new String[]{
                                String.valueOf(id),
                        };

                        Log.getLogger().verbose(LOG_TAG, "createRoom room exists");
                        database.update(DatabaseHelper.TABLE_ROOM, values,
                                DatabaseHelper.COLUMN_ID + " = ?", whereArgs);

                    } else {
                        database.insert(DatabaseHelper.TABLE_ROOM, null, values);
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                Log.getLogger().error(LOG_TAG, " createRoom exception " + e.toString());
            }
        }
    }

    private void createOrUpdateRoomContact(Room room, RoomParticipant participant) {

        if (!database.isOpen()) return;

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.ROOM_CONTACT_ID,
                participant.getId());
        values.put(DatabaseHelper.ROOM_CONTACT_STATUS,
                participant.getStatus().toString());
        values.put(DatabaseHelper.ROOM_CONTACT_ROOM_ID,
                room.getId());

        String role = "";
        if (participant.getRole() == MUCRole.participant)
            role = "user";
        else if (participant.getRole() == MUCRole.moderator)
            role = "moderator";
        else if (participant.getRole() == MUCRole.visitor)
            role = "guest";
        else
            role = "none";
        values.put(DatabaseHelper.ROOM_CONTACT_ROLE,
                role);

        try {
            Cursor cursor = getRoomContactIndex(room.getId(), participant.getId());
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                    String[] whereArgs = new String[]{
                            String.valueOf(id),
                    };
                    Log.getLogger().verbose(LOG_TAG, "createRoomContact room contact already exists");

                    database.update(DatabaseHelper.TABLE_ROOM_CONTACT, values,
                            DatabaseHelper.COLUMN_ID + " = ?", whereArgs);

                } else {
                    database.insert(DatabaseHelper.TABLE_ROOM_CONTACT, null, values);
                }
                values.clear();
                cursor.close();
            }
        } catch (Exception e) {
            Log.getLogger().error(LOG_TAG, " createRoomContact exception " + e.toString());
        }
    }

    private Cursor getRoomIndex(String roomId) {
        Cursor cursor;
        String[] whereArgs = new String[]{
                roomId.toString(),
        };
        try {
            if (!database.isOpen()) return null;

            cursor = database.query(DatabaseHelper.TABLE_ROOM, null,
                    DatabaseHelper.ROOM_ID + " = ?",
                    whereArgs,
                    null, null, null);
            return cursor;

        } catch (Exception e) {
            Log.getLogger().error(LOG_TAG, " getRoomIndex exception " + e.toString());
            return null;
        }
    }

    private Cursor getRoomContactIndex(String roomId, String contactId) {
        Cursor cursor;
        String[] whereArgs = new String[]{
                roomId.toString(),
                contactId.toString()

        };
        try {
            if (!database.isOpen())
                return null;
            cursor = database.query(DatabaseHelper.TABLE_ROOM_CONTACT, null,
                    DatabaseHelper.ROOM_CONTACT_ROOM_ID + " = ? AND " + DatabaseHelper.ROOM_CONTACT_ID + " = ?",
                    whereArgs,
                    null, null, null);

            return cursor;

        } catch (Exception e) {
            Log.getLogger().error(LOG_TAG, " getRoomContactIndex exception " + e.toString());
            return null;
        }
    }

    private List<RoomParticipant> getRoomParticipants(String roomId) {
        List<RoomParticipant> roomParticipants = new ArrayList<RoomParticipant>();
        String[] whereArgs = new String[]{
                roomId.toString(),
        };

        synchronized (syncObject) {

            try {
                if (!database.isOpen())
                    return roomParticipants;

                Cursor cursor = database.query(DatabaseHelper.TABLE_ROOM_CONTACT, null,
                        DatabaseHelper.ROOM_CONTACT_ROOM_ID + " = ?",
                        whereArgs,
                        null, null, null);

                cursor.moveToFirst();
                RoomParticipant participant;
                while (!cursor.isAfterLast()) {
                    participant = getRoomParticipant(cursor);
                    if (participant != null)
                        roomParticipants.add(participant);
                    cursor.moveToNext();
                }
                // make sure to disconnect the cursor
                cursor.close();
                return roomParticipants;
            } catch (Exception e) {
                Log.getLogger().error(LOG_TAG, " getRoomParticipants exception " + e.toString());
                return null;
            }
        }

    }

    private Room getRoom(Cursor cursor) {

        if (cursor.getCount() == 0) return null;

        Room room = new Room();
        room.setId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ROOM_ID)));
        room.setTopic(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ROOM_TOPIC)));
        room.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ROOM_NAME)));
        room.setJid(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ROOM_JID)));
        room.setCreatorId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ROOM_CREATOR_ID)));
        List<RoomParticipant> list = getRoomParticipants(room.getId());
        if (list!= null) {
            room.setParticipants(list);
            return room;
        } else {
            return null;
        }
    }

    private RoomParticipant getRoomParticipant(Cursor cursor) {
        if (cursor.getCount() == 0) return null;
        RoomParticipant p = new RoomParticipant();
        p.setId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ROOM_CONTACT_ID)));
        p.setRole(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ROOM_CONTACT_ROLE)));
        p.setStatus(RoomStatus.fromString(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ROOM_CONTACT_STATUS))));

        String id = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ROOM_CONTACT_ID));
        Contact contact;
        contact = RainbowContext.getInfrastructure().getContactCacheMgr().getContactFromCorporateId(id);

        if (contact == null) {
            Log.getLogger().error(LOG_TAG, " getRoomParticipant for contact is null for id " + id);
            return null;
        }
        p.setContact(contact);
        return p;

    }

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<Room>();

        String selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_ROOM;
        synchronized (syncObject) {
            try {

                if (!database.isOpen()) return rooms;

                Cursor cursor = database.rawQuery(selectQuery, null);

                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    Room room;
                    do {
                        room = getRoom(cursor);
                        if (room != null)
                            rooms.add(room);
                    } while (cursor.moveToNext());
                }

                // make sure to disconnect the cursor
                cursor.close();
                Log.getLogger().info(LOG_TAG, " database contains :" + rooms.size());
                return rooms;
            } catch (Exception e) {
                Log.getLogger().error(LOG_TAG, " getAllRooms exception " + e.toString());
                return null;
            }
        }
    }

    private void deleteRoomContact(Room room) {
        try {
            for (RoomParticipant participant : room.getParticipants().getCopyOfDataList()) {

                Cursor cursor = getRoomContactIndex(room.getId(), participant.getId());
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                        String[] whereArgs = new String[]{
                                String.valueOf(id),
                        };

                        database.delete(DatabaseHelper.TABLE_ROOM_CONTACT,
                                DatabaseHelper.COLUMN_ID + " = ?", whereArgs);
                    }
                    cursor.close();
                }
            }

        } catch (Exception e) {
            Log.getLogger().error(LOG_TAG, " deleteRoomContact exception " + e.toString());
        }
    }
    public void deleteRoom(Room room) {
        synchronized (syncObject) {
            if (!database.isOpen()) return;

            try {
                Cursor cursor = getRoomIndex(room.getId());
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                        String[] whereArgs = new String[]{
                                String.valueOf(id),
                        };
                        Log.getLogger().verbose(LOG_TAG, " deleting room ID is: " + room.getId());


                        database.delete(DatabaseHelper.TABLE_ROOM, DatabaseHelper.COLUMN_ID
                                + " = ?", whereArgs);

                        deleteRoomContact(room);
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                Log.getLogger().error(LOG_TAG, " deleteRoom exception " + e.toString());
            }
        }
    }
}

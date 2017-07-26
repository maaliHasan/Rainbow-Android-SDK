package com.ale.infra.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ale.infra.contact.IContact;
import com.ale.util.log.Log;

/**
 * Created by wilsius on 24/11/16.
 */


public class DatabaseHelper extends SQLiteOpenHelper {

    private Object syncObject;

    private static final String LOG_TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "rainbow.db";


    public static final String TABLE_CONVERSATION = "conversation";
    public static final String COLUMN_ID = "_id";
    public static final String LAST_UPDATED = "lastupdated";


    public static final String CONVERSATION_PEER_ID = "peer_id";
    public static final String CONVERSATION_TYPE = "type";
    public static final String CONVERSATION_TOPIC = "topic";
    public static final String CONVERSATION_NAME = "name";
    public static final String CONVERSATION_JID = "jid";
    public static final String CONVERSATION_ID = "id";
    public static final String CONVERSATION_LAST_MESSAGE_CONTENT = "last_message_content";
    public static final String CONVERSATION_LAST_MESSAGE_TIMESTAMP = "last_message_timestamp";
    public static final String CONVERSATION_LAST_MESSAGE_SENDERJID  = "last_message_sender_jid";
    public static final String CONVERSATION_LAST_MESSAGE_SENT = "last_message_isSent";

    private static final String CONVERSATION_CREATE = "create table "
            + TABLE_CONVERSATION + "( " +
            COLUMN_ID + " integer primary key autoincrement," +
            LAST_UPDATED + " long," +
            CONVERSATION_ID + " text," +
            CONVERSATION_JID + " text," +
            CONVERSATION_NAME + " text," +
            CONVERSATION_TOPIC + " text," +
            CONVERSATION_TYPE + " text," +
            CONVERSATION_LAST_MESSAGE_CONTENT + " text," +
            CONVERSATION_LAST_MESSAGE_SENDERJID + " text," +
            CONVERSATION_LAST_MESSAGE_TIMESTAMP + " long," +
            CONVERSATION_LAST_MESSAGE_SENT + " boolean," +
            CONVERSATION_PEER_ID + " text );";


    public static final String TABLE_CONTACT = "contact_table";
    public static final String DIRCONTACT_IS_ROSTER = "roster";
    public static final String DIRCONTACT_JID = "jid";
    public static final String DIRCONTACT_COUNTRY = "country";
    public static final String DIRCONTACT_LANGUAGE = "language";
    public static final String DIRCONTACT_LOGIN_EMAIL = "loginEmail";
    public static final String DIRCONTACT_JIDTEL = "jidtel";
    public static final String DIRCONTACT_TYPE = "type";
    public static final String DIRCONTACT_ROLE = "role";
    public static final String DIRCONTACT_ROLE_USER = IContact.ContactRole.USER.toString();
    public static final String DIRCONTACT_ROLE_ADMIN = IContact.ContactRole.ADMIN.toString();
    public static final String DIRCONTACT_ROLE_GUEST = IContact.ContactRole.GUEST.toString();
    public static final String DIRCONTACT_FIRSTNAME = "firstname";
    public static final String DIRCONTACT_LASTNAME = "lastname";
    public static final String DIRCONTACT_COMPANYNAME = "companyname";
    public static final String DIRCONTACT_JOBTITILE = "jobtitle";
    public static final String DIRCONTACT_TITILE = "title";
    public static final String DIRCONTACT_NICKNAME = "nickname";
    public static final String DIRCONTACT_PERSONAL_PHONE_NUMBER = "personalphonenumber";
    public static final String DIRCONTACT_MOBILE_PHONE_NUMBER = "personalnhonenumber";
    public static final String DIRCONTACT_OFFICE_PHONE_NUMBER = "workphonenumber";
    public static final String DIRCONTACT_PERSONAL_MOBILE_PHONE_NUMBER = "personalmobilephonenumber";
    public static final String DIRCONTACT_EMAILADDRESS = "emailaddress";
    public static final String DIRCONTACT_POSTALADDRESS = "postaladdresses";
    public static final String DIRCONTACT_CORPORATEID = "corporateid";
    public static final String DIRCONTACT_COMPANYID = "companyid";
    public static final String DIRCONTACT_LASTAVATARUPDATE = "lastavatarupdate";



    private static final String CONTACT_CREATE = "create table "
            + TABLE_CONTACT + "( " +
            COLUMN_ID + " integer primary key autoincrement," +
            LAST_UPDATED + " long," +
            DIRCONTACT_IS_ROSTER + " integer," +
            DIRCONTACT_JID + " text," +
            DIRCONTACT_COUNTRY + " text," +
            DIRCONTACT_LANGUAGE + " text," +
            DIRCONTACT_LOGIN_EMAIL + " text," +
            DIRCONTACT_JIDTEL + " text," +
            DIRCONTACT_TYPE + " text," +
            DIRCONTACT_FIRSTNAME + " text," +
            DIRCONTACT_LASTNAME + " text," +
            DIRCONTACT_COMPANYNAME + " text," +
            DIRCONTACT_JOBTITILE + " text," +
            DIRCONTACT_TITILE + " text," +
            DIRCONTACT_NICKNAME + " text," +
            DIRCONTACT_PERSONAL_PHONE_NUMBER + " text," +
            DIRCONTACT_MOBILE_PHONE_NUMBER + " text," +
            DIRCONTACT_OFFICE_PHONE_NUMBER + " text," +
            DIRCONTACT_PERSONAL_MOBILE_PHONE_NUMBER + " text," +
            DIRCONTACT_EMAILADDRESS + " text," +
            DIRCONTACT_POSTALADDRESS + " text," +
            DIRCONTACT_CORPORATEID + " text," +
            DIRCONTACT_COMPANYID + " text," +
            DIRCONTACT_LASTAVATARUPDATE + " text," +
            DIRCONTACT_ROLE + " text );";


    public static final String TABLE_ROOM = "room_table";
    public static final String ROOM_TOPIC = "topic";
    public static final String ROOM_NAME = "name";
    public static final String ROOM_JID = "jid";
    public static final String ROOM_ID = "id";
    public static final String ROOM_CREATOR_ID = "creator_id";
    public static final String ROOM_SCHEDULED_CONF = "room_scheduled_conf";
    public static final String ROOM_SCHEDULED_START_DATE = "room_scheduled_start_date";

    private static final String ROOM_CREATE = "create table "
            + TABLE_ROOM + "( " +
            COLUMN_ID + " integer primary key autoincrement," +
            LAST_UPDATED + " long," +
            ROOM_TOPIC + " text," +
            ROOM_NAME + " text," +
            ROOM_JID + " text," +
            ROOM_ID + " text," +
            ROOM_SCHEDULED_CONF + " int," +
            ROOM_SCHEDULED_START_DATE + " long," +
            ROOM_CREATOR_ID + " text );";


    public static final String TABLE_ROOM_CONTACT = "room_contact_table";
    public static final String ROOM_CONTACT_ID =  "contact_jid";
    public static final String ROOM_CONTACT_STATUS =  "contact_status";
    public static final String ROOM_CONTACT_ROLE = "contact_role";
    public static final String ROOM_CONTACT_ROOM_ID = "room_jid";


    private static final String ROOM_CONTACT_CREATE = "create table "
            + TABLE_ROOM_CONTACT + "( " +
            COLUMN_ID + " integer primary key autoincrement," +
            LAST_UPDATED + " long," +
            ROOM_CONTACT_ID + " text," +
            ROOM_CONTACT_STATUS + " text," +
            ROOM_CONTACT_ROLE + " text," +
            ROOM_CONTACT_ROOM_ID + " text );";

    public static final String TABLE_CHAT = "chat_table";
    public static final String CHAT_OWNER_JID = "owner_jid";
    public static final String CHAT_CONTENT = "content";
    public static final String CHAT_SENT = "sent";
    public static final String CHAT_CONTACT_JID = "contact_jid";
    public static final String CHAT_MESSAGE_ID = "messageID";
    public static final String CHAT_MAM_MESSAGE_ID = "mam_message_id";
    public static final String CHAT_DELIVERY_STATE = "delivery_state";
    public static final String CHAT_TIMESTAMP = "timestamp";
    public static final String CHAT_TIMESTAMP_READ = "timestamp_read";
    public static final String CHAT_CALL_LOG_TYPE = "call_log_type";
    public static final String CHAT_CALL_LOG_CALLER = "call_log_caller";
    public static final String CHAT_CALL_LOG_CALLEE = "call_log_callee";
    public static final String CHAT_CALL_LOG_STATE = "call_log_state";
    public static final String CHAT_CALL_LOG_DURATION = "call_log_duration";


    private static final String CHAT_CREATE = "create table "
            + TABLE_CHAT + "( " +
            COLUMN_ID + " integer primary key autoincrement," +
            LAST_UPDATED + " long," +
            CHAT_OWNER_JID + " text," +
            CHAT_CONTENT + " text," +
            CHAT_SENT + " text," +
            CHAT_CONTACT_JID + " text," +
            CHAT_MESSAGE_ID + " text," +
            CHAT_MAM_MESSAGE_ID + " text," +
            CHAT_TIMESTAMP + " long," +
            CHAT_TIMESTAMP_READ + " long," +
            CHAT_CALL_LOG_TYPE + " boolean," +
            CHAT_CALL_LOG_CALLER + " text," +
            CHAT_CALL_LOG_CALLEE + " text," +
            CHAT_CALL_LOG_STATE + " text," +
            CHAT_CALL_LOG_DURATION + " text," +
            CHAT_DELIVERY_STATE + " text );";


    private static final int DATABASE_VERSION = 31;

    public DatabaseHelper(Context context, Object syncObj) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.syncObject = syncObj;
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        Log.getLogger().info(LOG_TAG, "onCreate database");
        sqLiteDatabase.execSQL(CONTACT_CREATE);
        sqLiteDatabase.execSQL(CONVERSATION_CREATE);
        sqLiteDatabase.execSQL(ROOM_CREATE);
        sqLiteDatabase.execSQL(ROOM_CONTACT_CREATE);
        sqLiteDatabase.execSQL(CHAT_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        Log.getLogger().info(LOG_TAG, "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        synchronized (syncObject) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CONVERSATION);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOM);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOM_CONTACT);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT);
            onCreate(sqLiteDatabase);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.getLogger().info(LOG_TAG, "Downgrade database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        synchronized (syncObject) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CONVERSATION);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOM);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOM_CONTACT);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT);
            onCreate(sqLiteDatabase);
        }
    }


    public void clearTables(SQLiteDatabase sqLiteDatabase) {
        synchronized (syncObject) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CONVERSATION);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOM);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOM_CONTACT);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT);
            onCreate(sqLiteDatabase);
        }
    }
}

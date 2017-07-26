package com.ale.infra.googlepush;

import android.content.Context;
import android.os.PowerManager;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.manager.ChatMgr;
import com.ale.infra.manager.Conversation;
import com.ale.infra.manager.IIMNotificationMgr;
import com.ale.infra.manager.IMMessage;
import com.ale.infra.manager.INotificationFactory;
import com.ale.infra.manager.MultiUserChatMgr;
import com.ale.infra.manager.room.Room;
import com.ale.infra.xmpp.XmppConnection;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.List;
import java.util.Map;


public class GcmPushService extends FirebaseMessagingService
{

    private static final String LOG_TAG = "GcmPushService";
    private static final String LOCK_NAME_STATIC = "com.ale.rainbow.GcmPushService";
    private String m_senderJid;
    private String m_msgContent;
    private String m_senderName;
    private PowerManager.WakeLock lock = null;
    private boolean inDebug = false;
    private boolean m_isRoomType = false;
    private String m_msgId;
    private String m_roomJid;
    private String m_roomName;
    private String m_dateString;


    public GcmPushService()
    {
    }

    @Override
    public void onCreate()
    {
        Log.getLogger().info(LOG_TAG, "onCreate");
        PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
        lock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
        lock.setReferenceCounted(true);
        inDebug = RainbowContext.getInfrastructure().isInDebugMode(this.getApplicationContext());
        super.onCreate();
    }

    @Override
    public void onDestroy()
    {
        Log.getLogger().verbose(LOG_TAG, ">onDestroy");
        if (lock.isHeld())
            lock.release();
        super.onDestroy();
    }

    @Override
    public void onMessageReceived(RemoteMessage message)
    {

        synchronized (this) {
            Map<String, String> data = message.getData();

            lock.acquire();
            Log.getLogger().info(LOG_TAG, "Received GCM Message: ");





            // Message for Chat :
            // Received GCM Message: Bundle[{message-id=web_0613e78c-284c-40c8-be51-004d8a94827d13, google.sent_time=1485512257723,
            // last-message-body=test, first-last-name=Geo67 Fra, stamp=2017-01-27T10:17:37.604059Z,
            // google.message_id=0:1485512257734122%7bdae875f9fd7ecd, message-count=1,
            // last-message-sender=3141fe3b4e3f494eb72205802dba70b5@demo-all-in-one-dev-1.opentouch.cloud, collapse_key=do_not_collapse}]

            // Message for Room :
            // Received GCM Message: Bundle[{message-id=web_689b2c5c-6cb9-4b06-948f-b43917e79f6f5, google.sent_time=1485161165936,
            // last-message-body=my room test, first-last-name=, google.message_id=0:1485161165943611%7bdae875f9fd7ecd, message-count=2,
            // last-message-sender=room_1a81575fbedf43b4954aa9c0d00060cd@muc.openrainbow.net, collapse_key=do_not_collapse}]


            //        {"to":"dYLKW_gV224444444U:APA91bvHGwZ8c3w33mtL0ZPUrzNgx1
            //            Jii4DaVfSiPZxUeQdje5p80qfJ3-QD6MGGSiresDQFXdZY9Mnf2BojeSy5_QN0-3MgrK8tnvVsWhDhUjoH4kZXO0SSKh8QB51pIPJXM4Zrs_ngj","time_to_live":86400,"data":{
            //            "first-last-name":"Leontev Andrei","last-message-body":"hello mucc2","last-message-sender":"1001@app.pingme.sqanet.fr","last-message-type":"gr
            //            oupchat","message-count":3,"message-id":"8P5McnrRS8lDX6Fk1021","room-jid":"ale@conference.app.pingme.sqanet.fr","room-name":"Room","stamp":"20
            //            17-02-14T15:52:22.386370Z"}}


            try {
                String senderJid = data.get("last-message-sender");
                if (StringsUtil.isNullOrEmpty(senderJid)) {
                    Log.getLogger().warn(LOG_TAG, "Wrong GCM Message" );
                    return;
                }

                String messageType = data.get("last-message-type");

                if (!StringsUtil.isNullOrEmpty(messageType) && "call".equalsIgnoreCase(messageType)) {
                    XmppConnection connection = RainbowContext.getInfrastructure().getXmppConnection();

                    if (connection == null) {
                        Log.getLogger().warn(LOG_TAG, "Call message ignore, application is not started");
                        return;
                    }

                    String callAction = data.get("call-action");
                    String callId = data.get("call-id");
                    String medias = data.get("call-media");
                    String callResource = data.get("call-resource");
                    connection.getTelephonyMgr().handlePushMessage(senderJid, callResource, callId, callAction, medias);
                } else {
                    m_msgId = data.get("message-id");
                    m_msgContent = data.get("last-message-body");
                    m_senderName = data.get("first-last-name");

                    m_dateString = data.get("stamp");
                    m_senderJid = data.get("last-message-sender");

                    m_isRoomType = false;

                    if (!StringsUtil.isNullOrEmpty(messageType) && "groupchat".equalsIgnoreCase(messageType)) {
                        m_isRoomType = true;
                        m_roomJid = data.get("room-jid");
                        m_roomName = data.get("room-name");

                        if (StringsUtil.isNullOrEmpty(m_roomJid)) {
                            Log.getLogger().warn(LOG_TAG, "Wrong GCM Message: No room Jid");
                            return;
                        }
                    }

                    if (StringsUtil.isNullOrEmpty(m_senderJid)) {
                        Log.getLogger().warn(LOG_TAG, "No SenderId given in GCM Message");
                        return;
                    }

                    IContactCacheMgr contactCacheMgr = RainbowContext.getInfrastructure().getContactCacheMgr();
                    if (contactCacheMgr != null) {
                        Contact contact = contactCacheMgr.getContactFromJid(m_senderJid);
                        if (contact != null && contactCacheMgr.isLoggedInUser(contact)) {
                            Log.getLogger().verbose(LOG_TAG, "[GcmPushService:onMessageReceived] last-message-sender is me - skip");
                            return;
                        }
                    }

                    if (RainbowContext.getApplicationState() == RainbowContext.ApplicationState.STOPPED) {
                        Log.getLogger().verbose(LOG_TAG, "Application is Stopped");
                        RainbowContext.getInfrastructure().runMinimalInfrastructure(getApplicationContext());
                        prepareImNotificationWithAppStopped();
                    } else {
                        Log.getLogger().verbose(LOG_TAG, "Application is still running");
                        prepareImNotification();
                    }
                }
            } finally {
                lock.release();
            }
        }
    }

    private void prepareImNotificationWithAppStopped()
    {
        Log.getLogger().verbose(LOG_TAG, ">prepareImNotificationWithAppStopped");

        INotificationFactory notifFactory = RainbowContext.getPlatformServices().getNotificationFactory();
        IIMNotificationMgr imNotifMgr = notifFactory.getIMNotificationMgr();
        if (imNotifMgr != null)
        {
            Log.getLogger().verbose(LOG_TAG, "imNotifMgr is available");

            IMMessage imMsg = prepareMessage(true);

            Contact contact = null;

            List<DirectoryContact> contacts = RainbowContext.getInfrastructure().getDatabaseMgr().getContactDataSource().getAllContacts(false, false);
            for (DirectoryContact dir : contacts)
            {
                if (dir.getImJabberId().equalsIgnoreCase(m_senderJid))
                {
                    contact = RainbowContext.getInfrastructure().getContactCacheMgr().createContactIfNotExistOrUpdate(dir);
                    RainbowContext.getInfrastructure().getContactCacheMgr().downloadContactAvatar(contact, false);
                    break;
                }
            }

            if (contact == null)
            {
                Log.getLogger().info(LOG_TAG, ">contact not found in DB" + m_senderJid);
                DirectoryContact receivedDirContact = new DirectoryContact();
                receivedDirContact.setImJabberId(m_senderJid);
                receivedDirContact.setFirstName(m_senderName);
                contact = new Contact(receivedDirContact, null);
            }

            if (!m_isRoomType)
            {
                imNotifMgr.addImNotifWithAppStopped(contact, imMsg);
            }
            else
            {
                List<Room> rooms = RainbowContext.getInfrastructure().getDatabaseMgr().getRoomDataSource().getAllRooms();
                for (Room room : rooms)
                {
                    if (room.getJid().equalsIgnoreCase(m_roomJid))
                    {
                        Log.getLogger().info(LOG_TAG, ">room found in DB" + room.getName());
                        imNotifMgr.addImNotifWithAppStopped(room, contact, imMsg);
                        return;
                    }
                }
                // no room found create one
                Room room = new Room();
                room.setName(m_roomName);
                room.setJid(m_roomJid);
                Log.getLogger().info(LOG_TAG, "> no room found in DB" + room.getName());
                imNotifMgr.addImNotifWithAppStopped(room, contact, imMsg);
            }
        }
        else
        {
            Log.getLogger().warn(LOG_TAG, "imNotifMgr is NOT available");
        }
    }

    private IMMessage prepareMessage(boolean isAppStopped)
    {
        // Prepare IM Message :
        IMMessage imMsg = new IMMessage();
        imMsg.setDeliveryState(IMMessage.DeliveryState.RECEIVED);

        imMsg.setContactJid(m_senderJid);

        if (inDebug)
        {
            if (isAppStopped)
                m_msgContent = m_msgContent + " (Pushed App Stopped)";
            else
                m_msgContent = m_msgContent + " (Pushed)";
        }
        imMsg.setMessageId(m_msgId);
        imMsg.setMessageContent(m_msgContent);

        if (!StringsUtil.isNullOrEmpty(m_dateString))
        {
            imMsg.setTimeStampFromDateString(m_dateString);
        }
        else
        {
            Date date = new Date();
            imMsg.setTimeStamp(date.getTime());
        }
        return imMsg;
    }

    private void prepareImNotification()
    {

        // New Message ;
        // Received GCM Message: Bundle[{google.sent_time=1478074321312, last-message-body=test, first-last-name=Georges2 Francisco2,
        // google.message_id=0:1478074321343947%7bdae875f9fd7ecd, message-count=1,
        // last-message-sender=3e4b17f4575d47de843028657856492d@jerome-all-in-one-dev-1.opentouch.cloud/web_win_1.16.1_uGVFYAqb, collapse_key=do_not_collapse}]


        // Prepare IM Message :
        IMMessage imMsg = prepareMessage(false);

        ChatMgr chatMgr = RainbowContext.getInfrastructure().getChatMgr();

        if (chatMgr == null)
            return;

        Conversation conv = null;

        if (!m_isRoomType)
        {
            conv = chatMgr.storeMessage(m_senderJid, imMsg);
        }
        else
        {
            MultiUserChatMgr multiChatMgr = RainbowContext.getInfrastructure().getMultiUserChatMgr();
            if (multiChatMgr != null)
            {
                conv = chatMgr.getConversationFromJid(m_roomJid);
                if (conv != null)
                {
                    multiChatMgr.storeMessage(conv, imMsg);
                }
                else
                {
                    Log.getLogger().warn(LOG_TAG, "No room conversation for push message");
                }
            }
        }
        if (conv != null)
        {
            chatMgr.notifyImReceived(conv, imMsg);
        }
    }

    @Override
    public void onDeletedMessages()
    {
        Log.getLogger().verbose(LOG_TAG, "Deleted messages on server");
    }

    @Override
    public void onMessageSent(String msgId)
    {
        Log.getLogger().verbose(LOG_TAG, "Upstream message sent. Id=" + msgId);
    }
}

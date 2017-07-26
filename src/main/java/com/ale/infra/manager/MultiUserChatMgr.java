package com.ale.infra.manager;

import android.content.Context;
import android.support.v4.util.Pair;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.list.IItemListChangeListener;
import com.ale.infra.manager.fileserver.RainbowFileDescriptor;
import com.ale.infra.manager.room.IRoomMgr;
import com.ale.infra.manager.room.Room;
import com.ale.infra.proxy.conversation.IConversationProxy;
import com.ale.infra.proxy.room.IRoomProxy;
import com.ale.infra.xmpp.AbstractRainbowXMPPConnection;
import com.ale.infra.xmpp.XmppUtils;
import com.ale.infra.xmpp.xep.DeliveryReceipt.RainbowDeliveryReceivedReceipt;
import com.ale.infra.xmpp.xep.DeliveryReceipt.RainbowDeliveryTimestampReceipt;
import com.ale.infra.xmpp.xep.IMamNotification;
import com.ale.infra.xmpp.xep.MUC.RainbowGroupChatInvitation;
import com.ale.infra.xmpp.xep.Room.RoomConferenceEvent;
import com.ale.infra.xmpp.xep.Room.RoomMultiUserChatEvent;
import com.ale.infra.xmpp.xep.archived.RainbowArchived;
import com.ale.infra.xmpp.xep.message.StoreMessagePacketExtension;
import com.ale.infra.xmpp.xep.outofband.RainbowOutOfBandData;
import com.ale.util.DateTimeUtil;
import com.ale.util.Duration;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.NotFilter;
import org.jivesoftware.smack.filter.StanzaExtensionFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.packet.GroupChatInvitation;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wilsius on 26/07/16.
 */
public class MultiUserChatMgr implements IChatMgr, Conversation.ConversationListener, IItemListChangeListener
{

    final static String LOG_TAG = "MultiUserChatMgr";
    private static final StanzaFilter INVITATION_FILTER = new AndFilter(StanzaTypeFilter.MESSAGE, new StanzaExtensionFilter(new GroupChatInvitation("")), new NotFilter(MessageTypeFilter.ERROR));
    private String CHAT_STATE_NAMESPACE = "http://jabber.org/protocol/chatstates";
    private AbstractRainbowXMPPConnection m_connection;
    private IContactCacheMgr m_contactCache;
    private MultiUserChatManager m_mucMgr;
    private ChatMgr m_chatMgr;
    private Map<Conversation, MultiUserChat> m_poolMucChat = new HashMap<>();
    private Set<MultiUserChat> m_chats = new HashSet<>();
    private IRoomMgr m_roomMgr;
    private final StanzaListener m_invitationPacketListener = new StanzaListener()
    {
        @Override
        public void processPacket(Stanza packet)
        {
            final Message message = (Message) packet;
            Log.getLogger().info(LOG_TAG, "Invitation received MUC :");

            final RainbowGroupChatInvitation chatInvitation = RainbowGroupChatInvitation.from(message);
            Log.getLogger().info(LOG_TAG, "MUC Invitation received chat invite=" + chatInvitation.toString());

            if (m_roomMgr != null)
            {
                Room newRoom = new Room();
                newRoom.setId(chatInvitation.getThread());
                m_roomMgr.getRoomData(newRoom, new IRoomProxy.IGetRoomDataListener()
                {
                    @Override
                    public void onGetRoomDataSuccess(Room room)
                    {
                        m_roomMgr.displayPendingRoomNotification(room);
                    }

                    @Override
                    public void onGetRoomDataFailure(String roomId)
                    {
                    }
                });
            }
        }
    };
    private String m_language;
    private Timer m_timerJoin = null;
    private MessageListener m_messageListener = new MessageListener()
    {
        @Override
        public void processMessage(Message message)
        {
            Log.getLogger().info(LOG_TAG, ">processMessage:" + message);
            RainbowDeliveryReceivedReceipt rainbowDeliveryReceivedReceipt = message.getExtension(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE);
            RainbowDeliveryTimestampReceipt rainbowDeliveryTimestamp = message.getExtension(RainbowDeliveryTimestampReceipt.ELEMENT, RainbowDeliveryTimestampReceipt.NAMESPACE);
            ExtensionElement chatStateExtension = message.getExtension(CHAT_STATE_NAMESPACE);

            if (rainbowDeliveryReceivedReceipt != null)
            {
                if (message.getFrom().contains(RainbowContext.getPlatformServices().getApplicationData().getUserJidIm()))
                {
                    Log.getLogger().verbose(LOG_TAG, "RainbowDeliveryReceivedReceipt Extension detected ; event=" + rainbowDeliveryReceivedReceipt.getEvent() + " entity=" + rainbowDeliveryReceivedReceipt.getEntity());
                    // <message xmlns='jabber:client' from='room_3711df9136d144109eac1a63d195a10c@muc.demo-all-in-one-dev-1.opentouch.cloud/501b066c10c943a3b8d2db5526d9e972@demo-all-in-one-dev-1.opentouch.cloud/web_win_1.16.0_sDk9jflT'
                    // to='0a82c95d3f454572b78b5490651a481e@demo-all-in-one-dev-1.opentouch.cloud/mobile_android_356571062442546' type='groupchat'>
                    // <timestamp xmlns='urn:xmpp:receipts' value='2016-10-25T09:18:19.709016Z'/>
                    // <received xmlns='urn:xmpp:receipts' event='read' entity='client' type='muc' id='web_31b46dfa-4d11-41be-810f-ef42a344508667'/></message>

                    Pair<Conversation, IMMessage> result = m_chatMgr.findMessageFromMsgId(rainbowDeliveryReceivedReceipt.getId());
                    if (result != null)
                    {
                        m_chatMgr.setNewImMessageState(rainbowDeliveryReceivedReceipt, rainbowDeliveryTimestamp, message);
                    }
                }
                else
                {
                    Log.getLogger().verbose(LOG_TAG, "RainbowDeliveryReceivedReceipt not concerning us");
                }
            }
            if (chatStateExtension != null)
            {
                parseChatStateExtension(message, chatStateExtension);
            }

            parseRoomMessage(message);
        }

    };
    private Room.RoomListener m_roomListener = new Room.RoomListener()
    {
        @Override
        public synchronized void roomUpdated(Room updatedRoom)
        {
            Log.getLogger().debug(LOG_TAG, ">roomUpdated(m_roomListener)");
            if (updatedRoom.isUserActive())
            {
                joinRoomIfNeeded(updatedRoom);
            }
            else
            {
                leaveRoom(updatedRoom);
            }
        }
    };

    public MultiUserChatMgr(AbstractRainbowXMPPConnection connection, Context applicationContext, final IRoomMgr roomMgr, ChatMgr chatMgr)
    {
        m_connection = connection;
        m_contactCache = RainbowContext.getInfrastructure().getContactCacheMgr();

        m_chatMgr = chatMgr;
        m_mucMgr = MultiUserChatManager.getInstanceFor(connection);
        m_roomMgr = roomMgr;
        m_language = Locale.getDefault().getLanguage();

        m_roomMgr.setMultiUserChatMgr(this);
        m_roomMgr.getAllRooms().registerChangeListener(this);

        DeliveryReceiptManager.getInstanceFor(m_connection).setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.disabled);

        ProviderManager.removeExtensionProvider(GroupChatInvitation.ELEMENT, GroupChatInvitation.NAMESPACE);
        ProviderManager.removeExtensionProvider(RoomMultiUserChatEvent.ELEMENT, RoomMultiUserChatEvent.NAMESPACE);
        ProviderManager.removeExtensionProvider(GroupChatInvitation.ELEMENT, GroupChatInvitation.NAMESPACE);


        ProviderManager.addExtensionProvider(RoomMultiUserChatEvent.ELEMENT, RoomMultiUserChatEvent.NAMESPACE, new RoomMultiUserChatEvent.Provider());


        //detect incoming multichat invitations
        ProviderManager.addExtensionProvider(GroupChatInvitation.ELEMENT, GroupChatInvitation.NAMESPACE, new RainbowGroupChatInvitation.Provider());

        connection.addAsyncStanzaListener(m_invitationPacketListener, INVITATION_FILTER);
    }

    void findOrCreateMucFromServer(final Conversation conversation, final IRoomProxy.IGetRoomDataListener listener)
    {
        Log.getLogger().verbose(LOG_TAG, ">createMucFromServer");
        if (!conversation.isRoomType())
        {
            Log.getLogger().warn(LOG_TAG, "createMucFromServer; tried on NOT ROOM Type");
            return;
        }

        Room newRoom = null;
        if (m_roomMgr != null)
        {
            newRoom = m_roomMgr.getRoomById(conversation.getPeerId());
        }
        if (newRoom == null)
        {
            newRoom = new Room();
            newRoom.setId(conversation.getPeerId());
        }

        Log.getLogger().verbose(LOG_TAG, "calling getRoomData for " + conversation.getDisplayName(""));
        m_roomMgr.getRoomData(newRoom, new IRoomProxy.IGetRoomDataListener()
        {
            @Override
            public void onGetRoomDataSuccess(Room room)
            {
                Log.getLogger().verbose(LOG_TAG, ">onGetRoomDataSuccess");

                Room existingRoom = m_roomMgr.getRoomById(room.getId());
                if (existingRoom == null)
                {
                    existingRoom = room;
                    conversation.setRoom(existingRoom);
                }
                else
                {
                    conversation.setRoom(existingRoom);
                    existingRoom.update(room);
                }

                MultiUserChat multiUserChat = m_mucMgr.getMultiUserChat(conversation.getJid());
                m_poolMucChat.put(conversation, multiUserChat);

                joinRoomIfNeeded(existingRoom);

                conversation.notifyConversationUpdated();

                if (listener != null)
                    listener.onGetRoomDataSuccess(existingRoom);
            }

            @Override
            public void onGetRoomDataFailure(String roomUniqueIdentifier)
            {
                Log.getLogger().warn(LOG_TAG, ">onGetRoomDataFailure");

                if (listener != null)
                    listener.onGetRoomDataFailure(roomUniqueIdentifier);
            }
        });
    }

    @Override
    public void sendIsTypingState(Conversation conversation, ChatState state)
    {
        Log.getLogger().verbose(LOG_TAG, ">sendIsTypingState");

        if (conversation != null && conversation.getRoom() != null)
        {
            if (conversation.getRoom().isUserActive())
            {
                Log.getLogger().verbose(LOG_TAG, "User is Active");
                MultiUserChat muc = m_poolMucChat.get(conversation);
                if (muc == null)
                {
                    muc = m_mucMgr.getMultiUserChat(conversation.getJid());
                    Message smackMessage = new Message(conversation.getJid());
                    smackMessage.setBody(null);
                    smackMessage.setType(Message.Type.groupchat);
                    smackMessage.setSubject(null);
                    smackMessage.setTo(conversation.getRoom().getJid());
                    smackMessage.setFrom(m_contactCache.getUser().getContactId());
                    ChatStateExtension stateExtension = new ChatStateExtension(state);
                    smackMessage.addExtension(stateExtension);
                    try
                    {
                        muc.sendMessage(smackMessage);
                    }
                    catch (SmackException.NotConnectedException e)
                    {
                        Log.getLogger().verbose(LOG_TAG, "sendIsTypingState exception : " + e.toString());
                    }
                }
            }
        }
    }

    @Override
    public Conversation createNewConversationFromJid(String contactJid, IConversationProxy.ICreateConversationListener listener)
    {
        return m_chatMgr.createNewConversationFromJid(contactJid, listener);
    }

    @Override
    public void sendSingleMessageReadDelivery(Conversation conversation, IMMessage imMsg)
    {
        m_chatMgr.sendSingleMessageReadDelivery(conversation, imMsg);
    }

    @Override
    public void refreshConversations(IConversationProxy.IGetAllConversationListener listener)
    {

    }

    @Override
    public ArrayItemList<Conversation> getConversations()
    {
        return null;
    }

    @Override
    public void deleteAllMessages(Conversation conv, IMamNotification iMamNotification)
    {
        m_chatMgr.deleteAllMessages(conv, iMamNotification);
    }

    @Override
    public void sendMessage(IMMessage message, RainbowFileDescriptor fileDescriptor, Conversation conversation)
    {
        Log.getLogger().verbose(LOG_TAG, ">sendMessage");

        if (conversation != null)
        {
            MultiUserChat muc = m_poolMucChat.get(conversation);
            if (muc == null)
            {
                muc = m_mucMgr.getMultiUserChat(conversation.getJid());
            }

            try
            {
                Message smackMsg = new Message(conversation.getJid());
                smackMsg.setLanguage(m_language);
                smackMsg.setBody(message.getMessageContent());
                if (message.getMessageContent().isEmpty())
                {
                    smackMsg.addExtension(new StoreMessagePacketExtension());
                }
                if (!StringsUtil.isNullOrEmpty(message.getMessageId()))
                    smackMsg.setStanzaId(message.getMessageId());

                if (fileDescriptor != null)
                {
                    message.setFileDescriptor(fileDescriptor);

                    RainbowOutOfBandData oobExt = new RainbowOutOfBandData(fileDescriptor.getFileUrl(), fileDescriptor.getTypeMIME(), fileDescriptor.getFileName(), fileDescriptor.getSize());
                    smackMsg.addExtension(oobExt);
                }

                muc.sendMessage(smackMsg);

                message.setDeliveryState(IMMessage.DeliveryState.SENT);
                message.setMessageId(smackMsg.getStanzaId());
                storeMessage(conversation, message);
                m_chatMgr.notifyImSent(conversation);
                conversation.getMessages().fireDataChanged();
            }
            catch (SmackException.NotConnectedException e)
            {
                Log.getLogger().error(LOG_TAG, "An error occured while Sending message: " + e.getMessage());
                //onSendMessageError();
            }
        }
    }

    /**
     * store instant message in the local cache
     *
     * @param conversation (String) : conversation of the room
     * @param message      (IMMessage) : the message
     */
    public boolean storeMessage(Conversation conversation, IMMessage message)
    {
        if (conversation == null)
            return false;

        String body = "";
        if (!StringsUtil.isNullOrEmpty(message.getMessageContent()))
        {
            body = message.getMessageContent().charAt(0) + "xxxx" + message.getMessageContent().charAt(message.getMessageContent().length() - 1);
        }
        String room = "";
        if (conversation.getRoom() != null)
            room = conversation.getRoom().getName();
        Log.getLogger().debug(LOG_TAG, ">storeMessage Id: " + message.getMessageId() + " Body: " + body + " for room :" + room);


        if (conversation.isMsgAlreadyStored(message))
        {
            return false;
        }
        else
        {
            conversation.addMessage(message);
            conversation.setLastMessage(message);
            if (RainbowContext.getInfrastructure().getDatabaseMgr() != null)
                RainbowContext.getInfrastructure().getDatabaseMgr().getChatDataSource().createOrUpdateChat(conversation.getJid(), message);

            m_chatMgr.sortConversations();
            return true;
        }

    }

    private void joinAllActiveRooms()
    {
        Log.getLogger().verbose(LOG_TAG, ">joinAllActiveRooms");
        if (m_connection != null && m_connection.isAuthenticated())
        {
            for (Room room : m_roomMgr.getAllRooms().getCopyOfDataList())
            {
                joinOrLeave(room);
            }
        }
    }

    private void joinOrLeave(Room room)
    {
        if (room.isUserActive())
        {
            forceJoinRoom(room);
        }
        else
        {
            leaveRoom(room);
        }
        room.registerChangeListener(m_roomListener);
    }

    private void forceJoinRoom(Room room)
    {
        if (room == null)
            return;

        if (room.isUserActive())
        {
            MultiUserChat multiUserChat = m_mucMgr.getMultiUserChat(room.getJid());
            joinIfNeeded(multiUserChat, room, true);
        }
    }

    public void joinRoomIfNeeded(Room room)
    {
        if (room == null)
            return;

        if (room.isUserActive())
        {
            MultiUserChat multiUserChat = m_mucMgr.getMultiUserChat(room.getJid());
            joinIfNeeded(multiUserChat, room, false);
        }
    }

    private void joinIfNeeded(MultiUserChat muc, Room room, boolean force)
    {
        if (room == null)
            return;
        if (muc == null)
            return;
        if (m_connection == null || !m_connection.isAuthenticated())
            return;

        Log.getLogger().verbose(LOG_TAG, "joinIfNeeded: " + room.getName());

        if (force || !muc.isJoined() && !StringsUtil.isNullOrEmpty(m_connection.getUser()))
        {
            try
            {
                //if already joined, first leave and rejoin
                muc.join(m_connection.getUser());
                connectListener(muc, room);
                Log.getLogger().debug(LOG_TAG, "join room " + room.getName());
            }
            catch (SmackException.NoResponseException e)
            {
                Log.getLogger().error(LOG_TAG, "joinIfNeeded NoResponseException from smack " + e.toString());
            }
            catch (XMPPException.XMPPErrorException e)
            {
                Log.getLogger().error(LOG_TAG, "joinIfNeeded XMPPErrorException from smack " + e.toString());
            }
            catch (SmackException.NotConnectedException e)
            {
                Log.getLogger().error(LOG_TAG, "joinIfNeeded NotConnectedException from smack " + e.toString());
            }
        }
    }


    private void leaveRoom(Room room)
    {
        if (!room.isUserActive())
        {
            MultiUserChat multiUserChat = m_mucMgr.getMultiUserChat(room.getJid());
            leaveRoom(multiUserChat);
            Log.getLogger().debug(LOG_TAG, "leaveRoom " + room.getName());
        }
    }

    private void leaveRoom(MultiUserChat muc)
    {

        if (muc == null)
            return;
        try
        {
            muc.leave(); //also check if is joined and removes all listeners
        }
        catch (SmackException.NotConnectedException e)
        {
            Log.getLogger().error(LOG_TAG, "leaveRoom NotConnectedException from smack " + e.toString());
        }
    }

    public Conversation getConversationByRoomId(String roomId)
    {
        Log.getLogger().verbose(LOG_TAG, ">getConversationByRoomId: " + roomId);

        for (Conversation conv : m_chatMgr.getConversations().getCopyOfDataList())
        {
            if (conv.isRoomType())
            {
                if (conv.getRoom() != null && conv.getRoom().getId() != null && conv.getRoom().getId().equals(roomId))
                {
                    return conv;
                }
            }
        }
        return null;
    }

    @Override
    public void sendMessagesReadDelivery(Conversation conversation)
    {
        m_chatMgr.sendMessagesReadDelivery(conversation);
    }

    @Override
    public Conversation getConversationFromJid(String jid)
    {
        return m_chatMgr.getConversationFromJid(jid);
    }

    @Override
    public void refreshMoreMessages(String jid, String with, String lastMamImId, int nbMessagesToRetrieve, IMamNotification iMamNotification) {
        m_chatMgr.refreshMoreMessages(jid, with, lastMamImId, nbMessagesToRetrieve, iMamNotification);
    }

    @Override
    public void registerChangeListener(ChatMgr.IChatMgrListener changeListener) {
        m_chatMgr.registerChangeListener(changeListener);
    }

    @Override
    public void unregisterChangeListener(ChatMgr.IChatMgrListener changeListener) {
        m_chatMgr.unregisterChangeListener(changeListener);
    }

    @Override
    public void refreshMessages(final Conversation conversation, final int nbMessagesToRetrieve, final IMamNotification iMamNotification) {
        if (conversation == null)
        {
            Log.getLogger().warn(LOG_TAG, "refreshMessages ; conversation is NULL");
            return;
        }


        if (RainbowContext.getInfrastructure().isXmppConnected())
        {
            Log.getLogger().verbose(LOG_TAG, ">refreshMessages; " + conversation.getName());
            Thread myThread2 = new Thread() {
                public void run() {
                    m_chatMgr.refreshMessages(conversation, conversation.getJid(), m_contactCache.getUser().getImJabberId(), nbMessagesToRetrieve, iMamNotification);

                    MultiUserChat multiUserChat = m_poolMucChat.get(conversation);
                    if (multiUserChat == null && !StringsUtil.isNullOrEmpty(conversation.getJid()))
                    {
                        multiUserChat = m_mucMgr.getMultiUserChat(conversation.getJid());
                        m_poolMucChat.put(conversation, multiUserChat);
                    }

                    joinRoomIfNeeded(conversation.getRoom());
                }
            };
            myThread2.start();
        }
        else
        {
            Log.getLogger().verbose(LOG_TAG, ">refreshMessages not xmpp connected; ");
        }

    }

    private void connectListener(final MultiUserChat multiUserChat, final Room room)
    {
        if (multiUserChat == null)
            return;

        multiUserChat.removeMessageListener(m_messageListener);
        multiUserChat.addMessageListener(m_messageListener);

        m_chats.add(multiUserChat);
    }

    private synchronized void parseRoomMessage(Message message)
    {
        Log.getLogger().verbose(LOG_TAG, ">parseRoomMessage");

        Room room = m_roomMgr.getRoomByJid(XmppUtils.getRoomJid(message.getFrom()));
        MultiUserChat multiUserChat = m_mucMgr.getMultiUserChat(XmppUtils.getRoomJid(message.getFrom()));

        if (room == null || multiUserChat == null)
            return;

        DeliveryReceiptRequest receiptExtension = message.getExtension(DeliveryReceiptRequest.ELEMENT, new DeliveryReceiptRequest().getNamespace());
        RainbowArchived archived = message.getExtension(RainbowArchived.ELEMENT, RainbowArchived.NAMESPACE);
        RainbowOutOfBandData outOfBand = message.getExtension(RainbowOutOfBandData.ELEMENT, RainbowOutOfBandData.NAMESPACE);
        String foundLanguage = null;

        if (message.getBodyLanguages().size() > 0)
        {
            if (message.getBodyLanguages().contains(m_language))
                foundLanguage = m_language;
            else
                foundLanguage = message.getBodyLanguages().get(0);
        }

        Conversation conv = m_chatMgr.getConversationFromJid(room.getJid());
        if (message.getBody(foundLanguage) != null)
        {
            Log.getLogger().verbose(LOG_TAG, "managing message");

            boolean sent;
            String from = XmppUtils.getFromJid(message.getFrom());
            String userJid = m_contactCache.getUser().getImJabberId();
            if (userJid.equals(from))
            {
                Log.getLogger().verbose(LOG_TAG, " Message sent to ROOM");
                m_chatMgr.sendMsgReceiptAck(message.getStanzaId(), room.getJid(), message.getType().equals(Message.Type.groupchat), RainbowDeliveryReceivedReceipt.EVENT_READ);
                sent = true;
            }
            else
            {
                Log.getLogger().verbose(LOG_TAG, " Message received from " + message.getFrom());
                sent = false;
            }
            if (conv == null)
            {
                conv = m_chatMgr.createNewConversationFromRoom(room, null);
                m_poolMucChat.put(conv, multiUserChat);
            }

            IMMessage imMessage = null;
            RoomMultiUserChatEvent roomMultiUserChatEvent = message.getExtension(RoomMultiUserChatEvent.ELEMENT, RoomMultiUserChatEvent.NAMESPACE);
            if (roomMultiUserChatEvent != null)
            {
                imMessage = new IMMessage(roomMultiUserChatEvent.getJid(), roomMultiUserChatEvent.getEventType());
                switch (roomMultiUserChatEvent.getEventType()) {

                    case CONFERENCEADD:
                        final Room roomToUpdate  = m_roomMgr.getRoomByJid(message.getFrom());
                        if (room != null) {
                            room.setInactiveConference(false);
                            TimerTask delayedTask = new TimerTask() {
                                @Override
                                public void run() {
                                    m_roomMgr.getRoomData(roomToUpdate, null);
                                }
                            };
                            Timer updateRoom = new Timer();
                            updateRoom.schedule(delayedTask, Duration.FIVE_SECONDS_IN_MILLISECONDS);
                        }
                        break;
                    case CONFERENCEREMOVED:
                        room = m_roomMgr.getRoomByJid(message.getFrom());
                        if (room != null) {
                            room.setInactiveConference(true);
                            m_roomMgr.getRoomData(room, null);
                        }
                        break;
                    default:
                        break;
                }
            }else
            {
                imMessage = new IMMessage(from, message.getBody(foundLanguage), sent);
            }
            if (archived != null && !StringsUtil.isNullOrEmpty(archived.getStamp()))
            {
                imMessage.setTimeStampFromDateString(archived.getStamp());
            }
            imMessage.setMessageId(message.getStanzaId());
            if (receiptExtension != null)
            {
                Log.getLogger().verbose(LOG_TAG, "ReceiptRequest received");

                if (!sent)
                {
                    imMessage.setDeliveryState(IMMessage.DeliveryState.RECEIVED);
                    m_chatMgr.sendMsgReceiptAck(imMessage.getMessageId(), room.getJid(), message.getType().equals(Message.Type.groupchat), RainbowDeliveryReceivedReceipt.EVENT_RECEIVED);
                }
                else
                {
                    imMessage.setDeliveryState(IMMessage.DeliveryState.SENT_SERVER_RECEIVED);
                    m_chatMgr.sendMsgReceiptAck(imMessage.getMessageId(), room.getJid(), message.getType().equals(Message.Type.groupchat), RainbowDeliveryReceivedReceipt.EVENT_READ);
                }

            }

            m_chatMgr.addFileDescriptorToIMMessage(outOfBand, from, imMessage);

            if (storeMessage(conv, imMessage) && !sent)
            {
                m_chatMgr.notifyImReceived(conv, imMessage);
            }
        }
    }

    private void parseChatStateExtension(Message message, ExtensionElement chatStateExtension)
    {
        Log.getLogger().verbose(LOG_TAG, "parseChatStateExtension");
        String roomId = XmppUtils.getRoomJid(message.getFrom());
        String from = XmppUtils.getFromJid(message.getFrom());
        Contact contact = m_contactCache.getContactFromJid(from);
        if (contact == null)
        {
            Log.getLogger().error(LOG_TAG, "No contact found for Chat state : " + from);
            return;
        }
        if (!contact.getContactId().equals(m_contactCache.getUser().getContactId()))
        {
            ChatState state;
            try
            {
                state = ChatState.valueOf(chatStateExtension.getElementName());
                Log.getLogger().verbose(LOG_TAG, "chatState = " + state.toString());
                switch (state)
                {
                    case active:
                        Log.getLogger().verbose(LOG_TAG, "State Active");
                        m_chatMgr.notifyIsTypingInRoom(contact, false, roomId);
                        break;

                    case composing:
                        Log.getLogger().verbose(LOG_TAG, "State Composing");
                        m_chatMgr.notifyIsTypingInRoom(contact, true, roomId);
                        break;

                    case paused:
                        Log.getLogger().verbose(LOG_TAG, "State Paused");
                        break;

                    case inactive:
                        Log.getLogger().verbose(LOG_TAG, "State Inactive");
                        break;

                    case gone:
                        Log.getLogger().verbose(LOG_TAG, "State Gone");
                        break;
                }
            }
            catch (Exception ex)
            {
                Log.getLogger().verbose(LOG_TAG, "parseChatStateExtension exception : " + ex.toString());
            }
        }
    }

    public void refreshRoomConferenceInfo (String roomJid, String confEndPoint) {

        if (StringsUtil.isNullOrEmpty(roomJid) ) {
            Log.getLogger().warn(LOG_TAG, "refreshRoomConferenceInfo: Bad informations: " +roomJid + " : " + confEndPoint );
            return;
        }
        Room room = m_roomMgr.getRoomByJid(roomJid);
        if (room == null) {
            Log.getLogger().warn(LOG_TAG, "refreshRoomConferenceInfo: Bad roomId: " +roomJid );
            return;
        }
        m_roomMgr.getRoomData(room,null);
    }


    private void cancelJoinTimer()
    {
        if (m_timerJoin != null)
        {
            m_timerJoin.cancel();
            m_timerJoin = null;
        }
    }

    private void scheduleJoinTimer()
    {
        Log.getLogger().debug(LOG_TAG, ">scheduleJoinTimer");
        cancelJoinTimer();

        if (m_connection == null || !m_connection.isConnected())
        {
            Log.getLogger().debug(LOG_TAG, ">scheduleJoinTimer");
            return;
        }

        m_timerJoin = new Timer();
        m_timerJoin.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                joinAllActiveRooms();
            }
        }, Duration.FIVE_SECONDS_IN_MILLISECONDS);
    }

    @Override
    public void conversationAdded(final Conversation conversationAdded)
    {
        Log.getLogger().verbose(LOG_TAG, ">conversationAdded");

        if (conversationAdded.isRoomType())
        {
            m_roomMgr.addNewRoom(conversationAdded.getRoom());
        }
    }

    @Override
    public void conversationDeleted(Conversation conversationDeleted)
    {
        Log.getLogger().verbose(LOG_TAG, ">conversationDeleted");
        m_roomMgr.deleteRoom(conversationDeleted.getRoom());
    }

    @Override
    public void conversationUpdated(Conversation updatedConversation)
    {
        Log.getLogger().verbose(LOG_TAG, ">conversationUpdated");

    }

    public void disconnect()
    {
        cancelJoinTimer();

        if (m_roomMgr != null)
        {
            m_roomMgr.getAllRooms().unregisterChangeListener(this);

            for (Room room : m_roomMgr.getAllRooms().getCopyOfDataList())
            {
                room.unregisterChangeListener(m_roomListener);
            }

            m_roomMgr.setMultiUserChatMgr(null);
            m_roomMgr = null;
        }

        if (m_connection != null)
        {
            m_connection.removeAsyncStanzaListener(m_invitationPacketListener);

            ProviderManager.removeExtensionProvider(GroupChatInvitation.ELEMENT, GroupChatInvitation.NAMESPACE);
            ProviderManager.removeExtensionProvider(RoomMultiUserChatEvent.ELEMENT, RoomMultiUserChatEvent.NAMESPACE);

            m_connection = null;
        }

        for (MultiUserChat chat : m_chats)
            chat.removeMessageListener(m_messageListener);

        m_chats.clear();

        m_chatMgr = null;
    }

    @Override
    public void dataChanged()
    {
        Log.getLogger().debug(LOG_TAG, ">dataChanged(allRoomsListener)");
        scheduleJoinTimer();
    }
}

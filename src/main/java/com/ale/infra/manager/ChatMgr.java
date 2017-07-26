package com.ale.infra.manager;

import android.content.Context;
import android.content.Intent;
import android.support.v4.util.Pair;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.application.RainbowIntent;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.database.ChatDataSource;
import com.ale.infra.database.ConversationDataSource;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.invitation.Invitation;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.manager.fileserver.IFileMgr;
import com.ale.infra.manager.fileserver.IFileProxy;
import com.ale.infra.manager.fileserver.RainbowFileDescriptor;
import com.ale.infra.manager.pgiconference.IPgiConferenceMgr;
import com.ale.infra.manager.pgiconference.PgiConference;
import com.ale.infra.manager.room.IRoomMgr;
import com.ale.infra.manager.room.Room;
import com.ale.infra.manager.room.RoomChange;
import com.ale.infra.platformservices.IDataNetworkMonitor;
import com.ale.infra.proxy.conversation.IConversationProxy;
import com.ale.infra.proxy.room.IRoomProxy;
import com.ale.infra.rainbow.api.ConversationType;
import com.ale.infra.xmpp.AbstractRainbowXMPPConnection;
import com.ale.infra.xmpp.XmppIntent;
import com.ale.infra.xmpp.XmppUtils;
import com.ale.infra.xmpp.xep.DeletedReceipt.DeletedReceiptPacketExtension;
import com.ale.infra.xmpp.xep.DeletedReceipt.RainbowDeletedReceipt;
import com.ale.infra.xmpp.xep.DeliveryReceipt.DeliveryReceiptPacketExtension;
import com.ale.infra.xmpp.xep.DeliveryReceipt.RainbowDeliveryReceivedReceipt;
import com.ale.infra.xmpp.xep.DeliveryReceipt.RainbowDeliveryTimestampReceipt;
import com.ale.infra.xmpp.xep.DeliveryReceipt.TimestampReceiptPacketExtension;
import com.ale.infra.xmpp.xep.IMamNotification;
import com.ale.infra.xmpp.xep.MamIQResult;
import com.ale.infra.xmpp.xep.MamMessagePacketExtension;
import com.ale.infra.xmpp.xep.MamMgr;
import com.ale.infra.xmpp.xep.ManagementReceipt.ManagementReceiptPacketExtension;
import com.ale.infra.xmpp.xep.Room.RoomConferenceEvent;
import com.ale.infra.xmpp.xep.Room.RoomMultiUserChatEvent;
import com.ale.infra.xmpp.xep.Time.TimeReceiveIq;
import com.ale.infra.xmpp.xep.Time.TimeRequestIq;
import com.ale.infra.xmpp.xep.archived.RainbowArchived;
import com.ale.infra.xmpp.xep.archived.RainbowArchivedPacketExtension;
import com.ale.infra.xmpp.xep.calllog.CallLogPacketExtension;
import com.ale.infra.xmpp.xep.message.StoreMessagePacketExtension;
import com.ale.infra.xmpp.xep.outofband.RainbowOutOfBandData;
import com.ale.infra.xmpp.xep.pgiconference.PgiConferenceInfoExtension;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.DefaultExtensionElement;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.carbons.packet.CarbonExtension;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.ChatStateListener;
import org.jivesoftware.smackx.chatstates.ChatStateManager;
import org.jivesoftware.smackx.forward.packet.Forwarded;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by cebruckn on 30/10/15.
 */
public class ChatMgr implements IChatMgr, ChatManagerListener, ChatMessageListener, ChatStateListener, XmppContactMgr.XmppContactMgrListener {

    final static String LOG_TAG = "ChatMgr";


    public static final String BODY_FILETRANSFER_TAG = "%%FILE%%TRANSFER%%";

    private final Set<IChatMgrListener> m_changeListeners = new HashSet<>();
    private static final int TIMEOUT_30S = 30000;
    private final IDataNetworkMonitor m_dataNetworkMonitor;
    private final String m_userJid;
    private final XmppContactMgr m_xmppContactMgr;
    private Context m_applicationContext;
    private AbstractRainbowXMPPConnection m_connection;
    private ArrayItemList<Conversation> m_conversations;
    private Map<String, Chat> m_poolChat = new HashMap<>();
    private IMamNotification m_mamNotif = null;
    private String m_language;
    private boolean resentDone = false;
    private long timestampDiffFromServer = 0;
    private long timestampAtRequest = 0;
    private ConversationDataSource conversationDataSource;
    private ChatDataSource chatDataSource;
    private final MamMgr m_mamMgr;
    private IContactCacheMgr m_contactCache;
    private IConversationProxy m_conversationProxy;
    private final IFileMgr m_fileMgr;
    private final IPgiConferenceMgr m_pgiMgr;
    private Set<Chat> m_chats = new HashSet<>();

    public ChatMgr(AbstractRainbowXMPPConnection c, Context applicationContext, XmppContactMgr xmppContactMgr, IConversationProxy conversationProxy, IFileMgr fileMgr, IPgiConferenceMgr pgiMgr) {
        m_connection = c;
        m_applicationContext = applicationContext;
        m_dataNetworkMonitor = RainbowContext.getInfrastructure().getDataNetworkMonitor();
        m_contactCache = RainbowContext.getInfrastructure().getContactCacheMgr();
        m_userJid = RainbowContext.getPlatformServices().getApplicationData().getUserJidIm();
        m_language= Locale.getDefault().getLanguage();
        m_xmppContactMgr = xmppContactMgr;
        m_conversationProxy = conversationProxy;
        m_fileMgr = fileMgr;
        m_pgiMgr = pgiMgr;

        if (RainbowContext.getInfrastructure().getDatabaseMgr() != null) {
            conversationDataSource = RainbowContext.getInfrastructure().getDatabaseMgr().getConversationDataSource();
            chatDataSource = RainbowContext.getInfrastructure().getDatabaseMgr().getChatDataSource();
        }else {
            conversationDataSource = null;
            chatDataSource = null;
        }

        m_conversations = new ArrayItemList<>();

        if (m_connection != null) {
            ChatStateManager.getInstance(m_connection);
            ChatManager.getInstanceFor(m_connection).addChatListener(this);
        } else
            Log.getLogger().warn(LOG_TAG, "XMPP connection is NULL");

        m_mamMgr = new MamMgr(m_connection, m_userJid);

        if(m_xmppContactMgr != null) {
            m_xmppContactMgr.registerChangeListener(this);
        }


        // provider for messages with DELIVERY RECEIPT extension:
        ProviderManager.removeExtensionProvider(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE);

        ProviderManager.addExtensionProvider(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE, new ExtensionElementProvider<RainbowDeliveryReceivedReceipt>() {

            @Override
            public RainbowDeliveryReceivedReceipt parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {
                DeliveryReceiptPacketExtension deliveryReceiptPacketExtension = new DeliveryReceiptPacketExtension(parser);
                return new RainbowDeliveryReceivedReceipt(deliveryReceiptPacketExtension.getQueryid(), deliveryReceiptPacketExtension.getEvent(), deliveryReceiptPacketExtension.getEntity(), false);
            }
        });
        
        ProviderManager.addExtensionProvider(RainbowDeliveryTimestampReceipt.ELEMENT, RainbowDeliveryTimestampReceipt.NAMESPACE, new ExtensionElementProvider<RainbowDeliveryTimestampReceipt>() {

            @Override
            public RainbowDeliveryTimestampReceipt parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {
                TimestampReceiptPacketExtension deliverytimestampPacketExtension = new TimestampReceiptPacketExtension(parser);
                return new RainbowDeliveryTimestampReceipt(deliverytimestampPacketExtension.getTimestampValue(), deliverytimestampPacketExtension.getTimestamp());
            }
        });

        ProviderManager.addExtensionProvider("conversation", ManagementReceiptPacketExtension.NAMESPACE, new ExtensionElementProvider<ExtensionElement>() {
            @Override
            public DefaultExtensionElement parse(XmlPullParser parser, int initialDepth) throws org.xmlpull.v1.XmlPullParserException,
                    IOException {
                return new ManagementReceiptPacketExtension(parser);
            }
        });

        ProviderManager.addExtensionProvider("room", ManagementReceiptPacketExtension.NAMESPACE, new ExtensionElementProvider<ExtensionElement>() {
            @Override
            public DefaultExtensionElement parse(XmlPullParser parser, int initialDepth) throws org.xmlpull.v1.XmlPullParserException,
                    IOException {
                return new ManagementReceiptPacketExtension(parser);
            }
        });

        ProviderManager.addExtensionProvider("unmute", ManagementReceiptPacketExtension.NAMESPACE, new ExtensionElementProvider<ExtensionElement>() {
            @Override
            public DefaultExtensionElement parse(XmlPullParser parser, int initialDepth) throws org.xmlpull.v1.XmlPullParserException,
                    IOException {
                return new ManagementReceiptPacketExtension(parser);
            }
        });

        ProviderManager.addExtensionProvider("mute", ManagementReceiptPacketExtension.NAMESPACE, new ExtensionElementProvider<ExtensionElement>() {
            @Override
            public DefaultExtensionElement parse(XmlPullParser parser, int initialDepth) throws org.xmlpull.v1.XmlPullParserException,
                    IOException {
                return new ManagementReceiptPacketExtension(parser);
            }
        });

        ProviderManager.addExtensionProvider("group", ManagementReceiptPacketExtension.NAMESPACE, new ExtensionElementProvider<ExtensionElement>() {
            @Override
            public DefaultExtensionElement parse(XmlPullParser parser, int initialDepth) throws org.xmlpull.v1.XmlPullParserException,
                    IOException {
                return new ManagementReceiptPacketExtension(parser);
            }
        });

        ProviderManager.addExtensionProvider("userinvite", ManagementReceiptPacketExtension.NAMESPACE, new ExtensionElementProvider<ExtensionElement>() {
            @Override
            public DefaultExtensionElement parse(XmlPullParser parser, int initialDepth) throws org.xmlpull.v1.XmlPullParserException,
                    IOException {
                return new ManagementReceiptPacketExtension(parser);
            }
        });

        ProviderManager.addExtensionProvider("usersettings", ManagementReceiptPacketExtension.NAMESPACE, new ExtensionElementProvider<ExtensionElement>() {
            @Override
            public DefaultExtensionElement parse(XmlPullParser parser, int initialDepth) throws org.xmlpull.v1.XmlPullParserException,
                    IOException {
                return new ManagementReceiptPacketExtension(parser);
            }
        });

        ProviderManager.addExtensionProvider(RainbowDeletedReceipt.ELEMENT, RainbowDeletedReceipt.NAMESPACE, new ExtensionElementProvider<RainbowDeletedReceipt>() {

            @Override
            public RainbowDeletedReceipt parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {
                DeletedReceiptPacketExtension deletedReceiptPacketExtension = new DeletedReceiptPacketExtension(parser);
                return new RainbowDeletedReceipt(deletedReceiptPacketExtension.getQueryid(), deletedReceiptPacketExtension.getWith());
            }
        });

        ProviderManager.addExtensionProvider("joincompanyinvite", ManagementReceiptPacketExtension.NAMESPACE, new ExtensionElementProvider<ExtensionElement>() {
            @Override
            public DefaultExtensionElement parse(XmlPullParser parser, int initialDepth) throws org.xmlpull.v1.XmlPullParserException,
                    IOException {
                return new ManagementReceiptPacketExtension(parser);
            }
        });

        ProviderManager.addExtensionProvider("joincompanyrequest", ManagementReceiptPacketExtension.NAMESPACE, new ExtensionElementProvider<ExtensionElement>() {
            @Override
            public DefaultExtensionElement parse(XmlPullParser parser, int initialDepth) throws org.xmlpull.v1.XmlPullParserException,
                    IOException {
                return new ManagementReceiptPacketExtension(parser);
            }
        });


        ProviderManager.addExtensionProvider(RainbowArchived.ELEMENT, RainbowArchived.NAMESPACE, new ExtensionElementProvider<RainbowArchived>() {
            @Override
            public RainbowArchived parse(XmlPullParser parser, int initialDepth) throws org.xmlpull.v1.XmlPullParserException,
                    IOException {
                RainbowArchivedPacketExtension rainbowArchivedPacketExtension = new RainbowArchivedPacketExtension(parser);
                return new RainbowArchived(rainbowArchivedPacketExtension.getStamp());
            }
        });

        ProviderManager.addExtensionProvider(RainbowOutOfBandData.ELEMENT, RainbowOutOfBandData.NAMESPACE, new RainbowOutOfBandData.Provider());

        ProviderManager.addExtensionProvider(CallLogPacketExtension.ELEMENT, CallLogPacketExtension.NAMESPACE, new CallLogPacketExtension.Provider());

        ProviderManager.addExtensionProvider(PgiConferenceInfoExtension.ELEMENT, PgiConferenceInfoExtension.NAMESPACE, new PgiConferenceInfoExtension.Provider());
        ProviderManager.addIQProvider(TimeRequestIq.ELEMENT, TimeRequestIq.NAMESPACE, new IQProvider<IQ>()
        {
            @Override
            public IQ parse(XmlPullParser xmlPullParser, int i) throws XmlPullParserException, IOException, SmackException
            {
                return new TimeReceiveIq(xmlPullParser, TimeRequestIq.ELEMENT, TimeRequestIq.NAMESPACE);
            }
        });

        ProviderManager.addExtensionProvider(RoomConferenceEvent.ELEMENT, RoomConferenceEvent.NAMESPACE, new RoomConferenceEvent.Provider());



    }

    public void getConversationsFromDB(){
        Log.getLogger().info(LOG_TAG, ">getConversationsFromDB");
        if (conversationDataSource != null) {
            List<Conversation> allConversationFromDatabase = conversationDataSource.getAllConversationFromDatabase();
            if( allConversationFromDatabase != null && allConversationFromDatabase.size() > 0)
            {
                m_conversations.addAll(allConversationFromDatabase);
                sortConversations();
            }
        }
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        Log.getLogger().info(LOG_TAG, ">chatCreated");
        chat.addMessageListener(this);

        m_chats.add(chat);

        if( !StringsUtil.isNullOrEmpty(chat.getParticipant())) {
            String buddyJid = StringsUtil.getJidWithoutDevicePartAndTelPart(chat.getParticipant());
            m_poolChat.put(buddyJid, chat);
        }
    }

    @Override
    public void stateChanged(Chat chat, ChatState state) {
        Log.getLogger().verbose(LOG_TAG, ">stateChanged");
        Contact otherContact = m_contactCache.getContactFromJid(chat.getParticipant());
        if (otherContact == null) {
            Log.getLogger().warn(LOG_TAG, "No contact found for Jid=" + chat.getParticipant());
            return;
        }

        switch (state) {
            case active:
                Log.getLogger().debug(LOG_TAG, "State Active");
                notifyIsTyping(otherContact, false);
                break;

            case composing:
                Log.getLogger().debug(LOG_TAG, "State Composing");
                notifyIsTyping(otherContact, true);
                break;

            case paused:
                Log.getLogger().debug(LOG_TAG, "State Paused");
                break;

            case inactive:
                Log.getLogger().debug(LOG_TAG, "State Inactive");
                break;

            case gone:
                Log.getLogger().debug(LOG_TAG, "State Gone");
                break;
        }
    }

    @Override
    public void processMessage(Chat chat, Message message) {
        Log.getLogger().info(LOG_TAG, ">>>processMessage");

        // MAM messages:
        // check if message is an archived one.
        // (get the mamExtension according to the namespace)
        MamMessagePacketExtension mamExtension = (MamMessagePacketExtension) message.getExtension(MamMessagePacketExtension.NAMESPACE);
        ExtensionElement deliveryReceiptExtension = message.getExtension(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE);
        ExtensionElement deliveryTimestampExtension = message.getExtension(RainbowDeliveryTimestampReceipt.ELEMENT, RainbowDeliveryTimestampReceipt.NAMESPACE);
        ManagementReceiptPacketExtension managementExtension = (ManagementReceiptPacketExtension) message.getExtension(ManagementReceiptPacketExtension.NAMESPACE);
        CarbonExtension copyCarbonExtension = (CarbonExtension) message.getExtension(CarbonExtension.NAMESPACE);
        RainbowDeletedReceipt deletedExtension = message.getExtension(RainbowDeletedReceipt.ELEMENT, RainbowDeletedReceipt.NAMESPACE);
        RainbowArchived archived = message.getExtension(RainbowArchived.ELEMENT, RainbowArchived.NAMESPACE);
        RainbowOutOfBandData outOfBand = message.getExtension(RainbowOutOfBandData.ELEMENT, RainbowOutOfBandData.NAMESPACE);
        CallLogPacketExtension callLogExt = message.getExtension(CallLogPacketExtension.ELEMENT, CallLogPacketExtension.NAMESPACE);
        PgiConferenceInfoExtension pgiConfInfoExt = message.getExtension(PgiConferenceInfoExtension.ELEMENT, PgiConferenceInfoExtension.NAMESPACE);

        ExtensionElement deliveryRequestExtension = message.getExtension(DeliveryReceiptRequest.ELEMENT, DeliveryReceipt.NAMESPACE);

        RoomConferenceEvent roomConferenceEvent = message.getExtension(RoomConferenceEvent.ELEMENT, RoomConferenceEvent.NAMESPACE);

        if (mamExtension != null) {
            manageMessageMam(mamExtension);
        } else if (roomConferenceEvent != null) {
            // User has been invited to join a conference
            MultiUserChatMgr multiUserChatMgr = RainbowContext.getInfrastructure().getMultiUserChatMgr();
            multiUserChatMgr.refreshRoomConferenceInfo(roomConferenceEvent.getRoomJid(), roomConferenceEvent.getConfEndPointId());
        } else if (deliveryReceiptExtension != null && deliveryReceiptExtension instanceof RainbowDeliveryReceivedReceipt) {
            RainbowDeliveryReceivedReceipt rainbowDeliveryReceivedReceipt = (RainbowDeliveryReceivedReceipt) deliveryReceiptExtension;
            RainbowDeliveryTimestampReceipt rainbowDeliveryTimestamp = null;
            if (deliveryTimestampExtension != null && deliveryTimestampExtension instanceof RainbowDeliveryTimestampReceipt) {
                rainbowDeliveryTimestamp = (RainbowDeliveryTimestampReceipt) deliveryTimestampExtension;
            }
            Log.getLogger().verbose(LOG_TAG, "RainbowDeliveryReceivedReceipt Extension detected ; event=" + rainbowDeliveryReceivedReceipt.getEvent() + " entity=" + rainbowDeliveryReceivedReceipt.getEntity());
            setNewImMessageState(rainbowDeliveryReceivedReceipt, rainbowDeliveryTimestamp, message);
        } else if (managementExtension != null) {
            processManagementMessage(managementExtension);
        } else if (pgiConfInfoExt != null) {
            processPgiConferenceInfo(pgiConfInfoExt);
        } else if (deletedExtension != null) {
            manageDeletedMessage(deletedExtension);
        } else if (copyCarbonExtension != null) {
            manageCopyCarbonMessage(message, copyCarbonExtension);
        } else if (message.getType().equals(Message.Type.chat) || message.getType().equals(Message.Type.normal)) {
            //CT : extract language from body, ie device language is difference from IM language
            List<String> bodyLanguages = message.getBodyLanguages();
            String foundLanguage = null;

            if (bodyLanguages.size() > 0) {
               if (message.getBodyLanguages().contains(m_language))
                   foundLanguage = m_language;
               else
                   foundLanguage = message.getBodyLanguages().get(0);
            }

            if (message.getBody(foundLanguage) != null || callLogExt != null) {
                Log.getLogger().verbose(LOG_TAG, " managing message");
                String buddyJid;
                if (message.getFrom().equals(m_userJid)) {
                    Log.getLogger().verbose(LOG_TAG, " Message sent to " + message.getTo());
                    buddyJid = StringsUtil.getJidWithoutDevicePartAndTelPart(message.getTo());
                } else {
                    Log.getLogger().verbose(LOG_TAG, " Message received from " + message.getFrom());
                    buddyJid = StringsUtil.getJidWithoutDevicePartAndTelPart(message.getFrom());
                }

                if (buddyJid != null) {
                    IMMessage imMessage = new IMMessage(buddyJid, message.getBody(foundLanguage), false);
                    if( archived != null &&
                            !StringsUtil.isNullOrEmpty(archived.getStamp()) ){
                        imMessage.setTimeStampFromDateString(archived.getStamp());
                    }
                    imMessage.setMessageId(message.getStanzaId());
                    if( callLogExt != null) {
                        imMessage.setCallLogEvent(callLogExt, m_userJid);
                    }

                    addFileDescriptorToIMMessage(outOfBand, buddyJid, imMessage);

                    if (message.getFrom().equals(m_userJid))
                        imMessage.setDeliveryState(IMMessage.DeliveryState.SENT);
                    else
                        imMessage.setDeliveryState(IMMessage.DeliveryState.RECEIVED);

                    if (deliveryRequestExtension != null && deliveryRequestExtension instanceof DeliveryReceiptRequest) {
                        Log.getLogger().verbose(LOG_TAG, "Message is requested perform action to acknowledge it.");

                        sendMsgReceiptAck(imMessage.getMessageId(), buddyJid, message.getType().equals(Message.Type.groupchat),
                                RainbowDeliveryReceivedReceipt.EVENT_RECEIVED);
                    }
                    Conversation conv = storeMessage(buddyJid, imMessage);

                    if(conv != null)
                        notifyImReceived(conv, imMessage);
                } else{
                    Log.getLogger().warn(LOG_TAG, "Cannot retrieve buddy in OUR Roster");
                }
            }
        } else {
            Log.getLogger().warn(LOG_TAG, " Wrong Message Type");
        }
    }


    protected void addFileDescriptorToIMMessage(RainbowOutOfBandData outOfBand, String buddyJid, IMMessage imMessage) {
        if( outOfBand != null) {
            Log.getLogger().verbose(LOG_TAG, "OOB EVENT DETECTED: ");

            final Conversation finalConv = getConversationFromJid(buddyJid);
            RainbowFileDescriptor fileDescriptor = m_fileMgr.getFileDescriptorFromUrl(outOfBand.getUrl(), new IFileProxy.IGetFileDescriptorListener() {
                @Override
                public void onGetFileDescriptorSuccess(RainbowFileDescriptor fileDescriptor) {
                    Log.getLogger().verbose(LOG_TAG, ">onGetFileDescriptorSuccess");
                    if( finalConv != null)
                        finalConv.getMessages().fireDataChanged();
                }

                @Override
                public void onGetFileDescriptorFailed(RainbowServiceException exception) {
                    Log.getLogger().warn(LOG_TAG, ">onGetFileDescriptorFailed");
                    if( finalConv != null)
                        finalConv.getMessages().fireDataChanged();
                }
            });
            if( fileDescriptor != null) {
                Log.getLogger().verbose(LOG_TAG, "setFileDescriptor");
                imMessage.setFileDescriptor(fileDescriptor);
            } else {
                // TODO manage RainbowFileDescriptor not yet available : how to do ?
                Log.getLogger().warn(LOG_TAG, "MISSING RainbowFileDescriptor for URL:"+outOfBand.getUrl());
            }
        }
    }

    private void manageCopyCarbonMessage(Message message, CarbonExtension copyCarbonExtension) {
        Log.getLogger().verbose(LOG_TAG, ">manageCopyCarbonMessage");

        Forwarded fwd = copyCarbonExtension.getForwarded();
        if (fwd == null) {
            Log.getLogger().verbose(LOG_TAG, " Forwarded extension not found");
            return;
        }

        Message fwdStanza = (Message) fwd.getForwardedPacket();
        RainbowOutOfBandData outOfBand = fwdStanza.getExtension(RainbowOutOfBandData.ELEMENT, RainbowOutOfBandData.NAMESPACE);
        CallLogPacketExtension callLogExt = fwdStanza.getExtension(CallLogPacketExtension.ELEMENT, CallLogPacketExtension.NAMESPACE);
        RainbowArchived archived =  fwdStanza.getExtension(RainbowArchived.ELEMENT, RainbowArchived.NAMESPACE);
        String foundLanguage = null;

        if (fwdStanza.getBodyLanguages().size() > 0) {
            if (fwdStanza.getBodyLanguages().contains(m_language))
                foundLanguage = m_language;
            else
                foundLanguage = fwdStanza.getBodyLanguages().get(0);
        }


        if (copyCarbonExtension.getDirection().equals(CarbonExtension.Direction.sent)) {
            Log.getLogger().verbose(LOG_TAG, "CopyCarbon SENT Message");

            if ( fwdStanza.getBody(foundLanguage) != null) {
                Log.getLogger().verbose(LOG_TAG, "CopyCarbon SENT ");

                Contact myContact = m_contactCache.getUser();
                IMMessage imMsg = new IMMessage(myContact.getImJabberId(), fwdStanza.getBody(foundLanguage), true);
                if( archived != null && !StringsUtil.isNullOrEmpty(archived.getStamp()) ){
                    imMsg.setTimeStampFromDateString(archived.getStamp());
                }
                imMsg.setMessageId(fwdStanza.getStanzaId());
                imMsg.setDeliveryState(IMMessage.DeliveryState.SENT_SERVER_RECEIVED);
                imMsg.setCallLogEvent(callLogExt, null);
                imMsg.setMsgSent(true);
                addFileDescriptorToIMMessage(outOfBand, myContact.getImJabberId(), imMsg);

                storeMessage(fwdStanza.getTo(), imMsg);
            }
        } else {
            Log.getLogger().verbose(LOG_TAG, "CopyCarbon RECEIVED Message");
            if ( fwdStanza.getBody(foundLanguage) != null) {
                Log.getLogger().verbose(LOG_TAG, "CopyCarbon RECEIVED ");

                String from = StringsUtil.getJidWithoutDevicePartAndTelPart(fwdStanza.getFrom());
                IMMessage imMsg = new IMMessage(from, fwdStanza.getBody(foundLanguage), true);
                if( archived != null && !StringsUtil.isNullOrEmpty(archived.getStamp()) ){
                    imMsg.setTimeStampFromDateString(archived.getStamp());
                }
                imMsg.setMessageId(fwdStanza.getStanzaId());
                imMsg.setDeliveryState(IMMessage.DeliveryState.RECEIVED);
                imMsg.setCallLogEvent(callLogExt, null);
                imMsg.setMsgSent(false);
                addFileDescriptorToIMMessage(outOfBand, from, imMsg);

                storeMessage(from, imMsg);
                notifyImReceived(getConversationFromJid(from), imMsg);
            }
        }

        ExtensionElement deliveryReceiptExtension = fwdStanza.getExtension(DeliveryReceipt.ELEMENT,DeliveryReceipt.NAMESPACE);
        ExtensionElement deliveryTimestampExtension = message.getExtension(RainbowDeliveryTimestampReceipt.ELEMENT, RainbowDeliveryTimestampReceipt.NAMESPACE);
        RainbowDeletedReceipt deletedExtension = (RainbowDeletedReceipt) fwdStanza.getExtension(RainbowDeletedReceipt.NAMESPACE);
        if( deliveryReceiptExtension != null && deliveryReceiptExtension instanceof RainbowDeliveryReceivedReceipt){
            RainbowDeliveryTimestampReceipt rainbowDeliveryTimestamp = null;
            if (deliveryTimestampExtension != null &&  deliveryTimestampExtension instanceof RainbowDeliveryTimestampReceipt) {
                rainbowDeliveryTimestamp = (RainbowDeliveryTimestampReceipt) deliveryTimestampExtension;
            }
            Log.getLogger().verbose(LOG_TAG, "Rainbow Delivery Receipt detected in CopyCarbon");
            setNewImMessageState((RainbowDeliveryReceivedReceipt) deliveryReceiptExtension, rainbowDeliveryTimestamp, fwdStanza);
        }
        else if (deletedExtension != null) {
            manageDeletedMessage(deletedExtension);
        }
    }


    private void processManagementMessage(ManagementReceiptPacketExtension message) {
        Log.getLogger().verbose(LOG_TAG, ">processManagementMessage");

        switch (message.getObjectManaged()) {
            case "conversation":
                Log.getLogger().verbose(LOG_TAG, "conversation item detected");
                switch (message.getManagementOperation()) {
                    case "conversation":
                        if ("delete".equalsIgnoreCase(message.getAction()) &&
                                !StringsUtil.isNullOrEmpty(message.getConversationId())) {
                            Log.getLogger().info(LOG_TAG, ">processManagementMessage deleteConversation: " + message.getConversationId());
                            Conversation conv = getConversationFromId(message.getConversationId());
                            deleteConversation(conv);
                        }
                        break;
                    default:
                        Log.getLogger().info(LOG_TAG, ">unknown management operation: " + message.toString());
                        break;
                }
                break;

            case "room":
                Log.getLogger().verbose(LOG_TAG, "room item detected");
                RoomChange roomChange = message.getRoomChange();
                IRoomMgr roomMgr = RainbowContext.getInfrastructure().getRoomMgr();
                if( RainbowContext.getInfrastructure().getRoomMgr() != null)
                    roomMgr.onRoomChange(roomChange);
                break;

            case "mute": {
                Log.getLogger().verbose(LOG_TAG, "mute item detected");
                String convId = message.getConversationIdForMuteAction();
                Conversation conversation = getConversationFromId(convId);
                if (conversation != null) {
                    Log.getLogger().verbose(LOG_TAG, "Mute received for Conversation; "+ conversation.getName());
                    conversation.setMuteValue(true);
                }
                break;
            }

            case "unmute": {
                Log.getLogger().verbose(LOG_TAG, "unmute item detected");
                String convId = message.getConversationIdForMuteAction();
                Conversation conversation = getConversationFromId(convId);
                if (conversation != null) {
                    Log.getLogger().verbose(LOG_TAG, "Unmute received for Conversation; "+ conversation.getName());
                    conversation.setMuteValue(false);
                }
                break;
            }

            case "group":
                Log.getLogger().verbose(LOG_TAG, "group item detected");
                if (message.getUserId().isEmpty() && !message.getGroupId().isEmpty()) {
                    //Action on group
                    switch (message.getAction()) {
                        case "delete":
                            RainbowContext.getInfrastructure().getGroupMgr().deleteGroup(message.getGroupId());
                            break;
                        case "create":
                            RainbowContext.getInfrastructure().getGroupMgr().addNewGroup(message.getGroupId());
                            break;
                        default:
                            break;
                    }
                } else if (!message.getGroupId().isEmpty()) {
                        RainbowContext.getInfrastructure().getGroupMgr().memberHasChanged(message.getGroupId());
                }
                break;

            case "usersettings":
                Log.getLogger().verbose(LOG_TAG, "usersetting item detected");
                RainbowContext.getInfrastructure().getContactCacheMgr().getUserSettings();
                break;

            case "userinvite":
                Log.getLogger().verbose(LOG_TAG, "userinvite item detected");
                Invitation invitation =  message.getInvitation();
                if (Invitation.SendingType.RECEIVED == invitation.getSendingType()) {
                    RainbowContext.getInfrastructure().getInvitationMgr().refreshReceivedUserInvitationList();//refresh received invitations
                } else if (Invitation.SendingType.SENT == invitation.getSendingType()) {
                    RainbowContext.getInfrastructure().getInvitationMgr().refreshSentUserInvitationList();//refresh received invitations
                }
                break;

            case "joincompanyinvite":
                Log.getLogger().verbose(LOG_TAG, "joincompanyinvite item detected");
                RainbowContext.getInfrastructure().getCompanyInvitationMgr().refreshReceivedCompanyInvitationList();//refresh received invitations
                break;

            case "joincompanyrequest":
                Log.getLogger().verbose(LOG_TAG, "compagny join request item detected");
                RainbowContext.getInfrastructure().getCompanyInvitationMgr().refreshJoinCompanyRequestList();
                break;


            default:
                break;

        }

    }

    private void processPgiConferenceInfo(PgiConferenceInfoExtension pgiConfInfoExt) {
        Log.getLogger().verbose(LOG_TAG, ">processPgiConferenceInfo: "+m_pgiMgr.getConferencesCache().size());

        for(PgiConference pgiConf : m_pgiMgr.getConferencesCache()) {
            Log.getLogger().verbose(LOG_TAG, "  PgiConference id="+pgiConf.getId());

            if( pgiConf.getId().equals(pgiConfInfoExt.getConfId())) {
                Log.getLogger().verbose(LOG_TAG, "  PgiConference found");

                boolean wasActive = pgiConf.isConfActive();
                pgiConf.update(pgiConfInfoExt);

                if (pgiConf.isConfActive() && !wasActive) {
                    Intent intent = new Intent(RainbowIntent.ACTION_RAINBOW_PGI_JOIN_SUCCESS);
                    m_applicationContext.sendBroadcast(intent);
                }
                if (!pgiConf.isConfActive() && wasActive) {
                    //de associate room from conference
                    RainbowContext.getInfrastructure().getRoomMgr().dissociatePgiConference(pgiConf.getId());
                }
            }
        }
    }

    private void manageDeletedMessage(RainbowDeletedReceipt deletedExtension) {
        Log.getLogger().verbose(LOG_TAG, ">manageDeletedMessage");

        String with = deletedExtension.getWith();
        Contact contactWith = m_contactCache.getContactFromJid(with);
        if( contactWith == null) {
            Log.getLogger().warn(LOG_TAG, "Contact not recognized - skip message");
            return;
        }

        Conversation convToClear = getConversationFromJid(with);
        if( convToClear == null) {
            Log.getLogger().warn(LOG_TAG, "Conversation not found - skip message");
            return;
        }
        deleteMessages(convToClear);

        m_conversations.fireDataChanged();

        notifyConversationsUpdated();

        IIMNotificationMgr imNotificationMgr = RainbowContext.getPlatformServices().getNotificationFactory().getIMNotificationMgr();
        Contact imNotifContact = imNotificationMgr.getLastContactNotified();
        if( contactWith.equals(imNotifContact) ) {
            Log.getLogger().verbose(LOG_TAG, "Conversation msg deleted for IMNotif detected");
            imNotificationMgr.cancelNotification();
        }
    }

    private void manageMessageMam(MamMessagePacketExtension mamExtension) {
        Log.getLogger().verbose(LOG_TAG, ">manageMessageMam");

        if (StringsUtil.isNullOrEmpty(mamExtension.getType())) {
            Log.getLogger().error(LOG_TAG, "MAM message without type");
            return;
        }

        if (mamExtension.getType().equalsIgnoreCase("chat") ) {
            manageChatMessageMam(mamExtension);
        } else if (mamExtension.getType().equalsIgnoreCase("groupchat")) {
            manageGroupChatMessageMam(mamExtension);
        }
    }

    private void manageGroupChatMessageMam(MamMessagePacketExtension mamExtension) {
        Log.getLogger().verbose(LOG_TAG, ">manageGroupChatMessageMam");

        String from;
        String roomId;
        boolean flagSent;

        from = XmppUtils.getFromJid(mamExtension.getFrom());

        IMMessage imMessage;
        RoomMultiUserChatEvent roomEvent = mamExtension.getRoomEvent();
        if (roomEvent != null) {
            Log.getLogger().verbose(LOG_TAG, "RoomEventType detected");
            imMessage = new IMMessage(roomEvent.getJid(), roomEvent.getEventType());
        } else {
            Log.getLogger().verbose(LOG_TAG, "Normal Msg detected");
            imMessage = new IMMessage(null, "", false);
            imMessage.setContactJid(from);
        }

        imMessage.setFromMaM(true);

        imMessage.setMamMessageId(mamExtension.getMamId());

        if (!StringsUtil.isNullOrEmpty(mamExtension.getMessageId()))
            imMessage.setMessageId(mamExtension.getMessageId());
        else
            imMessage.setMessageId(mamExtension.getMamId());

        if (!StringsUtil.isNullOrEmpty(mamExtension.getBody())) {
            imMessage.setMessageContent(mamExtension.getBody());
        }

        roomId = XmppUtils.getRoomJid(mamExtension.getFrom());

        flagSent = (from.equalsIgnoreCase(m_userJid));

        if (mamExtension.getStampLong() != null) {
            imMessage.setTimeStamp(mamExtension.getStampLong());
        }
        if( mamExtension.getReceivedDate() != null) {
            imMessage.setMessageDateReceived(mamExtension.getReceivedDate());
        }
        if( mamExtension.getReadDate() != null) {
            imMessage.setMessageDateRead(mamExtension.getReadDate());
        }
        addFileDescriptorToIMMessage(mamExtension.getOobEvent(), roomId, imMessage);

        if (flagSent) {
            Log.getLogger().verbose(LOG_TAG, "Flag is Sent");
            imMessage.setDeliveryState(IMMessage.DeliveryState.SENT_SERVER_RECEIVED);

            imMessage.setContactJid(from);
        } else {
            Log.getLogger().verbose(LOG_TAG, "Flag is Received");
            imMessage.setDeliveryState(IMMessage.DeliveryState.RECEIVED);
            if (mamExtension.isRead()) {
                Log.getLogger().verbose(LOG_TAG, "Message id " + imMessage.getMessageId() + " is Received and Read");
                imMessage.setDeliveryState(IMMessage.DeliveryState.READ);
            }
        }

        imMessage.setMsgSent(flagSent);

        if (roomId != null) {
            storeMessage(roomId, imMessage);
        } else {
            Log.getLogger().warn(LOG_TAG, "otherContact is NULL");
        }
    }

    private void manageChatMessageMam(MamMessagePacketExtension mamExtension) {
        Log.getLogger().verbose(LOG_TAG, ">manageChatMessageMam");

        IMMessage imMessage = new IMMessage(null, "", false);

        imMessage.setFromMaM(true);

        imMessage.setMamMessageId(mamExtension.getMamId());

        if (!StringsUtil.isNullOrEmpty(mamExtension.getMessageId()))
            imMessage.setMessageId(mamExtension.getMessageId());
        else
            imMessage.setMessageId(mamExtension.getMamId());

        if (!StringsUtil.isNullOrEmpty(mamExtension.getBody())) {
            imMessage.setMessageContent(mamExtension.getBody());
        }

        String otherContactJid;
        String from = "";
        if( mamExtension.getFrom() != null)
            from = XmppUtils.parseBareJid(mamExtension.getFrom());
        String to = "";
        if( mamExtension.getTo() != null)
            to = XmppUtils.parseBareJid(mamExtension.getTo());
        boolean flagSent = (from.equalsIgnoreCase(m_userJid));
        if (flagSent) {
            otherContactJid = to;
        } else {
            otherContactJid = from;
        }
        imMessage.setContactJid(otherContactJid);

        if (mamExtension.isRead()) {
            imMessage.setMessageDateRead(mamExtension.getReadDate());
        }

        if (mamExtension.getStampLong() != null) {
            imMessage.setTimeStamp(mamExtension.getStampLong());
        }
        if( mamExtension.getReceivedDate() != null) {
            imMessage.setMessageDateReceived(mamExtension.getReceivedDate());
        }
        if( mamExtension.getReadDate() != null) {
            imMessage.setMessageDateRead(mamExtension.getReadDate());
        }
        imMessage.setCallLogEvent(mamExtension.getCallLogEvent(), m_userJid);
        addFileDescriptorToIMMessage(mamExtension.getOobEvent(), otherContactJid, imMessage);

        if (imMessage.getCallLogEvent() != null) {
            Log.getLogger().verbose(LOG_TAG, "callLog detected");
        }

        if (flagSent) {
            Log.getLogger().verbose(LOG_TAG, "Flag is Sent");
            if (mamExtension.isRead()) {
                imMessage.setDeliveryState(IMMessage.DeliveryState.SENT_CLIENT_READ);
            } else if (mamExtension.isReceived()) {
                imMessage.setDeliveryState(IMMessage.DeliveryState.SENT_CLIENT_RECEIVED);
            } else {
                imMessage.setDeliveryState(IMMessage.DeliveryState.SENT_SERVER_RECEIVED);
            }
        } else {
            Log.getLogger().verbose(LOG_TAG, "Flag is Received");
            imMessage.setDeliveryState(IMMessage.DeliveryState.RECEIVED);
            if (mamExtension.isRead()) {
                Log.getLogger().verbose(LOG_TAG, "Message id " + imMessage.getMessageId() + " is Received and Read");
                imMessage.setDeliveryState(IMMessage.DeliveryState.READ);
            } else if ( mamExtension.isRequested() ) {
                Log.getLogger().verbose(LOG_TAG, "Message Id " + imMessage.getMessageId() + " is requested => Send Ack");
                sendMsgReceiptAck(mamExtension.getMessageId(), mamExtension.getFrom(), mamExtension.getType().equalsIgnoreCase("groupchat"), RainbowDeliveryReceivedReceipt.EVENT_RECEIVED);
            }
        }

        imMessage.setMsgSent(flagSent);

        if (otherContactJid != null) {
            storeMessage(otherContactJid, imMessage);
        } else {
            Log.getLogger().warn(LOG_TAG, "otherContact is NULL");
        }
    }

    private void changeIMMessageForTransferFile(IMMessage imMessage) {
        String originalMsgContent = imMessage.getMessageContent().replace(BODY_FILETRANSFER_TAG,"");
        String[] msgSplitted = originalMsgContent.split("\\|\\|");

        if( msgSplitted.length >= 6) {
            StringBuilder newMsg = new StringBuilder();
            newMsg.append(msgSplitted[0]);
            newMsg.append("\n");
            newMsg.append(msgSplitted[1]);
            newMsg.append(" ( ");
            float fileSize = Float.valueOf(msgSplitted[2]);
            if(fileSize < 1024 ) {
                newMsg.append(String.valueOf(fileSize));
                newMsg.append("B )");
            } else {
                newMsg.append(String.format(Locale.getDefault(), "%.1f", fileSize/(1024*1024)));
                newMsg.append("MB )");
            }
            newMsg.append("\n");
            newMsg.append(msgSplitted[4]);

            imMessage.setMessageContent(newMsg.toString());
        }
    }

    void setNewImMessageState(RainbowDeliveryReceivedReceipt deliveryReceipt, RainbowDeliveryTimestampReceipt deliveryTimestamp, Message message) {
        Log.getLogger().verbose(LOG_TAG, ">setNewImMessageState");
        Conversation conversation;
        String deliveryEvent = deliveryReceipt.getEvent();
        String deliveryEntity = deliveryReceipt.getEntity();
        Date deliveryTimestampValue = null;
        if (deliveryTimestamp != null) {
            deliveryTimestampValue = deliveryTimestamp.getTimestampValueInDateFormat();
        }


        Pair<Conversation, IMMessage> result = findMessageFromMsgId(deliveryReceipt.getId());
        if (result == null) {
            Log.getLogger().verbose(LOG_TAG, "Message not found; " + deliveryReceipt.getId());
            if (deliveryEntity.equalsIgnoreCase(RainbowDeliveryReceivedReceipt.ENTITY_CLIENT) &&
                    deliveryEvent.equalsIgnoreCase(RainbowDeliveryReceivedReceipt.EVENT_READ)) {
                String contactJid = StringsUtil.getJidWithoutDevicePartAndTelPart(message.getTo());

                conversation = getConversationFromJid(contactJid);
                if(conversation == null) {
                    Log.getLogger().warn(LOG_TAG, "Conversation not found; " + deliveryReceipt.getId());
                    return;
                }
                Log.getLogger().verbose(LOG_TAG, "Decrease counter from conversation; " + conversation.getContact().getDisplayName(""));
                conversation.decrementUnreadCounter();
                notifyConversationsUpdated();
            }

            return;
        }


        conversation = result.first;
        IMMessage imMessage = result.second;
        Log.getLogger().verbose(LOG_TAG, "Found msg with DeliveryReceiptId; " + imMessage.getMessageId());

        Log.getLogger().verbose(LOG_TAG, "     Event; " + deliveryEvent);
        Log.getLogger().verbose(LOG_TAG, "     Entity; " + deliveryEntity);
        if (!StringsUtil.isNullOrEmpty(deliveryEvent) &&
                !StringsUtil.isNullOrEmpty(deliveryEntity)) {
            Log.getLogger().verbose(LOG_TAG, "     Switching IMMessage delivery State; " + deliveryEvent);

            if( imMessage.getMessageDate() == null ) {
                imMessage.setTimeStamp(deliveryTimestamp.getTimestamp());
            }

            if( imMessage.isMsgSent()) {
                Log.getLogger().verbose(LOG_TAG, "Changing SENT Message State");
                if (deliveryEntity.equalsIgnoreCase(RainbowDeliveryReceivedReceipt.ENTITY_SERVER) &&
                        deliveryEvent.equalsIgnoreCase(RainbowDeliveryReceivedReceipt.EVENT_RECEIVED)) {
                    Log.getLogger().verbose(LOG_TAG, "     SERVER has RECEIVED the message");
                    if (imMessage.getDeliveryState().equals(IMMessage.DeliveryState.SENT_CLIENT_RECEIVED) ||
                            imMessage.getDeliveryState().equals(IMMessage.DeliveryState.SENT_CLIENT_READ)) {
                        Log.getLogger().debug(LOG_TAG, "Dont set Message State to SENT_SERVER_STATE, we already have better State");
                        return;
                    }
                    imMessage.setDeliveryState(IMMessage.DeliveryState.SENT_SERVER_RECEIVED);
                    if (conversation.getType().equals(ConversationType.ROOM)) {
                        sendMsgReceiptAck(imMessage.getMessageId(), conversation.getRoom().getJid(), conversation.isRoomType(),
                                RainbowDeliveryReceivedReceipt.EVENT_RECEIVED);
                        sendMsgReceiptAck(imMessage.getMessageId(), conversation.getRoom().getJid(), conversation.isRoomType(),
                                RainbowDeliveryReceivedReceipt.EVENT_READ);
                    }
                    conversation.getMessages().fireDataChanged();
                } else if (deliveryEntity.equalsIgnoreCase(RainbowDeliveryReceivedReceipt.ENTITY_CLIENT) &&
                        deliveryEvent.equalsIgnoreCase(RainbowDeliveryReceivedReceipt.EVENT_RECEIVED)) {
                    Log.getLogger().verbose(LOG_TAG, "     CLIENT has RECEIVED the message");
                    if (imMessage.getDeliveryState().equals(IMMessage.DeliveryState.SENT_CLIENT_READ)) {
                        Log.getLogger().debug(LOG_TAG, "Dont set Message State to SENT_CLIENT_RECEIVED, we already have better State");
                        return;
                    }
                    if (conversation.getType() != ConversationType.ROOM) {
                        imMessage.setDeliveryState(IMMessage.DeliveryState.SENT_CLIENT_RECEIVED);
                    }
                    else
                        imMessage.setDeliveryState(IMMessage.DeliveryState.SENT_SERVER_RECEIVED);
                    conversation.getMessages().fireDataChanged();
                } else if (deliveryEntity.equalsIgnoreCase(RainbowDeliveryReceivedReceipt.ENTITY_CLIENT) &&
                        deliveryEvent.equalsIgnoreCase(RainbowDeliveryReceivedReceipt.EVENT_READ)) {
                    Log.getLogger().verbose(LOG_TAG, "     CLIENT has READ the message");
                    if (conversation.getType() != ConversationType.ROOM) {
                        imMessage.setMessageDateRead(deliveryTimestampValue);
                        imMessage.setDeliveryState(IMMessage.DeliveryState.SENT_CLIENT_READ);
                    }
                    else
                        imMessage.setDeliveryState(IMMessage.DeliveryState.SENT_SERVER_RECEIVED);
                    conversation.getMessages().fireDataChanged();
                }

            }
            else {
                Log.getLogger().verbose(LOG_TAG, "Changing RECEIVED Message State");

                if (deliveryEntity.equalsIgnoreCase(RainbowDeliveryReceivedReceipt.ENTITY_CLIENT) &&
                        deliveryEvent.equalsIgnoreCase(RainbowDeliveryReceivedReceipt.EVENT_READ)) {
                    Log.getLogger().verbose(LOG_TAG, "     CLIENT has READ the message");
                    imMessage.setDeliveryState(IMMessage.DeliveryState.READ);

                    if (conversation.isChatType() ||
                            (conversation.isRoomType() && XmppUtils.getFromJid(message.getFrom()).equalsIgnoreCase(m_userJid))) {
                        //Chat or room message from me => decrement
                        conversation.decrementUnreadCounter();

                        IIMNotificationMgr imNotificationMgr = RainbowContext.getPlatformServices().getNotificationFactory().getIMNotificationMgr();
                        if( imNotificationMgr != null)
                            imNotificationMgr.setImReadState(imMessage);
                    }
                    conversation.getMessages().fireDataChanged();
                }
            }
            if (chatDataSource != null)
                chatDataSource.updateDeliveryState(conversation.getJid(),imMessage);
            notifyConversationsUpdated();
        }
    }

    Pair<Conversation, IMMessage> findMessageFromMsgId(String messageId) {
        if (StringsUtil.isNullOrEmpty(messageId)) {
            Log.getLogger().warn(LOG_TAG, ">findMessageFromMsgId; messageId is NULL");
            return null;
        }

        for (Conversation conv : m_conversations.getCopyOfDataList()) {
            for (IMMessage msg : conv.getMessages().getCopyOfDataList()) {
                if (!StringsUtil.isNullOrEmpty(msg.getMessageId()) && msg.getMessageId().equals(messageId)) {
                    return new Pair<>(conv, msg);
                }
            }
        }
        return null;
    }



    /**
     * store instant message in the local cache
     *
     * @param contactJid (String) : person related to this chat
     * @param message    (IMMessage) : the message
     */
    public Conversation storeMessage(String contactJid, IMMessage message) {
        contactJid = StringsUtil.getJidWithoutDevicePartAndTelPart(contactJid);
        String body = "";
        if (!StringsUtil.isNullOrEmpty(message.getMessageContent())) {
            body = message.getMessageContent().charAt(0) + "xxxx" + message.getMessageContent().charAt(message.getMessageContent().length()-1);

            if (message.getMessageContent().startsWith(BODY_FILETRANSFER_TAG)) {
                Log.getLogger().verbose(LOG_TAG, "Changing Msg Content for File Transfer");
                changeIMMessageForTransferFile(message);
            }
        }

        Log.getLogger().debug(LOG_TAG, ">storeMessage Id: " + message.getMessageId() + " Body: " + body + " Jid: " + contactJid);



        Conversation conv = getConversationFromJid(contactJid);
        if (conv == null) {
            Log.getLogger().debug(LOG_TAG, "Contact Jid " + contactJid + " is NOT in Conversation List");
            conv = createNewConversationFromJid(contactJid, null);
        }

        if(conv.isMsgAlreadyStored(message))
        {
            return null;
        }
        else
        {
            conv.addMessage(message);
            conv.setLastMessage(message);
            if (chatDataSource != null)
                chatDataSource.createOrUpdateChat(contactJid, message);

            if(conversationDataSource != null)
                conversationDataSource.createOrUpdateConversation(conv);

            sortConversations();

            return conv;
        }
    }

    void sortConversations()
    {
        Log.getLogger().verbose(LOG_TAG, ">sortConversations");

        List<Conversation> conversations = m_conversations.getCopyOfDataList();
        Collections.sort(conversations, new Comparator<Conversation>() {
            @Override
            public int compare(Conversation conv1, Conversation conv2) {

                if (conv1 == null && conv2 == null)
                    return 0;
                if (conv1 == null || conv1.getLastMessage() == null)
                    return 1;
                if (conv2 == null || conv2.getLastMessage() == null)
                    return -1;

                Long msg1TimeStamp = conv1.getLastMessage().getTimeStamp();
                Long msg2TimeStamp = conv2.getLastMessage().getTimeStamp();

                return msg2TimeStamp.compareTo(msg1TimeStamp);
            }
        });

        m_conversations.replaceAll(conversations);
    }

    public Conversation createNewConversationFromJid(String contactJid, IConversationProxy.ICreateConversationListener listener) {
        Conversation conv;
        Contact contact = m_contactCache.getContactFromJid(contactJid);
        if (contact == null) {
            Log.getLogger().verbose(LOG_TAG, "Contact for conversation doesn't exist - create it");
            DirectoryContact dirContact = new DirectoryContact();
            dirContact.setImJabberId(contactJid);

            contact = m_contactCache.createContactIfNotExistOrUpdate(dirContact);
        }
        conv = new Conversation(contact);

        addConversation(conv, null, listener);
        return conv;
    }

    public Conversation createNewConversationFromRoom(Room room,IConversationProxy.ICreateConversationListener listener) {
        Conversation conv;

        conv = new Conversation(room);
        conv.setName(room.getName());
        conv.setPeerId(room.getId());
        conv.setTopic(room.getTopic());

        addConversation(conv, room, listener);
        return conv;
    }

    private void addConversation(final Conversation conv, final Room room, final IConversationProxy.ICreateConversationListener listener) {

        Conversation existingConversation = getConversationFromId(conv.getId());
        if (existingConversation == null)
            existingConversation = getConversationFromJid(conv.getJid());

        if (existingConversation == null) {
            m_conversations.add(conv);
            conv.notifyConversationAdded();
            notifyConversationsUpdated();
            sortConversations();
        }
        else
        {
            existingConversation.update(conv);
        }

        RainbowContext.getInfrastructure().getConversationProxy().createConversation(conv, new IConversationProxy.ICreateConversationListener() {
            @Override
            public void onCreationSuccess(String id) {
                Log.getLogger().debug(LOG_TAG, "createConversation onCreationSuccess");
                conv.setId(id);
                if( room != null)
                    room.setConversationId(id);
                if (conversationDataSource != null)
                    conversationDataSource.createOrUpdateConversation(conv);

                if( listener != null)
                    listener.onCreationSuccess(id);
            }

            @Override
            public void onCreationError() {
                Log.getLogger().warn(LOG_TAG, "createConversation onCreationError");

                if( listener != null)
                    listener.onCreationError();
            }
        });
        if (conversationDataSource != null)
            conversationDataSource.createOrUpdateConversation(conv);
    }

    /**
     * get all messages exchanged with the speaker
     */
    @Override
    public void refreshMessages(Conversation conversation, int nbMessagesToRetrieve, IMamNotification iMamNotification) {
        if (conversation == null) {
            return;
        }

        refreshMessages(conversation, null, conversation.getContact().getImJabberId(), nbMessagesToRetrieve, iMamNotification);
    }

    void refreshMessages(Conversation conversation, final String to, final String with, final int nbMessagesToRetrieve, IMamNotification iMamNotification) {
        if (conversation == null) {
            Log.getLogger().warn(LOG_TAG, "refreshMessage ; convId is NULL");

            if (iMamNotification != null)
                iMamNotification.complete(new ArrayItemList<IMMessage>(),true);
            Log.getLogger().warn(LOG_TAG, "refreshMessage ; conv  is NULL");
            return;
        }

        m_mamNotif = iMamNotification;
        Conversation c = getConversationFromJid(conversation.getJid());
        if (c == null)
            c = getConversationFromJid(conversation.getId());

        final Conversation conv = c;
        if (RainbowContext.getInfrastructure().isXmppConnected() && conv != null) {
            Thread myThread = new Thread() {
                public void run() {
                    try {
                        conv.setMamInProgress(true);

                        StanzaListener packetListener = new StanzaListener() {
                            @Override
                            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                                MamIQResult iqResult = (MamIQResult) packet;

                                conv.setMamInProgress(false);

                                if (conversationDataSource != null)
                                    conversationDataSource.synchroDB(conv);
                                ArrayItemList<IMMessage> messages = conv.getMessages();
                                if (messages != null && messages.getCount() > 0)
                                {
                                    conv.setLastMessage(messages.get(messages.getCount() - 1));
                                }
                                conv.setFirstMamDone(true);
                                m_mamNotif.complete(messages, iqResult.isComplete());

                                m_connection.removeSyncStanzaListener(this);
                            }
                        };

                        m_connection.addSyncStanzaListener(packetListener, new StanzaTypeFilter(MamIQResult.class));
                        m_mamMgr.getArchivedMessages(to, with, nbMessagesToRetrieve);
                    } catch (Exception e) {
                        Log.getLogger().error(LOG_TAG, "refreshMessages; Exception=" + e);
                        m_mamNotif.error(e);
                    }
                }
            };
            myThread.start();
        } else {
            Log.getLogger().verbose(LOG_TAG, ">refreshMessages not xmpp connected; " + to);
            if (conv ==null )
                m_mamNotif.complete(new ArrayItemList<IMMessage>(), true);
        }
    }

    @Override
    public void refreshMoreMessages(final String jid, final String with, final String lastMamImId, final int nbMessagesToRetrieve, IMamNotification iMamNotification) {
        Log.getLogger().verbose(LOG_TAG, ">refreshMoreMessages; " + jid);

        m_mamNotif = iMamNotification;
        if (RainbowContext.getInfrastructure().isXmppConnected()) {

            Thread myThread = new Thread() {
                public void run() {
                    try {
                        final Conversation conv = getConversationFromJid(jid);
                        if (conv != null) {
                            conv.setMamInProgress(true);

                            StanzaListener packetListener = new StanzaListener() {
                                @Override
                                public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                                    MamIQResult iqResult = (MamIQResult) packet;

                                    conv.setMamInProgress(false);

                                    ArrayItemList<IMMessage> messages = conv.getMessages();
                                    m_mamNotif.complete(messages, iqResult.isComplete());

                                    m_connection.removeSyncStanzaListener(this);
                                }
                            };

                            m_connection.addSyncStanzaListener(packetListener, new StanzaTypeFilter(MamIQResult.class));
                            m_mamMgr.getArchivedMessages(jid, with, nbMessagesToRetrieve, lastMamImId);
                        }
                    } catch (Exception e) {
                        m_mamNotif.timeout();
                    }
                }
            };
            myThread.start();
        } else {
            Log.getLogger().verbose(LOG_TAG, ">refreshMessages not xmpp connected; " + jid);
            Conversation conv = getConversationFromJid(jid);
            if (conv != null)
                m_mamNotif.complete(conv.getMessages(), true);
            else
                m_mamNotif.complete(new ArrayItemList<IMMessage>(), true);
        }
    }

    @Override
    public void deleteAllMessages(final Conversation conv, IMamNotification iMamNotification) {
        if( conv == null || conv.getContact() == null ) {
            Log.getLogger().warn(LOG_TAG, ">deleteAllMessages; skipped wrong parameter");
            return;
        }
        Log.getLogger().verbose(LOG_TAG, ">refreshMoreMessages; " + conv.getContact().getDisplayName(""));

        m_mamNotif = iMamNotification;

        Thread myThread = new Thread() {
            public void run() {
                try {
                    MamIQResult iqResult = m_mamMgr.deleteArchivedConversation(conv.getContact().getImJabberId(), null);
                    deleteMessages(conv);
                    notifyConversationsUpdated();

                    if (m_mamNotif != null)
                        m_mamNotif.complete(conv.getContact().getImJabberId(), iqResult.isComplete());
                } catch (Exception e) {
                    Log.getLogger().info(LOG_TAG, "deleteAllMessages Exception" + e);
                    if (m_mamNotif != null)
                        m_mamNotif.timeout();
                }
            }
        };
        myThread.start();
    }

    public void deleteConversation(Conversation conv) {
        if (conv != null) {
            if (conversationDataSource != null)
                conversationDataSource.deleteConversation(conv);
            m_conversations.delete(conv);
            notifyConversationsUpdated();
        }
    }

    private void deleteMessages(Conversation conv) {
        if (conv != null) {
            if (conversationDataSource != null)
                conversationDataSource.deleteMessages(conv);
            conv.getMessages().clear();
            conv.setLastMessage(null);
            conv.setUnreadMsgNb(0);
        }
    }

    private void deleteConversation(final Conversation conv, final IConversationProxy.IDeleteConversationListener listener) {
        if (conv == null) {
            Log.getLogger().verbose(LOG_TAG, "can not delete conversation because it does not exist");
            return;
        }
        Log.getLogger().verbose(LOG_TAG, "delete conversation : " + conv.toString());

        RainbowContext.getInfrastructure().getConversationProxy().deleteConversation(conv.getId(), new IConversationProxy.IDeleteConversationListener() {
            @Override
            public void onDeletionSuccess() {
                Log.getLogger().info(LOG_TAG, ">onDeletionSuccess");
                deleteConversation(conv);
                conv.notifyConversationDeleted();
                if( listener != null)
                    listener.onDeletionSuccess();
            }

            @Override
            public void onDeletionError() {
                Log.getLogger().warn(LOG_TAG, ">onDeletionError");
                if( listener != null)
                    listener.onDeletionError();
            }
        });

        if( conv.getContact() != null) {
            String imJabberId = conv.getContact().getImJabberId();
            Chat chat = m_poolChat.get(imJabberId);
            if (chat != null) {
                Log.getLogger().verbose(LOG_TAG, "Close Chat");
                m_poolChat.remove(imJabberId);
                chat.close();
            }
        }
    }


    public void deleteConversations(final List<Conversation> convList, final IConversationProxy.IDeleteConversationListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">deleteConversations");
        if ( convList == null || convList.size() == 0) {
            Log.getLogger().warn(LOG_TAG, "No conversation to delete given");
            return;
        }
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                final CountDownLatch remainingRequest = new CountDownLatch(convList.size());

                final Integer[] requestSuccessCounter = {0};

                for (Conversation conv : convList) {

                    deleteConversation(conv, new IConversationProxy.IDeleteConversationListener() {
                        @Override
                        public void onDeletionSuccess() {
                            Log.getLogger().verbose(LOG_TAG, ">onDeletionSuccess");
                            requestSuccessCounter[0]++;
                            remainingRequest.countDown();
                        }

                        @Override
                        public void onDeletionError() {
                            Log.getLogger().warn(LOG_TAG, ">onDeletionError");
                            remainingRequest.countDown();
                        }
                    });
                }

                try {
                    Log.getLogger().verbose(LOG_TAG, "before await");
                    remainingRequest.await(10, TimeUnit.SECONDS);
                    Log.getLogger().verbose(LOG_TAG, "after await");
                } catch (InterruptedException e) {
                    Log.getLogger().error(LOG_TAG, "Exception while waiting: "+e.getMessage());
                }

                if(requestSuccessCounter[0] == convList.size()) {
                    if (listener != null)
                        listener.onDeletionSuccess();
                } else {
                    if (listener != null)
                        listener.onDeletionError();
                }
            }
        });
        myThread.start();
    }

    @Override
    public void sendIsTypingState(Conversation conversation, ChatState state) {
        Log.getLogger().verbose(LOG_TAG, ">sendIsTypingState");

        if (conversation != null) {
            Chat chat = m_poolChat.get(conversation.getJid());
            if (chat == null) {
                chat = ChatManager.getInstanceFor(m_connection).createChat(conversation.getJid(), this);
            }

            try {
                ChatStateManager.getInstance(m_connection).setCurrentState(state, chat);
            } catch (SmackException.NotConnectedException e) {
                onSendTypingStateError();
            }
        }
    }

    private void onSendTypingStateError() {
        Log.getLogger().warn(LOG_TAG, ">onSendTypingStateError");

        // TODO giro : to be removed
        Intent intent = new Intent(XmppIntent.SEND_MESSAGE_FAILED);
        intent.setPackage(m_applicationContext.getPackageName());
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);

        m_applicationContext.sendBroadcast(intent);
    }

    /**
     * send instant message through xmpp
     * @param message   (IMMessage) : message
     * @param fileDescriptor : the fileDescriptor
     * @param conversation (Conversation) : the conversation
     */
    @Override
    public void sendMessage(IMMessage message, RainbowFileDescriptor fileDescriptor, Conversation conversation) {
        sendMessage(message, fileDescriptor, conversation.getJid());
    }

    private void sendMessage(IMMessage message, RainbowFileDescriptor fileDescriptor, String convJid) {
        Log.getLogger().debug(LOG_TAG, ">sendMessage");

        if (!StringsUtil.isNullOrEmpty(convJid)) {
            Chat chat = m_poolChat.get(convJid);
            if (chat == null) {
                chat = ChatManager.getInstanceFor(m_connection).createChat(convJid, this);
            }

            try {
                Message smackMsg = new Message(convJid);
                smackMsg.setType(Message.Type.chat);
                smackMsg.setLanguage(m_language);
                smackMsg.setBody(message.getMessageContent());
                if( message.getMessageContent().isEmpty()) {
                    smackMsg.addExtension(new StoreMessagePacketExtension());
                }
                if (!StringsUtil.isNullOrEmpty(message.getMessageId()))
                    smackMsg.setStanzaId(message.getMessageId());

                DeliveryReceiptRequest.addTo(smackMsg);
                if( fileDescriptor != null) {
                    message.setFileDescriptor(fileDescriptor);

                    RainbowOutOfBandData oobExt = new RainbowOutOfBandData(fileDescriptor.getFileUrl(),
                            fileDescriptor.getTypeMIME(),
                            fileDescriptor.getFileName(),
                            fileDescriptor.getSize());
                    smackMsg.addExtension(oobExt);
                }

                chat.sendMessage(smackMsg);

                message.setDeliveryState(IMMessage.DeliveryState.SENT);
                message.setMessageId(smackMsg.getStanzaId());

                storeMessage(convJid, message);
                Conversation conv = getConversationFromJid(convJid);
                notifyImSent(conv);
                conv.getMessages().fireDataChanged();
            } catch (SmackException.NotConnectedException e) {
                onSendMessageError();
            }
        }
    }

    public void onSendMessageError() {
        Log.getLogger().warn(LOG_TAG, ">onSendMessageError");

        // TODO giro : to be removed
        Intent intent = new Intent(XmppIntent.SEND_MESSAGE_FAILED);
        intent.setPackage(m_applicationContext.getPackageName());
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);

        m_applicationContext.sendBroadcast(intent);
    }

    private void notifyConversationsUpdated() {
        Log.getLogger().verbose(LOG_TAG, ">notifyConversationsUpdated");
        synchronized (m_changeListeners) {
            for (IChatMgrListener listener : m_changeListeners) {
                listener.onConversationsUpdated();
            }
        }
    }

    public void notifyImReceived(Conversation conversation, IMMessage imMessage) {
        Log.getLogger().verbose(LOG_TAG, ">notifyImReceived");
        synchronized (m_changeListeners) {
            for (IChatMgrListener listener : m_changeListeners) {
                listener.onImReceived(conversation, imMessage);
            }
        }
    }

    private void notifyIsTyping(Contact contact, boolean isTyping) {
        Log.getLogger().verbose(LOG_TAG, ">notifyIsTyping");
        synchronized (m_changeListeners) {
            for (IChatMgrListener listener : m_changeListeners) {
                listener.isTypingState(contact, isTyping, null);
            }
        }
    }

    void notifyIsTypingInRoom(Contact contact, boolean isTyping, String roomId) {
        Log.getLogger().verbose(LOG_TAG, ">notifyIsTypingInRoom");
        synchronized (m_changeListeners) {
            for (IChatMgrListener listener : m_changeListeners) {
                listener.isTypingState(contact, isTyping, roomId);
            }
        }
    }

    void notifyImSent(Conversation conversation) {
        Log.getLogger().debug(LOG_TAG, ">notifyImSent");
        synchronized (m_changeListeners) {
            for (IChatMgrListener listener : m_changeListeners) {
                listener.onImSent(conversation);
            }
        }
    }

    public void onConversationsAdded(List<Conversation> conversations) {
        Log.getLogger().verbose(LOG_TAG, ">onConversationsAdded: "+conversations.size());

        List<Conversation> currentConversations = m_conversations.getCopyOfDataList();

        for (Conversation conv : currentConversations) {
            conv.setFromServer(false);
        }

        for (final Conversation conversation : conversations)
        {
            String last ="No message";
            if (conversation.getLastMessage()!= null)
                last = conversation.getLastMessage().getMessageContent();
            Log.getLogger().info(LOG_TAG, "  conversation Jid =" + conversation.getJid() + " Id: " + conversation.getId() + " last message :" + last );

            if (conversation.isChatType()) {
                Contact contact = m_contactCache.createContactIfNotExistOrUpdate(conversation.getContact().getDirectoryContact());

                if (contact == null) {
                    Log.getLogger().info(LOG_TAG, "  conversation contact is NULL");
                }
                conversation.setContact(contact);
            }

            conversation.setFromServer(true);


            Conversation existingConversation = getConversationFromId(conversation.getId());
            if (existingConversation == null) {
                existingConversation = getConversationFromJid(conversation.getJid());

                if (existingConversation == null) {
                    currentConversations.add(conversation);
                    conversation.notifyConversationAdded();
                    existingConversation = conversation;
                } else {
                    Log.getLogger().verbose(LOG_TAG, ">onConversationsAdded existingConversation");
                    existingConversation.update(conversation);
                }
            } else {
                existingConversation.update(conversation);
            }


            if (existingConversation.isRoomType()) {
                MultiUserChatMgr multiUserChatMgr = RainbowContext.getInfrastructure().getMultiUserChatMgr();
                if( multiUserChatMgr != null) {
                    final Conversation finalExistingConversation = existingConversation;
                    multiUserChatMgr.findOrCreateMucFromServer(existingConversation, new IRoomProxy.IGetRoomDataListener() {
                        @Override
                        public void onGetRoomDataSuccess(Room room) {
                            if (conversationDataSource != null)
                                conversationDataSource.createOrUpdateConversation(finalExistingConversation);
                            Log.getLogger().debug(LOG_TAG, "Room correctly retrieved " + room.getId());
                        }

                        @Override
                        public void onGetRoomDataFailure(String roomId) {
                            Log.getLogger().warn(LOG_TAG, "Room retrieved failed " + roomId);
                            deleteConversation(finalExistingConversation);
                            sortConversations();
                        }
                    });
                }
            } else {
                if (conversationDataSource != null)
                    conversationDataSource.createOrUpdateConversation(existingConversation);
            }
        }

        m_conversations.replaceAllWithoutNotification(currentConversations);

        for (Conversation conv : m_conversations.getCopyOfDataList()) {

            if (!conv.isFromServer()) {
                //Conversation was destroyed on server side remove it from DB
                Log.getLogger().verbose(LOG_TAG, "delete conversation and sync db : " + conv.toString());
                deleteConversation(conv);
            }
            if (StringsUtil.isNullOrEmpty(conv.getId())) {
                // Conversation was created offline recreate it and resent offline IM
                addConversation(conv, null, null);
            }
        }

        sortConversations();
        notifyConversationsUpdated();
    }

    @Override
    public Conversation getConversationFromJid(String jid) {
        if (StringsUtil.isNullOrEmpty(jid)) {
            Log.getLogger().warn(LOG_TAG, "Jid given is NULL/Empty");
            return null;
        }

        for (Conversation conv : m_conversations.getCopyOfDataList()) {
            if ( conv.getJid() != null && conv.getJid().equalsIgnoreCase(jid)) {
                return conv;
            }
        }
        return null;
    }

    public Conversation getConversationFromId(String id) {
        if (StringsUtil.isNullOrEmpty(id)) {
            Log.getLogger().warn(LOG_TAG, "Conversation Id given is NULL/Empty");
            return null;
        }

        for (Conversation conv : m_conversations.getCopyOfDataList()) {
            if (conv.getId().equalsIgnoreCase(id)) {
                return conv;
            }
        }
        return null;
    }


    public Conversation getConversationWithContact(Contact contact) {
        if (contact == null || !contact.isCorporate()) {
            Log.getLogger().warn(LOG_TAG, "Contact is not correct");
            return null;
        }

        for (Conversation conv : m_conversations.getCopyOfDataList()) {
            if (conv.getContact().equals(contact)) {
                return conv;
            }
        }
        return null;
    }

    /**
     * get all Conversations
     *
     * @return ArrayItemList<Conversation>
     */
    public ArrayItemList<Conversation> getConversations() {
        Log.getLogger().verbose(LOG_TAG, ">getConversations");

        return m_conversations;
    }

    @Override
    public void registerChangeListener(IChatMgrListener changeListener) {
        synchronized (m_changeListeners) {
            m_changeListeners.add(changeListener);
        }
    }

    @Override
    public void unregisterChangeListener(IChatMgrListener changeListener) {
        synchronized (m_changeListeners) {
            m_changeListeners.remove(changeListener);
        }
    }

    @Override
    public void sendMessagesReadDelivery(final Conversation conversation) {
        Log.getLogger().verbose(LOG_TAG, ">sendMessagesReadDelivery");
        if( !m_dataNetworkMonitor.isDataNetworkAvailable() || !m_connection.isConnected() ) {
            Log.getLogger().debug(LOG_TAG, "Not Connected or No DataNetwork Available - skip");
            return;
        }

        Thread deliveryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (conversation != null && conversation.getMessages() != null ) {

                    for (IMMessage imMessage : conversation.getMessages().getCopyOfDataList()) {
                        if (!imMessage.isMsgSent()) {
                            //if (conversation.getType().equals(ConversationType.ROOM)) {
                            //    sendMsgReceiptAck(imMessage.getMessageId(), conversation.getRoom().getJid(), conversation.isRoomType(),
                            //            RainbowDeliveryReceivedReceipt.EVENT_READ);
                            //}
                            Log.getLogger().verbose(LOG_TAG, "Sending Msg Read Acknowledgement; " + imMessage.getMessageId());
                            if (imMessage.getDeliveryState().equals(IMMessage.DeliveryState.READ) ) {
                                Log.getLogger().verbose(LOG_TAG, "    We have already sent READ STATE for this Message");
                                continue;
                            }

                            imMessage.setDeliveryState(IMMessage.DeliveryState.READ);

                            String recipientMsg = imMessage.getContactJid();
                            if(conversation.getRoom() != null) {
                                recipientMsg = conversation.getRoom().getJid();
                            }
                            sendMsgReceiptAck(imMessage.getMessageId(), recipientMsg, conversation.isRoomType(),
                                    RainbowDeliveryReceivedReceipt.EVENT_READ);
                            conversation.resetUnreadCounter();
                            if (chatDataSource != null)
                                chatDataSource.updateDeliveryState(conversation.getJid(), imMessage);
                            notifyConversationsUpdated();
                        }
                    }
                }
            }
        }, "deliveryThread");
        deliveryThread.start();
    }

    @Override
    public void sendSingleMessageReadDelivery(Conversation conversation, IMMessage imMsg) {
        Log.getLogger().verbose(LOG_TAG, ">sendSingleMessageReadDelivery");

        if (conversation != null && conversation.getMessages() != null && imMsg != null) {

            if (!imMsg.isMsgSent() && !StringsUtil.isNullOrEmpty(imMsg.getMessageId())) {
                Log.getLogger().verbose(LOG_TAG, "Sending Msg Read Acknowledgement for Id; " + imMsg.getMessageId());
                if (imMsg.getDeliveryState().equals(IMMessage.DeliveryState.READ)) {
                    Log.getLogger().verbose(LOG_TAG, "    We have already sent READ STATE for this Message");
                    return;
                }

                imMsg.setDeliveryState(IMMessage.DeliveryState.READ);

                String recipientMsg = imMsg.getContactJid();
                if(conversation.getRoom() != null) {
                    recipientMsg = conversation.getRoom().getJid();
                }
                sendMsgReceiptAck(imMsg.getMessageId(), recipientMsg, conversation.isRoomType(),
                        RainbowDeliveryReceivedReceipt.EVENT_READ);
                conversation.decrementUnreadCounter();
                if (chatDataSource != null)
                    chatDataSource.updateDeliveryState(conversation.getJid(), imMsg);
                notifyConversationsUpdated();
            }
        }
    }

    void sendMsgReceiptAck(String receiptId, String contactJid, boolean isMultiChat, String receiptEvent) {
        if (StringsUtil.isNullOrEmpty(receiptId)) {
            Log.getLogger().warn(LOG_TAG, ">sendMsgReceiptAck; bad receiptId given");
            return;
        }

        RainbowDeliveryReceivedReceipt extension = new RainbowDeliveryReceivedReceipt(receiptId, receiptEvent, RainbowDeliveryReceivedReceipt.ENTITY_CLIENT, isMultiChat);

        Message messageAck = new Message();
        messageAck.setFrom(m_userJid);
        if (isMultiChat){
            messageAck.setType(Message.Type.groupchat);
            messageAck.setTo(XmppUtils.parseBareJid(contactJid));
        }
        else {
            messageAck.setType(Message.Type.chat);
            messageAck.setTo(contactJid);
        }
        messageAck.addExtension(extension);

        try {
            if( m_connection.isConnected() )
                m_connection.sendStanza(messageAck);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    public int getTotalUnreadMsgNb() {
        int nbUnreadMsg = 0;

        for(Conversation conv : m_conversations.getCopyOfDataList()) {
            nbUnreadMsg += conv.getUnreadMsgNb();
        }

        return nbUnreadMsg;
    }

    private void reSentMessage(Conversation conv) {
        for (IMMessage message : conv.getMessages().getCopyOfDataList()) {

            List <IMMessage> list = conv.getMessages().getCopyOfDataList();
            for (int i = 0 ;  i  < list.size(); i ++) {

                message = list.get(i);
                if (message.getDeliveryState() == IMMessage.DeliveryState.SENT && !message.isFromMaM()) {
                    if (!conv.isRoomType()) {
                        sendMessage(message, null, conv.getJid());
                        message.setFromMaM(true);

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Log.getLogger().verbose(LOG_TAG, "Exception while sending message"+ e.toString());
                        }
                    }
                    if (chatDataSource != null)
                        chatDataSource.createOrUpdateChat(conv.getJid(), message);
                }
            }
        }
    }

    public void resentMessages() {

        if (!resentDone) {
            resentDone = true;
            Thread myThread = new Thread() {
                public void run() {
                    for (Conversation conv : m_conversations.getCopyOfDataList())
                        reSentMessage(conv);

                }
            };
            myThread.start();
        }
    }

    public void refreshConversations(final IConversationProxy.IGetAllConversationListener listener)
    {
        Log.getLogger().info(LOG_TAG, ">refreshConversations");
        if( m_conversationProxy == null) {
            Log.getLogger().warn(LOG_TAG, ">refreshConversations; conversationProxy is NULL");
            return;
        }

        for (Conversation conv: m_conversations.getCopyOfDataList() ) {
            // All conversation must be said as not from server to allow full re-initialization of conversation list
            conv.setFromServer(false);
        }

        m_conversationProxy.getAllConversations(this, new IConversationProxy.IGetAllConversationListener()
        {
            @Override
            public void onGetConversationsSuccess(List<Conversation> conversationList)
            {
                Log.getLogger().verbose(LOG_TAG, ">refreshConversations onGetConversationsSuccess :" + conversationList.size());
                if (listener != null)
                    listener.onGetConversationsSuccess(conversationList);
            }

            @Override
            public void onGetConversationsError()
            {
                Log.getLogger().warn(LOG_TAG, "> refreshConversations onGetConversationsError");
                if (listener != null)
                    listener.onGetConversationsError();
            }
        });
    }

    public void updateConversation(String conversationId, boolean muteState, final IConversationProxy.IUpdateConversationListener listener) {
        Log.getLogger().info(LOG_TAG, ">updateConversation");
        if (m_conversationProxy == null) {
            Log.getLogger().warn(LOG_TAG, ">updateConversation; conversationProxy is NULL");
            return;
        }

        m_conversationProxy.updateConversation(m_contactCache.getUser().getCorporateId(), conversationId, muteState, new IConversationProxy.IUpdateConversationListener() {
            @Override
            public void onUpdateConversationSuccess(String conversationId) {
                Log.getLogger().verbose(LOG_TAG, ">onUpdateConversationSuccess");
                if (listener != null)
                    listener.onUpdateConversationSuccess(conversationId);
            }

            @Override
            public void onUpdateConversationFailed(String conversationId) {
                Log.getLogger().verbose(LOG_TAG, ">onUpdateConversationFailed");
                if (listener != null)
                    listener.onUpdateConversationSuccess(conversationId);
            }
        });
    }

    @Override
    public void onUserLoaded() {
        refreshConversations(null);
    }

    @Override
    public void rostersChanged() {
    }

    public void invalidateConvMam() {
        Log.getLogger().verbose(LOG_TAG, ">invalidateConvMam");
        for(Conversation conv : m_conversations.getCopyOfDataList()) {
            conv.setFirstMamDone(false);
        }
    }

    public void disconnect()
    {
        if  (m_xmppContactMgr != null)
        {
            m_xmppContactMgr.unregisterChangeListener(this);
        }

        if (m_connection != null)
        {
            if (m_mamMgr != null)
                m_mamMgr.disconnect();

            ProviderManager.removeExtensionProvider(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE);
            ProviderManager.removeExtensionProvider(RainbowDeliveryTimestampReceipt.ELEMENT, RainbowDeliveryTimestampReceipt.NAMESPACE);
            ProviderManager.removeExtensionProvider("conversation", ManagementReceiptPacketExtension.NAMESPACE);
            ProviderManager.removeExtensionProvider("room", ManagementReceiptPacketExtension.NAMESPACE);
            ProviderManager.removeExtensionProvider("unmute", ManagementReceiptPacketExtension.NAMESPACE);
            ProviderManager.removeExtensionProvider("mute", ManagementReceiptPacketExtension.NAMESPACE);
            ProviderManager.removeExtensionProvider("group", ManagementReceiptPacketExtension.NAMESPACE);
            ProviderManager.removeExtensionProvider("userinvite", ManagementReceiptPacketExtension.NAMESPACE);
            ProviderManager.removeExtensionProvider("usersettings", ManagementReceiptPacketExtension.NAMESPACE);
            ProviderManager.removeExtensionProvider(RainbowDeletedReceipt.ELEMENT, RainbowDeletedReceipt.NAMESPACE);
            ProviderManager.removeExtensionProvider("joincompanyinvite", ManagementReceiptPacketExtension.NAMESPACE);
            ProviderManager.removeExtensionProvider("joincompanyrequest", ManagementReceiptPacketExtension.NAMESPACE);
            ProviderManager.removeExtensionProvider(RainbowArchived.ELEMENT, RainbowArchived.NAMESPACE);
            ProviderManager.removeExtensionProvider(RainbowOutOfBandData.ELEMENT, RainbowOutOfBandData.NAMESPACE);
            ProviderManager.removeExtensionProvider(CallLogPacketExtension.ELEMENT, CallLogPacketExtension.NAMESPACE);
            ProviderManager.removeExtensionProvider(PgiConferenceInfoExtension.ELEMENT, PgiConferenceInfoExtension.NAMESPACE);
            ProviderManager.removeIQProvider(TimeRequestIq.ELEMENT, TimeRequestIq.NAMESPACE);
            ProviderManager.removeExtensionProvider(RoomConferenceEvent.ELEMENT, RoomConferenceEvent.NAMESPACE);

            ChatManager.getInstanceFor(m_connection).removeChatListener(this);

            for (Chat chat : m_chats)
                chat.removeMessageListener(this);

            m_chats.clear();
        }

        synchronized (m_changeListeners) {
            m_changeListeners.clear();
        }
    }

    public interface IChatMgrListener {
        void onImReceived(Conversation conversation, IMMessage imMessage);

        void isTypingState(Contact other, boolean isTyping, String roomId);

        void onImSent(Conversation message);

        void onConversationsUpdated();
    }

    public void cancelNotifications() {
        if (RainbowContext.getPlatformServices().getNotificationFactory() != null && RainbowContext.getPlatformServices().getNotificationFactory().getIMNotificationMgr() != null)
            RainbowContext.getPlatformServices().getNotificationFactory().getIMNotificationMgr().cancelAll();
    }

    public void getServerTime() {

        Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.getLogger().debug(LOG_TAG, "getServerTime");

                    TimeRequestIq timeRequestIq = new TimeRequestIq(m_connection.getHost(), m_connection.getConfiguration().getResource(), "essai");

                    try {
                        StanzaListener packetListener = new StanzaListener() {
                            @Override
                            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                                TimeReceiveIq result = (TimeReceiveIq) packet;
                                m_connection.removeSyncStanzaListener(this);
                                Log.getLogger().info(LOG_TAG, "getServerTime done - available : ");

                                long timestampWhenReceived = new Date().getTime();
                                if (result.getTimestamFromServer() != 0)
                                    timestampDiffFromServer = timestampAtRequest - result.getTimestamFromServer() + (timestampWhenReceived - timestampAtRequest) / 2;
                                else
                                    timestampDiffFromServer = 0;
                                Log.getLogger().info(LOG_TAG, "Server Time diff is: " + timestampDiffFromServer + " server utc is: " + result.getUtc());

                            }
                        };

                        m_connection.addSyncStanzaListener(packetListener, new StanzaTypeFilter(TimeReceiveIq.class));
                        timestampAtRequest = new Date().getTime();
                        m_connection.createPacketCollectorAndSend(timeRequestIq).nextResultOrThrow(TIMEOUT_30S);


                    } catch (Exception e) {
                        timestampDiffFromServer = 0;
                        Log.getLogger().error(LOG_TAG, "Error while getting server time", e);
                    }
                }
            });
        thread.start();
        }


}

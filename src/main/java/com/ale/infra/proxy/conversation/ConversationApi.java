package com.ale.infra.proxy.conversation;


import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.EmailAddress;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.contact.RainbowPresence;
import com.ale.infra.manager.Conversation;
import com.ale.infra.manager.IMMessage;
import com.ale.infra.manager.room.IRoomMgr;
import com.ale.infra.manager.room.Room;
import com.ale.infra.platformservices.IJSONParser;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.infra.rainbow.api.ConversationType;
import com.ale.infra.xmpp.xep.calllog.CallLogPacketExtension;
import com.ale.rainbow.JSONParser;
import com.ale.util.DateTimeUtil;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by georges on 10/05/16.
 */
public class ConversationApi implements IConversationApi {
    private static final String LOG_TAG = "ConversationApi";

    private Conversation m_conversation;
    private List<Contact> m_unresolvedContacts = new ArrayList<>();

    public ConversationApi(IPlatformServices platformServices, IContactCacheMgr contactCacheMgr, JSONObject jsonObj) throws Exception {
        if (platformServices.getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, "Parsing conv; " + jsonObj.toString());

        String ownUserJid = RainbowContext.getPlatformServices().getApplicationData().getUserJidIm();

        JSONParser jsonParser = new JSONParser(jsonObj);

        String convType = jsonParser.getString(RestResponse.TYPE);

        IMMessage lastMessage = new IMMessage();

        String dateStrg = jsonParser.getString(RestResponse.LAST_MSG_DATE);
        if (!StringsUtil.isNullOrEmpty(dateStrg)) {
            lastMessage.setTimeStamp(DateTimeUtil.getStampFromStringDate(dateStrg));
        }
        String msgContent = jsonParser.getString(RestResponse.LAST_MSG_CONTENT);
        if (!StringsUtil.isNullOrEmpty(msgContent)) {
            lastMessage.setMessageContent(msgContent);
        }
        String lastMessageSenderJid = jsonParser.getString(RestResponse.LAST_MSG_SENDER_ID);

        boolean isMsgSent = false;
        if (lastMessageSenderJid != null) {
            isMsgSent = lastMessageSenderJid.equalsIgnoreCase(ownUserJid);
        }
        lastMessage.setMsgSent(isMsgSent);

        IJSONParser lastMsgCallObject = jsonParser.getObject(RestResponse.LAST_MSG_CALL);
        if (lastMsgCallObject != null) {
            Log.getLogger().verbose(LOG_TAG, "Call is not NULL");
            String callState = lastMsgCallObject.getString(RestResponse.LAST_MSG_CALL_STATE);
            if (!StringsUtil.isNullOrEmpty(callState)) {
                CallLogPacketExtension callLogEvent = new CallLogPacketExtension();
                callLogEvent.setCallerJid(lastMessageSenderJid);
                if (callState.equals("missed")) {
                    callLogEvent.setState(CallLogPacketExtension.STATE_MISSED);
                } else if (callState.equals("answered")) {
                    String callDuration = lastMsgCallObject.getString(RestResponse.LAST_MSG_CALL_DURATION);
                    callLogEvent.setState(CallLogPacketExtension.STATE_ANSWERED);
                    if (StringsUtil.isNullOrEmpty(callDuration)) {
                        callLogEvent.setDuration(callDuration);
                    }
                }
                lastMessage.setCallLogEvent(callLogEvent, ownUserJid);
            }
        }

        String contactJid = jsonParser.getString(RestResponse.JID_IM);
        Contact contact = contactCacheMgr.getContactFromJid(contactJid);
        if (contact == null) {
            Log.getLogger().verbose(LOG_TAG, "Create Contact (not found in ContactCache)");
            DirectoryContact dirContact = new DirectoryContact();
            dirContact.setLastName(jsonParser.getString(RestResponse.LASTNAME));
            dirContact.setFirstName(jsonParser.getString(RestResponse.FIRSTNAME));
            dirContact.setImJabberId(contactJid);
            dirContact.setCorporateId(jsonParser.getString(RestResponse.PEER_ID));
            dirContact.addEmailAddress(jsonParser.getString(RestResponse.LOGIN), EmailAddress.EmailType.WORK);
            dirContact.setIsRoster(false);
            if (!StringsUtil.isNullOrEmpty(convType) && convType.equals(RestResponse.TYPE_BOT)) {
                dirContact.setType(DirectoryContact.DirectoryContactType.BOT);
            } else {
                dirContact.setPresence(null, RainbowPresence.UNSUBSCRIBED);
            }

            contact = contactCacheMgr.getContactFromJid(contactJid);
            if (contact != null) {
                Log.getLogger().verbose(LOG_TAG, "Contact " + contact.getDisplayName4Log("") + " is already known");
            } else {
                Log.getLogger().verbose(LOG_TAG, "Contact " + dirContact.getDisplayName4Log("") + " is not resolved");
                contact = contactCacheMgr.createContactIfNotExistOrUpdate(dirContact);
                m_unresolvedContacts.add(contact);
            }
        }

        if (RestResponse.TYPE_ROOM.equals(convType)) {
            Room room = null;

            if (!StringsUtil.isNullOrEmpty(lastMessageSenderJid)) {
                lastMessage.setContactJid(lastMessageSenderJid);
            }

            IRoomMgr roomMgr = RainbowContext.getInfrastructure().getRoomMgr();
            if (roomMgr != null) {
               room = roomMgr.getRoomByJid(contactJid);
            }
            if (room == null) {
                room = new Room();
                room.setJid(contactJid);
            }
            m_conversation = new Conversation(room);
            m_conversation.setContact(contact);
        } else {
            m_conversation = new Conversation(contact);
            lastMessage.setContactJid(contact.getImJabberId());
        }
        m_conversation.setId(jsonParser.getString("id"));
        m_conversation.setPeerId(jsonParser.getString(RestResponse.PEER_ID));
        m_conversation.setMuteValue(jsonParser.getBoolean(RestResponse.MUTE, false));
        m_conversation.setUnreceivedMsgNb(jsonParser.getInt(RestResponse.UNRECEIVED_MSG_NUMBER));
        m_conversation.setUnreadMsgNb(jsonParser.getInt(RestResponse.UNREAD_MSG_NUMBER));

        m_conversation.setTopic(jsonParser.getString(RestResponse.TOPIC));
        m_conversation.setName(jsonParser.getString(RestResponse.NAME));

        m_conversation.setLastMessage(lastMessage);

        m_conversation.setType(ConversationType.fromString(convType));
    }

    @Override
    public Conversation getConversation() {
        return m_conversation;
    }

    @Override
    public List<Contact> getUnresolvedContacts() {
        return m_unresolvedContacts;
    }
}

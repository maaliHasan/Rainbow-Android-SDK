/******************************************************************************
 * Copyright � 2014 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : lcourant 8 avr. 2014
 ******************************************************************************
 * Defects
 *
 */

package com.ale.infra.manager;

import com.ale.infra.manager.fileserver.RainbowFileDescriptor;
import com.ale.infra.xmpp.xep.Room.RoomMultiUserChatEvent;
import com.ale.infra.xmpp.xep.calllog.CallLogPacketExtension;
import com.ale.util.DateTimeUtil;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.io.Serializable;
import java.util.Date;

import static com.ale.infra.xmpp.xep.Room.RoomMultiUserChatEvent.RoomEventType.UNDEFINED;
import static com.ale.infra.xmpp.xep.calllog.CallLogPacketExtension.STATE_ANSWERED;
import static com.ale.infra.xmpp.xep.calllog.CallLogPacketExtension.STATE_MISSED;

/**
 * @author lcourant
 * 
 */
public class IMMessage implements Serializable {

	private static final String LOG_TAG = "IMMessage";

	public static final String START_MISSED_CALL_STRG = "missedCall||";
	public static final String START_ACTIVE_CALL_STRG = "activeCallMsg||";

	public enum DeliveryState
	{
		SENT,
		SENT_SERVER_RECEIVED,
		SENT_CLIENT_RECEIVED,
		SENT_CLIENT_READ,
		RECEIVED,
		READ,
		UNKNOWN
	}

	private String m_content;
	private Date m_messageReceived = null;
	private Date m_messageRead = null;
	private long timeStamp;
	private boolean m_sent;
	private String m_contactJid;
	private String m_messageId="";
	private String m_MamMessageId;
	private DeliveryState deliveryState = DeliveryState.UNKNOWN;
	private boolean m_fromMaM = false;
	private RoomMultiUserChatEvent.RoomEventType m_roomEventType = RoomMultiUserChatEvent.RoomEventType.UNDEFINED;
	private RainbowFileDescriptor fileDescriptor;
	private CallLogPacketExtension m_callLogEvent = null;

	public IMMessage()
	{
		m_sent = false;
	}

	public IMMessage(IMMessage message)
	{
		m_contactJid = message.getContactJid();
		setMessageContent(message.getMessageContent());
		timeStamp = message.getTimeStamp();
		m_sent = message.isMsgSent();
		m_MamMessageId = message.getMamMessageId();
	}

	public IMMessage(String contactJid, String content, boolean sentMsg)
	{
		m_contactJid = contactJid;
		setMessageContent(content);
		timeStamp = new Date().getTime();
		m_sent = sentMsg;
	}

	public IMMessage(String contactJid, String content, Date date, boolean sentMsg)
	{
		m_contactJid = contactJid;
		setMessageContent(content);
		timeStamp = date.getTime();
		m_sent = sentMsg;
	}

	public IMMessage(String contactJid, RoomMultiUserChatEvent.RoomEventType roomEventType)
	{
		m_contactJid = contactJid;
		timeStamp = new Date().getTime();
		m_roomEventType = roomEventType;
	}

	public String getMessageContent() {
		return m_content;
	}

	public void setMessageContent(String msg) {
		if( !StringsUtil.isNullOrEmpty(msg) ) {
			if( msg.startsWith(START_MISSED_CALL_STRG) ) {
				m_callLogEvent = new CallLogPacketExtension();
				m_callLogEvent.setState(STATE_MISSED);
			} else if( msg.startsWith(START_ACTIVE_CALL_STRG) ) {

				String duration = "";
				String[] contentSplitted = msg.split("\\|\\|");
				if( contentSplitted.length >= 3) {
					duration = contentSplitted[2];
				}

				m_callLogEvent = new CallLogPacketExtension(null,null,STATE_ANSWERED,duration);
			}
		}

		m_content = msg;
	}

	public Date getMessageDate() {
		if( this.timeStamp <= 0)
			return null;

		return new Date(this.timeStamp);
	}

	public Date getMessageDateRead() { return m_messageRead; }

	public void setMessageDateRead(Date date)
	{
		m_messageRead = date;
	}

	public Date getMessageDateReceived() { return m_messageReceived; }

	public void setMessageDateReceived(Date date)
	{
		m_messageReceived = date;
	}

	public boolean isMsgSent() { return m_sent;}

	public void setMsgSent(boolean sent) { m_sent = sent;}

	public String getContactJid() {
		return m_contactJid;
	}

	public void setContactJid(String contactJid) {
		m_contactJid = contactJid;
	}

	public void setMamMessageId(String messageId) {
	m_MamMessageId = messageId;
}

	public String getMamMessageId() {
		return m_MamMessageId;
	}

	public void setMessageId(String messageId) {
		m_messageId = messageId;
	}

	public String getMessageId() {
		return m_messageId;
	}

	public DeliveryState getDeliveryState() {
		return deliveryState;
	}

	public void setDeliveryState(DeliveryState deliveryState) {
		this.deliveryState = deliveryState;
	}

	public boolean isFromMaM() {
		return m_fromMaM;
	}

	public void setFromMaM(boolean m_fromMaM) {
		this.m_fromMaM = m_fromMaM;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public void setTimeStampFromDateString(String date) {
		this.timeStamp = DateTimeUtil.getStampFromStringDate(date);
	}

	public RoomMultiUserChatEvent.RoomEventType getRoomEventType() {
		return m_roomEventType;
	}

	public CallLogPacketExtension getCallLogEvent() {
		return m_callLogEvent;
	}

	public void setCallLogEvent(CallLogPacketExtension callLogEvent,String userJid) {
		if( callLogEvent != null) {
			this.m_callLogEvent = callLogEvent;
			if ( !StringsUtil.isNullOrEmpty(userJid) ) {
				if (callLogEvent.getCallerJid().equals(userJid))
					setMsgSent(true);
				else
					setMsgSent(false);
			}
		}
	}

	public void setRoomEventType(RoomMultiUserChatEvent.RoomEventType roomEventType) {
		m_roomEventType = roomEventType;
	}

	public boolean isRoomEventType() {
        return !m_roomEventType.equals(UNDEFINED);
    }

    public boolean isWebRtcEventType() {
		if( m_callLogEvent != null)
			return true;
        return !StringsUtil.isNullOrEmpty(m_content) &&
				(m_content.startsWith(START_MISSED_CALL_STRG) || m_content.startsWith(START_ACTIVE_CALL_STRG));
    }

    public boolean isEventType() {
        return isWebRtcEventType() || isRoomEventType();
    }

	public boolean isAnsweredCall() {
		if( m_callLogEvent != null)
			return m_callLogEvent.isAnswered();
		return !StringsUtil.isNullOrEmpty(m_content) && m_content.startsWith(START_ACTIVE_CALL_STRG);
	}

	public void setFileDescriptor(RainbowFileDescriptor fileDescriptor) {
		this.fileDescriptor = fileDescriptor;
	}

	public RainbowFileDescriptor getFileDescriptor() {
		return fileDescriptor;
	}

	public boolean isFileDescriptorAvailable() {
		return fileDescriptor != null;
	}

	public void dumpInLog(String dumpLogTag) {
		if( m_content != null ) {
			Log.getLogger().info(dumpLogTag, "    content="+m_content);
		}
		if( m_messageReceived != null ) {
			Log.getLogger().info(dumpLogTag, "    messageReceived="+m_messageReceived);
		}
		if( m_messageRead != null ) {
			Log.getLogger().info(dumpLogTag, "    messageRead="+m_messageRead);
		}
		Log.getLogger().info(dumpLogTag, "    timeStamp="+timeStamp);
		Log.getLogger().info(dumpLogTag, "    sent="+m_sent);
		Log.getLogger().info(dumpLogTag, "    deliveryState="+deliveryState);
		Log.getLogger().info(dumpLogTag, "    fromMaM="+m_fromMaM);

		if( m_contactJid != null ) {
			Log.getLogger().info(dumpLogTag, "    contactJid="+m_contactJid);
		}
		if( m_messageId != null ) {
			Log.getLogger().info(dumpLogTag, "    messageId="+m_messageId);
		}
		if( m_MamMessageId != null ) {
			Log.getLogger().info(dumpLogTag, "    MamMessageId="+ m_MamMessageId);
		}
		if( this.fileDescriptor != null ) {
			this.fileDescriptor.dumpInLog(dumpLogTag);
		}
		if( m_callLogEvent != null ) {
			Log.getLogger().info(dumpLogTag, "    CallLog Caller="+ m_callLogEvent.getCallerJid());
			Log.getLogger().info(dumpLogTag, "    CallLog Callee="+ m_callLogEvent.getCalleeJid());
			Log.getLogger().info(dumpLogTag, "    CallLog State="+ m_callLogEvent.getState());
			Log.getLogger().info(dumpLogTag, "    CallLog Duration="+ m_callLogEvent.getDuration());
		}
		Log.getLogger().info(dumpLogTag, "    roomEventType="+m_roomEventType);
		Log.getLogger().info(dumpLogTag, "    isWebRtcEventType="+isWebRtcEventType());
		Log.getLogger().info(dumpLogTag, "    ---");
    }
}

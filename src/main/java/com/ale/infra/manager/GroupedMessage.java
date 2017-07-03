package com.ale.infra.manager;

import com.ale.infra.manager.fileserver.IFileDescriptorListener;
import com.ale.infra.manager.fileserver.RainbowFileDescriptor;
import com.ale.infra.xmpp.xep.calllog.CallLogPacketExtension;
import com.ale.util.log.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by georges on 10/06/16.
 */
public class GroupedMessage extends AbstractGroupedMessage implements IFileDescriptorListener {

    private static final String LOG_TAG = "GroupedMessage";

    private Date m_messageDate;
    private Date m_messageDateRead;
    private boolean m_sent = false;
    private String m_contactJid;
    private boolean read;
    private List<IMMessage> m_messages = new ArrayList<>();
    private boolean m_isRoomEventType = false;
    private boolean m_isWebRtcEventType = false;
    private boolean m_isAnsweredCall = false;
    private RainbowFileDescriptor m_fileDescriptor = null;
    private CallLogPacketExtension m_callLogEvent = null;

    public GroupedMessage(IMMessage message)
    {
        m_contactJid = message.getContactJid();
        m_messageDate = message.getMessageDate();
        m_messageDateRead = message.getMessageDateRead();
        m_sent = message.isMsgSent();
        m_messages.add(message);
        m_isRoomEventType = message.isRoomEventType();
        m_isWebRtcEventType = message.isWebRtcEventType();
        m_isAnsweredCall = message.isAnsweredCall();
        m_fileDescriptor = message.getFileDescriptor();
        m_callLogEvent = message.getCallLogEvent();
    }

    public void addMessage(IMMessage message)
    {
        m_messages.add(message);
    }

    public List<IMMessage> getMessageList()
    {
        return m_messages;
    }

    public Date getDate() { return m_messageDate; }

    public void setMessageDate(Date date)
    {
        m_messageDate = date;
    }

    public Date getMessageDateRead() {
        return m_messageDateRead;
    }

    public void setMessageDateRead(Date date) {
        m_messageDateRead = date;
    }

    @Override
    public boolean isDateType() {
        return false;
    }

    public boolean isMsgSent() { return m_sent;}

    public void setMsgSent(boolean sent) { m_sent = sent;}

    public String getContactJid() {
        return m_contactJid;
    }

    public void setContactJid(String contactJid) {
        m_contactJid = contactJid;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isRead() {
        return read;
    }

    public String getMessageContent() {
        StringBuilder content = new StringBuilder();
        for(IMMessage msg : m_messages) {
            if( content.length() > 0) {
                content.append("\n");
            }
            content.append(msg.getMessageContent());
        }
        return content.toString();
    }

    public boolean isRoomEventType() {
        return m_isRoomEventType;
    }

    public boolean isWebRtcEventType() {
        return m_isWebRtcEventType;
    }

    @Override
    public boolean isEventType() {
        return m_isRoomEventType || m_isWebRtcEventType;
    }

    @Override
    public boolean isFileDescriptorAvailable() {
        return m_fileDescriptor != null;
    }

    @Override
    public RainbowFileDescriptor getFileDescriptor() {
        return m_fileDescriptor;
    }

    public void setFileDescriptor(RainbowFileDescriptor fileDescriptor) {
        if( m_fileDescriptor != null)
            m_fileDescriptor.unregisterChangeListener(this);
        m_fileDescriptor = fileDescriptor;
        m_fileDescriptor.registerChangeListener(this);
    }

    @Override
    public boolean isAnsweredCall() {
        return m_isAnsweredCall;
    }

    public String getAnsweredDuration() {
        if( m_callLogEvent != null) {
            return m_callLogEvent.getDuration();
        }
//        String[] contentSplitted = getMessageContent().split("\\|\\|");
//        if( contentSplitted.length >= 3) {
//            return contentSplitted[2];
//        }
        return "";
    }

    @Override
    public void onFileDescriptorUpdated(RainbowFileDescriptor fileDescriptor) {
        Log.getLogger().verbose(LOG_TAG, ">onFileDescriptorUpdated");
    }

}

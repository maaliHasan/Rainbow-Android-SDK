package com.ale.infra.manager;

import com.ale.infra.manager.fileserver.RainbowFileDescriptor;

import java.util.Date;

/**
 * Created by georges on 10/06/16.
 */
public abstract class AbstractGroupedMessage {

    protected Date m_messageDate;

    public Date getDate() { return m_messageDate; }

    public void setMessageDate(Date date)
    {
        m_messageDate = date;
    }

    public abstract boolean isDateType();

    public abstract boolean isMsgSent();

    public abstract boolean isRoomEventType();

    public abstract boolean isWebRtcEventType();

    public abstract boolean isEventType();

    public abstract boolean isFileDescriptorAvailable();

    public abstract RainbowFileDescriptor getFileDescriptor();

    public abstract boolean isAnsweredCall();
}

package com.ale.infra.manager;

import com.ale.infra.manager.fileserver.RainbowFileDescriptor;

import java.util.Date;

/**
 * Created by georges on 10/06/16.
 */
public  class GroupedDate extends AbstractGroupedMessage {


    public GroupedDate(Date date)
    {
        this.m_messageDate = date;
    }

    @Override
    public boolean isDateType() {
        return true;
    }

    @Override
    public boolean isMsgSent() {
        return false;
    }

    @Override
    public boolean isRoomEventType() {
        return false;
    }

    @Override
    public boolean isWebRtcEventType() {
        return false;
    }

    @Override
    public boolean isEventType() {
        return false;
    }

    @Override
    public boolean isFileDescriptorAvailable() {
        return false;
    }

    @Override
    public RainbowFileDescriptor getFileDescriptor() {
        return null;
    }

    @Override
    public boolean isAnsweredCall() {
        return false;
    }
}

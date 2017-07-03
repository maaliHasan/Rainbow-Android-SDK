package com.ale.infra.manager.room;

import com.ale.util.StringsUtil;

/**
 * Created by wilsius on 02/09/16.
 */
public class RoomChange {
    private String roomId;
    private String roomJid;
    private RoomStatus status;
    private String userJid;
    private String topic;

    public RoomChange(String roomId, String roomJid){
        this.roomId = roomId;
        this.roomJid = roomJid;
    }

    public void setStatus(String status) {
        this.status = RoomStatus.fromString(status);
    }

    public void setUserJid(String userJid) {
        this.userJid = userJid;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public boolean isTopicChange() {
        return !StringsUtil.isNullOrEmpty(getTopic());
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomJid() {
        return roomJid;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public String getUserJid() {
        return userJid;
    }
}

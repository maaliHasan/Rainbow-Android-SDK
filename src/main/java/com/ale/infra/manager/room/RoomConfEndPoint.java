package com.ale.infra.manager.room;

import com.ale.util.log.Log;

import java.util.Date;

/**
 * Created by georges on 28/04/2017.
 */

public class RoomConfEndPoint {
    private String userId;
    private String confEndpointId;
    private String mediaType;
    private Date additionDate;
    private String privilege;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setConfEndpointId(String confEndpointId) {
        this.confEndpointId = confEndpointId;
    }

    public String getConfEndpointId() {
        return confEndpointId;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setAdditionDate(Date additionDate) {
        this.additionDate = additionDate;
    }

    public Date getAdditionDate() {
        return additionDate;
    }

    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }

    public String getPrivilege() {
        return privilege;
    }

    public void dumpInLog(String dumpLogTag) {
        if( userId != null ) {
            Log.getLogger().info(dumpLogTag, "    userId="+userId);
        }
        if( confEndpointId != null ) {
            Log.getLogger().info(dumpLogTag, "    confEndpointId="+confEndpointId);
        }
        Log.getLogger().info(dumpLogTag, "    mediaType="+mediaType);
        if( additionDate != null ) {
            Log.getLogger().info(dumpLogTag, "    additionDate="+additionDate);
        }
        Log.getLogger().info(dumpLogTag, "    privilege="+privilege);
        Log.getLogger().info(dumpLogTag, "    ---");
    }

}

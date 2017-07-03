package com.ale.infra.manager.room;

import com.ale.infra.contact.Contact;
import com.ale.infra.data_model.IMultiSelectable;
import com.ale.util.log.Log;

import org.jivesoftware.smackx.muc.MUCRole;

import java.util.Date;

/**
 * Created by wilsius on 29/07/16.
 */
public class RoomParticipant implements IMultiSelectable {

    String id;
    Contact contact;
    RoomStatus status = RoomStatus.UNKNOWN;
    Date additionDate;
    MUCRole role;

    public MUCRole getRole() {
        return role;
    }

    public void setRole(String role) {

        if ("user".equalsIgnoreCase(role))
            this.setRole(MUCRole.participant);
        else if ("moderator".equals(role))
            this.setRole(MUCRole.moderator);
        else if ("guest".equals(role))
            this.setRole(MUCRole.visitor);
        else
            this.setRole(MUCRole.none);

    }

    public void setRole(MUCRole role) {
        this.role = role;
    }

    public Date getAdditionDate() {
        return additionDate;
    }

    public void setAdditionDate(Date additionDate) {
        this.additionDate = additionDate;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public void dumpInLog(String dumpLogTag) {
        if( id != null ) {
            Log.getLogger().info(dumpLogTag, "    id="+id);
        }
        if( contact != null ) {
            Log.getLogger().info(dumpLogTag, "    contact="+contact.getDisplayName(""));
        }
        Log.getLogger().info(dumpLogTag, "    status="+status);
        Log.getLogger().info(dumpLogTag, "    role="+role);
        if( additionDate != null ) {
            Log.getLogger().info(dumpLogTag, "    additionDate="+additionDate);
        }
        Log.getLogger().info(dumpLogTag, "    ---");
    }

    @Override
    public int getSelectableType() {
        if( getRole().equals(MUCRole.moderator))
            return 1;
        else if( getRole().equals(MUCRole.participant))
            return 2;
        else if( getRole().equals(MUCRole.visitor))
            return 3;

        return 0;
    }
}

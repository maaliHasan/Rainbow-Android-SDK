package com.ale.infra.proxy.room;

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.manager.room.RoomParticipant;
import com.ale.infra.manager.room.RoomStatus;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.util.log.Log;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by wilsius on 29/07/16.
 */
public class GetUserRoomDataResponse extends RestResponse {
    private static final String LOG_TAG = "GetUserRoomDataResponse";

    private RoomParticipant roomParticipant;

    public GetUserRoomDataResponse(IPlatformServices platformServices,IContactCacheMgr contactCacheMgr,String data) throws Exception {
        if (platformServices.getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, "Parsing Room; " + data);

        roomParticipant = new RoomParticipant();
        JSONObject obj1 = new JSONObject(data);
        JSONObject obj =  obj1.getJSONObject("data");


        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));

        roomParticipant.setId(obj.getString("userId"));

        Contact contact = contactCacheMgr.getContactFromCorporateId(roomParticipant.getId());
        if( contact == null) {
            DirectoryContact dirContact = new DirectoryContact();
            dirContact.setCorporateId(roomParticipant.getId());

            contact = contactCacheMgr.createContactIfNotExistOrUpdate(dirContact);
        }

        roomParticipant.setContact(contact);

        roomParticipant.setRole(obj.getString("privilege"));
        Date date = df.parse(obj.getString("additionDate"));
        roomParticipant.setAdditionDate(date);

        String status = obj.getString("status");
        roomParticipant.setStatus(RoomStatus.fromString(status));
    }

    public RoomParticipant getRoomParticipant() { return roomParticipant; }

}

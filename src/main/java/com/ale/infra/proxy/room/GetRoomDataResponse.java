package com.ale.infra.proxy.room;

import android.support.annotation.NonNull;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.contact.RainbowPresence;
import com.ale.infra.manager.room.Room;
import com.ale.infra.manager.room.RoomConfEndPoint;
import com.ale.infra.manager.room.RoomParticipant;
import com.ale.infra.manager.room.RoomStatus;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.util.log.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by wilsius on 29/07/16.
 */
public class GetRoomDataResponse extends RestResponse {
    private static final String LOG_TAG = "GetRoomDataResponse";

    private final List<Contact> m_unresolvedContacts = new ArrayList<>();
    private final SimpleDateFormat m_dateFormat;
    private final IContactCacheMgr m_contactCacheMgr;
    private Room room;

    public GetRoomDataResponse(String data) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, "Parsing Room; " + data);

        m_dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        m_dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        m_contactCacheMgr = RainbowContext.getInfrastructure().getContactCacheMgr();

        room = new Room();
        List<RoomParticipant> pList = new ArrayList<>();
        JSONObject obj1 = new JSONObject(data);
        JSONObject obj =  obj1.getJSONObject("data");
        room.setName(obj.getString("name"));
        room.setTopic(obj.getString("topic"));
        room.setId(obj.getString("id"));
        room.setJid(obj.getString("jid"));
        room.setCreatorId(obj.getString("creator"));


        if (obj.has("users")){
            JSONArray array = obj.getJSONArray("users");
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);

                RoomParticipant participant = parseUser(jsonObject);
                pList.add(participant);
            }
            room.setParticipants(pList);
        }
        if (obj.has("creationDate"))
            room.setCreationDate(m_dateFormat.parse(obj.getString("creationDate")));
        if (obj.has("visibility"))
            room.setVisibility(!obj.getString("visibility").equals("private"));

        if (obj.has("confEndpoints")) {
            Log.getLogger().verbose(LOG_TAG, "confEndpoints available");
            room.clearRoomConfEndPoints();
            JSONArray array = obj.getJSONArray("confEndpoints");
            for (int i = 0; i < array.length(); i++) {
                Log.getLogger().verbose(LOG_TAG, String.format("confEndpoints %d", i));
                JSONObject jsonObject = array.getJSONObject(i);

                RoomConfEndPoint confEndPoint = parseConfEndPoint(jsonObject);
                room.addRoomConfEndPoints(confEndPoint);
            }
        }

//        "conference" : {
//            "scheduledStartDate" : ISODate("2017-06-23T15:00:00.000Z"),
//                    "guestEmails" : [],
//            "scheduled" : true
//        }

        if (obj.has("conference")) {
            JSONObject jsonObject = obj.getJSONObject("conference");
            //room.setScheduledConf(jsonObject.getBoolean("scheduled"));
            room.setScheduledConf(false);
            if (obj.has("scheduledStartDate"))
            room.setScheduledStartDate(m_dateFormat.parse(obj.getString("scheduledStartDate")));
            if (obj.has("guestEmails")) {
                JSONArray array = obj.getJSONArray("guestEmails");

                for (int i = 0; i < array.length(); i++) {
                    Log.getLogger().verbose(LOG_TAG, "guest: " + array.getString(i));
                    room.getGuests().add(array.getString(i));
                }
            }
        } else {
            room.setScheduledConf(false);
        }
    }

    @NonNull
    private RoomConfEndPoint parseConfEndPoint(JSONObject jsonObject) throws JSONException, ParseException {
        RoomConfEndPoint confEndPoint = new RoomConfEndPoint();

        if( jsonObject.has("userId"))
            confEndPoint.setUserId(jsonObject.getString("userId"));

        if( jsonObject.has("confEndpointId"))
            confEndPoint.setConfEndpointId(jsonObject.getString("confEndpointId"));

        if( jsonObject.has("mediaType"))
            confEndPoint.setMediaType(jsonObject.getString("mediaType"));

        return confEndPoint;
    }

    @NonNull
    private RoomParticipant parseUser(JSONObject jsonObject) throws JSONException, ParseException {
        RoomParticipant participant = new RoomParticipant();
        participant.setId(jsonObject.getString("userId"));

        String contactJid  = jsonObject.getString("jid_im");

        DirectoryContact dirContact = new DirectoryContact();
        dirContact.setCorporateId(participant.getId());
        dirContact.setImJabberId(contactJid);
        dirContact.setIsRoster(false);
        dirContact.setPresence(null, RainbowPresence.UNSUBSCRIBED);


        Contact contact = m_contactCacheMgr.getContactFromJid(contactJid);
        if( contact != null) {
            Log.getLogger().verbose(LOG_TAG, "Contact "+contact.getDisplayName4Log("")+" is already known");
        } else {
            Log.getLogger().verbose(LOG_TAG, "Contact "+dirContact.getDisplayName4Log("")+" is not resolved");
            contact = m_contactCacheMgr.createContactIfNotExistOrUpdate(dirContact);
            m_unresolvedContacts.add(contact);
        }


        participant.setContact(contact);

        participant.setRole(jsonObject.getString("privilege"));
        Date date = m_dateFormat.parse(jsonObject.getString("additionDate"));
        participant.setAdditionDate(date);

        String statusStrg = jsonObject.getString("status");
        RoomStatus status = RoomStatus.fromString(statusStrg);
        participant.setStatus(status);
        return participant;
    }

    public Room getRoom() { return room; }

    public List<Contact> getUnresolvedContacts() {
        return m_unresolvedContacts;
    }
}

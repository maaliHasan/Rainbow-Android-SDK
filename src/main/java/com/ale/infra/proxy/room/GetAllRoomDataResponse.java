package com.ale.infra.proxy.room;

import android.support.annotation.NonNull;

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.manager.room.Room;
import com.ale.infra.manager.room.RoomConfEndPoint;
import com.ale.infra.manager.room.RoomParticipant;
import com.ale.infra.manager.room.RoomStatus;
import com.ale.infra.platformservices.IPlatformServices;
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
public class GetAllRoomDataResponse extends RestResponse {
    private static final String LOG_TAG = "GetAllRoomDataResponse";

    private final IContactCacheMgr m_contactCacheMgr;
    private final IPlatformServices m_platformServices;
    private ArrayList<Room> m_rooms;
    private List<Contact> m_unresolvedContacts = new ArrayList<>();

    public GetAllRoomDataResponse(IPlatformServices platformServices, IContactCacheMgr contactCacheMgr, String data) throws Exception{
        m_rooms = new ArrayList<>();
        m_contactCacheMgr = contactCacheMgr;
        m_platformServices = platformServices;

        if (m_platformServices.getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, "Parsing All Rooms; " + data);

        JSONObject obj = new JSONObject(data);
        JSONArray array = obj.getJSONArray("data");
        for (int i= 0; i < array.length() ; i++){
            Room room =  getRoomDataResponse(array.get(i).toString());
            m_rooms.add(room);
        }
    }

    private Room getRoomDataResponse(String data) throws Exception{

        if (m_platformServices.getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, "Parsing Room; " + data);


        Room room = new Room();
        List<RoomParticipant> participants = new ArrayList<RoomParticipant>();
        JSONObject obj = new JSONObject(data);
        room.setName(obj.getString("name"));
        room.setTopic(obj.getString("topic"));
        room.setId(obj.getString("id"));
        room.setJid(obj.getString("jid"));
        room.setCreatorId(obj.getString("creator"));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));

        if (obj.has("users")){
            JSONArray array = obj.getJSONArray("users");
            for (int i = 0; i < array.length(); i++) {
                JSONObject partJsonObject = array.getJSONObject(i);
                RoomParticipant participant = new RoomParticipant();
                participant.setId(partJsonObject.getString("userId"));

                DirectoryContact dirContact = new DirectoryContact();
                dirContact.setCorporateId(participant.getId());
                String contactJid = partJsonObject.getString("jid_im");
                dirContact.setImJabberId(contactJid);


                Contact contact = m_contactCacheMgr.getContactFromJid(contactJid);
                if( contact != null) {
                    Log.getLogger().verbose(LOG_TAG, "Contact "+contact.getDisplayName4Log("")+" is already known");
                } else {
                    Log.getLogger().verbose(LOG_TAG, "Contact "+dirContact.getDisplayName4Log("")+" is not resolved");
                    contact = m_contactCacheMgr.createContactIfNotExistOrUpdate(dirContact);
                    m_unresolvedContacts.add(contact);
                }

                participant.setContact(contact);

                participant.setRole(partJsonObject.getString("privilege"));
                Date date = df.parse(partJsonObject.getString("additionDate"));
                participant.setAdditionDate(date);

                String statusStrg = partJsonObject.getString("status");
                RoomStatus status = RoomStatus.fromString(statusStrg);
                participant.setStatus(status);

                participants.add(participant);
            }
            room.setParticipants(participants);
        }
        if (obj.has("creationDate"))
            room.setCreationDate(df.parse(obj.getString("creationDate")));
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

        return room;
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

    public List<Room> getRooms () { return m_rooms; }

    public List<Contact> getUnresolvedContacts() {
        return m_unresolvedContacts;
    }
}

package com.ale.infra.manager.room;


import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.data_model.IMultiSelectable;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.manager.pgiconference.PgiConference;
import com.ale.infra.searcher.IDisplayable;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.jivesoftware.smackx.muc.MUCRole;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Room implements IMultiSelectable, IDisplayable {
    private static final String LOG_TAG = "Room";

    private Contact m_user;
    private String name = "";
    private boolean visibility = false;
    private String topic = "";
    private String jid = "";
    private String id = "";
    private String creatorId = "";
    private String conversationId = "";
    private Date creationDate;
    private ArrayItemList<RoomParticipant> participants;
    private final Set<RoomListener> m_changeListeners = new HashSet<RoomListener>();
    private List<RoomConfEndPoint> m_confEndPoints = new ArrayList<>();
    private PgiConference pgiConference;
    private boolean scheduledConf;
    private Date scheduledStartDate;
    private boolean inactiveConference;


    private List<String> guests;




    public Room (){
        participants = new ArrayItemList<>();
        guests = new ArrayList<String>();
        scheduledConf = false;
        scheduledStartDate = new Date();
        inactiveConference = false;

        if( RainbowContext.getInfrastructure().getContactCacheMgr() != null)
            m_user = RainbowContext.getInfrastructure().getContactCacheMgr().getUser();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public ArrayItemList<RoomParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<RoomParticipant> participants) {
        this.participants.replaceAll(participants);
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String convId) {
        this.conversationId = convId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public boolean isVisible() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUserInvited() {
        if (RainbowContext.getInfrastructure().getContactCacheMgr() == null ||
                m_user == null)
            return false;

        RoomParticipant roomParticipant = findOwnUserInRoom();
        if ( roomParticipant != null && roomParticipant.getStatus().isInvited()) {
            return true;
        }
        return false;
    }

    public boolean isUserRejected() {
        if (RainbowContext.getInfrastructure().getContactCacheMgr() == null ||
                m_user == null)
            return false;

        RoomParticipant roomParticipant = findOwnUserInRoom();
        if ( roomParticipant != null && roomParticipant.getStatus().isRejected()) {
            return true;
        }
        return false;
    }

    public boolean isUserActive() {
        if (RainbowContext.getInfrastructure().getContactCacheMgr() == null ||
                m_user == null)
            return false;

        RoomParticipant roomParticipant = findOwnUserInRoom();
        if ( roomParticipant != null && roomParticipant.getStatus().isAccepted()) {
            return true;
        }
        return false;
    }

    public boolean isRoomVisibleForGui() {
        if (RainbowContext.getInfrastructure().getContactCacheMgr() == null ||
                m_user == null)
            return false;

        RoomParticipant roomParticipant = findOwnUserInRoom();
        if (roomParticipant == null)
            return false;
        switch (roomParticipant.getStatus()) {
            case ACCEPTED:
            case PENDING:
            case UNSUBSCRIBED:
            case INVITED:
                return true;
            case UNKNOWN:
            case DELETED:
            case REJECTED:
            default:
                return false;
        }
    }

    public boolean isUserUnsubscribed() {
        if (RainbowContext.getInfrastructure().getContactCacheMgr() == null ||
                m_user == null)
            return false;

        RoomParticipant roomParticipant = findOwnUserInRoom();
        if ( roomParticipant != null && roomParticipant.getStatus().isUnsubscribed()) {
            return true;
        }
        return false;
    }

    public boolean isUserDeleted() {
        if (RainbowContext.getInfrastructure().getContactCacheMgr() == null ||
                m_user == null)
            return false;

        RoomParticipant roomParticipant = findOwnUserInRoom();
        if ( roomParticipant != null && roomParticipant.getStatus().isDeleted()) {
            return true;
        }
        return false;
    }

    public RoomParticipant findOwnUserInRoom() {

        for(RoomParticipant roomPart : getParticipants().getCopyOfDataList()) {
            if( roomPart.getId().equals(m_user.getCorporateId())) {
                return roomPart;
            }
        }
        return null;
    }

    public void resolveParticipants(){
        Log.getLogger().verbose(LOG_TAG, ">resolveParticipants: " + getName());

        for (final RoomParticipant participant : this.getParticipants().getCopyOfDataList()) {
            RainbowContext.getInfrastructure().getContactCacheMgr().resolveDirectoryContactById(getId(), participant.getId(), new IRoomParticipantListener() {
                @Override
                public void roomParticipantFoundSuccess(Contact contact) {
                    Log.getLogger().verbose(LOG_TAG, "roomParticipantFoundSuccess: " + contact);
                    participant.setContact(contact);
                    RainbowContext.getInfrastructure().getContactCacheMgr().createContactIfNotExistOrUpdate(participant.getContact().getDirectoryContact());
                    participant.setContact(contact);
                }

                @Override
                public void roomParticipantFoundFailed(String roomId, String contactId ) {
                    Log.getLogger().warn(LOG_TAG, "roomParticipantFoundFailed for roomId = " + roomId + " and contactId = " + contactId);
                }
            });
            if (participant.getContact() != null) {
                Contact contact = RainbowContext.getInfrastructure().getContactCacheMgr().createContactIfNotExistOrUpdate(participant.getContact().getDirectoryContact());
                participant.setContact(contact);
            } else {
                Log.getLogger().warn(LOG_TAG, "Contact in RoomParticipant is NULL");
            }
        }
    }

    public void notifyRoomUpdated()
    {
        synchronized (m_changeListeners)
        {
            for (RoomListener listener : m_changeListeners)
            {
                listener.roomUpdated(this);
            }
        }
    }

    public void registerChangeListener(RoomListener changeListener)
    {
        synchronized (m_changeListeners)
        {
            if (!(m_changeListeners.contains(changeListener)))
                m_changeListeners.add(changeListener);
        }
    }

    public void unregisterChangeListener(RoomListener changeListener)
    {
        synchronized (m_changeListeners)
        {
            m_changeListeners.remove(changeListener);
        }
    }

    public RoomParticipant getParticipantWithId(String id) {
        for(RoomParticipant participant : participants.getCopyOfDataList()) {
            //TODO : check why sometimes get participant.getId = null
            if( id.equals(participant.getId()))
                return participant;
        }
        return null;
    }

    public void changeParticipantStatus(RoomChange roomChange) {
        if ( !roomChange.getStatus().isDeleted() &&
                !roomChange.getStatus().isAccepted() &&
                !roomChange.getStatus().isRejected() &&
                !roomChange.getStatus().isPending() &&
                !roomChange.getStatus().isInvited() &&
                !roomChange.getStatus().isUnsubscribed() ) {
            Log.getLogger().error(LOG_TAG, "Unknown room status: " + roomChange.getStatus());
            return;
        }
        Log.getLogger().verbose(LOG_TAG, "Participant " + roomChange.getStatus().toString() + " in Room; " + getName());
        changeParticipantStatus(roomChange.getUserJid(), roomChange.getStatus());
    }

    private synchronized RoomParticipant changeParticipantStatus(String jid, RoomStatus status) {
        Log.getLogger().verbose(LOG_TAG, ">changeParticipantStatus; "+jid);

        RoomParticipant roomPart = getRoomParticipantFromContactJid(jid);
        if( roomPart != null) {
            Log.getLogger().verbose(LOG_TAG, "Room part found");
            if (status.isPending() ||
                    status.isInvited())
                roomPart.setStatus(RoomStatus.INVITED);
            else
                roomPart.setStatus(status);
        } else {
            Log.getLogger().verbose(LOG_TAG, "Room part not found");
            if( status.isDeleted() || status.isRejected()) {
                Log.getLogger().warn(LOG_TAG, "Contact removed didn't belong to Room");
                return null;
            }

            IContactCacheMgr contactCache = RainbowContext.getInfrastructure().getContactCacheMgr();
            Contact contact = contactCache.getContactFromJid(jid);
            if( contact == null) {
                // TODO manage search here ?
                Log.getLogger().warn(LOG_TAG, "Contact added not found in Cache");
                return null;
            }

            roomPart = new RoomParticipant();
            roomPart.setRole(MUCRole.participant);
            roomPart.setContact(contact);
            roomPart.setAdditionDate(new Date());
            if (status.isAccepted())
                roomPart.setStatus(status);
            else if (status.isPending() || status.isInvited())
                roomPart.setStatus(RoomStatus.INVITED);
            else
                Log.getLogger().warn(LOG_TAG, "Room part State not managed; "+status);
            getParticipants().add(roomPart);
        }

        return roomPart;
    }

    public synchronized void addParticipant(RoomParticipant part) {
        RoomParticipant roomPart = getRoomParticipantFromContactJid(part.getContact().getImJabberId());
        if (roomPart == null) {
            getParticipants().add(part);
        }
    }

    public RoomParticipant getRoomParticipantFromContactJid(String jid) {
        Log.getLogger().verbose(LOG_TAG, ">getRoomParticipantFromContactJid");

        for (RoomParticipant currentRoomParticipant : getParticipants().getCopyOfDataList()) {
            if (currentRoomParticipant.getContact().getImJabberId().equals(jid)) {
                return currentRoomParticipant;
            }
        }
        return null;
    }

    public boolean isUserOwner() {
        return getCreatorId().equals(m_user.getCorporateId());
    }

    public boolean isRoomArchived() {
        return isUserUnsubscribed();
    }

    public void dumpInLog(String dumpLogTag) {
        if( m_user != null ) {
            m_user.dumpInLog(dumpLogTag);
        }
        if( name != null ) {
            Log.getLogger().info(dumpLogTag, "    name="+name);
        }
        if( topic != null ) {
            Log.getLogger().info(dumpLogTag, "    topic="+topic);
        }
        if( jid != null ) {
            Log.getLogger().info(dumpLogTag, "    jid="+jid);
        }
        if( id != null ) {
            Log.getLogger().info(dumpLogTag, "    id="+id);
        }
        if( creatorId != null ) {
            Log.getLogger().info(dumpLogTag, "    creatorId="+creatorId);
        }
        if( conversationId != null ) {
            Log.getLogger().info(dumpLogTag, "    conversationId="+conversationId);
        }
        if( creationDate != null ) {
            Log.getLogger().info(dumpLogTag, "    creationDate="+creationDate);
        }
        Log.getLogger().info(dumpLogTag, "    visibility="+visibility);
        if( participants != null && participants.getCount() > 0 ) {
            Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
            Log.getLogger().info(dumpLogTag, "    participants=" + participants.getCount());
            for(RoomParticipant roomPart: participants.getCopyOfDataList()) {
                roomPart.dumpInLog(dumpLogTag);
            }
        } else {
            Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
            Log.getLogger().info(dumpLogTag, "    participants Empty");
        }
        if( m_confEndPoints != null && m_confEndPoints.size() > 0 ) {
            Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
            Log.getLogger().info(dumpLogTag, "    EndPoints=" + m_confEndPoints.size());
            for(RoomConfEndPoint endPoint: m_confEndPoints) {
                endPoint.dumpInLog(dumpLogTag);
            }
        } else {
            Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
            Log.getLogger().info(dumpLogTag, "    EndPoints Empty");
        }

    }

    @Override
    public int getSelectableType() {
        if( isUserInvited())
            return 0;
        else if( isUserActive())
            return 1;
        else if( isRoomArchived())
            return 2;
        else
            return 3;
    }

    public Contact getOwner() {
        for(RoomParticipant part : participants.getCopyOfDataList()) {
            if( part.getRole().equals(MUCRole.moderator))
                return part.getContact();
        }
        return null;
    }

    public RoomStatus getUserStatus() {

        for(RoomParticipant part : participants.getCopyOfDataList()) {
            if( part.getContact().equals(m_user))
                return part.getStatus();
        }

        return null;
    }

    public boolean doesContainOnlyParticipants(List<Contact> contacts) {

        List<Contact> partsWithoutUser = getParticipantsWithoutUser();
        if( partsWithoutUser.size() != contacts.size())
            return false;

        for(Contact contactPart : contacts) {
            if( !partsWithoutUser.contains(contactPart) )
                return false;
        }
        return true;
    }

    public List<Contact> getParticipantsWithoutUser() {

        List<Contact> parts = new ArrayList<>();
        for(RoomParticipant part : participants.getCopyOfDataList()) {
            if( !part.getContact().getImJabberId().equals(m_user.getImJabberId()) ) {
                parts.add(part.getContact());
            }
        }
        return parts;
    }

    public List<RoomConfEndPoint> getEndPoints() {
        return m_confEndPoints;
    }

    public void clearRoomConfEndPoints() {
        m_confEndPoints.clear();
    }

    public void addRoomConfEndPoints(RoomConfEndPoint confEndPoint) {
        m_confEndPoints.add(confEndPoint);
    }

    public void setEndPoint(RoomConfEndPoint confEndPoint) {
        m_confEndPoints.clear();
        m_confEndPoints.add(confEndPoint);
    }

    private void setEndPoints(List<RoomConfEndPoint> endPoints) {
        m_confEndPoints.clear();
        m_confEndPoints.addAll(endPoints);
    }

    public void setPgiConference(PgiConference pgiConference) {
        this.pgiConference = pgiConference;
    }

    public PgiConference getPgiConference() {
        return pgiConference;
    }

    public interface RoomListener
    {
        void roomUpdated(Room updatedRoom);
    }

    public interface IRoomParticipantListener {
        void roomParticipantFoundSuccess(Contact contact);
        void roomParticipantFoundFailed(String roomId, String contactId);
    }

    @Override
    public boolean equals(Object o) {
        if (o==null) return false;
        if (!(o instanceof Room)) return false;
        else return this.getId().equals(((Room) o).getId());
    }

    public void update(Room newRoom) {//update values and keep listeners
//        m_user not updated
//        m_changeListeners not updated
        setName(newRoom.getName());
        setVisibility(newRoom.isVisible());
        setTopic(newRoom.getTopic());
        setJid(newRoom.getJid());
        setId(newRoom.getId());
        setCreatorId(newRoom.getCreatorId());
        setConversationId(newRoom.getConversationId());
        setCreationDate(newRoom.getCreationDate());
        setEndPoints(newRoom.getEndPoints());

        setPgiConference(newRoom.getPgiConference());
        setScheduledStartDate(newRoom.getScheduledStartDate());
        setScheduledConf(newRoom.isScheduledConf());

        participants.replaceAll(newRoom.getParticipants().getCopyOfDataList());

        notifyRoomUpdated();
    }


    public boolean isScheduledConf() {
        return scheduledConf;
    }

    public void setScheduledConf(boolean scheduledConf) {
        this.scheduledConf = scheduledConf;
    }

    public Date getScheduledStartDate() {
        return scheduledStartDate;
    }

    public void setScheduledStartDate(Date scheduledStartDate) {
        this.scheduledStartDate = scheduledStartDate;
    }

    public List<String> getGuests() {
        return guests;
    }

    public void setGuests(List<String> guests) {
        this.guests = guests;
    }

    @Override
    public String getDisplayName(String unknownNameString) {
        if( !StringsUtil.isNullOrEmpty(getName()) )
            return getName();
        return unknownNameString;
    }

    public boolean isInactiveConference() {
        return inactiveConference;
    }

    public void setInactiveConference(boolean inactiveConference) {
        this.inactiveConference = inactiveConference;
    }

}

package com.ale.infra.manager;

import com.ale.infra.contact.Contact;
import com.ale.infra.data_model.IMultiSelectable;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.manager.room.Room;
import com.ale.infra.proxy.conversation.IRainbowConversation;
import com.ale.infra.rainbow.api.ConversationType;
import com.ale.infra.searcher.IDisplayable;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by grobert on 20/05/16.
 */
public class Conversation implements IMultiSelectable, IDisplayable, IRainbowConversation
{
    private static final String LOG_TAG = "Conversation";
    private final Set<ConversationListener> m_changeListeners = new HashSet<>();
    private Contact m_contact;


    private Room m_room;
    private List<Contact> m_contacts;
    private String id;
    private String peerId;
    private ConversationType m_type;
    private Boolean m_mute = false;
    private ArrayItemList<IMMessage> m_messages = new ArrayItemList<>();
    private ArrayList<IMMessage> m_newMessages = new ArrayList<>();
    private boolean m_isFirstRead = true;
    private int unreceivedMsgNb;
    private int unreadMsgNb;
    private IMMessage lastMessage;
    private String topic;
    private String name;
    private boolean m_firstMamDone = false;
    private boolean m_fromServer = false;
    private boolean mamInProgress;

    public Conversation(Contact contact)
    {
        m_contact = contact;
        lastMessage = new IMMessage();
        m_contacts = new ArrayList<>();
        m_type = ConversationType.USER;
        id="";
    }

    public Conversation(Room room)
    {
        m_contact = new Contact();
        lastMessage = new IMMessage();
        m_contacts = new ArrayList<>();
        m_room = room;
        m_type = ConversationType.ROOM;
        if(room != null) {
            name = room.getName();
            peerId = room.getId();
            topic = room.getTopic();
        }
        id="";
    }

    public Conversation()
    {
        m_contact = new Contact();
        lastMessage = new IMMessage();
        m_contacts = new ArrayList<>();
        m_room = null;
        m_type = ConversationType.USER;
        id="";
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public synchronized List<IMMessage> getNewsMessages() {
        return (List<IMMessage>) m_newMessages.clone();
    }

    public synchronized void  removeNewsMessages() {
        m_newMessages.clear();
    }

    public void setIsNotFirstRead() {
        m_isFirstRead = false;
    }

    public boolean isFirstRead() {
        return m_isFirstRead;
    }

    public ArrayItemList<IMMessage> getMessages()
    {
        return m_messages;
    }

    public ArrayList<IMMessage> getCurrentMessages() {
        ArrayList<IMMessage> messages = new ArrayList<>();

        for (IMMessage message : m_messages.getItems()) {
            messages.add(message);
        }

        return messages;
    }

    public synchronized void addMessage(IMMessage message)
    {
        if (m_messages == null) {
            Log.getLogger().warn(LOG_TAG, "No messages into Conversation");
            return;
        }

        if (!isMsgAlreadyStored(message))
        {
            if (!m_isFirstRead) {
                m_newMessages.add(message);
            }
            Log.getLogger().verbose(LOG_TAG, "Conversation has "+m_messages.getCount()+" messages");

            if( mamInProgress && message.isFromMaM() ) {
                m_messages.addWithoutNotification(message);
            } else {
                m_messages.add(message);
            }

            if(message.getDeliveryState().equals(IMMessage.DeliveryState.RECEIVED)) {
                incrementUnreadCounter();
            }

            if(!mamInProgress)
                sortMessages();
        }
    }

    public void setMamInProgress(boolean mamInProgress) {
        this.mamInProgress = mamInProgress;

        if(!mamInProgress) {
            Log.getLogger().verbose(LOG_TAG, "Merge MAM messages and clear it");

            sortMessages();
        }
    }

    private void sortMessages()
    {
        List<IMMessage> msgRightOrder = m_messages.getCopyOfDataList();

        Collections.sort(msgRightOrder, new Comparator<IMMessage>()
        {
            @Override
            public int compare(IMMessage message, IMMessage otherMessage)
            {
                if (message.getMessageDate() == null && otherMessage.getMessageDate() == null)
                    return 0;

                if(message.getMessageDate() == null)
                    return -1;

                if(otherMessage.getMessageDate() == null)
                    return 1;

                return message.getMessageDate().compareTo(otherMessage.getMessageDate());
            }
        });

        m_messages.replaceAll(msgRightOrder);
    }

    private void incrementUnreadCounter()
    {
        Log.getLogger().verbose(LOG_TAG, ">incrementUnreadCounter");
        this.unreadMsgNb++;
        notifyConversationUpdated();
    }

    void decrementUnreadCounter()
    {
        Log.getLogger().verbose(LOG_TAG, ">decrementUnreadCounter");
        if (this.unreadMsgNb > 0)
        {
            this.unreadMsgNb--;
        }
        notifyConversationUpdated();
    }

    void resetUnreadCounter() {
        Log.getLogger().verbose(LOG_TAG, ">resetUnreadCounter");
        this.unreadMsgNb = 0;
        notifyConversationUpdated();
    }

    boolean isMsgAlreadyStored(IMMessage message)
    {
        Log.getLogger().verbose(LOG_TAG, ">isMsgIdAlreadyStored; " + message.getMessageId());

        if (StringsUtil.isNullOrEmpty(message.getMessageId()))
            return false;

        List<IMMessage> currentMessages = m_messages.getCopyOfDataList();

//        if(mamInProgress)
//            currentMessages = m_mamMessages;

        for (IMMessage msg : currentMessages)
        {
            if (StringsUtil.isNullOrEmpty(msg.getMessageId()))
            {
                Log.getLogger().verbose(LOG_TAG, "There is a message without id");
                if ( msg.getMessageContent() != null &&
                        msg.getMessageContent().equals(message.getMessageContent()))
                {
                    Log.getLogger().verbose(LOG_TAG, "Same message detected, change its Id");
                    msg.setMessageId(message.getMessageId());
                    msg.setMamMessageId(message.getMamMessageId());
                    msg.setFromMaM(message.isFromMaM());
                    return true;
                }
                continue;
            }

            if (msg.getMessageId().equals(message.getMessageId()))
            {
                Log.getLogger().verbose(LOG_TAG, "this message is already stored");
                msg.setMamMessageId(message.getMamMessageId());
                msg.setFromMaM(message.isFromMaM());
                msg.setRoomEventType(message.getRoomEventType());
                if( StringsUtil.isNullOrEmpty(msg.getContactJid()) ) {
                    msg.setContactJid(message.getContactJid());
                }

                if(message.getFileDescriptor() != null) {
                    Log.getLogger().verbose(LOG_TAG, "Store field FileDescriptor");
                    msg.setFileDescriptor(message.getFileDescriptor());
                }

                msg.setDeliveryState(message.getDeliveryState());

                return true;
            }
        }
        Log.getLogger().verbose(LOG_TAG, "this message is new");
        return false;
    }

    public Contact getContact()
    {
        return m_contact;
    }

    public void setContact(Contact contact)
    {
        if( contact != null) {
            m_contact = contact;
        }
    }

    public List<Contact> getContacts() {
        return m_contacts;
    }

    public void setContacts(List<Contact> m_contacts) {
        this.m_contacts = m_contacts;
    }

    public ConversationType getType()
    {
        return m_type;
    }

    public void setType(ConversationType type)
    {
        this.m_type = type;
    }

    public int getUnreceivedMsgNb()
    {
        return unreceivedMsgNb;
    }

    public void setUnreceivedMsgNb(int unreceivedMsgNb)
    {
        this.unreceivedMsgNb = unreceivedMsgNb;
    }

    public int getUnreadMsgNb()
    {
        return unreadMsgNb;
    }

    public void setUnreadMsgNb(int unreadMsgNb)
    {
        this.unreadMsgNb = unreadMsgNb;
        notifyConversationUpdated();
    }

    public IMMessage getLastMessage()
    {
        return this.lastMessage;
    }

    public void setLastMessage(IMMessage message)
    {
        if( this.lastMessage == null || this.lastMessage.getMessageDate() == null) {
            this.lastMessage = message;
        } else if( message != null && message.getMessageDate() != null ) {
            Date lastMsgDate = this.lastMessage.getMessageDate();
            Date msgDate = message.getMessageDate();
            if( lastMsgDate.before(msgDate)) {
                this.lastMessage = message;
            }
        }
    }

    public String getTopic() {
        return topic;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getJid() {
        if( m_type == ConversationType.ROOM) {
            if( m_room != null)
                return m_room.getJid();
        }
        else {
            if (m_contact != null)
                return m_contact.getImJabberId();
        }
        return null;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }


    public Room getRoom() {
        return m_room;
    }

    public void setRoom(Room room) {
        this.m_room = room;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Conversation)) return false;

        Conversation conv = (Conversation) o;

        return (conv.getId() != null && this.getId() != null && conv.getId().equalsIgnoreCase(this.getId()));
    }

    @Override
    public int hashCode() {
        int result = 0;
        if( this.id != null ) result = 31 * result + this.id.hashCode();
        if( this.lastMessage != null ) result = 31 * result + this.lastMessage.hashCode();
        if( this.name != null ) result = 31 * result + this.name.hashCode();
        return result;
    }

    public void notifyConversationUpdated()
    {
        synchronized (m_changeListeners)
        {
            for (ConversationListener listener : m_changeListeners)
            {
                listener.conversationUpdated(this);
            }
        }
    }

    void notifyConversationAdded()
    {
        synchronized (m_changeListeners)
        {
            for (ConversationListener listener : m_changeListeners)
            {
                listener.conversationAdded(this);
            }
        }
    }

    void notifyConversationDeleted()
    {
        synchronized (m_changeListeners)
        {
            for (ConversationListener listener : m_changeListeners)
            {
                listener.conversationDeleted(this);
            }
        }
    }

    public void registerChangeListener(ConversationListener changeListener)
    {
        synchronized (m_changeListeners)
        {
            m_changeListeners.add(changeListener);
        }
    }

    public void unregisterChangeListener(ConversationListener changeListener)
    {
        synchronized (m_changeListeners)
        {
            m_changeListeners.remove(changeListener);
        }
    }

    public void update(Conversation conversation)
    {
        unreadMsgNb = conversation.getUnreadMsgNb();
        unreceivedMsgNb = conversation.getUnreceivedMsgNb();
        lastMessage = conversation.getLastMessage();
        m_fromServer = conversation.isFromServer();
        if( m_contact != null)
            m_contact.merge(conversation.getContact());
        if( conversation.getRoom() != null)
            m_room = conversation.getRoom();

        notifyConversationUpdated();
    }

    public boolean isRoomType() {
        return getType().equals(ConversationType.ROOM);
    }

    public boolean isChatType() {
        return (getType().equals(ConversationType.USER) || getType().equals(ConversationType.BOT));
    }
    public Boolean isMuted() {
        return m_mute;
    }

    public void setMuteValue(Boolean mute) {
        if( mute != null) {
            m_mute = mute;
            notifyConversationUpdated();
        }
    }
    public boolean isFirstMamDone() {
        return m_firstMamDone;
    }

    void setFirstMamDone(boolean firstMaM) {
        m_firstMamDone = firstMaM;
    }

    boolean isFromServer() {
        return m_fromServer;
    }

    void setFromServer(boolean fromServer) {
        this.m_fromServer = fromServer;
    }


    @Override
    public String getDisplayName(String unknownNameString) {
        if( isRoomType()) {
            // Room
            if (!StringsUtil.isNullOrEmpty(getName()))
                return getName();
        } else {
            // Conversation PeerToPeer
            if( m_contact != null)
                return m_contact.getDisplayName("");
        }
        return unknownNameString;
    }

    public void dumpInLog(String dumpLogTag) {
        if( id != null ) {
            Log.getLogger().info(dumpLogTag, "    id="+id);
        }
        if( peerId != null ) {
            Log.getLogger().info(dumpLogTag, "    peerId="+peerId);
        }
        if( topic != null ) {
            Log.getLogger().info(dumpLogTag, "    topic="+topic);
        }
        if( name != null ) {
            Log.getLogger().info(dumpLogTag, "    name="+name);
        }
        if( m_mute != null ) {
            Log.getLogger().info(dumpLogTag, "    mute="+m_mute);
        }
        if( m_type != null ) {
            Log.getLogger().info(dumpLogTag, "    type="+m_type);
        }
        Log.getLogger().info(dumpLogTag, "    isFirstRead="+m_isFirstRead);
        Log.getLogger().info(dumpLogTag, "    unreceivedMsgNb="+unreceivedMsgNb);
        Log.getLogger().info(dumpLogTag, "    unreadMsgNb="+unreadMsgNb);
        Log.getLogger().info(dumpLogTag, "    firstMamDone="+m_firstMamDone);
        Log.getLogger().info(dumpLogTag, "    fromServer="+m_fromServer);
        if( m_contact != null ) {
            Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
            Log.getLogger().info(dumpLogTag, "    Contact:");
            m_contact.dumpInLog(dumpLogTag);
        }
        if( m_room != null ) {
            Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
            Log.getLogger().info(dumpLogTag, "    Room");
            m_room.dumpInLog(dumpLogTag);
        }
        if( m_contacts != null && m_contacts.size() > 0 ) {
            Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
            Log.getLogger().info(dumpLogTag, "    Contacts=" + m_contacts.size());
            for(Contact contact: m_contacts) {
                contact.dumpInLog(dumpLogTag);
            }
        }
        if( lastMessage != null ) {
            Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
            Log.getLogger().info(dumpLogTag, "    LastMessage:");
            lastMessage.dumpInLog(dumpLogTag);
        }
        if( m_messages != null && m_messages.getCount() > 0 ) {
            Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
            Log.getLogger().info(dumpLogTag, "    messages=" + m_messages.getCount());
            for(IMMessage message: m_messages.getCopyOfDataList()) {
                message.dumpInLog(dumpLogTag);
            }
        }
        if( m_newMessages != null && m_newMessages.size() > 0 ) {
            Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
            Log.getLogger().info(dumpLogTag, "    newMessages=" + m_newMessages.size());
            for(IMMessage message: m_newMessages) {
                message.dumpInLog(dumpLogTag);
            }
        }
        Log.getLogger().info(dumpLogTag, "    ///////////////////////////////////");
    }

    @Override
    public int getSelectableType() {
        return 0;
    }

    public interface ConversationListener
    {
        void conversationAdded(Conversation conversationAdded);

        void conversationDeleted(Conversation conversationDeleted);
        /**
         * called when contact has been updated
         */
        void conversationUpdated(Conversation updatedConversation);
    }
}

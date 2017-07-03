package com.ale.rainbow.manager;

import com.ale.infra.contact.Contact;
import com.ale.infra.manager.Conversation;
import com.ale.infra.manager.IMMessage;

/**
 * Created by georges on 07/09/2016.
 */
public class IMNotifInfos {

    private Contact m_contact;
    private IMMessage m_lastImNotRead;
    private Conversation m_conversation;

    public IMNotifInfos(Conversation conversation,Contact contact,IMMessage lastImNotRead) {
        this.m_contact = contact;
        this.m_lastImNotRead = lastImNotRead;
        this.m_conversation = conversation;
    }

    public Contact getContact() {
        return m_contact;
    }

    public void setContact(Contact contact) {
        this.m_contact = contact;
    }

    public IMMessage getLastImNotRead() {
        return m_lastImNotRead;
    }

    public void setLastImNotRead(IMMessage lastImNotRead) {
        this.m_lastImNotRead = lastImNotRead;
    }

    public Conversation getConversation() {
        return m_conversation;
    }

    public void setConversation(Conversation m_conversation) {
        this.m_conversation = m_conversation;
    }
}

package com.ale.infra.proxy.conversation;

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.IContact;
import com.ale.infra.manager.Conversation;
import com.ale.infra.rainbow.api.ConversationType;

import java.util.List;

/**
 * Created by georges on 10/05/16.
 */
public interface IConversationApi
{
    Conversation getConversation();

    List<Contact> getUnresolvedContacts();
}

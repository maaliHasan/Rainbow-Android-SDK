package com.ale.listener;

import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.manager.IMMessage;
import com.ale.infra.proxy.conversation.IRainbowConversation;

import java.util.List;

public interface IRainbowImListener {
    /**
     * this event is triggered when a new message is received for a given conversation.
     * @param conversationId : Conversation Id of the conversation which received a new message
     * @param message : message received. It's a IMMessage object.
     */
    void onImReceived(String conversationId, IMMessage message);

    /**
     * This event is triggered when a message sent is correctly sent.
     * @param conversationId : Conversation id of the given conversation.
     * @param message ; message sent/ It is a IMMessage object
     */
    void onImSent(String conversationId, IMMessage message);

    /**
     * This event is triggered when the typing state of the other contact changes
     * @param other     Contact of the P2P conversation
     * @param isTyping  True if the contact is currently typing
     * @param roomId
     */
    void isTypingState(IRainbowContact other, boolean isTyping, String roomId);

    /**
     * This event is triggered after calling getMessagesFromConversation into Im class
     * @param status : three states possible : Im.SUCCESS / Im.ERROR / Im.TIMEOUT
     * @param conversationId : conversation id of the conversation
     * @param messages : list of messages retrieved
     */
    void onMessagesListUpdated(int status, String conversationId, List<IMMessage> messages);

    /**
     * This event is triggered after calling getMoreMessagesFromConversation into Im class
     * @param status : three states possible : Im.SUCCESS / Im.ERROR / Im.TIMEOUT
     * @param conversationId : conversation id of the conversation
     * @param messages : list of messages retrieved
     */
    void onMoreMessagesListUpdated(int status, String conversationId, List<IMMessage> messages);
}
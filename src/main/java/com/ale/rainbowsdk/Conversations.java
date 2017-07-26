package com.ale.rainbowsdk;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.list.IItemListChangeListener;
import com.ale.infra.manager.Conversation;
import com.ale.infra.proxy.conversation.IConversationProxy;
import com.ale.infra.proxy.conversation.IRainbowConversation;
import com.ale.listener.IRainbowGetConversationListener;

import java.util.ArrayList;
import java.util.List;

/**
 * This module is used to manage conversations
 */

public class Conversations {

    private static final String LOG_TAG = "Conversations";

    private ArrayItemList<IRainbowConversation> m_conversations;

    private IItemListChangeListener m_conversationsListener = new IItemListChangeListener() {
        @Override
        public void dataChanged() {
            List<Conversation> conversations = RainbowContext.getInfrastructure().getChatMgr().getConversations().getCopyOfDataList();
            m_conversations.replaceAll((ArrayList<IRainbowConversation>) (ArrayList<?>) conversations);
        }
    };


    Conversations() {
        m_conversations = new ArrayItemList<>();
    }

    /**
     * Get all conversations for the logged user
     * @return a list of IRainbowConversation object which is composed of :<br>
     *      Rainbow  conversation<br>
     *      Ocntact associated with you in the conversation<br>
     *      The last message (IMMessage object)<br>
     *      Messages lits of the conversation
     */
    public ArrayItemList<IRainbowConversation> getAllConversations() {
        return m_conversations;
    }

    /**
     * Get the conversation or create a new one if it doesn't exist
     * 
     * @param contactJid : contactJid associated to the conversation
     * @param listener : interface which allow to know if the conversation is successfully loaded.
     */
    public IRainbowConversation getConversationFromContact(String  contactJid, final IRainbowGetConversationListener listener) {
        // Search the existing conversation and trigger it if exists
        // TODO: use getConversationFromId method if really needed and test null pointer for listener
        // listener.onGetConversationSuccess(conversation);


        // Or create a new conversation with the Jid and trigger it
        return RainbowContext.getInfrastructure().getChatMgr().createNewConversationFromJid(contactJid, new IConversationProxy.ICreateConversationListener() {
            @Override
            public void onCreationSuccess(String id) {
                if (listener != null) {
                    listener.onGetConversationSuccess(RainbowContext.getInfrastructure().getChatMgr().getConversationFromId(id));
                }

            }

            @Override
            public void onCreationError() {
                if (listener != null) {
                    listener.onGetConversationError();
                }

            }
        });
    }

    public IRainbowConversation getConversationFromId(String conversationId) {
        for (IRainbowConversation conversation : m_conversations.getItems()) {
            if (conversation.getId().equals(conversationId)) {
                return conversation;
            }
        }
        return null;
    }

    protected void registerChangeListener() {
        RainbowContext.getInfrastructure().getChatMgr().getConversations().registerChangeListener(m_conversationsListener);
    }

    protected void unregisterChangeListener() {
        RainbowContext.getInfrastructure().getChatMgr().getConversations().unregisterChangeListener(m_conversationsListener);
    }
}

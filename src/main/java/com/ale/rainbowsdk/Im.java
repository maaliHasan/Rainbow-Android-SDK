package com.ale.rainbowsdk;

import com.ale.infra.IInfrastructure;
import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.manager.ChatMgr;
import com.ale.infra.manager.Conversation;
import com.ale.infra.manager.IMMessage;
import com.ale.infra.proxy.conversation.IRainbowConversation;
import com.ale.infra.xmpp.xep.IMamNotification;
import com.ale.listener.IRainbowImListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Instant messaging module
 */

public class Im {

    private ArrayList<IRainbowImListener> m_imListener;
    private String m_lastMamImId = null;

    public static final int SUCCESS = 0;
    public static final int ERROR = 1;
    public static final int TIMEOUT = 2;


    private ChatMgr.IChatMgrListener m_listener = new ChatMgr.IChatMgrListener() {
        @Override
        public void onImReceived(Conversation conversation, IMMessage imMessage) {
            notifyOnImReceived(conversation.getId(), imMessage);
        }

        @Override
        public void isTypingState(Contact other, boolean isTyping, String roomId) {
            notifyIsTypingState(other, isTyping, roomId);
        }

        @Override
        public void onImSent(Conversation conversation) {
            notifyOnImSent(conversation.getId(), conversation.getLastMessage());
        }

        @Override
        public void onConversationsUpdated() {

        }
    };

    Im() {
        m_imListener = new ArrayList<>();
    }

    public void finalize() {
        try {
            super.finalize();
            IInfrastructure infra = RainbowContext.getInfrastructure();
            if (infra.getChatMgr() != null) {
                infra.getChatMgr().unregisterChangeListener(m_listener);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * Get the messages list to a given conversation
     *
     * @param conversation : the given conversation to retrieve nbMessagesToRetrieve messages
     * @param nbMessagesToRetrieve : number of messages to retrieve
     */
    public void getMessagesFromConversation(final IRainbowConversation conversation, int nbMessagesToRetrieve) {
        IMamNotification imamNotification = new IMamNotification() {
            @Override
            public void complete(ArrayItemList<IMMessage> messages, boolean complete) {
                if (messages.getCount() > 0) {
                    m_lastMamImId = messages.get(0).getMamMessageId();
                }
                notifyOnMessagesListUpdated(SUCCESS, conversation.getId(), messages.getCopyOfDataList());
            }

            @Override
            public void complete(String jid, boolean complete) {

            }

            @Override
            public void error(Exception e) {
                notifyOnMessagesListUpdated(ERROR, conversation.getId(), null);
            }

            @Override
            public void timeout() {
                notifyOnMessagesListUpdated(TIMEOUT, conversation.getId(), null);
            }
        };
        if (conversation.isRoomType()) {
            RainbowContext.getInfrastructure().getMultiUserChatMgr().refreshMessages((Conversation) conversation, nbMessagesToRetrieve, imamNotification);
        } else {
            RainbowContext.getInfrastructure().getChatMgr().refreshMessages((Conversation) conversation, nbMessagesToRetrieve, imamNotification);
        }

    }

    /**
     * Get nbMessagesToRetrieve more messages for a given conversation
     *
     * @param conversation : the given conversation to retrieve nbMessagesToRetrieve more messages
     * @param nbMessagesToRetrieve : number of messages to retrieve
     */
    public void getMoreMessagesFromConversation(final IRainbowConversation conversation, int nbMessagesToRetrieve) {
        String jid = conversation.getContact().getImJabberId();
        String with = jid;
        if (conversation.isRoomType()) {
            jid = conversation.getJid();
            with = RainbowSdk.instance().myProfile().getConnectedUser().getImJabberId();
        }

        IMamNotification iMamNotification = new IMamNotification() {
            @Override
            public void complete(ArrayItemList<IMMessage> messages, boolean complete) {
                if (messages.getCount() > 0) {
                    m_lastMamImId = messages.get(0).getMamMessageId();
                }
                notifyOnMoreMessagesListUpdated(Im.SUCCESS, conversation.getId(), messages.getCopyOfDataList());
            }

            @Override
            public void complete(String jid, boolean complete) {

            }

            @Override
            public void error(Exception e) {
                notifyOnMoreMessagesListUpdated(Im.ERROR, conversation.getId(), null);
            }

            @Override
            public void timeout() {
                notifyOnMoreMessagesListUpdated(Im.TIMEOUT, conversation.getId(), null);
            }
        };

        if (conversation.isRoomType()) {
            RainbowContext.getInfrastructure().getMultiUserChatMgr().refreshMoreMessages(jid, with, m_lastMamImId, nbMessagesToRetrieve, iMamNotification);
        } else {
            RainbowContext.getInfrastructure().getChatMgr().refreshMoreMessages(jid, with, m_lastMamImId, nbMessagesToRetrieve, iMamNotification);
        }

    }


    /**
     * Send a message into a given conversation
     * @param conversation : the given conversation
     * @param message : the message to send.
     */
    public void sendMessageToConversation(IRainbowConversation conversation, String message) {
        IMMessage immessage = new IMMessage(RainbowContext.getInfrastructure().getContactCacheMgr().getUser().getImJabberId(), message, true);
        if (conversation.isRoomType()) {
            RainbowContext.getInfrastructure().getMultiUserChatMgr().sendMessage(immessage, null, (Conversation) conversation);
        } else {
            RainbowContext.getInfrastructure().getChatMgr().sendMessage(immessage, null, (Conversation)conversation);
        }

    }

    public void markMessagesFromConversationAsRead(IRainbowConversation conversation) {
        Conversation conv = RainbowContext.getInfrastructure().getChatMgr().getConversationFromId(conversation.getId());
        if (conv != null) {
            RainbowContext.getInfrastructure().getChatMgr().sendMessagesReadDelivery(conv);
        }
    }


    private void notifyOnImReceived(String convId, IMMessage message) {
        for (IRainbowImListener listener : m_imListener) {
            listener.onImReceived(convId, message);
        }
    }

    private void notifyIsTypingState(Contact other, boolean isTyping, String roomId) {
        for (IRainbowImListener listener : m_imListener) {
            listener.isTypingState(other, isTyping, roomId);
        }
    }

    private void notifyOnImSent(String convId, IMMessage message) {
        for (IRainbowImListener listener : m_imListener) {
            listener.onImSent(convId, message);
        }
    }

    private void notifyOnMessagesListUpdated(int status, String conversationId, List<IMMessage> messages) {
        for (IRainbowImListener listener : m_imListener) {
            listener.onMessagesListUpdated(status, conversationId, messages);
        }
    }

    private void notifyOnMoreMessagesListUpdated(int status, String conversationId, List<IMMessage> messages) {
        for (IRainbowImListener listener : m_imListener) {
            listener.onMoreMessagesListUpdated(status, conversationId, messages);
        }
    }

    public void registerListener(IRainbowImListener listener) {
        if (!m_imListener.contains(listener)) {
            m_imListener.add(listener);
        }
    }

    public void unregisterListener(IRainbowImListener listener) {
        if (m_imListener.contains(listener)) {
            m_imListener.remove(listener);
        }
    }

    protected void registerChangeListener() {
        RainbowContext.getInfrastructure().getChatMgr().registerChangeListener(m_listener);
    }

    protected void unregisterChangeListener() {
        RainbowContext.getInfrastructure().getChatMgr().unregisterChangeListener(m_listener);
    }

}



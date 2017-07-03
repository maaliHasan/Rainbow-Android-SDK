package com.ale.infra.manager;

import com.ale.infra.list.ArrayItemList;
import com.ale.infra.manager.fileserver.RainbowFileDescriptor;
import com.ale.infra.proxy.conversation.IConversationProxy;
import com.ale.infra.xmpp.xep.IMamNotification;

import org.jivesoftware.smackx.chatstates.ChatState;

/**
 * Created by georges on 19/12/2016.
 */

public interface IChatMgr {

    void refreshMoreMessages(String jid, String with, String lastMamImId, int nbMessagesToRetrieve, IMamNotification iMamNotification);

    void deleteAllMessages(Conversation conv, IMamNotification iMamNotification);

    void sendIsTypingState(Conversation conversation, ChatState state);

    void refreshMessages(Conversation conversation, int nbMessagesToRetrieve, IMamNotification iMamNotification);

    void unregisterChangeListener(ChatMgr.IChatMgrListener changeListener);

    void sendMessagesReadDelivery(Conversation conversation);

    void sendMessage(IMMessage message, RainbowFileDescriptor fileDescriptor, Conversation conversation);

    Conversation getConversationFromJid(String jid);

    Conversation createNewConversationFromJid(String contactJid, IConversationProxy.ICreateConversationListener listener);

    void registerChangeListener(ChatMgr.IChatMgrListener listener);

    void sendSingleMessageReadDelivery(Conversation conversation, IMMessage imMsg);

    void refreshConversations(IConversationProxy.IGetAllConversationListener listener);

    ArrayItemList<Conversation> getConversations();
}

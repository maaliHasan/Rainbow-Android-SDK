package com.ale.infra.proxy.conversation;

import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.manager.ChatMgr;
import com.ale.infra.manager.Conversation;

import java.util.List;

/**
 * Created by georges on 10/05/16.
 */
public interface IConversationProxy
{
    void getAllConversations(ChatMgr chatMgr, IGetAllConversationListener listener);

    void createConversation(Conversation conv, ICreateConversationListener listener);

    void deleteConversation(String conversationId, IDeleteConversationListener listener);

    void updateConversation(String userId, String conversationId, boolean muteState, IUpdateConversationListener listener);

    void muteAllConversations(String userId, boolean muteState, IUpdateConversationListener updateConversationListener);

    void downLoadConversation(String userId, String conversationId, IDownloadConversationListener downloadConversationListener);

    interface ICreateConversationListener
    {
        void onCreationSuccess(String id);

        void onCreationError();
    }

    interface IDeleteConversationListener
    {
        void onDeletionSuccess();

        void onDeletionError();
    }

    interface IGetAllConversationListener
    {
        void onGetConversationsSuccess(List<Conversation> conversations);

        void onGetConversationsError();
    }

    interface IUpdateConversationListener
    {
        void onUpdateConversationSuccess(String conversationId);
        void onUpdateConversationFailed(String conversationId);
    }

    interface IDownloadConversationListener
    {
        void onDownloadConversationSuccess();
        void onDownloadConversationFailed(RainbowServiceException exception);
    }
}

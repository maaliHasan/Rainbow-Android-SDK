package com.ale.infra.rainbow.api;

import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.proxy.conversation.GetAllConversationsResponse;

/**
 * Created by georges on 10/05/16.
 */
public interface IRainbowConversationService extends IRainbowService
{
    void getAllConversations(String userId, IAsyncServiceResultCallback<GetAllConversationsResponse> callback);

    void createConversation(String userId, String peerId, String type, IAsyncServiceResultCallback<String> callback);

    void deleteConversation(String userId, String concersationId, IAsyncServiceVoidCallback callback);

    void updateConversation(String userId, String conversationId, boolean muteState, IAsyncServiceResultCallback callback);

    void downloadConversation(String userId, String conversationId, IAsyncServiceResultCallback<String> callback);
}

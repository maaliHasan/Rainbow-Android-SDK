package com.ale.listener;

import com.ale.infra.proxy.conversation.IRainbowConversation;

/**
 * Created by letrongh on 27/04/2017.
 */

public interface IRainbowGetConversationListener {
    /**
     *
     * @param conversation ; the conversation is successfully found
     */
    void onGetConversationSuccess(IRainbowConversation conversation);

    /**
     *
     */
    void onGetConversationError();
}

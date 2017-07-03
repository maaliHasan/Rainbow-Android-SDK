package com.ale.listener;

import com.ale.infra.manager.Conversation;
import com.ale.infra.manager.IMMessage;

/**
 * Created by letrongh on 06/04/2017.
 */

public interface IRainbowConversationsListener {

    /**
     *  This event is fired when the list of conversations has changed.
     *
     *  For example:
     *      - conversation added
     *      - conversation removed
     *      - conversations sorted
     *      - new last message in a conversation
     */
    void onConversationsChanged();

    /**
     *  Maybe to be removed too and get new IM somewhere else?
     * @param conversation
     * @param message
     */
    void onConversationChanged(Conversation conversation, IMMessage message);
}

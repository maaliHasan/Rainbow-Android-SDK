package com.ale.infra.searcher;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.manager.ChatMgr;
import com.ale.infra.manager.Conversation;
import com.ale.util.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by georges on 21/11/2016.
 */

public class ConversationSearcher extends AbstractSearcher {

    private static final String LOG_TAG = "ConversationSearcher";
    private final ChatMgr m_chatMgr;

    public ConversationSearcher() {
        m_chatMgr = RainbowContext.getInfrastructure().getChatMgr();
    }

    public List<IDisplayable> searchByName(String query)
    {
        Log.getLogger().verbose(LOG_TAG, ">searchByName: " + query);

        List<IDisplayable> convsFound = new ArrayList<>();

        if (m_chatMgr != null) {
            List<Conversation> conversations = m_chatMgr.getConversations().getCopyOfDataList();
            for (Conversation conversation : conversations) {
                if (isMatchingQuery(conversation, query)) {
                    convsFound.add(conversation);
                }
            }
        }

        return convsFound;
    }
}

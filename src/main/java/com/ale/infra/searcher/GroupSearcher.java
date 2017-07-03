package com.ale.infra.searcher;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Group;
import com.ale.infra.manager.ChatMgr;
import com.ale.infra.manager.Conversation;
import com.ale.infra.manager.IGroupMgr;
import com.ale.util.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by georges on 24/11/2016.
 */
public class GroupSearcher extends AbstractSearcher {

    private static final String LOG_TAG = "GroupSearcher";
    private final IGroupMgr m_groupMgr;

    public GroupSearcher() {
        m_groupMgr = RainbowContext.getInfrastructure().getGroupMgr();
    }

    public List<IDisplayable> searchByName(String query)
    {
        Log.getLogger().verbose(LOG_TAG, ">searchByName: " + query);

        List<IDisplayable> groupsFound = new ArrayList<>();

        List<Group> groups = m_groupMgr.getGroups().getCopyOfDataList();
        for(Group group : groups) {
            if( isMatchingQuery(group, query)) {
                groupsFound.add(group);
            }
        }

        return groupsFound;
    }
}

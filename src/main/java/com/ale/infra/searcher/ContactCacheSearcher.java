package com.ale.infra.searcher;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.manager.Conversation;
import com.ale.util.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by georges on 21/11/2016.
 */

public class ContactCacheSearcher extends AbstractSearcher {

    private static final String LOG_TAG = "ContactCacheSearcher";

    private final IContactCacheMgr m_contactCacheMgr;

    public ContactCacheSearcher() {
        m_contactCacheMgr = RainbowContext.getInfrastructure().getContactCacheMgr();
    }

    public List<IDisplayable> searchByNameOnRosterAndLocal(String query)
    {
        Log.getLogger().verbose(LOG_TAG, "searchContactsByName(" + query + ") : START.");

        List<IDisplayable> contactsFound = new ArrayList<>();
        Contact me = m_contactCacheMgr.getUser();

        for(Contact contact : m_contactCacheMgr.getRosterAndLocal().getCopyOfDataList()) {
            if( contact.equals(me) || contact.getFirstEmailAddress().equals(me.getFirstEmailAddress())) {
                Log.getLogger().warn(LOG_TAG, "Skip own user or local contact from Search Result");
                continue;
            }
            if( isMatchingQuery(contact, query) ) {
                contactsFound.add(contact);
            }
        }

        return contactsFound;
    }

    public List<IDisplayable> searchByNameOnRosterAndLocalWithConversationFilter(String query, List<IDisplayable> conversationList)
    {
        Log.getLogger().verbose(LOG_TAG, ">searchByNameOnRosterAndLocalWithConversationFilter: " + query);

        List<IDisplayable> rosterAndLocalFound = searchByNameOnRosterAndLocal(query);

        for(IDisplayable displayable: conversationList) {
            if (displayable instanceof Conversation) {
                Conversation conversation = (Conversation) displayable;
                if ( conversation.isChatType() && rosterAndLocalFound.contains(conversation.getContact()) ) {
                    rosterAndLocalFound.remove(conversation.getContact());
                }
            }
        }

        return rosterAndLocalFound;
    }

}

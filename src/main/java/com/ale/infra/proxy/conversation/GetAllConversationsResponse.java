package com.ale.infra.proxy.conversation;

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.manager.Conversation;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.util.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by georges on 10/05/16.
 */
public class GetAllConversationsResponse extends RestResponse
{
    private static final String LOG_TAG = "GetAllConversationsResponse";

    List<Conversation> conversations = new ArrayList<>();
    List<Contact> m_unresolvedContacts = new ArrayList<>();

    public GetAllConversationsResponse(IPlatformServices platformServices, IContactCacheMgr contactCacheMgr, String conversations) throws Exception
    {
        if (platformServices.getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, "Parsing conv; "+conversations);

        JSONObject convsObj = new JSONObject(conversations);
        JSONArray arrayUsersFounded = convsObj.getJSONArray("data");

        for (int index = 0; index < arrayUsersFounded.length(); index++)
        {
            JSONObject jsonObj = arrayUsersFounded.getJSONObject(index);

            ConversationApi conversationApi = new ConversationApi(platformServices, contactCacheMgr, jsonObj);
            if( conversationApi != null && conversationApi.getConversation() != null) {
                this.conversations.add(conversationApi.getConversation());
                m_unresolvedContacts.addAll(conversationApi.getUnresolvedContacts());
            }
        }
    }

    public List<Conversation> getConversations()
    {
        return conversations;
    }

    public List<Contact> getUnresolvedContacts()
    {
        return m_unresolvedContacts;
    }
}

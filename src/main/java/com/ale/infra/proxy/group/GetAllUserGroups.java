package com.ale.infra.proxy.group;

import com.ale.infra.contact.Group;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.manager.Conversation;
import com.ale.infra.proxy.conversation.ConversationApi;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.util.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by wilsius on 11/10/16.
 */

public class GetAllUserGroups extends RestResponse {

    private static final String LOG_TAG = "GetAllUserGroups";

    ArrayItemList<Group> groups = new ArrayItemList<>();

    public GetAllUserGroups(String json) throws Exception {
        Log.getLogger().verbose(LOG_TAG, "Parsing user groups; " + json);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));


        JSONObject convsObj = new JSONObject(json);
        JSONArray arrayUsersGroups = convsObj.getJSONArray("data");


        for (int index = 0; index < arrayUsersGroups.length(); index++) {
            JSONObject jsonObj = arrayUsersGroups.getJSONObject(index);
            Group group = new Group(true);
            group.setId( jsonObj.getString("id"));
            group.setName(jsonObj.getString("name"));
            group.setOwner(jsonObj.getString("owner"));

            if (jsonObj.has("creationDate"))
                group.setCreationDate(df.parse(jsonObj.getString("creationDate")));
            groups.add(group);
        }
    }

    public ArrayItemList<Group> getGroups() {
        return groups;
    }
}


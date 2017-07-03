package com.ale.infra.proxy.group;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.Group;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.util.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by wilsius on 11/10/16.
 */

public class AddUserInGroup extends RestResponse {

    private static final String LOG_TAG = "AddUserInGroup";
    private final IContactCacheMgr m_contactCacheMgr;

    Group group = new Group(true);

    public AddUserInGroup(String json) throws Exception {
        Log.getLogger().verbose(LOG_TAG, "Parsing UserAdded; " + json);

        m_contactCacheMgr = RainbowContext.getInfrastructure().getContactCacheMgr();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));


        JSONObject convsObj = new JSONObject(json);
        JSONObject data = convsObj.getJSONObject("data");
        group.setId(data.getString("id"));
        group.setName(data.getString("name"));
        group.setOwner(data.getString("owner"));
        group.setComment(data.getString("comment"));

        if (data.has("creationDate"))
            group.setCreationDate(df.parse(data.getString("creationDate")));

        if (data.has("users")) {
            JSONArray array = data.getJSONArray("users");

            for (int i = 0; i < array.length(); i++) {
                String memberId = array.getString(i);
                Log.getLogger().verbose(LOG_TAG, "User received");
                DirectoryContact dirContact = new DirectoryContact();
                dirContact.setCorporateId(memberId);
                Contact memberContact = m_contactCacheMgr.createContactIfNotExistOrUpdate(dirContact);
                group.getGroupMembers().add(memberContact);
            }
        }
    }

    public Group getGroup() {
        return group;
    }

}

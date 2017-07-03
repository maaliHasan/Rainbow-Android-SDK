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

public class GetGroupResponse extends RestResponse {

    private static final String LOG_TAG = "GetGroupResponse";

    Group group = new Group(true);

    public GetGroupResponse(String json) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, "Parsing group; " + json);

        IContactCacheMgr contactCacheMgr = RainbowContext.getInfrastructure().getContactCacheMgr();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));


        JSONObject convsObj = new JSONObject(json);
        JSONObject data = convsObj.getJSONObject("data");
        if (data.has("comment")) {
            group.setComment(data.getString("comment"));
        }
        group.setId(data.getString("id"));
        group.setName(data.getString("name"));
        group.setOwner(data.getString("owner"));

        if (data.has("creationDate"))
            group.setCreationDate(df.parse(data.getString("creationDate")));

        JSONArray array = data.getJSONArray("users");

        for (int i = 0; i < array.length(); i++) {
            if( !array.isNull(i)) {
                JSONObject j = array.getJSONObject(i);

                DirectoryContact dirContact = new DirectoryContact();
                dirContact.setCorporateId(j.getString("id"));
                dirContact.setImJabberId(j.getString("jid_im"));
                dirContact.setFirstName(j.getString("firstName"));
                dirContact.setLoginEmail(j.getString("loginEmail"));
                dirContact.setLastName(j.getString("lastName"));
                if( !j.isNull("lastAvatarUpdateDate") ) {
                    dirContact.setLastAvatarUpdateDate(j.getString("lastAvatarUpdateDate"));
                }

                Contact contact = contactCacheMgr.createContactIfNotExistOrUpdate(dirContact);
                group.getGroupMembers().add(contact);
            }
        }
    }

    public Group getGroup() {
        return group;
    }

}

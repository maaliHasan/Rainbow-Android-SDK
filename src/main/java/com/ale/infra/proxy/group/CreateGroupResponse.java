package com.ale.infra.proxy.group;

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.Group;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.util.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by wilsius on 11/10/16.
 */

public class CreateGroupResponse extends RestResponse {

    private static final String LOG_TAG = "CreateGroupResponse";

    private final IPlatformServices m_platformServices;
    private IContactCacheMgr m_contactCacheMgr;
    private Group m_group;

    public CreateGroupResponse(IContactCacheMgr contactCacheMgr, IPlatformServices platformServices, String json) throws Exception{
        m_contactCacheMgr = contactCacheMgr;
        m_platformServices = platformServices;
        m_group = new Group(true);

        if (m_platformServices.getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, "Parsing group; " + json);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));


        JSONObject convsObj = new JSONObject(json);
        JSONObject data = convsObj.getJSONObject("data");
        m_group.setId(data.getString("id"));
        m_group.setName(data.getString("name"));
        m_group.setOwner(data.getString("owner"));
        if (data.has("comment")) {
            m_group.setComment(data.getString("comment"));
        }
        m_group.setId(data.getString("id"));

        if (data.has("creationDate"))
            m_group.setCreationDate(df.parse(data.getString("creationDate")));

        if (data.has("users")) {
            JSONArray array = data.getJSONArray("users");

            for (int i = 0; i < array.length(); i++) {
                JSONObject j = array.getJSONObject(i);

                if( j != null) {
                    DirectoryContact dirContact = new DirectoryContact();
                    dirContact.setCorporateId(j.getString("id"));
                    dirContact.setImJabberId(j.getString("jid_im"));
                    dirContact.setFirstName(j.getString("firstName"));
                    dirContact.setLoginEmail(j.getString("loginEmail"));
                    dirContact.setLastName(j.getString("lastName"));
                    if( !j.isNull("lastAvatarUpdateDate") ) {
                        dirContact.setLastAvatarUpdateDate(j.getString("lastAvatarUpdateDate"));
                    }

                    Contact contact = m_contactCacheMgr.createContactIfNotExistOrUpdate(dirContact);
                    m_group.getGroupMembers().add(contact);
                }
            }
        }
    }

    public Group getGroup() {
        return m_group;
    }

}

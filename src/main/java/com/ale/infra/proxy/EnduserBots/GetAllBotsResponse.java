package com.ale.infra.proxy.EnduserBots;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.util.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wilsius on 08/08/16.
 */
public class GetAllBotsResponse extends RestResponse
{
    private static final String LOG_TAG = "GetAllBotsResponse";

    public List<DirectoryContact> getDirContacts() {
        return m_dirContacts;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    List<DirectoryContact> m_dirContacts = new ArrayList<>();
    int limit;
    int offset;

    public GetAllBotsResponse(String data) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, "Parsing bots; "+ data);

        JSONObject obj = new JSONObject(data);
        JSONArray bots = obj.getJSONArray("data");

        for (int index = 0; index < bots.length(); index++)
        {
            JSONObject jsonObj = bots.getJSONObject(index);

            DirectoryContact dirContact = new DirectoryContact();
            dirContact.setLastName(jsonObj.getString(RestResponse.NAME));
            dirContact.setFirstName("");
            dirContact.setType(DirectoryContact.DirectoryContactType.BOT);
            dirContact.setImJabberId(jsonObj.getString(RestResponse.JID));
            dirContact.setCorporateId(jsonObj.getString(RestResponse.ID));

            m_dirContacts.add(dirContact);
        }

        limit = obj.getInt(RestResponse.LIMIT);
        offset = obj.getInt(RestResponse.OFFSET);
    }

}

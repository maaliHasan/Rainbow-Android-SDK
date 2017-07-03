package com.ale.infra.proxy.EnduserBots;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.util.log.Log;

import org.json.JSONObject;

/**
 * Created by wilsius on 08/08/16.
 */
public class GetBotDataResponse extends RestResponse
{
    private static final String LOG_TAG = "GetVersionResponse";


    Contact contact;

    public GetBotDataResponse(String data) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, "Parsing bots; "+ data);

        JSONObject bot = new JSONObject(data);

        Contact contact = new Contact();
        DirectoryContact dirContact = new DirectoryContact();
        dirContact.setType(DirectoryContact.DirectoryContactType.BOT);
        dirContact.setLastName(bot.getString(RestResponse.NAME));
        dirContact.setFirstName("");
        String contactJid = bot.getString(RestResponse.JID);
        dirContact.setImJabberId(contactJid);
        dirContact.setCorporateId(bot.getString(RestResponse.ID));
        contact.setDirectoryContact(dirContact);
        this.contact = contact;
    }

    public Contact getContact() {
        return contact;
    }
}

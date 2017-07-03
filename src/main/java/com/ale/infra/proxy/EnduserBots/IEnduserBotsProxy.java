package com.ale.infra.proxy.EnduserBots;

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;

import java.util.List;

/**
 * Created by wilsius on 08/08/16.
 */
public interface IEnduserBotsProxy {
    void getAllBots(int limit, int offset, final IGetAllBotsListener listener);
    void getBotData (String botId, final IGetBotDataListener listener);


    interface IGetAllBotsListener
    {
        void onGetAllBotsSuccess(List<DirectoryContact> dirContacts);
        void onGetAllBotsFailure();
    }

    interface IGetBotDataListener
    {
        void onGetBotDataSuccess(Contact contact);
        void onGetBotDataFailure();
    }
}

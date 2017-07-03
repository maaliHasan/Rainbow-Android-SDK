package com.ale.infra.manager;

import com.ale.infra.contact.Contact;
import com.ale.infra.manager.room.Room;

/**
 * Created by georges on 15/02/16.
 */
public interface IIMNotificationMgr {
    void cancelNotification();

    void cancelAll();

    void stop();

    Contact getLastContactNotified();

    void setImReadState(IMMessage imMessage);

    void filterConversation(Conversation conversation);

    void unfilterConversation(Conversation conversation);

    void addImNotifWithAppStopped(Contact contact, IMMessage imMessage);

    void addImNotifWithAppStopped(Room room, Contact contact, IMMessage imMessage);
}

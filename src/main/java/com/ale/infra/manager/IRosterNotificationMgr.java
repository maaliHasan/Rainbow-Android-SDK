package com.ale.infra.manager;


import com.ale.infra.contact.Contact;
import com.ale.infra.manager.room.Room;

public interface IRosterNotificationMgr {
    Contact getLastContactNotified();

    void displayNotification(Contact contact, boolean newNotif);

    void cancelNotification();

    void cancelAll();

    void stop();
}

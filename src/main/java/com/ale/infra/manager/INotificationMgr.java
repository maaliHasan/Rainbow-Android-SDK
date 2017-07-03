package com.ale.infra.manager;


import com.ale.infra.manager.room.Room;

public interface INotificationMgr {
    void displayNotification(boolean newNotif);

    void cancelNotification();

    void cancelAll();

    void stop();

}

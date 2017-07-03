package com.ale.infra.manager;

import com.ale.infra.xmpp.XmppConnection;

/**
 * Created by georges on 15/02/16.
 */
public interface INotificationFactory
{
    IIMNotificationMgr getIMNotificationMgr();

    void createIMNotificationMgr(XmppConnection connection, ChatMgr m_chatMgr);

    void stopIMNotificationMgr();
}

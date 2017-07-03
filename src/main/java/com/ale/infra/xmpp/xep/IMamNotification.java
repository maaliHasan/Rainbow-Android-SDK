package com.ale.infra.xmpp.xep;

import com.ale.infra.list.ArrayItemList;
import com.ale.infra.manager.IMMessage;

/**
 * Created by georges on 14/03/16.
 */
public interface IMamNotification {

    /**
     * notification sent when messages have been received
     * @param messages : list of messages
     * @param complete (boolean) : true if all archived messages have been downloaded
     */
    void complete(ArrayItemList<IMMessage> messages, boolean complete);

    void complete(String jid, boolean complete);

    void error(Exception e);

    void timeout();
}

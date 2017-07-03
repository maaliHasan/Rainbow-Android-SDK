package com.ale.infra.xmpp.xep;

import com.ale.infra.list.ArrayItemList;
import com.ale.infra.manager.IMMessage;

/**
 * Created by georges on 12/04/16.
 */
public interface ILastActivityNotification {
    void complete(long time);

    void error();
}

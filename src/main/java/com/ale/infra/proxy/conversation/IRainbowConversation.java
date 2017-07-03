package com.ale.infra.proxy.conversation;

import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.manager.IMMessage;

import java.util.ArrayList;

/**
 * Created by letrongh on 27/03/2017.
 */

public interface IRainbowConversation {
    String getId();
    String getJid();
    boolean isRoomType();
    IRainbowContact getContact();
    IMMessage getLastMessage(); // TODO: probably the same --> create another class to return?
    ArrayItemList<IMMessage> getMessages();
}

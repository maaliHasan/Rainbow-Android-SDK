package com.ale.listener;

import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.contact.RainbowPresence;

/**
 * Created by letrongh on 21/04/2017.
 */

public interface IRainbowContactsListener {

    void onContactUpdated(IRainbowContact updatedContact);

    void onPresenceChanged(IRainbowContact updatedContact, RainbowPresence presence);

}

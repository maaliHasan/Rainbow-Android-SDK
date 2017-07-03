package com.ale.infra.xmpp;

import com.ale.util.StringsUtil;

import org.jxmpp.util.XmppStringUtils;

/**
 * Created by wilsius on 05/09/16.
 */
public class XmppUtils extends XmppStringUtils{


    //return first part of jid => room jid
    public  static String getRoomJid (String jid) {
        return parseBareJid(jid);
    }


    //If jid = room/jid => return jid
    // If jd  = room/jid/ressource => return jid
    public static String getFromJid(String jid) {
        String from = parseResource(jid);

        if (StringsUtil.isNullOrEmpty(parseResource(from)))
            return from;
        else
            return parseBareJid(from);

    }
}

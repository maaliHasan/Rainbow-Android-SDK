package com.ale.infra.xmpp.xep.calllog;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PlainStreamElement;

/**
 * Created by georges on 15/01/16.
 */
public class DeleteCallLogRequestIQ extends IQ implements PlainStreamElement
{
    public static String ELEMENT = "delete";
    public static String NAMESPACE = "jabber:iq:telephony:call_log";
    private String peer;

    public DeleteCallLogRequestIQ(String peer)
    {
        super(ELEMENT, NAMESPACE);
        this.peer = peer;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml)
    {
        xml.attribute("peer", peer);
        xml.rightAngleBracket();

        return xml;
    }
}

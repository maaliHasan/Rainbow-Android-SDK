package com.ale.infra.xmpp.xep.calllog;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PlainStreamElement;

/**
 * Created by georges on 15/01/16.
 */
public class CallLogRequestIQ extends IQ implements PlainStreamElement
{
    public static String ELEMENT = "query";
    public static String NAMESPACE = "jabber:iq:telephony:call_log";
    private int lastMsgCtr;

    public CallLogRequestIQ(int lastMsgCtr)
    {
        super(ELEMENT, NAMESPACE);
        this.lastMsgCtr = lastMsgCtr;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml)
    {
        xml.rightAngleBracket();

        if (lastMsgCtr > 0)
        {
            xml.halfOpenElement("set");
            xml.optAttribute("xmlns", "http://jabber.org/protocol/rsm");
            xml.rightAngleBracket();
            xml.optElement("max", String.valueOf(lastMsgCtr));
            xml.optElement("before", "");
            xml.closeElement("set");
        }

        return xml;
    }
}

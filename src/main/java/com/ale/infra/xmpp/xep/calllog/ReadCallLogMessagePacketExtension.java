package com.ale.infra.xmpp.xep.calllog;

import com.ale.util.StringsUtil;

import org.jivesoftware.smack.packet.DefaultExtensionElement;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.xmlpull.v1.XmlPullParser;

/**
 * Created by grobert on 29/04/16.
 */
public class ReadCallLogMessagePacketExtension extends DefaultExtensionElement
{

    public static final String ELEMENT = "read";
    public static final String NAMESPACE = "urn:xmpp:telephony:call_log:receipts";
    private String callId;

    public ReadCallLogMessagePacketExtension(XmlPullParser parser)
    {
        super(ELEMENT, NAMESPACE);

        // example of CallLOg message to parse :

        //<read xmlns='urn:xmpp:telephony:call_log:receipts' call_id='15C9B6AE39C#7460#1947'/>


        // set the call_id from the read attribute:
        setCallId(parser.getAttributeValue(null, "call_id"));
    }

    public ReadCallLogMessagePacketExtension(String  callId)
    {
        super(ELEMENT, NAMESPACE);

        // example of CallLOg message to parse :

        //<read xmlns='urn:xmpp:telephony:call_log:receipts' call_id='15C9B6AE39C#7460#1947'/>


        // set the call_id from the read attribute:
        setCallId(callId);
    }

    public String getCallId()
    {
        return callId;
    }

    public void setCallId(String callId)
    {
        this.callId = callId;
    }

    @Override
    public String toString()
    {
        StringBuilder toXml = new StringBuilder();

        toXml.append("ReadCallLogMessagePacketExtension{");
        if (!StringsUtil.isNullOrEmpty(callId))
            toXml.append(", call_id='" + callId + '\'');

        toXml.append('}');

        return toXml.toString();
    }

    @Override
    public String toXML()
    {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.attribute("call_id", callId);
        xml.closeEmptyElement();
        return xml.toString();
    }
}
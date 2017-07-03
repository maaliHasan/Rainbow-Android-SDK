package com.ale.infra.xmpp.xep.calllog;

import com.ale.util.StringsUtil;

import org.jivesoftware.smack.packet.DefaultExtensionElement;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.xmlpull.v1.XmlPullParser;

/**
 * Created by grobert on 29/04/16.
 */
public class DeletedCallLogMessagePacketExtension extends DefaultExtensionElement
{

    public static final String ELEMENT = "deleted_call_log";
    public static final String NAMESPACE = "jabber:iq:notification:telephony:call_log";
    private String peer;

    public DeletedCallLogMessagePacketExtension(XmlPullParser parser)
    {
        super(ELEMENT, NAMESPACE);

        // example of CallLOg message to parse :

        //<deleted_call_log xmlns="jabber:iq:notification:telephony:call_log" peer="7e152a360ca648c69d339902d971ae87@demo-all-in-one-dev-1.opentouch.cloud" />


        // set the peer from the result attribute:
        setPeer(parser.getAttributeValue(null, "peer"));
    }

    public String getPeer()
    {
        return peer;
    }

    public void setPeer(String peer)
    {
        this.peer = peer;
    }

    @Override
    public String toString()
    {
        StringBuilder toXml = new StringBuilder();

        toXml.append("DeletedCallLogMessagePacketExtension{");
        if (!StringsUtil.isNullOrEmpty(peer))
            toXml.append(", peer='" + peer + '\'');

        toXml.append('}');

        return toXml.toString();
    }

    @Override
    public String toXML()
    {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.attribute("peer", peer);
        xml.closeEmptyElement();
        return xml.toString();
    }
}
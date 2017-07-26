package com.ale.infra.xmpp.xep.Room;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by wilsius on 07/07/2017.
 */

public class RoomConferenceEvent implements ExtensionElement {
    public static final String ELEMENT = "x";
    public static final String NAMESPACE = "jabber:x:audioconference";

    /**
     * original ID of the delivered message
     */
    private String m_Roomjid;
    private String m_message;
    private String m_confEndPointId;


    public RoomConferenceEvent(String jid, String message, String confEndPointId) {
        super();

        m_Roomjid = jid;
        m_message = message;
        m_confEndPointId = confEndPointId;
    }

    @Override
    public XmlStringBuilder toXML() {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.attribute("jid", m_Roomjid);
        xml.attribute("message", m_message);
        xml.attribute("confendpointid", m_confEndPointId);
        xml.closeEmptyElement();
        return xml;
    }

    /**
     * @param packet
     * @return the RoomConferenceEventType or null
     */
    public static RoomConferenceEvent from(Stanza packet) {
        return packet.getExtension(ELEMENT, NAMESPACE);
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    public static class Provider extends ExtensionElementProvider<RoomConferenceEvent> {

        @Override
        public RoomConferenceEvent parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException {
            String message = parser.getAttributeValue("", "message");
            String jid = parser.getAttributeValue("", "jid");
            String confendpointid = parser.getAttributeValue("", "confendpointid");
            // Advance to end of extension.
            parser.next();
            return new RoomConferenceEvent( jid,message, confendpointid);
        }
    }


    public String getRoomJid() {
        return m_Roomjid;
    }

    public String getMessage() {
        return m_message;
    }

    public String getConfEndPointId() {
        return m_confEndPointId;
    }


}

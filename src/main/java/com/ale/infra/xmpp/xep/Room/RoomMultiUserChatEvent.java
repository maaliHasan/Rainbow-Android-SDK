package com.ale.infra.xmpp.xep.Room;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by trunk1 on 23/01/2017.
 */

public class RoomMultiUserChatEvent implements ExtensionElement {
    public static final String ELEMENT = "event";
    public static final String NAMESPACE = "jabber:client";

    /**
     * original ID of the delivered message
     */
    private RoomEventType m_eventType = RoomEventType.UNDEFINED;
    private String m_jid;

    public enum RoomEventType
    {
        WELCOME("welcome"), INVITATION("invitation"), JOIN("join"), LEAVE("leave"), CLOSE("disconnect"), CONFERENCEADD ("conferenceAdd"), CONFERENCEREMOVED ("conferenceRemove"), UNDEFINED("undefined");
        private String value;

        RoomEventType(String value) {

            this.value = value;
        }

        @Override
        public String toString()
        {
            return value;
        }

        public static RoomEventType fromString(String text) {
            if (text != null) {
                for (RoomEventType status : RoomEventType.values()) {
                    if (text.equalsIgnoreCase(status.value)) {
                        return status;
                    }
                }
            }
            return UNDEFINED;
        }
    }

    public RoomMultiUserChatEvent(String name, String jid)
    {
        super();

        m_eventType = RoomEventType.fromString(name);
        m_jid = jid;
    }

    @Override
    public XmlStringBuilder toXML()
    {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.attribute("jid", m_eventType);
        xml.attribute("thread", m_jid);
        xml.closeEmptyElement();
        return xml;
    }

    /**
     *
     * @param packet
     * @return the GroupChatInvitation or null
     */
    public static RoomMultiUserChatEvent from(Stanza packet) {
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

    public static class Provider extends ExtensionElementProvider<RoomMultiUserChatEvent> {

        @Override
        public RoomMultiUserChatEvent parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException {
            String name = parser.getAttributeValue("", "name");
            String jid = parser.getAttributeValue("", "jid");
            // Advance to end of extension.
            parser.next();
            return new RoomMultiUserChatEvent(name, jid);
        }
    }

    public RoomEventType getEventType() {
        return m_eventType;
    }

    public String getJid() {
        return m_jid;
    }

}


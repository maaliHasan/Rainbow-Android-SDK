package com.ale.infra.xmpp.xep.message;

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

public class UserVcardUpdateEvent implements ExtensionElement {
    public static final String ELEMENT = "x";
    public static final String NAMESPACE = "vcard-temp:x:update";

    /**
     * original ID of the delivered message
     */
    private String m_data;


    public UserVcardUpdateEvent(String data)
    {
        m_data = data;
    }

    @Override
    public XmlStringBuilder toXML()
    {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.attribute("data", m_data);
        xml.closeEmptyElement();
        return xml;
    }

    /**
     *
     * @param packet
     * @return the GroupChatInvitation or null
     */
    public static UserVcardUpdateEvent from(Stanza packet) {
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

    public static class Provider extends ExtensionElementProvider<UserVcardUpdateEvent> {

        @Override
        public UserVcardUpdateEvent parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException {
            String data = parser.getAttributeValue("", "data");
            // Advance to end of extension.
            parser.next();
            return new UserVcardUpdateEvent(data);
        }
    }

    public String getData() {
        return m_data;
    }

}


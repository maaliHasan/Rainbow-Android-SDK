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
    private boolean m_avatar = false;


    public UserVcardUpdateEvent(String data, boolean avatar)
    {
        m_data = data;
        m_avatar = avatar;
    }

    @Override
    public XmlStringBuilder toXML()
    {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        if (m_data != null)
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
            boolean b = false;
            int eventType = parser.next();
            while (eventType != XmlPullParser.END_TAG) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String elementName = parser.getName();

                        switch (elementName) {
                            case "avatar":
                                b = true;
                                break;

                            default:
                                break;
                        }
                }
                eventType = parser.next();
            }
            return new UserVcardUpdateEvent(data,b);
        }
    }

    public String getData() {
        return m_data;
    }

    public boolean isAvatar() {
        return m_avatar;
    }

}


package com.ale.infra.xmpp.xep.MUC;

import org.jivesoftware.smack.packet.DefaultExtensionElement;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.EmbeddedExtensionProvider;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smackx.muc.packet.GroupChatInvitation;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by georges on 08/06/16.
 */
public class RainbowGroupChatInvitation implements ExtensionElement {

    public static final String ELEMENT = "x";

    public static final String NAMESPACE = "jabber:x:conference";

    /**
     * original ID of the delivered message
     */
    private String thread;
    private String roomAddress;

    public RainbowGroupChatInvitation(String roomAddress, String thread)
    {
        super();

        this.roomAddress = roomAddress;
        this.thread = thread;
    }

    @Override
    public XmlStringBuilder toXML()
    {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.attribute("jid", roomAddress);
        xml.attribute("thread", thread);
        xml.closeEmptyElement();
        return xml;
    }

    /**
     *
     * @param packet
     * @return the GroupChatInvitation or null
     */
    public static RainbowGroupChatInvitation from(Stanza packet) {
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

    /**
     * This Provider parses and returns RainbowGroupChatInvitation packets.
     */
//    public static class Provider extends EmbeddedExtensionProvider<RainbowGroupChatInvitation>
//    {
//        @Override
//        protected RainbowGroupChatInvitation createReturnExtension(String currentElement, String currentNamespace,
//                                                        Map<String, String> attributeMap, List<? extends ExtensionElement> content)
//        {
//            return new RainbowGroupChatInvitation(attributeMap.get("jid"), attributeMap.get("thread"));
//        }
//    }

    public static class Provider extends ExtensionElementProvider<RainbowGroupChatInvitation> {

        @Override
        public RainbowGroupChatInvitation parse(XmlPullParser parser,
                                         int initialDepth) throws XmlPullParserException,
                IOException {
            String roomAddress = parser.getAttributeValue("", "jid");
            String thread = parser.getAttributeValue("", "thread");
            // Advance to end of extension.
            parser.next();
            return new RainbowGroupChatInvitation(roomAddress, thread);
        }
    }

    public String getRoomAddress() {
        return roomAddress;
    }

    public String getThread() {
        return thread;
    }
}


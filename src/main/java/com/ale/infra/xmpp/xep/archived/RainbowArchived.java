package com.ale.infra.xmpp.xep.archived;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.provider.EmbeddedExtensionProvider;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;

import java.util.List;
import java.util.Map;

/**
 * Created by georges on 27/01/2017.
 */

public class RainbowArchived implements ExtensionElement
{
    public static final String ELEMENT = "archived";
    public static final String NAMESPACE = "urn:xmpp:mam:tmp";

    /**
     * original ID of the delivered message
     */
    private final String stamp;

    public RainbowArchived(String stamp)
    {
        this.stamp = StringUtils.maybeToString(stamp);
    }

    public String getStamp()
    {
        return stamp;
    }

    @Override
    public String getElementName()
    {
        return ELEMENT;
    }

    @Override
    public String getNamespace()
    {
        return NAMESPACE;
    }

    @Override
    public XmlStringBuilder toXML()
    {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.attribute("stamp", stamp);
        xml.closeEmptyElement();
        return xml;
    }

    /**
     * Get the {@link DeliveryReceipt} extension of the packet, if any.
     *
     * @param p the packet
     * @return the {@link DeliveryReceipt} extension or {@code null}
     * @deprecated use {@link #from(Message)} instead
     */
    @Deprecated
    public static DeliveryReceipt getFrom(Message p) {
        return from(p);
    }

    /**
     * Get the {@link DeliveryReceipt} extension of the message, if any.
     *
     * @param message the message.
     * @return the {@link DeliveryReceipt} extension or {@code null}
     */
    public static DeliveryReceipt from(Message message) {
        return message.getExtension(ELEMENT, NAMESPACE);
    }

    /**
     * This Provider parses and returns DeliveryReceipt packets.
     */
    public static class Provider extends EmbeddedExtensionProvider<DeliveryReceipt>
    {

        @Override
        protected DeliveryReceipt createReturnExtension(String currentElement, String currentNamespace,
                                                        Map<String, String> attributeMap, List<? extends ExtensionElement> content)
        {
            return new DeliveryReceipt(attributeMap.get("stamp"));
        }

    }
}

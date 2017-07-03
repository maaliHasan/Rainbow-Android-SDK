package com.ale.infra.xmpp.xep.DeletedReceipt;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.provider.EmbeddedExtensionProvider;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;

import java.util.List;
import java.util.Map;

/**
 * Created by georges on 08/06/16.
 */
public class RainbowDeletedReceipt implements ExtensionElement
{
    public static final String NAMESPACE = "jabber:iq:notification";
    public static final String ELEMENT = "deleted";

    /**
     * original ID of the delivered message
     */
    private final String id;
    private final String with;

    public RainbowDeletedReceipt(String id,String with)
    {
        this.id = StringUtils.requireNotNullOrEmpty(id, "id must not be null");
        this.with = StringUtils.requireNotNullOrEmpty(with, "with must not be null");
    }

    public String getId()
    {
        return id;
    }

    public String getWith()
    {
        return with;
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
        xml.attribute("id", id);
        xml.attribute("with", with);
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
            return new DeliveryReceipt(attributeMap.get("id"));
        }

    }
}

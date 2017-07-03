package com.ale.infra.xmpp.xep.DeliveryReceipt;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.EmbeddedExtensionProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;

import java.util.List;
import java.util.Map;

/**
 * Created by georges on 08/06/16.
 */
public class RainbowDeliveryReceivedReceipt extends DeliveryReceipt {

    public static final String EVENT_RECEIVED = "received";
    public static final String EVENT_READ = "read";

    public static final String ENTITY_CLIENT = "client";
    public static final String ENTITY_SERVER = "server";

    /**
     * original ID of the delivered message
     */
    private final String event;
    private final String entity;
    private boolean isMultiChat;

    public RainbowDeliveryReceivedReceipt(String id, String event, String entity, boolean isMultiChat)
    {
        super(id);

        this.event = event;
        this.entity = entity;
        this.isMultiChat = isMultiChat;
    }

    @Override
    public XmlStringBuilder toXML()
    {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.attribute("id", getId());
        xml.attribute("event", event);
        xml.attribute("entity", entity);
        if (isMultiChat) {
            xml.attribute("type", "muc");
        }
        xml.closeEmptyElement();
        return xml;
    }

    /**
     * This Provider parses and returns RainbowDeletedReceipt packets.
     */
    public static class Provider extends EmbeddedExtensionProvider<RainbowDeliveryReceivedReceipt>
    {

        @Override
        protected RainbowDeliveryReceivedReceipt createReturnExtension(String currentElement, String currentNamespace,
                                                        Map<String, String> attributeMap, List<? extends ExtensionElement> content)
        {
            return new RainbowDeliveryReceivedReceipt(attributeMap.get("id"), attributeMap.get("event"), attributeMap.get("entity"),false);
        }
    }

    public String getEvent() {
        return event;
    }

    public String getEntity() {
        return entity;
    }
}


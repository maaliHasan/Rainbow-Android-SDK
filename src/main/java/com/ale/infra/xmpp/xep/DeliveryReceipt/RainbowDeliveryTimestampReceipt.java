package com.ale.infra.xmpp.xep.DeliveryReceipt;

import com.ale.util.DateTimeUtil;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;

import java.util.Date;


public class RainbowDeliveryTimestampReceipt implements ExtensionElement {

    public static final String NAMESPACE = "urn:xmpp:receipts";
    public static final String ELEMENT = "timestamp";

    private final Date m_timestampValue;
    private final long m_timestamp;

    public RainbowDeliveryTimestampReceipt(Date timestampValue, Long timestamp) {
        m_timestampValue = timestampValue;
        this.m_timestamp = timestamp;
    }

    @Override
    public XmlStringBuilder toXML()
    {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.attribute("value", getTimestampValue());
        xml.closeEmptyElement();
        return xml;
    }

    private String getTimestampValue() {
        return DateTimeUtil.getStringStampFromDate(m_timestampValue);
    }

    public Date getTimestampValueInDateFormat() {
        return m_timestampValue;
    }

    public Long getTimestamp() {
        return m_timestamp;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }
}

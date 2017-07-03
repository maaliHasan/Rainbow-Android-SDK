package com.ale.infra.xmpp.xep.DeliveryReceipt;

import com.ale.util.DateTimeUtil;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.jivesoftware.smack.packet.DefaultExtensionElement;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Date;


public class TimestampReceiptPacketExtension extends DefaultExtensionElement {

    private static final String LOG_TAG = "TimestampReceiptPacketExtension";

    private Date m_timestampValue;


    private Long timestamp;

    private String timestampValue;

    public TimestampReceiptPacketExtension(XmlPullParser parser) {
        super(RainbowDeliveryTimestampReceipt.ELEMENT, RainbowDeliveryTimestampReceipt.NAMESPACE);

        try {
            String name = parser.getName();
            int eventType = parser.getEventType();

            while ( eventType!=XmlPullParser.END_DOCUMENT ) {
                if (!StringsUtil.isNullOrEmpty(name) &&
                        (eventType != XmlPullParser.END_TAG)) {
                    switch (name) {
                        case "timestamp":
//                            Log.getLogger().verbose(LOG_TAG, "Received Tag detected");
                            setTimestampValue(parser.getAttributeValue(null, "value"));
                            setTimestamp(parser.getAttributeValue(null, "value"));
                            break;
                        default:
                            break;
                    }
                }

                parser.next();
                name = parser.getName();
                eventType = parser.getEventType();
                if (!StringsUtil.isNullOrEmpty(name) && (name.equals(RainbowDeliveryTimestampReceipt.ELEMENT)) &&
                        (eventType == XmlPullParser.END_TAG)) {
//                    Log.getLogger().verbose(LOG_TAG, "received END_TAG reached");
                    break;
                }
            }
        } catch (XmlPullParserException e) {
            Log.getLogger().error(LOG_TAG, "parseTimeStampStanza; XmlPullParserException " + e.getMessage());
        } catch (IOException e) {
            Log.getLogger().error(LOG_TAG, "parseTimeStampStanza; IOException " + e.getMessage());
        }


    }

    private void setTimestampValue(String timestampValue) {
        if (timestampValue != null) {
            m_timestampValue = DateTimeUtil.getDateFromStringStamp(timestampValue);
        }
    }

    public Date getTimestampValue() {
        return m_timestampValue;
    }


    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = DateTimeUtil.getStampFromStringDate(timestamp);
    }

}

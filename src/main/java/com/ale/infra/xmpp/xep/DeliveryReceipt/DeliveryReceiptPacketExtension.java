package com.ale.infra.xmpp.xep.DeliveryReceipt;

import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.jivesoftware.smack.packet.DefaultExtensionElement;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


/**
 * Created by georges on 09/06/16.
 */
public class DeliveryReceiptPacketExtension extends DefaultExtensionElement {

    private static final String LOG_TAG = "RainbowArchivedPacketExtension";

    private String queryid;
    private String event;
    private String entity;

    public DeliveryReceiptPacketExtension(XmlPullParser parser) {
        super(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE);

        // example of MAM message to parse :

//  <message xmlns='jabber:client' from='j_9124927308@openrainbow.net/mobile_android_1.9.999_2dc124c7-6546-4ee4-9939-f37815136ef4' to='j_5925263767@openrainbow.net' id='Ox9j4-82'>
//  <received xmlns='urn:xmpp:receipts' id='yibgH-140' event='read' entity='client'/></message>

        try {


            String name = parser.getName();
            int eventType = parser.getEventType();

            // parse xml structure until end of tag called "result"
            // do not parse the whole xml structure since this parser is used afterwards
            // warning : we have to keep the same initial depth when we have finished to parse.
            while ( eventType!=XmlPullParser.END_DOCUMENT ) {
                if (!StringsUtil.isNullOrEmpty(name) &&
                        (eventType != XmlPullParser.END_TAG)) {
                    switch (name) {
                        case "received":
//                            Log.getLogger().verbose(LOG_TAG, "Received Tag detected");
                            setQueryid(parser.getAttributeValue(null, "id"));
                            setEntity(parser.getAttributeValue(null, "entity"));
                            setEvent(parser.getAttributeValue(null, "event"));
                            break;
                        default:
                            break;
                    }
                }

                parser.next();
                name = parser.getName();
                eventType = parser.getEventType();
                if (!StringsUtil.isNullOrEmpty(name) && (name.equals(DeliveryReceipt.ELEMENT)) &&
                        (eventType == XmlPullParser.END_TAG)) {
//                    Log.getLogger().verbose(LOG_TAG, "received END_TAG reached");
                    break;
                }
            }

        }catch (XmlPullParserException e) {
            Log.getLogger().error(LOG_TAG, "parseMamStanza; XmlPullParserException " + e.getMessage());
        }catch (IOException e) {
            Log.getLogger().error(LOG_TAG, "parseMamStanza; IOException " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "MamMessagePacketExtension{" +
                ", queryid='" + queryid + '\'' +
                ", event='" + event + '\'' +
                ", entity='" + entity + '\'' +
                '}';
    }

    @Override
    public String toXML() {
        // TODO : usefull ?
        return null;
    }

    public void setQueryid(String queryid) {
        this.queryid = queryid;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getQueryid() {
        return queryid;
    }

    public String getEvent() {
        return event;
    }

    public String getEntity() {
        return entity;
    }

}

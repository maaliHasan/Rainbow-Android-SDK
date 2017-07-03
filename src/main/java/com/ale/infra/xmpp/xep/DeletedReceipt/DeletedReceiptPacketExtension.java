package com.ale.infra.xmpp.xep.DeletedReceipt;

import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.jivesoftware.smack.packet.DefaultExtensionElement;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


/**
 * Created by georges on 09/06/16.
 */
public class DeletedReceiptPacketExtension extends DefaultExtensionElement {

    private static final String LOG_TAG = "DeletedReceiptPacketExtension";


    private String queryid;
    private String with;

    public DeletedReceiptPacketExtension(XmlPullParser parser) {
        super(RainbowDeletedReceipt.ELEMENT, RainbowDeletedReceipt.NAMESPACE);

        // example of message to parse :

//       <message xmlns='jabber:client' from='demo-all-in-one-dev-1.opentouch.cloud' to='31e00f59a34f44aa86ebb31c2f9e00f4@demo-all-in-one-dev-1.opentouch.cloud' type='chat'>
// <deleted id='all' with='c8b7155cabbe440da91d735af958e6f7@demo-all-in-one-dev-1.opentouch.cloud' xmlns='jabber:iq:notification'/></message>

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
                        case RainbowDeletedReceipt.ELEMENT:
//                            Log.getLogger().verbose(LOG_TAG, "Received Tag detected");
                            setQueryid(parser.getAttributeValue(null, "id"));
                            setWith(parser.getAttributeValue(null, "with"));
                            break;
                        default:
                            break;
                    }
                }

                parser.next();
                name = parser.getName();
                eventType = parser.getEventType();
                if (!StringsUtil.isNullOrEmpty(name) && (name.equals(RainbowDeletedReceipt.ELEMENT)) &&
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
                ", with='" + with + '\'' +
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

    public void setWith(String with) {
        this.with = with;
    }

    public String getQueryid() {
        return queryid;
    }

    public String getWith() {
        return with;
    }

}

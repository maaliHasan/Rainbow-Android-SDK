package com.ale.infra.xmpp.xep.archived;

import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.jivesoftware.smack.packet.DefaultExtensionElement;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


/**
 * Created by georges on 09/06/16.
 */
public class RainbowArchivedPacketExtension extends DefaultExtensionElement {

    private static final String LOG_TAG = "RainbowArchivedPacketExtension";

    private String stamp;
    private String event;
    private String entity;

    public RainbowArchivedPacketExtension(XmlPullParser parser) {
        super(RainbowArchived.ELEMENT, RainbowArchived.NAMESPACE);

        // example of Archived message to parse :

//  <archived by='demo-all-in-one-dev-1.opentouch.cloud' xmlns='urn:xmpp:mam:tmp' stamp='2017-01-27T09:51:15.464356Z'/>

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
                        case RainbowArchived.ELEMENT:
                            setStamp(parser.getAttributeValue(null, "stamp"));
                            break;
                        default:
                            break;
                    }
                }

                parser.next();
                name = parser.getName();
                eventType = parser.getEventType();
                if (!StringsUtil.isNullOrEmpty(name) && (name.equals(RainbowArchived.ELEMENT)) &&
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
        return "RainbowArchivedPacketExtension{" +
                " stamp='" + stamp + '\'' +
                '}';
    }

    @Override
    public String toXML() {
        // TODO : usefull ?
        return null;
    }

    public void setStamp(String stamp) {
        this.stamp = stamp;
    }

    public String getStamp() {
        return stamp;
    }

}

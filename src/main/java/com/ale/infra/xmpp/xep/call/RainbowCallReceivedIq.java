package com.ale.infra.xmpp.xep.call;


import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Element;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by jhiertz on 13/10/2016.
 */

public class RainbowCallReceivedIq extends IQProvider<CallIq> {

    @Override
    public CallIq parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {
        // Define the data we are trying to collect with sane defaults
        String phoneNumber = null;
        boolean directCall = false;

        // Start parsing loop
        outerloop: while(true) {
            int eventType = parser.getEventType();
            switch(eventType) {
                case XmlPullParser.START_TAG:
                    String elementName = parser.getName();
                    switch (elementName) {
                        case "call":
                            phoneNumber = parser.getAttributeValue(null,"phoneNumber");
                            directCall = Boolean.valueOf(parser.getAttributeValue(null,"directCall"));
                            break;
                    }
                    break;
                case XmlPullParser.END_TAG:
                    // Abort condition: if the are on a end tag (closing element) of the same depth
                    if (parser.getDepth() == initialDepth) {
                        break outerloop;
                    }
                    break;
            }
            parser.next();
        }

        // Construct the IQ instance at the end of parsing, when all data has been collected
        return new CallIq("", "", phoneNumber, directCall);
    }
}

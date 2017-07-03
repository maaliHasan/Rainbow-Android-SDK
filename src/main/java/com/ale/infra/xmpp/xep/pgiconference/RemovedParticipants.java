package com.ale.infra.xmpp.xep.pgiconference;

import com.ale.util.StringsUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by georges on 16/05/2017.
 */

public class RemovedParticipants {

    public static final String ELEMENT = "removed-participants";

//		<removed-participants>
//			<participant-id>5911be845defe84fdd1fd43c</participant-id>
//		</removed-participants>

    public List<String> parse(XmlPullParser parser) throws XmlPullParserException, IOException {

        List<String> removedParticipants = new ArrayList<>();

        String name = parser.getName();
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (!StringsUtil.isNullOrEmpty(name)) {
                switch (name) {
                    case "participant-id":
                        String participantId = parser.nextText();
                        if( !StringsUtil.isNullOrEmpty(participantId) )
                            removedParticipants.add(participantId);
                        break;

                    default:
                        break;
                }
            }

            parser.next();
            name = parser.getName();
            eventType = parser.getEventType();
            if (!StringsUtil.isNullOrEmpty(name) && (name.equals(ELEMENT)) &&
                    (eventType == XmlPullParser.END_TAG)) {
                break;
            }
        }

        return removedParticipants;
    }

}

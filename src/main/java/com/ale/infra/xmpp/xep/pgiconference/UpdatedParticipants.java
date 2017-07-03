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

public class UpdatedParticipants {

    public static final String ELEMENT = "updated-participants";
    public static final String PARTICIPANTS_ELEMENT = "participants";


    public List<ConferenceParticipant> parse(XmlPullParser parser) throws XmlPullParserException, IOException {

        List<ConferenceParticipant> updatedParticipants = new ArrayList<>();


        String name = parser.getName();
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (!StringsUtil.isNullOrEmpty(name)) {
                switch (name) {
                    case ConferenceParticipant.ELEMENT:
                        ConferenceParticipant confPart = new ConferenceParticipant();
                        updatedParticipants.add(confPart.parse(parser));
                        break;

                    default:
                        break;
                }
            }

            parser.next();
            name = parser.getName();
            eventType = parser.getEventType();
            if (!StringsUtil.isNullOrEmpty(name) && (name.equals(ELEMENT) || name.equals(PARTICIPANTS_ELEMENT)) &&
                    (eventType == XmlPullParser.END_TAG)) {
                break;
            }
        }

        return updatedParticipants;
    }

}

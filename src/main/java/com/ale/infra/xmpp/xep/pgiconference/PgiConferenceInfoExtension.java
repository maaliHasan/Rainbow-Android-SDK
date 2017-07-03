package com.ale.infra.xmpp.xep.pgiconference;

import com.ale.util.StringsUtil;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

/**
 * Created by georges on 09/05/2017.
 */

public class PgiConferenceInfoExtension implements ExtensionElement {

    public static final String ELEMENT = "conference-info";
    public static final String NAMESPACE = "jabber:iq:conference";



    private String m_confId;
    private ConferenceState m_state;
    private List<ConferenceParticipant> m_updatedParticipants;
    private List<String> m_removedParticipants;
    private List<String> m_talkersParticipants;


//    <conference-info xmlns='jabber:iq:conference'>
//        <conference-id>5911be835defe84fdd1fd43b</conference-id>
//        <conference-state>
//            <active>true</active>
//            <talker-active>true</talker-active>
//            <recording-started>false</recording-started>
//            <participant-count>1</participant-count>
//        </conference-state>
//        <participants>
//            <participant>
//                <participant-id>5911be845defe84fdd1fd43c</participant-id>
//                <jid-im>9aebd4c25f6f4c0487d9f48ef9403a48@aio-pcg-all-in-one-dev-1.opentouch.cloud</jid-im>
//                <phone-number>+33668056253</phone-number>
//                <role>member</role>
//                <mute>off</mute>
//                <hold>off</hold>
//                <cnx-state>ringing</cnx-state>
//            </participant>
//        </participants>
//    </conference-info>

    public PgiConferenceInfoExtension() {
    }

    public PgiConferenceInfoExtension(String confId,ConferenceState state,List<ConferenceParticipant> updatedParticipants,List<String> removedParticipants,List<String> talkers) {
        m_confId = confId;
        m_state = state;
        m_updatedParticipants = updatedParticipants;
        m_removedParticipants = removedParticipants;
        m_talkersParticipants = talkers;
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    public ConferenceState getState() {
        return m_state;
    }

    public void setState(ConferenceState state) {
        m_state = state;
    }

    public String getConfId() {
        return m_confId;
    }

    public List<ConferenceParticipant> getUpdatedParticipants() {
        return m_updatedParticipants;
    }

    @Override
    public String toXML() {
        StringBuilder xml = new StringBuilder();
        xml.append(String.format("<%s xmlns='%s'>", ELEMENT, NAMESPACE));

        if ( !StringsUtil.isNullOrEmpty(m_confId) )
            xml.append(String.format("<conference-id>%s</conference-id>", m_confId));

        xml.append(String.format("</%s>", ELEMENT));
        return xml.toString();
    }

    public List<String> getRemovedParticipants() {
        return m_removedParticipants;
    }

    public List<String> getTalkersParticipants() {
        return m_talkersParticipants;
    }

//    <conference-info xmlns='jabber:iq:conference'>
//        <conference-id>5911be835defe84fdd1fd43b</conference-id>
//        <conference-state>
//            <active>true</active>
//            <talker-active>true</talker-active>
//            <recording-started>false</recording-started>
//            <participant-count>1</participant-count>
//        </conference-state>
//        <participants>
//            <participant>
//                <participant-id>5911be845defe84fdd1fd43c</participant-id>
//                <jid-im>9aebd4c25f6f4c0487d9f48ef9403a48@aio-pcg-all-in-one-dev-1.opentouch.cloud</jid-im>
//                <phone-number>+33668056253</phone-number>
//                <role>member</role>
//                <mute>off</mute>
//                <hold>off</hold>
//                <cnx-state>ringing</cnx-state>
//            </participant>
//        </participants>
//    </conference-info>

    public static class Provider extends ExtensionElementProvider<PgiConferenceInfoExtension> {

        @Override
        public PgiConferenceInfoExtension parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException {

            String confId = "";
            ConferenceState confState = null;
            List<ConferenceParticipant> updatedParticipants = null;
            List<String> removedParticipants = null;
            List<String> talkers = null;

            String name = parser.getName();
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (!StringsUtil.isNullOrEmpty(name)) {
                    switch (name) {
                        case "conference-id":
                            confId = parser.nextText();
                            break;
                        case ConferenceState.ELEMENT:
                            confState = new ConferenceState();
                            confState.parse(parser);
                            break;

                        case UpdatedParticipants.PARTICIPANTS_ELEMENT:
                        case UpdatedParticipants.ELEMENT:
                            UpdatedParticipants updatedParts = new UpdatedParticipants();
                            updatedParticipants = updatedParts.parse(parser);
                            break;

                        case RemovedParticipants.ELEMENT:
                            RemovedParticipants removedParts = new RemovedParticipants();
                            removedParticipants = removedParts.parse(parser);
                            break;

                        case Talkers.ELEMENT:
                            Talkers talkersPart = new Talkers();
                            talkers = talkersPart.parse(parser);
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

            return new PgiConferenceInfoExtension(confId, confState, updatedParticipants, removedParticipants, talkers);
        }
    }

}

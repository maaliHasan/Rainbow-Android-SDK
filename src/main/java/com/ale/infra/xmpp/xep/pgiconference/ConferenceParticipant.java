package com.ale.infra.xmpp.xep.pgiconference;

import com.ale.util.StringsUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by georges on 09/05/2017.
 */

public class ConferenceParticipant {

    public static final String ELEMENT = "participant";

    private String m_participantId;
    private String m_jidIm;
    private String m_phoneNumber;
    private String m_role;
    private String m_cnxState;
    private boolean m_mute = false;
    private boolean m_hold = false;

//    <participant>
//        <participant-id>5911be845defe84fdd1fd43c</participant-id>
//        <jid-im>9aebd4c25f6f4c0487d9f48ef9403a48@aio-pcg-all-in-one-dev-1.opentouch.cloud</jid-im>
//        <phone-number>+33668056253</phone-number>
//        <role>member</role>
//        <mute>off</mute>
//        <hold>off</hold>
//        <cnx-state>ringing</cnx-state>
//    </participant>

    public ConferenceParticipant parse(XmlPullParser parser) throws XmlPullParserException, IOException {

        String name = parser.getName();
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (!StringsUtil.isNullOrEmpty(name)) {
                switch (name) {
                    case "participant-id":
                        m_participantId = parser.nextText();
                        break;

                    case "jid-im":
                        m_jidIm = parser.nextText();
                        break;

                    case "phone-number":
                        m_phoneNumber = parser.nextText();
                        break;

                    case "role":
                        m_role = parser.nextText();
                        break;

                    case "cnx-state":
                        m_cnxState = parser.nextText();
                        break;

                    case "mute":
                        String muteState = parser.nextText();
                        if( !StringsUtil.isNullOrEmpty(muteState) && muteState.equals("on"))
                            m_mute = true;
                        else
                            m_mute = false;
                        break;

                    case "hold":
                        String hold = parser.nextText();
                        if( !StringsUtil.isNullOrEmpty(hold) && hold.equals("on"))
                            m_hold = true;
                        else
                            m_hold = false;
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

        return this;
    }

    public String getParticipantId() {
        return m_participantId;
    }

    public void setParticipantId(String participantId) {
        m_participantId = participantId;
    }

    public String getJidIm() {
        return m_jidIm;
    }

    public void setJidIm(String jidIm) {
        m_jidIm = jidIm;
    }

    public String getPhoneNumber() {
        return m_phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        m_phoneNumber = phoneNumber;
    }

    public String getRole() {
        return m_role;
    }

    public void setRole(String role) {
        m_role = role;
    }

    public String getCnxState() {
        return m_cnxState;
    }

    public void setCnxState(String cnxState) {
        m_cnxState = cnxState;
    }

    public boolean isMuted() {
        return m_mute;
    }

    public void setMute(boolean mute) {
        m_mute = mute;
    }

    public boolean isHold() {
        return m_hold;
    }

    public void setHold(boolean hold) {
        m_hold = hold;
    }
}

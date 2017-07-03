package com.ale.infra.xmpp.xep.pgiconference;

import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by georges on 09/05/2017.
 */

public class ConferenceState {
    public static final String ELEMENT = "conference-state";

    private boolean m_confActive = false;
    private boolean m_talkerActive = false;
    private boolean m_recordingStarted = false;
    private int m_participantCount = 0;


//        <conference-state>
//            <active>true</active>
//            <talker-active>true</talker-active>
//            <recording-started>false</recording-started>
//            <participant-count>1</participant-count>
//        </conference-state>

    public void parse(XmlPullParser parser) throws XmlPullParserException, IOException {

        String name = parser.getName();
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (!StringsUtil.isNullOrEmpty(name)) {
                switch (name) {
                    case "active":
                        String confActive = parser.nextText();
                        if (!StringsUtil.isNullOrEmpty(confActive) && confActive.equals("true"))
                            m_confActive = true;
                        else
                            m_confActive = false;
                        break;

                    case "talker-active":
                        String talkerActive = parser.nextText();
                        if( !StringsUtil.isNullOrEmpty(talkerActive) && talkerActive.equals("true"))
                            m_talkerActive = true;
                        else
                            m_talkerActive = false;
                        break;

                    case "recording-started":
                        String recordingStarted = parser.nextText();
                        if( !StringsUtil.isNullOrEmpty(recordingStarted) && recordingStarted.equals("true"))
                            m_recordingStarted = true;
                        else
                            m_recordingStarted = false;
                        break;

                    case "participant-count":
                        String count = parser.nextText();
                        if( !StringsUtil.isNullOrEmpty(count) )
                            m_participantCount = Integer.valueOf(count);
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

    }

    public boolean isConfActive() {
        return m_confActive;
    }

    public void setConfActive(boolean confActive) {
        this.m_confActive = confActive;
    }

    public boolean isTalkerActive() {
        return m_talkerActive;
    }

    public void setTalkerActive(boolean talkerActive) {
        this.m_talkerActive = talkerActive;
    }

    public boolean isRecordingStarted() {
        return m_recordingStarted;
    }

    public void setRecordingStarted(boolean recordingStarted) {
        this.m_recordingStarted = recordingStarted;
    }

    public int getParticipantCount() {
        return m_participantCount;
    }

//    private boolean m_confActive = false;
//    private boolean m_talkerActive = false;
//    private boolean m_recordingStarted = false;
//    private int m_participantCount = 0;

    public void dumpInLog(String dumpLogTag) {
        Log.getLogger().info(dumpLogTag, "    confActive="+m_confActive);
        Log.getLogger().info(dumpLogTag, "    talkerActive="+m_talkerActive);
        Log.getLogger().info(dumpLogTag, "    recordingStarted="+m_recordingStarted);
        Log.getLogger().info(dumpLogTag, "    participantCount="+m_participantCount);
    }
}

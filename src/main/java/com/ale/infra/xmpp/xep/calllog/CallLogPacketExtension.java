package com.ale.infra.xmpp.xep.calllog;

import com.ale.util.DateTimeUtil;
import com.ale.util.StringsUtil;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


/**
 * Created by georges on 06/03/17.
 */
public class CallLogPacketExtension implements ExtensionElement
{

    public static final String ELEMENT = "call_log";
    public static final String NAMESPACE = "jabber:iq:notification:telephony:call_log";

    public static final String STATE_MISSED = "missed";
    public static final String STATE_ANSWERED = "answered";


    private String m_callerJid;
    private String m_calleeJid;
    private String m_state;
    private String m_duration;
    private String m_media;
    private String m_type;
    private String m_callId;
    private Boolean m_isAck;

    //      <call_log xmlns='jabber:iq:notification:telephony:call_log' type='webrtc'>
    //          <caller>cf9270dc883c48908ca62871463284cb@demo-all-in-one-dev-1.opentouch.cloud</caller>
    //          <callee>7e152a360ca648c69d339902d971ae87@demo-all-in-one-dev-1.opentouch.cloud</callee>
    //          <state>answered</state>
    //          <media>audio</media>
    //          <duration>10560</duration>
    //          <call_id>d4bltnan0es4j</call_id>
    //          <ack read='false'/>
    //      </call_log>


    public CallLogPacketExtension()
    {
    }

    public CallLogPacketExtension(String caller, String callee, String state, String duration)
    {
        m_callerJid = caller;
        m_calleeJid = callee;
        m_state = state;
        m_duration = duration;
    }

    @Override
    public String getElementName()
    {
        return ELEMENT;
    }

    @Override
    public String getNamespace()
    {
        return NAMESPACE;
    }

    public String getCallerJid()
    {
        return m_callerJid;
    }

    public void setCallerJid(String caller)
    {
        m_callerJid = caller;
    }

    public String getCalleeJid()
    {
        return m_calleeJid;
    }

    public void setCalleeJid(String callee)
    {
        m_calleeJid = callee;
    }

    public String getState()
    {
        return m_state;
    }

    public String getMedia()
    {
        return m_media;
    }

    public String getCallId()
    {
        return m_callId;
    }

    public Boolean isAck()
    {
        return m_isAck;
    }

    public String getType()
    {
        return m_type;
    }

    public void setState(String state)
    {
        m_state = state;
    }

    public boolean isAnswered()
    {
        return !StringsUtil.isNullOrEmpty(m_state) && m_state.equals(STATE_ANSWERED);
    }

    public String getDuration()
    {
        return m_duration;
    }

    public void setDuration(String duration)
    {
        m_duration = duration;
    }

    @Override
    public String toXML()
    {
        StringBuilder xml = new StringBuilder();
        xml.append(String.format("<%s xmlns='%s'", ELEMENT, NAMESPACE));
        if (!StringsUtil.isNullOrEmpty(m_type))
            xml.append(String.format(" type=%s", m_type));

        xml.append(">");

        if (!StringsUtil.isNullOrEmpty(m_callerJid))
            xml.append(String.format("<caller>%s</caller>", m_callerJid));
        if (!StringsUtil.isNullOrEmpty(m_calleeJid))
            xml.append(String.format("<callee>%s</callee>", m_calleeJid));
        if (!StringsUtil.isNullOrEmpty(m_state))
            xml.append(String.format("<state>%s</state>", m_state));
        if (!StringsUtil.isNullOrEmpty(m_duration))
            xml.append(String.format("<duration>%d</duration>", m_duration));
        if (!StringsUtil.isNullOrEmpty(m_media))
            xml.append(String.format("<media>%s</media>", m_media));
        if (!StringsUtil.isNullOrEmpty(m_callId))
            xml.append(String.format("<call_id>%s</call_id>", m_callId));
        if (m_isAck != null)
            xml.append(String.format("<ack read=%b />", m_isAck));

        xml.append(String.format("</%s>", ELEMENT));
        return xml.toString();
    }

    public void setType(String type)
    {
        m_type = type;
    }

    private void setCaller(String caller)
    {
        m_callerJid = caller;
    }

    private void setCallee(String callee)
    {
        m_calleeJid = callee;
    }

    public void setMedia(String media)
    {
        m_media = media;
    }

    private void setCallId(String callId)
    {
        m_callId = callId;
    }

    private void setIsAck(Boolean isAck)
    {
        m_isAck = isAck;
    }

    public static class Provider extends ExtensionElementProvider<CallLogPacketExtension>
    {

        @Override
        public CallLogPacketExtension parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException
        {
            CallLogPacketExtension callLogPacketExtension = new CallLogPacketExtension();

            String name = parser.getName();
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                if (!StringsUtil.isNullOrEmpty(name))
                {
                    switch (name)
                    {
                        case ELEMENT:
                            callLogPacketExtension.setType(parser.getAttributeValue(null, "type"));
                            break;
                        case "caller":
                            callLogPacketExtension.setCaller(parser.nextText());
                            break;
                        case "callee":
                            callLogPacketExtension.setCallee(parser.nextText());
                            break;
                        case "state":
                            callLogPacketExtension.setState(parser.nextText());
                            break;
                        case "duration":
                            callLogPacketExtension.setDuration(DateTimeUtil.getDurationFromMs(parser.nextText()));
                            break;
                        case "media":
                            callLogPacketExtension.setMedia(parser.nextText());
                            break;
                        case "call_id":
                            callLogPacketExtension.setCallId(parser.nextText());
                            break;
                        case "ack":
                            callLogPacketExtension.setIsAck(Boolean.valueOf(parser.getAttributeValue(null, "read")));
                            break;
                        default:
                            break;
                    }
                }

                parser.next();
                name = parser.getName();
                eventType = parser.getEventType();
                if (!StringsUtil.isNullOrEmpty(name) && (name.equals(ELEMENT)) && (eventType == XmlPullParser.END_TAG))
                {
                    break;
                }
            }

            return callLogPacketExtension;
        }
    }

}

package com.ale.infra.xmpp.xep.calllog;

import com.ale.util.DateTimeUtil;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.DefaultExtensionElement;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Date;

/**
 * Created by grobert on 29/04/16.
 */
public class UpdatedCallLogMessagePacketExtension extends DefaultExtensionElement implements ICallLogPacketExtension
{

    public static final String ELEMENT = "updated_call_log";
    public static final String NAMESPACE = "jabber:iq:notification:telephony:call_log";
    private static final String LOG_TAG = "UpdatedCallLogMessagePacketExtension";
    private String id;
    private Long stampLong;

    private CallLogPacketExtension callLogEvent;

    public UpdatedCallLogMessagePacketExtension(XmlPullParser parser)
    {
        super(ELEMENT, NAMESPACE);

        CallLogPacketExtension.Provider callLogProvider = new CallLogPacketExtension.Provider();
        // example of CallLOg message to parse :

        //<updated_call_log xmlns='jabber:iq:notification:telephony:call_log' id='1494943960081007'>
        //  <forwarded xmlns='urn:xmpp:forward:0'>
        //      <delay xmlns='urn:xmpp:delay' stamp='2017-05-16T14:12:40.081007Z'/>
        //      <call_log xmlns='jabber:iq:notification:telephony:call_log' type='webrtc'>
        //          <caller>cf9270dc883c48908ca62871463284cb@demo-all-in-one-dev-1.opentouch.cloud</caller>
        //          <callee>7e152a360ca648c69d339902d971ae87@demo-all-in-one-dev-1.opentouch.cloud</callee>
        //          <state>answered</state>
        //          <media>audio</media>
        //          <duration>10560</duration>
        //          <call_id>d4bltnan0es4j</call_id>
        //          <ack read='false'/>
        //      </call_log>
        //  </forwarded>
        // </result>


        try
        {
            // set the id from the result attribute:
            setId(parser.getAttributeValue(null, "id"));

            String name = parser.getName();
            int eventType = parser.getEventType();

            // parse xml structure until end of tag called "result"
            // do not parse the whole xml structure since this parser is used afterwards
            // warning : we have to keep the same initial depth when we have finished to parse.
            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                if (!StringsUtil.isNullOrEmpty(name) && (eventType != XmlPullParser.END_TAG))
                {
                    switch (name)
                    {
                        case "delay":
                            String stamp = parser.getAttributeValue(null, "stamp");
                            if (!StringsUtil.isNullOrEmpty(stamp))
                            {
                                setStampLong(DateTimeUtil.getStampFromStringDate(stamp));
                            }
                            break;
                        case CallLogPacketExtension.ELEMENT:
                        {
                            String nameSpace = parser.getNamespace();

                            if (!StringsUtil.isNullOrEmpty(nameSpace) && nameSpace.equals(CallLogPacketExtension.NAMESPACE))
                            {
                                try
                                {
                                    callLogEvent = callLogProvider.parse(parser);
                                }
                                catch (SmackException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        }
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
        }
        catch (XmlPullParserException e)
        {
            Log.getLogger().error(LOG_TAG, "parseMamStanza; XmlPullParserException " + e.getMessage());
        }
        catch (IOException e)
        {
            Log.getLogger().error(LOG_TAG, "parseMamStanza; IOException " + e.getMessage());
        }
    }

    @Override
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public Long getStampLong()
    {
        return this.stampLong;
    }

    public void setStampLong(Long stamp)
    {
        this.stampLong = stamp;
    }

    @Override
    public String toString()
    {
        StringBuilder toXml = new StringBuilder();

        toXml.append("UpdatedCallLogMessagePacketExtension{");
        if (!StringsUtil.isNullOrEmpty(id))
            toXml.append(", id='" + id + '\'');
        if (stampLong != null)
            toXml.append(", stamp='" + new Date(stampLong).toString() + '\'');
        if (callLogEvent != null)
        {
            toXml.append(", callLog caller='" + callLogEvent.getCallerJid() + '\'');
            toXml.append(", callLog callee='" + callLogEvent.getCalleeJid() + '\'');
            toXml.append(", callLog state='" + callLogEvent.getState() + '\'');
            toXml.append(", callLog duration='" + callLogEvent.getDuration() + '\'');
        }


        toXml.append('}');

        return toXml.toString();
    }

    @Override
    public String toXML()
    {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.attribute("id", id);
        xml.closeEmptyElement();
        return xml.toString();
    }

    @Override
    public CallLogPacketExtension getCallLogEvent()
    {
        return callLogEvent;
    }

}
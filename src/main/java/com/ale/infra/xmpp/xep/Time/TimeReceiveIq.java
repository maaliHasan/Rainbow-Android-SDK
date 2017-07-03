package com.ale.infra.xmpp.xep.Time;

import com.ale.infra.xmpp.xep.call.CallIq;
import com.ale.util.DateTimeUtil;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by wilsius on 06/06/2017.
 */


public class TimeReceiveIq extends IQ implements ExtensionElement {
    public static final String ELEMENT = "time";
    private static final String LOG_TAG = "TimeReceiveIq";
    private String utc;


    public TimeReceiveIq(XmlPullParser parser, String begin, String end) {
        super(begin, end);

        try {
            // parse next tag:
            parser.next();
            String name = parser.getName();
            int eventType = parser.getEventType();

            // parse xml structure until end of tag called "fin"
            // do not parse the whole xml structure since this parser is used afterwards
            // warning : we have to keep the same initial depth when we have finished to parse.
            while (!((!StringsUtil.isNullOrEmpty(name)) && (name.equalsIgnoreCase(ELEMENT)) && (eventType == XmlPullParser.END_TAG)))
            {
                if (!StringsUtil.isNullOrEmpty(name) && (eventType != XmlPullParser.END_TAG)) {
                    switch (name) {
                        case "utc":
                            setUtc(parser.nextText());
                            break;
                        default:
                            break;
                    }
                }
                parser.next();
                name = parser.getName();
                eventType = parser.getEventType();
            }
        } catch (XmlPullParserException e) {
            Log.getLogger().error(LOG_TAG, "TimeReceiveIq : XmlPullParserException " + e.getMessage());
        } catch (IOException e) {
            Log.getLogger().error(LOG_TAG, "TimeReceiveIq : IOException " + e.getMessage());
        }
    }


    private void setUtc(String utc) {
        this.utc = utc;
    }
    public String getUtc() {
        return utc;
    }
    public long getTimestamFromServer(){
        if (!StringsUtil.isNullOrEmpty(utc)) {
            return getStamp(utc);
        }
        else
            return 0L;
    }

    private long getStamp (String stamp ) {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";

        String stampFiltered = stamp;

        if( stamp.length()> pattern.length()) {
            stampFiltered = stampFiltered.substring(0, pattern.length());
        }
        TimeZone tz = TimeZone.getTimeZone("GMT");

        DateFormat df = new SimpleDateFormat(pattern);
        df.setTimeZone(tz);
        try {
            return df.parse(stampFiltered).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date().getTime();
        }
    }

    @Override
    protected IQ.IQChildElementXmlStringBuilder getIQChildElementBuilder(IQ.IQChildElementXmlStringBuilder xml) {
        return null;
    }

    @Override
    public String getNamespace() {
        return super.getChildElementNamespace();
    }

    @Override
    public String getElementName() {
        return super.getChildElementName();
    }
}

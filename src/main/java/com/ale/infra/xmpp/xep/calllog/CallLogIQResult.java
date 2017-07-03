package com.ale.infra.xmpp.xep.calllog;

import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.IQ;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/*
    <iq xmlns='jabber:client' from='from' to='to' id='XuFBg-27' type='result'>
        <query xmlns='jabber:iq:telephony:call_log'>
            <set xmlns='http://jabber.org/protocol/rsm'>
                <first>1493222116097614</first>
                <last>1494943960081007</last>
                <count>488</count>
             </set>
         </query>
     </iq>
 */


/**
 * Created by georges on 14/03/16.
 */
public class CallLogIQResult extends IQ implements ExtensionElement
{

    public static final String ELEMENT = "query";
    private static final String LOG_TAG = "CallLogIQResult";
    private String first;
    private String last;
    private String count;

    public CallLogIQResult(XmlPullParser parser, String begin, String end)
    {
        super(begin, end);

        try
        {

            // parse next tag:
            parser.next();
            String name = parser.getName();
            int eventType = parser.getEventType();

            // parse xml structure until end of tag called "fin"
            // do not parse the whole xml structure since this parser is used afterwards
            // warning : we have to keep the same initial depth when we have finished to parse.
            while (!((!StringsUtil.isNullOrEmpty(name)) && (name.equalsIgnoreCase(ELEMENT)) && (eventType == XmlPullParser.END_TAG)))
            {
                if (!StringsUtil.isNullOrEmpty(name) && (eventType != XmlPullParser.END_TAG))
                {
                    switch (name)
                    {
                        case "first":
                            setFirst(parser.nextText());
                            break;
                        case "last":
                            setLast(parser.nextText());
                            break;
                        case "count":
                            setCount(parser.nextText());
                            break;
                        default:
                            break;
                    }
                }
                parser.next();
                name = parser.getName();
                eventType = parser.getEventType();
            }
        }
        catch (XmlPullParserException e)
        {
            Log.getLogger().error(LOG_TAG, "CallLogIQResult : XmlPullParserException " + e.getMessage());
        }
        catch (IOException e)
        {
            Log.getLogger().error(LOG_TAG, "CallLogIQResult : IOException " + e.getMessage());
        }
    }

    public String getFirst()
    {
        return first;
    }

    public void setFirst(String first)
    {
        this.first = first;
    }

    public int getCount()
    {
        if (StringsUtil.isNullOrEmpty(count))
            return -1;

        return Integer.parseInt(count);
    }

    public void setCount(String count)
    {
        this.count = count;
    }

    public String getLast()
    {
        return last;
    }

    public void setLast(String last)
    {
        this.last = last;
    }

    @Override
    public String getFrom()
    {
        // remove resource from jabberid

        String id = super.getFrom();
        int index = super.getFrom().indexOf("/");
        if (index != -1)
        {
            id = super.getFrom().substring(0, index);
        }
        return id;
    }

    @Override
    public String getTo()
    {
        // remove resource from jabberid
        String id = super.getTo();
        int index = super.getTo().indexOf("/");
        if (index != -1)
        {
            id = super.getTo().substring(0, index);
        }
        return id;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml)
    {
        return null;
    }

    @Override
    public String getNamespace()
    {
        return super.getChildElementNamespace();
    }

    @Override
    public String getElementName()
    {
        return super.getChildElementName();
    }
}
package com.ale.infra.xmpp.xep.calllog;

import com.ale.util.StringsUtil;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.IQ;
import org.xmlpull.v1.XmlPullParser;

/*
<iq xmlns="jabber:client" from="5f137da83c64469d92024eea18b35b92@demo-all-in-one-dev-1.opentouch.cloud" to="5f137da83c64469d92024eea18b35b92@demo-all-in-one-dev-1.opentouch.cloud/web_win_1.25.11_1tl080Zs" id="1dfe02a5-1a35-47ac-888e-b6f49d6d58ae:sendIQ" type="result">
   <deleted xmlns="jabber:iq:telephony:call_log" count="11" />
</iq>
 */


/**
 * Created by georges on 14/03/16.
 */
public class DeleteCallLogIQResult extends IQ implements ExtensionElement
{

    public static final String ELEMENT = "deleted";
    private static final String LOG_TAG = "DeleteCallLogIQResult";
    private String count;

    public DeleteCallLogIQResult(XmlPullParser parser, String begin, String end)
    {
        super(begin, end);

        setCount(parser.getAttributeValue(null, "count"));
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
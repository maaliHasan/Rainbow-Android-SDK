package com.ale.infra.xmpp.xep;

import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.IQ;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/*
<iq xmlns='jabber:client'
    from='4e68a0facdb84aba9b2cdea2a7e1f6a6@openrainbow.com'
    to='4e68a0facdb84aba9b2cdea2a7e1f6a6@openrainbow.com/Smack'
    id='eb3f4013586d41d5871280ba98b59f13@openrainbow.com'
    type='result'>
    <fin xmlns='L56' queryid='f27' complete='true'>
        <set xmlns='http://jabber.org/protocol/rsm'>
            <first>1456328989240937</first>
            <last>1456416490746793</last>
            <count>24</count>
        </set>
    </fin>
</iq>
 */


/**
 * Created by georges on 14/03/16.
 */
public class MamIQResult extends IQ implements ExtensionElement {

    final private String LOG_TAG = "MamIQResult";

    private String queryid;
    private String deleteid;
    private String first;
    private String last;
    private String count;
    private boolean complete;



    public MamIQResult(XmlPullParser parser, String begin, String end) {
       super(begin,end);

        try {

            for (int i=0; i> parser.getAttributeCount();i++)  {
                String attributeName = parser.getAttributeName(i);
                switch (attributeName) {
                    case "queryid":
                        // set the queryid from the result attribute:
                        setQueryid(parser.getAttributeValue(i));
                        break;
                    case "deleteid":
                        //Set the deleid if exists
                        setDeleteid(parser.getAttributeValue(i));
                        break;
                    case "complete":
                        switch (parser.getAttributeValue(i)) {
                            case "true":
                                setComplete(true);
                                break;
                            default:
                                setComplete(false);
                                break;
                        }
                    default:
                        break;
                }

            }
//            // set the queryid from the result attribute:
//            setQueryid(parser.getAttributeValue(null, "queryid"));
//
//            //Set the deleid if exists
//            setDeleteid(parser.getAttributeValue(null, "deleteid"));
//
//
//
//            if (deleteid == null) {
//                // complete attribute:
//                switch (parser.getAttributeValue(null, "complete")){
//                    case "true":
//                       setComplete(true);
//                       break;
//                 default:
//                        setComplete(false);
//                        break;
//                }
//            }

            // parse next tag:
            parser.next();
            String name = parser.getName();
            int eventType = parser.getEventType();

            // parse xml structure until end of tag called "fin"
            // do not parse the whole xml structure since this parser is used afterwards
            // warning : we have to keep the same initial depth when we have finished to parse.
            while (!((!StringsUtil.isNullOrEmpty(name))&&(name.equalsIgnoreCase("fin")) && (eventType==3))) {
                if (!StringsUtil.isNullOrEmpty(name) &&
                        (parser.getEventType() != XmlPullParser.END_TAG)) {
                    switch (name) {
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
        }catch (XmlPullParserException e) {
            Log.getLogger().error(LOG_TAG, "MamIQResult : XmlPullParserException " + e.getMessage());
        }catch (IOException e) {
            Log.getLogger().error(LOG_TAG, "MamIQResult : IOException " + e.getMessage());
        }
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public int getCount() {
        if (StringsUtil.isNullOrEmpty(count))
            return -1;

        Integer countValue = Integer.parseInt(count);
        return countValue;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public String getQueryid() {
        return queryid;
    }

    public void setQueryid(String queryid) {
        this.queryid = queryid;
    }

    public void setDeleteid(String id) {
        this.deleteid = id;
    }

    @Override
    public String getFrom() {
        // remove resource from jabberid

        String id = super.getFrom();
        int index = super.getFrom().indexOf("/");
        if (index != -1) {
            id = super.getFrom().substring(0, index);
        }
        return id;
    }

    @Override
    public String getTo() {
        // remove resource from jabberid
        String id = super.getTo();
        int index = super.getTo().indexOf("/");
        if (index != -1) {
            id = super.getTo().substring(0, index);
        }
        return id;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        //TODO : usefull ?
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
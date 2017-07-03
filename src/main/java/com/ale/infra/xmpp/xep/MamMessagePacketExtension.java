package com.ale.infra.xmpp.xep;

import com.ale.infra.xmpp.xep.Room.RoomMultiUserChatEvent;
import com.ale.infra.xmpp.xep.calllog.CallLogPacketExtension;
import com.ale.infra.xmpp.xep.outofband.RainbowOutOfBandData;
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
public class MamMessagePacketExtension extends DefaultExtensionElement {

    private static final String LOG_TAG = "MamMessagePacketExtension";

    public static final String ELEMENT = "result";
    public static final String NAMESPACE = "urn:xmpp:mam:1";

    private String queryid;
    private String mamId;
    private String from;
    private String to;
    private String type;
    private String body;
    private boolean requested = false;
    private String messageId;
    private boolean read;
    private boolean received;

    private Long stampLong;
    private Long longReadDate;
    private Long longReceivedDate;

    private RoomMultiUserChatEvent roomEvent;
    private RainbowOutOfBandData oobEvent;
    private CallLogPacketExtension callLogEvent;

    public MamMessagePacketExtension(XmlPullParser parser) {
        super(ELEMENT, NAMESPACE);

        RoomMultiUserChatEvent.Provider roomEventProvider = new RoomMultiUserChatEvent.Provider();
        RainbowOutOfBandData.Provider oobProvider = new RainbowOutOfBandData.Provider();
        CallLogPacketExtension.Provider callLogProvider = new CallLogPacketExtension.Provider();
        // example of MAM message to parse :

//            <result xmlns='urn:xmpp:mam:1' mamId='1460561749345184' queryid='f970648f49084909b074f43bf92e577a_1' >
//                <forwarded xmlns = 'urn:xmpp:forward:0'>
//                    <message xmlns='jabber:client' from='f970648f49084909b074f43bf92e577a@openrainbow.net/Smack'
//                          to='aa70b93e45fa432ea3da1ad572231d08@openrainbow.net' mamId='3W6sY-247' type='chat'>
//                        <body>fffffffff</body>
//                        <thread>970575a4-0505-44cd-ba83-aa4c040c5a30</thread>
//                        <active xmlns='http://jabber.org/protocol/chatstates'/>
//                    </message>
//                    <delay xmlns='urn:xmpp:delay' from='openrainbow.net' stamp='2016-04-13T15:35:49.345Z'/>
//                </forwarded>
//            </result>

//        <message
//        from='kingrichard@royalty.england.lit/throne'
//        mamId='bi29sg183b4v'
//        to='northumberland@shakespeare.lit/westminster'>
//        <received xmlns='urn:xmpp:receipts' mamId='richard2-4.1.247'/>
//        </message>

        try {
            // set the queryid from the result attribute:
            setQueryid(parser.getAttributeValue(null, "queryid"));

            String name = parser.getName();
            int eventType = parser.getEventType();

            // parse xml structure until end of tag called "result"
            // do not parse the whole xml structure since this parser is used afterwards
            // warning : we have to keep the same initial depth when we have finished to parse.
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (!StringsUtil.isNullOrEmpty(name) &&
                        (eventType != XmlPullParser.END_TAG)) {
                    switch (name) {
                        case ELEMENT:
                            setMamId(parser.getAttributeValue(null, "id"));
                            break;
                        case "message":
                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                switch (parser.getAttributeName(i)) {
                                    case "to":
                                        setTo(parser.getAttributeValue(null, "to"));
                                        break;
                                    case "from":
                                        setFrom(parser.getAttributeValue(null, "from"));
                                        break;
                                    case "type":
                                        setType(parser.getAttributeValue(null, "type"));
                                        break;
                                    case "id":
                                        setMessageId(parser.getAttributeValue(null, "id"));
                                        break;
                                    default:
                                        break;
                                }
                            }

                            break;
                        case "body":
                            setBody(parser.nextText());
                            break;
                        case "delay":
                            String stamp = parser.getAttributeValue(null, "stamp");
                            if (!StringsUtil.isNullOrEmpty(stamp)) {
                                setStampLong(DateTimeUtil.getStampFromStringDate(stamp));
                            }
                            break;
                        case "request":
                            setRequested(true);
                            break;
                        //<ack received='false' read='false'/>
                        case "ack":
                            setRead(parser.getAttributeValue(null, "read"));
                            String readStamp = parser.getAttributeValue(null, "read_timestamp");
                            if (!StringsUtil.isNullOrEmpty(readStamp)) {
                                setLongReadDate(DateTimeUtil.getStampFromStringDate(readStamp));
                            }
                            setReceived(parser.getAttributeValue(null, "received"));
                            String receivedStamp = parser.getAttributeValue(null, "recv_timestamp");
                            if (!StringsUtil.isNullOrEmpty(receivedStamp)) {
                                setLongReceivedDate(DateTimeUtil.getStampFromStringDate(receivedStamp));
                            }
                            break;
                        case "event":
                            if (roomEventProvider != null) {
                                try {
                                    roomEvent = roomEventProvider.parse(parser);
                                } catch (SmackException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        case "x": {
                            String nameSpace = parser.getNamespace();

                            if ( !StringsUtil.isNullOrEmpty(nameSpace) &&
                                    nameSpace.equals(RainbowOutOfBandData.NAMESPACE))
                                if (oobProvider != null) {
                                    try {
                                        oobEvent = oobProvider.parse(parser);
                                    } catch (SmackException e) {
                                        e.printStackTrace();
                                    }
                                }
                            break;
                        }
                        case CallLogPacketExtension.ELEMENT: {
                            String nameSpace = parser.getNamespace();

                            if ( !StringsUtil.isNullOrEmpty(nameSpace) &&
                                    nameSpace.equals(CallLogPacketExtension.NAMESPACE))
                                if (callLogProvider != null) {
                                    try {
                                        callLogEvent = callLogProvider.parse(parser);
                                    } catch (SmackException e) {
                                        e.printStackTrace();
                                    }
                                }
                            break;
                        }
                        // TODO : thread , active , forwarded tags ? usefull ?
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

            if (callLogEvent != null) {
                from = callLogEvent.getCallerJid();
                to = callLogEvent.getCalleeJid();
            }

        } catch (XmlPullParserException e) {
            Log.getLogger().error(LOG_TAG, "parseMamStanza; XmlPullParserException " + e.getMessage());
        } catch (IOException e) {
            Log.getLogger().error(LOG_TAG, "parseMamStanza; IOException " + e.getMessage());
        }
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        // remove resource from jabberid
//        String id = from;
//        int index = from.indexOf("/");
//        if (index != -1) {
//            id = from.substring(0, index);
//        }
        this.from = from;
    }

    public String getMamId() {
        return mamId;
    }

    public void setMamId(String id) {
        this.mamId = id;
    }

    public String getQueryid() {
        return queryid;
    }

    public void setQueryid(String queryid) {
        this.queryid = queryid;
    }

    public void setStampLong(Long stamp) {
        this.stampLong = stamp;
    }

    public Long getStampLong() {
        return this.stampLong ;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        // remove resource from jabberid
        String id = to;
        int index = to.indexOf("/");
        if (index != -1) {
            id = to.substring(0, index);
        }
        this.to = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder toXml = new StringBuilder();

        toXml.append("MamMessagePacketExtension{");
        if( !StringsUtil.isNullOrEmpty(body))
            toXml.append("body='" + body + '\'');
        if( !StringsUtil.isNullOrEmpty(queryid))
            toXml.append(", queryid='" + queryid + '\'');
        if( !StringsUtil.isNullOrEmpty(mamId))
            toXml.append(", mamId='" + mamId + '\'');
        if( !StringsUtil.isNullOrEmpty(from))
            toXml.append(", from='" + from + '\'');
        if( !StringsUtil.isNullOrEmpty(to))
            toXml.append(",; to='" + to + '\'');
        if( !StringsUtil.isNullOrEmpty(type))
            toXml.append(", type='" + type + '\'');
        if( stampLong != null)
            toXml.append(", stamp='" + new Date(stampLong).toString() + '\'');
        if( longReadDate != null)
            toXml.append(",  readDate='" + new Date(longReadDate).toString() + '\'');
        if( longReceivedDate != null)
            toXml.append(",  receivedDate='" + new Date(longReceivedDate).toString() + '\'');
        if( roomEvent != null ) {
            toXml.append(", roomEvent name='" + roomEvent.getElementName() + '\'');
            toXml.append(", roomEvent type='" + roomEvent.getEventType() + '\'');
            toXml.append(", roomEvent Jid='" + roomEvent.getJid() + '\'');
        }
        if( oobEvent != null ) {
            toXml.append(", oob name='" + oobEvent.getElementName() + '\'');
            toXml.append(", oob url='" + oobEvent.getUrl() + '\'');
            toXml.append(", oob Mime='" + oobEvent.getMime() + '\'');
            toXml.append(", oob Size='" + oobEvent.getSize() + '\'');
        }
        if( callLogEvent != null ) {
            toXml.append(", callLog caller='" + callLogEvent.getCallerJid() + '\'');
            toXml.append(", callLog callee='" + callLogEvent.getCalleeJid() + '\'');
            toXml.append(", callLog state='" + callLogEvent.getState() + '\'');
            toXml.append(", callLog duration='" + callLogEvent.getDuration() + '\'');
        }


        toXml.append('}');

        return toXml.toString();
    }

    @Override
    public String toXML() {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.attribute("id", mamId);
        xml.attribute("from", from);
        xml.attribute("to", to);
        xml.closeEmptyElement();
        return xml.toString();
    }


    public void setRequested(boolean requested) {
        this.requested = requested;
    }

    public boolean isRequested() {
        return requested;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setRead(String readStrg) {
        if( StringsUtil.isNullOrEmpty(readStrg)) {
            return;
        }
        this.read = Boolean.valueOf(readStrg);
    }

    public Date getReadDate() {
        if( longReadDate == null)
            return null;

        return new Date(longReadDate);
    }

    public boolean isRead() {
        return this.read;
    }

    public void setReceived(String receivedStrg) {
        if( StringsUtil.isNullOrEmpty(receivedStrg)) {
            return;
        }
        this.received = Boolean.valueOf(receivedStrg);
    }

    public Date getReceivedDate() {
        if( longReceivedDate == null)
            return null;

        return new Date(longReceivedDate);
    }

    public boolean isReceived() {
        return this.received;
    }

    public void setLongReadDate(Long longReadDate) {
        this.longReadDate = longReadDate;
    }

    public Long getLongReadDate() {
        return longReadDate;
    }

    private void setLongReceivedDate(Long longReceivedDate) {
        this.longReceivedDate = longReceivedDate;
    }

    public Long getLongReceivedDate() {
        return longReceivedDate;
    }

    public RoomMultiUserChatEvent getRoomEvent() {
        return roomEvent;
    }

    public RainbowOutOfBandData getOobEvent() {
        return oobEvent;
    }

    public CallLogPacketExtension getCallLogEvent() {
        return callLogEvent;
    }

}
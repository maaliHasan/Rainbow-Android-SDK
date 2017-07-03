package com.ale.infra.xmpp.xep;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PlainStreamElement;

/**
 * Created by georges on 15/01/16.
 */
public class MamRequestIQ extends IQ implements PlainStreamElement {

    public static String ELEMENT = "query";
    public static String NAMESPACE = "urn:xmpp:mam:1";

    private String with;
    private int lastMsgCtr;
    private String lastMessageId = null;

    public MamRequestIQ(String with, String queryId) {
        super(ELEMENT,NAMESPACE);
        this.with = with;
        setStanzaId(queryId);
        this.lastMsgCtr = -1;
    }

    public MamRequestIQ(String with, String queryId,int lastMsgCtr) {
        super(ELEMENT,NAMESPACE);
        this.with = with;
        setStanzaId(queryId);
        this.lastMsgCtr = lastMsgCtr;
    }

    public MamRequestIQ(String with, String queryId, int lastMsgCtr, String lastMessageId) {
        super(ELEMENT,NAMESPACE);
        this.with = with;
        setStanzaId(queryId);
        this.lastMsgCtr = lastMsgCtr;
        this.lastMessageId = lastMessageId;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.halfOpenElement("x");
        xml.optAttribute("xmlns", "jabber:x:data");
        xml.optAttribute("type", "submit");
        xml.rightAngleBracket();

        xml.halfOpenElement("field");
        xml.optAttribute("var", "FORM_TYPE");
        xml.optAttribute("type", "hidden");
        xml.rightAngleBracket();
        xml.optElement("value", "urn:xmpp:mam:1");
        xml.closeElement("field");

        if (with != null) {
            xml.halfOpenElement("field");
            xml.optAttribute("var", "with");
            xml.rightAngleBracket();
            xml.optElement("value", with);
            xml.closeElement("field");
        }

        xml.closeElement("x");

        if (lastMsgCtr > 0 ) {
            xml.halfOpenElement("set");
            xml.optAttribute("xmlns", "http://jabber.org/protocol/rsm");
            xml.rightAngleBracket();
            xml.optElement("max", String.valueOf(lastMsgCtr));
            if (lastMessageId == null) {
                xml.optElement("before", "");
            } else {
                xml.optElement("before", lastMessageId);
            }
            xml.closeElement("set");
        }
        return xml;
    }
}

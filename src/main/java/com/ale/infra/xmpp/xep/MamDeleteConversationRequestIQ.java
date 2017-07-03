package com.ale.infra.xmpp.xep;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PlainStreamElement;



import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PlainStreamElement;
/**
 * Created by wilsius on 18/05/16.
 */
public class MamDeleteConversationRequestIQ extends IQ implements PlainStreamElement {


        public static String ELEMENT = "delete";
        public static String NAMESPACE = "urn:xmpp:mam:1";

        private String from;
        private String deleteId;
        private String before;

        public MamDeleteConversationRequestIQ(String from, String before) {
            super(ELEMENT,NAMESPACE);

            this.from = from;

            this.before = before;

        }
//<iq type='set' id='j_8170989110@openrainbow.net' xmlns='jabber:client'>
//<delete xmlns='urn:xmpp:mam:0' deleteid='remove_j_8170989110@openrainbow.net'>
//<x xmlns='jabber:x:data' type='submit'><field var='FORM_TYPE' type='hidden'>
//<value>urn:xmpp:mam:0</value></field><field var='with'><value>j_8170989110@openrainbow.net</value></field></x>
//<set xmlns='http://jabber.org/protocol/rsm'><before>2016-05-18T07:16:10Z</before></set></delete></iq>


        @Override
        protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
            String deleteId = "delete_" + from;

            xml.attribute("deleteid", deleteId);
            xml.rightAngleBracket();

            xml.halfOpenElement("x");
            xml.optAttribute("xmlns", "jabber:x:data");
            xml.optAttribute("type", "submit");
            xml.rightAngleBracket();

            xml.halfOpenElement("field");
            xml.optAttribute("var", "FORM_TYPE");
            xml.optAttribute("type", "hidden");
            xml.rightAngleBracket();
            xml.optElement("value", "urn:xmpp:mam:0");
            xml.closeElement("field");

            xml.halfOpenElement("field");
            xml.optAttribute("var", "with");
            xml.rightAngleBracket();
            xml.optElement("value", from );
            xml.closeElement("field");

            xml.closeElement("x");

                xml.halfOpenElement("set");
                xml.optAttribute("xmlns", "http://jabber.org/protocol/rsm");
                xml.rightAngleBracket();
                if (before == null) {
                    xml.optElement("before", "");
                } else {
                    xml.optElement("before", before);
                }
                xml.closeElement("set");
            return xml;
        }
    }

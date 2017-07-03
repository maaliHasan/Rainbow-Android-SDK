package com.ale.infra.xmpp.xep.Command;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PlainStreamElement;
import org.jivesoftware.smackx.xdata.FormField;
import org.jivesoftware.smackx.xdata.packet.DataForm;

/**
 * Created by georges on 19/10/2016.
 */

public class RainbowEnablePush extends IQ implements PlainStreamElement {

    public static String ELEMENT = "enable";
    public static String NAMESPACE = "urn:xmpp:push:0";


    public static final String FIELD_FORM_TYPE = "FORM_TYPE";
    public static final String FIELD_FORM_TYPE_VALUE = "http://jabber.org/protocol/pubsub#publish-options";
    public static final String FIELD_SECRET = "secret";


    private String node = null;
    private String jid = null;
    private RainbowDataForm dataForm = null;



// <iq type='set' id='127' xmlns='jabber:client'>
// <enable xmlns='urn:xmpp:push:0' node='6995817385395586061' jid='pubsub.app.pingme.sqanet.fr'>
// <x xmlns='jabber:x:data'>
//     <field var='FORM_TYPE'><value>http://jabber.org/protocol/pubsub#publish-options</value></field>
//     <field var='secret'><value>13743318809274027245</value></field>
// </x>
// </enable>
// </iq>


    public RainbowEnablePush(String to, String node, String jid) {
        super(ELEMENT,NAMESPACE);
        this.setType(Type.set);
        //this.setTo(to);

        this.node = node;
        this.jid = jid;
        dataForm = new RainbowDataForm(null);

        addField(FIELD_FORM_TYPE, FIELD_FORM_TYPE_VALUE);
    }

    public RainbowEnablePush(String to) {
        this(to,"","","");
    }

    public RainbowEnablePush(String to, String node, String jid, String secret) {
        this(to, node, jid);

        addField(FIELD_SECRET, secret);
    }

    public void addField(String fieldName, String value) {
        FormField field = new FormField(fieldName);
        field.addValue(value);
        dataForm.addField(field);
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.attribute("node", this.node);
        xml.attribute("jid", this.jid);

        xml.rightAngleBracket();

        if( this.dataForm.getFields().size() > 0) {
            xml.append(this.dataForm.toXML());
        }

        return xml;
    }
}

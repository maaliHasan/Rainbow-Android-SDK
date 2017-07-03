package com.ale.infra.xmpp.xep.Command;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PlainStreamElement;
import org.jivesoftware.smackx.xdata.FormField;
import org.jivesoftware.smackx.xdata.packet.DataForm;

/**
 * Created by georges on 19/10/2016.
 */

public class RainbowCommandPush extends IQ implements PlainStreamElement {

    public static String ELEMENT = "command";
    public static String NAMESPACE = "http://jabber.org/protocol/commands";

    public static String ACTION_EXECUTE = "execute";

    public static String NODE_REGISTER_PUSH = "register-push-gcm";
    public static String NODE_UNREGISTER_PUSH = "unregister-push";

    private static final String FIELD_TOKEN = "token";
    private static final String FIELD_DEVICENAME = "device-name";
    private static final String FIELD_DEVICEID = "device-id";


    private String node = null;
    private String action = null;
    private RainbowDataForm dataForm = null;


//    <iq type='set' id='127' to='app.pingme.sqanet.fr' xmlns='jabber:client'>
//    <command xmlns='http://jabber.org/protocol/commands' node='register-push-gcm' action='execute'>
//    <x xmlns='jabber:x:data' type='submit'>
//    <field var='token'><value>dYLKW_gV22U:APA91bHGwZ8c3w33mtL0ZPUrzNgx1Jii4DaVfSiPZxUeQdje5p80qfJ3-QD6MGGSiresDQFXdZY9Mnf2BojeSy5_QN0-3MgrK8tnvVsWhDhUjoH4kZXO0SSKh8QB51pIPJXM4Zrs_ngj</value></field>
//    <field var='device-name'><value>Nexus5x</value></field>
//    <field var='device-id'><value>100500</value></field>
//    </x>
//    </command>
//    </iq>


    public RainbowCommandPush(String to, String node, String action) {
        super(ELEMENT,NAMESPACE);
        this.setType(Type.set);
        this.setTo(to);

        this.node = node;
        this.action = action;
        dataForm = new RainbowDataForm(DataForm.Type.submit);
    }

    public RainbowCommandPush(String to) {
        this(to,NODE_REGISTER_PUSH, ACTION_EXECUTE);
    }

    public RainbowCommandPush(String to, String token, String deviceName, String deviceId, boolean activation) {
        this(to, NODE_REGISTER_PUSH, ACTION_EXECUTE);

        if (!activation) {
            node = NODE_UNREGISTER_PUSH;
        }
        addField(FIELD_TOKEN, token);
        addField(FIELD_DEVICENAME, deviceName);
        addField(FIELD_DEVICEID, deviceId);
    }

    public void addField(String fieldName, String value) {
        FormField field = new FormField(fieldName);
        field.addValue(value);
        dataForm.addField(field);
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.attribute("node", this.node);
        xml.attribute("action", this.action);

        xml.rightAngleBracket();

        if( this.dataForm.getFields().size() > 0) {
            xml.append(this.dataForm.toXML());
        }

        return xml;
    }
}

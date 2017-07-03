package com.ale.infra.xmpp.xep.message;

import org.jivesoftware.smack.packet.ExtensionElement;


/**
 * Created by georges on 06/03/17.
 */
public class StoreMessagePacketExtension implements ExtensionElement {

    public static final String ELEMENT = "store";
    public static final String NAMESPACE = "urn:xmpp:hints";

    public static final String NAME_STORE = "store";

    public static final String TYPE_EMPTY = "empty";

//   <store xmlns="urn:xmpp:hints"/>

    public StoreMessagePacketExtension() {
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }


    @Override
    public String toXML() {
        StringBuilder xml = new StringBuilder();
        xml.append(String.format("<%s ", ELEMENT));
        xml.append(String.format("xmlns=\"%s\"/>", NAMESPACE));
        return xml.toString();
    }
}

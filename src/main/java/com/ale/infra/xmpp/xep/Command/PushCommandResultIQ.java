package com.ale.infra.xmpp.xep.Command;

import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.IQ;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by georges on 19/10/2016.
 */

public class PushCommandResultIQ extends IQ implements ExtensionElement {

    private static final String LOG_TAG = "PushCommandResultIQ";


    public static final String ELEMENT = "command";

    public static final String NAMESPACE = "http://jabber.org/protocol/commands";

    private String status;
    private String node;
    private String jid;
    private String secret;


    public PushCommandResultIQ(XmlPullParser parser) {
        super(ELEMENT, NAMESPACE);

        // <iq xmlns='jabber:client' from='jerome-all-in-one-dev-1.opentouch.cloud'
        // to='3838fb41a8804294bfbedc3caa2d5400@jerome-all-in-one-dev-1.opentouch.cloud/mobile_android_356571062442546'
        // id='BDQYZ-181' type='status'>
        // <command xmlns='http://jabber.org/protocol/commands' sessionid='2016-10-19T11:42:34.560816Z'
        // node='register-push-gcm' status='completed'><x xmlns='jabber:x:data' type='status'>
        // <field var='jid'><value>pubsub.jerome-all-in-one-dev-1.opentouch.cloud</value></field>
        // <field var='node'><value>12488598993181611467</value></field>
        // <field var='secret'><value>2885101682956746271</value></field>
        // </x></command></iq>

        try {

            String name = parser.getName();
            String currentAttributeName = "";
            int eventType = parser.getEventType();

            // parse xml structure until end of tag called "status"
            // do not parse the whole xml structure since this parser is used afterwards
            // warning : we have to keep the same initial depth when we have finished to parse.
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (!StringsUtil.isNullOrEmpty(name) &&
                        (eventType != XmlPullParser.END_TAG)) {
                    switch (name) {
                        case ELEMENT:
                            status = parser.getAttributeValue("", "status");
                            break;
                        case "field":
                            currentAttributeName = parser.getAttributeValue("", "var");
                            break;
                        case "value":
                            switch (currentAttributeName) {
                                case "jid":
                                    jid = parser.nextText();
                                    break;
                                case "node":
                                    node = parser.nextText();
                                    break;
                                case "secret":
                                    secret = parser.nextText();
                                    break;
                            }
                            break;
                        default:
                            break;
                    }
                }
                parser.next();
                name = parser.getName();
                eventType = parser.getEventType();
                if (!StringsUtil.isNullOrEmpty(name) && (name.equals(ELEMENT)) &&
                        (eventType == XmlPullParser.END_TAG)) {
                    Log.getLogger().verbose(LOG_TAG, "command END_TAG reached");
                    break;
                }
            }
        } catch (XmlPullParserException e) {
            Log.getLogger().error(LOG_TAG, "parseMamStanza; XmlPullParserException " + e.getMessage());
        } catch (IOException e) {
            Log.getLogger().error(LOG_TAG, "parseMamStanza; IOException " + e.getMessage());
        }
    }

    public boolean isStatusCompleted() {
        return( status != null && status.equals("completed") );
    }

    public String getNode() {
        return node;
    }

    public String getJid() {
        return jid;
    }

    public String getSecret() {
        return secret;
    }

    public String getElementName() {
        return ELEMENT;
    }

    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        //TODO : usefull ?
        return null;
    }
}

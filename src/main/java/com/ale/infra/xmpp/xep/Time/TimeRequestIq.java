package com.ale.infra.xmpp.xep.Time;

import org.jivesoftware.smack.packet.IQ;

/**
 * Created by wilsius on 05/06/2017.
 */

public class TimeRequestIq extends IQ {

    public static final String NAMESPACE = "urn:xmpp:time";
    public static final String ELEMENT = "time";

    public TimeRequestIq(String to, String from, String timeId) {
        super(ELEMENT, NAMESPACE);
        this.setTo(to);
        this.setFrom(from);
    }
//
//    <iq type='get'
//    from='romeo@montague.net/orchard'
//    to='juliet@capulet.com/balcony'
//    id='time_1'>
//  <time xmlns='urn:xmpp:time'/>
//</iq>

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        return xml;
    }
}

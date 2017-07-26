package com.ale.infra.xmpp.xep.calendar;

import com.ale.util.StringsUtil;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by trunk1 on 23/01/2017.
 */

public class UntilExtension implements ExtensionElement
{
    public static final String ELEMENT = "until";
    public static final String NAMESPACE = "jabber:client";

    /**
     * original ID of the delivered message
     */
    private String m_until;


    private UntilExtension(String until)
    {
        m_until = until;
    }

    public static UntilExtension from(Stanza packet)
    {
        return packet.getExtension(ELEMENT, NAMESPACE);
    }

    @Override
    public String toXML()
    {
        StringBuilder xml = new StringBuilder();
        xml.append(String.format("<%s xmlns='%s'>", ELEMENT, NAMESPACE));

        if (!StringsUtil.isNullOrEmpty(m_until))
            xml.append(m_until);

        xml.append(String.format("</%s>", ELEMENT));
        return xml.toString();
    }

    @Override
    public String getNamespace()
    {
        return NAMESPACE;
    }

    @Override
    public String getElementName()
    {
        return ELEMENT;
    }

    public String getUntil()
    {
        return m_until;
    }

    public static class Provider extends ExtensionElementProvider<UntilExtension>
    {
        @Override
        public UntilExtension parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException
        {
            return new UntilExtension(parser.nextText());
        }
    }
}


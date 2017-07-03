package com.ale.infra.xmpp.xep.outofband;

import com.ale.util.StringsUtil;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by georges on 14/02/2017.
 */

public class RainbowOutOfBandData implements ExtensionElement {

    public static final String NAMESPACE = "jabber:x:oob";
    public static final String ELEMENT = "x";

    private final String m_url;
    private final String m_mime;
    private final String m_fileName;
    private final long m_size;

    // Rainbow message format :
    //      <url>https://demo.openrainbow.org:443/api/rainbow/fileserver/v1.0/files/58cfb4053f2969d36a43847f</url>
    //      <mime>image/jpeg</mime>
    //      <filename>IMG_0085.JPG</filename>
    //      <size>387423</size>


    public RainbowOutOfBandData(String url, String mime, String fileName, long size) {
        m_url = url;
        m_mime = mime;
        m_fileName = fileName;
        m_size = size;
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    public String getUrl() {
        return m_url;
    }

    public String getMime() {
        return m_mime;
    }

    public String getFileName() {
        return m_fileName;
    }

    public long getSize() {
        return m_size;
    }


    @Override
    public String toXML() {
        StringBuilder xml = new StringBuilder();
        xml.append(String.format("<%s xmlns='%s'><url>", ELEMENT, NAMESPACE));
        xml.append(m_url);
        xml.append("</url>");

        if (m_fileName != null)
            xml.append(String.format("<filename>%s</filename>", m_fileName));

        if (m_mime != null)
            xml.append(String.format("<mime>%s</mime>", m_mime));

        if (m_size >= 0)
            xml.append(String.format("<size>%d</size>", m_size));

        xml.append(String.format("</%s>", ELEMENT));
        return xml.toString();
    }


    // Rainbow message format :
    //      <url>https://demo.openrainbow.org:443/api/rainbow/fileserver/v1.0/files/58cfb4053f2969d36a43847f</url>
    //      <mime>image/jpeg</mime>
    //      <filename>IMG_0085.JPG</filename>
    //      <size>387423</size>

    public static class Provider extends ExtensionElementProvider<RainbowOutOfBandData> {

        @Override
        public RainbowOutOfBandData parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException {

            String url = "";
            String mime = "";
            String sizeStrg = "";
            String fileName = "";

            String name = parser.getName();
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (!StringsUtil.isNullOrEmpty(name)) {
                    switch (name) {
                        case "url":
                            url = parser.nextText();
                            break;
                        case "mime":
                            mime = parser.nextText();
                            break;
                        case "filename":
                            fileName = parser.nextText();
                            break;
                        case "size":
                            sizeStrg = parser.nextText();
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
                    break;
                }
            }


            Long size = Long.valueOf(0);
            if( !StringsUtil.isNullOrEmpty(sizeStrg))
                size = Long.valueOf(sizeStrg);
            return new RainbowOutOfBandData(url, mime, fileName, size);
        }
    }

}
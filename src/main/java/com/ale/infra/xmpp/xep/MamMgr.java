package com.ale.infra.xmpp.xep;

import com.ale.infra.xmpp.AbstractRainbowXMPPConnection;
import com.ale.util.StringsUtil;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.DefaultExtensionElement;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.id.StanzaIdUtil;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by georges on 14/03/16.
 */
public class MamMgr {
    private static final String LOG_TAG = "MamMgr";

    private static int m_msgCounter = 0;

    private final AbstractRainbowXMPPConnection m_connection;
    private final String m_userJid;

    private final long TIMEOUT_30S = 30000;

    public MamMgr(AbstractRainbowXMPPConnection connection, String userJid) {
        m_connection = connection;
        m_userJid = userJid;

        ////////// MAM providers /////////////
        // provider for result iq when archived messages have been received:
        ProviderManager.addIQProvider("fin", MamMessagePacketExtension.NAMESPACE, new IQProvider<IQ>() {
            @Override
            public IQ parse(XmlPullParser xmlPullParser, int i) throws XmlPullParserException, IOException, SmackException {
                return new MamIQResult(xmlPullParser,"fin", MamMessagePacketExtension.NAMESPACE);
            }
        });

        ProviderManager.addIQProvider("fin", MamMessagePacketExtension.NAMESPACE, new IQProvider<IQ>() {
            @Override
            public IQ parse(XmlPullParser xmlPullParser, int i) throws XmlPullParserException, IOException, SmackException {
                return new MamIQResult(xmlPullParser , "fin", MamMessagePacketExtension.NAMESPACE);
            }
        });

        // provider for messages with MAM extension:
        ProviderManager.addExtensionProvider(MamMessagePacketExtension.ELEMENT, MamMessagePacketExtension.NAMESPACE, new ExtensionElementProvider<ExtensionElement>() {
            @Override
            public DefaultExtensionElement parse(XmlPullParser parser, int initialDepth) throws org.xmlpull.v1.XmlPullParserException,
                    IOException {
                return new MamMessagePacketExtension(parser);
            }
        });
    }


    /**
     * get archived messages through XMPP protocol (MAM)
     * @param to
     * @param with
     * @param lastMsgCtr (int)
     * @return succeeded(boolean) : true if request has succeeded.
     */
    public MamIQResult getArchivedMessages(String to, String with, int lastMsgCtr) throws Exception{
        MamRequestIQ requestIQ = new MamRequestIQ(with, getMsgQueryId(),lastMsgCtr);
        requestIQ.setType(IQ.Type.set);
        requestIQ.setStanzaId(StanzaIdUtil.newStanzaId());
        if ( !StringsUtil.isNullOrEmpty(to) )
            requestIQ.setTo(to);

        return getArchivedMessages(requestIQ);
    }

    /**
     * get archived messages through XMPP protocol (MAM)
     * @param to
     * @param with
     * @param lastMsgCtr
     * @param lastMessageId
     * @return succeeded(boolean) : true if request has succeeded.
     */
    public MamIQResult getArchivedMessages(String to, String with, int lastMsgCtr, String lastMessageId) throws Exception{
        MamRequestIQ requestIQ = new MamRequestIQ(with, getMsgQueryId(), lastMsgCtr, lastMessageId);
        requestIQ.setType(IQ.Type.set);
        requestIQ.setStanzaId(StanzaIdUtil.newStanzaId());
        if ( !StringsUtil.isNullOrEmpty(to) )
            requestIQ.setTo(to);

        return getArchivedMessages(requestIQ);
    }

    public MamIQResult deleteArchivedConversation(String imJabberId, String dateBefore) throws Exception {
        MamDeleteConversationRequestIQ requestIQ = new MamDeleteConversationRequestIQ (imJabberId, dateBefore);

        requestIQ.setType(IQ.Type.set);
        requestIQ.setStanzaId(StanzaIdUtil.newStanzaId());


        return deleteArchivedConversation(requestIQ);
    }


    private MamIQResult getArchivedMessages(MamRequestIQ requestIQ) throws Exception{
        MamIQResult iqResult = m_connection.createPacketCollectorAndSend(requestIQ).nextResultOrThrow(TIMEOUT_30S);
            if(iqResult.getStanzaId().equalsIgnoreCase(requestIQ.getStanzaId())){
            } else {
                throw new Exception("Error while getting archived messages : request and result stanza ids are different !");
            }
        return iqResult;
    }

    private MamIQResult deleteArchivedConversation(MamDeleteConversationRequestIQ requestIQ) throws Exception {
        MamIQResult iqResult = m_connection.createPacketCollectorAndSend(requestIQ).nextResultOrThrow();
        if(iqResult.getStanzaId().equalsIgnoreCase(requestIQ.getStanzaId())){
        } else {
            throw new Exception("Error while getting archived messages : request and result stanza ids are different !");
        }
        return iqResult;
    }


    private String getMsgQueryId() {
        StringBuilder queryId = new StringBuilder();
        if( m_userJid.contains("@")) {
            queryId.append(m_userJid.substring(0,m_userJid.indexOf("@")));
        }
        else {
            queryId.append(m_userJid);
        }
        queryId.append("_");
        queryId.append(String.valueOf(m_msgCounter++));
        return queryId.toString();
    }

    public void disconnect()
    {
        if( m_connection != null)
        {
            ProviderManager.removeIQProvider("fin", MamMessagePacketExtension.NAMESPACE);

            ProviderManager.removeIQProvider("fin", MamMessagePacketExtension.NAMESPACE);

            // provider for messages with MAM extension:
            ProviderManager.removeExtensionProvider(MamMessagePacketExtension.ELEMENT, MamMessagePacketExtension.NAMESPACE);
        }
    }
}

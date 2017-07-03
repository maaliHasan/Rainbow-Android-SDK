package com.ale.infra.xmpp.xep;

import com.ale.infra.xmpp.AbstractRainbowXMPPConnection;
import com.ale.infra.xmpp.XMPPWebSocketConnection;
import com.ale.infra.xmpp.xep.ManagementReceipt.ManagementReceiptPacketExtension;

import org.jivesoftware.smack.packet.DefaultExtensionElement;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;

/**
 * Created by wilsius on 14/06/16.
 */
public class ManagementMgr {

    private static final String LOG_TAG = "ManagementMgr";

    private final AbstractRainbowXMPPConnection m_connection;

    public ManagementMgr(AbstractRainbowXMPPConnection connection) {
        m_connection = connection;

        ProviderManager.addExtensionProvider("conversation", ManagementReceiptPacketExtension.NAMESPACE, new ExtensionElementProvider<ExtensionElement>() {
            @Override
            public DefaultExtensionElement parse(XmlPullParser parser, int initialDepth) throws org.xmlpull.v1.XmlPullParserException,
                    IOException {
                return new ManagementReceiptPacketExtension(parser);
            }
        });
    }
}

/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import com.ale.util.log.Log;

import net.java.sip.communicator.impl.protocol.jabber.extensions.DefaultPacketExtensionProvider;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Element;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

//import net.java.sip.communicator.impl.protocol.jabber.extensions.gtalk.*;

/**
 * An implementation of a Jingle IQ provider that parses incoming Jingle IQs.
 *
 * @author Emil Ivov
 */
public class JingleIQProvider extends IQProvider
{
    private static final String LOG_TAG = "JingleIQProvider";

    /**
     * Creates a new instance of the <tt>JingleIQProvider</tt> and register all
     * jingle related extension providers. It is the responsibility of the
     * application to register the <tt>JingleIQProvider</tt> itself.
     */
    public JingleIQProvider()
    {
        // <group/> provider
        ProviderManager.addExtensionProvider(GroupPacketExtension.ELEMENT_NAME, GroupPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<GroupPacketExtension>(GroupPacketExtension.class));

        //<description/> provider
        ProviderManager.addExtensionProvider(RtpDescriptionPacketExtension.ELEMENT_NAME, RtpDescriptionPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<RtpDescriptionPacketExtension>(RtpDescriptionPacketExtension.class));

        //<payload-type/> provider
        ProviderManager.addExtensionProvider(PayloadTypePacketExtension.ELEMENT_NAME, RtpDescriptionPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<PayloadTypePacketExtension>(PayloadTypePacketExtension.class));
        ProviderManager.addExtensionProvider(RtcpFbPacketExtension.ELEMENT_NAME, RtcpFbPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<RtcpFbPacketExtension>(RtcpFbPacketExtension.class));

        //<rtcp-mux/> provider
        ProviderManager.addExtensionProvider(RtcpMuxExtension.ELEMENT_NAME, RtpDescriptionPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<RtcpMuxExtension>(RtcpMuxExtension.class));

        //<parameter/> provider
        ProviderManager.addExtensionProvider(ParameterPacketExtension.ELEMENT_NAME, RtpDescriptionPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<ParameterPacketExtension>(ParameterPacketExtension.class));

        //<rtp-hdrext/> provider
        ProviderManager.addExtensionProvider(RTPHdrExtPacketExtension.ELEMENT_NAME, RTPHdrExtPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<RTPHdrExtPacketExtension>(RTPHdrExtPacketExtension.class));

        //<encryption/> provider
        ProviderManager.addExtensionProvider(EncryptionPacketExtension.ELEMENT_NAME, RtpDescriptionPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<EncryptionPacketExtension>(EncryptionPacketExtension.class));


        //<crypto/> provider
        ProviderManager.addExtensionProvider(CryptoPacketExtension.ELEMENT_NAME, RtpDescriptionPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<CryptoPacketExtension>(CryptoPacketExtension.class));

        //<streams/> provider
        ProviderManager.addExtensionProvider(StreamsPacketExtension.ELEMENT_NAME, RtpDescriptionPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<StreamsPacketExtension>(StreamsPacketExtension.class));

        //<stream/> provider
        ProviderManager.addExtensionProvider(StreamPacketExtension.ELEMENT_NAME, RtpDescriptionPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<StreamPacketExtension>(StreamPacketExtension.class));

        //<ssrc/> provider
        ProviderManager.addExtensionProvider(SsrcPacketExtension.ELEMENT_NAME, RtpDescriptionPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<SsrcPacketExtension>(SsrcPacketExtension.class));

        //<zrtp-hash/> provider
        ProviderManager.addExtensionProvider(ZrtpHashPacketExtension.ELEMENT_NAME, ZrtpHashPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<ZrtpHashPacketExtension>(ZrtpHashPacketExtension.class));

        //ice-udp transport
        ProviderManager.addExtensionProvider(IceUdpTransportPacketExtension.ELEMENT_NAME, IceUdpTransportPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<IceUdpTransportPacketExtension>(IceUdpTransportPacketExtension.class));

        //<raw-udp/> provider
        ProviderManager.addExtensionProvider(RawUdpTransportPacketExtension.ELEMENT_NAME, RawUdpTransportPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<RawUdpTransportPacketExtension>(RawUdpTransportPacketExtension.class));

        //        //Google P2P transport
        //        ProviderManager.addExtensionProvider(
        //            GTalkTransportPacketExtension.ELEMENT_NAME,
        //            GTalkTransportPacketExtension.NAMESPACE,
        //            new DefaultPacketExtensionProvider<GTalkTransportPacketExtension>(
        //                            GTalkTransportPacketExtension.class));

        //ice-udp <candidate/> provider
        ProviderManager.addExtensionProvider(CandidatePacketExtension.ELEMENT_NAME, IceUdpTransportPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<CandidatePacketExtension>(CandidatePacketExtension.class));

        //raw-udp <candidate/> provider
        ProviderManager.addExtensionProvider(CandidatePacketExtension.ELEMENT_NAME, RawUdpTransportPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<CandidatePacketExtension>(CandidatePacketExtension.class));

        //        //Google P2P <candidate/> provider
        //        ProviderManager.addExtensionProvider(
        //            GTalkCandidatePacketExtension.ELEMENT_NAME,
        //            GTalkTransportPacketExtension.NAMESPACE,
        //            new DefaultPacketExtensionProvider<GTalkCandidatePacketExtension>(
        //                            GTalkCandidatePacketExtension.class));

        //ice-udp <remote-candidate/> provider
        ProviderManager.addExtensionProvider(RemoteCandidatePacketExtension.ELEMENT_NAME, IceUdpTransportPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<RemoteCandidatePacketExtension>(RemoteCandidatePacketExtension.class));

        //inputevt <inputevt/> provider
        ProviderManager.addExtensionProvider(InputEvtPacketExtension.ELEMENT_NAME, InputEvtPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<InputEvtPacketExtension>(InputEvtPacketExtension.class));

        //coin <conference-info/> provider
        //ProviderManager.addExtensionProvider(CoinPacketExtension.ELEMENT_NAME, CoinPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<CoinPacketExtension>(CoinPacketExtension.class));

        /*
         * XEP-0251: Jingle Session Transfer <transfer/> and <transferred>
         * providers
         */
        ProviderManager.addExtensionProvider(TransferPacketExtension.ELEMENT_NAME, TransferPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<TransferPacketExtension>(TransferPacketExtension.class));
        ProviderManager.addExtensionProvider(TransferredPacketExtension.ELEMENT_NAME, TransferredPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<TransferredPacketExtension>(TransferredPacketExtension.class));

        ProviderManager.addExtensionProvider(DtlsFingerprintPacketExtension.ELEMENT_NAME, DtlsFingerprintPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<DtlsFingerprintPacketExtension>(DtlsFingerprintPacketExtension.class));
        // <source/> provider
        ProviderManager.addExtensionProvider(SourcePacketExtension.ELEMENT_NAME, SourcePacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<SourcePacketExtension>(SourcePacketExtension.class));
        ProviderManager.addExtensionProvider(ParameterPacketExtension.ELEMENT_NAME, SourcePacketExtension.NAMESPACE, new DefaultPacketExtensionProvider<ParameterPacketExtension>(ParameterPacketExtension.class));
    }

    @Override
    public Element parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException
    {
        JingleIQ jingleIQ = new JingleIQ();

        //let's first handle the "jingle" element params.
        JingleAction action = JingleAction.parseString(parser.getAttributeValue("", JingleIQ.ACTION_ATTR_NAME));
        String initiator = parser.getAttributeValue("", JingleIQ.INITIATOR_ATTR_NAME);
        String responder = parser.getAttributeValue("", JingleIQ.RESPONDER_ATTR_NAME);
        String sid = parser.getAttributeValue("", JingleIQ.SID_ATTR_NAME);
        String localType = parser.getAttributeValue("", JingleIQ.LOCAL_TYPE_ATTR_NAME);

        jingleIQ.setAction(action);
        jingleIQ.setInitiator(initiator);
        jingleIQ.setResponder(responder);
        jingleIQ.setSID(sid);
        jingleIQ.setLocalType(localType);

        boolean done = false;

        // Sub-elements providers
        DefaultPacketExtensionProvider<GroupPacketExtension> groupProvider = new DefaultPacketExtensionProvider<GroupPacketExtension>(GroupPacketExtension.class);
        DefaultPacketExtensionProvider<ContentPacketExtension> contentProvider = new DefaultPacketExtensionProvider<ContentPacketExtension>(ContentPacketExtension.class);
        ReasonProvider reasonProvider = new ReasonProvider();
        DefaultPacketExtensionProvider<TransferPacketExtension> transferProvider = new DefaultPacketExtensionProvider<TransferPacketExtension>(TransferPacketExtension.class);
        DefaultPacketExtensionProvider<CoinPacketExtension> coinProvider = new DefaultPacketExtensionProvider<CoinPacketExtension>(CoinPacketExtension.class);

        // Now go on and parse the jingle element's content.
        int eventType;
        String elementName;
        String namespace;

        while (!done)
        {
            eventType = parser.next();
            elementName = parser.getName();
            namespace = parser.getNamespace();

            if (eventType == XmlPullParser.START_TAG)
            {
                try
                {
                    // <group/>
                    if (elementName.equals(GroupPacketExtension.ELEMENT_NAME))
                    {
                        GroupPacketExtension group = groupProvider.parseExtension(parser);
                        jingleIQ.setGroup(group);
                    }
                    // <content/>
                    else if (elementName.equals(ContentPacketExtension.ELEMENT_NAME))
                    {
                        ContentPacketExtension content = contentProvider.parseExtension(parser);
                        jingleIQ.addContent(content);
                    }
                    // <reason/>
                    else if (elementName.equals(ReasonPacketExtension.ELEMENT_NAME))
                    {
                        ReasonPacketExtension reason = reasonProvider.parseExtension(parser);
                        jingleIQ.setReason(reason);
                    }
                    // <transfer/>
                    else if (elementName.equals(TransferPacketExtension.ELEMENT_NAME) && namespace.equals(TransferPacketExtension.NAMESPACE))
                    {
                        jingleIQ.addExtension(transferProvider.parseExtension(parser));
                    }
                    else if (elementName.equals(CoinPacketExtension.ELEMENT_NAME))
                    {
                        jingleIQ.addExtension(coinProvider.parseExtension(parser));
                    }
                }
                catch (Exception e)
                {
                    Log.getLogger().error(LOG_TAG, "Impossible to parse extension", e);
                }

                //<mute/> <active/> and other session-info elements
                if (namespace.equals(SessionInfoPacketExtension.NAMESPACE))
                {
                    SessionInfoType type = SessionInfoType.valueOf(elementName);

                    //<mute/>
                    if (type == SessionInfoType.mute || type == SessionInfoType.unmute)
                    {
                        String name = parser.getAttributeValue("", MuteSessionInfoPacketExtension.NAME_ATTR_VALUE);

                        jingleIQ.setSessionInfo(new MuteSessionInfoPacketExtension(type == SessionInfoType.mute, name));
                    }
                    //<hold/>, <unhold/>, <active/>, etc.
                    else
                    {
                        jingleIQ.setSessionInfo(new SessionInfoPacketExtension(type));
                    }
                }
            }

            if ((eventType == XmlPullParser.END_TAG) && parser.getName().equals(JingleIQ.ELEMENT_NAME))
            {
                done = true;
            }
        }
        return jingleIQ;
    }
}

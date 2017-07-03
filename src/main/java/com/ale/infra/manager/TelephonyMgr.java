package com.ale.infra.manager;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.PermissionChecker;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.capabilities.ICapabilities;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.contact.RainbowPresence;
import com.ale.infra.manager.call.ITelephonyListener;
import com.ale.infra.manager.call.MediaState;
import com.ale.infra.manager.call.Statistics;
import com.ale.infra.manager.call.WebRTCCall;
import com.ale.infra.platformservices.ICallStateChangedNotifier;
import com.ale.infra.platformservices.IGsmPhone;
import com.ale.infra.proxy.admin.ISettings;
import com.ale.infra.proxy.admin.SettingsProxy;
import com.ale.infra.xmpp.AbstractRainbowXMPPConnection;
import com.ale.infra.xmpp.XmppConnection;
import com.ale.infra.xmpp.xep.call.CallIq;
import com.ale.infra.xmpp.xep.call.RainbowCallReceivedIq;
import com.ale.rainbow.phone.LoudspeakerHelper;
import com.ale.rainbow.phone.MuteHelper;
import com.ale.util.Duration;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import net.java.sip.communicator.impl.protocol.jabber.extensions.InitiationPacketExtensionProvider;
import net.java.sip.communicator.impl.protocol.jabber.extensions.JingleUtils;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.AcceptPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleAction;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQProvider;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JinglePacketFactory;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ProceedPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ProposePacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.Reason;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RejectPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RetractPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RtpDescriptionPacketExtension;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.iqrequest.AbstractIqRequestHandler;
import org.jivesoftware.smack.iqrequest.IQRequestHandler;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.StatsObserver;
import org.webrtc.StatsReport;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static org.webrtc.PeerConnection.SignalingState.HAVE_REMOTE_OFFER;
import static org.webrtc.PeerConnection.SignalingState.STABLE;


/**
 * Created by cebruckn on 30/10/15.
 */
public class TelephonyMgr implements ChatMessageListener, ChatManagerListener
{
    private static final int LOOPBACK = 16;
    private final static String LOG_TAG = "TelephonyMgr";
    private final PCObserver m_pcObserver = new PCObserver();
    private final SDPObserver m_sdpObserver = new SDPObserver();
    private final Context m_applicationContext;
    private final AudioManager m_audioManager;
    private final Contact m_user;
    private final String m_resource;
    private final IContactCacheMgr m_contactCacheMgr;
    private final List<ITelephonyListener> m_listeners = new ArrayList<>();
    private final ICapabilities m_capabilities;
    private final XmppContactMgr m_xmppContactMgr;
    private ArrayList<PeerConnection.IceServer> m_iceServers;
    private ICallStateChangedNotifier m_callStateChangedNotifier;
    private IGsmPhone m_gsmPhone;
    private AbstractRainbowXMPPConnection m_connection;
    private PeerConnection m_peerConnection;
    private VideoTrack m_localVideoTrack;
    private IStreamManagementListener m_streamManagementListener;
    private MediaStream m_addedStream;
    private boolean m_needToSendTransportReplace = false;
    private Timer m_iceDisconnectedTimer;
    private CameraVideoCapturer m_capturer;
    private WebRTCCall m_currentCall;
    private ILocalVideoTrackListener m_localVideoTrackListener;
    private AudioSource m_audioSource;
    private VideoSource m_videoSource;
    private PeerConnectionFactory m_peerConnectionFactory;
    private MediaStream m_mediaCallerStream;
    private JingleAction m_nextAction = null;
    private String m_pushCallSender;
    private ProposePacketExtension m_pushProposePacketExtension;
    private RainbowPresence m_savedPresence;
    private boolean m_isMutted;
    private Handler m_handler = new Handler(Looper.getMainLooper());
    private Timer m_statsTimer;
    private Statistics m_statistics = new Statistics();
    private SessionDescription m_localSdpForIceRestart;
    private Set<Chat> m_chats = new HashSet<>();

    private final ConnectionListener m_connectionListener = new ConnectionListener()
    {
        @Override
        public void connected(XMPPConnection connection)
        {
        }

        @Override
        public void authenticated(XMPPConnection connection, boolean resumed)
        {
            checkSendTransportReplaceNeeded();

            if (!StringsUtil.isNullOrEmpty(m_pushCallSender) && m_pushProposePacketExtension != null)
            {
                handlePropose(m_pushCallSender, m_pushProposePacketExtension);
                m_pushCallSender = null;
                m_pushProposePacketExtension = null;
            }
        }

        @Override
        public void connectionClosed()
        {
            if (m_peerConnection != null)
                m_needToSendTransportReplace = true;
        }

        @Override
        public void connectionClosedOnError(Exception e)
        {
            if (m_peerConnection != null)
                m_needToSendTransportReplace = true;
        }

        @Override
        public void reconnectionSuccessful()
        {
            checkSendTransportReplaceNeeded();
        }

        @Override
        public void reconnectingIn(int seconds)
        {
        }

        @Override
        public void reconnectionFailed(Exception e)
        {
        }
    };

    private AudioManager.OnAudioFocusChangeListener m_audioListener = new AudioManager.OnAudioFocusChangeListener()
    {
        @Override
        public void onAudioFocusChange(int focusChange)
        {
            Log.getLogger().info(LOG_TAG, "onAudioFocusChange: " + focusChange);
        }
    };

    public TelephonyMgr(AbstractRainbowXMPPConnection c, Context applicationContext, XmppContactMgr xmppContactMgr, IContactCacheMgr contactCacheMgr, String newResourceId, SettingsProxy settingsProxy, ICapabilities capabilities)
    {
        m_connection = c;
        m_applicationContext = applicationContext;
        m_audioManager = (AudioManager) m_applicationContext.getSystemService(Context.AUDIO_SERVICE);
        m_user = contactCacheMgr.getUser();
        m_resource = newResourceId;
        m_contactCacheMgr = contactCacheMgr;
        m_capabilities = capabilities;
        m_xmppContactMgr = xmppContactMgr;

        m_connection.addConnectionListener(m_connectionListener);

        m_connection.registerIQRequestHandler(new AbstractIqRequestHandler(CallIq.ELEMENT, CallIq.NAMESPACE, IQ.Type.set, IQRequestHandler.Mode.async)
        {
            @Override
            public IQ handleIQRequest(IQ iqRequest)
            {
                CallIq iq = (CallIq) iqRequest;
                manageCallMessage(iq);
                return iq.result();
            }
        });

        ProviderManager.addIQProvider(CallIq.ELEMENT, CallIq.NAMESPACE, new RainbowCallReceivedIq());

        initializeIceServers(settingsProxy);

        initializeJingle();

        initializeSessionsInitiation();

        initializeGSMPhone();
    }

    private void initializeIceServers(SettingsProxy settingsProxy)
    {
        m_iceServers = new ArrayList<>();

        if (settingsProxy == null)
            return;

        settingsProxy.getIceServers(new ISettings.IIceServersListener()
        {
            @Override
            public void onGetIceServersSuccess(List<PeerConnection.IceServer> servers)
            {
                Log.getLogger().info(LOG_TAG, "Found " + servers.size() + " Ice Servers");

                m_iceServers.addAll(servers);
            }

            @Override
            public void onGetIceServersFailure()
            {
                Log.getLogger().error(LOG_TAG, "NO ICE SERVERS FROM RAINBOW");
            }
        });
    }

    private List<PeerConnection.IceServer> getOrderedIceServers()
    {
        LocationMgr.Region region = RainbowContext.getInfrastructure().getLocationMgr().calculRegion();

        Log.getLogger().info(LOG_TAG, " turn server region is: " + region.toString());

        if (region == LocationMgr.Region.AMERICA)
        {
            return setRegionServerFirst("turn-na");
        }
        if (region == LocationMgr.Region.EUROPA)
        {
            return setRegionServerFirst("turn-eu");
        }
        if (region == LocationMgr.Region.ASIA)
        {
            return setRegionServerFirst("turn-as");
        }
        if (region == LocationMgr.Region.OCEANIA)
        {
            return setRegionServerFirst("turn-oc");
        }

        return m_iceServers;

    }

    private List<PeerConnection.IceServer> setRegionServerFirst(String reg)
    {
        List<PeerConnection.IceServer> list = new ArrayList<>();

        for (PeerConnection.IceServer server : m_iceServers)
        {
            if (server.toString().contains(reg))
            {
                list.add(0, server);
            }
            else
            {
                list.add(server);
            }
        }

        for (PeerConnection.IceServer server : list)
            Log.getLogger().info(LOG_TAG, " regien: " + reg + " server is" + server.toString());

        return list;
    }

    private MediaConstraints getSdpConstraints(boolean withIceRestart)
    {
        MediaConstraints sdpMediaConstraints = new MediaConstraints();
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("VoiceActivityDetection", "true"));
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

        if (withIceRestart)
            sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("IceRestart", "true"));

        return sdpMediaConstraints;
    }

    private MediaConstraints getPcConstraints()
    {
        MediaConstraints pcConstraints = new MediaConstraints();
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair("RtpDataChannels", "true"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

        return pcConstraints;
    }

    private MediaConstraints getAudioConstraints()
    {
        MediaConstraints audioConstraints = new MediaConstraints();
        audioConstraints.optional.add(new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
        audioConstraints.optional.add(new MediaConstraints.KeyValuePair("googAutoGainControl", "true"));
        audioConstraints.optional.add(new MediaConstraints.KeyValuePair("googHighpassFilter", "true"));
        audioConstraints.optional.add(new MediaConstraints.KeyValuePair("googNoiseSupression", "true"));
        audioConstraints.optional.add(new MediaConstraints.KeyValuePair("googNoisesuppression2", "true"));
        audioConstraints.optional.add(new MediaConstraints.KeyValuePair("googEchoCancellation2", "true"));
        audioConstraints.optional.add(new MediaConstraints.KeyValuePair("googAutoGainControl2", "true"));

        return audioConstraints;
    }

    private void initializeJingle()
    {
        ServiceDiscoveryManager discoManager = ServiceDiscoveryManager.getInstanceFor(m_connection);
        discoManager.addFeature("urn:xmpp:jingle:1");
        discoManager.addFeature("urn:xmpp:jingle:apps:rtp:1");
        discoManager.addFeature("urn:xmpp:jingle:apps:rtp:audio");
        discoManager.addFeature("urn:xmpp:jingle:apps:rtp:video");
        discoManager.addFeature("urn:xmpp:jingle:transports:ice-udp:1");
        discoManager.addFeature("urn:xmpp:jingle:transports:dtls-sctp:1");
        discoManager.addFeature("urn:ietf:rfc:5761");
        discoManager.addFeature("urn:ietf:rfc:5888");

        m_connection.registerIQRequestHandler(new AbstractIqRequestHandler(JingleIQ.ELEMENT_NAME, JingleIQ.NAMESPACE, IQ.Type.set, IQRequestHandler.Mode.async)
        {
            @Override
            public IQ handleIQRequest(IQ iqRequest)
            {
                synchronized (m_applicationContext)
                {
                    processPacket(iqRequest);
                    return IQ.createResultIQ(iqRequest);
                }
            }
        });

        ProviderManager.addIQProvider(JingleIQ.ELEMENT_NAME, JingleIQ.NAMESPACE, new JingleIQProvider());
    }

    private void initializeSessionsInitiation()
    {
        ProviderManager.addExtensionProvider(AcceptPacketExtension.ELEMENT_NAME, AcceptPacketExtension.NAMESPACE, new InitiationPacketExtensionProvider<>(AcceptPacketExtension.class));
        ProviderManager.addExtensionProvider(ProposePacketExtension.ELEMENT_NAME, ProposePacketExtension.NAMESPACE, new InitiationPacketExtensionProvider<>(ProposePacketExtension.class));
        ProviderManager.addExtensionProvider(ProceedPacketExtension.ELEMENT_NAME, ProceedPacketExtension.NAMESPACE, new InitiationPacketExtensionProvider<>(ProceedPacketExtension.class));
        ProviderManager.addExtensionProvider(RejectPacketExtension.ELEMENT_NAME, RejectPacketExtension.NAMESPACE, new InitiationPacketExtensionProvider<>(RejectPacketExtension.class));
        ProviderManager.addExtensionProvider(RetractPacketExtension.ELEMENT_NAME, RetractPacketExtension.NAMESPACE, new InitiationPacketExtensionProvider<>(RetractPacketExtension.class));

        ChatManager.getInstanceFor(m_connection).addChatListener(this);
    }


    private void initializeGSMPhone()
    {
        m_callStateChangedNotifier = new ICallStateChangedNotifier()
        {
            @Override
            public void notifyCallStateChangedIdle()
            {
                if (m_currentCall != null)
                    MuteHelper.unmute();
            }

            @Override
            public void notifyCallStateChangedOffHook()
            {
                if (m_currentCall != null)
                    MuteHelper.mute(true);
            }

            @Override
            public void notifyCallStateChangedRinging(String incomingNumber)
            {

            }
        };

        m_gsmPhone = RainbowContext.getPlatformServices().getGsmPhone();

        m_handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (m_gsmPhone != null)
                    m_gsmPhone.listenTelephonyEvents(m_callStateChangedNotifier);
            }
        });
    }

    private void checkSendTransportReplaceNeeded()
    {
        if (m_needToSendTransportReplace && m_peerConnection != null)
        {
            Log.getLogger().info(LOG_TAG, "ICE Restart");

            m_nextAction = JingleAction.TRANSPORT_REPLACE;
            m_peerConnection.createOffer(m_sdpObserver, getSdpConstraints(true));
            m_needToSendTransportReplace = false;
        }
    }

    private void manageCallMessage(CallIq iq)
    {
        IGsmPhone gsmPhone = RainbowContext.getPlatformServices().getGsmPhone();

        if (iq.isDirectCall())
            gsmPhone.makeCall(iq.getPhoneNumber());
        else
            gsmPhone.initiateCall(iq.getPhoneNumber());
    }

    public void makeCall(final Contact contact, final boolean video)
    {
        if (m_currentCall != null)
        {
            Log.getLogger().warn(LOG_TAG, "Existing call in progress, ignore make call");
        }
        else
        {
            Log.getLogger().info(LOG_TAG, "Make call to " + contact.getImJabberId());

            m_currentCall = new WebRTCCall(contact, m_capabilities);
            m_currentCall.setState(MediaState.RINGING_OUTGOING);
            m_currentCall.setInitiatedWithVideo(video);

            Thread t = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    sendPropose(contact, video);
                }
            });
            t.start();

            fireCallAdded();

            saveCurrentPresence();
            sendCallPresence(video);
        }
    }

    private void makeCallAfterInitiation(String from)
    {
        if (m_peerConnection == null)
        {
            m_currentCall.setJid(from);
            m_currentCall.setIsOutgoing(true);

            createPeerConnection(m_currentCall.wasInitiatedWithVideo());

            m_peerConnection.createOffer(m_sdpObserver, getSdpConstraints(true));
        }
        else
            Log.getLogger().warn(LOG_TAG, "MakeCallAfterInitiation ignored since an existing peerconnection exists");
    }

    private void createPeerConnection(boolean withVideo)
    {
        PeerConnectionFactory.initializeAndroidGlobals(m_applicationContext, true, true, true);

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        options.networkIgnoreMask = LOOPBACK;
        m_peerConnectionFactory = new PeerConnectionFactory(options);

        m_peerConnectionFactory.setVideoHwAccelerationOptions(m_currentCall.getEglBaseContext(), m_currentCall.getEglBaseContext());

        PeerConnection.RTCConfiguration configuration = new PeerConnection.RTCConfiguration(getOrderedIceServers());
        configuration.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        configuration.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;

        m_peerConnection = m_peerConnectionFactory.createPeerConnection(configuration, getPcConstraints(), m_pcObserver);

        // First we create an AudioSource
        m_audioSource = m_peerConnectionFactory.createAudioSource(getAudioConstraints());

        // Creates a VideoCapturerAndroid instance for the device name
        m_capturer = createVideoCapturer();

        // First we create a VideoSource
        m_videoSource = m_peerConnectionFactory.createVideoSource(m_capturer);

        createMediaStream(withVideo);

        if (withVideo)
            m_capturer.startCapture(640, 480, 30);

        m_isMutted = false;
    }

    private void createMediaStream(boolean withVideo)
    {
        // Once we have that, we can create our AudioTrack
        // Note that AUDIO_TRACK_ID can be any string that uniquely
        // identifies that audio track in your application
        AudioTrack localAudioTrack = m_peerConnectionFactory.createAudioTrack(UUID.randomUUID().toString(), m_audioSource);

        // We start out with an empty MediaStream object,
        // created with help from our PeerConnectionFactory
        // Note that LOCAL_MEDIA_STREAM_ID can be any string
        m_mediaCallerStream = m_peerConnectionFactory.createLocalMediaStream(UUID.randomUUID().toString());

        // Now we can add our tracks.
        m_mediaCallerStream.addTrack(localAudioTrack);

        m_localVideoTrack = null;

        if (withVideo)
        {
            // Once we have that, we can create our VideoTrack
            // Note that VIDEO_TRACK_ID can be any string that uniquely
            // identifies that video track in your application
            m_localVideoTrack = m_peerConnectionFactory.createVideoTrack(UUID.randomUUID().toString(), m_videoSource);

            if (m_localVideoTrackListener != null)
            {
                m_localVideoTrackListener.onLocalVideoTrackCreated(m_localVideoTrack);
            }

            m_mediaCallerStream.addTrack(m_localVideoTrack);
        }

        m_peerConnection.addStream(m_mediaCallerStream);
    }

    private CameraVideoCapturer createVideoCapturer()
    {
        CameraVideoCapturer videoCapturer;
        if (Camera2Enumerator.isSupported(m_applicationContext))
        {
            Log.getLogger().debug(LOG_TAG, "Creating capturer using camera2 API.");
            videoCapturer = createCameraCapturer(new Camera2Enumerator(m_applicationContext));
        }
        else
        {
            Log.getLogger().debug(LOG_TAG, "Creating capturer using camera1 API.");
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
        }

        return videoCapturer;
    }

    private CameraVideoCapturer createCameraCapturer(CameraEnumerator enumerator)
    {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Log.getLogger().debug(LOG_TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames)
        {
            if (enumerator.isFrontFacing(deviceName))
            {
                Log.getLogger().debug(LOG_TAG, "Creating front facing camera capturer.");
                CameraVideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null)
                {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Log.getLogger().debug(LOG_TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames)
        {
            if (!enumerator.isFrontFacing(deviceName))
            {
                Log.getLogger().debug(LOG_TAG, "Creating other camera capturer.");
                CameraVideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null)
                {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private void processPacket(Stanza packet)
    {
        if (m_currentCall == null)
        {
            Log.getLogger().warn(LOG_TAG, "Received Jingle packet when no call left !" + packet.toString());
            return;
        }

        try
        {
            JingleIQ jiq = (JingleIQ) packet;

            if (m_currentCall.getSid() != null && !m_currentCall.getSid().equals(jiq.getSID()))
            {
                Log.getLogger().warn(LOG_TAG, "Received Jingle packet with wrong SID !" + m_currentCall.getSid());
                return;
            }

            Log.getLogger().info(LOG_TAG, jiq.getAction().toString().toUpperCase(Locale.getDefault()) + ": " + jiq.toXML());

            switch (jiq.getAction())
            {
                case SESSION_INITIATE:
                {
                    SessionDescription bridgeOfferSdp = JingleUtils.toSdp(jiq, SessionDescription.Type.OFFER.toString());
                    m_currentCall.setJid(jiq.getFrom());
                    m_currentCall.setSid(jiq.getSID());
                    Log.getLogger().info(LOG_TAG, bridgeOfferSdp.description);

                    JingleIQ ringing = JinglePacketFactory.createRinging(jiq);
                    m_connection.sendStanza(ringing);

                    createPeerConnection(m_currentCall.wasInitiatedWithVideo());

                    m_peerConnection.setRemoteDescription(m_sdpObserver, bridgeOfferSdp);

                    if (m_peerConnection.signalingState() == HAVE_REMOTE_OFFER)
                    {
                        // We just set the remote offer, time to create our answer.
                        Log.getLogger().info(LOG_TAG, "createAnswer");
                        m_peerConnection.createAnswer(m_sdpObserver, getSdpConstraints(true));

                        m_currentCall.setState(MediaState.ACTIVE);

                        fireCallModified();

                        if (m_currentCall.wasInitiatedWithVideo())
                            LoudspeakerHelper.activateLoudspeaker(m_applicationContext);

                        m_audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

                        requestAudioMode(AudioManager.STREAM_VOICE_CALL);
                    }

                    break;
                }
                case SESSION_ACCEPT:
                {
                    SessionDescription acceptSdp = JingleUtils.toSdp(jiq, SessionDescription.Type.ANSWER.toString());
                    m_currentCall.setJid(jiq.getFrom());
                    m_currentCall.setSid(jiq.getSID());
                    Log.getLogger().info(LOG_TAG, acceptSdp.description);

                    if (m_peerConnection.signalingState() != STABLE)
                        m_peerConnection.setRemoteDescription(m_sdpObserver, acceptSdp);

                    if (m_currentCall.wasInitiatedWithVideo())
                        LoudspeakerHelper.activateLoudspeaker(m_applicationContext);

                    m_audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

                    requestAudioMode(AudioManager.STREAM_VOICE_CALL);

                    break;
                }
                case TRANSPORT_ACCEPT:
                {
                    if (m_localSdpForIceRestart != null)
                    {
                        m_peerConnection.setLocalDescription(m_sdpObserver, m_localSdpForIceRestart);
                        m_localSdpForIceRestart = null;
                    }

                    SessionDescription acceptSdp = JingleUtils.toSdp(jiq, SessionDescription.Type.ANSWER.toString());
                    m_currentCall.setJid(jiq.getFrom());
                    m_currentCall.setSid(jiq.getSID());
                    Log.getLogger().info(LOG_TAG, acceptSdp.description);

                    if (m_peerConnection.signalingState() != STABLE)
                        m_peerConnection.setRemoteDescription(m_sdpObserver, acceptSdp);

                    break;
                }
                case SESSION_TERMINATE:
                {
                    terminateCall();
                    break;
                }
                case TRANSPORT_INFO:
                {
                    String sdpMid = "audio";
                    int sdpMLineIndex;

                    for (ContentPacketExtension cpe : jiq.getContentList())
                    {
                        sdpMid = cpe.getName();
                    }

                    sdpMLineIndex = JingleUtils.getMediaIndex(m_peerConnection.getRemoteDescription(), sdpMid);

                    if (sdpMLineIndex == -1)
                        sdpMLineIndex = JingleUtils.getMediaIndex(m_peerConnection.getLocalDescription(), sdpMid);

                    if (m_peerConnection != null)
                        m_peerConnection.addIceCandidate(new IceCandidate(sdpMid, sdpMLineIndex, JingleUtils.toCandidateString(jiq)));
                    else
                        Log.getLogger().warn(LOG_TAG, "Received Ice Candidate while peerconnection is null or in wrong state");
                    break;
                }
                case CONTENT_ADD:
                case CONTENT_REMOVE:
                case CONTENT_MODIFY:
                {
                    m_nextAction = JingleAction.CONTENT_ACCEPT;

                    SessionDescription bridgeOfferSdp = JingleUtils.toSdp(jiq, SessionDescription.Type.OFFER.toString());
                    Log.getLogger().info(LOG_TAG, bridgeOfferSdp.description);
                    m_peerConnection.setRemoteDescription(m_sdpObserver, bridgeOfferSdp);

                    if (m_peerConnection.signalingState() == HAVE_REMOTE_OFFER)
                    {
                        // We just set the remote offer, time to create our answer.
                        Log.getLogger().info(LOG_TAG, "createAnswer");
                        m_peerConnection.createAnswer(m_sdpObserver, getSdpConstraints(false));
                    }

                    break;
                }
                case CONTENT_ACCEPT:
                {
                    SessionDescription acceptSdp = JingleUtils.toSdp(jiq, SessionDescription.Type.ANSWER.toString());
                    m_currentCall.setJid(jiq.getFrom());
                    m_currentCall.setSid(jiq.getSID());
                    Log.getLogger().info(LOG_TAG, acceptSdp.description);

                    m_peerConnection.setRemoteDescription(m_sdpObserver, acceptSdp);

                    fireCallModified();

                    break;
                }
                case TRANSPORT_REPLACE:
                {
                    if (m_localSdpForIceRestart != null && m_currentCall.isOutgoing())
                    {
                        Log.getLogger().warn(LOG_TAG, "TRANSPORT_REPLACE is ignored for incoming call during ICE Restart");
                        return;
                    }

                    SessionDescription bridgeOfferSdp = JingleUtils.toSdp(jiq, SessionDescription.Type.OFFER.toString());
                    m_currentCall.setJid(jiq.getFrom());
                    m_currentCall.setSid(jiq.getSID());
                    Log.getLogger().info(LOG_TAG, bridgeOfferSdp.description);

                    m_peerConnection.setRemoteDescription(m_sdpObserver, bridgeOfferSdp);

                    m_nextAction = JingleAction.TRANSPORT_ACCEPT;
                    m_peerConnection.createAnswer(m_sdpObserver, getSdpConstraints(true));

                    break;
                }
                default:
                    Log.getLogger().warn(LOG_TAG, " : Unknown Jingle IQ received : " + jiq.toString());
                    break;
            }
        }
        catch (Exception e)
        {
            Log.getLogger().error(LOG_TAG, "Error", e);
        }
    }

    private void requestAudioMode(int stream)
    {
        // Request audio focus for playback
        // Music Interop ++
        int result = m_audioManager.requestAudioFocus(m_audioListener, stream, AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            Log.getLogger().warn(LOG_TAG, "AudioManager.AUDIOFOCUS REQUEST DENIED");
    }

    public void takeCall(boolean withVideo)
    {
        Log.getLogger().info(LOG_TAG, "Take call");

        if (m_currentCall == null)
            return;

        m_currentCall.setInitiatedWithVideo(withVideo);

        sendAccept();
        sendProceed();

        saveCurrentPresence();
        sendCallPresence(withVideo);
    }

    public void rejectCall()
    {
        Log.getLogger().info(LOG_TAG, "Reject call");

        if (m_currentCall == null)
            return;

        sendReject(m_user.getImJabberId());
        sendReject(m_currentCall.getDistant().getImJabberId());
    }

    public void setStreamAddedListener(IStreamManagementListener streamAdded)
    {
        m_streamManagementListener = streamAdded;
    }

    public void hangupCall()
    {
        Log.getLogger().info(LOG_TAG, "Hangup call");

        if (m_currentCall == null)
            return;

        if (m_peerConnection != null)
        {
            JingleIQ iq = JinglePacketFactory.createSessionTerminate(null, m_currentCall.getJid(), m_currentCall.getSid(), Reason.SUCCESS, null);

            try
            {
                m_connection.sendStanza(iq);
            }
            catch (SmackException.NotConnectedException e)
            {
                Log.getLogger().error(LOG_TAG, "Problem while sending session terminate");
            }
        }
        else
            sendRetract();

        terminateCall();
    }

    public MediaStream getAddedStream()
    {
        return m_addedStream;
    }

    @Override
    public void processMessage(Chat chat, Message message)
    {
        ProposePacketExtension proposePacketExtension = message.getExtension(ProposePacketExtension.ELEMENT_NAME, ProposePacketExtension.NAMESPACE);
        AcceptPacketExtension acceptPacketExtension = message.getExtension(AcceptPacketExtension.ELEMENT_NAME, AcceptPacketExtension.NAMESPACE);
        RetractPacketExtension retractPacketExtension = message.getExtension(RetractPacketExtension.ELEMENT_NAME, RetractPacketExtension.NAMESPACE);
        RejectPacketExtension rejectPacketExtension = message.getExtension(RejectPacketExtension.ELEMENT_NAME, RejectPacketExtension.NAMESPACE);
        ProceedPacketExtension proceedPacketExtension = message.getExtension(ProceedPacketExtension.ELEMENT_NAME, ProceedPacketExtension.NAMESPACE);

        if (proposePacketExtension != null)
        {
            handlePropose(message.getFrom(), proposePacketExtension);
        }
        else if (acceptPacketExtension != null)
        {
            handleAccept(message.getFrom());
        }
        else if (retractPacketExtension != null)
        {
            handleRetract(message.getFrom());
        }
        else if (rejectPacketExtension != null)
        {
            handleReject();
        }
        else if (proceedPacketExtension != null)
        {
            handleProceed(message.getFrom());
        }
    }

    private void handleProceed(String from)
    {
        makeCallAfterInitiation(from);
    }

    private void handleReject()
    {
        terminateCall();
    }

    private void handleRetract(String from)
    {
        if (m_currentCall != null && m_currentCall.getJid().equals(from))
            terminateCall();
        else
            Log.getLogger().info(LOG_TAG, "Retract doesn't match current call => ignore it");
    }

    private void handleAccept(String from)
    {
        if ((m_user.getImJabberId() + "/" + m_resource).equals(from))
        {
            Log.getLogger().info(LOG_TAG, "Call has been answered on this device");
            return;
        }

        Log.getLogger().info(LOG_TAG, "Call has been answered else where");

        terminateCall();
    }

    private void handlePropose(String from, ProposePacketExtension proposePacketExtension)
    {
        if (!m_capabilities.isWebRtcAllowed())
        {
            Log.getLogger().info(LOG_TAG, "WebRTC is not allowed !");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (PermissionChecker.checkSelfPermission(m_applicationContext, Manifest.permission.RECORD_AUDIO) != PermissionChecker.PERMISSION_GRANTED)
                return;

            if (PermissionChecker.checkSelfPermission(m_applicationContext, Manifest.permission.CAMERA) != PermissionChecker.PERMISSION_GRANTED)
                return;
        }

        if (!m_gsmPhone.isCallStateIdle())
        {
            Log.getLogger().warn(LOG_TAG, "There's a current GSM call, ignore the WebRTC call on this device");
            return;
        }

        if (m_currentCall == null)
        {
            Log.getLogger().info(LOG_TAG, "Incoming call from " + from);

            boolean video = false;
            boolean sharing = false;

            List<RtpDescriptionPacketExtension> medias = proposePacketExtension.getChildExtensionsOfType(RtpDescriptionPacketExtension.class);

            for (RtpDescriptionPacketExtension media : medias)
            {
                if ("video".equals(media.getMedia()))
                    video = true;
                else if ("sharing".equals(media.getMedia()))
                    sharing = true;
            }

            Contact contact = m_contactCacheMgr.getContactFromJid(from);
            if (contact == null)
            {
                Log.getLogger().verbose(LOG_TAG, "Contact for call doesn't exist - create it");
                DirectoryContact dirContact = new DirectoryContact();
                dirContact.setImJabberId(from);

                contact = m_contactCacheMgr.createContactIfNotExistOrUpdate(dirContact);
            }

            m_currentCall = new WebRTCCall(contact, m_capabilities);
            m_currentCall.setState(MediaState.RINGING_INCOMING);
            m_currentCall.setJid(from);
            m_currentCall.setSid(proposePacketExtension.getId());
            m_currentCall.setInitiatedWithVideo(video);
            m_currentCall.setInitiatedWithShare(sharing);

            requestAudioMode(AudioManager.STREAM_RING);

            fireCallAdded();
        }
        else
            Log.getLogger().warn(LOG_TAG, "An call already exists, ignore propose message");

    }

    private void sendCallPresence(boolean video)
    {
        if (m_savedPresence == null)
            return;

        if (video)
            m_xmppContactMgr.sendPresence(Presence.Type.available, Presence.Mode.dnd, "video");
        else
            m_xmppContactMgr.sendPresence(Presence.Type.available, Presence.Mode.dnd, "audio");
    }

    private void saveCurrentPresence()
    {
        if (!m_user.getPresence().isXA())
            m_savedPresence = m_user.getPresence();
    }

    private void resetPresence()
    {
        if (m_savedPresence != null)
            m_xmppContactMgr.sendPresence(m_savedPresence);

        m_savedPresence = null;
    }

    private void sendProceed()
    {
        ProceedPacketExtension proceedPacketExtension = new ProceedPacketExtension();
        proceedPacketExtension.setId(m_currentCall.getSid());
        Message message = new Message(m_currentCall.getJid());
        message.setFrom(m_user.getImJabberId() + "/" + m_resource);
        message.addExtension(proceedPacketExtension);

        sendStanza(message);
    }

    private void sendReject(String to)
    {
        RejectPacketExtension rejectPacketExtension = new RejectPacketExtension();
        rejectPacketExtension.setId(m_currentCall.getSid());
        Message message = new Message(to);
        message.setFrom(m_user.getImJabberId() + "/" + m_resource);
        message.addExtension(rejectPacketExtension);

        sendStanza(message);
    }

    private void sendRetract()
    {
        RetractPacketExtension retractPacketExtension = new RetractPacketExtension();
        retractPacketExtension.setId(m_currentCall.getSid());
        Message message = new Message(m_currentCall.getDistant().getImJabberId());
        message.setFrom(m_user.getImJabberId() + "/" + m_resource);
        message.addExtension(retractPacketExtension);

        sendStanza(message);
    }

    private void sendAccept()
    {
        AcceptPacketExtension acceptPacketExtension = new AcceptPacketExtension();
        acceptPacketExtension.setId(m_currentCall.getSid());
        Message message = new Message(m_user.getImJabberId());
        message.setFrom(m_user.getImJabberId() + "/" + m_resource);
        message.addExtension(acceptPacketExtension);

        sendStanza(message);
    }

    private void sendPropose(Contact contact, boolean video)
    {
        ProposePacketExtension proposePacketExtension = new ProposePacketExtension();
        RtpDescriptionPacketExtension rtpDescriptionPacketExtension = new RtpDescriptionPacketExtension();
        rtpDescriptionPacketExtension.setMedia("audio");

        proposePacketExtension.setId(m_currentCall.getSid());
        proposePacketExtension.addDescription(rtpDescriptionPacketExtension);

        if (video)
        {
            rtpDescriptionPacketExtension = new RtpDescriptionPacketExtension();
            rtpDescriptionPacketExtension.setMedia("video");

            proposePacketExtension.addDescription(rtpDescriptionPacketExtension);
        }

        Message message = new Message(contact.getImJabberId());
        message.setFrom(m_user.getImJabberId() + "/" + m_resource);
        message.addExtension(proposePacketExtension);

        sendStanza(message);
    }

    private void sendStanza(Stanza message)
    {
        try
        {
            m_connection.setFromMode(XMPPConnection.FromMode.UNCHANGED);
            m_connection.sendStanza(message);
        }
        catch (SmackException.NotConnectedException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to send message : ", e);
        }
        finally
        {
            m_connection.setFromMode(XMPPConnection.FromMode.OMITTED);
        }
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally)
    {
        chat.addMessageListener(this);

        m_chats.add(chat);
    }

    private void startIceDisconnectedTimer()
    {
        stopIceDisconnectedTimer();

        m_iceDisconnectedTimer = new Timer("ICEDisconnectedTimer");
        m_iceDisconnectedTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                if (m_peerConnection == null)
                    return;

                if (m_connection.isConnected())
                    Log.getLogger().info(LOG_TAG, "Wait for TRANSPORT-REPLACE");
                else
                    m_needToSendTransportReplace = true;
            }
        }, Duration.ONE_SECOND_IN_MILLISECONDS);
    }

    private void stopIceDisconnectedTimer()
    {
        if (m_iceDisconnectedTimer != null)
            m_iceDisconnectedTimer.cancel();
    }

    private synchronized void terminateCall()
    {
        stopIceDisconnectedTimer();
        stopStatsTimer();

        m_handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                LoudspeakerHelper.deactivateLoudspeaker(m_applicationContext);
                MuteHelper.unmute();
            }
        });

        if (m_peerConnection != null)
        {
            m_peerConnection.dispose();
            m_peerConnection = null;
        }

        if (m_currentCall != null)
        {
            m_currentCall.release();
            m_currentCall = null;
        }

        fireCallRemoved();

        m_localSdpForIceRestart = null;
        m_localVideoTrack = null;
        m_addedStream = null;
        m_needToSendTransportReplace = false;
        m_nextAction = null;
        m_streamManagementListener = null;
        m_localVideoTrackListener = null;

        try
        {
            if (m_capturer != null)
                m_capturer.stopCapture();
        }
        catch (InterruptedException e)
        {
            Log.getLogger().error(LOG_TAG, "Unable to stop video capture: ", e);
        }
        finally
        {
            if (m_capturer != null)
                m_capturer.dispose();
        }

        if (m_audioSource != null)
        {
            m_audioSource.dispose();
            m_audioSource = null;
        }

        if (m_videoSource != null)
        {
            m_videoSource.dispose();
            m_videoSource = null;
        }

        if (m_peerConnectionFactory != null)
        {
            m_peerConnectionFactory.dispose();
            m_peerConnectionFactory = null;
        }

        m_audioManager.setMode(AudioManager.MODE_NORMAL);

        // Request audio focus for playback
        // Music Interop ++
        int result = m_audioManager.abandonAudioFocus(m_audioListener);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            Log.getLogger().warn(LOG_TAG, "AudioManager.AUDIOFOCUS REQUEST DENIED");

        resetPresence();

        Log.getLogger().info(LOG_TAG, "Call is terminated");
    }

    public void registerTelephonyListener(ITelephonyListener listener)
    {
        synchronized (m_listeners)
        {
            if (!m_listeners.contains(listener))
                m_listeners.add(listener);
        }
    }

    public void unregisterTelephonyListener(ITelephonyListener listener)
    {
        synchronized (m_listeners)
        {
            if (m_listeners.contains(listener))
                m_listeners.remove(listener);
        }
    }

    private void fireCallAdded()
    {
        synchronized (m_listeners)
        {
            for (ITelephonyListener listener : m_listeners.toArray(new ITelephonyListener[m_listeners.size()]))
                listener.onCallAdded(m_currentCall);
        }
    }

    private void fireCallModified()
    {
        synchronized (m_listeners)
        {
            for (ITelephonyListener listener : m_listeners.toArray(new ITelephonyListener[m_listeners.size()]))
                listener.onCallModified(m_currentCall);
        }
    }

    private void fireCallRemoved()
    {
        synchronized (m_listeners)
        {
            for (ITelephonyListener listener : m_listeners.toArray(new ITelephonyListener[m_listeners.size()]))
                listener.onCallRemoved(m_currentCall);
        }
    }

    public WebRTCCall getCurrentCall()
    {
        return m_currentCall;
    }

    public VideoTrack getLocalVideoTrack()
    {
        return m_localVideoTrack;
    }

    public void setLocalVideoTrackListener(ILocalVideoTrackListener localVideoTrackListener)
    {
        m_localVideoTrackListener = localVideoTrackListener;
    }

    public boolean addVideo()
    {
        Log.getLogger().info(LOG_TAG, "Add Video");

        if (m_peerConnection.signalingState() != STABLE || !isAudioConnected())
        {
            Log.getLogger().warn(LOG_TAG, "User should wait for stable connection state");
            return false;
        }

        m_nextAction = JingleAction.CONTENT_ADD;

        m_peerConnection.removeStream(m_mediaCallerStream);

        createMediaStream(true);

        m_capturer.startCapture(640, 480, 30);

        m_peerConnection.createOffer(m_sdpObserver, getSdpConstraints(false));

        sendCallPresence(true);

        LoudspeakerHelper.activateLoudspeaker(m_applicationContext);

        return true;
    }

    public boolean dropVideo()
    {
        Log.getLogger().info(LOG_TAG, "Drop Video");

        if (m_peerConnection.signalingState() != STABLE || !isAudioConnected())
        {
            Log.getLogger().warn(LOG_TAG, "User should wait for stable connection state");
            return false;
        }

        m_nextAction = JingleAction.CONTENT_REMOVE;

        m_peerConnection.removeStream(m_mediaCallerStream);

        createMediaStream(false);

        try
        {
            m_capturer.stopCapture();
        }
        catch (InterruptedException e)
        {
            Log.getLogger().error(LOG_TAG, "Unable to stop video capture: ", e);
        }

        m_peerConnection.createOffer(m_sdpObserver, getSdpConstraints(false));

        sendCallPresence(false);

        return true;
    }

    public void handlePushMessage(String senderJid, String msgId, String callAction, String medias)
    {
        XmppConnection connection = RainbowContext.getInfrastructure().getXmppConnection();

        if (connection == null)
            return;

        if ("propose".equals(callAction))
        {
            m_pushCallSender = senderJid;
            m_pushProposePacketExtension = new ProposePacketExtension();
            m_pushProposePacketExtension.setId(msgId);

            if (StringsUtil.isNullOrEmpty(medias))
            {
                RtpDescriptionPacketExtension rtpDescriptionPacketExtension = new RtpDescriptionPacketExtension();
                rtpDescriptionPacketExtension.setMedia("audio");
                m_pushProposePacketExtension.addDescription(rtpDescriptionPacketExtension);
            }
            else
            {
                for (String media : medias.split(","))
                {
                    RtpDescriptionPacketExtension rtpDescriptionPacketExtension = new RtpDescriptionPacketExtension();
                    rtpDescriptionPacketExtension.setMedia(media);
                    m_pushProposePacketExtension.addDescription(rtpDescriptionPacketExtension);
                }
            }

            connection.forceOpenWebSocket();
        }
        else
        {
            m_pushCallSender = null;
            m_pushProposePacketExtension = null;
        }
    }

    public void mute(boolean mute, boolean distant)
    {
        if (m_mediaCallerStream != null)
        {
            for (AudioTrack track : m_mediaCallerStream.audioTracks)
                track.setEnabled(!mute);
        }

        if (distant && m_addedStream != null)
        {
            for (AudioTrack track : m_addedStream.audioTracks)
                track.setEnabled(!mute);
        }

        m_isMutted = mute;
    }

    public boolean isMuted()
    {
        return m_isMutted;
    }

    public void disconnect()
    {
        if(m_connection != null)
        {
            m_connection.removeConnectionListener(m_connectionListener);
            m_connection.unregisterIQRequestHandler(CallIq.ELEMENT, CallIq.NAMESPACE, IQ.Type.set);
            ProviderManager.removeIQProvider(CallIq.ELEMENT, CallIq.NAMESPACE);

            m_connection.unregisterIQRequestHandler(JingleIQ.ELEMENT_NAME, JingleIQ.NAMESPACE, IQ.Type.set);
            ProviderManager.removeIQProvider(JingleIQ.ELEMENT_NAME, JingleIQ.NAMESPACE);

            ChatManager.getInstanceFor(m_connection).removeChatListener(this);

            for (Chat chat : m_chats)
                chat.removeMessageListener(this);

            m_chats.clear();
        }

        m_handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (m_gsmPhone != null)
                    m_gsmPhone.stopListenTelephonyEvents(m_callStateChangedNotifier);
            }
        });
    }

    public void switchCamera()
    {
        Log.getLogger().info(LOG_TAG, "Switch Camera");

        m_capturer.switchCamera(null);
    }

    public void enableStatsEvents(boolean enable, int periodMs)
    {
        if (enable)
        {

            try
            {
                stopStatsTimer();

                m_statsTimer = new Timer();
                m_statsTimer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        getStats();
                    }
                }, 0, periodMs);
            }
            catch (Exception e)
            {
                Log.getLogger().error(LOG_TAG, "Can not schedule statistics timer", e);
            }
        }
        else
        {
            m_statsTimer.cancel();
        }
    }

    private void stopStatsTimer()
    {
        if (m_statsTimer != null)
            m_statsTimer.cancel();
    }

    private void getStats()
    {
        if (m_peerConnection == null)
        {
            return;
        }

        boolean success = m_peerConnection.getStats(new StatsObserver()
        {
            @Override
            public void onComplete(StatsReport[] reports)
            {
                Statistics.Quality indicator = m_statistics.extractQualityIndicatorFromStats(reports);

                if (m_streamManagementListener != null)
                    m_streamManagementListener.onQualityChanged(indicator);

            }
        }, null);

        if (!success)
        {
            Log.getLogger().error(LOG_TAG, "getStats() returns false!");
        }
    }

    public boolean isAudioConnected()
    {
        if (m_peerConnection != null)
        {
            PeerConnection.IceConnectionState state = m_peerConnection.iceConnectionState();
            return isAudioConnected(state);
        }

        return true;
    }

    private boolean isAudioConnected(PeerConnection.IceConnectionState newState)
    {
        return newState == PeerConnection.IceConnectionState.CONNECTED || newState == PeerConnection.IceConnectionState.COMPLETED || newState == PeerConnection.IceConnectionState.CLOSED;
    }

    public interface IStreamManagementListener
    {
        void onStreamAdded(MediaStream stream);

        void onStreamRemoved(MediaStream stream);

        void onQualityChanged(Statistics.Quality quality);

        void onAudioConnectionChange(boolean connected);
    }

    public interface ILocalVideoTrackListener
    {
        void onLocalVideoTrackCreated(VideoTrack videoTrack);
    }

    // Implementation detail: handle offer creation/signaling and answer setting,
    // as well as adding remote ICE candidates once the answer SDP is set.
    private class SDPObserver implements SdpObserver
    {
        @Override
        public void onCreateSuccess(SessionDescription sdp)
        {
            Log.getLogger().info(LOG_TAG, "SDP create success type: " + sdp.type.canonicalForm() + ", desc:" + sdp.description);

            if (m_peerConnection == null)
                return;

            if (m_nextAction != JingleAction.TRANSPORT_REPLACE)
                m_peerConnection.setLocalDescription(m_sdpObserver, sdp);
            else
                m_localSdpForIceRestart = sdp;

            if (sdp.type == SessionDescription.Type.ANSWER)
            {
                JingleAction action = JingleAction.SESSION_ACCEPT;

                if (m_nextAction != null)
                {
                    action = m_nextAction;
                    m_nextAction = null;
                }

                try
                {
                    sendSessionState(sdp, action, ContentPacketExtension.CreatorEnum.responder);
                }
                catch (SmackException.NotConnectedException e)
                {
                    Log.getLogger().error(LOG_TAG, "Problem while sending " + action);
                }
            }
            else if (sdp.type == SessionDescription.Type.OFFER)
            {
                JingleAction action = JingleAction.SESSION_INITIATE;

                if (m_nextAction != null)
                {
                    action = m_nextAction;
                    m_nextAction = null;
                }

                try
                {
                    sendSessionState(sdp, action, ContentPacketExtension.CreatorEnum.initiator);
                }
                catch (SmackException.NotConnectedException e)
                {
                    Log.getLogger().error(LOG_TAG, "Problem while sending " + action);
                }
            }
        }

        private void sendSessionState(SessionDescription sdp, JingleAction action, ContentPacketExtension.CreatorEnum creator) throws SmackException.NotConnectedException
        {
            JingleIQ sessionAccept = JingleUtils.toJingle(sdp, action, creator);
            sessionAccept.setTo(m_currentCall.getJid());
            sessionAccept.setSID(m_currentCall.getSid());
            sessionAccept.setType(IQ.Type.set);

            if (action == JingleAction.CONTENT_ADD || action == JingleAction.CONTENT_MODIFY)
            {
                if (m_localVideoTrack != null)
                    sessionAccept.setMediaType("video");
            }
            else
            {
                if (m_localVideoTrack != null)
                    sessionAccept.setLocalType("audio+video");
                else
                    sessionAccept.setLocalType("audio");
            }

            m_connection.sendStanza(sessionAccept);
        }

        @Override
        public void onSetSuccess()
        {
            Log.getLogger().info(LOG_TAG, "On SDP SET SUCCESS");
        }


        @Override
        public void onCreateFailure(final String error)
        {
            Log.getLogger().error(LOG_TAG, "createSDP error: " + error);
            terminateCall();
        }

        @Override
        public void onSetFailure(final String error)
        {
            Log.getLogger().error(LOG_TAG, "setSDP error: " + error);
            terminateCall();
        }
    }

    // Implementation detail: observe ICE & stream changes and react accordingly.
    private class PCObserver implements PeerConnection.Observer
    {
        @Override
        public void onIceCandidate(final IceCandidate candidate)
        {
            Log.getLogger().info(LOG_TAG, "ICE CANDIDATE!!! " + candidate);
            sendTransportInfo(candidate);
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates)
        {
            for (IceCandidate candidate : iceCandidates)
                Log.getLogger().info(LOG_TAG, "ICE CANDIDATE REMOVED!!! " + candidate);
        }

        private void sendTransportInfo(IceCandidate candidate)
        {
            JingleIQ iq;

            if (m_currentCall.isOutgoing())
                iq = JingleUtils.createTransportInfo(m_currentCall.getJid(), candidate, ContentPacketExtension.CreatorEnum.initiator);
            else
                iq = JingleUtils.createTransportInfo(m_currentCall.getJid(), candidate, ContentPacketExtension.CreatorEnum.responder);

            if (iq != null)
            {
                iq.setSID(m_currentCall.getSid());
                try
                {
                    m_connection.sendStanza(iq);
                }
                catch (SmackException.NotConnectedException e)
                {
                    Log.getLogger().error(LOG_TAG, "Problem while sending transport info");
                }

                Log.getLogger().info(LOG_TAG, "sent transport-info: " + iq.toXML());
            }
        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState newState)
        {
            Log.getLogger().info(LOG_TAG, "SIGNALING STATE: " + newState);
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState newState)
        {
            Log.getLogger().info(LOG_TAG, "ICE CONN CHANGE: " + newState);

            if (newState == PeerConnection.IceConnectionState.DISCONNECTED || newState == PeerConnection.IceConnectionState.FAILED)
            {
                startIceDisconnectedTimer();
            }

            if (newState == PeerConnection.IceConnectionState.CONNECTED)
            {
                if (m_currentCall.isOutgoing())
                {
                    m_currentCall.setState(MediaState.ACTIVE);
                    fireCallModified();
                }
            }

            if (m_streamManagementListener != null)
                m_streamManagementListener.onAudioConnectionChange(isAudioConnected(newState));
        }

        @Override
        public void onIceConnectionReceivingChange(boolean receiving)
        {
            Log.getLogger().info(LOG_TAG, "ICE Connection Receiving : " + receiving);
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState newState)
        {
            Log.getLogger().info(LOG_TAG, "ICE Gather CHANGE: " + newState);
        }

        @Override
        public void onAddStream(final MediaStream stream)
        {
            Log.getLogger().info(LOG_TAG, "ADD STREAM: " + stream);

            if (stream.audioTracks.size() == 0 && stream.videoTracks.size() == 1 || (m_addedStream != null && m_addedStream.audioTracks.size() == 0 && m_addedStream.videoTracks.size() == 1))
                m_currentCall.setCanAddVideo(false);
            else
                m_currentCall.setCanAddVideo(true);

            if (m_addedStream != null)
            {
                if (m_addedStream.audioTracks.size() == 0 && m_addedStream.videoTracks.size() == 1 && stream.audioTracks.size() == 1 && stream.videoTracks.size() == 0)
                {
                    stream.videoTracks.add(0, m_addedStream.videoTracks.get(0));
                    m_addedStream.videoTracks.remove(0);
                }

                if (m_addedStream.audioTracks.size() == 1 && m_addedStream.videoTracks.size() == 0 && stream.audioTracks.size() == 0 && stream.videoTracks.size() == 1)
                {
                    stream.audioTracks.add(0, m_addedStream.audioTracks.get(0));
                    m_addedStream.audioTracks.remove(0);
                }
            }

            m_addedStream = stream;

            if (m_streamManagementListener != null)
                m_streamManagementListener.onStreamAdded(stream);
        }

        @Override
        public void onRemoveStream(final MediaStream stream)
        {
            Log.getLogger().info(LOG_TAG, "REMOVE STREAM: " + stream);

            if (stream.videoTracks.size() < 1)
                return;

            if (m_streamManagementListener != null)
                m_streamManagementListener.onStreamRemoved(stream);
        }

        @Override
        public void onDataChannel(final DataChannel dc)
        {
            Log.getLogger().info(LOG_TAG, "Data channel opened: " + dc);
        }

        @Override
        public void onRenegotiationNeeded()
        {
            Log.getLogger().info(LOG_TAG, "onRenegotiationNeeded");
        }
    }
}

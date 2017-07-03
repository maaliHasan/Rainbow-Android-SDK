package com.ale.infra.xmpp;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.manager.ChatMgr;
import com.ale.infra.platformservices.IPeriodicWorker;
import com.ale.infra.platformservices.IPeriodicWorkerManager;
import com.ale.infra.platformservices.ISeamless3GToWifiRoaming;
import com.ale.rainbow.datanetworkmonitor.DataNetworkMonitor;
import com.ale.util.Duration;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.ConnectionException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.SynchronizationPoint;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.compress.packet.Compress;
import org.jivesoftware.smack.compress.packet.Compressed;
import org.jivesoftware.smack.compression.XMPPInputOutputStream;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Element;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PlainStreamElement;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.StartTls;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.sasl.packet.SaslStreamElements;
import org.jivesoftware.smack.sm.SMUtils;
import org.jivesoftware.smack.sm.StreamManagementException;
import org.jivesoftware.smack.sm.packet.StreamManagement;
import org.jivesoftware.smack.sm.predicates.Predicate;
import org.jivesoftware.smack.sm.provider.ParseStreamManagement;
import org.jivesoftware.smack.tcp.BundleAndDeferCallback;
import org.jivesoftware.smack.util.ArrayBlockingQueueWithShutdown;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.jivesoftware.smack.util.StringUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class XMPPWebSocketConnection extends AbstractRainbowXMPPConnection {

    public static final String PING_WORKER_NAME = "PING_SERVER";
    public static final long PING_WORKER_PERIOD = Duration.THREE_MINUTES_IN_MILLISECONDS;
    private static final String LOG_TAG = "XMPPWebSocketConnection";
    private static final int QUEUE_SIZE = 500;
    private static final int LIST_MAX_VALUE = 10000;

    /**
     * The WebSocket which is used for this connection.
     */
    private WebSocket m_client;
    protected String m_sessionID = null;
    private IPeriodicWorkerManager m_periodicMgr;
    private XMPPWebSocketConfiguration m_config;


    private ISeamless3GToWifiRoaming m_roamingListener = new ISeamless3GToWifiRoaming() {
        @Override
        public void onSeamless3GToWifiRoaming() {
            Log.getLogger().info(LOG_TAG, "onSeamless3GToWifiRoaming");

            if (m_client != null) {
                m_client.close();
                m_client = null;
                if( packetWriter != null)
                    packetWriter.shutdown(true);
            }
        }
    };
    private IPeriodicWorker m_pingWorker = new IPeriodicWorker()
    {
        @Override
        public void work()
        {
            Log.getLogger().warn(LOG_TAG, "NO PING during 3 minutes / Ping Failed");
            m_periodicMgr.stopWorker(PING_WORKER_NAME);

            SmackException e = new SmackException("Ping Failed");
            notifyConnectionError(e);
        }
    };
    private boolean m_fakeWebSocketClient = false;
    private WebSocket m_fakeClient;
    private DataNetworkMonitor m_dataNetworkMonitor;

    /////////////////////////
    // Use only for Tests ;
    public void setFakeWebSocket(WebSocket webSocket) {
        m_fakeClient = webSocket;
        m_fakeWebSocketClient = true;
    }
    public SASLAuthentication getSaslAuthentication() {
        return this.saslAuthentication;
    }

    /**
     *
     */
    private boolean disconnectedButResumeable = false;

    /**
     * Flag to indicate if the socket was closed intentionally by Smack.
     * <p>
     * This boolean flag is used concurrently, therefore it is marked volatile.
     * </p>
     */
    private volatile boolean socketClosed = false;

    private boolean usingTLS = false;

    /**
     * Protected access level because of unit test purposes
     */
    protected PacketWriter packetWriter;

    private final SynchronizationPoint<Exception> initialOpenStreamSend = new SynchronizationPoint<>(this);

    private final SynchronizationPoint<XMPPException> maybeCompressFeaturesReceived = new SynchronizationPoint<>(this);

    private final SynchronizationPoint<XMPPException> compressSyncPoint = new SynchronizationPoint<>(this);

    private SynchronizationPoint<SmackException> webSocketConnected = new SynchronizationPoint<>(this);


    private static boolean useSmDefault = false;

    private static boolean useSmResumptionDefault = true;

    /**
     * The stream ID of the stream that is currently resumable, ie. the stream we hold the state
     * for in {@link #clientHandledStanzasCount}, {@link #serverHandledStanzasCount} and
     * {@link #unacknowledgedStanzas}.
     */
    private String smSessionId;

    private final SynchronizationPoint<XMPPException> smResumedSyncPoint = new SynchronizationPoint<XMPPException>(
            this);

    private final SynchronizationPoint<XMPPException> smEnabledSyncPoint = new SynchronizationPoint<XMPPException>(
            this);

    /**
     * The client's preferred maximum resumption time in seconds.
     */
    private int smClientMaxResumptionTime = -1;

    /**
     * The server's preferred maximum resumption time in seconds.
     */
    private int smServerMaxResumptimTime = -1;

    /**
     * Indicates whether Stream Management (XEP-198) should be used if it's supported by the server.
     */
    private boolean useSm = useSmDefault;
    private boolean useSmResumption = useSmResumptionDefault;

    /**
     * The counter that the server sends the client about it's current height. For example, if the server sends
     * {@code <a h='42'/>}, then this will be set to 42 (while also handling the {@link #unacknowledgedStanzas} queue).
     */
    private long serverHandledStanzasCount = 0;

    /**
     * The counter for stanzas handled ("received") by the client.
     * <p>
     * Note that we don't need to synchronize this counter. Although JLS 17.7 states that reads and writes to longs are
     * not atomic, it guarantees that there are at most 2 separate writes, one to each 32-bit half. And since
     * {@link SMUtils#incrementHeight(long)} masks the lower 32 bit, we only operate on one half of the long and
     * therefore have no concurrency problem because the read/write operations on one half are guaranteed to be atomic.
     * </p>
     */
    private long clientHandledStanzasCount = 0;

    private BlockingQueue<Stanza> unacknowledgedStanzas;

    /**
     * Set to true if Stream Management was at least once enabled for this connection.
     */
    private boolean smWasEnabledAtLeastOnce = false;

    /**
     * This listeners are invoked for every stanza that got acknowledged.
     */
    private final Collection<StanzaListener> stanzaAcknowledgedListeners = new ConcurrentLinkedQueue<StanzaListener>();

    /**
     * This listeners are invoked for a acknowledged stanza that has the given stanza ID. They will
     * only be invoked once and automatically removed after that.
     */
    private final Map<String, StanzaListener> stanzaIdAcknowledgedListeners = new ConcurrentHashMap<>();

    /**
     * Predicates that determine if an stream management ack should be requested from the server.
     * <p>
     * We use a linked hash set here, so that the order how the predicates are added matches the
     * order in which they are invoked in order to determine if an ack request should be send or not.
     * </p>
     */
    private final Set<StanzaFilter> requestAckPredicates = new LinkedHashSet<>();


    private Pattern patternBody = Pattern.compile("<body[^>]*>(.*?)</body>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private boolean m_useSMAckListener = true;


    /**
     * Creates a new XMPP connection over TCP (optionally using proxies).
     * <p>
     * Note that XMPPTCPConnection constructors do not establish a connection to the server
     * and you must call {@link #connect()}.
     * </p>
     *
     * @param config the connection configuration.
     */
    public XMPPWebSocketConnection(XMPPWebSocketConfiguration config) {
        super(config);
        m_config = config;
        this.host = config.getHost();
        this.port = config.getPort();

        m_periodicMgr = RainbowContext.getInfrastructure().getPeriodicWorkerManager();

        addConnectionListener(new AbstractConnectionListener() {
            @Override
            public void connectionClosedOnError(Exception e) {
                if (e instanceof XMPPException.StreamErrorException) {
                    dropSmState();
                }
            }
        });
    }

    @Override
    protected void throwNotConnectedExceptionIfAppropriate() throws NotConnectedException {
        if (packetWriter == null) {
            throw new NotConnectedException();
        }
        packetWriter.throwNotConnectedExceptionIfDoneAndResumptionNotPossible();
    }

    @Override
    protected void throwAlreadyConnectedExceptionIfAppropriate() throws SmackException.AlreadyConnectedException {
        if (isConnected() && !disconnectedButResumeable) {
            throw new SmackException.AlreadyConnectedException();
        }
    }

    @Override
    protected void throwAlreadyLoggedInExceptionIfAppropriate() throws SmackException.AlreadyLoggedInException {
        if (isAuthenticated() && !disconnectedButResumeable) {
            throw new SmackException.AlreadyLoggedInException();
        }

    }

    @Override
    protected void afterSuccessfulLogin(final boolean resumed) throws NotConnectedException {
        // Reset the flag in case it was set
        disconnectedButResumeable = false;
        super.afterSuccessfulLogin(resumed);
    }

    @Override
    protected synchronized void loginNonAnonymously(String username, String password, String resource) throws XMPPException, SmackException, IOException {
        Log.getLogger().info(LOG_TAG, ">loginNonAnonymously");

        if (saslAuthentication.hasNonAnonymousAuthentication()) {
            // Authenticate using SASL
            if (password != null) {
                saslAuthentication.authenticate(username, password, resource);
            } else {
                saslAuthentication.authenticate(resource, config.getCallbackHandler());
            }
        } else {
            throw new SmackException("No non-anonymous SASL authentication mechanism available");
        }

        // If compression is enabled then request the server to use stream compression. XEP-170
        // recommends to perform stream compression before resource binding.
        if (config.isCompressionEnabled()) {
            useCompression();
        }

        if (isSmResumptionPossible()) {
            smResumedSyncPoint.sendAndWaitForResponse(new StreamManagement.Resume(clientHandledStanzasCount, smSessionId));
            if (smResumedSyncPoint.wasSuccessful()) {
                // We successfully resumed the stream, be done here
                afterSuccessfulLogin(true);
                return;
            }
            // SM resumption failed, what Smack does here is to report success of
            // lastFeaturesReceived in case of sm resumption was answered with 'failed' so that
            // normal resource binding can be tried.
            Log.getLogger().verbose(LOG_TAG, "Stream resumption failed, continuing with normal stream establishment process");
        }

        List<Stanza> previouslyUnackedStanzas = new LinkedList<>();
        if (unacknowledgedStanzas != null) {
            // There was a previous connection with SM enabled but that was either not resumable or
            // failed to resume. Make sure that we (re-)send the unacknowledged stanzas.
            unacknowledgedStanzas.drainTo(previouslyUnackedStanzas);
            // Reset unacknowledged stanzas to 'null' to signal that we never send 'enable' in this
            // XMPP session (There maybe was an enabled in a previous XMPP session of this
            // connection instance though). This is used in writePackets to decide if stanzas should
            // be added to the unacknowledged stanzas queue, because they have to be added right
            // after the 'enable' stream element has been sent.
            dropSmState();
        }

        // Now bind the resource. It is important to do this *after* we dropped an eventually
        // existing Stream Management state. As otherwise <bind/> and <session/> may end up in
        // unacknowledgedStanzas and become duplicated on reconnect. See SMACK-706.
        bindResourceAndEstablishSession(resource);

        if (isSmAvailable() && useSm) {
            Log.getLogger().verbose(LOG_TAG, "Use StreamManagement");
            // Remove what is maybe left from previously stream managed sessions
            serverHandledStanzasCount = 0;
            // XEP-198 3. Enabling Stream Management. If the server response to 'Enable' is 'Failed'
            // then this is a non recoverable error and we therefore throw an exception.
            smEnabledSyncPoint.sendAndWaitForResponseOrThrow(new StreamManagement.Enable(useSmResumption, smClientMaxResumptionTime));
            synchronized (requestAckPredicates) {
                if (requestAckPredicates.isEmpty()) {
                    // Assure that we have at lest one predicate set up that so that we request acks
                    // for the server and eventually flush some stanzas from the unacknowledged
                    // stanza queue
                    requestAckPredicates.add(Predicate.forMessagesOrAfter5Stanzas());
                }
            }
        }
        // (Re-)send the stanzas *after* we tried to enable SM
        for (Stanza stanza : previouslyUnackedStanzas) {
            sendStanzaInternal(stanza);
        }

        afterSuccessfulLogin(false);
    }

    @Override
    public synchronized void loginAnonymously() throws XMPPException, SmackException, IOException {
        // Wait with SASL auth until the SASL mechanisms have been received
        saslFeatureReceived.checkIfSuccessOrWaitOrThrow();

        if (saslAuthentication.hasAnonymousAuthentication()) {
            saslAuthentication.authenticateAnonymously();
        } else {
            throw new SmackException("No anonymous SASL authentication mechanism available");
        }

        // If compression is enabled then request the server to use stream compression
        if (config.isCompressionEnabled()) {
            useCompression();
        }

        bindResourceAndEstablishSession(null);

        afterSuccessfulLogin(false);
    }

    @Override
    public boolean isSecureConnection() {
        return usingTLS;
    }

    public boolean isSocketClosed() {
        return socketClosed;
    }

    /**
     * Shuts the current connection down. After this method returns, the connection must be ready
     * for re-use by connect.
     */
    @Override
    public void shutdown() {
        if (isSmEnabled()) {
            try {
                // Try to send a last SM Acknowledgement. Most servers won't find this information helpful, as the SM
                // state is dropped after a clean disconnect anyways. OTOH it doesn't hurt much either.
                sendSmAcknowledgementInternal();
            } catch (NotConnectedException e) {
                Log.getLogger().error(LOG_TAG, "Can not send final SM ack as connection is not connected", e);
            }
        }

        if( m_client != null) {
            m_client.setStringCallback(null);
            m_client.setClosedCallback(null);
        }

        shutdown(false);

        if (m_periodicMgr != null)
            m_periodicMgr.stopWorker(PING_WORKER_NAME);

        if (m_dataNetworkMonitor != null)
            m_dataNetworkMonitor.unregisterSeamless3GToWifiRoamingListener(m_roamingListener);

        connectionListeners.clear();
    }

    public void closeWebSocket() {
        if (m_client != null) {
            if (m_client.getSocket() != null)
                m_client.getSocket().end();
            m_client = null;
        }
    }
    /**
     * Performs an unclean disconnect and shutdown of the connection. Does not send a closing stream stanza.
     */
    public synchronized void instantShutdown() {
        shutdown(true);
    }

    private void shutdown(boolean instant) {
        if (disconnectedButResumeable) {
            return;
        }
//        if (packetReader != null) {
//            packetReader.shutdown();
//        }
        if (packetWriter != null) {
            packetWriter.shutdown(instant);
        }

        // Set socketClosed to true. This will cause the PacketReader
        // and PacketWriter to ignore any Exceptions that are thrown
        // because of a read/write from/to a closed stream.
        // It is *important* that this is done before socket.disconnect()!
        socketClosed = true;
        try {
            if( m_client != null) {
                m_client.close();
                m_client = null;
            }
        } catch (Exception e) {
            Log.getLogger().warn(LOG_TAG, "shutdown", e);
        }

        setWasAuthenticated();
        // If we are able to resume the stream, then don't set
        // connected/authenticated/usingTLS to false since we like behave like we are still
        // connected (e.g. sendStanza should not throw a NotConnectedException).
        if (isSmResumptionPossible() && instant) {
            disconnectedButResumeable = true;
        } else {
            disconnectedButResumeable = false;
            // Reset the stream management session id to null, since if the stream is cleanly closed, i.e. sending a closing
            // stream tag, there is no longer a stream to resume.
            smSessionId = null;
        }
        authenticated = false;
        connected = false;
        usingTLS = false;
        reader = null;
        writer = null;

        maybeCompressFeaturesReceived.init();
        compressSyncPoint.init();
        smResumedSyncPoint.init();
        smEnabledSyncPoint.init();
        initialOpenStreamSend.init();
    }

    @Override
    public void send(PlainStreamElement element) throws NotConnectedException {
        if( element.toXML() != null )
            Log.getLogger().verbose(LOG_TAG, ">send; "+element.toXML().toString());
        packetWriter.sendStreamElement(element);
    }

    @Override
    protected void sendStanzaInternal(Stanza packet) throws NotConnectedException {
        packetWriter.sendStreamElement(packet);
        if (isSmEnabled()) {
            for (StanzaFilter requestAckPredicate : requestAckPredicates) {
                if (requestAckPredicate.accept(packet)) {
                    requestSmAcknowledgementInternal();
                    break;
                }
            }
        }
    }

    private void connectUsingConfiguration() throws IOException, SmackException {
        Log.getLogger().verbose(LOG_TAG, ">connectUsingConfiguration");

        m_dataNetworkMonitor = RainbowContext.getInfrastructure().getDataNetworkMonitor();

        if (m_dataNetworkMonitor != null)
            m_dataNetworkMonitor.registerSeamless3GToWifiRoamingListener(m_roamingListener);

        socketClosed = false;
        try {
            // Ensure a clean starting state
            if (m_client != null) {
                m_client.close();
                m_client = null;
            }
            m_sessionID = null;

            if( !m_fakeWebSocketClient ) {
                try {
                    AsyncHttpClient asyncHttpClient = AsyncHttpClient.getDefaultInstance();
                    if (m_config.getCustomSSLContext() != null)
                        asyncHttpClient.getSSLSocketMiddleware().setSSLContext(m_config.getCustomSSLContext());
                    if (m_config.getTrustManager() != null)
                        asyncHttpClient.getSSLSocketMiddleware().setTrustManagers(m_config.getTrustManager());
                    if (m_config.getHostVerifier() != null)
                        asyncHttpClient.getSSLSocketMiddleware().setHostnameVerifier(m_config.getHostVerifier());
                    if (m_config.getEngineConfigurator() != null)
                        asyncHttpClient.getSSLSocketMiddleware().addEngineConfigurator(m_config.getEngineConfigurator());


                    asyncHttpClient.websocket(m_config.getURI().toString(), "xmpp", new WebSocketConnectionListener());
                } catch (URISyntaxException e) {
                    Log.getLogger().error(LOG_TAG, "connectInternal - Exception ; " + e.getMessage());
                }
            } else {
                // Use fakeWebSocket
                m_client = m_fakeClient;
                webSocketConnected.reportSuccess();
            }
        } catch (Exception e) {
            Log.getLogger().error(LOG_TAG, "connectInternal - Exception ; " + e.getMessage());
            throw new ConnectionException(e);
        }
    }

    /**
     * Initializes the connection by creating a stanza(/packet) reader and writer and opening a
     * XMPP stream to the server.
     *
     * @throws XMPPException  if establishing a connection to the server fails.
     * @throws SmackException if the server failes to respond back or if there is anther error.
     * @throws IOException
     */
    private void initConnection() throws IOException, SmackException {
        Log.getLogger().verbose(LOG_TAG, ">initConnection");
        compressionHandler = null;

        if( packetWriter == null ) {
            Log.getLogger().verbose(LOG_TAG, "FirstInitialisation");

            packetWriter = new PacketWriter();

            // Start the packet writer. This will open an XMPP stream to the server
            packetWriter.init();

            // Wait with SASL auth until the SASL mechanisms have been received
            //saslFeatureReceived.checkIfSuccessOrWaitOrThrow();

            // Start the packet reader. The startup() method will block until we
            // get an opening stream packet back from server

            // Notify listeners that a new connection has been established
            for (ConnectionCreationListener listener : getConnectionCreationListeners()) {
                listener.connectionCreated(this);
            }
        } else {
            packetWriter.setWebSocket(m_client);
            packetWriter.init();
        }
        openStream();
    }


    /**
     * Returns the compression handler that can be used for one compression methods offered by the server.
     *
     * @return a instance of XMPPInputOutputStream or null if no suitable instance was found
     */
    private XMPPInputOutputStream maybeGetCompressionHandler() {
        Compress.Feature compression = getFeature(Compress.Feature.ELEMENT, Compress.NAMESPACE);
        if (compression == null) {
            // Server does not support compression
            return null;
        }
        for (XMPPInputOutputStream handler : SmackConfiguration.getCompresionHandlers()) {
            String method = handler.getCompressionMethod();
            if (compression.getMethods().contains(method))
                return handler;
        }
        return null;
    }

    @Override
    public boolean isUsingCompression() {
        return compressionHandler != null && compressSyncPoint.wasSuccessful();
    }

    /**
     * <p>
     * Starts using stream compression that will compress network traffic. Traffic can be
     * reduced up to 90%. Therefore, stream compression is ideal when using a slow speed network
     * connection. However, the server and the client will need to use more CPU time in order to
     * un/compress network data so under high load the server performance might be affected.
     * </p>
     * <p>
     * Stream compression has to have been previously offered by the server. Currently only the
     * zlib method is supported by the client. Stream compression negotiation has to be done
     * before authentication took place.
     * </p>
     *
     * @throws NotConnectedException
     * @throws XMPPException
     * @throws SmackException.NoResponseException
     */
    private void useCompression() throws NotConnectedException, SmackException.NoResponseException, XMPPException {
        maybeCompressFeaturesReceived.checkIfSuccessOrWait();
        // If stream compression was offered by the server and we want to use
        // compression then send compression request to the server
        if ((compressionHandler = maybeGetCompressionHandler()) != null) {
            compressSyncPoint.sendAndWaitForResponseOrThrow(new Compress(compressionHandler.getCompressionMethod()));
        } else {
            Log.getLogger().warn(LOG_TAG, "Could not enable compression because no matching handler/method pair was found");
        }
    }

    /**
     * Establishes a connection to the XMPP server and performs an automatic login
     * only if the previous connection state was logged (authenticated). It basically
     * creates and maintains a socket connection to the server.<p>
     * <p/>
     * Listeners will be preserved from a previous connection if the reconnection
     * occurs after an abrupt termination.
     *
     * @throws XMPPException  if an error occurs while trying to establish the connection.
     * @throws SmackException
     * @throws IOException
     */
    @Override
    protected void connectInternal() throws SmackException, IOException, XMPPException {
        Log.getLogger().verbose(LOG_TAG, ">connectInternal");

        webSocketConnected.init();

        // Establishes the TCP connection to the server and does setup the reader and writer. Throws an exception if
        // there is an error establishing the connection
        connectUsingConfiguration();


        // Wait with SASL auth until the SASL mechanisms have been received
        webSocketConnected.checkIfSuccessOrWaitOrThrow();


        // We connected successfully to the servers TCP port
        socketClosed = false;
        initConnection();

        Log.getLogger().verbose(LOG_TAG, "before saslFeatureReceived");
        // Wait with SASL auth until the SASL mechanisms have been received
        saslFeatureReceived.checkIfSuccessOrWaitOrThrow();
        Log.getLogger().verbose(LOG_TAG, "after saslFeatureReceived");


        // Make note of the fact that we're now connected.
        connected = true;
        callConnectionConnectedListener();

        // Automatically makes the login if the user was previously connected successfully
        // to the server and the connection was terminated abruptly
        if (wasAuthenticated && !streamWasResumed()) {
            login();
            notifyReconnection();
        }
    }

    /**
     * Sends out a notification that there was an error with the connection
     * and closes the connection. Also prints the stack trace of the given exception
     *
     * @param e the exception that causes the connection disconnect event.
     */
    private synchronized void notifyConnectionError(Exception e) {
        // Listeners were already notified of the exception, return right here.
        if ((packetWriter == null || packetWriter.done())) return;

        // Closes the connection temporary. A reconnection is possible
        instantShutdown();

        // Notify connection listeners of the error.
        callConnectionClosedOnErrorListener(e);
    }

    public synchronized void notifyConnectionError() {
        // force disconnected mode
        disconnectedButResumeable = false;

        // Closes the connection temporary. A reconnection is possible
        instantShutdown();

        // Notify connection listeners of the error.
        callConnectionClosedOnErrorListener(null);
    }

    @Override
    protected void afterFeaturesReceived() throws SmackException.SecurityRequiredException, NotConnectedException {
        StartTls startTlsFeature = getFeature(StartTls.ELEMENT, StartTls.NAMESPACE);
        if (startTlsFeature != null) {
            if (startTlsFeature.required() && config.getSecurityMode() == ConnectionConfiguration.SecurityMode.disabled) {
                notifyConnectionError(new SmackException.SecurityRequiredByServerException());
                return;
            }

            if (config.getSecurityMode() != ConnectionConfiguration.SecurityMode.disabled) {
                send(new StartTls());
            }
        }
        // If TLS is required but the server doesn't offer it, disconnect
        // from the server and throw an error. First check if we've already negotiated TLS
        // and are secure, however (features get parsed a second time after TLS is established).
        if (!isSecureConnection() && startTlsFeature == null
                && getConfiguration().getSecurityMode() == ConnectionConfiguration.SecurityMode.required) {
            throw new SmackException.SecurityRequiredByClientException();
        }

        if (getSASLAuthentication().authenticationSuccessful()) {
            // If we have received features after the SASL has been successfully completed, then we
            // have also *maybe* received, as it is an optional feature, the compression feature
            // from the server.
            maybeCompressFeaturesReceived.reportSuccess();
        }
    }

    /**
     * Resets the parser using the latest connection's reader. Reseting the parser is necessary
     * when the plain connection has been secured or when a new opening stream element is going
     * to be sent by the server.
     *
     * @throws SmackException if the parser could not be reset.
     */
    private void openStream() throws SmackException {
        Log.getLogger().verbose(LOG_TAG, ">openStream");

        Log.getLogger().verbose(LOG_TAG, " writePackets: >OpenStream");
        m_client.send("<open xmlns='urn:ietf:params:xml:ns:xmpp-framing' to='" + m_config.getServiceName() + "' version='1.0'/>");
//        packetWriter.sendStreamElement(new Element() {
//            @Override
//            public CharSequence toXML() {
//                return "<open xmlns='urn:ietf:params:xml:ns:xmpp-framing' to='" + m_config.getServiceName() + "' version='1.0'/>";
//            }
//        });
        initialOpenStreamSend.reportSuccess();
    }

    private class PacketWriter {
        static final int QUEUE_SIZE = XMPPWebSocketConnection.QUEUE_SIZE;

        private final ArrayBlockingQueueWithShutdown<Element> queue = new ArrayBlockingQueueWithShutdown<>(QUEUE_SIZE, true);

        /**
         * Needs to be protected for unit testing purposes.
         */
        SynchronizationPoint<SmackException.NoResponseException> shutdownDone = new SynchronizationPoint<>(XMPPWebSocketConnection.this);

        /**
         * If set, the stanza(/packet) writer is shut down
         */
        volatile Long shutdownTimestamp = null;

        private volatile boolean instantShutdown;

        private ExecutorService m_xmppExecutor;

        PacketWriter() {
            m_xmppExecutor = Executors.newFixedThreadPool(1);
        }

        /**
         * Initializes the writer in order to be used. It is called at the first connection and also
         * is invoked if the connection is disconnected by an error.
         */
        void init() {
            shutdownDone.init();
            shutdownTimestamp = null;

            if (unacknowledgedStanzas != null) {
                // It's possible that there are new stanzas in the writer queue that
                // came in while we were disconnected but resumable, drain those into
                // the unacknowledged queue so that they get resent now
                drainWriterQueueToUnacknowledgedStanzas();
            }

            queue.start();
           Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    writePackets();
                }
            }, "Smack Packet Writer (" + getConnectionCounter() + ")");
            thread.start();
        }

        void setWebSocket(WebSocket client) {
            m_client = client;
        }

        private boolean done() {
            return shutdownTimestamp != null;
        }

        void throwNotConnectedExceptionIfDoneAndResumptionNotPossible() throws NotConnectedException {
            if (done() && !isSmResumptionPossible()) {
                // Don't throw a NotConnectedException is there is an resumable stream available
                throw new NotConnectedException();
            }
        }

        /**
         * Sends the specified element to the server.
         *
         * @param element the element to send.
         * @throws NotConnectedException
         */
        void sendStreamElement(Element element) throws NotConnectedException {
            throwNotConnectedExceptionIfDoneAndResumptionNotPossible();

            boolean enqueued = false;
            while (!enqueued) {
                try {
                    //Log.getLogger().info(LOG_TAG, "writePackets; add in Queue; "+element.toXML().toString());
                    queue.put(element);
                    enqueued = true;
                } catch (InterruptedException e) {
                    throwNotConnectedExceptionIfDoneAndResumptionNotPossible();
                    // If the method above did not throw, then the sending thread was interrupted
                    // TODO in a later version of Smack the InterruptedException should be thrown to
                    // allow users to interrupt a sending thread that is currently blocking because
                    // the queue is full.
                    Log.getLogger().warn(LOG_TAG, "Sending thread was interrupted", e);
                }
            }
        }

        /**
         * Shuts down the stanza(/packet) writer. Once this method has been called, no further
         * packets will be written to the server.
         */
        void shutdown(boolean instant) {
            instantShutdown = instant;
            shutdownTimestamp = System.currentTimeMillis();
            queue.shutdown();
            if (m_client != null) {
                m_client.close();
                m_client = null;
            }
            try {
                shutdownDone.checkIfSuccessOrWait();
            } catch (SmackException.NoResponseException e) {
                Log.getLogger().warn(LOG_TAG, "shutdownDone was not marked as successful by the writer thread", e);
            }
        }

        /**
         * Maybe return the next available element from the queue for writing. If the queue is shut down <b>or</b> a
         * spurious interrupt occurs, <code>null</code> is returned. So it is important to check the 'done' condition in
         * that case.
         *
         * @return the next element for writing or null.
         */
        private Element nextStreamElement() {
            Element packet = null;
            try {
                packet = queue.take();
            } catch (InterruptedException e) {
                if (!queue.isShutdown()) {
                    // Users shouldn't try to interrupt the packet writer thread
                    Log.getLogger().warn(LOG_TAG, "Packet writer thread was interrupted. Don't do that. Use disconnect() instead.", e);
                }
            }
            return packet;
        }

        private void writePackets() {
            Log.getLogger().verbose(LOG_TAG, ">writePackets");
            try {
                while (!done()) {
                    if( m_client == null) {
                        return;
                    }
                    final Element element = nextStreamElement();
                    if (element == null) {
                        continue;
                    }

                    Stanza packet = null;
                    if (element instanceof Stanza) {
                        packet = (Stanza) element;
                    } else if (element instanceof StreamManagement.Enable) {
                        // The client needs to add messages to the unacknowledged stanzas queue
                        // right after it sent 'enabled'. Stanza will be added once
                        // unacknowledgedStanzas is not null.
                        unacknowledgedStanzas = new ArrayBlockingQueue<>(QUEUE_SIZE);
                    }
                    // Check if the stream element should be put to the unacknowledgedStanza
                    // queue. Note that we can not do the put() in sendStanzaInternal() and the
                    // packet order is not stable at this point (sendStanzaInternal() can be
                    // called concurrently).
                    if (unacknowledgedStanzas != null && packet != null ) {
                        // If the unacknowledgedStanza queue is nearly full, request an new ack
                        // from the server in order to drain it
                        if (unacknowledgedStanzas.size() == 0.8 * XMPPWebSocketConnection.QUEUE_SIZE) {
                            sendElement(StreamManagement.AckRequest.INSTANCE);
                        }
                        if( m_useSMAckListener) {
                            try {
                                // It is important that we put the stanza in the unacknowledged stanza
                                // queue before we put it on the wire
                                unacknowledgedStanzas.put(packet);
                            } catch (InterruptedException e) {
                                throw new IllegalStateException(e);
                            }
                        }
                    }

                    String bodyStrg = element.toXML().toString();
                    if( !StringsUtil.isNullOrEmpty(bodyStrg) &&
                            !bodyStrg.startsWith("<a ") &&
                            !bodyStrg.startsWith("<r ")) {
                        Log.getLogger().info(LOG_TAG, "writePackets; "+ removeBody(bodyStrg));
                    }
                    sendElement(element);
                }

                if (!instantShutdown) {
                    // Flush out the rest of the queue.
                    try {
                        while (!queue.isEmpty()) {
                            Element packet = queue.remove();
                            Log.getLogger().info(LOG_TAG, "writePackets; "+ removeBody(packet.toXML().toString()));
                            sendElement(packet);
                        }
                    } catch (Exception e) {
                        Log.getLogger().warn(LOG_TAG,
                                "Exception flushing queue during shutdown, ignore and continue",
                                e);
                    }

                    // Close the stream.
                    try {
                        Log.getLogger().info(LOG_TAG, "writePackets; </stream:stream>");
                        sendElement(new Stanza() {
                            @Override
                            public CharSequence toXML() {
                                return "</stream:stream>";
                            }
                        });
                    } catch (Exception e) {
                        Log.getLogger().warn(LOG_TAG, "Exception writing closing stream element", e);
                    }
                    // Delete the queue contents (hopefully nothing is left).
                    queue.clear();
                } else if (instantShutdown && isSmEnabled()) {
                    // This was an instantShutdown and SM is enabled, drain all remaining stanzas
                    // into the unacknowledgedStanzas queue
                    drainWriterQueueToUnacknowledgedStanzas();
                }
            } catch (Exception e) {
                // The exception can be ignored if the the connection is 'done'
                // or if the it was caused because the socket got closed
                if (!(done() || isSocketClosed())) {
                    notifyConnectionError(e);
                } else {
                    Log.getLogger().verbose(LOG_TAG, "Ignoring Exception in writePackets()", e);
                }
            } finally {
                Log.getLogger().verbose(LOG_TAG, "Reporting shutdownDone success in writer thread");
                shutdownDone.reportSuccess();
            }
        }

        private void sendElement(final Element element)
        {
            m_xmppExecutor.submit(new Runnable() {
                @Override
                public void run() {
                        String s = element.toXML().toString();
                        m_client.send(s);
                        if (element instanceof Stanza) {
                            firePacketSendingListeners((Stanza) element);
                        }
                }
            });
        }

        private void drainWriterQueueToUnacknowledgedStanzas() {
            List<Element> elements = new ArrayList<>(queue.size());
            queue.drainTo(elements);

            if(!m_useSMAckListener) {
                Log.getLogger().verbose(LOG_TAG, ">drainWriterQueueToUnacknowledgedStanzas not used; skip");
                return;
            }
            for (Element element : elements) {
                if (element instanceof Stanza) {
                    unacknowledgedStanzas.add((Stanza) element);
                }
            }
        }
    }

    /**
     * Set if Stream Management should be used by default for new connections.
     *
     * @param useSmDefault true to use Stream Management for new connections.
     */
    public void setUseStreamManagementDefault(boolean useSmDefault) {
        this.useSmDefault = useSmDefault;
    }

    /**
     * Set if Stream Management resumption should be used by default for new connections.
     *
     * @param useSmResumptionDefault true to use Stream Management resumption for new connections.
     * @deprecated use {@link #setUseStreamManagementResumptionDefault(boolean)} instead.
     */
    @Deprecated
    public void setUseStreamManagementResumptiodDefault(boolean useSmResumptionDefault) {
        setUseStreamManagementResumptionDefault(useSmResumptionDefault);
    }

    /**
     * Set if Stream Management resumption should be used by default for new connections.
     *
     * @param useSmResumptionDefault true to use Stream Management resumption for new connections.
     */
    public void setUseStreamManagementResumptionDefault(boolean useSmResumptionDefault) {
        if (useSmResumptionDefault) {
            // Also enable SM is resumption is enabled
            setUseStreamManagementDefault(useSmResumptionDefault);
        }
        XMPPWebSocketConnection.useSmResumptionDefault = useSmResumptionDefault;
    }

    /**
     * Set if Stream Management should be used if supported by the server.
     *
     * @param useSm true to use Stream Management.
     */
    public void setUseStreamManagement(boolean useSm) {
        this.useSm = useSm;
    }

    /**
     * Set if Stream Management resumption should be used if supported by the server.
     *
     * @param useSmResumption true to use Stream Management resumption.
     */
    public void setUseStreamManagementResumption(boolean useSmResumption) {
        if (useSmResumption) {
            // Also enable SM is resumption is enabled
            setUseStreamManagement(useSmResumption);
        }
        this.useSmResumption = useSmResumption;
    }

    /**
     * Set the preferred resumption time in seconds.
     *
     * @param resumptionTime the preferred resumption time in seconds
     */
    public void setPreferredResumptionTime(int resumptionTime) {
        smClientMaxResumptionTime = resumptionTime;
    }

    /**
     * Add a predicate for Stream Management acknowledgment requests.
     * <p>
     * Those predicates are used to determine when a Stream Management acknowledgement request is send to the server.
     * Some pre-defined predicates are found in the <code>org.jivesoftware.smack.sm.predicates</code> package.
     * </p>
     * <p>
     * If not predicate is configured, the {@link Predicate#forMessagesOrAfter5Stanzas()} will be used.
     * </p>
     *
     * @param predicate the predicate to add.
     * @return if the predicate was not already active.
     */
    public boolean addRequestAckPredicate(StanzaFilter predicate) {
        synchronized (requestAckPredicates) {
            return requestAckPredicates.add(predicate);
        }
    }

    /**
     * Remove the given predicate for Stream Management acknowledgment request.
     *
     * @param predicate the predicate to remove.
     * @return true if the predicate was removed.
     */
    public boolean removeRequestAckPredicate(StanzaFilter predicate) {
        synchronized (requestAckPredicates) {
            return requestAckPredicates.remove(predicate);
        }
    }

    /**
     * Remove all predicates for Stream Management acknowledgment requests.
     */
    public void removeAllRequestAckPredicates() {
        synchronized (requestAckPredicates) {
            requestAckPredicates.clear();
        }
    }

    /**
     * Send an unconditional Stream Management acknowledgement request to the server.
     *
     * @throws StreamManagementException.StreamManagementNotEnabledException if Stream Mangement is not enabled.
     * @throws NotConnectedException                                         if the connection is not connected.
     */
    public void requestSmAcknowledgement() throws StreamManagementException.StreamManagementNotEnabledException, NotConnectedException {
        if (!isSmEnabled()) {
            throw new StreamManagementException.StreamManagementNotEnabledException();
        }
        requestSmAcknowledgementInternal();
    }

    private void requestSmAcknowledgementInternal() throws NotConnectedException {
        packetWriter.sendStreamElement(StreamManagement.AckRequest.INSTANCE);
    }

    /**
     * Send a unconditional Stream Management acknowledgment to the server.
     * <p>
     * See <a href="http://xmpp.org/extensions/xep-0198.html#acking">XEP-198: Stream Management § 4. Acks</a>:
     * "Either party MAY send an <a/> element at any time (e.g., after it has received a certain number of stanzas,
     * or after a certain period of time), even if it has not received an <r/> element from the other party."
     * </p>
     *
     * @throws StreamManagementException.StreamManagementNotEnabledException if Stream Management is not enabled.
     * @throws NotConnectedException                                         if the connection is not connected.
     */
    public void sendSmAcknowledgement() throws StreamManagementException.StreamManagementNotEnabledException, NotConnectedException {
        if (!isSmEnabled()) {
            throw new StreamManagementException.StreamManagementNotEnabledException();
        }
        sendSmAcknowledgementInternal();
    }

    private void sendSmAcknowledgementInternal() throws NotConnectedException {
        packetWriter.sendStreamElement(new StreamManagement.AckAnswer(clientHandledStanzasCount));
    }

    /**
     * Add a Stanza acknowledged listener.
     * <p>
     * Those listeners will be invoked every time a Stanza has been acknowledged by the server. The will not get
     * automatically removed. Consider using {@link #addStanzaIdAcknowledgedListener(String, StanzaListener)} when
     * possible.
     * </p>
     *
     * @param listener the listener to add.
     */
    public void addStanzaAcknowledgedListener(StanzaListener listener) {
        stanzaAcknowledgedListeners.add(listener);
    }

    /**
     * Remove the given Stanza acknowledged listener.
     *
     * @param listener the listener.
     * @return true if the listener was removed.
     */
    public boolean removeStanzaAcknowledgedListener(StanzaListener listener) {
        return stanzaAcknowledgedListeners.remove(listener);
    }

    /**
     * Remove all stanza acknowledged listeners.
     */
    public void removeAllStanzaAcknowledgedListeners() {
        stanzaAcknowledgedListeners.clear();
    }


    public void setUseStreamManagementAckListener(boolean useSMAckListener) {
        m_useSMAckListener = useSMAckListener;
    }

    /**
     * Add a new Stanza ID acknowledged listener for the given ID.
     * <p>
     * The listener will be invoked if the stanza with the given ID was acknowledged by the server. It will
     * automatically be removed after the listener was run.
     * </p>
     *
     * @param id       the stanza ID.
     * @param listener the listener to invoke.
     * @return the previous listener for this stanza ID or null.
     * @throws StreamManagementException.StreamManagementNotEnabledException if Stream Management is not enabled.
     */
    public StanzaListener addStanzaIdAcknowledgedListener(final String id, StanzaListener listener) throws StreamManagementException.StreamManagementNotEnabledException {
        if( m_useSMAckListener == false){
            return null;
        }

        // Prevent users from adding callbacks that will never get removed
        if (!smWasEnabledAtLeastOnce) {
            throw new StreamManagementException.StreamManagementNotEnabledException();
        }
        // Remove the listener after max. 12 hours
        final int removeAfterSeconds = Math.min(getMaxSmResumptionTime(), 12 * 60 * 60);
        schedule(new Runnable() {
            @Override
            public void run() {
                stanzaIdAcknowledgedListeners.remove(id);
            }
        }, removeAfterSeconds, TimeUnit.SECONDS);
        return stanzaIdAcknowledgedListeners.put(id, listener);
    }

    /**
     * Remove the Stanza ID acknowledged listener for the given ID.
     *
     * @param id the stanza ID.
     * @return true if the listener was found and removed, false otherwise.
     */
    public StanzaListener removeStanzaIdAcknowledgedListener(String id) {
        if( m_useSMAckListener == false){
            return null;
        }

        return stanzaIdAcknowledgedListeners.remove(id);
    }

    /**
     * Removes all Stanza ID acknowledged listeners.
     */
    public void removeAllStanzaIdAcknowledgedListeners() {
        if( m_useSMAckListener == false){
            return;
        }

        stanzaIdAcknowledgedListeners.clear();
    }

    /**
     * Returns true if Stream Management is supported by the server.
     *
     * @return true if Stream Management is supported by the server.
     */
    public boolean isSmAvailable() {
        return hasFeature(StreamManagement.StreamManagementFeature.ELEMENT, StreamManagement.NAMESPACE);
    }

    /**
     * Returns true if Stream Management was successfully negotiated with the server.
     *
     * @return true if Stream Management was negotiated.
     */
    public boolean isSmEnabled() {
        return smEnabledSyncPoint.wasSuccessful();
    }

    /**
     * Returns true if the stream was successfully resumed with help of Stream Management.
     *
     * @return true if the stream was resumed.
     */
    public boolean streamWasResumed() {
        return smResumedSyncPoint.wasSuccessful();
    }

    /**
     * Returns true if the connection is disconnected by a Stream resumption via Stream Management is possible.
     *
     * @return true if disconnected but resumption possible.
     */
    public boolean isDisconnectedButSmResumptionPossible() {
        return disconnectedButResumeable && isSmResumptionPossible();
    }


    /**
     * Returns true if the stream is resumable since milliseconds.
     *
     * @return true if the stream is resumable.
     */
    public boolean isSmResumptionPossible(long milliseconds) {
        // There is no resumable stream available
        if (smSessionId == null)
            return false;

        final Long shutdownTimestamp = packetWriter.shutdownTimestamp;
        // Seems like we are already reconnected, report true
        if (shutdownTimestamp == null) {
            return true;
        }

        // See if resumption time is over
        long current = System.currentTimeMillis();
        long maxResumptionMillies = milliseconds;
        if (current > (shutdownTimestamp + maxResumptionMillies)) {
            // Stream resumption is *not* possible if the current timestamp is greater then the greatest timestamp where
            // resumption is possible
            return false;
        } else {
            return true;
        }
    }


    /**
     * Returns true if the stream is resumable.
     *
     * @return true if the stream is resumable.
     */
    public boolean isSmResumptionPossible() {
        // There is no resumable stream available
        if (smSessionId == null)
            return false;

        final Long shutdownTimestamp = packetWriter.shutdownTimestamp;
        // Seems like we are already reconnected, report true
        if (shutdownTimestamp == null) {
            return true;
        }

        // See if resumption time is over
        long current = System.currentTimeMillis();
        long maxResumptionMillies = ((long) getMaxSmResumptionTime()) * 1000;
        if (current > shutdownTimestamp + maxResumptionMillies) {
            // Stream resumption is *not* possible if the current timestamp is greater then the greatest timestamp where
            // resumption is possible
            return false;
        } else {
            return true;
        }
    }

    /**
     * Drop the stream management state. Sets {@link #smSessionId} and
     * {@link #unacknowledgedStanzas} to <code>null</code>.
     */
    private void dropSmState() {
        // clientHandledCount and serverHandledCount will be reset on <enable/> and <enabled/>
        // respective. No need to reset them here.
        smSessionId = null;
        unacknowledgedStanzas = null;
    }

    /**
     * Get the maximum resumption time in seconds after which a managed stream can be resumed.
     * <p>
     * This method will return {@link Integer#MAX_VALUE} if neither the client nor the server specify a maximum
     * resumption time. Be aware of integer overflows when using this value, e.g. do not add arbitrary values to it
     * without checking for overflows before.
     * </p>
     *
     * @return the maximum resumption time in seconds or {@link Integer#MAX_VALUE} if none set.
     */
    public int getMaxSmResumptionTime() {
        int clientResumptionTime = smClientMaxResumptionTime > 0 ? smClientMaxResumptionTime : Integer.MAX_VALUE;
        int serverResumptionTime = smServerMaxResumptimTime > 0 ? smServerMaxResumptimTime : Integer.MAX_VALUE;
        return Math.min(clientResumptionTime, serverResumptionTime);
    }

    private void processHandledCount(long handledCount) throws StreamManagementException.StreamManagementCounterError {
        if( m_useSMAckListener == false){
            Log.getLogger().verbose(LOG_TAG, ">processHandledCount not used; skipped");
            return;
        }

        long ackedStanzasCount = Math.abs(handledCount-serverHandledStanzasCount);
        final List<Stanza> ackedStanzas = new ArrayList<>();

//        Log.getLogger().error(LOG_TAG, "processHandledCount "+handledCount+"/"+serverHandledStanzasCount+"/"+unacknowledgedStanzas.size());

        if( ackedStanzasCount > unacknowledgedStanzas.size()) {
            Log.getLogger().warn(LOG_TAG, "We should not have NULL ackedStanza; "+handledCount+"/"+serverHandledStanzasCount+"/"+unacknowledgedStanzas.size());
        }
        for (long i = 0; (i < ackedStanzasCount) && (i < LIST_MAX_VALUE); i++) {
            Stanza ackedStanza = unacknowledgedStanzas.poll();
            // If the server ack'ed a stanza, then it must be in the
            // unacknowledged stanza queue. There can be no exception.
            if (ackedStanza != null) {
                // Old code that stopped Connection here
//                throw new StreamManagementException.StreamManagementCounterError(handledCount, serverHandledStanzasCount,
//                        ackedStanzasCount, ackedStanzas);
                ackedStanzas.add(ackedStanza);
                //Log.getLogger().error(LOG_TAG, "ackedStanzas added; "+handledCount+"/"+serverHandledStanzasCount);
            } else {
                Log.getLogger().warn(LOG_TAG, "We should not have NULL ackedStanza; "+handledCount+"/"+serverHandledStanzasCount);
            }
        }

        boolean atLeastOneStanzaAcknowledgedListener = false;
        if (!stanzaAcknowledgedListeners.isEmpty()) {
            // If stanzaAcknowledgedListeners is not empty, the we have at least one
            atLeastOneStanzaAcknowledgedListener = true;
        } else {
            // Otherwise we look for a matching id in the stanza *id* acknowledged listeners
            for (Stanza ackedStanza : ackedStanzas) {
                String id = ackedStanza.getStanzaId();
                if (id != null && stanzaIdAcknowledgedListeners.containsKey(id)) {
                    atLeastOneStanzaAcknowledgedListener = true;
                    break;
                }
            }
        }

        // Only spawn a new thread if there is a chance that some listener is invoked
        if (atLeastOneStanzaAcknowledgedListener) {
            asyncGo(new Runnable() {
                @Override
                public void run() {
                    for (Stanza ackedStanza : ackedStanzas) {
                        for (StanzaListener listener : stanzaAcknowledgedListeners) {
                            try {
                                listener.processPacket(ackedStanza);
                            } catch (NotConnectedException e) {
                                Log.getLogger().verbose(LOG_TAG, "Received not connected exception", e);
                            }
                        }
                        String id = ackedStanza.getStanzaId();
                        if (StringUtils.isNullOrEmpty(id)) {
                            continue;
                        }
                        StanzaListener listener = stanzaIdAcknowledgedListeners.remove(id);
                        if (listener != null) {
                            try {
                                listener.processPacket(ackedStanza);
                            } catch (NotConnectedException e) {
                                Log.getLogger().verbose(LOG_TAG, "Received not connected exception", e);
                            }
                        }
                    }
                }
            });
        }

        serverHandledStanzasCount = handledCount;
    }

    /**
     * Set the default bundle and defer callback used for new connections.
     *
     * @param defaultBundleAndDeferCallback
     * @see BundleAndDeferCallback
     * @since 4.1
     */
    public static void setDefaultBundleAndDeferCallback(BundleAndDeferCallback defaultBundleAndDeferCallback) {
    }

    /**
     * Set the bundle and defer callback used for this connection.
     * <p>
     * You can use <code>null</code> as argument to reset the callback. Outgoing stanzas will then
     * no longer get deferred.
     * </p>
     *
     * @param bundleAndDeferCallback the callback or <code>null</code>.
     * @see BundleAndDeferCallback
     * @since 4.1
     */
    public void setBundleandDeferCallback(BundleAndDeferCallback bundleAndDeferCallback) {
    }

    private class WebSocketConnectionListener implements AsyncHttpClient.WebSocketConnectCallback {
        @Override
        public void onCompleted(final Exception ex, final WebSocket webSocket) {
            Thread webSocketThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (ex == null && webSocket != null) {
                            Log.getLogger().info(LOG_TAG, ">>>onCompleted");

                            m_client = webSocket;
                            m_client.setStringCallback(new WebSocketStringCallback());
                            m_client.setClosedCallback(new CompletedCallback() {
                                @Override
                                public void onCompleted(Exception ex) {
                                    if (!(socketClosed || !connected)) {
                                        Log.getLogger().error(LOG_TAG, "WebSocket is closed !");
                                        // Closes the connection temporary. A reconnection is possible
                                        instantShutdown();

                                        // Notify connection listeners of the error.
                                        callConnectionClosedOnErrorListener(ex);
                                    }
                                }
                            });

                            webSocketConnected.reportSuccess();
                        } else {
                            connected = false;
                            notifyConnectionError(ex);
                            webSocketConnected.reportFailure(new SmackException(ex));
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        synchronized (XMPPWebSocketConnection.this) {
                            XMPPWebSocketConnection.this.notifyAll();
                        }
                    }
                }
            },"webSocketThread");
            webSocketThread.start();
        }
    }


    private class WebSocketStringCallback implements WebSocket.StringCallback {
        @Override
        public void onStringAvailable(String strResponse) {
            try {
                parsePackets(strResponse);
            } catch (Exception e) {
                if (isConnected()) {
                    notifyConnectionError(e);
                }
            }
        }
    }

    private String removeBody(String content){
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            return content;
        else
            return patternBody.matcher(content).replaceAll("<body>XXXX</body>");
    }

    public void parsePackets(String strResponse) throws Exception {
        if( !StringsUtil.isNullOrEmpty(strResponse) &&
                !strResponse.startsWith("<a ") &&
                !strResponse.startsWith("<r ")) {
            Log.getLogger().info(LOG_TAG, ">parsePackets; " + removeBody(strResponse));
        }

        m_periodicMgr.stopWorker(PING_WORKER_NAME);
        m_periodicMgr.startWorker(m_pingWorker, PING_WORKER_PERIOD, PING_WORKER_NAME);


        initialOpenStreamSend.checkIfSuccessOrWait();


//        strResponse = strResponse.replaceAll("type=\'webrtc-ringing\'", "");
//        strResponse = strResponse.replaceAll("type=\'webrtc-end\'", "");
        if( strResponse.contains("type=\'webrtc-")) {
            Log.getLogger().info(LOG_TAG, "WebRtc Message received - Skip it / to manage later");
            return;
        }
        if(  strResponse.contains("type=\'file'")) {
            strResponse = strResponse.replace("type=\'file'","type=\'chat'");
            strResponse = strResponse.replace("<body>","<body>"+ ChatMgr.BODY_FILETRANSFER_TAG);
        }

        strResponse = strResponse.replaceAll("type=\'management\'", "");
        strResponse = strResponse.replaceAll("type=\'webrtc\'", "type=\'chat\'");

        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        parser.setInput(new ByteArrayInputStream(strResponse.getBytes()), null);

        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if( eventType == XmlPullParser.END_TAG) {
                eventType = parser.next();
                continue;
            }

            final String name = parser.getName();
            if (name != null) {
                switch (name) {
                    case Message.ELEMENT:
                    case IQ.IQ_ELEMENT:
                    case Presence.ELEMENT:
                        try {
                            parseAndProcessStanza(parser);
                            // To perform GREAT TRAFFIC TEST ;
                            //packetWriter.sendStreamElement(new Presence(Presence.Type.available));
                        } finally {
                            clientHandledStanzasCount = SMUtils.incrementHeight(clientHandledStanzasCount);
                        }
                        break;
                    case "stream":
                        // We found an opening stream.
                        if ("jabber:client".equals(parser.getNamespace(null))) {
                            streamId = parser.getAttributeValue("", "id");
                            String reportedServiceName = parser.getAttributeValue("", "from");
                            assert (reportedServiceName.equals(m_config.getServiceName()));
                        }
                        break;
                    case "error":
                        Log.getLogger().error(LOG_TAG, ">parsePackets; Error ELEMENT" );
                        throw new XMPPException.StreamErrorException(PacketParserUtils.parseStreamError(parser));
                    case "features":
                        parseFeatures(parser);
                        break;
                    case "proceed":
                        try {
                            // Secure the connection by negotiating TLS
                            //proceedTLSReceived();
                            // Send a new opening stream to the server
                            openStream();
                        } catch (Exception e) {
                            // We report any failure regarding TLS in the second stage of XMPP
                            // connection establishment, namely the SASL authentication
                            saslFeatureReceived.reportFailure(new SmackException(e));
                            throw e;
                        }
                        break;
                    case "failure":
                        Log.getLogger().error(LOG_TAG, ">parsePackets; failure ELEMENT" );
                        String namespace = parser.getNamespace(null);
                        switch (namespace) {
                            case "urn:ietf:params:xml:ns:xmpp-tls":
                                // TLS negotiation has failed. The server will disconnect the connection
                                // TODO Parse failure stanza
                                throw new XMPPException.XMPPErrorException("TLS negotiation has failed", null);
                            case "http://jabber.org/protocol/compress":
                                // Stream compression has been denied. This is a recoverable
                                // situation. It is still possible to authenticate and
                                // use the connection but using an uncompressed connection
                                // TODO Parse failure stanza
                                compressSyncPoint.reportFailure(new XMPPException.XMPPErrorException(
                                        "Could not establish compression", null));
                                break;
                            case SaslStreamElements.NAMESPACE:
                                // SASL authentication has failed. The server may disconnect the connection
                                // depending on the number of retries
                                final SaslStreamElements.SASLFailure failure = PacketParserUtils.parseSASLFailure(parser);
                                getSASLAuthentication().authenticationFailed(failure);
                                break;
                        }
                        break;
                    case SaslStreamElements.Challenge.ELEMENT:
                        // The server is challenging the SASL authentication made by the client
                        String challengeData = parser.nextText();
                        getSASLAuthentication().challengeReceived(challengeData);
                        break;
                    case SaslStreamElements.Success.ELEMENT:
                        SaslStreamElements.Success success = new SaslStreamElements.Success(parser.nextText());
                        // We now need to bind a resource for the connection
                        // Open a new stream and wait for the response
                        openStream();
                        // The SASL authentication with the server was successful. The next step
                        // will be to bind the resource
                        getSASLAuthentication().authenticated(success);
                        break;
                    case Compressed.ELEMENT:
                        // Server confirmed that it's possible to use stream compression. Start
                        // stream compression
                        // Send a new opening stream to the server
                        openStream();
                        // Notify that compression is being used
                        compressSyncPoint.reportSuccess();
                        break;
                    case StreamManagement.Enabled.ELEMENT:
                        StreamManagement.Enabled enabled = ParseStreamManagement.enabled(parser);
                        if (enabled.isResumeSet()) {
                            smSessionId = enabled.getId();
                            if (StringUtils.isNullOrEmpty(smSessionId)) {
                                XMPPException.XMPPErrorException xmppException = new XMPPException.XMPPErrorException(
                                        "Stream Management 'enabled' element with resume attribute but without session id received",
                                        new XMPPError(
                                                XMPPError.Condition.bad_request));
                                smEnabledSyncPoint.reportFailure(xmppException);
                                throw xmppException;
                            }
                            smServerMaxResumptimTime = enabled.getMaxResumptionTime();
                        } else {
                            // Mark this a non-resumable stream by setting smSessionId to null
                            smSessionId = null;
                        }
                        clientHandledStanzasCount = 0;
                        smWasEnabledAtLeastOnce = true;
                        smEnabledSyncPoint.reportSuccess();
                        //Log.getLogger().verbose(LOG_TAG, "Stream Management (XEP-198): succesfully enabled");
                        break;
                    case StreamManagement.Failed.ELEMENT:
                        Log.getLogger().warn(LOG_TAG, ">parsePackets; SM Failed ELEMENT" );
                        StreamManagement.Failed failed = ParseStreamManagement.failed(parser);
                        XMPPError xmppError = new XMPPError(failed.getXMPPErrorCondition());
                        XMPPException xmppException = new XMPPException.XMPPErrorException("Stream Management failed", xmppError);
                        // If only XEP-198 would specify different failure elements for the SM
                        // enable and SM resume failure case. But this is not the case, so we
                        // need to determine if this is a 'Failed' response for either 'Enable'
                        // or 'Resume'.
                        if (smResumedSyncPoint.requestSent()) {
                            smResumedSyncPoint.reportFailure(xmppException);
                        } else {
                            if (!smEnabledSyncPoint.requestSent()) {
                                throw new IllegalStateException("Failed element received but SM was not previously enabled");
                            }
                            smEnabledSyncPoint.reportFailure(xmppException);
                            // Report success for last lastFeaturesReceived so that in case a
                            // failed resumption, we can continue with normal resource binding.
                            // See text of XEP-198 5. below Example 11.
                            lastFeaturesReceived.reportSuccess();
                        }
                        break;
                    case StreamManagement.Resumed.ELEMENT:
                        StreamManagement.Resumed resumed = ParseStreamManagement.resumed(parser);
                        if (!smSessionId.equals(resumed.getPrevId())) {
                            throw new StreamManagementException.StreamIdDoesNotMatchException(smSessionId, resumed.getPrevId());
                        }
                        // Mark SM as enabled and resumption as successful.
                        smResumedSyncPoint.reportSuccess();
                        smEnabledSyncPoint.reportSuccess();
                        // First, drop the stanzas already handled by the server
                        processHandledCount(resumed.getHandledCount());
                        // Then re-send what is left in the unacknowledged queue
                        List<Stanza> stanzasToResend = new ArrayList<>(unacknowledgedStanzas.size());
                        unacknowledgedStanzas.drainTo(stanzasToResend);
                        for (Stanza stanza : stanzasToResend) {
                            sendStanzaInternal(stanza);
                        }
                        // If there where stanzas resent, then request a SM ack for them.
                        // Writer's sendStreamElement() won't do it automatically based on
                        // predicates.
                        if (!stanzasToResend.isEmpty()) {
                            requestSmAcknowledgementInternal();
                        }
                        break;
                    case StreamManagement.AckAnswer.ELEMENT:
                        StreamManagement.AckAnswer ackAnswer = ParseStreamManagement.ackAnswer(parser);
                        processHandledCount(ackAnswer.getHandledCount());
                        break;
                    case StreamManagement.AckRequest.ELEMENT:
                        ParseStreamManagement.ackRequest(parser);
                        if (smEnabledSyncPoint.wasSuccessful()) {
                            sendSmAcknowledgementInternal();
                        } else {
                            Log.getLogger().warn(LOG_TAG, "SM Ack Request received while SM is not enabled");
                        }
                        break;
                    case "open":
                        Log.getLogger().debug(LOG_TAG, ">parsePackets; open ELEMENT" );
                        // Nothing to do - just prevent warning Log
                        break;
                    case "disconnect":
                        Log.getLogger().warn(LOG_TAG, ">parsePackets; disconnect ELEMENT" );
                        notifyConnectionError(null);
                        break;
                    default:
                        Log.getLogger().warn(LOG_TAG, "Unknown top level stream element: " + name);
                        break;
                }
            }
            eventType = parser.next();
        }
    }
}
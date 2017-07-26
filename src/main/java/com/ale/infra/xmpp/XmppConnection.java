package com.ale.infra.xmpp;

import android.content.Context;
import android.content.Intent;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.application.RainbowIntent;
import com.ale.infra.capabilities.ICapabilities;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.manager.CallLogMgr;
import com.ale.infra.manager.ChatMgr;
import com.ale.infra.manager.INotificationFactory;
import com.ale.infra.manager.MultiUserChatMgr;
import com.ale.infra.manager.TelephonyMgr;
import com.ale.infra.manager.XmppContactMgr;
import com.ale.infra.manager.fileserver.IFileMgr;
import com.ale.infra.manager.pgiconference.IPgiConferenceMgr;
import com.ale.infra.manager.room.IRoomMgr;
import com.ale.infra.platformservices.IDataNetworkChangedListener;
import com.ale.infra.platformservices.IDataNetworkMonitor;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.admin.SettingsProxy;
import com.ale.infra.proxy.conversation.IConversationProxy;
import com.ale.infra.proxy.directory.IDirectoryProxy;
import com.ale.infra.proxy.users.IUserProxy;
import com.ale.security.util.SSLUtil;
import com.ale.util.Duration;
import com.ale.util.Util;
import com.ale.util.log.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.koushikdutta.async.http.WebSocket;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.carbons.CarbonManager;
import org.jivesoftware.smackx.csi.ClientStateIndicationManager;
import org.jivesoftware.smackx.ping.PingManager;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLContext;

public class XmppConnection implements ConnectionListener, IDataNetworkChangedListener
{
    private static final String LOG_TAG = "XmppConnection";

    private static ConnectionState m_connectionState = ConnectionState.DISCONNECTED;
    private final Context m_applicationContext;
    private final IDataNetworkMonitor m_dataNetworkMonitor;
    private final XmppPushMgr m_pushMgr;
    private final IPlatformServices m_platformServices;
    private final CallLogMgr m_callLogMgr;
    private AbstractRainbowXMPPConnection m_connection;
    private ReconnectionManager m_reconnectionManager;
    private String m_password;
    private String m_username;
    private XmppContactMgr m_xmppContactMgr;
    private ChatMgr m_chatMgr;
    private MultiUserChatMgr m_multiChatMgr;
    private boolean m_clientStateIndicationState = false;
    private TelephonyMgr m_telephonyMgr;
    private boolean m_connectionForcedClose;
    private Timer m_timerDisconnection = null;
    private Timer m_reconnectionTimer;

    public XmppConnection(Context context, IPlatformServices platformServices, IContactCacheMgr contactCacheMgr, IDirectoryProxy directoryProxy, IUserProxy userProxy, IConversationProxy conversationProxy, IRoomMgr roomMgr, SettingsProxy settingsProxy, IFileMgr fileMgr, IPgiConferenceMgr pgiMgr, ICapabilities capabilities)
    {
        Log.getLogger().info(LOG_TAG, ">XmppConnection");

        m_connectionState = ConnectionState.IDLE;
        m_applicationContext = context.getApplicationContext();
        m_platformServices = platformServices;
        m_password = m_platformServices.getApplicationData().getUserJidPassword();

        String jid = m_platformServices.getApplicationData().getUserJidIm();
        String[] jidSplitted = jid.split("@");
        String serviceName = null;
        if (jidSplitted.length > 1) {
            m_username = jidSplitted[0];
            serviceName = jidSplitted[1];
        }

        String host = m_platformServices.getApplicationData().getHost();
        if (host == null) {
            host = m_platformServices.getApplicationData().getDefaultHost();
        }

        XMPPWebSocketConfiguration.Builder builder = XMPPWebSocketConfiguration.builder();
        builder.setServiceName(serviceName);
        builder.setUsernameAndPassword(m_username, m_password);
        builder.setPort(443);
        builder.setFile("/websocket");
        builder.setHost(host);
        builder.setUseHttps(true);
        builder.setDebuggerEnabled(true);
        builder.setResource(getNewResourceId());


        SSLContext sslContext = SSLUtil.getSecurityContext().getSslContext();
        builder.setCustomSSLContext(sslContext);
        builder.setHostVerifier(new RainbowHostnameVerifier());
        builder.setTrustManager(SSLUtil.getSecurityContext().getTrustManagers());
        builder.setEngineConfigurator(new RainbowAsyncSSLEngineConfigurator());

        m_dataNetworkMonitor = RainbowContext.getInfrastructure().getDataNetworkMonitor();
        m_dataNetworkMonitor.registerDataNetworkChangedListener(this);

        m_connection = new XMPPWebSocketConnection(builder.build());
        m_connection.setPacketReplyTimeout(Duration.FIFTEEN_SECONDS_IN_MILLISECONDS);

        m_pushMgr = canGetFirebaseInstance() ? new XmppPushMgr(context, m_connection, serviceName) : null;

        m_connectionForcedClose = false;
        acquireWakeLock(true);

        m_callLogMgr = new CallLogMgr(m_connection, m_applicationContext, contactCacheMgr, m_platformServices);
        m_xmppContactMgr = new XmppContactMgr(m_connection, m_platformServices, contactCacheMgr, directoryProxy, userProxy);
        m_chatMgr = new ChatMgr(m_connection, m_applicationContext, m_xmppContactMgr, conversationProxy, fileMgr, pgiMgr);
        m_multiChatMgr = new MultiUserChatMgr(m_connection, m_applicationContext, roomMgr, m_chatMgr);
        m_telephonyMgr = new TelephonyMgr(m_connection, m_applicationContext, m_xmppContactMgr, contactCacheMgr, getNewResourceId(), settingsProxy, capabilities);


        INotificationFactory notificationFactory = m_platformServices.getNotificationFactory();
        if (notificationFactory != null) {
            notificationFactory.createIMNotificationMgr(this, m_chatMgr);
        }

        //Set ConnectionListener here to catch initial connect();
        m_connection.addConnectionListener(this);
    }

    public boolean isConnectedOrResummable() {
        if (m_connection == null)
            return  false;

        if (m_connection.isConnected()) {
            return true;
        }

        return m_connection.isSmResumptionPossible(Duration.TWO_MINUTES_IN_MILLISECONDS);
    }


    public boolean isConnected() {
        if (m_connection == null)
            return  false;

        return (m_connection.isConnected() && m_connection.isAuthenticated());
    }


    /////////////////////////
    // Use only for Tests ;
    public void setFakeWebSocket(WebSocket webSocket) {
        ((XMPPWebSocketConnection)m_connection).setFakeWebSocket(webSocket);
    }

    public AbstractRainbowXMPPConnection getConnection() {
        return m_connection;
    }

//    public void setWebSocketConnection(AbstractRainbowXMPPConnection connection)
//    {
//        this.m_connection = connection;
//    }

    public XmppContactMgr getXmppContactMgr()
    {
        return m_xmppContactMgr;
    }


    public ConnectionState getConnectionState()
    {
        return m_connectionState;
    }




    public ChatMgr getChatMgr()
    {
        return m_chatMgr;
    }


    public MultiUserChatMgr getMultiUserChatMgr()
    {
        return m_multiChatMgr;
    }

    public void connect() throws IOException, XMPPException, SmackException
    {
        Log.getLogger().info(LOG_TAG, ">connect");

        m_connection.setUseStreamManagementDefault(true);
        m_connection.setUseStreamManagementResumptionDefault(true);
        m_connection.setUseStreamManagement(true);

        m_connection.connect();
    }


    private String getNewResourceId()
    {
        StringBuilder newId = new StringBuilder();
        newId.append("mobile_android_");
        newId.append(Util.getDeviceImei(m_applicationContext));

        return newId.toString();
    }

    public void disconnect() {
        Log.getLogger().info(LOG_TAG, "disconnect()");

        if (m_pushMgr != null)
            m_pushMgr.deActivatePushNotification();

        cancelDisconnectionTimer();

        if (m_reconnectionManager != null)
            m_reconnectionManager.disableAutomaticReconnection();

        if (m_telephonyMgr != null)
            m_telephonyMgr.disconnect();

        if (m_callLogMgr != null)
            m_callLogMgr.disconnect();

        if (m_chatMgr != null)
            m_chatMgr.disconnect();

        if (m_multiChatMgr != null)
            m_multiChatMgr.disconnect();

        if (m_connection != null) {

            m_connection.removeConnectionListener(this);

            if (m_platformServices.getNotificationFactory() != null) {
                m_platformServices.getNotificationFactory().stopIMNotificationMgr();
            }

            if (m_connection.isConnected() && getConnectionState() == XmppConnection.ConnectionState.CONNECTED) {
                m_connectionState = ConnectionState.DISCONNECTING;

                try {
                    CarbonManager carbonManager = CarbonManager.getInstanceFor(m_connection);
                    carbonManager.sendCarbonsEnabled(false);
                    StanzaFilter filter = new StanzaTypeFilter(IQ.class);
                    PacketCollector collector;
                    if (m_xmppContactMgr != null) {
                        collector = m_connection.createPacketCollectorAndSend(filter, m_xmppContactMgr.getSettingsPresence());
                        m_xmppContactMgr.sendPresence(Presence.Type.unavailable, null, null);
                    } else
                        collector = m_connection.createPacketCollectorAndSend(filter, new Presence(Presence.Type.unavailable));

                    collector.nextResultOrThrow();

                } catch (Exception e) {
                    Log.getLogger().error(LOG_TAG, "Problem while disconnecting: ", e);
                }
            }

            m_connection.shutdown();
            m_connection = null;

            Log.getLogger().info(LOG_TAG, "disconnecting done ");

            if (m_xmppContactMgr != null)
                m_xmppContactMgr.disconnect();
        }
        else
        {
            if( m_xmppContactMgr != null)
                m_xmppContactMgr.disconnect();
        }
    }

    public void forceCloseWebSocket()
    {
        Log.getLogger().info(LOG_TAG, ">forceCloseWebSocket for GCM");
        if (m_connection != null)
        {
            //cancelDisconnectionTimer();
            m_dataNetworkMonitor.unregisterDataNetworkChangedListener(this);
            if( m_reconnectionManager != null)
                m_reconnectionManager.disableAutomaticReconnection();

            ((XMPPWebSocketConnection)m_connection).closeWebSocket();
            m_connectionForcedClose = true;
            acquireWakeLock(false);
        }
    }

    public boolean isForcedClose() { return m_connectionForcedClose; }

    public void forceOpenWebSocket()
    {
        Log.getLogger().info(LOG_TAG, ">forceOpenWebSocket");
        //scheduleDisconnectionTimer();
        m_dataNetworkMonitor.registerDataNetworkChangedListener(this);
        if( !m_dataNetworkMonitor.isDataNetworkAvailable() ) {
            Log.getLogger().info(LOG_TAG, "DataNetwork Not Available");
            return;
        }

        acquireWakeLock(true);
        m_connectionForcedClose = false;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (m_connection != null && m_reconnectionManager != null) {

                    m_reconnectionManager.enableAutomaticReconnection();
                    m_reconnectionManager.setFixedDelay(0);
                    startReconnectionModeTimer();

                    m_connection.notifyConnectionError();
                }
            }
        });
        thread.start();
    }

    @Override
    public void connected(XMPPConnection connection)
    {
        m_connectionState = ConnectionState.CONNECTED;

        Log.getLogger().info(LOG_TAG, "==============   CONNECTED   ==============");

        if (m_connection == null || m_connectionState == ConnectionState.DISCONNECTING) {
            return;
        }


        sendConnectionChangedNotification();

        try
        {
            m_connection.login(m_username, m_password, getNewResourceId());

            m_connection.sendStanza(new Presence(Presence.Type.available,"",5, Presence.Mode.available));

            ReconnectionManager.setEnabledPerDefault(true);
            m_reconnectionManager = ReconnectionManager.getInstanceFor(m_connection);

            m_reconnectionManager.setReconnectionPolicy(ReconnectionManager.ReconnectionPolicy.RANDOM_INCREASING_DELAY);
            m_reconnectionManager.enableAutomaticReconnection();

            PingManager pingManager = PingManager.getInstanceFor(m_connection);
            pingManager.setPingInterval(-1);

            //Enabling carbons
            CarbonManager carbonManager = CarbonManager.getInstanceFor(m_connection);
            try {
                if (carbonManager.isSupportedByServer()) {
                    carbonManager.sendCarbonsEnabled(true);
                }
            } catch (Exception e) {
                Log.getLogger().error(LOG_TAG, "Carbon Exception; " + e.toString());
            }

            if (ClientStateIndicationManager.isSupported(m_connection))
                Log.getLogger().verbose(LOG_TAG, "CSI is Managed by Server");
            else
                Log.getLogger().verbose(LOG_TAG, "CSI is NOT Managed by Server");
        }
        catch (Exception e1)
        {
            Log.getLogger().warn(LOG_TAG, "connectionClosedOnError; " + e1.getMessage());
        }
    }

    //MessageListener

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed)
    {
        Log.getLogger().info(LOG_TAG, "==============   AUTHENTICATED   ==============");

        if (m_connection == null || m_connectionState == ConnectionState.DISCONNECTING) {
            cancelDisconnectionTimer();
            return;
        }
        m_connectionState = ConnectionState.CONNECTED;

        Intent intent = new Intent(RainbowIntent.ACTION_RAINBOW_XMPP_AUTHENTICATION_SUCCESS);
        m_applicationContext.sendBroadcast(intent);


        //No more login here, login is done in "connected" callback only way to change resource value at each connection
        //m_connection.login();

        sendConnectionChangedNotification();

        if( !m_connection.streamWasResumed() ) {
            m_xmppContactMgr.loadUserInfos();
            if (m_chatMgr != null)
                m_chatMgr.cancelNotifications();
        }
        else {
            m_xmppContactMgr.getRoster().setRosterLoadedAtLogin(true);
        }

        m_chatMgr.getServerTime();
        m_chatMgr.resentMessages();
        cancelDisconnectionTimer();

        if (m_pushMgr != null) {
            m_pushMgr.activatePushNotification();
        }

        m_callLogMgr.retrieveCallLogs(null);
    }

    private void cancelDisconnectionTimer() {
        if( m_timerDisconnection != null) {
            m_timerDisconnection.cancel();
            m_timerDisconnection = null;
        }
    }

    private void scheduleDisconnectionTimer() {
        Log.getLogger().info(LOG_TAG, ">scheduleDisconnectionTimer");
        cancelDisconnectionTimer();

        if (m_connection == null || m_connectionState == ConnectionState.DISCONNECTING) {
            Log.getLogger().info(LOG_TAG, ">stop scheduleDisconnectionTimer");
            return;
        }

        m_timerDisconnection = new Timer();
        m_timerDisconnection.schedule(new TimerTask() {
            @Override
            public void run() {
                sendConnectionChangedNotification();
            }
        },Duration.THIRTY_SECONDS_IN_MILLISECONDS, Duration.THIRTY_SECONDS_IN_MILLISECONDS);
    }
    //ConnectionListener

    @Override
    public void connectionClosed()
    {
        m_connectionState = ConnectionState.DISCONNECTED;
        Log.getLogger().info(LOG_TAG, "==============   CONNECTIONCLOSED   ==============");

        sendConnectionChangedNotification();
        scheduleDisconnectionTimer();

        m_chatMgr.invalidateConvMam();
    }

    @Override
    public void connectionClosedOnError(Exception e)
    {
        m_connectionState = ConnectionState.DISCONNECTED;

        Log.getLogger().warn(LOG_TAG, "==============   CONNECTIONCLOSEDONERROR   ==============");
        if (e != null)
            Log.getLogger().warn(LOG_TAG, "connectionClosedOnError; " + e.getMessage());

        sendConnectionChangedNotification();
        scheduleDisconnectionTimer();

        m_chatMgr.invalidateConvMam();
    }

    @Override
    public void reconnectingIn(int seconds)
    {
        m_connectionState = ConnectionState.RECONNECTING;
        Log.getLogger().info(LOG_TAG, "==============   RECONNECTINGIN   ==============");

        if( m_dataNetworkMonitor.isDataNetworkAvailable() ) {
            sendConnectionChangedNotification();
        } else {
            Log.getLogger().info(LOG_TAG, "  reconnectingIn should not append");
        }
    }

    @Override
    public void reconnectionSuccessful()
    {
        m_connectionState = ConnectionState.CONNECTED;
        Log.getLogger().warn(LOG_TAG, "==============   RECONNECTIONSUCCESSFUL   ==============");

        sendConnectionChangedNotification();

        m_xmppContactMgr.loadUserInfos();

        cancelDisconnectionTimer();
    }

    @Override
    public void reconnectionFailed(final Exception e)
    {
        m_connectionState = ConnectionState.DISCONNECTED;
        Log.getLogger().warn(LOG_TAG, "==============   RECONNECTIONFAILED   ==============", e);

        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run() {
                if (m_connection != null && m_connectionState != ConnectionState.DISCONNECTING) {
                    m_connection.notifyConnectionError();
                }
            }
        }, "Reconnection failed async");
        thread.start();
    }

    private void sendConnectionChangedNotification()
    {

        if (m_connection == null || m_connectionState == ConnectionState.DISCONNECTING) {
            Log.getLogger().info(LOG_TAG, ">stop sendConnectionChangedNotification");
            return;
        }
        Log.getLogger().info (LOG_TAG, ">sendConnectionChangedNotification connection is " + m_connectionState.toString());
        Intent intent = new Intent(XmppIntent.CONNECTION_STATE_CHANGE);
        m_applicationContext.sendBroadcast(intent);
    }

    @Override
    public void wifiOr3GAvailabilityChanged(boolean isWifiOr3GAvailable)
    {
        Log.getLogger().info(LOG_TAG, "==============   wifiOr3GAvailabilityChanged " + isWifiOr3GAvailable + "  ==============");
    }

    @Override
    public void dataNetworkAvailabilityChanged(boolean isNetworkAvailable)
    {
        Log.getLogger().info(LOG_TAG, "==============   dataNetworkAvailabilityChanged " + isNetworkAvailable + "  ==============");

        if (isNetworkAvailable)
        {
            Log.getLogger().verbose(LOG_TAG, "Data link is now available / enable Xmpp auto Reconnection");

            if (m_reconnectionManager != null)
            {
                m_reconnectionManager.enableAutomaticReconnection();
                m_reconnectionManager.setFixedDelay(0);

                startReconnectionModeTimer();
            }

            if( m_connection != null)
                m_connection.notifyConnectionError();
        }
        else
        {
            Log.getLogger().warn(LOG_TAG, "Data link is NOT available / disable Xmpp auto Reconnection");
            if (m_connection != null && m_reconnectionManager != null) {
                m_reconnectionManager.disableAutomaticReconnection();
            }
        }
    }

    private void startReconnectionModeTimer()
    {
        if (m_reconnectionTimer != null)
            m_reconnectionTimer.cancel();

        m_reconnectionTimer = new Timer();
        m_reconnectionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (m_reconnectionManager != null)
                {
                    m_reconnectionManager.setReconnectionPolicy(ReconnectionManager.ReconnectionPolicy.RANDOM_INCREASING_DELAY);
                }
            }
        },Duration.ONE_SECOND_IN_MILLISECONDS);
    }

    public void filterStateIndication(final boolean active) {
        Log.getLogger().verbose(LOG_TAG, ">filterStateIndication; "+String.valueOf(active));
        if (m_connection == null) {
            Log.getLogger().warn(LOG_TAG, "ClientStateIndication : connection is NULL");
            return;
        }
        if (!ClientStateIndicationManager.isSupported(m_connection)) {
            Log.getLogger().warn(LOG_TAG, "ClientStateIndication is Not Supported by Server");
            return;
        }

        final Thread receptionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (active) {
                        if (!m_clientStateIndicationState) {
                            Log.getLogger().verbose(LOG_TAG, "Activating ClientStateIndication");
                            ClientStateIndicationManager.inactive(m_connection);
                            m_clientStateIndicationState = true;
                        }
                    } else {
                        if (m_clientStateIndicationState) {
                            Log.getLogger().verbose(LOG_TAG, "Disactivating ClientStateIndication");
                            ClientStateIndicationManager.active(m_connection);
                            m_clientStateIndicationState = false;
                        }
                    }
                } catch (Exception e) {
                    Log.getLogger().warn(LOG_TAG, "An error occured while changing stateIndication: "+e.getMessage());
                }
            }
        }, "filterStateIndicationThread");
        receptionThread.start();
    }


    private void acquireWakeLock(boolean wakeLockState) {
        if (m_platformServices != null && m_platformServices.getDeviceSleepLock() != null) {
            if (wakeLockState) {
                m_platformServices.getDeviceSleepLock().acquire();
            } else {
                m_platformServices.getDeviceSleepLock().release();
            }
        }
    }

    public XmppPushMgr getXmppPushMgr() {
        return m_pushMgr;
    }

    public CallLogMgr getCallLogMgr()
    {
        return m_callLogMgr;
    }


    public enum ConnectionState {
        IDLE ("IDLE"),
        CONNECTED ("CONNECTED"),
        CONNECTING ("CONNECTING"),
        RECONNECTING ("RECONNECTING"),
        DISCONNECTING ("DISCONNECTING"),
        DISCONNECTED ("DISCONNECTED");

        private final String name;

        ConnectionState(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    public TelephonyMgr getTelephonyMgr()
    {
        return m_telephonyMgr;
    }

    private boolean canGetFirebaseInstance() {
        try {
            FirebaseInstanceId.getInstance();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }
}

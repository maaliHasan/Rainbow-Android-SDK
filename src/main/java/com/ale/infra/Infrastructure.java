/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : Rainbow Android
 * Summary :
 * *****************************************************************************
 * History
 *
 */
package com.ale.infra;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.application.RainbowIntent;
import com.ale.infra.capabilities.CapabilitiesMgr;
import com.ale.infra.capabilities.ICapabilities;
import com.ale.infra.contact.ContactCacheMgr;
import com.ale.infra.contact.ContactFactory;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.database.DatabaseMgr;
import com.ale.infra.database.IDatabaseMgr;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.RESTAsyncRequest;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.manager.CallLogMgr;
import com.ale.infra.manager.ChatMgr;
import com.ale.infra.manager.CompanyInvitationMgr;
import com.ale.infra.manager.GroupMgr;
import com.ale.infra.manager.ICompanyInvitationMgr;
import com.ale.infra.manager.IGroupMgr;
import com.ale.infra.manager.IInvitationMgr;
import com.ale.infra.manager.InvitationMgr;
import com.ale.infra.manager.LocationMgr;
import com.ale.infra.manager.MultiUserChatMgr;
import com.ale.infra.manager.XmppContactMgr;
import com.ale.infra.manager.fileserver.FileServerMgr;
import com.ale.infra.manager.fileserver.IFileMgr;
import com.ale.infra.manager.fileserver.IFileProxy;
import com.ale.infra.manager.pgiconference.IPgiConferenceMgr;
import com.ale.infra.manager.pgiconference.IPgiConferenceProxy;
import com.ale.infra.manager.pgiconference.PgiConferenceMgr;
import com.ale.infra.manager.room.IRoomMgr;
import com.ale.infra.manager.room.RoomMgr;
import com.ale.infra.platformservices.IDeviceSleepLock;
import com.ale.infra.platformservices.IPeriodicWorkerManager;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.platformservices.IScreenStateChangeNotifier;
import com.ale.infra.proxy.EnduserBots.EnduserBotsProxy;
import com.ale.infra.proxy.EnduserBots.IEnduserBotsProxy;
import com.ale.infra.proxy.admin.SettingsProxy;
import com.ale.infra.proxy.authentication.AuthenticationProxy;
import com.ale.infra.proxy.authentication.AuthenticationResponse;
import com.ale.infra.proxy.authentication.IAuthentication;
import com.ale.infra.proxy.avatar.AvatarProxy;
import com.ale.infra.proxy.avatar.IAvatarProxy;
import com.ale.infra.proxy.company.CompanyProxy;
import com.ale.infra.proxy.company.ICompanyProxy;
import com.ale.infra.proxy.conversation.ConversationProxy;
import com.ale.infra.proxy.conversation.IConversationProxy;
import com.ale.infra.proxy.directory.DirectoryProxy;
import com.ale.infra.proxy.directory.IDirectoryProxy;
import com.ale.infra.proxy.fileserver.FileProxy;
import com.ale.infra.proxy.group.GroupProxy;
import com.ale.infra.proxy.group.IGroupProxy;
import com.ale.infra.proxy.notifications.INotificationProxy;
import com.ale.infra.proxy.notifications.NotificationsProxy;
import com.ale.infra.proxy.pgiconference.PgiConferenceProxy;
import com.ale.infra.proxy.profile.ProfileProxy;
import com.ale.infra.proxy.provisionning.ApkProvisionnerProxy;
import com.ale.infra.proxy.provisionning.IApkProvisionner;
import com.ale.infra.proxy.room.IRoomProxy;
import com.ale.infra.proxy.room.RoomProxy;
import com.ale.infra.proxy.users.IUserProxy;
import com.ale.infra.proxy.users.UsersProxy;
import com.ale.infra.rainbow.api.IServicesFactory;
import com.ale.infra.rainbow.api.RainbowServicesFactory;
import com.ale.infra.xmpp.XmppConnection;
import com.ale.rainbow.datanetworkmonitor.DataNetworkMonitor;
import com.ale.rainbow.periodicworker.PeriodicWorkerManager;
import com.ale.rainbow.periodicworker.ScreenStateReceiver;
import com.ale.listener.IConnectionListener;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.security.util.HttpAuthorizationUtil;
import com.ale.util.Duration;
import com.ale.util.log.Log;
import com.android.volley.NoConnectionError;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.ale.infra.Infrastructure.InfrastructureState.STOP_ASKED;

/**
 * The infrastructure hide the infrastructure complexity to provide a consolidated api level.
 */
public class Infrastructure implements IInfrastructure
{
    private static final String LOG_TAG = "Infrastructure";
    private static final String RAINBOW_NAME = "rainbow";

    private AuthenticationProxy m_authenticationProxy;
    private NotificationsProxy m_notificationsProxy;
    private DirectoryProxy m_directoryProxy;
    private IAvatarProxy m_avatarProxy;
    private RoomProxy m_roomProxy;
    private EnduserBotsProxy m_botsProxy;
    private GroupProxy m_groupProxy;
    private IConversationProxy m_conversationProxy;
    private IUserProxy m_usersProxy;
    private FileProxy m_fileProxy;
    private ICapabilities m_capabilities;
    private ApkProvisionnerProxy m_apkProvisionner;
    private PgiConferenceProxy m_pgiConferenceProxy;

    private IGroupMgr m_groupMgr;
    private RoomMgr m_roomMgr;
    private IContactCacheMgr m_contactCacheMgr;
    private IInvitationMgr m_invitationMgr;
    private ICompanyInvitationMgr m_companyMgr;
    private IDatabaseMgr m_databaseMgr;
    private FileServerMgr m_fileMgr;
    private LocationMgr m_locationMgr;
    private PgiConferenceMgr m_pgiConferenceMgr;

    private Timer m_reAuthentificationTimer = null;
    private IPlatformServices m_platformService;
    private boolean m_isApiSessionStarted;
    private XmppConnection m_connection;
    private IServicesFactory m_servicesFactory;
    private Context m_applicationContext;
    private String userLogin = null;
    private String password = null;
    private IAuthentication.IAuthenticationListener authenticationListener = null;
    private IConnectionListener m_connectionListener;
    private Timer m_xmppStopTimer;

    private boolean m_authenticated = false;
    private ScreenStateReceiver m_screenStateReceiver;

    private PeriodicWorkerManager m_periodicWorkerManager;

    private DataNetworkMonitor m_dataNetworkMonitor;
    private PowerManager.WakeLock m_partialWakeLock = null;

    private IRESTAsyncRequest m_restAsyncRequest;
    private InfrastructureState m_state = InfrastructureState.IDLE;
    private SettingsProxy m_settings;

    private boolean dbLoaded = false;

    public enum InfrastructureState
    {
        IDLE,
        CONNECTING,
        CONNECTED,
        STOP_ASKED
    }

    @Override
    public InfrastructureState getState() {
        return m_state;
    }

    @Override
    public void setState(InfrastructureState state) {
        m_state = state;
    }

    public Infrastructure(IPlatformServices platformServices)
    {
        Log.getLogger().info(LOG_TAG, "Create infrastructure");

        m_platformService = platformServices;
    }

    @Override
    public boolean isInDebugMode(Context context) {
        if (context == null) return false;
        final String packageName = context.getPackageName();
        final PackageManager pm = context.getPackageManager();

        ApplicationInfo appInfo;
        try {
            appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            return ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public ICapabilities getCapabilities()
    {
        return m_capabilities;
    }

    @Override
    public XmppConnection getXmppConnection()
    {
        return m_connection;
    }

    @Override
    public void setXmppConnection(XmppConnection connection) {
        m_connection = connection;
    }

    @Override
    public XmppContactMgr getXmppContactMgr() {
        if( m_connection != null)
            return m_connection.getXmppContactMgr();
        return null;
    }

    @Override
    public ChatMgr getChatMgr() {
        if( m_connection != null)
            return m_connection.getChatMgr();
        return null;
    }

    @Override
    public CallLogMgr getCallLogMgr() {
        if( m_connection != null)
            return m_connection.getCallLogMgr();
        return null;
    }

    @Override
    public MultiUserChatMgr getMultiUserChatMgr() {
        if( m_connection != null)
            return m_connection.getMultiUserChatMgr();
        return null;
    }

    @Override
    public IDirectoryProxy getDirectoryProxy()
    {
        return m_directoryProxy;
    }

    @Override
    public IConversationProxy getConversationProxy()
    {
        return m_conversationProxy;
    }

    @Override
    public INotificationProxy getNotificationsProxy()
    {
        return m_notificationsProxy;
    }

    @Override
    public IUserProxy getUsersProxy()
    {
        return m_usersProxy;
    }

    @Override
    public IAvatarProxy getAvatarProxy()
    {
        return m_avatarProxy;
    }

    @Override
    public IEnduserBotsProxy getEnduserBotsProxy() {
        return m_botsProxy;
    }

    @Override
    public IRoomProxy getRoomProxy()
    {
        return m_roomProxy;
    }

    @Override
    public IFileProxy getFileProxy()
    {
        return m_fileProxy;
    }

    @Override
    public IGroupMgr getGroupMgr()
    {
        return m_groupMgr;
    }

    @Override
    public IFileMgr getFileServerMgr()
    {
        return m_fileMgr;
    }

    @Override
    public IPgiConferenceMgr getPgiConferenceMgr()
    {
        return m_pgiConferenceMgr;
    }

    @Override
    public IContactCacheMgr getContactCacheMgr()
    {
        return m_contactCacheMgr;
    }

    @Override
    public IAuthentication getAuthenticationProxy()
    {
        return m_authenticationProxy;
    }

    @Override
    public IApkProvisionner getApkProvisionner()
    {
        if (m_apkProvisionner == null) {
            m_restAsyncRequest = new RESTAsyncRequest(m_applicationContext);
            m_apkProvisionner = new ApkProvisionnerProxy(m_restAsyncRequest, m_platformService);
        }
        return m_apkProvisionner;
    }

    @Override
    public IPgiConferenceProxy getPgiConferenceProxy()
    {
        return m_pgiConferenceProxy;
    }

    @Override
    public IGroupProxy getGroupProxy() {
        return m_groupProxy;
    }

    @Override
    public IInvitationMgr getInvitationMgr()
    {
        return m_invitationMgr;
    }

    @Override
    public ICompanyInvitationMgr getCompanyInvitationMgr()
    {
        return m_companyMgr;
    }

    @Override
    public IDatabaseMgr getDatabaseMgr()
    {
        return m_databaseMgr;
    }

    @Override
    public IRoomMgr getRoomMgr()
    {
        return m_roomMgr;
    }


    @Override
    public LocationMgr getLocationMgr()
    {
        return m_locationMgr;
    }

    @Override
    public void run(Context applicationContext)
    {
        Log.getLogger().info(LOG_TAG, "Run event handler");
        m_applicationContext = applicationContext;

        m_periodicWorkerManager = new PeriodicWorkerManager(m_applicationContext);

        m_dataNetworkMonitor = new DataNetworkMonitor(m_applicationContext);

        initLocks();

        if (m_restAsyncRequest == null) {
            // This factory construct and initializes the http clients used in lite
            m_restAsyncRequest = new RESTAsyncRequest(m_applicationContext);
        }

        m_servicesFactory = new RainbowServicesFactory();

        createServiceProxies();

        createScreenStateReceiver();

        registerScreenStateNotifier(m_platformService);

        //load DB ASAP
        if( m_contactCacheMgr != null) {
            m_contactCacheMgr.retrieveMobileLocalContacts();
        }
    }

    @Override
    public void runMinimalInfrastructure(Context applicationContext)
    {
        m_applicationContext = applicationContext;

        if (m_restAsyncRequest == null) {
            // This factory construct and initializes the http clients used in lite
            m_restAsyncRequest = new RESTAsyncRequest(m_applicationContext);
        }

        m_servicesFactory = new RainbowServicesFactory();

        createProxies();
    }

    @Override
    public void shutdownConnection()
    {
        Log.getLogger().info(LOG_TAG, "logout");

        m_isApiSessionStarted = false;

        if (m_usersProxy !=null)
            m_usersProxy.setContactCacheMgr(null);

        if (m_reAuthentificationTimer != null)
        {
            m_reAuthentificationTimer.cancel();
            m_reAuthentificationTimer = null;
        }

        if (m_connection != null)
        {
            if(m_contactCacheMgr!=null)
                m_contactCacheMgr.removeObserver(m_connection);

            if(m_groupMgr != null)
                m_groupMgr.removeObserver(m_connection);

            if(m_invitationMgr != null)
                m_invitationMgr.removeObserver(m_connection);

            if(m_roomMgr != null)
            {
                m_roomMgr.removeObserver(m_connection);
                m_roomMgr.setChatMgr(null);
            }

            m_connection.forceCloseWebSocket();
            m_connection = null;
        }

        if (m_authenticationProxy != null)
        {
            m_authenticationProxy.disconnectOfRainbowServer(new IAuthentication.IDisconnectionListener()
            {
                @Override
                public void onSuccess()
                {
                    Log.getLogger().info(LOG_TAG, "success for the disconnection of rainbow server...");
                    shutdownHttpClientAndResetSSL();
                }

                @Override
                public void onFailure()
                {
                    Log.getLogger().error(LOG_TAG, "impossible to disconnect of the rainbow server...");
                    shutdownHttpClientAndResetSSL();
                }
            });
        }
        else
            shutdownHttpClientAndResetSSL();

        if (m_databaseMgr != null)
            m_databaseMgr.shutdown();

        m_databaseMgr = null;

        if (m_screenStateReceiver != null) {
            m_applicationContext.unregisterReceiver(m_screenStateReceiver);
            m_screenStateReceiver = null;
        }

        m_periodicWorkerManager.unregisterWorkers();
        m_periodicWorkerManager = null;

        if (m_dataNetworkMonitor != null)
            m_dataNetworkMonitor.stop();
        m_dataNetworkMonitor = null;

        if (m_partialWakeLock != null)
        {
            while (m_partialWakeLock.isHeld())
            {
                m_partialWakeLock.release();
            }
        }

        RainbowContext.setInfrastructure(null);

        Intent intent =  new Intent(RainbowIntent.ACTION_RAINBOW_LOGOUT);
        m_applicationContext.sendBroadcast(intent);

        m_state = InfrastructureState.IDLE;
    }

    private void shutdownHttpClientAndResetSSL()
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if (m_restAsyncRequest != null)
                    m_restAsyncRequest.abort();
            }
        });
        thread.start();
    }

    @Override
    public boolean isApiSessionStarted()
    {
        return m_isApiSessionStarted;
    }

    @Override
    public boolean startConnectionProcess()
    {
        Log.getLogger().info(LOG_TAG, ">startConnectionProcess");

        if (RainbowContext.getApplicationState() == RainbowContext.ApplicationState.STOPPED)
            return false;

        m_state = InfrastructureState.CONNECTING;

//        m_contactCacheMgr.initContext(m_applicationContext);
//        m_restAsyncRequest.setContext(m_applicationContext);

        connectToRainbowServer();

        return true;
    }

    @Override
    public boolean startLoginApplicationProcess(String applicationId, String applicationSecret, final IAuthentication.IAuthenticationListener listener) {

        authenticationListener = new IAuthentication.IAuthenticationListener() {
            @Override
            public void onSuccess(AuthenticationResponse response) {
                Log.getLogger().info(LOG_TAG, "Authentication to application Success");
                listener.onSuccess(response);
            }

            @Override
            public void onFailure(RainbowServiceException exception) {
                Log.getLogger().error(LOG_TAG, "Authentication to application Failure");
                listener.onFailure(exception);
            }
        };

        m_authenticationProxy.authenticateApplication(applicationId, applicationSecret, authenticationListener);

        return true;
    }

    private void createScreenStateReceiver()
    {
        m_screenStateReceiver = new ScreenStateReceiver();
        m_applicationContext.registerReceiver(m_screenStateReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        m_applicationContext.registerReceiver(m_screenStateReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
    }

    private void connectToRainbowServer()
    {
        Log.getLogger().info(LOG_TAG, ">connectToRainbowServer");

        if (RainbowContext.getApplicationState() == RainbowContext.ApplicationState.STOPPED)
            return;

        userLogin = RainbowContext.getPlatformServices().getApplicationData().getUserLogin();
        password = RainbowContext.getPlatformServices().getApplicationData().getUserPassword();


        loadDB();

        authenticationListener = new IAuthentication.IAuthenticationListener()
        {
            @Override
            public void onSuccess(AuthenticationResponse response)
            {
                Log.getLogger().info(LOG_TAG, "Authentication Success");
                RainbowContext.getPlatformServices().getApplicationData().setLoggedOut(false);

                if (checkEndAskedAndnotify() )
                    return;

                if( m_companyMgr != null) {
                    m_companyMgr.refreshReceivedCompanyInvitationList();
                    m_companyMgr.refreshJoinCompanyRequestList();
                }
                if( m_capabilities != null) {
                    m_capabilities.getUserFeatures(m_contactCacheMgr.getUser().getCorporateId());
                }

                m_authenticated = true;
                if (m_reAuthentificationTimer == null)
                {
                    if (checkEndAskedAndnotify() )
                        return;
                    connectToXMPPServer();
                }
                long timeExpired = HttpAuthorizationUtil.getTimeExpired();
                long currentTime = System.currentTimeMillis();
                if (timeExpired >= currentTime)
                {
                    Log.getLogger().debug(LOG_TAG, "New authentification done for user : " + userLogin);
                    Log.getLogger().debug(LOG_TAG, "time waited " + Long.toString(timeExpired - currentTime));
                    m_reAuthentificationTimer = new Timer();
                    m_reAuthentificationTimer.schedule(new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            m_authenticationProxy.authenticate(userLogin, password, authenticationListener);
                        }
                    }, timeExpired - currentTime);
                }
            }

            @Override
            public void onFailure(RainbowServiceException exception)
            {
                Log.getLogger().warn(LOG_TAG, "Authentication Failure");
                m_authenticated = false;
                if( exception== null) {
                    Log.getLogger().warn(LOG_TAG, "Exception is NULL");
                    return;
                }

                if ( exception.getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED ||
                        exception.getCause() instanceof NoConnectionError) {
                    //m_frameworkPublisherHelper.pushEvent(Tag.FRWK_LOGON_AUTHENTICATION_FAILED);
                    Log.getLogger().verbose(LOG_TAG, "Error 401 detected on Authentication; " + exception.getDetailsMessage());
                    Intent intent =  new Intent(RainbowIntent.ACTION_RAINBOW_LOGIN_AUTHENTICATION_FAILED);
                    intent.putExtra("detailsCode", exception.getDetailsCode());
                    if(exception.getCause() instanceof NoConnectionError) {
                        // Force Wrong Pwd error for old Android Device behavior
                        intent.putExtra("detailsCode", 401500);
                    }
                    m_applicationContext.sendBroadcast(intent);
                } else {
                    //m_frameworkPublisherHelper.pushEvent(Tag.FRWK_LOGON_ERROR);
                    Intent intent =  new Intent(RainbowIntent.ACTION_RAINBOW_LOGIN_ERROR);
                    intent.putExtra("detailsCode", exception.getDetailsCode());
                    m_applicationContext.sendBroadcast(intent);
                }
                m_connectionListener.onSigninFailed(exception.getDetailsCode(), exception.getDetailsMessage());
            }
        };

        if( m_authenticationProxy != null)
            m_authenticationProxy.authenticate(userLogin, password, authenticationListener);

    }

    private synchronized boolean checkEndAskedAndnotify() {
        if( m_state == STOP_ASKED) {
            Log.getLogger().verbose(LOG_TAG, "STOP ASKED -> notify and leave");
            this.notify();
            return true;
        }

        return false;
    }

    @Override
    public void connectToXMPPServer()
    {
        Log.getLogger().verbose(LOG_TAG, ">connectToXMPPServer");

        Thread connectThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (checkEndAskedAndnotify() )
                        return;
                    m_connection = new XmppConnection(m_applicationContext, m_platformService,
                            m_contactCacheMgr, m_directoryProxy, m_usersProxy, m_conversationProxy,
                            m_roomMgr, m_settings, m_fileMgr, m_pgiConferenceMgr,
                            m_capabilities);


                    m_connectionListener.onXmppCreated();
                    m_state = InfrastructureState.CONNECTED;
                    m_isApiSessionStarted = true;


                    if (m_databaseMgr != null) {
                        m_databaseMgr.loadContacts();
                        getRoomMgr().getRoomsFromDB();
                        getChatMgr().getConversationsFromDB();
                    }

                    if (!dbLoaded) Log.getLogger().info(LOG_TAG, ">connectToXMPPServer Sleep");

                    while (!dbLoaded) {
                        Thread.sleep(10);

                    }
                    Log.getLogger().info(LOG_TAG, ">connectToXMPPServer Sleep end");
                    //getRoomMgr().getRoomsFromDB();

                    //getChatMgr().getConversationsFromDB();

                    Intent intent =  new Intent(RainbowIntent.ACTION_RAINBOW_LOGIN_SUCCESS);
                    boolean databaseEmpty = m_databaseMgr != null && m_databaseMgr.isEmpty();
                    if (!databaseEmpty ) {
                        m_applicationContext.sendBroadcast(intent);
                    }

                    m_connectionListener.onSigninSuccessed();

                    // Register observer before connection
                    m_contactCacheMgr.setObserver(m_connection);
                    m_groupMgr.setObserver(m_connection);
                    m_invitationMgr.setObserver(m_connection);
                    m_roomMgr.setObserver(m_connection);
                    m_roomMgr.setChatMgr(m_connection.getChatMgr());

                    m_connection.connect();
                    if (checkEndAskedAndnotify() ) {
                        Log.getLogger().verbose(LOG_TAG, "EndAsked and already connected -> disconnect");
                        m_connection.disconnect();
                        return;
                    }

                    if (databaseEmpty) {
                        m_applicationContext.sendBroadcast(intent);
                    }
                    m_contactCacheMgr.getBots();

                    m_pgiConferenceMgr.retrieveConferences();

                    m_fileMgr.refreshOwnFileDescriptors(null);
                    m_fileMgr.refreshOtherFileDescriptors(null);

                }
                catch (Exception e)
                {
                    Log.getLogger().error(LOG_TAG, "Impossible to start xmpp connection", e);
                    Intent intent = new Intent(RainbowIntent.ACTION_RAINBOW_LOGIN_ERROR);
                    m_applicationContext.sendBroadcast(intent);
                }
            }
        });
        connectThread.setName("connectThread");
        connectThread.start();

    }

    @Override
    public boolean isXmppConnected() {
        if (m_connection != null && m_connection.isConnected() && m_dataNetworkMonitor.isDataNetworkAvailable())
            return true;

        return false;
    }

    public boolean isRestConnected() {
        if (m_dataNetworkMonitor != null && m_dataNetworkMonitor.isDataNetworkAvailable() &&
                m_authenticated)
            return true;

        return false;
    }

    @Override
    public synchronized void stopConnection(int milliseconds) {
        try {
            wait(milliseconds);
        } catch (InterruptedException e) {
            Log.getLogger().error(LOG_TAG, "stopConnection exception: "+e.getMessage());
        }
    }

    private void createServiceProxies()
    {
        createProxies();

        createContactFactory();
    }

    private void createProxies()
    {

        m_databaseMgr = new DatabaseMgr(m_applicationContext);

        m_avatarProxy = new AvatarProxy(m_servicesFactory, m_restAsyncRequest, m_platformService);

        m_directoryProxy = new DirectoryProxy(m_servicesFactory, m_restAsyncRequest, m_platformService);

        m_usersProxy = new UsersProxy(m_servicesFactory, m_restAsyncRequest, m_platformService);

        m_botsProxy = new EnduserBotsProxy(m_servicesFactory, m_restAsyncRequest, m_platformService);

        ICompanyProxy companyProxy = new CompanyProxy(m_servicesFactory, m_restAsyncRequest, m_platformService);

        m_contactCacheMgr = new ContactCacheMgr(m_platformService, m_directoryProxy, m_avatarProxy,
                m_usersProxy, m_botsProxy, m_databaseMgr);

        m_roomProxy = new RoomProxy(m_servicesFactory, m_contactCacheMgr, m_restAsyncRequest, m_platformService);

        m_usersProxy.setContactCacheMgr(m_contactCacheMgr);

        m_authenticationProxy = new AuthenticationProxy(m_servicesFactory, m_restAsyncRequest, m_platformService, m_contactCacheMgr.getUser());

        m_conversationProxy = new ConversationProxy(m_servicesFactory, m_restAsyncRequest, m_platformService, m_contactCacheMgr, m_directoryProxy);

        m_notificationsProxy = new NotificationsProxy(m_servicesFactory, m_restAsyncRequest, m_platformService);

        if (m_apkProvisionner == null) {
            m_apkProvisionner = new ApkProvisionnerProxy(m_restAsyncRequest, m_platformService);
        }

        m_groupProxy = new GroupProxy(m_servicesFactory, m_restAsyncRequest, m_contactCacheMgr, m_platformService);

        m_fileProxy = new FileProxy(m_servicesFactory, m_restAsyncRequest, m_platformService);

        ProfileProxy profileProxy = new ProfileProxy(m_servicesFactory, m_restAsyncRequest, m_platformService);

        m_pgiConferenceProxy = new PgiConferenceProxy(m_servicesFactory, m_restAsyncRequest, m_platformService);

        // MGR PART :

        m_capabilities = new CapabilitiesMgr(profileProxy, m_applicationContext);

        m_invitationMgr = new InvitationMgr(m_contactCacheMgr, m_platformService, m_usersProxy, m_notificationsProxy);

        m_companyMgr = new CompanyInvitationMgr(m_contactCacheMgr, m_platformService, m_usersProxy, companyProxy, m_avatarProxy);

        m_groupMgr = new GroupMgr(m_contactCacheMgr, m_directoryProxy, m_groupProxy);

        m_pgiConferenceMgr = new PgiConferenceMgr(m_pgiConferenceProxy);

        m_roomMgr = new RoomMgr(m_contactCacheMgr, m_pgiConferenceMgr, m_platformService, m_databaseMgr, m_roomProxy, m_capabilities);

        m_settings = new SettingsProxy(m_servicesFactory, m_restAsyncRequest, m_platformService);
        m_fileMgr = new FileServerMgr(m_applicationContext, m_fileProxy);

        m_contactCacheMgr.initContext(m_applicationContext);
        m_restAsyncRequest.setContext(m_applicationContext);
        m_pgiConferenceMgr.setContext(m_applicationContext);

        m_locationMgr = new LocationMgr(m_applicationContext);
    }

    private void createContactFactory()
    {
        ContactFactory contactFactory = new ContactFactory();
        RainbowContext.setContactFactory(contactFactory);
    }

    private void initLocks()
    {
        final PowerManager pm = (PowerManager) m_applicationContext.getSystemService(Context.POWER_SERVICE);
        m_partialWakeLock = pm.newWakeLock((PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), RAINBOW_NAME);
        m_partialWakeLock.setReferenceCounted(true);

        RainbowContext.getPlatformServices().setDeviceSleepLock(new IDeviceSleepLock()
        {

            @Override
            public void release()
            {
                if (m_partialWakeLock.isHeld())
                {
                    m_partialWakeLock.release();
                }
            }

            @Override
            public void acquire()
            {
                m_partialWakeLock.acquire();
            }
        });
    }

    private void registerScreenStateNotifier(IPlatformServices platformServices)
    {
        m_screenStateReceiver.setScreenStateChangeNotifier(new IScreenStateChangeNotifier() {
            @Override
            public void notifyScreenOn() {
                Log.getLogger().verbose(LOG_TAG, ">notifyScreenOn");
                if( m_xmppStopTimer != null) {
                    m_xmppStopTimer.cancel();
                }
            }

            @Override
            public void notifyScreenOff() {

                Log.getLogger().verbose(LOG_TAG, ">notifyScreenOff");
                long duration = Duration.FIVE_MINUTES_IN_MILLISECONDS;
                if (isInDebugMode(m_applicationContext))
                    duration = Duration.FIVE_SECONDS_IN_MILLISECONDS;


                XmppConnection xmppConnection = RainbowContext.getInfrastructure().getXmppConnection();
                if (xmppConnection != null) {
                    xmppConnection.filterStateIndication(true);
                    m_xmppStopTimer = new Timer("XmppStopTimer");
                    m_xmppStopTimer.schedule(new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            Log.getLogger().verbose(LOG_TAG, "We stop the XMPP Connection now");
                            XmppConnection xmppConnection = RainbowContext.getInfrastructure().getXmppConnection();
                            if( xmppConnection != null) {
                                xmppConnection.forceCloseWebSocket();
                            }
                        }
                    }, duration);
                }
            }
        });
    }

    @Override
    public boolean isPermissionAllowed(String androidPermission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // This Build is < 6 , you can Access to permission
            return true;
        }
        if (m_applicationContext.checkSelfPermission(androidPermission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        return false;
    }

    @Override
    public ScreenStateReceiver getScreenStateReceiver() {
        return m_screenStateReceiver;
    }

    @Override
    public IPeriodicWorkerManager getPeriodicWorkerManager() {
        return m_periodicWorkerManager;
    }

    @Override
    public DataNetworkMonitor getDataNetworkMonitor() {
        return m_dataNetworkMonitor;
    }

    public IRESTAsyncRequest getRestAsyncRequest() {
        return m_restAsyncRequest;
    }

    public void setApplicationContext(Context context) {
        m_applicationContext = context;
    }

    public Context getApplicationContext() {
        return m_applicationContext;
    }

    public void registerConnectionListener(IConnectionListener listener){
        m_connectionListener = listener;
    }

    public void unregisterConnectionListener(IConnectionListener listener){
        m_connectionListener = null;
    }

    private void loadDB() {

        dbLoaded = false;
        Thread loadDB = new Thread(new Runnable() {
            @Override
            public void run() {
                if (m_databaseMgr != null) {
                    Log.getLogger().verbose(LOG_TAG, ">connectToXMPPServer load DB");
                    m_databaseMgr.loadContacts();
                    Log.getLogger().verbose(LOG_TAG, ">connectToXMPPServer load DB End");

                    m_contactCacheMgr.setUser();
                    dbLoaded = true;
                }

            }
        });
        loadDB.setName("connectThread");
        loadDB.start();

    }
}

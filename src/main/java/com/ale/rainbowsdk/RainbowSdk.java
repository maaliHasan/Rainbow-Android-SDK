package com.ale.rainbowsdk;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.application.RainbowIntent;
import com.ale.infra.datastorage.DataStorage;
import com.ale.infra.googlepush.GooglePushRegService;
import com.ale.infra.manager.INotificationFactory;
import com.ale.infra.platformservices.IFileAccessService;
import com.ale.preferences.PreferencesFactory;
import com.ale.rainbow.AndroidLogger;
import com.ale.rainbow.periodicworker.ScreenStateReceiver;
import com.ale.security.util.CryptUtil;
import com.ale.security.util.SSLInitException;
import com.ale.security.util.SSLUtil;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * This is the main class of the SDK for Android.
 * This allows you to use the different Rainbow services.
 */

public class RainbowSdk
{

    public enum ErrorCode
    {
        CONNECTION_WRONG_LOGIN_OR_PWD,
        CONNECTION_ACCOUNT_LOCKED,
        CONNECTION_ACCOUNT_NOT_ACTIVATE,
        CONNECTION_COMPANY_NOT_ACTIVATE,
        CONNECTION_ERROR,
        NO_NETWORK_CONNECTION
    }

    private static final String LOG_TAG = "RainbowSdk";

    private static final int KEY_PHRASE_STRING_SIZE = 32;
    private static final int KEY_PHRASE_INT_SIZE = 130;

    private static final String PREFERENCE_NAME = "rainbow";

    private Context m_context;
    private boolean m_isInitialized = false;
    private String m_applicationId;
    private String m_applicationSecret;
    private static RainbowSdk m_instance;
    private Connection m_connection;
    private Contacts m_contacts;
    private Conversations m_conversations;
    private Im m_im;
    private MyProfile m_myProfile;

    private AndroidPlateformServices m_androidPlatformServices;
    private boolean logStarted = false;

    private NotificationCompat.Builder m_notificationBuilder;

    private INotificationFactory m_notificationFactory = null;

    private String m_pushGoogleSenderId = "";

    private RainbowSdk()
    {

    }

    /**
     * Initialize the SDK with no applicationId / applicationSecret
     */
    public void initialize()
    {
        if (m_notificationBuilder == null) {
            Log.getLogger().error(LOG_TAG, "initialize: m_notificationBuilder is null");
            throw new IllegalStateException("You must call setNotificationBuilder before.");
        }

        try
        {
            ProviderInstaller.installIfNeeded(m_context);
        }
        catch (GooglePlayServicesRepairableException e)
        {
            Log.getLogger().error(LOG_TAG,"GooglePlayServicesRepairableException",e);
        }
        catch (GooglePlayServicesNotAvailableException e)
        {
            Log.getLogger().error(LOG_TAG,"GooglePlayServicesNotAvailableException",e);
        }


        IFileAccessService fileAccessService = new IFileAccessService() {
            @Override
            public File getDirectory(String name) {
                return m_context.getDir(name, Context.MODE_PRIVATE);
            }
        };
        m_androidPlatformServices = new AndroidPlateformServices(m_context);
        m_androidPlatformServices.setFileAccessService(fileAccessService);

        RainbowContext.setPlatformServices(m_androidPlatformServices);

        SharedPreferences sharedPref = m_context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        DataStorage dataStorage = new DataStorage(sharedPref, m_context);

        PreferencesFactory preferencesFactory = new PreferencesFactory(dataStorage);
        m_androidPlatformServices.setPreferencesFactory(preferencesFactory);

        CryptUtil.initWithKeyPhrase(getOrGenerateKeyPhrase());

        startLogger();

        // Initializes the SSL stores
        // Done here once at all at application start. Application.OnCreate() is also called when
        // mic preferences are opened although the mic application has not been started before.
        initializeSSL();
    }

    /**
     * Initialize the SDK with applicationId / applicationSecret
     *
     * @param applicationId Id of your application that you got on the hub
     * @param applicationSecret Secret key of your application that you got on the hub
     */
    public void initialize(String applicationId, String applicationSecret) {
        initialize();
        m_applicationId = applicationId;
        m_applicationSecret = applicationSecret;
    }

    public static RainbowSdk instance()
    {
        if (m_instance == null)
        {
            m_instance = new RainbowSdk();
        }
        return m_instance;
    }

    public Context getContext()
    {
        return m_context;
    }

    /**
     * Access to the Connection service
     *
     * @return The single instance of Connection
     */
    public Connection connection()
    {
        if (m_connection == null) {
            m_connection = new Connection();
        }
        return m_connection;
    }

    /**
     * Access to the Contacts service
     *
     * @return The single instance of Contacts
     */
    public Contacts contacts()
    {
        if (m_contacts == null)
        {
            m_contacts = new Contacts();
        }
        return m_contacts;
    }

    /**
     * Access to the Conversations service
     *
     * @return The single instance of Contacts
     */
    public Conversations conversations()
    {
        if (m_conversations == null)
        {
            m_conversations = new Conversations();
        }
        return m_conversations;
    }

    /**
     * Access to the IM service
     *
     * @return The single instance of Contacts
     */
    public Im im()
    {

        if (m_im == null) {
            m_im = new Im();
        }
        return m_im;
    }

    /**
     * Access to the MyProfile service
     *
     * @return The single instance of Contacts
     */
    public MyProfile myProfile()
    {
        if (m_myProfile == null) {
            m_myProfile = new MyProfile();
        }
        return m_myProfile;
    }

    public boolean isInitialized() {
        return m_isInitialized;
    }

    private String getOrGenerateKeyPhrase()
    {
        String currentKeyPhrase = m_androidPlatformServices.getApplicationData().getKeyPhrase();

        if (StringsUtil.isNullOrEmpty(currentKeyPhrase))
        {
            SecureRandom random = new SecureRandom();
            currentKeyPhrase = new BigInteger(KEY_PHRASE_INT_SIZE, random).toString(KEY_PHRASE_STRING_SIZE);
            m_androidPlatformServices.getApplicationData().setKeyPhrase(currentKeyPhrase);
        }

        return currentKeyPhrase;
    }

    private void startLogger() {

        if (logStarted) {
            //Log already started skip
            return;
        }


        String loggingLevel = m_androidPlatformServices.getUserPreferences().getLoggingLevel();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (m_context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED))
        {
            logStarted = true;
            AndroidLogger logger = new AndroidLogger(m_context, loggingLevel);
            Log.setLogger(logger);
            logger.info(LOG_TAG, ">>>> Rainbow Application start <<<<");
        }
    }

    /**
     * Initialize SSL stores
     */
    private void initializeSSL()
    {
        try
        {
            SSLUtil.initialize();
        }
        catch (SSLInitException e)
        {
            Log.getLogger().error(LOG_TAG, "Unable to initialize SSL", e);
        }
    }

    /**
     * Public method not intended to be used by third party
     *
     * @hide
     */
    public void initializeServices() {
        Log.getLogger().info(LOG_TAG, "initialize");

        // very important: always disable java network cache so that roaming works correctly
        // with intranet and internet having same ics url
        System.setProperty("networkaddress.cache.ttl", "0");

//        createGsmPhone();

//        initializeGSMPhone();

        initServices();

        RainbowContext.setApplicationState(RainbowContext.ApplicationState.INITIALIZING);
    }

    private void initServices()
    {
        Log.getLogger().info(LOG_TAG, "initialize services");

        m_context.startService(new Intent(m_context, ScreenStateReceiver.class));

//        m_context.bindService(new Intent(m_context, RainbowService.class), m_rainbowServiceConnection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);

        if (checkPlayServices()) {
            Log.getLogger().verbose(LOG_TAG, "Starting GooglePushRegService");
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(m_context, GooglePushRegService.class);
            m_context.startService(intent);
        } else {
            Log.getLogger().error(LOG_TAG, "No PlayService available for GooglePush");
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(m_context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                Log.getLogger().info(LOG_TAG, "GooglePlayService not available; " + resultCode);
//                apiAvailability.getErrorDialog(getApplicationContext(), resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
//                        .show();
            } else {
                Log.getLogger().info(LOG_TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    public INotificationFactory getNotificationFactory() {
        return m_notificationFactory;
    }

    public void setNotificationFactory(INotificationFactory m_notificationFactory) {
        this.m_notificationFactory = m_notificationFactory;
    }

    public NotificationCompat.Builder getNotificationBuilder() {
        return this.m_notificationBuilder;
    }

    public void setNotificationBuilder(Context context, Class activity, int iconId, String contentTitle, String contentText, int colorId) {
        m_context = context;

        if (iconId == 0) {
            Log.getLogger().info(LOG_TAG, "setNotificationBuilder: iconId is equals to 0");
        }

        PendingIntent contentIntent = PendingIntent.getActivity(m_context, 0, RainbowIntent.getLauncherIntent(m_context, activity), 0);

        m_notificationBuilder = new NotificationCompat.Builder(m_context).setWhen(System.currentTimeMillis())
                .setSmallIcon(iconId)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(contentIntent).setColor(colorId);
    }

    String getApplicationId() {
        return m_applicationId;
    }

    String getApplicationSecret() {
        return m_applicationSecret;
    }

    String getHost() {
        if (RainbowContext.getPlatformServices().getApplicationData().getHost() != null) {
            return RainbowContext.getPlatformServices().getApplicationData().getHost();
        }
        return RainbowContext.getPlatformServices().getApplicationData().getDefaultHost();
    }

    void setInitialized(boolean initialized) {
        m_isInitialized = initialized;
        if (m_isInitialized) {
            Log.getLogger().info(LOG_TAG, "RainbowSdk is initialized");
        } else {
            Log.getLogger().info(LOG_TAG, "RainbowSdk is NOT initialized");
        }
    }

    public void setPushGoogleSenderId(String pushGoogleSenderId) {
        m_pushGoogleSenderId = pushGoogleSenderId;
    }

    public String getPushGoogleSenderId() {
        return m_pushGoogleSenderId;
    }

}

package com.ale.rainbowsdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.ale.infra.Infrastructure;
import com.ale.infra.application.IApplicationData;
import com.ale.infra.application.RainbowContext;
import com.ale.infra.datastorage.RainbowCredentials;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.platformservices.IDataNetworkMonitor;
import com.ale.infra.proxy.authentication.AuthenticationResponse;
import com.ale.infra.proxy.authentication.IAuthentication;
import com.ale.listener.IConnectionListener;
import com.ale.listener.ResponseListener;
import com.ale.listener.SigninResponseListener;
import com.ale.listener.SignoutResponseListener;
import com.ale.listener.StartResponseListener;
import com.ale.rainbow.datanetworkmonitor.DataNetworkMonitor;
import com.ale.service.RainbowService;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.util.List;

/**
 * This module allows to connect to the Rainbow Cloud Services.
 * Your application whill have to use this module to be able to sign-in
 * with the user credentials and to listen to the connection state with the Rainbow Cloud Services.
 */

public class Connection
{
    private static final String LOG_TAG = "Connection";

    private ConnectionState m_state;

    private IConnectionListener m_connectionListener = new IConnectionListener() {

        @Override
        public void onSigninSuccessed() {
            notifyOnSigninSucceeded();
        }

        @Override
        public void onSigninFailed(int responseCode, String err) {
            notifyOnSigninFailed(responseCode, err);
        }

        @Override
        public void onXmppCreated() {
            // TODO : find another way to do it
            RainbowSdk.instance().conversations().registerChangeListener();
            RainbowSdk.instance().contacts().registerChangeListener();
            RainbowSdk.instance().im().registerChangeListener();
            RainbowSdk.instance().contacts().registerInvitationChangeListener();
        }

    };

    private SigninResponseListener m_signinResponseListener;
    private SignoutResponseListener signoutResponseListener;

    private ServiceConnection m_rainbowServiceConnection;

    Connection()
    {
        m_state = ConnectionState.RAINBOW_CONNECTIONDISCONNECTED;
    }

    /**
     * Allow to sign-in to Rainbow environment from a Rainbow official user account that already exists
     * ========================================================================
     * @param login Rainbow user login
     * @param password Rainbow user password
     * @param host Rainbow server
     * @param listener Listener used to execute code if sign-in has succeeded or failed <br/>
     *                 - if the signin action is successed, the onSigninSucceeded callback will be called<br/>
     *                 - if the signin action is failed, the onSigninFailed callback will be called <br/>
     *                 In this case, error code will be returned; <br/>
     *                 List of possible error codes;<br/>
     *                      CONNECTION_WRONG_LOGIN_OR_PWD, <br/>
     *                      CONNECTION_ACCOUNT_LOCKED, <br/>
     *                      CONNECTION_ACCOUNT_NOT_ACTIVATE, <br/>
     *                      CONNECTION_COMPANY_NOT_ACTIVATE, <br/>
     *                      CONNECTION_ERROR, <br/>
     *                      NO_NETWORK_CONNECTION
     */
    public void signin(final String login, final String password, final String host, final SigninResponseListener listener) {
        Log.getLogger().info(LOG_TAG, ">signin for " + login);

        if (m_rainbowServiceConnection == null)
            throw new IllegalStateException("You must call start method before signin");

        String exHost = RainbowContext.getPlatformServices().getApplicationData().getHost();

        if (!StringsUtil.isNullOrEmpty(host) && !host.equals(exHost)) {
            resetCacheAndDatabase();
            RainbowContext.getPlatformServices().getApplicationData().setHost(host);
        }

        checkLoginApplication(new ResponseListener() {
            @Override
            public void onSuccess() {
                m_signinResponseListener = listener;

                m_state = ConnectionState.RAINBOW_CONNECTIONINPROGRESS;

                if (isParametersChanged(login, password)) {
                    resetCacheAndDatabase();
                }
                storeParameters(login, password);

                IDataNetworkMonitor dataNetworkMonitor = RainbowContext.getInfrastructure().getDataNetworkMonitor();
                if (dataNetworkMonitor.isDataNetworkAvailable()) {
                    RainbowContext.getInfrastructure().registerConnectionListener(m_connectionListener);
                    RainbowContext.getInfrastructure().startConnectionProcess();
                    m_state = ConnectionState.RAINBOW_CONNECTIONCONNECTED;
                } else {
                    notifyOnSigninFailed(1234, "No network connection. Unable to login.");
                    m_state = ConnectionState.RAINBOW_CONNECTIONDISCONNECTED;
                }
            }

            @Override
            public void onRequestFailed(RainbowSdk.ErrorCode errorCode, String err) {
                notifyOnSigninFailed(0, err);
            }
        });
    }

    /**
     * Allow to sign-in to Rainbow environment from a Rainbow official user account that already exists
     * ========================================================================
     * @param login Rainbow user login
     * @param password Rainbow user password
     * @param listener Listener used to execute code if sign-in has succeeded or failed <br/>
     *                 - if the signin action is successed, the onSigninSucceeded callback will be called<br/>
     *                 - if the signin action is failed, the onSigninFailed callback will be called <br/>
     *                 In this case, error code will be returned; <br/>
     *                 List of possible error codes;<br/>
     *                      CONNECTION_WRONG_LOGIN_OR_PWD, <br/>
     *                      CONNECTION_ACCOUNT_LOCKED, <br/>
     *                      CONNECTION_ACCOUNT_NOT_ACTIVATE, <br/>
     *                      CONNECTION_COMPANY_NOT_ACTIVATE, <br/>
     *                      CONNECTION_ERROR, <br/>
     *                      NO_NETWORK_CONNECTION
     */
    public void signin(String login, String password, SigninResponseListener listener) {
        signin(login, password, null, listener);
    }

    /**
     * Start Rainbow SDK
     *
     * @param listener Listener used to execute code if start has succeeded or failed
     */
    public void start(final StartResponseListener listener)
    {
        Log.getLogger().info(LOG_TAG, ">start Rainbow SDK");

        try
        {
            if (m_rainbowServiceConnection != null)
                RainbowSdk.instance().getContext().unbindService(m_rainbowServiceConnection);
        }
        catch (Exception e)
        {
            Log.getLogger().info(LOG_TAG, "Unbind error: " + e.toString());
        }

        m_rainbowServiceConnection = new ServiceConnection()
        {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service)
            {
                if (listener != null) {
                    listener.onStartSucceeded();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name)
            {
                if (listener != null) {
                    listener.onRequestFailed(RainbowSdk.ErrorCode.CONNECTION_ERROR, "Start service failed");
                }

            }
        };

        Context context = RainbowSdk.instance().getContext();
        context.bindService(new Intent(context, RainbowService.class), m_rainbowServiceConnection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
    }


    /**
     * Allow to sign-out from Rainbow
     * @param listener Listener used to execute code if sign-out has succeeded or failed
     */
    public void signout(SignoutResponseListener listener)
    {
        RainbowContext.getPlatformServices().getApplicationData().setLoggedOut(true);

        RainbowSdk.instance().conversations().unregisterChangeListener();
        RainbowSdk.instance().contacts().unregisterChangeListener();
        RainbowSdk.instance().im().unregisterChangeListener();
        RainbowSdk.instance().contacts().unregisterInvitationChangeListener();

        Log.getLogger().info(LOG_TAG, ">Uninitialize and exit");

        uninitialize();
        IApplicationData applicationData = RainbowContext.getPlatformServices().getApplicationData();
        if (applicationData != null && !RainbowContext.getInfrastructure().isInDebugMode(RainbowSdk.instance().getContext())) {
            applicationData.setUserPassword(null);
        }

        m_state = ConnectionState.RAINBOW_CONNECTIONDISCONNECTED;

        if (listener != null) {
            listener.onSignoutSucceeded();
        }
    }

    public void uninitialize()
    {
        if (RainbowContext.getApplicationState() == RainbowContext.ApplicationState.STOPPED)
        {
            Log.getLogger().info(LOG_TAG, "already uninitializing services");
            return;
        }


        // WearableManager.m_NotificationIsLocalOnly = true;

        Log.getLogger().info(LOG_TAG, "uninitialize services");

        // flag the application is stopping
        RainbowContext.setApplicationState(RainbowContext.ApplicationState.STOPPING);

        if (RainbowContext.getInfrastructure().getState().equals(Infrastructure.InfrastructureState.CONNECTING))
        {
            RainbowContext.getInfrastructure().setState(Infrastructure.InfrastructureState.STOP_ASKED);
            Log.getLogger().verbose(LOG_TAG, "Need to wait IDLE MODE");
            RainbowContext.getInfrastructure().stopConnection(5000);
            Log.getLogger().verbose(LOG_TAG, "End Wait");
        }

        // disconnect connection to service (unbind)
        closeConnectionToService();
        // stop listening to data network changes
        DataNetworkMonitor dataNetworkMonitor = RainbowContext.getInfrastructure().getDataNetworkMonitor();
        if (dataNetworkMonitor != null)
            dataNetworkMonitor.stop();

        //		m_periodicWorkerManager.unregisterWorkers();

        RainbowSdk.instance().uninitializeModules();

        // flag the application is stopped
        RainbowContext.setApplicationState(RainbowContext.ApplicationState.STOPPED);
    }

    private void closeConnectionToService()
    {
        try
        {
            RainbowSdk.instance().getContext().unbindService(m_rainbowServiceConnection);
            m_rainbowServiceConnection = null;
        }
        catch (Exception e)
        {
            Log.getLogger().info(LOG_TAG, "Error unbinding Rainbow service", e);
        }
    }

    /**
     * Get the current connection state with the Rainbow Cloud Services.
     * @return The connection state which can be: RAINBOW_CONNECTIONCONNECTED
     * or RAINBOW_CONNECTIONINPROGRESS or RAINBOW_CONNECTIONDISCONNECTED
     */
    public ConnectionState getState()
    {
        return m_state;
    }

    public boolean isConnected() {
        return m_state.equals(ConnectionState.RAINBOW_CONNECTIONCONNECTED);
    }

    public boolean isDisconnected() {
        return m_state.equals(ConnectionState.RAINBOW_CONNECTIONDISCONNECTED);
    }

    public boolean isInProgress() {
        return m_state.equals(ConnectionState.RAINBOW_CONNECTIONINPROGRESS);
    }

    private void notifyOnSigninSucceeded()
    {
        if (m_signinResponseListener != null)
        {
            m_signinResponseListener.onSigninSucceeded();
            m_signinResponseListener = null;
            RainbowContext.getInfrastructure().unregisterConnectionListener(m_connectionListener);
        }
    }

    private void notifyOnSigninFailed(int responseCode, String err)
    {
        if (m_signinResponseListener != null)
        {
            RainbowSdk.ErrorCode errorCode;
            switch (responseCode)
            {
                case 1234:
                    errorCode = RainbowSdk.ErrorCode.NO_NETWORK_CONNECTION;
                    break;
                case 401500:
                    errorCode = RainbowSdk.ErrorCode.CONNECTION_WRONG_LOGIN_OR_PWD;
                    break;
                case 401501:
                    errorCode = RainbowSdk.ErrorCode.CONNECTION_ACCOUNT_LOCKED;
                    break;
                case 401520:
                    errorCode = RainbowSdk.ErrorCode.CONNECTION_ACCOUNT_NOT_ACTIVATE;
                    break;
                case 401521:
                    errorCode = RainbowSdk.ErrorCode.CONNECTION_COMPANY_NOT_ACTIVATE;
                    break;
                default:
                    errorCode = RainbowSdk.ErrorCode.CONNECTION_ERROR;
                    break;
            }
            m_signinResponseListener.onRequestFailed(errorCode, err);
            m_signinResponseListener = null;
        }
    }


    private boolean isParametersChanged(String login, String password)
    {
        String exUserLogin = RainbowContext.getPlatformServices().getApplicationData().getUserLogin();
        return !login.equals(exUserLogin);
    }

    private void resetCacheAndDatabase()
    {
        if (RainbowContext.getInfrastructure().getContactCacheMgr() != null)
            RainbowContext.getInfrastructure().getContactCacheMgr().clearCachePhoto(true);

        if (RainbowContext.getInfrastructure().getDatabaseMgr() != null)
            RainbowContext.getInfrastructure().getDatabaseMgr().resetDatabase();
    }

    private void storeParameters(String login, String password)
    {
        RainbowContext.getPlatformServices().getApplicationData().setUserLogin(login);
        RainbowContext.getPlatformServices().getApplicationData().setUserPassword(password);

        List<RainbowCredentials> credentials = RainbowContext.getPlatformServices().getApplicationData().getCredentialsList();
        if (credentials != null)
        {
            boolean credFound = false;
            for (RainbowCredentials currentCred : credentials)
            {
                if (currentCred.getLogin().equals(login))
                {
                    currentCred.setPwd(password);
                    credFound = true;
                }
            }
            if (!credFound)
            {
                RainbowCredentials cred = new RainbowCredentials(login, password);
                credentials.add(cred);
            }
            RainbowContext.getPlatformServices().getApplicationData().setCredentialsList(credentials);
        }
        else
        {
            Log.getLogger().error(LOG_TAG, "Deserialization Problem for Credentials list");
        }
    }


    /**
     * If the application is on the official server, check if applicationId and applicationSecret are correct. (not really done yet, wait for prod server)
     *
     * If the application is on sandbox or other host, it's ok
     *
     * @param listener
     */
    private void checkLoginApplication(final ResponseListener listener) {
        if (RainbowSdk.instance().getApplicationId() != null && RainbowSdk.instance().getApplicationSecret() != null) {
            RainbowContext.getInfrastructure().startLoginApplicationProcess(RainbowSdk.instance().getApplicationId(), RainbowSdk.instance().getApplicationSecret(), new IAuthentication.IAuthenticationListener() {
                @Override
                public void onSuccess(AuthenticationResponse response) {
                    Log.getLogger().info(LOG_TAG, "authentication application SUCCESS on: " + RainbowSdk.instance().getHost());
                    RainbowSdk.instance().setInitialized(true);
                    listener.onSuccess();
                }

                @Override
                public void onFailure(RainbowServiceException exception) {
                    Log.getLogger().error(LOG_TAG, "authentication application FAILURE: " + exception);
                    RainbowSdk.instance().setInitialized(true);
//                        listener.onRequestFailed(RainbowSdk.ErrorCode.CONNECTION_ERROR, "authentication application FAILURE: " + exception);
                    listener.onSuccess();
                }
            });
        } else {
            Log.getLogger().info(LOG_TAG, "no applicationId or applicationSecret provided - considered as Rainbow for the moment");
            Log.getLogger().info(LOG_TAG, "authentication application SUCCESS on: " + RainbowSdk.instance().getHost());
            RainbowSdk.instance().setInitialized(true);
            listener.onSuccess();
        }
    }


    private enum ConnectionState
    {
        RAINBOW_CONNECTIONCONNECTED,
        RAINBOW_CONNECTIONINPROGRESS,
        RAINBOW_CONNECTIONDISCONNECTED
    }
}


/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : ICSService.java
 * Summary :
 * *****************************************************************************
 * History
 * 2010-10-04 m.geyer crms00264357 No more events if connection cut by Reverse Proxy
 * 2011/11/08 cebruckn crms00345131 [Crash]-Crash when using other applications
 * 2011/11/29 cebruckn crms00348152 [GUI]-Certificate installation prompt hidden
 */
package com.ale.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.ale.infra.IInfrastructure;
import com.ale.infra.application.RainbowContext;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.util.log.Log;

/**
 * The service running in background.
 */
public class RainbowService extends Service
{
    private static final String LOG_TAG = "RainbowService";

    public static final int EVENT_NOTIFICATION = 123456;

    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private final IBinder m_binder = new LocalBinder();

    @Override
    public void onCreate()
    {
        Log.getLogger().info(LOG_TAG, "onCreate");
        super.onCreate();

        Log.getLogger().clearUserActionLog();

        Log.getLogger().userAction("RainbowService started");

        try
        {
            // Set Foreground Service :
//            Context context = getApplicationContext();
//            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, RainbowIntent.getLauncherIntent(context, StartupActivity.class), 0);
//
//            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context).setWhen(System.currentTimeMillis()).setSmallIcon(R.drawable.biz_failure_status).setContentTitle(context.getString(R.string.app_name)).setContentText(context.getString(R.string.app_name)).setContentIntent(contentIntent).setColor(context.getResources().getColor(R.color.otc_color));
//
//            Notification notification = notificationBuilder.build();
//            notification.flags |= Notification.FLAG_NO_CLEAR;
//
//            startForeground(EVENT_NOTIFICATION, notification);

            setToBackground(RainbowSdk.instance().getNotificationBuilder());

            RainbowSdk.instance().initializeServices();

            initializeInfra();
        }
        catch (Exception e)
        {
            Log.getLogger().error(LOG_TAG, "Unable to initialize infrastructure", e);
        }
    }

    @Override
    public void onDestroy()
    {
        Log.getLogger().userAction("Service stopped");

        final IInfrastructure infrastructure = RainbowContext.getInfrastructure();
        if (infrastructure != null)
        {
            try
            {
                // Logout must not be launched in the main thread
                Thread t = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        infrastructure.shutdownConnection();
                    }
                });
                t.setName("Infrastructure Shutdown");
                t.start();
                t.join();
            }
            catch (InterruptedException e)
            {
                Log.getLogger().error(LOG_TAG, "Problem occured while waiting for the Infrastructure Shutdown thread", e);
            }
        }

        super.onDestroy();
        Log.getLogger().info(LOG_TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        Log.getLogger().debug(LOG_TAG, "onBind");
        return m_binder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        Log.getLogger().debug(LOG_TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    /**
     * Initialize infrastructure.
     */
    private void initializeInfra()
    {
        Log.getLogger().info(LOG_TAG, "initializeInfra");

        RainbowContext.getInfrastructure().run(RainbowSdk.instance().getContext());
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        //RainbowSdk.instance().connection().uninitialize();
        RainbowSdk.instance().connection().uninitialize();
        super.onTaskRemoved(rootIntent);
    }

    /**
     * The Class LocalBinder.
     */
    public class LocalBinder extends Binder
    {
        public void getService()
        {
        }
    }

    public void setToBackground(NotificationCompat.Builder notificationBuilder) {
        Log.getLogger().info(LOG_TAG, "setToBackground");
        if (notificationBuilder == null) {
            Log.getLogger().info(LOG_TAG, "notificationBuilder is null");
        } else {
            Notification notification = notificationBuilder.build();
            notification.flags |= Notification.FLAG_NO_CLEAR;
            notification.priority = Notification.PRIORITY_MIN;
            startForeground(EVENT_NOTIFICATION, notification);
        }
    }

}

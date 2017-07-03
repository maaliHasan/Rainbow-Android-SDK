package com.ale.infra.xmpp;

import android.content.Context;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.xmpp.xep.Command.PushCommandResultIQ;
import com.ale.infra.xmpp.xep.Command.RainbowCommandPush;
import com.ale.infra.xmpp.xep.Command.RainbowEnablePush;
import com.ale.util.StringsUtil;
import com.ale.util.Util;
import com.ale.util.log.Log;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Element;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by georges on 20/10/2016.
 */

public class XmppPushMgr
{
    private static String LOG_TAG = "XmppPushMgr";

    private final AbstractRainbowXMPPConnection m_connection;
    private final String m_serviceName;
    private final Context m_applicationContext;
    private boolean m_pushEnabled = false;

    public XmppPushMgr(Context pContext, AbstractRainbowXMPPConnection connection, String serviceName)
    {
        m_connection = connection;
        m_serviceName = serviceName;
        m_applicationContext = pContext.getApplicationContext();

        ProviderManager.addIQProvider(PushCommandResultIQ.ELEMENT, PushCommandResultIQ.NAMESPACE, new IQProvider()
        {
            @Override
            public Element parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException
            {
                return new PushCommandResultIQ(parser);
            }
        });
    }

    public void activatePushNotification()
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Log.getLogger().verbose(LOG_TAG, ">activatePushNotification");

                String pushToken = RainbowContext.getPlatformServices().getApplicationData().getGooglePushToken();

                if (!StringsUtil.isNullOrEmpty(pushToken))
                {
                    Log.getLogger().verbose(LOG_TAG, "Command for Push activation");

                    RainbowCommandPush cmdIq = new RainbowCommandPush(m_serviceName, pushToken, "android", Util.getDeviceImei(m_applicationContext), true);
                    try
                    {
                        PushCommandResultIQ commandResultIQ = m_connection.createPacketCollectorAndSend(cmdIq).nextResultOrThrow();

                        if (commandResultIQ.isStatusCompleted())
                        {
                            Log.getLogger().verbose(LOG_TAG, "PushCommandResultIQ returned SUCCESS");

                            RainbowEnablePush enablePush = new RainbowEnablePush(m_serviceName, commandResultIQ.getNode(), commandResultIQ.getJid(), commandResultIQ.getSecret());
                            IQ packet = m_connection.createPacketCollectorAndSend(enablePush).nextResultOrThrow();
                            if (packet.getStanzaId().equalsIgnoreCase(enablePush.getStanzaId()))
                            {
                                Log.getLogger().verbose(LOG_TAG, "EnablePush Response received");
                                setPushEnabled(true);
                            }
                            else
                            {
                                Log.getLogger().warn(LOG_TAG, "No response from EnablePush Request");
                            }

                            // >parsePackets; <iq xmlns='jabber:client' from='3838fb41a8804294bfbedc3caa2d5400@jerome-all-in-one-dev-1.opentouch.cloud'
                            // to='3838fb41a8804294bfbedc3caa2d5400@jerome-all-in-one-dev-1.opentouch.cloud/mobile_android_356571062442546'
                            // id='5sv8A-17' type='result'/>
                        }
                        else
                        {
                            Log.getLogger().warn(LOG_TAG, "PushCommandResultIQ returned an ERROR");
                        }
                    }
                    catch (Exception e)
                    {
                        Log.getLogger().warn(LOG_TAG, "Exception while trying to activate Push");
                        e.printStackTrace();
                    }
                }
                else
                {
                    Log.getLogger().warn(LOG_TAG, "PUBSUB for Push activation not possible - No Token available");
                }
            }
        });
        thread.start();
    }

    public void deActivatePushNotification()
    {
        Log.getLogger().verbose(LOG_TAG, ">deActivatePushNotification");

        String pushToken = RainbowContext.getPlatformServices().getApplicationData().getGooglePushToken();

        if (!StringsUtil.isNullOrEmpty(pushToken))
        {
            Log.getLogger().verbose(LOG_TAG, "Command for Push de activation");

            RainbowCommandPush cmdIq = new RainbowCommandPush(m_serviceName, pushToken, "android", Util.getDeviceImei(m_applicationContext), false);
            try
            {
                m_connection.send(cmdIq);
            }
            catch (Exception e)
            {
                Log.getLogger().warn(LOG_TAG, "Exception while trying to de activate Push");
                e.printStackTrace();
            }
        }
        else
        {
            Log.getLogger().warn(LOG_TAG, "PUBSUB for Push activation not possible - No Token available");
        }

        ProviderManager.removeIQProvider(PushCommandResultIQ.ELEMENT, PushCommandResultIQ.NAMESPACE);
    }

    public synchronized boolean isPushEnabled()
    {
        return m_pushEnabled;
    }

    public synchronized void setPushEnabled(boolean pushEnabled)
    {
        this.m_pushEnabled = pushEnabled;
    }
}

/******************************************************************************
 * Copyright © 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : cebruckn 26 août 2011
 * *****************************************************************************
 * Defects
 * 2012/02/14 cebruckn crms00361192 Support HTC Explorer
 * 2012/07/11 cebruckn crms00385280 GS3: mute can be offered in MIC appli
 * 2012/07/11 cebruckn crms00385304 HTC Desire C: the mute does not work
 */

package com.ale.rainbow.phone;

import com.ale.infra.application.RainbowContext;
import com.ale.util.log.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * @author cebruckn
 */
public final class MuteHelper
{
    private static final String LOG_TAG = "MuteHelper";
    private static Set<IMuteSateChangeListener> m_changeListeners = new HashSet<IMuteSateChangeListener>();

    private MuteHelper()
    {
        throw new UnsupportedOperationException();
    }

    public static void registerChangeListener(IMuteSateChangeListener changeListener)
    {
        m_changeListeners.add(changeListener);
    }

    public static void unregisterChangeListener(IMuteSateChangeListener changeListener)
    {
        if (m_changeListeners.contains(changeListener))
        {
            m_changeListeners.remove(changeListener);
        }
    }

    private static void fireMuteStateChanged()
    {
        for (IMuteSateChangeListener listener : m_changeListeners)
        {
            listener.onStateChanged();
        }
    }

    public static void refreshMuteState()
    {
        fireMuteStateChanged();
    }

    public static boolean canDeviceMute()
    {
        return true;
    }

    public static boolean isMuted()
    {
        return RainbowContext.getInfrastructure().getXmppConnection().getTelephonyMgr().isMuted();
    }

    public static void mute(boolean distant)
    {
        Log.getLogger().info(LOG_TAG, "Mute");

        RainbowContext.getInfrastructure().getXmppConnection().getTelephonyMgr().mute(true, distant);

        refreshMuteState();
    }

    public static void unmute()
    {
        Log.getLogger().info(LOG_TAG, "Unmute");

        RainbowContext.getInfrastructure().getXmppConnection().getTelephonyMgr().mute(false, true);

        refreshMuteState();
    }

    public interface IMuteSateChangeListener
    {
        void onStateChanged();
    }
}

/******************************************************************************
 * Copyright © 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : cebruckn 4 oct. 2011
 * *****************************************************************************
 * Defects
 * 2011/10/04 cebruckn crms00340188 the  loudspeaker icon isn't updated in the second screen
 */

package com.ale.rainbow.phone;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

import com.ale.util.log.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * @author cebruckn
 *
 */
public final class LoudspeakerHelper
{
    private static final String LOG_TAG = "LoudspeakerHelper";
    private static Set<ILoudspeakerSateChangeListener> m_changeListeners = new HashSet<ILoudspeakerSateChangeListener>();

    private LoudspeakerHelper()
    {
        throw new UnsupportedOperationException();
    }

    public static void registerChangeListener(ILoudspeakerSateChangeListener changeListener)
    {
        m_changeListeners.add(changeListener);
    }

    public static void unregisterChangeListener(ILoudspeakerSateChangeListener changeListener)
    {
        if (m_changeListeners.contains(changeListener))
        {
            m_changeListeners.remove(changeListener);
        }
    }

    private static void fireLouskeapkerStateChanged()
    {
        for (ILoudspeakerSateChangeListener listener : m_changeListeners)
        {
            listener.onStateChanged();
        }
    }

    public static void refreshLoudspeakerState()
    {
        fireLouskeapkerStateChanged();
    }

    public static boolean canDeviceActiveLoudspeaker()
    {
        // Samsung Galaxy Tab can't activate loudspeaker
        if ("samsung".equalsIgnoreCase(Build.MANUFACTURER) && ("GT-P1000".equalsIgnoreCase(Build.MODEL)))
        {
            return false;
        }

        return true;
    }

    public static void activateLoudspeaker(Context context)
    {
        Log.getLogger().info(LOG_TAG, "activate loudspeaker");

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);

        refreshLoudspeakerState();
    }

    public static void deactivateLoudspeaker(Context context)
    {
        Log.getLogger().info(LOG_TAG, "deactivate loudspeaker");

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(false);

        refreshLoudspeakerState();
    }

    public static boolean isLoudspeakerOn(Context context)
    {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.isSpeakerphoneOn();
    }

    public interface ILoudspeakerSateChangeListener
    {
        void onStateChanged();
    }
}

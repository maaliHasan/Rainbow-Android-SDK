/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : ScreenStateReceiver.java
 * Author  : geyer2 1 oct. 2010
 * Summary : 
 ******************************************************************************
 * History
 * 1 oct. 2010  geyer2
 *  Creation
 * 2010/10/04 m.geyer crms00264357 No more events if connection cut by Reverse Proxy
 * 2010/10/21 m.geyer crms00268833 Event Server re-connection problems
 * 2010/10/29 m.geyer Handle Klocwork warnings 
 * 2010/11/05 M.Geyer crms00272218 if the mobile is in "black screen", the MIC incoming call screen isn't displayed
 * 2010/12/02 cebruckn crms00277912 [Stability]MyIC stops unexpectedly
 * 2010/12/15 M.Geyer crms00280430 EVS connection established if recover 3G while sleeping
 */

package com.ale.rainbow.periodicworker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.platformservices.IScreenStateChangeNotifier;
import com.ale.infra.platformservices.IScreenStateMonitor;
import com.ale.util.log.Log;
import com.commonsware.cwac.wakeful.WakefulIntentService;

import java.util.LinkedList;
import java.util.List;

/**
 * Broadcast receiver to handle Intent.ACTION_SCREEN_OFF and Intent.ACTION_SCREEN_ON. According to
 * screen state, evs connection is activated/deactivated, and polling for logs and static state is
 * adapted.
 * 
 * @author geyer2
 */
public class ScreenStateReceiver extends BroadcastReceiver implements IScreenStateMonitor
{
	/**
	 * Memorizes the current status of device physical screen. True if physical screen is on.
	 * 
	 * We do not retrieve the initial screen state, so consider the screen is on at application
	 * start.
	 */
	private boolean m_isScreenOn = true;
	
	private List<IScreenStateChangeNotifier> m_screenStateChangeNotifierList;
	
	public ScreenStateReceiver()
	{
		super();
		m_screenStateChangeNotifierList = new LinkedList<IScreenStateChangeNotifier>();
	}
	
	/**
	 * Sets new screen state. If state has changed, notifies it to the ones that have subscribed
	 * notifiers.
	 * 
	 * @param isScreenOn
	 *            true to memorize that the screen is on or off.
	 */
	private synchronized void setScreenOn(boolean isScreenOn)
	{
		if (m_isScreenOn != isScreenOn)
		{
			m_isScreenOn = isScreenOn;
			notifyScreenStateChange();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Intent serviceIntent = new Intent(context, ScreenStateService.class);
		serviceIntent.putExtra("action", intent.getAction());
		
		WakefulIntentService.sendWakefulWork(context, serviceIntent);
	}
	
	/**
	 * The Class ScreenStateService.
	 */
	public static class ScreenStateService extends WakefulIntentService
	{
		/**
		 * Instantiates a new screen state service.
		 */
		public ScreenStateService()
		{
			super("ScreenStateService");
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.commonsware.cwac.wakeful.WakefulIntentService#doWakefulWork(android.content.Intent)
		 */
		@Override
		protected void doWakefulWork(Intent intent)
		{
			Log.getLogger().info("ScreenStateService", "doWakefulWork " + intent.getAction());
			
			final String action = intent.getStringExtra("action");
			if (action != null)
			{
				ScreenStateReceiver screenStateReceiver = RainbowContext.getInfrastructure().getScreenStateReceiver();
				if (screenStateReceiver == null)
					return;

				if (action.equals(Intent.ACTION_SCREEN_OFF))
				{
					Log.getLogger().info("ScreenStateService", "doWakefulWork ACTION_SCREEN_OFF");
					screenStateReceiver.setScreenOn(false);
				}
				else if (action.equals(Intent.ACTION_SCREEN_ON))
				{
					Log.getLogger().info("ScreenStateService", "doWakefulWork ACTION_SCREEN_ON");
					screenStateReceiver.setScreenOn(true);
				}
			}
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alu.lite.infra.IScreenStateMonitor#isScreenOn()
	 */
	@Override
	public synchronized boolean isScreenOn()
	{
		return m_isScreenOn;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alu.lite.infra.IScreenStateMonitor#setScreenStateChangeNotifier(com.alu.lite.infra.
	 * IScreenStateChangeNotifier)
	 */
	@Override
	public synchronized void setScreenStateChangeNotifier(IScreenStateChangeNotifier screenStateChangeNotifier)
	{
		m_screenStateChangeNotifierList.add(screenStateChangeNotifier);
	}
	
	private synchronized void notifyScreenStateChange()
	{
		IScreenStateChangeNotifier[] notifiersCopy = m_screenStateChangeNotifierList.toArray(new IScreenStateChangeNotifier[0]);
		
		for (IScreenStateChangeNotifier notifier : notifiersCopy)
		{
			if (m_isScreenOn)
			{
				notifier.notifyScreenOn();
			}
			else
			{
				notifier.notifyScreenOff();
			}
		}
	}
}

/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : PeriodicWorkerManager.java
 * Author  : geyer2 5 oct. 2010
 * Summary : 
 ******************************************************************************
 * History
 * 5 oct. 2010  geyer2
 *  Creation
 *  2010/12/16 cebruckn crms00281196 The refresh polling is not always 10 minutes
 */

package com.ale.rainbow.periodicworker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

import com.ale.infra.platformservices.IPeriodicWorker;
import com.ale.infra.platformservices.IPeriodicWorkerManager;
import com.ale.util.log.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Android platform implementation for IPeriodicWorkerManager. Based on android alarm mechanism.
 * 
 * Works in conjunction with AlarmReceiver and PeriodicWorkerService.
 * 
 * @author geyer2
 */
public class PeriodicWorkerManager implements IPeriodicWorkerManager
{
	private static final String LOG_TAG = "PeriodicWorkerManager";
	
	/**
	 * Constant for adding worker id extra info to the intent sent to the service. This the way the
	 * PeriodicWorkerService will retrieve and execute the corresponding work.
	 */
	public static final String WORKERID_EXTRA_KEY = "periodicWorkerId";
	
	/** Memorizes the pending intents to be able to cancel alarm */
	private Map<String, PendingIntent> m_workers;
	/** Allows to retrieve a worker from the corresponding intent sent to the service */
	private Map<String, IPeriodicWorker> m_intents;
	private Context m_Context;
	private AlarmReceiver m_alarmReceiver;
	
	/**
	 * Instantiates a new periodic worker manager.
	 * 
	 * @param context
	 *            an android context
	 */
	public PeriodicWorkerManager(Context context)
	{
		m_Context = context;
		m_workers = new HashMap<String, PendingIntent>();
		m_intents = new HashMap<String, IPeriodicWorker>();
		
		m_alarmReceiver = new AlarmReceiver();
	}
	
	@Override
	public synchronized void startWorker(IPeriodicWorker worker, long period, String intentName)
	{
		if (worker == null)
		{
			Log.getLogger().error(LOG_TAG, "startWorker : null worker");
			return;
		}

		
		// Look if we already know these worker
		PendingIntent pendingIntent = m_workers.get(intentName);
		
		// If this is a new worker, create corresponding pendingIntent, and memorizes it
		if (pendingIntent == null)
		{
			
			Intent intent = new Intent(intentName);
			pendingIntent = PendingIntent.getBroadcast(m_Context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			m_Context.registerReceiver(m_alarmReceiver, new IntentFilter(intentName));
			
			// Memorizes the worker
			m_workers.put(intentName, pendingIntent);
			m_intents.put(intentName, worker);
		}
		
		// Calculate the moment the alarm should fire the first time
		long firstTime = SystemClock.elapsedRealtime() + period;
		
		// Configure alarm
		AlarmManager alarmManager = (AlarmManager) m_Context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, period, pendingIntent);
	}
	
	@Override
	public synchronized void stopWorker(String intentName)
	{
		if (intentName == null)
		{
			return;
		}
		
		PendingIntent pendingIntent = m_workers.remove(intentName);
		if (pendingIntent != null)
		{
			AlarmManager alarmManager = (AlarmManager) m_Context.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(pendingIntent);
		}
	}
	
	/**
	 * Gets the worker associated to 'intent'.
	 * 
	 * @param action
	 *            the intent to check
	 * @return the corresponding worker, or null if not found
	 */
	public synchronized IPeriodicWorker getWorker(String action)
	{
		if (m_intents.containsKey(action))
		{
			return m_intents.get(action);
		}
		
		Log.getLogger().verbose(LOG_TAG, "getWorker : no worker found");
		
		return null;
	}
	
	public void unregisterWorkers()
	{
		try
		{
			m_Context.unregisterReceiver(m_alarmReceiver);
		}
		catch (Exception e)
		{
			Log.getLogger().warn(LOG_TAG, "unregisterWorkers failed", e);
		}
	}
}

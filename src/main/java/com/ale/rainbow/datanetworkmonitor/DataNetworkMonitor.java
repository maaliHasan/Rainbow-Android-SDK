/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : cebruckn 10 juin 2010
 * Summary : 
 ******************************************************************************
 * History
 * 10 juin 2010  cebruckn
 *  Creation
 *  2010/11/02 cebruckn crms00271405 Sound notification on EVS connection
 *  2010/11/23 cebruckn crms00275945 Sound notification on EVS connection
 *  2011/08/02 cebruckn crms00329518 crash of the application after start it
 *  2012/03/21 cebruckn crms00367546 [OXO]My IC Mobile on Android application crash/does not connect to OXO //prebeta820//
 *  2012/07/11 cebruckn crms00385198 //MyIC Android 4.1//No MyIC reconnection when Data service is lost and comes back.
 *  2013/01/25 cebruckn crms00416517 Unable to release the call in My IC Mobile
 */

package com.ale.rainbow.datanetworkmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.ale.infra.platformservices.IDataNetworkChangedListener;
import com.ale.infra.platformservices.IDataNetworkMonitor;
import com.ale.infra.platformservices.IRoamingDetected;
import com.ale.infra.platformservices.ISeamless3GToWifiRoaming;
import com.ale.util.Duration;
import com.ale.util.log.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Monitors the data links (wifi or 3G), and computes the current 'data' availability.
 * 
 * @author cebruckn
 */
public class DataNetworkMonitor implements IDataNetworkMonitor
{
	private static final String LOG_TAG = "DataNetworkMonitor";
	private Context m_context;
	private boolean m_hasWifi = false;
	
	@Override
	public boolean hasWifi()
	{
		return m_hasWifi;
	}
	
	/** True if 3G available (false if only gprs or wifi available) */
	private boolean m_has3G = false;
	
	/** True if gsm gata available (grps, umts, ...). False if only wifi available. */
	private boolean m_hasCellularData = false;
	
	/** True if wifi or 3G available, false if gprs or no connection */
	private boolean m_hasWIFIor3G = false;
	
	private boolean m_isNetworkUp = false;
	
	private BroadcastReceiver m_connectivityActionReceiver;
	private PhoneStateListener m_dataConnectionStateListener;
	private List<IDataNetworkChangedListener> m_dataChangeNotifierList;
	private List<ISeamless3GToWifiRoaming> m_seamless3gToWifiRoamingNotifierList;
	private IRoamingDetected m_roamingDetectedNotifier;
	private Handler m_dataNetworkAvailabilityHandler = new Handler();
	private Runnable m_dataNetworkAvailabilityRunnable;
	
	/**
	 * Construct a new DataNetworkMonitor.
	 * 
	 * @param context
	 *            the context
	 */
	public DataNetworkMonitor(Context context)
	{
		m_context = context;
		
		m_dataChangeNotifierList = new LinkedList<IDataNetworkChangedListener>();
		m_seamless3gToWifiRoamingNotifierList = new LinkedList<ISeamless3GToWifiRoaming>();
		
		initialization();
		
		// listen to events
		listenDataEvents();
		
		// listen to wifi events
		listenWifiEvents(context);
	}
	
	@Override
	public boolean isDataNetworkAvailable()
	{
		return m_isNetworkUp;
	}
	
	private void initialization()
	{
		// initialize cellular data
		TelephonyManager telephonyManager = (TelephonyManager) m_context.getSystemService(Context.TELEPHONY_SERVICE);
		NetworkType networkType = NetworkType.fromAndroidValue(telephonyManager.getNetworkType());
		setDataAndNetworkType(telephonyManager.getDataState(), networkType);
		
		// initialize wifi
		ConnectivityManager connectivityManager = (ConnectivityManager) m_context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		NetworkInfo info = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		if (info.isConnected())
		{
			m_hasWifi = true;
		}
		else
		{
			m_hasWifi = false;
		}
		
		computeNetworkAvailability(false);
	}
	
	/**
	 * Listen and dispatch the telephony events
	 */
	private void listenDataEvents()
	{
		final TelephonyManager manager = (TelephonyManager) m_context.getSystemService(Context.TELEPHONY_SERVICE);
		
		// Listen to the phone event
		m_dataConnectionStateListener = new PhoneStateListener()
		{
			/*
			 * (non-Javadoc)
			 * 
			 * @seeandroid.telephony.PhoneStateListener# onDataConnectionStateChanged(int)
			 */
			@Override
			public void onDataConnectionStateChanged(int state, int network)
			{
				NetworkType networkType = NetworkType.fromAndroidValue(manager.getNetworkType());
				setDataAndNetworkType(manager.getDataState(), networkType);
				startComputingDataNetworkAvailability(true);
			}
			
		};
		manager.listen(m_dataConnectionStateListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
		
	}
	
	private void listenWifiEvents(Context context)
	{
		m_connectivityActionReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context theContext, Intent intent)
			{
				Log.getLogger().info(LOG_TAG, "onReceive intent CONNECTIVITY_ACTION");
				
				NetworkInfo info = intent.getParcelableExtra("networkInfo");
				
				if (info.getType() == ConnectivityManager.TYPE_WIFI)
				{
					if (info.isConnected())
					{
						Log.getLogger().info(LOG_TAG, "has wifi");
						
						if (m_has3G)
							notifySeamless3GToWifiRoaming();
						
						changeWifiConnected(true);
					}
					else
					{
						Log.getLogger().info(LOG_TAG, "no wifi");
						changeWifiConnected(false);
					}
					
					startComputingDataNetworkAvailability(true);
				}
			}
			
		};
		
		context.registerReceiver(m_connectivityActionReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
	private void changeWifiConnected(boolean connected)
	{
		if (m_hasWifi != connected)
		{
			notifyRoamingDetected();
		}
		
		m_hasWifi = connected;
	}
	
	private void setDataAndNetworkType(int state, NetworkType networkType)
	{
		if (state == TelephonyManager.DATA_CONNECTED)
		{
			m_hasCellularData = true;
			
			if (networkType.isAtLeast3G())
			{
				m_has3G = true;
			}
			else
			{
				m_has3G = false;
			}
		}
		else
		{
			m_hasCellularData = false;
			m_has3G = false;
		}
	}
	
	private void startComputingDataNetworkAvailability(final boolean notify)
	{
		// we need to delay the treatment of 500ms (500ms seems to be sufficient). Sometimes the
		// Android platform is giving us information of data recovery too early => web services done
		// on data recovery can fail with a socketException : Network unreachable
		
		if (m_dataNetworkAvailabilityRunnable != null)
			m_dataNetworkAvailabilityHandler.removeCallbacks(m_dataNetworkAvailabilityRunnable);
		
		m_dataNetworkAvailabilityRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				computeNetworkAvailability(notify);
			}
		};
		
		m_dataNetworkAvailabilityHandler.postDelayed(m_dataNetworkAvailabilityRunnable, Duration.HALF_A_SECOND_IN_MILLISECONDS);
	}
	
	private void computeNetworkAvailability(boolean notify)
	{
		Log.getLogger().debug(LOG_TAG, "computeDataNetworkAvailability");
		
		if (m_hasWifi)
		{
			setNetworkUp(notify);
			setHas3GorWifiUp(notify);
		}
		else
		{
			if (m_hasCellularData)
			{
				setNetworkUp(notify);
				
				if (m_has3G)
				{
					setHas3GorWifiUp(notify);
				}
				else
				{
					set3GandWifiDown(notify);
				}
			}
			else
			{
				setNetworkDown(notify);
				set3GandWifiDown(notify);
			}
		}
	}
	
	private void setNetworkUp(boolean notify)
	{
		if (!m_isNetworkUp)
		{
			Log.getLogger().info(LOG_TAG, "New data connection state : DATA_CONNECTED");
			m_isNetworkUp = true;
			
			if (notify)
			{
				notifyNetworkAvailability(true);
			}
		}
	}
	
	private void notifyNetworkAvailability(boolean isNetworkAvailable)
	{
		IDataNetworkChangedListener[] notifiers = m_dataChangeNotifierList.toArray(new IDataNetworkChangedListener[0]);
		
		for (IDataNetworkChangedListener notifier : notifiers)
		{
			notifier.dataNetworkAvailabilityChanged(isNetworkAvailable);
		}
	}
	
	private void setNetworkDown(boolean notify)
	{
		if (m_isNetworkUp)
		{
			Log.getLogger().info(LOG_TAG, "New data connection state : DATA_DISCONNECTED");
			m_isNetworkUp = false;
			
			if (notify)
			{
				notifyNetworkAvailability(false);
			}
		}
	}
	
	private void setHas3GorWifiUp(boolean notify)
	{
		if (!m_hasWIFIor3G)
		{
			Log.getLogger().info(LOG_TAG, "data connection type changed to 3G or wifi");
			
			m_hasWIFIor3G = true;
			
			if (notify)
			{
				notifyWIFIor3GAvailabilityChanged();
			}
		}
	}
	
	private void set3GandWifiDown(boolean notify)
	{
		if (m_hasWIFIor3G)
		{
			Log.getLogger().info(LOG_TAG, "data connection type changed, no more wifi or 3G");
			
			m_hasWIFIor3G = false;
			
			if (notify)
			{
				notifyWIFIor3GAvailabilityChanged();
			}
		}
	}
	
	public void stop()
	{
		// stop listening to data connection state changes
		TelephonyManager manager = (TelephonyManager) m_context.getSystemService(Context.TELEPHONY_SERVICE);
		manager.listen(m_dataConnectionStateListener, PhoneStateListener.LISTEN_NONE);
		
		if (m_connectivityActionReceiver != null)
		{
			// stop listening to connectivity action
			m_context.unregisterReceiver(m_connectivityActionReceiver);
			m_connectivityActionReceiver = null;
		}
		
		m_dataChangeNotifierList.clear();
		m_seamless3gToWifiRoamingNotifierList.clear();
		m_roamingDetectedNotifier = null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alu.lite.infra.IDataNetworkMonitor#hasData()
	 */
	@Override
	public boolean hasWifiOr3G()
	{
		return m_hasWIFIor3G;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alu.lite.infra.IDataNetworkMonitor#setDataNetworkChangeNotifier(com.alu.lite.infra.
	 * IDataNetworkChangeNotifier)
	 */
	@Override
	public void registerDataNetworkChangedListener(IDataNetworkChangedListener dataNetworkChangeNotifier)
	{
		if (!m_dataChangeNotifierList.contains(dataNetworkChangeNotifier))
			m_dataChangeNotifierList.add(dataNetworkChangeNotifier);
	}

	@Override
	public void unregisterDataNetworkChangedListener(IDataNetworkChangedListener dataNetworkChangeNotifier) {
		m_dataChangeNotifierList.remove(dataNetworkChangeNotifier);
	}

	private void notifyWIFIor3GAvailabilityChanged()
	{
		IDataNetworkChangedListener[] notifiers = m_dataChangeNotifierList.toArray(new IDataNetworkChangedListener[0]);
		
		for (IDataNetworkChangedListener notifier : notifiers)
		{
			notifier.wifiOr3GAvailabilityChanged(m_hasWIFIor3G);
		}
	}
	
	private void notifySeamless3GToWifiRoaming()
	{
		ISeamless3GToWifiRoaming[] notifiers = m_seamless3gToWifiRoamingNotifierList.toArray(new ISeamless3GToWifiRoaming[0]);
		
		for (ISeamless3GToWifiRoaming notifier : notifiers)
		{
			notifier.onSeamless3GToWifiRoaming();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alu.lite.infra.IDataNetworkMonitor#registerSeamless3GToWifiRoamingListener(com.alu.lite
	 * .infra.ISeamless3GToWifiRoaming)
	 */
	@Override
	public void registerSeamless3GToWifiRoamingListener(ISeamless3GToWifiRoaming seamless3gToWifiRoamingNotifier)
	{
		if (!m_seamless3gToWifiRoamingNotifierList.contains(seamless3gToWifiRoamingNotifier))
			m_seamless3gToWifiRoamingNotifierList.add(seamless3gToWifiRoamingNotifier);
	}

	@Override
	public void unregisterSeamless3GToWifiRoamingListener(ISeamless3GToWifiRoaming seamless3gToWifiRoamingNotifier)
	{
		if (m_seamless3gToWifiRoamingNotifierList.contains(seamless3gToWifiRoamingNotifier))
			m_seamless3gToWifiRoamingNotifierList.remove(seamless3gToWifiRoamingNotifier);
	}

	@Override
	public void registerRoamingDetectedListener(IRoamingDetected roamingDetectedNotifier)
	{
		m_roamingDetectedNotifier = roamingDetectedNotifier;
	}
	
	private void notifyRoamingDetected()
	{
		if (m_roamingDetectedNotifier != null)
		{
			m_roamingDetectedNotifier.onRoamingDetected();
		}
	}
}

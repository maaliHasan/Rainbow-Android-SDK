/******************************************************************************
 * Copyright © 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : geyer2 11 ao�t 2011
 ******************************************************************************
 * Defects
 *
 */

package com.ale.rainbowsdk;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;

import com.ale.infra.application.IApplicationData;
import com.ale.infra.manager.INotificationFactory;
import com.ale.infra.platformservices.IDataNetworkMonitor;
import com.ale.infra.platformservices.IDeviceSleepLock;
import com.ale.infra.platformservices.IFileAccessService;
import com.ale.infra.platformservices.IGsmPhone;
import com.ale.infra.platformservices.IJSONEncoder;
import com.ale.infra.platformservices.IJSONParser;
import com.ale.infra.platformservices.IPeriodicWorkerManager;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.platformservices.IScreenStateMonitor;
import com.ale.infra.user.IUserPreferences;
import com.ale.preferences.PreferencesFactory;
import com.ale.rainbow.JSONEncoder;
import com.ale.rainbow.JSONParser;

import org.json.JSONException;

/**
 * @author geyer2
 * 
 */
public class AndroidPlateformServices implements IPlatformServices
{
	private IGsmPhone m_gsmPhone;
	private IGsmPhone m_sipPhone;
	private IGsmPhone m_currentPhone;
	private IDataNetworkMonitor m_dataNetworkMonitor;
	private IPeriodicWorkerManager m_periodicWorkerManager;
	private IScreenStateMonitor m_screenStateMonitor;
	private IDeviceSleepLock m_DeviceSleepLock;
	private PreferencesFactory m_preferencesFactory;
	private IFileAccessService m_fileAccessService;
	private INotificationFactory m_notificationFactory;
	private IUserPreferences m_userPreference;
	private Context m_context;

	public AndroidPlateformServices(Context context) {
		m_context = context;
	}

//	public AndroidPlateformServices() {
//		m_gsmPhone = RainbowContext.getPlatformServices().getGsmPhone();
//		RainbowContext.getPlatformServices().setCurrentPhone(gsmPhone);
//	}

	@Override
	public IGsmPhone getGsmPhone()
	{
		return m_gsmPhone;
	}
	
	public void setGsmPhone(IGsmPhone gsmPhone)
	{
		m_gsmPhone = gsmPhone;
	}
	
	@Override
	public IDeviceSleepLock getDeviceSleepLock()
	{
		return m_DeviceSleepLock;
	}
	
	public void setDeviceSleepLock(IDeviceSleepLock deviceSleepLock)
	{
		m_DeviceSleepLock = deviceSleepLock;
	}
	
	@Override
	public long getElapsedRealtime()
	{
		return SystemClock.elapsedRealtime();
	}
	
	@Override
	public IApplicationData getApplicationData()
	{
 		return m_preferencesFactory.createApplicationData();
	}
	
	@Override
	public IUserPreferences getUserPreferences()
	{
		if( m_userPreference == null) {
			m_userPreference = m_preferencesFactory.createUserPreferences();
		}
		return m_userPreference;
	}
	
	public void setPreferencesFactory(PreferencesFactory preferencesFactory)
	{
		m_preferencesFactory = preferencesFactory;
	}
	
	@Override
	public IFileAccessService getFileAccessService()
	{
		return m_fileAccessService;
	}
	
	public void setFileAccessService(IFileAccessService fileAccessService)
	{
		m_fileAccessService = fileAccessService;
	}
	
	@Override
	public IGsmPhone getSipPhone()
	{
		return m_sipPhone;
	}
	
	public void setSipPhone(IGsmPhone sipPhone)
	{
		m_sipPhone = sipPhone;
	}
	
	@Override
	public IGsmPhone getCurrentPhone()
	{
		return m_currentPhone;
	}
	
	@Override
	public void setCurrentPhone(IGsmPhone currentPhone)
	{
		m_currentPhone = currentPhone;
	}
	
	@Override
	public IJSONParser createJSONParser(String json) throws JSONException
	{
		return new JSONParser(json);
	}

	@Override
	public IJSONEncoder createJSONEncoder()
	{
		return new JSONEncoder();
	}

	@Override
	public int getPlatformVersion()
	{
		return Build.VERSION.SDK_INT;
	}

	@Override
	public INotificationFactory getNotificationFactory()
	{
		return RainbowSdk.instance().getNotificationFactory();
//		if( m_notificationFactory == null)
//		{
//			m_notificationFactory = new NotificationFactory();
//		}
//		return m_notificationFactory;
	}
	
}

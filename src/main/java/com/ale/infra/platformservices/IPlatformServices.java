/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : IPlateformService.java
 * Summary : 
 ******************************************************************************
 * History
 * 2010/10/04 m.geyer crms00264357 No more events if connection cut by Reverse Proxy
 * 2010/12/02 cebruckn crms00277897 MyIC has pbs with 3G Data Link
 */
package com.ale.infra.platformservices;

import com.ale.infra.application.IApplicationData;
import com.ale.infra.manager.INotificationFactory;
import com.ale.infra.user.IUserPreferences;

/**
 * Interface providing specific services.
 * 
 */
public interface IPlatformServices
{
	/**
	 * Get the platform gsm phone
	 * 
	 * @return the gsm phone instance
	 */
	IGsmPhone getGsmPhone();


	/**
	 * Gets the wake lock. Gives the ability to prevent the device to go asleep.
	 * 
	 * @return a wake lock object
	 */
	IDeviceSleepLock getDeviceSleepLock();
	
	/**
	 * Returns number of milliseconds since the system was booted, including deep sleep. This clock
	 * should be used when measuring time intervals that may span periods of system sleep.
	 * 
	 * @return the elapsed realtime in milliseconds
	 */
	long getElapsedRealtime();
	
	/**
	 * Get a way to backup and restore internal application data in a persistent store.
	 * IApplicationData describes the datas that can be backuped.
	 * 
	 * @return an IApplicationData instance
	 */
	IApplicationData getApplicationData();

	IUserPreferences getUserPreferences();

	IFileAccessService getFileAccessService();
	
	IGsmPhone getSipPhone();
	
	IGsmPhone getCurrentPhone();
	
	void setCurrentPhone(IGsmPhone sipPhone);
	
	IJSONParser createJSONParser(String json) throws Exception;
	
	IJSONEncoder createJSONEncoder();
	
	int getPlatformVersion();

	INotificationFactory getNotificationFactory();

	void setDeviceSleepLock(IDeviceSleepLock deviceSleepLock);

}

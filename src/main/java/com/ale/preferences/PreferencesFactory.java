/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : DataStoragePreferencesManager.java
 * Summary : 
 ******************************************************************************
 * History
 * 
 */
package com.ale.preferences;

import com.ale.infra.application.ApplicationData;
import com.ale.infra.application.IApplicationData;
import com.ale.infra.datastorage.IDataStorage;
import com.ale.infra.user.IUserPreferences;
import com.ale.infra.user.UserPreferences;
import com.ale.rainbowsdk.RainbowSdk;

public class PreferencesFactory
{
	private IDataStorage m_dataStorage;
	
	/**
	 * Construct a new PreferenceManager
	 * 
	 * @param dataStorage
	 *            the data storage.
	 */
	public PreferencesFactory(IDataStorage dataStorage)
	{
		m_dataStorage = dataStorage;
	}
	
	public IUserPreferences createUserPreferences()
	{
		return new UserPreferences(m_dataStorage);
	}
	
	public IApplicationData createApplicationData()
	{
		return new ApplicationData(m_dataStorage);
	}
}

/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : DataStorage.java
 * Summary : 
 ******************************************************************************
 * History
 * 2013/11/13 cebruckn crms00466774 [Favorites]-Adapt to OT 2.0 favorites
 */
package com.ale.infra.datastorage;

import android.content.Context;
import android.content.SharedPreferences;

import com.ale.util.log.Log;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * DataStorage allow to store application local properties.
 */
public class DataStorage implements IDataStorage
{
	private static final String LOG_TAG = "DataStorage";

	private static final String CREDENTIALS_SERIALIZATION_FILENAME = "credentials.ser";
	private static final String PGI_OTHER_PHONE_NUMBER_LIST_SERIALIZATION_FILENAME = "pgiOtherPhoneNbList.ser";

	private SharedPreferences m_settings;
	private Context m_context;
	
	public DataStorage(SharedPreferences settings, Context context)
	{
		m_settings = settings;
		m_context = context;
	}
	
	@Override
	public String getValue(String propertyName, String defaultValue)
	{
		return m_settings.getString(propertyName, defaultValue);
	}
	
	@Override
	public int getValue(String propertyName, int defaultValue)
	{
		return m_settings.getInt(propertyName, defaultValue);
	}
	
	@Override
	public boolean getValue(String propertyName, boolean defaultValue)
	{
		return m_settings.getBoolean(propertyName, defaultValue);
	}
	
	@Override
	public void setValue(String propertyName, String value)
	{
		SharedPreferences.Editor editor = m_settings.edit();
		editor.putString(propertyName, value);
		editor.commit();
	}
	
	@Override
	public void setValue(String propertyName, int value)
	{
		SharedPreferences.Editor editor = m_settings.edit();
		editor.putInt(propertyName, value);
		editor.commit();
	}
	
	@Override
	public void setValue(String propertyName, boolean value)
	{
		SharedPreferences.Editor editor = m_settings.edit();
		editor.putBoolean(propertyName, value);
		editor.commit();
	}

	@Override
	public void setCredentialsList(List<RainbowCredentials> credentialsList)
	{
		if( credentialsList.size() > 5 ) {
			credentialsList = new ArrayList<>(credentialsList.subList(0, 5));
		}
		serializeObject(credentialsList, CREDENTIALS_SERIALIZATION_FILENAME);
	}

	@Override
	public List<RainbowCredentials> getCredentialsList()
	{
		ObjectSerializer mySerializer = new ObjectSerializer(m_context);
		FileInputStream serializedListFile = mySerializer.openFileForReading(CREDENTIALS_SERIALIZATION_FILENAME);

		if (serializedListFile == null)
		{
			Log.getLogger().warn(LOG_TAG, "No saved Credentials list found");
			return new ArrayList<>();
		}

		@SuppressWarnings("unchecked")
		List<RainbowCredentials> credentials = (List<RainbowCredentials>) mySerializer.deserializeObjectFromFile(serializedListFile);

		mySerializer.closeFileInputStream(serializedListFile);

		return credentials;
	}

	private void serializeObject(Object objectToSerialize, String serializationFilename)
	{
		if (objectToSerialize != null)
		{
			ObjectSerializer mySerializer = new ObjectSerializer(m_context);
			mySerializer.setObject(objectToSerialize, serializationFilename);
		}
	}


	@Override
	public void clear()
	{
		Log.getLogger().verbose(LOG_TAG, "clear");
		deleteFile(CREDENTIALS_SERIALIZATION_FILENAME);
		deleteFile(PGI_OTHER_PHONE_NUMBER_LIST_SERIALIZATION_FILENAME);
	}

	@Override
	public List<String> getPgiOtherPhoneNumberList() {
		ObjectSerializer mySerializer = new ObjectSerializer(m_context);
		FileInputStream serializedListFile = mySerializer.openFileForReading(PGI_OTHER_PHONE_NUMBER_LIST_SERIALIZATION_FILENAME);

		if (serializedListFile == null)
		{
			Log.getLogger().warn(LOG_TAG, "No saved Other list found");
			return new ArrayList<>();
		}

		@SuppressWarnings("unchecked")
		List<String> otherPhoneNbList = (List<String>) mySerializer.deserializeObjectFromFile(serializedListFile);

		mySerializer.closeFileInputStream(serializedListFile);

		return otherPhoneNbList;
	}

	@Override
	public void setPgiOtherPhoneNumberList(List<String> pgiOtherPhoneNumberList) {
		if( pgiOtherPhoneNumberList.size() > 5 ) {
			pgiOtherPhoneNumberList = new ArrayList<>(pgiOtherPhoneNumberList.subList(1, 5));
		}
		serializeObject(pgiOtherPhoneNumberList, PGI_OTHER_PHONE_NUMBER_LIST_SERIALIZATION_FILENAME);
	}

	private void deleteFile(String filename)
	{
		boolean deleted = m_context.deleteFile(filename);
		Log.getLogger().verbose(LOG_TAG, filename + " deleted=" + deleted);
	}
}

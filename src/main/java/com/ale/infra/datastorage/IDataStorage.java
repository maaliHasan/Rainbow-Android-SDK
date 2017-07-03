/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : IDataStorage.java
 * Summary : 
 ******************************************************************************
 * History
 * 2013/11/13 cebruckn crms00466774 [Favorites]-Adapt to OT 2.0 favorites
 */
package com.ale.infra.datastorage;

import java.util.List;

/**
 * The persistent data storage
 * 
 */
public interface IDataStorage
{
	
	/**
	 * Return a string value from the data storage.
	 * 
	 * @param propertyName
	 *            the property name.
	 * @param defaultValue
	 *            the default value.
	 * @return the string value found or the default value.
	 */
	String getValue(String propertyName, String defaultValue);
	
	/**
	 * Assign the specified value to the specified property.
	 * 
	 * @param propertyName
	 *            the property name.
	 * @param value
	 *            the value.
	 */
	void setValue(String propertyName, String value);
	
	/**
	 * Return an int value from the data storage.
	 * 
	 * @param propertyName
	 *            the property name.
	 * @param defaultValue
	 *            the default value.
	 * @return the int value found or the default value.
	 */
	int getValue(String propertyName, int defaultValue);
	
	/**
	 * Assign the specified value to the specified property.
	 * 
	 * @param propertyName
	 *            the property name.
	 * @param value
	 *            the value.
	 */
	void setValue(String propertyName, int value);
	
	/**
	 * Return a boolean value from the data storage.
	 * 
	 * @param propertyName
	 *            the property name.
	 * @param defaultValue
	 *            the default value.
	 * @return the boolean value found or the default value.
	 */
	boolean getValue(String propertyName, boolean defaultValue);
	
	/**
	 * Assign the specified value to the specified property.
	 * 
	 * @param propertyName
	 *            the property name.
	 * @param value
	 *            the value.
	 */
	void setValue(String propertyName, boolean value);

	void setCredentialsList(List<RainbowCredentials> credentialsList);

	List<RainbowCredentials> getCredentialsList();

	void clear();

    List<String> getPgiOtherPhoneNumberList();

	void setPgiOtherPhoneNumberList(List<String> pgiOtherPhoneNumberList);
}

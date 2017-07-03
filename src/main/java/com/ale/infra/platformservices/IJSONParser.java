/******************************************************************************
 * Copyright © 2013 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : cebruckn 16 d�c. 2013
 ******************************************************************************
 * Defects
 *
 */

package com.ale.infra.platformservices;

import org.json.JSONArray;

import java.util.Date;

/**
 * @author cebruckn
 * 
 */
public interface IJSONParser
{
	
	Long getLong(String field) throws Exception;
	
	Integer getInt(String field) throws Exception;
	
	Double getDouble(String field) throws Exception;
	
	Boolean getBoolean(String field,Boolean defaultValue) throws Exception;
	
	Object get(String field) throws Exception;
	
	String getString(String field) throws Exception;
	
	int getArraySize(String field) throws Exception;
	
	IJSONParser getObjectAtIndex(String field, int index) throws Exception;
	
	Date getDate(String field) throws Exception;
	
	IJSONParser getObject(String field) throws Exception;
	
	String getStringAtIndex(String field, int index) throws Exception;

	JSONArray getJSONArray(String field) throws Exception;
}

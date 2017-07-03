/******************************************************************************
 * Copyright © 2013 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : cebruckn 16 d�c. 2013
 ******************************************************************************
 * Defects
 *
 */

package com.ale.rainbow;

import com.ale.infra.platformservices.IJSONEncoder;
import com.ale.util.log.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author cebruckn
 * 
 */
public class JSONEncoder implements IJSONEncoder
{
	private static final String LOG_TAG = "JSONEncoder";
	
	@Override
	public String encode(String name, String value)
	{
		JSONObject json = new JSONObject();
		
		try
		{
			json.put(name, value);
		}
		catch (JSONException e)
		{
			Log.getLogger().error(LOG_TAG, e.getMessage());
		}
		
		return json.toString();
	}
	
}

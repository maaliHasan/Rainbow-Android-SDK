/******************************************************************************
 * Copyright © 2013 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : cebruckn 16 d�c. 2013
 ******************************************************************************
 * Defects
 *
 */

package com.ale.rainbow;

import com.ale.infra.platformservices.IJSONParser;
import com.ale.infra.utils.WSDateParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * @author cebruckn
 * 
 */
public class JSONParser implements IJSONParser
{
	private JSONObject m_parser;
	
	public JSONParser(String json) throws JSONException
	{
		m_parser = new JSONObject(json);
	}
	
	public JSONParser(JSONObject object)
	{
		m_parser = object;
	}
	
	@Override
	public String getString(String field) throws JSONException
	{
		if (m_parser.has(field) && !m_parser.isNull(field)) {
			String fieldValue = m_parser.getString(field);
			if( "null".equals(fieldValue) )
				return null;

			return fieldValue;
		}
		else
			return null;
	}
	
	@Override
	public Object get(String field) throws JSONException
	{
		if (m_parser.has(field)) {
			String fieldValue = m_parser.getString(field);
			if( "null".equals(fieldValue) )
				return null;

			return fieldValue;
		}
		else
			return null;
	}
	
	@Override
	public Boolean getBoolean(String field,Boolean defaultValue) throws JSONException
	{
		if (m_parser.has(field))
			return m_parser.getBoolean(field);
		else
			return defaultValue;
	}
	
	@Override
	public Double getDouble(String field) throws JSONException
	{
		if (m_parser.has(field))
			return m_parser.getDouble(field);
		else
			return null;
	}
	
	@Override
	public Integer getInt(String field) throws JSONException
	{
		if (m_parser.has(field))
			return m_parser.getInt(field);
		else
			return null;
	}
	
	@Override
	public Long getLong(String field) throws JSONException
	{
		if (m_parser.has(field))
			return m_parser.getLong(field);
		else
			return null;
	}
	
	@Override
	public int getArraySize(String field) throws JSONException
	{
		if (m_parser.has(field))
		{
			JSONArray array = m_parser.getJSONArray(field);
			return array.length();
		}
		
		return 0;
	}
	
	@Override
	public IJSONParser getObjectAtIndex(String field, int index) throws Exception
	{
		JSONArray array = m_parser.getJSONArray(field);
		return new JSONParser(array.getJSONObject(index));
	}
	
	@Override
	public Date getDate(String field) throws Exception
	{
		String date = m_parser.getString(field);
		return WSDateParser.parse(date);
	}
	
	@Override
	public IJSONParser getObject(String field) throws JSONException
	{
		if (m_parser.has(field)) {
			JSONObject jsonObject = m_parser.optJSONObject(field);
			if( jsonObject != null)
				return new JSONParser(jsonObject);
		}
		return null;
	}

	@Override
	public String getStringAtIndex(String field, int index) throws Exception
	{
		JSONArray array = m_parser.getJSONArray(field);
		return array.getString(index);
	}

	@Override
	public JSONArray getJSONArray(String field) throws Exception
	{
		if( m_parser.has(field))
			return m_parser.getJSONArray(field);

		return null;
	}
}

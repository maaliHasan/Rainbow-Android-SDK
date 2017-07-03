/******************************************************************************
 * Copyright © 2014 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : cebruckn 22 juil. 2014
 ******************************************************************************
 * Defects
 *
 */

package com.ale.infra.http;


import java.util.Map;

/**
 * @author cebruckn
 * 
 */
public class RESTResult
{
	private static final String LOG_TAG = "RESTResult";

	private String m_response;
	private Map<String,String> m_headers;

	public RESTResult(String response)
	{
		m_response = response;
	}

	public RESTResult(String response, Map<String,String> headers)
	{
		m_response = response;
		m_headers = headers;
	}

	public Map<String,String> getHeaders()
	{
		return m_headers;
	}


	public String getResponse() {
		return m_response;
	}
}

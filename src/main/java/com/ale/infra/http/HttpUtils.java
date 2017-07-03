/******************************************************************************
 * Copyright © 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : simonc1 4 oct. 2011
 ******************************************************************************
 * Defects
 * 2013/02/27 cebruckn crms00423393 XML Eventing-PhotoURL not anymore propagated
 */

package com.ale.infra.http;

import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author simonc1
 * 
 */
public final class HttpUtils
{
	
	private static final String LOG_TAG = "HttpUtils";
	
	private HttpUtils()
	{
	}
	
	public static boolean isValidUrl(String url)
	{
		// very basic check
		// TODO: improve that (not *that* obvious, btw)
		try
		{
			URL u = new URL(url); // this would check for the protocol
			u.toURI(); // does the extra checking required for validation of URI
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
		
	}
	
	public static byte[] loadByteArrayFromInputStream(InputStream is)
	{
		final int bufSize = 1024;
		byte[] readBuf = new byte[bufSize];
		
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int readCnt = is.read(readBuf);
			
			while (readCnt > 0)
			{
				baos.write(readBuf, 0, readCnt);
				readCnt = is.read(readBuf);
			}
			
			is.close();
			return baos.toByteArray();
		}
		catch (Exception e)
		{
			Log.getLogger().debug(LOG_TAG, "> loadByteArrayFromInputStream, exception: " + e.getMessage());
			return new byte[0];
		}
	}


	public static String getFqdnAndPortFromUrl(String urlStrg)
	{
		StringBuffer fqdnAndPort = new StringBuffer("");
		try
		{
			URL myUrl = new URL(urlStrg);
			
			if (!StringsUtil.isNullOrEmpty(myUrl.getProtocol()))
			{
				fqdnAndPort.append(myUrl.getProtocol());
				fqdnAndPort.append("://");
			}
			else
			{
				fqdnAndPort.append("http://");
			}
			
			if (!StringsUtil.isNullOrEmpty(myUrl.getHost()))
			{
				fqdnAndPort.append(myUrl.getHost());
			}
			else
			{
				return "";
			}
			
			if (myUrl.getPort() != -1)
			{
				fqdnAndPort.append(":");
				fqdnAndPort.append(String.valueOf(myUrl.getPort()));
			}
		}
		catch (MalformedURLException e)
		{
			Log.getLogger().warn(LOG_TAG, "MalformedURLException; " + e.getMessage());
		}
		return fqdnAndPort.toString();
	}
	
	public static String convertStreamToString(InputStream is) throws IOException
	{
		BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, UTF_8));
		StringBuilder responseStrBuilder = new StringBuilder();
		
		String inputStr;
		
		while ((inputStr = streamReader.readLine()) != null)
			responseStrBuilder.append(inputStr);
		
		return responseStrBuilder.toString();
	}
}

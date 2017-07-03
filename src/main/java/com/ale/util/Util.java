/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Summary : 
 ******************************************************************************
 * History
 * 2012/01/03 cebruckn crms00353980 [OXO] Download config files identified by MAC address
 * 2012/05/14 cebruckn crms00375744 micma# No Voicepath during call conversation in MyICMobile Android
 * 2012/07/20 cebruckn crms00386982 Application crashes when taking call automatically with Android 4.1 Jelly Bean
 */
package com.ale.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.webkit.URLUtil;

import com.ale.infra.application.IApplicationData;
import com.ale.infra.application.RainbowContext;

/**
 * Utility class
 * 
 */
public final class Util
{
	private static final int WIFI_RETRY_DELAY = 100;
	private static final int MAX_RETRY = 10;
	private static final String NO_MAC_AVAILABLE = "NO_MAC_ADDRESS_AVAILABLE";
	
	/**
	 * Instantiating utility classes does not make sense. Hence the constructors should either be
	 * private or (if you want to allow subclassing) protected. A common mistake is forgetting to
	 * hide the default constructor.
	 */
	private Util()
	{
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Check if the specified string is a valid url.
	 * 
	 * @param value
	 *            the string to test.
	 * @return true if the url is valid, false otherwise.
	 */
	// TODO URL UTIL : regarder URLUtil pour l'utiliser eventuellement dans HttpUtils
	public static boolean isURL(String value)
	{
		return (URLUtil.isHttpsUrl(value) || URLUtil.isHttpUrl(value));
	}

	/**
	 * Get Rainbow application version (set in manifest.xml)
	 *
	 * @return String
	 */
	public static String getRainbowVersion(Context appctxt)
	{
		try
		{
			PackageInfo pInfo = appctxt.getPackageManager().getPackageInfo("com.ale.rainbow", PackageManager.GET_META_DATA);
			return (pInfo.versionName);
		}
		catch (Exception e)
		{
			return "0";
		}
	}

	/**
	 * Get Device IMEI (NOIMEI if not available)
	 *
	 * @return String
	 */
	public static String getDeviceImei(Context appctxt)
	{
		String deviceId = null;
		if( isPermissionAllowed(appctxt, Manifest.permission.CALL_PHONE)) {
			TelephonyManager telMgr = (TelephonyManager) appctxt.getSystemService(Context.TELEPHONY_SERVICE);
			if (telMgr != null) {
				deviceId = telMgr.getDeviceId();
			}
		}
        if( deviceId == null) {
            WifiManager wifiManager = (WifiManager) appctxt.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wInfo = wifiManager.getConnectionInfo();
            deviceId = wInfo.getMacAddress().replace(":","");
        }
		if( deviceId == null) {
			deviceId = "NOIMEI";
		}

		return deviceId;
	}

	public static boolean isPermissionAllowed(Context appctxt, String androidPermission) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			// This Build is < 6 , you can Access to permission
			return true;
		}
		if (appctxt.checkSelfPermission(androidPermission) == PackageManager.PERMISSION_GRANTED) {
			return true;
		}

		return false;
	}

	public static void removeAllPersistantData()
	{
		clearApplicationData();
	}

	
	private static void clearApplicationData()
	{
		IApplicationData data = RainbowContext.getPlatformServices().getApplicationData();
		
		if (data != null)
			data.clear();
	}

}

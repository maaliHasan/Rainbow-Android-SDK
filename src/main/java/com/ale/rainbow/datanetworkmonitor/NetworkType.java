/******************************************************************************
 * Copyright © 2012 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : geyer2 29 f�vr. 2012
 ******************************************************************************
 * Defects
 *
 */

package com.ale.rainbow.datanetworkmonitor;

import android.telephony.TelephonyManager;

/**
 * Enum that encapsulates TelephonyManager.NETWORK_TYPE_xxxx
 * 
 * Since we compile for api level 8, we don't have definition in TelephonyManager for recent network
 * type.
 * 
 * See new constants from
 * http://developer.android.com/reference/android/telephony/TelephonyManager.html
 */
public enum NetworkType
{
	NETWORK_TYPE_1xRTT(TelephonyManager.NETWORK_TYPE_1xRTT),
	
	/** Either IS95A or IS95B */
	NETWORK_TYPE_CDMA(TelephonyManager.NETWORK_TYPE_CDMA),
	
	NETWORK_TYPE_EDGE(TelephonyManager.NETWORK_TYPE_EDGE),
	
	/** Since API level 11 */
	NETWORK_TYPE_EHRPD(14),
	
	/** EVDO revision 0 */
	NETWORK_TYPE_EVDO_0(TelephonyManager.NETWORK_TYPE_EVDO_0),
	
	/** EVDO revision A */
	NETWORK_TYPE_EVDO_A(TelephonyManager.NETWORK_TYPE_EVDO_A),
	
	/** EVDO revision B. Since API level 9 */
	NETWORK_TYPE_EVDO_B(12),
	
	NETWORK_TYPE_GPRS(TelephonyManager.NETWORK_TYPE_GPRS),
	
	NETWORK_TYPE_HSDPA(TelephonyManager.NETWORK_TYPE_HSDPA),
	
	NETWORK_TYPE_HSPA(TelephonyManager.NETWORK_TYPE_HSPA),
	
	/** HSPA+. Since API level 13 */
	NETWORK_TYPE_HSPAP(15),
	
	NETWORK_TYPE_HSUPA(TelephonyManager.NETWORK_TYPE_HSUPA),
	
	NETWORK_TYPE_IDEN(TelephonyManager.NETWORK_TYPE_IDEN),
	
	/** Since API level 11 */
	NETWORK_TYPE_LTE(13),
	
	NETWORK_TYPE_UMTS(TelephonyManager.NETWORK_TYPE_UMTS),
	
	NETWORK_TYPE_UNKNOWN(TelephonyManager.NETWORK_TYPE_UNKNOWN);
	
	private final int m_networkType;
	
	private NetworkType(int value)
	{
		m_networkType = value;
	}
	
	public int value()
	{
		return m_networkType;
	}
	
	public boolean isAtLeast3G()
	{
		switch (this)
		{
		case NETWORK_TYPE_1xRTT:
			return true;
		case NETWORK_TYPE_CDMA:
			return false;
		case NETWORK_TYPE_EDGE:
			return false;
		case NETWORK_TYPE_EHRPD:
			return true;
		case NETWORK_TYPE_EVDO_0:
			return true;
		case NETWORK_TYPE_EVDO_A:
			return true;
		case NETWORK_TYPE_EVDO_B:
			return true;
		case NETWORK_TYPE_GPRS:
			return false;
		case NETWORK_TYPE_HSDPA:
			return true;
		case NETWORK_TYPE_HSPA:
			return true;
		case NETWORK_TYPE_HSPAP:
			return true;
		case NETWORK_TYPE_HSUPA:
			return true;
		case NETWORK_TYPE_IDEN:
			return false;
		case NETWORK_TYPE_LTE:
			return true;
		case NETWORK_TYPE_UMTS:
			return true;
		case NETWORK_TYPE_UNKNOWN:
			return false;
		default:
			break;
		}
		return false;
	}
	
	/**
	 * Convert a TelephonyManager.NETWORK_TYPE_xxxx received from android platform into a
	 * NetworkType
	 */
	public static NetworkType fromAndroidValue(int network)
	{
		// Search a corresponding ordinal in NetworkType
		for (NetworkType enumValue : values())
		{
			if (enumValue.value() == network)
			{
				return enumValue;
			}
		}
		
		return NetworkType.NETWORK_TYPE_UNKNOWN;
	}
	
}

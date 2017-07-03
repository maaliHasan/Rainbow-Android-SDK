/******************************************************************************
 * Copyright © 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : IDataNetworkMonitor.java
 * Author  : ldouguet 21 mars 2011
 * Summary : 
 ******************************************************************************
 * History
 * 21 mars 2011  ldouguet
 *  Creation
 *  2012/03/21 cebruckn crms00367546 [OXO]My IC Mobile on Android application crash/does not connect to OXO //prebeta820//
 */

package com.ale.infra.platformservices;


/**
 * @author ldouguet
 * 
 */
public interface IDataNetworkMonitor
{
	boolean hasWifi();
	
	boolean hasWifiOr3G();

	void registerDataNetworkChangedListener(IDataNetworkChangedListener dataNetworkChangeNotifier);

	void unregisterDataNetworkChangedListener(IDataNetworkChangedListener dataNetworkChangeNotifier);

	void registerSeamless3GToWifiRoamingListener(ISeamless3GToWifiRoaming seamless3GToWifiRoamingNotifier);

	void unregisterSeamless3GToWifiRoamingListener(ISeamless3GToWifiRoaming seamless3GToWifiRoamingNotifier);
	
	void registerRoamingDetectedListener(IRoamingDetected roamingDetectedNotifier);
	
	boolean isDataNetworkAvailable();
}

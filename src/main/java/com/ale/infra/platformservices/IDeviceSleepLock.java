/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : IWakeLockManager.java
 * Author  : cebruckn 1 d�c. 2010
 * Summary : 
 ******************************************************************************
 * History
 * 1 d�c. 2010  cebruckn
 *  Creation
 *  2010/12/02 cebruckn crms00277897 MyIC has pbs with 3G Data Link
 */

package com.ale.infra.platformservices;

/**
 * @author cebruckn
 * 
 */
public interface IDeviceSleepLock
{
	/*
	 * Acquire the wake lock
	 */
	void acquire();
	
	/*
	 * Release the wake lock
	 */
	void release();
}

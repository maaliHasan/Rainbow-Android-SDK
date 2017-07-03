/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : AlarmReceiver.java
 * Author  : geyer2 5 oct. 2010
 * Summary : 
 ******************************************************************************
 * History
 * 5 oct. 2010  geyer2
 *  Creation
 * 2010/10/29 m.geyer Handle Klocwork warnings 
 * 2010/12/16 cebruckn crms00281196 The refresh polling is not always 10 minutes
 */

package com.ale.rainbow.periodicworker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.platformservices.IPeriodicWorker;
import com.ale.util.StringsUtil;
import com.commonsware.cwac.wakeful.WakefulIntentService;


/**
 * Broadcast receiver to get alarm notification. Works with PeriodicWorkerManager.
 * 
 * Uses cwac-wakeful component (CommonsWare Android Components) http://commonsware.com/cwac
 * 
 * @author geyer2
 */
public class AlarmReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// Filter unwanted intents
		String action = intent.getAction();
		if (StringsUtil.isNullOrEmpty(action))
		{
			return;
		}
		
		// Retrieve the PeriodicWorker corresponding to the current alarm
		// thanks to the intent
		PeriodicWorkerManager workerManager = (PeriodicWorkerManager) RainbowContext.getInfrastructure().getPeriodicWorkerManager();
		
		// This will launch an android service that will execute the work
		// under a wakelock (to prevent the device to go asleep after this method return)
		// This wakelock starts here and is released at sevice end.
		if (workerManager != null && workerManager.getWorker(action) != null)
		{
			WakefulIntentService.sendWakefulWork(context, PeriodicWorkerService.class, action);
		}
	}
}

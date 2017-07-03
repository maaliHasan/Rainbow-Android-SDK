/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : PeriodicWorkerService.java
 * Author  : geyer2 5 oct. 2010
 * Summary : 
 ******************************************************************************
 * History
 * 5 oct. 2010  geyer2
 *  Creation
 *  2010/12/16 cebruckn crms00281196 The refresh polling is not always 10 minutes
 */

package com.ale.rainbow.periodicworker;

import android.content.Intent;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.platformservices.IPeriodicWorker;

import com.ale.util.StringsUtil;
import com.ale.util.log.Log;
import com.commonsware.cwac.wakeful.WakefulIntentService;

/**
 * WakefulIntentService subclass, to execute periodical background work by handle wake lock.
 * 
 * Uses cwac-wakeful component (CommonsWare Android Components) http://commonsware.com/cwac
 * 
 * @author geyer2
 */
public class PeriodicWorkerService extends WakefulIntentService
{
	/**
	 * Instantiates a new background worker service.
	 * 
	 */
	public PeriodicWorkerService()
	{
		super("PeriodicWorkerService");
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.commonsware.cwac.wakeful.WakefulIntentService#doWakefulWork(android.content.Intent)
	 */
	@Override
	protected void doWakefulWork(Intent intent)
	{
		String action = intent.getStringExtra(PeriodicWorkerManager.WORKERID_EXTRA_KEY);
		if (StringsUtil.isNullOrEmpty(action))
		{
			return;
		}
		
		Log.getLogger().debug("PeriodicWorkerService", "doWakefulWork");
		PeriodicWorkerManager workerManager = (PeriodicWorkerManager) RainbowContext.getInfrastructure().getPeriodicWorkerManager();
				IPeriodicWorker worker = workerManager.getWorker(action);
		if (worker != null)
		{
			Log.getLogger().debug("PeriodicWorkerService", "Do the work");
			worker.work();
		}
	}
}

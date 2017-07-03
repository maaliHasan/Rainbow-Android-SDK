/***
	Copyright (c) 2009 CommonsWare, LLC

	Licensed under the Apache License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may obtain
	a copy of the License at
		http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
/*
 * 2010/12/02 cebruckn crms00277912 [Stability]MyIC stops unexpectedly
 * 2010/12/16 cebruckn crms00281196 The refresh polling is not always 10 minutes
 * */
package com.commonsware.cwac.wakeful;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PowerManager;

import com.ale.rainbow.periodicworker.PeriodicWorkerManager;

abstract public class WakefulIntentService extends IntentService
{
	abstract protected void doWakefulWork(Intent intent);

	private static final String LOCK_NAME_STATIC = "com.commonsware.cwac.wakeful.WakefulIntentService";
	private static PowerManager.WakeLock lockStatic = null;

	synchronized private static PowerManager.WakeLock getLock(Context context)
	{
		if (lockStatic == null)
		{
			PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

			lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
			lockStatic.setReferenceCounted(true);
		}

		return (lockStatic);
	}

	public static void sendWakefulWork(Context ctxt, Intent i)
	{
		if (PackageManager.PERMISSION_DENIED == ctxt.getPackageManager().checkPermission("android.permission.WAKE_LOCK", ctxt.getPackageName()))
		{
			throw new RuntimeException("Application requires the WAKE_LOCK permission!");
		}

		getLock(ctxt).acquire();
		ctxt.startService(i);
	}

	// ADDED FOR MICM
	public static void sendWakefulWork(Context ctxt, Class<?> clsService, String action)
	{
		// Add an extra to propagate the worker id
		// in order the service to be able to retrieve the corresponding worker
		Intent intent = new Intent(ctxt, clsService);
		intent.putExtra(PeriodicWorkerManager.WORKERID_EXTRA_KEY, action);
		sendWakefulWork(ctxt, intent);
	}

	public static void sendWakefulWork(Context ctxt, Class<?> clsService)
	{
		sendWakefulWork(ctxt, new Intent(ctxt, clsService));
	}

	public WakefulIntentService(String name)
	{
		super(name);
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		if (!getLock(this).isHeld())
		{ // fail-safe for crash restart
			getLock(this).acquire();
		}

		super.onStart(intent, startId);
	}

	@Override
	final protected void onHandleIntent(Intent intent)
	{
		try
		{
			doWakefulWork(intent);
		}
		finally
		{
			getLock(this).release();
		}
	}
}

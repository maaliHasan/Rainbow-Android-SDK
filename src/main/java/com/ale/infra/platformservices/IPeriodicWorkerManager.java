/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : IPeriodicWorkerManager.java
 * Author  : geyer2 5 oct. 2010
 * Summary : 
 ******************************************************************************
 * History
 * 5 oct. 2010  geyer2
 *  Creation
 *  2010/12/16 cebruckn crms00281196 The refresh polling is not always 10 minutes
 */

package com.ale.infra.platformservices;

/**
 * @author geyer2
 * 
 *         To handle periodical background job (and deals with device sleep mode)
 */
public interface IPeriodicWorkerManager
{
	
	/**
	 * Start a periodical work. Implementation must ensure that the device goes awake when sleeping,
	 * and stay awake during work execution (IPeriodicWorker.work()) This also resets the timer for
	 * an already started worker.
	 * 
	 * @param worker
	 *            The work to be executed periodically
	 * @param period
	 *            The period given in milliseconds
	 * @param workerName
	 *            Worker identification, to be used to stop the worker
	 */
	void startWorker(IPeriodicWorker worker, long period, String workerName);
	
	/**
	 * Stops a worker.
	 * 
	 * @param worker
	 *            the worker to be stopped
	 */
	void stopWorker(String workerName);
}

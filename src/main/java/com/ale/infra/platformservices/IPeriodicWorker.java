/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : IPeriodicWorker.java
 * Author  : geyer2 5 oct. 2010
 * Summary : 
 ******************************************************************************
 * History
 * 5 oct. 2010  geyer2
 *  Creation
 */

package com.ale.infra.platformservices;

/**
 * @author geyer2
 * 
 *         A worker to give to the IPeriodicWorkerManager
 */
public interface IPeriodicWorker
{
	
	/**
	 * The work to be executed periodically.
	 */
	void work();
}

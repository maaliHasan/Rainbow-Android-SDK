/******************************************************************************
 * Copyright © 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : IScreenStateChangeNotifier.java
 * Author  : ldouguet 21 mars 2011
 * Summary : 
 ******************************************************************************
 * History
 * 21 mars 2011  ldouguet
 *  Creation
 */

package com.ale.infra.platformservices;

/**
 * @author ldouguet
 * 
 */
public interface IScreenStateChangeNotifier
{
	void notifyScreenOn();
	
	void notifyScreenOff();
}

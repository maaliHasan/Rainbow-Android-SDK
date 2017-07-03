/******************************************************************************
 * Copyright © 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : IAuthenticateNotifier.java
 * Author  : ldouguet 23 mars 2011
 * Summary : 
 ******************************************************************************
 * History
 * 23 mars 2011  ldouguet
 *  Creation
 */

package com.ale.security.util;

/**
 * @author ldouguet
 * 
 */
public interface IAuthenticateNotifier
{
	void notifyAuthentProvided();
	
	void notifyAuthentCanceled();
}

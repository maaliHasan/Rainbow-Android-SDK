/******************************************************************************
 * Copyright © 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : ICallStateChangedNotifier.java
 * Author  : bcholley 16 mars 2011
 * Summary : 
 ******************************************************************************
 * History
 * 16 mars 2011  bcholley
 *  Creation
 */

package com.ale.infra.platformservices;

/**
 * Defines a general notifier called when the call state has changed (GSM or SIP).
 * 
 * These notify-functions are called whatever the call mode: GSM or SIP
 * 
 * @author bcholley
 * 
 */
public interface ICallStateChangedNotifier
{
	void notifyCallStateChangedIdle();
	
	void notifyCallStateChangedOffHook();
	
	void notifyCallStateChangedRinging(String incomingNumber);
}

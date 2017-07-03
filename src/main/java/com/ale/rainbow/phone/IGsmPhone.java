/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : IGsmPhone.java
 * Summary : 
 ******************************************************************************
 * History
 * 2012/02/14 cebruckn crms00361331 [OXO] MyIC mobile Android - Second call could not be answered
 */
package com.ale.rainbow.phone;

import com.ale.infra.platformservices.ICallStateChangedNotifier;

/**
 * TODO: Rename this interface to something like IPhone (as it is now an interface for both GSM and
 * SIP phone services) The Interface IGsmPhone, services related to the native phone application and
 * the SIP phone application.
 */
public interface IGsmPhone
{
	public enum GsmPhoneState
	{
		CALL_STATE_RINGING, CALL_STATE_IDLE, CALL_STATE_OFFHOOK
	}
	
	/**
	 * Make a (private) call
	 */
	void makeCall(String number);
	
	/**
	 * End gsm call.
	 */
	void endCall();
	
	/**
	 * Answer gsm incoming call.
	 */
	void answerRingingCall();
	
	/**
	 * Disable ringer mode.
	 */
	void disableRingerMode();
	
	/**
	 * Restore ringer mode.
	 */
	void restoreRingerMode();
	
	/**
	 * Reject call (take it and hang it up)
	 */
	void rejectCall();
	
	/**
	 * Set the notifier that will be notified when the call state has changed
	 * 
	 * @param callStateChangedNotifier
	 *            the notifier that will be notify
	 */
	void listenTelephonyEvents(ICallStateChangedNotifier callStateChangedNotifier);
	
	/**
	 * Stop to notify the ICallStateChangedNotifier previously set with
	 * listenTelephonyEvents(ICallStateChangedNotifier callStateChangedNotifier)
	 * 
	 */
	void stopListenTelephonyEvents(ICallStateChangedNotifier callStateChangedNotifier);
	
	/**
	 * Get the state phone
	 * 
	 * @return int corresponding to the
	 */
	GsmPhoneState getState();
	
	/**
	 * Return true if there is no current GSM call ( i.e. current state is CALL_STATE_IDLE)
	 */
	boolean isCallStateIdle();
	
	boolean isCallStateOffhook();
	
	boolean isPhoneAvailable();
}

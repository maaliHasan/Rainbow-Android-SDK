/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Summary : 
 ******************************************************************************
 * History
 * 2010/11/02 cebruckn crms00271405 Sound notification on EVS connection
 * 17/11/2010  LDO   crms00274731 : call attendant issue
 * 2012/07/17 cebruckn crms00386126 //Beta MIC Android R4.1// How to choose the default behaviour for calls from native  dialer?
 * 2013/01/25 cebruckn crms00416517 Unable to release the call in My IC Mobile
 * 2013/06/13 cebruckn crms00441529 [Enhancement]-Automatic start up
 */
package com.ale.infra.user;

/**
 * Preference used in the application.
 * 
 * These preferences contains the user preferences, entered by the user
 */
public interface IUserPreferences
{
	boolean hasToDisplayIncomingScreen();
	
	boolean isUseNotifications();
	
	boolean isUseSmartwatchNotifications();
	
	void setSmartwatchNotifications(boolean value);
	
	boolean isSoundOnNotifications();

	void setSoundOnNotifications(boolean value);

	boolean isVibrateOnNotifications();

	void setVibrateOnNotifications(boolean value);

	String getLoggingLevel();
	
	boolean useCallChooser();
	
	boolean isAutomaticStartActivated();
	
	boolean isVoipActivated();
	
	boolean isCellularVoipActivated();
	
	void setVoipActivated(boolean activated);
	
	String getStartingTab();
	
	String getDisplayNameFormat();

	boolean isUseContactFilteringMode();

	void setUseContactFilteringMode(boolean value);

	String getVoIpRingtone();

	boolean isLargeTextForConversations();

	boolean isCDNUsed();
}

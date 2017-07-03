/******************************************************************************
 * Copyright © 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : geyer2 7 nov. 2011
 ******************************************************************************
 * Defects
 * 2012/07/17 cebruckn crms00386126 //Beta MIC Android R4.1// How to choose the default behaviour for calls from native  dialer?
 * 2012/07/26 cebruckn crms00387824 //Beta MIC Android R4.1// How to choose the default behaviour for calls from native  dialer?
 * 2013/01/25 cebruckn crms00416517 Unable to release the call in My IC Mobile
 * 2013/06/13 cebruckn crms00441529 [Enhancement]-Automatic start up
 */

package com.ale.infra.user;

import com.ale.infra.datastorage.IDataStorage;
import com.ale.infra.application.RainbowContext;

/**
 * @author geyer2
 */
public class UserPreferences implements IUserPreferences
{
	public static final String DISPLAY_INCOMING_PARAM = "rainbow.parameters.display.incoming";
	public static final String LOG_LEVEL_PARAM = "rainbow.parameters.logs.level";
	public static final String USE_CALL_CHOOSER = "rainbow.parameters.use.call.chooser";
	public static final String VOIP_ACTIVATION = "rainbow.parameters.voip.activation";
	public static final String SEND_LOGS_BY_EMAIL = "rainbow.parameters.logs.sendByEmail";
	public static final String OPEN_ANDROID_SETTINGS = "rainbow.parameters.settings.android.open";
	public static final String START_TAB_SELECT = "rainbow.parameters.start.selecttab";
	public static final String DISPLAY_NAME_FORMAT = "rainbow.parameters.display.name";
	public static final String CONTACT_FILTERING_MODE = "rainbow.parameters.contact.use.android.filtering";
	public static final String VOIP_RINGTONE = "rainbow.parameters.voip.ringtone";
	public static final String CELLULAR_VOIP_ACTIVATION = "rainbow.parameters.cellular.voip.activation";
	public static final String USE_SMARTWATCH_PARAM = "rainbow.parameters.smartwatch.notifications";
	private static final String USE_NOTIFICATIONS_PARAM = "rainbow.parameters.use.notifications";
	private static final String VIBRATE_ON_NOTIFICATIONS = "rainbow.parameters.vibrate.notifications";
	private static final String SOUND_ON_NOTIFICATIONS = "rainbow.parameters.sound.notifications";
	private static final String AUTOMATIC_START = "rainbow.parameters.auto.start";
	private static final String LARGE_TEXT_CONVERSATIONS = "rainbow.parameters.largetext.conversations";
	private IDataStorage m_dataStorage;
	
	public UserPreferences(IDataStorage dataStorage)
	{
		m_dataStorage = dataStorage;
	}
	
	@Override
	public boolean hasToDisplayIncomingScreen()
	{
		return m_dataStorage.getValue(DISPLAY_INCOMING_PARAM, false);
	}

	@Override
	public boolean isUseNotifications()
	{
		return m_dataStorage.getValue(USE_NOTIFICATIONS_PARAM, true);
	}

	@Override
	public boolean isUseSmartwatchNotifications()
	{
		return m_dataStorage.getValue(USE_SMARTWATCH_PARAM, false);
	}

	@Override
	public void setSmartwatchNotifications(boolean value)
	{
		m_dataStorage.setValue(USE_SMARTWATCH_PARAM, value);
	}

	@Override
	public boolean isSoundOnNotifications()
	{
		return m_dataStorage.getValue(SOUND_ON_NOTIFICATIONS, false);
	}

	@Override
	public void setSoundOnNotifications(boolean value)
	{
		m_dataStorage.setValue(SOUND_ON_NOTIFICATIONS, value);
	}

	@Override
	public boolean isVibrateOnNotifications()
	{
		return m_dataStorage.getValue(VIBRATE_ON_NOTIFICATIONS, false);
	}

	@Override
	public void setVibrateOnNotifications(boolean value)
	{
		m_dataStorage.setValue(VIBRATE_ON_NOTIFICATIONS, value);
	}

	@Override
	public String getLoggingLevel()
	{
		return m_dataStorage.getValue(LOG_LEVEL_PARAM, "1");
	}
	
	@Override
	public String getStartingTab()
	{
		return m_dataStorage.getValue(START_TAB_SELECT, null);
	}
	
	@Override
	public String getDisplayNameFormat()
	{
		return m_dataStorage.getValue(DISPLAY_NAME_FORMAT, "0");
	}

	@Override
	public boolean isUseContactFilteringMode()
	{
		return m_dataStorage.getValue(CONTACT_FILTERING_MODE, true);

	}

	@Override
	public void setUseContactFilteringMode(boolean value) {
		m_dataStorage.setValue(CONTACT_FILTERING_MODE, value);
	}


	@Override
	public String getVoIpRingtone()
	{
		return m_dataStorage.getValue(VOIP_RINGTONE, null);
	}

	@Override
	public boolean isLargeTextForConversations() {
		return m_dataStorage.getValue(LARGE_TEXT_CONVERSATIONS, false);
	}

	@Override
	public boolean useCallChooser()
	{
		if (RainbowContext.getPlatformServices().getGsmPhone().isPhoneAvailable())
			return m_dataStorage.getValue(USE_CALL_CHOOSER, true);
		else
			return false;
	}

	@Override
	public boolean isAutomaticStartActivated()
	{
		return m_dataStorage.getValue(AUTOMATIC_START, false);
	}
	
	@Override
	public boolean isVoipActivated()
	{
		if (RainbowContext.getPlatformServices().getGsmPhone().isPhoneAvailable())
			return m_dataStorage.getValue(VOIP_ACTIVATION, true);
		else
			return true;
	}
	
	@Override
	public void setVoipActivated(boolean activated)
	{
		m_dataStorage.setValue(VOIP_ACTIVATION, activated);
	}

	@Override
	public boolean isCellularVoipActivated()
	{
		return m_dataStorage.getValue(CELLULAR_VOIP_ACTIVATION, false);
	}
}

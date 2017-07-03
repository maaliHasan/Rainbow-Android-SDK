/******************************************************************************
 * Copyright © 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : geyer2 23 ao�t 2011
 ******************************************************************************
 * Defects
 *
 */

package com.ale.infra.application;

import com.ale.infra.IInfrastructure;
import com.ale.infra.Infrastructure;
import com.ale.infra.contact.ContactFactory;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.util.log.Log;

/**
 * @author geyer2
 * 
 */
public final class RainbowContext
{
	private static final String LOG_TAG = "RainbowContext";

	private static ContactFactory m_contactFactory;

	/**
	 * The application may be in several states
	 */
	public enum ApplicationState
	{
		STOPPING, STOPPED, INITIALIZING, INITIALIZED,
        ONBOARDING_CREATEACCOUNT,
        ONBOARDING_CREATEACCOUNT_EMAILCODE,
		ONBOARDING_CREATEACCOUNT_VCARD,
        ONBOARDING_FORGOTPWD,
        ONBOARDING_FORGOTPWD_EMAILCODE,
        ONBOARDING_INVITED,
        ONBOARDING_INVITED_VCARD,
		ONBOARDING_FINISHED,
		STARTED
	}
	
	private static ApplicationState m_applicationState = ApplicationState.STOPPED;
	private static IInfrastructure m_infrastructure;
	private static IPlatformServices m_platformServices;

	public static ApplicationState getApplicationState()
	{
		return m_applicationState;
	}
	
	public static void setApplicationState(ApplicationState applicationState)
	{
		Log.getLogger().info(LOG_TAG, "Rainbow state = "+applicationState.toString());
		m_applicationState = applicationState;
	}
	
	public static IInfrastructure getInfrastructure()
	{
		if (m_infrastructure == null)
		{
			m_infrastructure = new Infrastructure(m_platformServices);
		}
		
		return m_infrastructure;
	}
	
	public static void setInfrastructure(IInfrastructure infrastructure)
	{
		m_infrastructure = infrastructure;
	}
	
	public static void setPlatformServices(IPlatformServices platformServices)
	{
		m_platformServices = platformServices;
	}
	
	public static IPlatformServices getPlatformServices()
	{
		return m_platformServices;
	}
	
	private RainbowContext()
	{
		// This is a static class
		throw new UnsupportedOperationException();
	}

	public static void setContactFactory(ContactFactory contactFactory)
	{
		m_contactFactory = contactFactory;
	}

	public static ContactFactory getContactFactory()
	{
		return m_contactFactory;
	}
}

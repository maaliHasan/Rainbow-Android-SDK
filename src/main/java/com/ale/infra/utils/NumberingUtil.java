/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Summary : 
 ******************************************************************************
 * History
 * 2010-09-16 M.Geyer crms00260219 [Dev] Update Numbering library
 * 2010-09-16 M.Geyer crms00261121 after using the number #00390676162, the application is closed.
 * 2011-01-05 cebruckn crms00285206 [crash] incoming call
 * 2012-01-17 M.Geyer crms00346201 it's not possible to launching a call with a number wich contains  special characters
 * 2012-02-01 M.Geyer crms00358806 Click-to-call does not work anymore
 * 2012-19-04 M.Geyer crms00371959 [OXO Android] makedialable method of numbering library returns an empty result for number like "+ 33 39067..."
 */
package com.ale.infra.utils;

import com.ale.infra.platformservices.IPlatformServices;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

/**
 * Numbering Utility. The numbering library is executed locally. It is initialized at startup using
 * the getDialingRule of the numbering web service.
 */
public final class NumberingUtil
{
	public static final String PRIVATECALL_ESCAPE = "*";
	public static final String FORCED_DIALABLE_ESCAPE = "#";
	
	/** Dialable char list contains 'x' because of MyTeamwork */
	private static final String DIALABLE_CHARS = "#*+()- 01234567890pw,.x";
	private static final String DIALABLE_CHARS_FIRST_DIGIT = "+*(#";
	private static final String LOG_TAG = "NumberingUtil";
	private static NumberingUtil m_instance = null;
	
	private IPlatformServices m_platformService;
	
	public static boolean isShortNumber(String number, int privatePlanSize)
	{
		Log.getLogger().verbose(LOG_TAG, "isShortNumber " + number);
		
		if (StringsUtil.isNullOrEmptyOrSpaces(number))
		{
			return false;
		}
		
		if (number.length() <= privatePlanSize)
		{
			Log.getLogger().verbose(LOG_TAG, "isShortNumber true");
			return true;
		}
		
		return false;
	}
	
	public static boolean isEmergencyNumber(String number, String emergencyNumbers)
	{
		Log.getLogger().verbose(LOG_TAG, "isEmergencyNumber " + number);
		
		if (StringsUtil.isNullOrEmptyOrSpaces(number) || StringsUtil.isNullOrEmptyOrSpaces(emergencyNumbers))
		{
			return false;
		}
		
		for (String emergency : emergencyNumbers.split(","))
		{
			if (number.equals(emergency))
			{
				Log.getLogger().verbose(LOG_TAG, "isEmergencyNumber true");
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks if a number is an USSD code.
	 * 
	 * @param strPattern
	 *            the number to check
	 * @return true, if is an USSD code
	 */
	public static boolean isUssdCode(String strPattern)
	{
		if (!StringsUtil.isNullOrEmptyOrSpaces(strPattern))
		{
			String strNumber = strPattern.trim();
			
			if (((strNumber.charAt(0) == '*') || (strNumber.charAt(0) == '#')) && (strNumber.charAt(strNumber.length() - 1) == '#'))
			{
				// USSD detected
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isDialable(String number)
	{
		if (StringsUtil.isNullOrEmptyOrSpaces(number))
		{
			Log.getLogger().verbose(LOG_TAG, "not dialable : " + number);
			return false;
		}

		String trimmedStringToCheck = number.trim();

		// Use internal algorithm, as fallback
		return isDialableWithoutNumberingApi(trimmedStringToCheck);
	}

	private static boolean isDialableWithoutNumberingApi(String stringToCheck)
	{
		if (!isNumbersFirstDigitValid(stringToCheck))
		{
			return false;
		}
		
		if (!areAllDigitsDialable(DIALABLE_CHARS, stringToCheck))
		{
			return false;
		}
		
		// 'x' is a valid char for MyTeamwork conference number
		// but should appear only once
		if (StringsUtil.countOccurrencesOfChar(stringToCheck, 'x') > 1)
		{
			return false;
		}
		
		return true;
	}
	
	private static boolean areAllDigitsDialable(String acceptedDialableChars, String stringToCheck)
	{
		for (char car : stringToCheck.toCharArray())
		{
			if (acceptedDialableChars.indexOf(car) == -1)
			{
				return false;
			}
		}
		
		return true;
	}
	
	private static boolean isNumbersFirstDigitValid(String strNumber)
	{
		char firstDigit = strNumber.charAt(0);
		
		// Numbers are ok
		if (Character.isDigit(firstDigit))
		{
			return true;
		}
		
		// Some other chars are ok, but only if there are other digits
		if (DIALABLE_CHARS_FIRST_DIGIT.indexOf(firstDigit) != -1)
		{
			if (strNumber.length() > 1)
			{
				return true;
			}
		}
		
		return false;
	}
}

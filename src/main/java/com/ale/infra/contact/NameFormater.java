/******************************************************************************
 * Copyright � 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : geyer2 21 oct. 2011
 ******************************************************************************
 * Defects
 *
 */

package com.ale.infra.contact;


import com.ale.util.StringsUtil;

/**
 * @author geyer2
 * 
 */
public final class NameFormater
{
	public static String formatAsFirstName(String firstName)
	{
		final String normalizedLowerCaseFirstName = StringsUtil.normalize(firstName).toLowerCase();
		
		return capitalizeSpaceComposedName(capitalizeDashComposedName(normalizedLowerCaseFirstName));
	}
	
	public static String formatAsLastName(String lastName)
	{
		return StringsUtil.normalize(lastName).toUpperCase();
	}
	
	private static String capitalizeComposedName(String displayFirstName, final String separator)
	{
		StringBuilder capitalizedComposedName = new StringBuilder();
		String[] nameParts = displayFirstName.split(separator);
		for (String namePart : nameParts)
		{
			if (namePart.length() != 0)
			{
				appendSeparatorIfNeeded(separator, capitalizedComposedName);
				capitalizedComposedName.append(capitalizeFirstLetter(namePart));
			}
		}
		return capitalizedComposedName.toString();
	}
	
	private static void appendSeparatorIfNeeded(final String separator, StringBuilder capitalizedComposedName)
	{
		if (capitalizedComposedName.length() != 0)
		{
			capitalizedComposedName.append(separator);
		}
	}
	
	private static StringBuilder capitalizeFirstLetter(String word)
	{
		StringBuilder capitalizedWord = new StringBuilder(word);
		if (capitalizedWord.length() > 0)
		{
			capitalizedWord.setCharAt(0, Character.toUpperCase(word.charAt(0)));
		}
		
		return capitalizedWord;
	}
	
	static String capitalizeSpaceComposedName(String name)
	{
		return capitalizeComposedName(name, " ");
	}
	
	static String capitalizeDashComposedName(String name)
	{
		return capitalizeComposedName(name, "-");
	}
	
	private NameFormater()
	{
		throw new UnsupportedOperationException();
	}
}

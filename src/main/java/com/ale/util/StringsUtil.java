/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : StringsUtil.java
 * Author  : ldouguet 9 juil. 2010
 * Summary : 
 ******************************************************************************
 * History
 * 9 juil. 2010  ldouguet
 *  Creation
 */

package com.ale.util;

import com.ale.util.log.Log;
import com.vdurmont.emoji.EmojiParser;

import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * The Class StringsUtil.
 * 
 * @author ldouguet
 */
public final class StringsUtil
{
	private static String LOG_TAG = "StringsUtil";

	/** Represents the empty string "" */
	public static final String EMPTY = "";
	public static String JID_TEL_PREFIX = "tel_";
	public static String JID_ROOM_PREFIX = "room_";

	public static SmileyMgr m_SmyleyMgr = new SmileyMgr();


	/**
	 * Checks if is null or empty.
	 * 
	 * @param pStringToTest
	 *            the string to test
	 * @return return true if the string to test is not null and not empty
	 */
	public static boolean isNullOrEmpty(final String pStringToTest)
	{
		return (pStringToTest == null || (pStringToTest.length() == 0));
	}
	
	/**
	 * Checks if is null or empty.
	 * 
	 * @param pStringToTest
	 *            the string to test
	 * @return return true if the string to test is not null and not empty and does not contain only
	 *         spaces
	 */
	public static boolean isNullOrEmptyOrSpaces(final String pStringToTest)
	{
		return (pStringToTest == null || (pStringToTest.trim().length() == 0));
	}
	
	public static boolean isNullOrEmptyOrSpacesOrEqualsNullString(final String pStringToTest)
	{
		return (isNullOrEmptyOrSpaces(pStringToTest) || pStringToTest.equalsIgnoreCase("null"));
	}
	
	/**
	 * Remove useless spaces : remove leading and trailing spaces of string, and replace multiple
	 * space by one space
	 * 
	 * @param text
	 *            the string to treat
	 * @return the string without the useless spaces
	 */
	public static String removeUselessSpaces(final String text)
	{
		if (text == null)
		{
			return null;
		}
		
		// remove useless spaces at the beginning and end of the string
		StringBuilder pattern = new StringBuilder(text.trim());
		
		// remove useless spaces
		while (true)
		{
			int index = pattern.indexOf("  ");
			
			if (-1 != index)
			{
				replaceAll(pattern, "  ", " ");
			}
			else
			{
				break;
			}
		}
		
		return pattern.toString();
	}
	
	public static void replaceAll(StringBuilder builder, String from, String to)
	{
		int index = builder.indexOf(from);
		while (index != -1)
		{
			builder.replace(index, index + from.length(), to);
			index += to.length(); // Move to the end of the replacement
			index = builder.indexOf(from, index);
		}
	}
	
	/**
	 * String normalization, to avoid null strings unwanted whitespaces
	 * 
	 * @return a copy of the string, with leading and trailing whitespace omitted. Null string is
	 *         converted to empty string
	 */
	public static String normalize(String stringValue)
	{
		if (stringValue == null)
		{
			return StringsUtil.EMPTY;
		}
		
		return stringValue.trim();
	}
	
	/**
	 * Instantiating utility classes does not make sense. Hence the constructors should either be
	 * private or (if you want to allow subclassing) protected. A common mistake is forgetting to
	 * hide the default constructor.
	 */
	private StringsUtil()
	{
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @param myinitials
	 * @return
	 */
	public static String[] split(String myinitials)
	{
		String delims = "[ ]+";
		String[] initials = myinitials.toLowerCase().trim().split(delims);
		// int ctrinit = 1;
		// for (String initial : initials)
		// {
		// Log.getLogger().verbose("StringUtil.split", "initial(" + (ctrinit++) + "); " + initial);
		// }
		return initials;
	}
	
	/**
	 * @param myinitials
	 * @return
	 */
	public static String[] splitfromGreaterToSmaller(String myinitials)
	{
		String[] initials = com.ale.util.StringsUtil.split(myinitials);
		Arrays.sort(initials, new Comparator<String>()
		{
			@Override
			public int compare(String str1, String str2)
			{
				if (str1.length() == str2.length())
					return 0;
				else if (str1.length() > str2.length())
					return -1;
				else
					return 1;
			}
		});
		// int ctrinit = 1;
		// for (String initial : initials)
		// {
		// Log.getLogger().verbose("StringUtil.splitfromGreaterToSmaller", "initial(" + (ctrinit++)
		// + "); " + initial);
		// }
		
		return initials;
	}
	
	public static List<Integer[]> findAllMatchingPositionOfInitials(String name, String initialsStrg)
	{
		Log.getLogger().verbose(LOG_TAG, ">findAllMatchingPositionOfInitials");
		List<Integer[]> stringPositions = new ArrayList<Integer[]>();
		
		String[] initials = com.ale.util.StringsUtil.splitfromGreaterToSmaller(initialsStrg);
		for (String initial : initials)
		{
			Log.getLogger().verbose(LOG_TAG, "initial=" + initial);
			boolean initialFound = false;
			int index = 0;
			while (!initialFound)
			{
				Log.getLogger().verbose(LOG_TAG, "findStartingWordOf(" + name + "," + initial);
				index = com.ale.util.StringsUtil.findStartingWordOf(name, initial, index);
				Log.getLogger().verbose(LOG_TAG, "index=" + index);
				if (index >= 0)
				{
					if (!checkIfIndexAlreadyFound(index, stringPositions))
					{
						Log.getLogger().verbose(LOG_TAG, "Initial found");
						stringPositions.add(new Integer[] { index, index + initial.length() });

						// Replace found initial by space characters
						StringBuilder nameFiltered = new StringBuilder();
						nameFiltered.append(name.substring(0,index));
						nameFiltered.append(StringUtils.repeat("_", initial.length()));
						nameFiltered.append(name.substring(index + initial.length(),name.length()));
						name = nameFiltered.toString();
						initialFound = true;
					}
					else
					{
						index++;
					}
				}
				else if (index == -1)
				{
					Log.getLogger().verbose(LOG_TAG, "initial=" + initial);
					initialFound = true;
				}
			}
		}
		
		return stringPositions;
	}
	
	/**
	 * @param index
	 * @param stringPositions
	 * @return
	 */
	public static boolean checkIfIndexAlreadyFound(int index, List<Integer[]> stringPositions)
	{
		Iterator<Integer[]> iter = stringPositions.iterator();
		while (iter.hasNext())
		{
			Integer[] pos = iter.next();
			if (pos[0] == index)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static int findStartingWordOf(String string, String wordBegining, int beginIndex)
	{
		int index = beginIndex;

		while(index >= 0) {
			index = string.toLowerCase().indexOf(wordBegining.toLowerCase(), index);
			if (index == 0)
				return index;
			if (index > 0 && (string.charAt(index - 1) != ' ') && (string.charAt(index - 1) != '\t')) {
				index++;
				continue;
			}
			return index;
		}

		return -1;
	}
	
	public static int countOccurrencesOfChar(String string, char charToSearch)
	{
		if (com.ale.util.StringsUtil.isNullOrEmpty(string))
		{
			return 0;
		}
		
		int count = 0;
		for (int i = 0; i < string.length(); i++)
		{
			if (string.charAt(i) == charToSearch)
			{
				count++;
			}
		}
		return count;
	}
	
	public static boolean isNumerical(String text)
	{
		if (text == null || text.isEmpty())
			return false;
		
		for (int i = 0; i < text.length(); i++)
		{
			if (!Character.isDigit(text.charAt(i)))
			{
				return false;
			}
		}
		return true;
	}
	
	public static List<Integer[]> findAllMatchingPositionOfString(String msgContent, String state)
	{
		Log.getLogger().verbose("StringsUtil", ">findAllMatchingPositionOfString");
		List<Integer[]> stringPositions = new ArrayList<Integer[]>();
		
		Log.getLogger().verbose("StringsUtil", "initial=" + state);
		int index = 0;
		while (index >= 0)
		{
			Log.getLogger().verbose("StringsUtil", "findStartingWordOf(" + msgContent + "," + state);
			index = com.ale.util.StringsUtil.findStartingWordOf(msgContent, state, index);
			Log.getLogger().verbose("StringsUtil", "index=" + index);
			if (index >= 0)
			{
				if (!checkIfIndexAlreadyFound(index, stringPositions))
				{
					Log.getLogger().verbose("StringsUtil", "Initial found");
					stringPositions.add(new Integer[] { index, index + state.length() });
				}
				else
				{
					index++;
				}
			}
		}
		
		return stringPositions;
	}

	public static String getJidWithoutDevicePartAndTelPart(String jabberId) {
		if (jabberId == null)
			return null;

		String id = jabberId;
		if( id.startsWith(JID_TEL_PREFIX)) {
			id = id.substring(JID_TEL_PREFIX.length(), id.length());
		}
		int index = id.indexOf("/");
		if (index != -1) {
			id = id.substring(0, index);
		}
		return id;
	}

	public static String getJidWithoutDomain(String jabberId) {
		if (jabberId == null)
			return null;

		int index = jabberId.indexOf("@");
		if (index != -1) {
			jabberId = jabberId.substring(0, index);
		}
		return jabberId;
	}

	public static String join(List<String> emails, String separator) {
		StringBuilder strgRes = new StringBuilder();

		for (String email :	emails) {
			if(strgRes.length() > 0) {
				strgRes.append(separator);
			}
			strgRes.append(email);
		}

		return strgRes.toString();
	}

	public static int getMajorVersion(String version) {
		if( isNullOrEmpty(version) )
			return 0;
		String versionSplit[] = version.split("\\.");
		if ( versionSplit.length < 2)
			return 0;

		return Integer.parseInt(versionSplit[0]);
	}

	public static int getMinorVersion(String version) {
		if( isNullOrEmpty(version) )
			return 0;
		String versionSplit[] = version.split("\\.");
		if ( versionSplit.length < 2)
			return 0;

		return Integer.parseInt(versionSplit[1]);
	}

	public static String convertSmileyAndEmoji(String input) {

		String smileyUnicode = m_SmyleyMgr.convertSmileyString(input);

		return EmojiParser.parseToUnicode(smileyUnicode);
	}


	public static  String convertStreamToString(Class context, String filename) {
		InputStream is = context.getResourceAsStream(filename);
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	public static  String convertStreamToString(InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	public static String getMD5Hash (String stringToHash) {
		MessageDigest m = null;

		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "";
		}

		m.update(stringToHash.getBytes(),0,stringToHash.length());
		String hash = new BigInteger(1, m.digest()).toString(16);
		return hash;
	}

}

/******************************************************************************
 * Copyright � 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : geyer2 12 juil. 2011
 ******************************************************************************
 * Defects
 * 2012/01/04 cebruckn crms00354119 [Favorites]-Wrong detail sheet displayed
 * 2012/01/05 cebruckn crms00354206 [Favorite] Some cached contact information are lost after contact details on a favorite (photo)
 * 2012/02/22 cebruckn crms00362776 [Favorites] MIC IM address resolution is case sensitive
 * 2012/02/23 M.Geyer  crms00363091 Anonymous call-log resolved as another user
 * 2012/04/03 cebruckn crms00369816 MyIC Mobile Android 4.1: [VVM] Bad user match in voice mail log when no "from" field in ICS element
 * 2012/04/04 M.Geyer  crms00369878 OT 1.1 Mobility- The number of calling party which set secred id can be seen on MyIC mobile Android
 * 2013/01/09 cebruckn crms00414373 Call Grouping not 100% accurate
 */

package com.ale.infra.contact;


/**
 * @author geyer2
 * 
 */
public class ContactFactory
{
	public IContact createCorporateContact()
	{
		IContact contact = new DirectoryContact();
		return contact;
	}

	public IContact createNativeContact()
	{
		IContact contact = new LocalContact();
		return contact;
	}
}

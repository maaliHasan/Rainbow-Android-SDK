/******************************************************************************
 * Copyright � 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : cebruckn 28 juil. 2011
 ******************************************************************************
 * Defects
 *
 */

package com.ale.infra.contact;

import java.util.List;

/**
 * @author cebruckn
 * 
 */
public interface IContactsCache
{
	void clear();
	
	void put(IContact contactToAdd);
	
	int getCacheSize();
	
	List<IContact> searchByInitials(String[] initials, boolean filterContactsWithoutImAddress);
	
	IContact searchOneContactByNumber(String phoneNumberToSearch);
	
	IContact searchOneContactByIM(String imAddress);
	
	IContact searchOneContactByEmail(String emailAddress);
	
	void remove(IContact contactToRemove);
}

/******************************************************************************
 * Copyright � 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : cebruckn 29 juin 2011
 ******************************************************************************
 * Defects
 * 2012/04/17 cebruckn crms00371718 [OXO] MyIC Android - Wrong handling of favorite numbers
 * 2012/07/11 cebruckn crms00385286 Bad photo displayed on a favorite
 */

package com.ale.infra.searcher.localContact;

import com.ale.infra.contact.IContact;

import java.util.List;

/**
 * @author cebruckn
 * 
 */
public interface ILocalContactSearcher
{
	List<IContact> searchByName(String pattern);

	List<IContact> searchAllWithEmail();

	List<IContact> searchAndroidFilteredAndWithEmail();

	void retrieveAllVisibleGroups();


	interface IlocalContactSearcherListener
	{
		void onSuccess(List<IContact> results);
		void onFailure();
	}
}

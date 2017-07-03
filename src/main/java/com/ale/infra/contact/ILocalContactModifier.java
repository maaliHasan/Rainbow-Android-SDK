/******************************************************************************
 * Copyright � 2012 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : cebruckn 20 janv. 2012
 ******************************************************************************
 * Defects
 *
 */

package com.ale.infra.contact;


/**
 * @author cebruckn
 * 
 */
public interface ILocalContactModifier
{
	void addImAddressToContact(IContact contact, String imAddress);
}

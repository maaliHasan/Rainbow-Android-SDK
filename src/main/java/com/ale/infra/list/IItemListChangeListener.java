/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : ItemListChangeListener.java
 * Summary : 
 ******************************************************************************
 * History
 * 
 */
package com.ale.infra.list;

/**
 * change when the list is updated
 * 
 */
public interface IItemListChangeListener
{
	
	/**
	 * Called when a set of data changed.
	 */
	void dataChanged();
}

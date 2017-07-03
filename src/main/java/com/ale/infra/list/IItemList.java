/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : ItemList.java
 * Summary : 
 ******************************************************************************
 * History
 * 2011/08/31 cebruckn crms00334343 application shutdown after an outgoing call from the missed call.
 */
package com.ale.infra.list;

import java.util.List;

/**
 * A list of item.
 * 
 * @param <T>
 *            the generic type
 */
public interface IItemList<T>
{
	
	/**
	 * Get the number of item in the list.
	 * 
	 * @return the number of items.
	 */
	int getCount();
	
	/**
	 * Get the element at the specified position.
	 * 
	 * @param position
	 *            the position
	 * @return the t
	 */
	T get(int position);
	
	/**
	 * Register a change listener.
	 * 
	 * @param changeListener
	 *            the change listener
	 */
	void registerChangeListener(IItemListChangeListener changeListener);
	
	void unregisterChangeListener(IItemListChangeListener changeListener);
	
	List<T> getItems();
	
	void update(T item);

	void deleteWithoutNotification(T item);

	boolean isRefreshNeeded();
	
	void delete(T item);
	
	void delete(List<T> items);
	
	void clear();
	
	List<T> getCopyOfDataList();

	T[] toArray();
}

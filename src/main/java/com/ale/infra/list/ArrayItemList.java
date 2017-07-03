/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : AbstractItemList.java
 * Summary : 
 ******************************************************************************
 * History
 * 2010/11/10 M.Geyer   crms00273211  Call log consultation when disconnected
 * 2011/08/31 cebruckn crms00334343 application shutdown after an outgoing call from the missed call.
 * 2012/08/27 cebruckn crms00392696 //MIC_Android_R4.1// MIC crash during voicemail message deletion
 */
package com.ale.infra.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract implementation of a item list.
 * 
 * @param <T>
 *            the generic type
 * 
 */
public class ArrayItemList<T> implements IItemList<T>, Serializable
{
	private static final long serialVersionUID = -435803684353398657L;
	
	private transient List<IItemListChangeListener> m_changeListeners = new ArrayList<IItemListChangeListener>();
	protected List<T> m_items = new ArrayList<T>();
	
	/**
	 * Flag to know if list must be refreshed (from ics). A begin, the list is empty, so initialized
	 * to false
	 */
	private boolean m_isRefreshNeeded = false;
	
	@Override
	public List<T> getItems()
	{
		return m_items;
	}

	@Override
	public synchronized T get(int position)
	{
		// In order to avoid concurrent access to the list (modification by proxy and display
		// by GUI), the GUI must avoid use this method to directly access to data. GUI must use
		// getCopyOfDataList() instead.
		return m_items.get(position);
	}

	@Override
	public synchronized int getCount()
	{
		return m_items.size();
	}

	@Override
	public void registerChangeListener(IItemListChangeListener changeListener)
	{
		if (!m_changeListeners.contains(changeListener))
			m_changeListeners.add(changeListener);
	}
	
	@Override
	public void unregisterChangeListener(IItemListChangeListener changeListener)
	{
		if (m_changeListeners.contains(changeListener))
			m_changeListeners.remove(changeListener);
	}
	
	/**
	 * Fire a data change event.
	 */
	public void fireDataChanged()
	{
		IItemListChangeListener[] m_listenersCopy = m_changeListeners.toArray(new IItemListChangeListener[0]);

		if(m_listenersCopy !=null && m_listenersCopy.length!=0){
			for (IItemListChangeListener listener : m_listenersCopy)
			{
				if(listener!=null) {
					listener.dataChanged();
				}
			}
		}
	}
	
	/**
	 * Clear the items list.
	 */
	@Override
	public void clear()
	{
		Boolean hasChanged = false;
		synchronized (this) {
			if (!m_items.isEmpty()) {
				m_items.clear();
				hasChanged = true;
			}
		}
		if (hasChanged)
			fireDataChanged();
	}

	public void add(T item)
	{
		if (item != null)
		{
			synchronized (this) {
				m_items.add(item);
			}
			fireDataChanged();
		}
	}

	public void addWithoutNotification(T item)
	{
		if (item != null)
		{
			synchronized (this) {
				m_items.add(item);
			}
		}
	}
	
	public void uniqueAdd(T item)
	{
		Boolean hasChanged = false;
		synchronized (this) {
			if ((item != null) && !m_items.contains(item)) {
				m_items.add(item);
				hasChanged = true;
			}
		}
		if (hasChanged)
			fireDataChanged();

	}

	/**
	 * Modify the element at T.
	 * 
	 * @param item
	 *            the item
	 */
	@Override
	public void update(T item)
	{
		synchronized (this) {
			int position = m_items.indexOf(item);
			if (position >= 0) {
				m_items.set(position, item);
			}
		}
		
		fireDataChanged();
	}

	/**
	 * Delete the specified item.
	 *
	 * @param item
	 *            the item to remove.
	 */
	@Override
	public void delete(T item)
	{

		Boolean hasChanged = false;
		synchronized (this) {
			hasChanged = m_items.remove(item);
		}
		if (hasChanged)
		{
			fireDataChanged();
		}
	}

	/**
	 * Delete the specified item.
	 *
	 * @param item
	 *            the item to remove.
	 */
	@Override
	public synchronized void deleteWithoutNotification(T item)
	{
		m_items.remove(item);
	}

	public void replaceAll(List<T> items)
	{
		synchronized (this) {
			m_items.clear();
			m_items.addAll(items);
		}
		
		fireDataChanged();
	}

	public void replaceAllWithoutNotification(List<T> items)
	{
		synchronized (this) {
			m_items.clear();
			m_items.addAll(items);
		}
	}

	public void setRefreshNeeded(boolean isRefreshNeeded)
	{
		m_isRefreshNeeded = isRefreshNeeded;
	}
	
	@Override
	public boolean isRefreshNeeded()
	{
		return m_isRefreshNeeded;
	}
	
	public void addAll(List<T> udaResults)
	{
		synchronized (this) {
			m_items.addAll(udaResults);
		}
		fireDataChanged();
	}
	
	@Override
	public synchronized List<T> getCopyOfDataList()
	{
		return new ArrayList<T>(m_items);
	}
	
	@Override
	public void delete(List<T> items)
	{
		int somethingHasChanged = 0;
		synchronized (this) {
			for (T item : items) {
				if (m_items.remove(item)) {
					somethingHasChanged ++;
				}
			}
		}
		if (somethingHasChanged > 0)
		{
			fireDataChanged();
		}
	}

	@Override
	public synchronized T[] toArray()
	{
		return (T[])m_items.toArray();
	}
}

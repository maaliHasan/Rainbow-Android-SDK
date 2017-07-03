Class ArrayItemList
===
---------

The Rainbow SDK offers the **ArrayItemList** implementation which allows to easily detect when data of a list are modified.  
The modification is detected when:  

- the list is cleared,
- an item has been added, updated or deleted,
- or all items are replaced.

However, when a property of an object in the list changes, the modification is not detected.  
For example, if you are listening to the conversations list, if a contact is updated, you are not triggered. 

Besides, you don't have the information of the modification but most of the time in Android, you don't need it to update an adapter.  
Basically, you just have to detect the modification, replace all items and then update with **notifyDataSetChanged**.

### Example: manage conversations list changes

You have to create an IItemListChangeListener object with a single overrided method:

	IItemListChangeListener m_changeListener = new IItemListChangeListener() {
		@Override
		public void dataChanged() {
			m_activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					m_adapter.updateConversations();
				}
			});

		}
	}

Register to the list changes:

	RainbowSdk.instance().conversations().getAllConversations().registerChangeListener(m_changeListener);

Don't forget to unregister:

	RainbowSdk.instance().conversations().getAllConversations().unregisterChangeListener(m_changeListener);

And then, in the adapter:

	public void updateConversations() {
		m_conversations = RainbowSdk.instance().conversations().getAllConversations();
		m_adapter.notifyDataSetChanged(); // Update GUI
	}

NB: If you need to work on the list, prefer doing it on a copy with the method **getCopyOfDataList()**.
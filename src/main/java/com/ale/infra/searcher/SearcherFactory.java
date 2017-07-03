package com.ale.infra.searcher;


import android.content.Context;
import android.provider.ContactsContract;

import com.ale.infra.searcher.localContact.ILocalContactSearcher;
import com.ale.infra.searcher.localContact.LocalContactContentObserver;
import com.ale.infra.searcher.localContact.LocalContactSearcher;
import com.ale.rainbow.BitmapConverter;

public class SearcherFactory {


    public ILocalContactSearcher createContactSearcher(Context context) {
        return new LocalContactSearcher(context, new BitmapConverter());
    }

    public ContactCacheSearcher createContactCacheSearcher() {
        return new ContactCacheSearcher();
    }

    public ConversationSearcher createConversationSearcher() {
        return new ConversationSearcher();
    }

    public GroupSearcher createGroupSearcher() {
        return new GroupSearcher();
    }

    public ILocalContactSearcher createContactWithOfficeNumberSearcher(Context context) {
        return null;
    }

    public void createContactObserver(Context context)
    {
        context.getContentResolver()
                .registerContentObserver(
                        ContactsContract.Contacts.CONTENT_URI, false,
                        new LocalContactContentObserver());
    }

    public RoomSearcher createRoomSearcher() {
        return new RoomSearcher();
    }
}

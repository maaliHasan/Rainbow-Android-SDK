/******************************************************************************
 * Copyright � 2012 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : franci11 11 janv. 2012
 * *****************************************************************************
 * Defects
 */

package com.ale.infra.contact;


import android.content.Context;

import com.ale.infra.list.ArrayItemList;
import com.ale.infra.manager.IInvitationMgr;
import com.ale.infra.manager.room.Room;
import com.ale.infra.searcher.IDisplayable;
import com.ale.infra.xmpp.XmppConnection;
import com.ale.infra.xmpp.xep.ILastActivityNotification;

import java.util.List;
import java.util.Set;

/**
 * @author franci11
 *
 */
public interface IContactCacheMgr {
    void initContext(Context m_applicationContext);

    ArrayItemList<Contact> getContacts();

    Contact getContactFromJid(String jabberId);

    ArrayItemList<Contact> getRosters();

    ArrayItemList<Contact> getRostersWithoutBot();

    ArrayItemList<Contact> getConnectedContacts();

    void downloadContactAvatar(Contact contact, boolean forceReload);

    Contact getUser();

    ContactCacheFileMgr getContactsCacheFile();

    void addNewContact(Contact contact);

    void updateListAndSortIt();

    void searchDelayedByFullName(String fullName, boolean searchLocalContacts,
                                 boolean searchConversations, boolean searchGroups, boolean searchCompanies, boolean searchRooms, IContactSearchListener listener);

    void abortSearchDelayed();

    ArrayItemList<IDisplayable> getSearchedContacts();

    void clearSearchedContacts();

    void retrieveMobileLocalContacts();

    void setObserver(XmppConnection connection);

    Contact getContactFromId(String contactId);

    Contact getContactFromEmail(Set<EmailAddress> addresses);

    Contact getContactFromEmail(String email);

    Contact getContactFromCorporateId (String corporateId);

    Contact createContactIfNotExistOrUpdate(DirectoryContact dirContact);

    void setDirectoryContact(Contact contact, DirectoryContact directoryContact);

    void clearMobileLocalContacts();

    void resolveDirectoryContacts(List<Contact> contacts);

    void resolveDirectoryContactById(String roomId, String contactId, Room.IRoomParticipantListener roomParticipantListener);

    void clearCachePhoto(boolean refreshContactPhotos);

    ArrayItemList<Contact> getRosterAndLocal();

    void getUserSettings();

    List<Contact> getRostersWithoutSubscription();

    void getBots();

    boolean isLoggedInUser(Contact contact);

    boolean isContactInformationVisible(Contact contact);

    boolean checkPhotoCacheDateValidity(String photoFileName, String lastAvatarUpdateDateStrg);

    void setInvitationMgr(IInvitationMgr invitationMgr);

    void refreshUser();

    void refreshContactLastActivityDate(Contact contact, ILastActivityNotification lastActivityNotification);

    void resolveContactFromDB(DirectoryContact dirContact);

    void setUser();

    void removeObserver(XmppConnection m_connection);
}

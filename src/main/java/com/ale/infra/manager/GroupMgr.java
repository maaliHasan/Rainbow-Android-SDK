package com.ale.infra.manager;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.Group;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.proxy.directory.IDirectoryProxy;
import com.ale.infra.proxy.group.IGroupProxy;
import com.ale.infra.xmpp.XmppConnection;
import com.ale.util.log.Log;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by wilsius on 12/10/16.
 */

public class GroupMgr implements IGroupMgr, XmppContactMgr.XmppContactMgrListener {
    private static final String LOG_TAG = "GroupMgr";

    private final IContactCacheMgr m_contactCacheMgr;
    private final IDirectoryProxy m_directory;
    private ArrayItemList<Group> m_allUserGroups;
    private IGroupProxy m_groupProxy;

    public GroupMgr(IContactCacheMgr contactCacheMgr, IDirectoryProxy directory, IGroupProxy groupProxy) {
        m_contactCacheMgr = contactCacheMgr;
        m_directory = directory;
        m_groupProxy = groupProxy;

        m_allUserGroups = new ArrayItemList<>();
    }

    @Override
    public void refreshUserGroupList() {
        Log.getLogger().verbose(LOG_TAG, ">refreshUserGroupList");

        RainbowContext.getInfrastructure().getGroupProxy().getAllUserGroups(null, 0, 100, new IGroupProxy.IGetAllUserGroupsListener() {
            @Override
            public void onGetAllUsersGroupsSuccess(ArrayItemList<Group> groups) {
                Log.getLogger().verbose(LOG_TAG, ">onGetAllUsersGroupsSuccess" );
                setGroup(groups);
            }

            @Override
            public void onGetAllUsersGroupsFailed() {
                Log.getLogger().error(LOG_TAG, ">onGetAllUsersGroupsFailed" );
            }
        });
    }

    public Group findGroupById (String groupId) {
        for (Group grp: m_allUserGroups.getCopyOfDataList()) {
            if (grp.getId().equals(groupId))
                return grp;
        }
        return null;
    }

    public ArrayItemList<Group> getGroups() {
        return m_allUserGroups;
    }

    public void addNewGroup(String groupId) {
        Log.getLogger().verbose(LOG_TAG, ">addNewGroup: " + groupId );
        Group group = new Group(true);
        group.setId(groupId);
        m_allUserGroups.add(group);
        getGroupInfo(group);
    }

    public void deleteGroup(String groupId) {
        Log.getLogger().verbose(LOG_TAG, ">deleteGroup: " + groupId );
        Group group = findGroupById(groupId);
        if (group != null)
            m_allUserGroups.delete(group);
    }

    public void memberHasChanged(String groupId) {
        Group group = findGroupById(groupId);
        if (group != null)
            getGroupInfo(group);
    }

    private void getGroupInfo( final Group group) {
        Log.getLogger().verbose(LOG_TAG, ">getGroupInfo");

        RainbowContext.getInfrastructure().getGroupProxy().getGroup(group.getId(), new IGroupProxy.IGetGroupListener() {
            @Override
            public void onGetGroupSuccess(Group grp) {
                Log.getLogger().verbose(LOG_TAG, ">onGetGroupSuccess");
                group.setOwner(grp.getOwner());
                group.setCreationDate(grp.getCreationDate());
                group.setName(grp.getName());

                if (grp.getGroupMembers() != null) {
                    group.setGroupMembers(grp.getGroupMembers());
                    resolveGroupMembers(group);
                }
                m_allUserGroups.fireDataChanged();
            }

            @Override
            public void onGetGroupFailed() {
                Log.getLogger().error(LOG_TAG, ">onGetGroupFailed");
                m_allUserGroups.fireDataChanged();
            }
        });
    }


    private synchronized void setGroup(ArrayItemList<Group> groups) {
        Log.getLogger().verbose(LOG_TAG, ">setGroup; "+groups.getCount());

        m_allUserGroups.clear();
        m_allUserGroups.addAll(groups.getCopyOfDataList());
        for (Group grp : groups.getCopyOfDataList()) {
            getGroupInfo(grp);
        }
    }

    private void resolveGroupMembers (final Group group) {
        if (!group.getMembersJid().isEmpty()) {
            m_directory.searchByJids(group.getMembersJid(), new IDirectoryProxy.IDirectoryListener() {
                @Override
                public void onCorporateSearchSuccess(List<DirectoryContact> searchResults) {
                    Log.getLogger().verbose(LOG_TAG, ">onCorporateSearchSuccess");

                    if (searchResults.size() > 0) {
                        group.getGroupMembers().clear();
                        for (DirectoryContact dirContact : searchResults) {
                            Contact contact = m_contactCacheMgr.createContactIfNotExistOrUpdate(dirContact);
                            if (contact != null)
                                group.getGroupMembers().add (contact);
                        }
                    }
                }

                @Override
                public void onFailure() {
                    Log.getLogger().warn(LOG_TAG, ">onFailure");
                }
            });
        }
    }

    @Override
    public void setObserver(XmppConnection connection) {
        if( connection != null && connection.getXmppContactMgr() != null) {
            connection.getXmppContactMgr().registerChangeListener(this);
        }
    }


    @Override
    public void onUserLoaded() {
        refreshUserGroupList();
    }

    @Override
    public void rostersChanged() {

    }

    @Override
    public void createGroup(String groupName, String groupComment, final IGroupProxy.IGroupCreationListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">createGroup");

        m_groupProxy.createGroup(groupName, groupComment, new IGroupProxy.IGroupCreationListener() {
            @Override
            public void onCreationSuccess(Group group) {
                Log.getLogger().verbose(LOG_TAG, ">onCreationSuccess");
                if (listener != null)
                    listener.onCreationSuccess(group);
            }

            @Override
            public void onCreationFailed() {
                Log.getLogger().warn(LOG_TAG, ">onCreationFailed");
                if (listener != null)
                    listener.onCreationFailed();
            }
        });
    }

    @Override
    public void addUserInGroup(Group group, Contact contact, final IGroupProxy.IAddUserInGroupListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">addUserInGroup");
        if (group == null) {
            Log.getLogger().warn(LOG_TAG, "Group not available");
            return;
        }
        if (contact == null || contact.getCorporateId() == null) {
            Log.getLogger().warn(LOG_TAG, "No contact to add in Group available");
            return;
        }

        m_groupProxy.addUserInGroup(group.getId(), contact.getCorporateId(), new IGroupProxy.IAddUserInGroupListener() {
            @Override
            public void onAddUserSuccess(String userId) {
                Log.getLogger().verbose(LOG_TAG, ">onAddUserSuccess");
                if (listener != null)
                    listener.onAddUserSuccess(userId);
            }

            @Override
            public void onAddUserFailed(String userId) {
                Log.getLogger().warn(LOG_TAG, ">onAddUserFailed");
                if (listener != null)
                    listener.onAddUserFailed(userId);
            }
        });
    }

    @Override
    public void addUsersInGroup(final Group group, final List<Contact> contacts, final IGroupProxy.IAddUsersInGroupListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">addUserInGroup");
        if (group == null) {
            Log.getLogger().warn(LOG_TAG, "Group not available");
            return;
        }
        if ( contacts == null || contacts.size() == 0) {
            Log.getLogger().warn(LOG_TAG, "No contact to add in Group available");
            return;
        }
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final CountDownLatch remainingRequest = new CountDownLatch(contacts.size());

                final Integer[] requestSuccessCounter = {0};
                for (Contact userIdToAdd : contacts) {
                    m_groupProxy.addUserInGroup(group.getId(), userIdToAdd.getCorporateId(), new IGroupProxy.IAddUserInGroupListener() {
                        @Override
                        public void onAddUserSuccess(String userId) {
                            Log.getLogger().verbose(LOG_TAG, ">onAddUserSuccess");
                            requestSuccessCounter[0]++;
                            remainingRequest.countDown();
                        }

                        @Override
                        public void onAddUserFailed(String userId) {
                            Log.getLogger().warn(LOG_TAG, ">onAddUserFailed");
                            remainingRequest.countDown();
                        }
                    });
                }

                try {
                    remainingRequest.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Log.getLogger().error(LOG_TAG, "Exception while waiting: "+e.getMessage());
                }

                if(requestSuccessCounter[0] == contacts.size()) {
                    if (listener != null)
                        listener.onAddUsersSuccess();
                } else {
                    if (listener != null)
                        listener.onAddUsersFailed();
                }
            }
        });
        myThread.start();
    }

    @Override
    public void removeObserver(XmppConnection connection)
    {
        if( connection != null && connection.getXmppContactMgr() != null) {
            connection.getXmppContactMgr().unregisterChangeListener(this);
        }
    }
}

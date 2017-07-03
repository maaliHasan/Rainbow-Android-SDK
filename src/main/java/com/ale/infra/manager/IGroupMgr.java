package com.ale.infra.manager;

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.Group;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.proxy.group.IGroupProxy;
import com.ale.infra.xmpp.XmppConnection;

import java.util.List;

/**
 * Created by wilsius on 12/10/16.
 */

public interface IGroupMgr {
    void refreshUserGroupList();
    Group findGroupById (String groupId);
    ArrayItemList<Group> getGroups();
    void addNewGroup(String groupId);
    void deleteGroup(String groupId);
    void memberHasChanged(String groupId);

    void setObserver(XmppConnection connection);
    void createGroup(String groupName, String groupComment, IGroupProxy.IGroupCreationListener listener);
    void addUserInGroup(Group group, Contact contact, IGroupProxy.IAddUserInGroupListener listener);
    void addUsersInGroup(Group group, List<Contact> contacts, IGroupProxy.IAddUsersInGroupListener listener);

    void removeObserver(XmppConnection m_connection);
}

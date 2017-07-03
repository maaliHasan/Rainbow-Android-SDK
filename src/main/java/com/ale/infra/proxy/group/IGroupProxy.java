package com.ale.infra.proxy.group;

import com.ale.infra.contact.Group;
import com.ale.infra.list.ArrayItemList;

/**
 * Created by wilsius on 11/10/16.
 */

public interface IGroupProxy {
    void getAllUserGroups (String userId, int offset, int limit, IGetAllUserGroupsListener listener);

    void getGroup (String groupId, IGetGroupListener listener);

    void createGroup(String name, String comment, IGroupCreationListener listener);

    void deleteGroup(String id, IGroupDeletionListener listener);

    void addUserInGroup(String groupId, final String userIdToAdd, final IAddUserInGroupListener listener);

    interface IGetAllUserGroupsListener
    {
        void onGetAllUsersGroupsSuccess(ArrayItemList<Group> groups);

        void onGetAllUsersGroupsFailed();
    }

    interface IGetGroupListener
    {
        void onGetGroupSuccess(Group group);

        void onGetGroupFailed();
    }

    interface IGroupCreationListener {
        void onCreationSuccess(Group group);
        void onCreationFailed();
    }

    interface IGroupDeletionListener {
        void onDeleteSuccess(String group);
        void onDeleteFailed();
    }

    interface IAddUserInGroupListener {
        void onAddUserSuccess(String userId);
        void onAddUserFailed(String userId);
    }

    interface IAddUsersInGroupListener {
        void onAddUsersSuccess();
        void onAddUsersFailed();
    }
}

package com.ale.infra.rainbow.api;

import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.proxy.group.AddUserInGroup;
import com.ale.infra.proxy.group.CreateGroupResponse;
import com.ale.infra.proxy.group.GetAllUserGroups;
import com.ale.infra.proxy.group.GetGroupResponse;
import com.ale.infra.proxy.group.IGroupProxy;

/**
 * Created by wilsius on 11/10/16.
 */

public interface IRainbowGroupsService extends IRainbowService {

    void getAllUserGroups (String userId, String containingUser, int offset, int limit, final IAsyncServiceResultCallback<GetAllUserGroups> callback);
    void getGroup(String userId, String groupId, final IAsyncServiceResultCallback<GetGroupResponse> callback);
    void createGroup(String userId,String name, String comment, final IAsyncServiceResultCallback<CreateGroupResponse> callback);
    void deleteGroup(String userId, String groupId, IGroupProxy.IGroupDeletionListener listener);
    void addUserInGroup(String userId, String groupId, String userIdToAdd, final IAsyncServiceResultCallback<AddUserInGroup> callback);
}

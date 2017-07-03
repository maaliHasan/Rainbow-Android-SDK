package com.ale.infra.proxy.group;

import com.ale.infra.application.IApplicationData;
import com.ale.infra.contact.Group;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.rainbow.api.IRainbowGroupsService;
import com.ale.infra.rainbow.api.IServicesFactory;
import com.ale.util.log.Log;

/**
 * Created by wilsius on 11/10/16.
 */

public class GroupProxy implements IGroupProxy {
    private static final String LOG_TAG = "GroupProxy";

    public static final String UNSUBSCRIBED = "unsubscribed";
    public static final String ACCEPTED = "accepted";

    private IApplicationData m_applicationData;
    private IRainbowGroupsService m_groupsService;
    private IContactCacheMgr m_contactCacheMgr;

    public GroupProxy(IServicesFactory servicesFactory, IRESTAsyncRequest restAsyncRequest, IContactCacheMgr contactCacheMgr, IPlatformServices platformService)
    {
        Log.getLogger().info(LOG_TAG, "initialization");
        m_applicationData = platformService.getApplicationData();
        m_groupsService = servicesFactory.createGroupsService(contactCacheMgr, restAsyncRequest, platformService);
        m_contactCacheMgr = contactCacheMgr;
    }

    @Override
    public void getAllUserGroups(String containingUser, int offset, int limit, final IGetAllUserGroupsListener listener) {
        String userId = m_contactCacheMgr.getUser().getCorporateId();
        m_groupsService.getAllUserGroups(userId, containingUser, offset, limit, new IAsyncServiceResultCallback<GetAllUserGroups>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetAllUserGroups> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "getAllUserGroups SUCCESS");

                    ArrayItemList<Group> groups = asyncResult.getResult().getGroups();
                    if (listener != null)
                        listener.onGetAllUsersGroupsSuccess(groups);
                } else {
                    Log.getLogger().info(LOG_TAG, "getAllUserGroups FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onGetAllUsersGroupsFailed();
                }
            }
        });
    }

    @Override
    public void getGroup(String groupId, final IGetGroupListener listener) {

        String userId = m_contactCacheMgr.getUser().getCorporateId();
        m_groupsService.getGroup(userId, groupId, new IAsyncServiceResultCallback<GetGroupResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetGroupResponse> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "getGroup SUCCESS");

                    Group group = asyncResult.getResult().getGroup();
                    if (listener != null)
                        listener.onGetGroupSuccess(group);
                } else {
                    Log.getLogger().info(LOG_TAG, "getGroup FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onGetGroupFailed();
                }
            }
        });
    }

    @Override
    public void createGroup(String name, String comment, final IGroupCreationListener listener) {

        String userId = m_contactCacheMgr.getUser().getCorporateId();
        m_groupsService.createGroup(userId, name, comment, new IAsyncServiceResultCallback<CreateGroupResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<CreateGroupResponse> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "CreateGroupResponse SUCCESS");

                    Group group = asyncResult.getResult().getGroup();
                    if (listener != null)
                        listener.onCreationSuccess(group);
                } else {
                    Log.getLogger().info(LOG_TAG, "CreateGroupResponse FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onCreationFailed();
                }
            }
        });

    }

    @Override
    public void addUserInGroup(String groupId, final String userIdToAdd, final IAddUserInGroupListener listener) {

        String userId = m_contactCacheMgr.getUser().getCorporateId();
        m_groupsService.addUserInGroup(userId, groupId, userIdToAdd, new IAsyncServiceResultCallback<AddUserInGroup>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<AddUserInGroup> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "addUserInGroup SUCCESS");

                    Group group = asyncResult.getResult().getGroup();
                    if (listener != null)
                        listener.onAddUserSuccess(userIdToAdd);
                } else {
                    Log.getLogger().info(LOG_TAG, "addUserInGroup FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onAddUserFailed(userIdToAdd);
                }
            }
        });

    }

    @Override
    public void deleteGroup(final String groupId, final IGroupDeletionListener listener) {

        String userId = m_contactCacheMgr.getUser().getCorporateId();
        m_groupsService.deleteGroup(userId, groupId, listener);
    }

}

package com.ale.infra.rainbow.adapter;

import com.ale.infra.application.IApplicationData;
import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.RESTResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.framework.RainbowServiceTag;
import com.ale.infra.proxy.group.AddUserInGroup;
import com.ale.infra.proxy.group.CreateGroupResponse;
import com.ale.infra.proxy.group.GetAllUserGroups;
import com.ale.infra.proxy.group.GetGroupResponse;
import com.ale.infra.proxy.group.IGroupProxy;
import com.ale.infra.rainbow.api.ApisConstants;
import com.ale.infra.rainbow.api.IRainbowGroupsService;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by wilsius on 11/10/16.
 */

public class RainbowGroupsServiceAdapter implements IRainbowGroupsService {

    private static final String LOG_TAG = "RainbowGroupsServiceAdapter";

    private final IPlatformServices m_platformServices;
    private IContactCacheMgr m_contactCacheMgr;
    private IRESTAsyncRequest m_restAsyncRequest;


    public RainbowGroupsServiceAdapter(IContactCacheMgr contactCacheMgr, IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices) {
        m_restAsyncRequest = restAsyncRequest;
        m_contactCacheMgr = contactCacheMgr;
        m_platformServices = platformServices;
    }

    @Override
    public RainbowServiceTag getTag()
    {
        return RainbowServiceTag.AVATAR;
    }

    private String getUrl()
    {
        String url = RainbowContext.getPlatformServices().getApplicationData().getServerUrl();
        if (url == null)
        {
            url = StringsUtil.EMPTY;
        }
        return url;
    }

    @Override
    public void getAllUserGroups(String userId, String containingUser, int offset, int limit, final IAsyncServiceResultCallback<GetAllUserGroups> callback) {
        Log.getLogger().verbose(LOG_TAG, ">getAllUserGroups");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);

        // sending token through header or parameters does not work (see https://tools.ietf.org/html/rfc6750)
        // but it works with URI
        try
        {
            restUrl.append(URLEncoder.encode(userId, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to get all groups");
        }
        restUrl.append("/groups");

        try
        {
            restUrl.append("?format=medium");
            if (!StringsUtil.isNullOrEmpty(containingUser))
                restUrl.append("&userId="  + containingUser);
            if (limit != 0)
                restUrl.append("&limit=" + limit);

            restUrl.append("&offset=" + offset);

        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "getAllUserGroups failed");
                    notifyResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    try
                    {
                        Log.getLogger().info(LOG_TAG, "getAllUserGroups success");
                        notifyResult(callback, null, new GetAllUserGroups(asyncResult.getResult().getResponse()));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST groups result");
                        notifyResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void getGroup(String userId, String groupId, final IAsyncServiceResultCallback<GetGroupResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">getGroup");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);

        // sending token through header or parameters does not work (see https://tools.ietf.org/html/rfc6750)
        // but it works with URI
        try
        {
            restUrl.append(URLEncoder.encode(userId, "UTF-8"));
            restUrl.append("/groups/");
            restUrl.append(URLEncoder.encode(groupId, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to get group");
        }

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "getGroup failed");
                    notifyGroupResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    try
                    {
                        Log.getLogger().info(LOG_TAG, "getGroup success");
                        notifyGroupResult(callback, null, new GetGroupResponse(asyncResult.getResult().getResponse()));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST group result");
                        notifyGroupResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }


    private void notifyResult(IAsyncServiceResultCallback<GetAllUserGroups> callback, RainbowServiceException alcServiceException, GetAllUserGroups response)
    {
        AsyncServiceResponseResult<GetAllUserGroups> asyncResult = new AsyncServiceResponseResult<GetAllUserGroups>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyGroupResult(IAsyncServiceResultCallback<GetGroupResponse> callback, RainbowServiceException alcServiceException, GetGroupResponse response)
    {
        AsyncServiceResponseResult<GetGroupResponse> asyncResult = new AsyncServiceResponseResult<GetGroupResponse>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }


    @Override
    public void createGroup(String userId, String name, String comment, final IAsyncServiceResultCallback<CreateGroupResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">createGroup");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);

        // sending token through header or parameters does not work (see https://tools.ietf.org/html/rfc6750)
        // but it works with URI
        try
        {
            restUrl.append(URLEncoder.encode(userId, "UTF-8"));
            restUrl.append("/groups");
        }
        catch (UnsupportedEncodingException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to get group");
        }

        JSONObject restBody = new JSONObject();
        try {
            restBody.put("name", name);
            restBody.put("comment", comment);
        } catch (Exception ex) {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }


        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "CreateGroupResponse failed");
                    notifyGroupCreationResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    try
                    {
                        Log.getLogger().info(LOG_TAG, "CreateGroupResponse success");
                        notifyGroupCreationResult(callback, null, new CreateGroupResponse(m_contactCacheMgr, m_platformServices, asyncResult.getResult().getResponse()));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST group result");
                        notifyGroupCreationResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }


    private void notifyGroupCreationResult(IAsyncServiceResultCallback<CreateGroupResponse> callback, RainbowServiceException alcServiceException, CreateGroupResponse response)
    {
        AsyncServiceResponseResult<CreateGroupResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }




    @Override
    public void addUserInGroup(String userId, String groupId, String userIdToAdd, final IAsyncServiceResultCallback<AddUserInGroup> callback) {
        Log.getLogger().verbose(LOG_TAG, ">addUserInGroup");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);

        // sending token through header or parameters does not work (see https://tools.ietf.org/html/rfc6750)
        // but it works with URI
        try
        {
            restUrl.append(URLEncoder.encode(userId, "UTF-8"));
            restUrl.append("/groups/");
            restUrl.append(URLEncoder.encode(groupId, "UTF-8"));
            restUrl.append("/users/");
            restUrl.append(URLEncoder.encode(userIdToAdd, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to get group");
        }

        Log.getLogger().verbose(LOG_TAG, ">addUserInGroup request; "+restUrl.toString());
        m_restAsyncRequest.sendPostRequest(restUrl.toString(), null, new IAsyncServiceResultCallback<RESTResult>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "AddUserInGroup failed");
                    notifyAddUserInGroupResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    try
                    {
                        Log.getLogger().info(LOG_TAG, "AddUserInGroup success");
                        notifyAddUserInGroupResult(callback, null, new AddUserInGroup(asyncResult.getResult().getResponse()));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST AddUserInGroup result");
                        notifyAddUserInGroupResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    private void notifyAddUserInGroupResult(IAsyncServiceResultCallback<AddUserInGroup> callback, RainbowServiceException alcServiceException, AddUserInGroup response)
    {
        AsyncServiceResponseResult<AddUserInGroup> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }


    @Override
    public void deleteGroup(String userId, final String groupId, final IGroupProxy.IGroupDeletionListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">deleteGroup");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);

        // sending token through header or parameters does not work (see https://tools.ietf.org/html/rfc6750)
        // but it works with URI
        try
        {
            restUrl.append(URLEncoder.encode(userId, "UTF-8"));
            restUrl.append("/groups/");
            restUrl.append(URLEncoder.encode(groupId, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to get group");
        }

        m_restAsyncRequest.sendDeleteRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (listener != null)
                {
                    if (asyncResult.exceptionRaised())
                        listener.onDeleteFailed();
                    else
                        listener.onDeleteSuccess(groupId);
                }
            }
        });
    }

}

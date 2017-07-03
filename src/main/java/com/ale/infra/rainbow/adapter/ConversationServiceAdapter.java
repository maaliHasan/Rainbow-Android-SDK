package com.ale.infra.rainbow.adapter;

import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.RESTResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseVoid;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.conversation.GetAllConversationsResponse;
import com.ale.infra.proxy.framework.RainbowServiceTag;
import com.ale.infra.rainbow.api.ApisConstants;
import com.ale.infra.rainbow.api.IRainbowConversationService;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by georges on 10/05/16.
 */
public class ConversationServiceAdapter implements IRainbowConversationService
{
    private static final String LOG_TAG = "ConversationServiceAdapter";
    
    private final IRESTAsyncRequest m_restAsyncRequest;
    private final IPlatformServices m_platformServices;
    private final IContactCacheMgr m_contactCacheMgr;


    public ConversationServiceAdapter(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices, IContactCacheMgr contactCacheMgr)
    {
        m_restAsyncRequest = restAsyncRequest;
        m_platformServices = platformServices;
        m_contactCacheMgr = contactCacheMgr;
    }

    @Override
    public RainbowServiceTag getTag()
    {
        return RainbowServiceTag.CONVERSATION;
    }

    private String getUrl()
    {
        String url = m_platformServices.getApplicationData().getServerUrl();
        if (url == null)
        {
            url = StringsUtil.EMPTY;
        }
        return url;
    }

    @Override
    public void getAllConversations(String userId, final IAsyncServiceResultCallback<GetAllConversationsResponse> callback)
    {
        Log.getLogger().verbose(LOG_TAG, ">getAllConversations");

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
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to get all conversations");
        }
        restUrl.append(ApisConstants.CONVERSATION);

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "getAllConversations failed");
                    notifyResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    try
                    {
                        Log.getLogger().info(LOG_TAG, "getAllConversations success");
                        notifyResult(callback, null, new GetAllConversationsResponse(m_platformServices, m_contactCacheMgr, asyncResult.getResult().getResponse()));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST Conversation result");
                        notifyResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void deleteConversation(String userId, String concersationId, final IAsyncServiceVoidCallback callback)
    {
        Log.getLogger().verbose(LOG_TAG, ">deleteConversation");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);

        // sending token through header or parameters does not work (see https://tools.ietf.org/html/rfc6750)
        // but it works with URI
        try
        {
            restUrl.append(URLEncoder.encode(userId, "UTF-8"));
            restUrl.append(ApisConstants.CONVERSATION);
            restUrl.append("/");
            restUrl.append(URLEncoder.encode(concersationId, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to delete conversation");
        }

        m_restAsyncRequest.sendDeleteRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "deleteConversation failed");
                    notifyVoidResult(callback, asyncResult.getException());
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "deleteConversation success");
                    notifyVoidResult(callback, null);
                }
            }
        });
    }

    @Override
    public void updateConversation(String userId, String conversationId, boolean muteState, final IAsyncServiceResultCallback callback) {
        Log.getLogger().verbose(LOG_TAG, ">updateConversation");

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
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to update conversation");
        }

        restUrl.append(ApisConstants.CONVERSATION);
        restUrl.append("/");

        try
        {
            restUrl.append(URLEncoder.encode(conversationId, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to update conversation");
        }

        JSONObject restBody = new JSONObject();

        try
        {
            restBody.put("mute", muteState);
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object for mute state value");
        }

        m_restAsyncRequest.sendPutRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>()
        {

            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "updateConversation failed");
                    notifyUpdateConversationResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "updateConversation success");
                    try
                    {
                        JSONObject response = new JSONObject(asyncResult.getResult().getResponse());
                        JSONObject data = response.getJSONObject("data");

                        notifyUpdateConversationResult(callback, null, data.getString("id"));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST update conversation result");
                        notifyUpdateConversationResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void downloadConversation(String userId, String conversationId, final IAsyncServiceResultCallback<String> callback) {
        Log.getLogger().info(LOG_TAG, ">downloadConversation");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);

        try
        {
            restUrl.append(URLEncoder.encode(userId, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to create conversation");
        }

        restUrl.append(ApisConstants.CONVERSATION);
        restUrl.append("/");

        try
        {
            restUrl.append(URLEncoder.encode(conversationId, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to create conversation");
        }
        restUrl.append("/");
        restUrl.append("downloads");

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), null, new IAsyncServiceResultCallback<RESTResult>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "downloadConversation failed");
                    notifyDownloadConversationResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    notifyDownloadConversationResult(callback, null, null);
                }
            }
        });
    }

    @Override
    public void createConversation(String userId, String peerId, String type, final IAsyncServiceResultCallback<String> callback)
    {
        Log.getLogger().verbose(LOG_TAG, ">createConversation");

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
            Log.getLogger().error(LOG_TAG, "Impossible to construct URI to create conversation");
        }

        restUrl.append(ApisConstants.CONVERSATION);

        JSONObject restBody = new JSONObject();

        try
        {
            restBody.put("peerId", peerId);
            restBody.put("type", type);
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "createConversation failed");
                    notifyCreateConversationResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "createConversation success");
                    try
                    {
                        JSONObject response = new JSONObject(asyncResult.getResult().getResponse());
                        JSONObject data = response.getJSONObject("data");

                        notifyCreateConversationResult(callback, null, data.getString("id"));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST Conversation result");
                        notifyCreateConversationResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    private void notifyCreateConversationResult(IAsyncServiceResultCallback<String> callback, RainbowServiceException alcServiceException, String id)
    {
        AsyncServiceResponseResult<String> asyncResult = new AsyncServiceResponseResult<String>(alcServiceException, id);
        callback.handleResult(asyncResult);
    }

    private void notifyDownloadConversationResult(IAsyncServiceResultCallback<String> callback, RainbowServiceException alcServiceException, String id)
    {
        AsyncServiceResponseResult<String> asyncResult = new AsyncServiceResponseResult<String>(alcServiceException, id);
        callback.handleResult(asyncResult);
    }

    private void notifyUpdateConversationResult(IAsyncServiceResultCallback<String> callback, RainbowServiceException alcServiceException, String id)
    {
        AsyncServiceResponseResult<String> asyncResult = new AsyncServiceResponseResult<String>(alcServiceException, id);
        callback.handleResult(asyncResult);
    }

    private void notifyResult(IAsyncServiceResultCallback<GetAllConversationsResponse> callback, RainbowServiceException alcServiceException, GetAllConversationsResponse response)
    {
        AsyncServiceResponseResult<GetAllConversationsResponse> asyncResult = new AsyncServiceResponseResult<GetAllConversationsResponse>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyVoidResult(IAsyncServiceVoidCallback callback, RainbowServiceException alcServiceException)
    {
        AsyncServiceResponseVoid asyncResult = new AsyncServiceResponseVoid(alcServiceException);
        callback.handleResult(asyncResult);
    }
}

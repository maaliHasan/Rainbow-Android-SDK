package com.ale.infra.rainbow.adapter;

import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.RESTResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.directory.SearchResponse;
import com.ale.infra.proxy.directory.SearchResponseByJid;
import com.ale.infra.proxy.framework.RainbowServiceTag;
import com.ale.infra.rainbow.api.ApisConstants;
import com.ale.infra.rainbow.api.IDirectoryService;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Directory service class
 */
public class DirectoryService implements IDirectoryService
{
    private static final String LOG_TAG = "DirectoryService";

    private final IRESTAsyncRequest m_restAsyncRequest;
    private final IPlatformServices m_platformServices;

    public DirectoryService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices)
    {
        m_restAsyncRequest = restAsyncRequest;
        m_platformServices = platformServices;
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
    public RainbowServiceTag getTag()
    {
        return RainbowServiceTag.DIRECTORY;
    }

    @Override
    public void searchByName(String userName, final IAsyncServiceResultCallback<SearchResponse> callback)
    {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.SEARCH_USER_BY_NAME);

        // sending token through header or parameters does not work (see https://tools.ietf.org/html/rfc6750)
        // but it works with URI
        restUrl.append("?displayName=");
        try
        {
            restUrl.append(URLEncoder.encode(userName, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "directory searchByName failed.");
                    notifyAuthenticationResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    try
                    {
                        Log.getLogger().info(LOG_TAG, "directory searchByName success.");
                        notifyAuthenticationResult(callback, null, new SearchResponse(m_platformServices, asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST directory result");
                        notifyAuthenticationResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void searchByMails(List<String> emails, final IAsyncServiceResultCallback<SearchResponse> callback) {
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(ApisConstants.LOGINEMAILS);
        restUrl.append("?format=full");

        JSONObject restBody = new JSONObject();
        try {
            restBody.put("loginEmail", new JSONArray(emails));
        } catch (Exception ex) {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "directory searchByMails failed.");
                    notifyAuthenticationResult(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "directory searchByMails success.");
                        notifyAuthenticationResult(callback, null, new SearchResponse(m_platformServices, asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST searchByMails result");
                        notifyAuthenticationResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void searchByJid(String userid, String jid, final IAsyncServiceResultCallback<SearchResponseByJid> callback) {
        Log.getLogger().verbose(LOG_TAG, ">searchByJid : " + jid);
        
        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(ApisConstants.JIDS);
        restUrl.append("/");
        try {
            restUrl.append(URLEncoder.encode(jid, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // sending token through header or parameters does not work (see https://tools.ietf.org/html/rfc6750)
        // but it works with URI
        restUrl.append("?userId=");
        try
        {
            restUrl.append(URLEncoder.encode(userid, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "directory searchByJid failed.");
                    notifyAuthenticationResultSearchById(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "directory searchByJid success.");
                        notifyAuthenticationResultSearchById(callback, null, new SearchResponseByJid(m_platformServices, asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST directory result");
                        notifyAuthenticationResultSearchById(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void searchNetwork(int limit, final IAsyncServiceResultCallback<SearchResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, "Search network...");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.NETWORK);
        restUrl.append("?format=full");
        restUrl.append("&limit=");
        restUrl.append(limit);

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "directory searchNetwork failed.");
                    notifyAuthenticationResult(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "directory searchNetwork success.");
                        notifyAuthenticationResult(callback, null, new SearchResponse(m_platformServices, asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST searchNetwork directory result");
                        notifyAuthenticationResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });

    }

    @Override
    public void searchByJids(List<String> jids, final IAsyncServiceResultCallback<SearchResponse> callback) {
        //Log.getLogger().verbose(LOG_TAG, ">searchByJids; "+jids.size());

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.USERS);
        restUrl.append(ApisConstants.JIDS);
        restUrl.append("?format=full");


        JSONObject restBody = new JSONObject();
        try {
            restBody.put("jid_im", new JSONArray(jids));
        } catch (Exception ex) {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "directory searchByJids failed.");
                    notifyAuthenticationResult(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "directory searchByJids success.");
                        notifyAuthenticationResult(callback, null, new SearchResponse(m_platformServices, asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST searchByJids result");
                        notifyAuthenticationResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });

    }

    @Override
    public void abortSearch()
    {
        Log.getLogger().verbose(LOG_TAG, ">abortSearch");
        if (m_restAsyncRequest != null)
            m_restAsyncRequest.abort();
    }

    private void notifyAuthenticationResult(IAsyncServiceResultCallback<SearchResponse> callback, RainbowServiceException alcServiceException, SearchResponse response)
    {
        AsyncServiceResponseResult<SearchResponse> asyncResult = new AsyncServiceResponseResult<SearchResponse>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyAuthenticationResultSearchById(IAsyncServiceResultCallback<SearchResponseByJid> callback, RainbowServiceException alcServiceException, SearchResponseByJid response)
    {
        AsyncServiceResponseResult<SearchResponseByJid> asyncResult = new AsyncServiceResponseResult<SearchResponseByJid>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }
}

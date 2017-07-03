package com.ale.infra.rainbow.adapter;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.RESTResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.avatar.GetAvatarResponse;
import com.ale.infra.proxy.company.GetCompanyResponse;
import com.ale.infra.proxy.framework.RainbowServiceTag;
import com.ale.infra.rainbow.api.ApisConstants;
import com.ale.infra.rainbow.api.IRainbowCompanyService;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.net.URLEncoder;

/**
 * Created by trunk1 on 13/02/2017.
 */

public class RainbowCompanyServiceAdapter implements IRainbowCompanyService {
    private static final String LOG_TAG = "RainbowCompanyServiceAdapter";

    private final IRESTAsyncRequest m_restAsyncRequest;
    private IPlatformServices m_platformServices;

    public RainbowCompanyServiceAdapter(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices)
    {
        m_restAsyncRequest = restAsyncRequest;
        m_platformServices = platformServices;
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
    public RainbowServiceTag getTag()
    {
        return RainbowServiceTag.USERS;
    }

    @Override
    public void getAllCompanies(final IAsyncServiceResultCallback<GetCompanyResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">getCompanies");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.COMPANY);
        try
        {
            restUrl.append("?format=full");
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "getAllCompanies Error while filling JSON Object");
        }

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "getAllCompanies failed" + asyncResult.getException());
                    notifyResponse_GetCompanyResponse(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "getAllCompanies success");
                        notifyResponse_GetCompanyResponse(callback, null, new GetCompanyResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "getAllCompanies Impossible to parse REST Company result");
                        notifyResponse_GetCompanyResponse(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void getCompany(String companyId, final IAsyncServiceResultCallback<GetCompanyResponse> callback) {
        if( StringsUtil.isNullOrEmpty(companyId)) {
            Log.getLogger().warn(LOG_TAG, "getCompany: companyId is Empty");
            return;
        }

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.COMPANY + "/");
        restUrl.append(companyId);

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public synchronized void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "getCompany failed" + asyncResult.getException());
                    notifyResponse_GetCompanyResponse(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "getCompany success" + asyncResult.getResult().getResponse().toString());
                        notifyResponse_GetCompanyResponse(callback, null, new GetCompanyResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "getCompany Impossible to parse REST Company result");
                        notifyResponse_GetCompanyResponse(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void searchByName(String name, final IAsyncServiceResultCallback<GetCompanyResponse> callback) {
        if( StringsUtil.isNullOrEmpty(name)) {
            Log.getLogger().warn(LOG_TAG, "searchByName: name is Empty");
            return;
        }
        Log.getLogger().verbose(LOG_TAG, ">searchByName");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.COMPANY);
        try
        {
            restUrl.append("?format=full");
            restUrl.append("&name=");
            restUrl.append(URLEncoder.encode(name, "UTF-8"));
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "searchByName Error while filling JSON Object");
        }

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {

            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "searchByName failed" + asyncResult.getException());
                    notifyResponse_GetCompanyResponse(callback, asyncResult.getException(), null);
                } else {
                    try {
                        Log.getLogger().info(LOG_TAG, "searchByName success");
                        notifyResponse_GetCompanyResponse(callback, null, new GetCompanyResponse(asyncResult.getResult().getResponse()));
                    } catch (Exception error) {
                        Log.getLogger().error(LOG_TAG, "searchByName Impossible to parse REST Company result");
                        notifyResponse_GetCompanyResponse(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void getCompanyBanner(String companyId, int size, final IAsyncServiceResultCallback<GetAvatarResponse> callback) {
        if( StringsUtil.isNullOrEmpty(companyId)) {
            Log.getLogger().warn(LOG_TAG, "getCompanyBanner: companyId is Empty");
            return;
        }

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.BANNER + "/");
        restUrl.append(companyId);
        try
        {
            restUrl.append("?size=");
            restUrl.append(String.valueOf(size));
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "getCompanyBanner Error while filling JSON Object");
        }

        try
        {
            m_restAsyncRequest.getAvatarFile(restUrl.toString(), new IAsyncServiceResultCallback<GetAvatarResponse>() {
                @Override
                public void handleResult(AsyncServiceResponseResult<GetAvatarResponse> asyncResult) {
                    if (asyncResult.exceptionRaised())
                    {
                        Log.getLogger().error(LOG_TAG, "getCompanyBanner failed");
                        notifyResponse_GetBannerResult(callback, asyncResult.getException(), null);
                    }
                    else
                    {
                        Log.getLogger().info(LOG_TAG, "getCompanyBanner success");
                        try {
                            notifyResponse_GetBannerResult(callback, null, asyncResult.getResult());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        catch (Exception e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to parse REST Banner result" + e.toString());
            notifyResponse_GetBannerResult(callback, new RainbowServiceException(e), null);
        }
   }


    private void notifyResponse_GetCompanyResponse(IAsyncServiceResultCallback<GetCompanyResponse> callback, RainbowServiceException alcServiceException, GetCompanyResponse response)
    {
        AsyncServiceResponseResult asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyResponse_GetBannerResult (IAsyncServiceResultCallback<GetAvatarResponse> callback, RainbowServiceException alcServiceException, GetAvatarResponse response)
    {
        AsyncServiceResponseResult asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

}


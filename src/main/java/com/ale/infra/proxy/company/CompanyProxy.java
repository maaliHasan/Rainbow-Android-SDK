package com.ale.infra.proxy.company;

import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.invitation.CompanyContact;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.avatar.GetAvatarResponse;
import com.ale.infra.proxy.avatar.IAvatarProxy;
import com.ale.infra.rainbow.api.IRainbowCompanyService;
import com.ale.infra.rainbow.api.IServicesFactory;
import com.ale.util.log.Log;

import java.util.List;

/**
 * Created by trunk1 on 13/02/2017.
 */

public class CompanyProxy implements ICompanyProxy {
    private static final String LOG_TAG = "CompanyProxy";

    private IRainbowCompanyService m_companyService;

    public CompanyProxy(IServicesFactory servicesFactory, IRESTAsyncRequest httpClientFactory, IPlatformServices platformService)
    {
        Log.getLogger().info(LOG_TAG, "initialization");
        m_companyService = servicesFactory.createCompanyService(httpClientFactory, platformService);
    }

    @Override
    public void getAllCompanies(final IGetCompanyDataListener listener) {
        Log.getLogger().info(LOG_TAG, ">getCompanies");

        m_companyService.getAllCompanies(new IAsyncServiceResultCallback<GetCompanyResponse>()
        {
            @Override
            public synchronized void handleResult(AsyncServiceResponseResult<GetCompanyResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().verbose(LOG_TAG, "getCompanies SUCCESS");

                    List<CompanyContact> companies = asyncResult.getResult().getCompanyList();
                    if (listener != null)
                        listener.onSuccess(companies);
                }
                else
                {
                    Log.getLogger().warn(LOG_TAG, "getCompanies FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onFailure(asyncResult.getException());
                }
            }
        });
    }

    @Override
    public void searchByName(String name, final IGetCompanyDataListener listener) {
        Log.getLogger().info(LOG_TAG, ">searchByName");

        m_companyService.searchByName(name, new IAsyncServiceResultCallback<GetCompanyResponse>()
        {
            @Override
            public synchronized void handleResult(AsyncServiceResponseResult<GetCompanyResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().verbose(LOG_TAG, "searchByName SUCCESS");

                    List<CompanyContact> companies = asyncResult.getResult().getCompanyList();
                    if (listener != null)
                        listener.onSuccess(companies);
                }
                else
                {
                    Log.getLogger().warn(LOG_TAG, "searchByName FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onFailure(asyncResult.getException());
                }
            }
        });
    }

    @Override
    public void getCompany(String companyId, final IGetCompanyDataListener listener) {
        Log.getLogger().info(LOG_TAG, ">getCompany");

        m_companyService.getCompany(companyId, new IAsyncServiceResultCallback<GetCompanyResponse>()
        {
            @Override
            public synchronized void handleResult(AsyncServiceResponseResult<GetCompanyResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().verbose(LOG_TAG, "getCompany SUCCESS");

                    List<CompanyContact> companies = asyncResult.getResult().getCompanyList();
                    if (listener != null)
                        listener.onSuccess(companies);
                }
                else
                {
                    Log.getLogger().warn(LOG_TAG, "getCompany FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onFailure(asyncResult.getException());
                }
            }
        });
    }

    @Override
    public void getCompanyBanner(String companyId, int size, final IAvatarProxy.IAvatarListener listener) {
        Log.getLogger().info(LOG_TAG, ">getCompanyBanner");

        m_companyService.getCompanyBanner(companyId, size, new IAsyncServiceResultCallback<GetAvatarResponse>()
        {
            @Override
            public synchronized void handleResult(AsyncServiceResponseResult<GetAvatarResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().verbose(LOG_TAG, "getCompanyBanner SUCCESS");

                    if (listener != null)
                        listener.onAvatarSuccess(asyncResult.getResult().getBitmap());
                }
                else
                {
                    Log.getLogger().warn(LOG_TAG, "getCompanyBanner FAILURE", asyncResult.getException());
                    if (asyncResult.getException().getStatusCode() != 404) {
                        Log.getLogger().info(LOG_TAG, "getCompanyBanner FAILURE for contact ", asyncResult.getException());
                    }

                    if (listener != null)
                        listener.onAvatarFailure(asyncResult.getException());
                }
            }
        });
    }
}

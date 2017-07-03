package com.ale.infra.rainbow.api;

import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.proxy.avatar.GetAvatarResponse;
import com.ale.infra.proxy.company.GetCompanyResponse;

/**
 * Created by trunk1 on 13/02/2017.
 */

public interface IRainbowCompanyService extends IRainbowService {

    void getAllCompanies(IAsyncServiceResultCallback<GetCompanyResponse> callback);

    void getCompany(String companyId, IAsyncServiceResultCallback<GetCompanyResponse> callback);

    void searchByName(String name, IAsyncServiceResultCallback<GetCompanyResponse> iAsyncServiceResultCallback);

    void getCompanyBanner(String companyId, int size, IAsyncServiceResultCallback<GetAvatarResponse> callback);

}

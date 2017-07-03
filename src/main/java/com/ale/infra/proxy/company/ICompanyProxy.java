package com.ale.infra.proxy.company;

import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.invitation.CompanyContact;
import com.ale.infra.proxy.avatar.IAvatarProxy;

import java.util.List;

/**
 * Created by trunk1 on 13/02/2017.
 */

public interface ICompanyProxy {

    void getAllCompanies(IGetCompanyDataListener listener);

    void getCompany(String companyId, IGetCompanyDataListener listener);

    void searchByName(String name, final IGetCompanyDataListener listener);

    void getCompanyBanner(String companyId, int size, final IAvatarProxy.IAvatarListener listener);

    interface IGetCompanyDataListener
    {
        void onSuccess(List<CompanyContact> requestList);

        void onFailure(RainbowServiceException exception);
    }
}

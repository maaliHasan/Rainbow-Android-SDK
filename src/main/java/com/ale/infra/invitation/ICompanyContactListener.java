package com.ale.infra.invitation;

/**
 * Created by trunk1 on 13/02/2017.
 */

public interface ICompanyContactListener {
    void companyContactUpdated(CompanyContact updatedCompanyContact);
    void onActionInProgress(boolean clickActionInProgress);
}

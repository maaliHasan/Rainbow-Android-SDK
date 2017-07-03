package com.ale.infra.manager;

import com.ale.infra.invitation.CompanyContact;
import com.ale.infra.invitation.CompanyInvitation;
import com.ale.infra.invitation.CompanyJoinRequest;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.proxy.company.ICompanyProxy;
import com.ale.infra.proxy.users.IUserProxy;

import java.util.List;

/**
 * Created by trunk1 on 01/12/2016.
 */

public interface ICompanyInvitationMgr {
    void refreshReceivedCompanyInvitationList();

    void acceptCompanyInvitation(CompanyInvitation invitation);

    void declineCompanyInvitation(CompanyInvitation invitation);

    ArrayItemList<CompanyInvitation> getReceivedCompanyInvitationList();

    void searchByName(String name, ICompanyProxy.IGetCompanyDataListener listener);

    List<CompanyInvitation> getPendingReceivedCompanyInvitationList();

    ArrayItemList<CompanyJoinRequest> getCompanyJoinRequestList();

    void refreshJoinCompanyRequestList();

    void cancelJoinCompanyRequest(CompanyJoinRequest companyJoinRequest, IUserProxy.IGetCompanyJoinRequestListener listener);

    void createJoinCompanyRequest(CompanyContact company, IUserProxy.IGetCompanyJoinRequestListener listener);

    void resendJoinCompanyRequest(final CompanyJoinRequest joinRequest, final IUserProxy.IGetCompanyJoinRequestListener listener);

    CompanyContact findCompanyContactById(String companyId);

    List<CompanyJoinRequest> getPendingCompanyJoinRequestList();

    CompanyJoinRequest findPendingCompanyJoinRequestByCompany(CompanyContact company);

    CompanyInvitation findReceivedCompanyInvitationWithCompanyInvitationId(String companyInvitationId);

}

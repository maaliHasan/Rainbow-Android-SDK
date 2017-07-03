package com.ale.infra.invitation;

/**
 * Created by trunk1 on 22/12/2016.
 */
public interface ICompanyInvitationListener {
    //called when invitation has been updated
    void companyInvitationUpdated(CompanyInvitation updatedInvitation);
}
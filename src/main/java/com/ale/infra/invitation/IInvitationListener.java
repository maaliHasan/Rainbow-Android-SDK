package com.ale.infra.invitation;

/**
 * Created by trunk1 on 22/12/2016.
 */
public interface IInvitationListener {
    void invitationUpdated(Invitation invitation);
    void onActionInProgress(final boolean clickActionInProgress);
}
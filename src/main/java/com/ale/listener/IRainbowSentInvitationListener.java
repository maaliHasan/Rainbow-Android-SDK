package com.ale.listener;


import com.ale.infra.http.adapter.concurrent.RainbowServiceException;

public interface IRainbowSentInvitationListener {

    void onInvitationSentSuccess(String contactEmail);

    void onInvitationSentError(RainbowServiceException exception);

    void onInvitationError();
}

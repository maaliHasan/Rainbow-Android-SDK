package com.ale.infra.rainbow.api;

import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;

/**
 * Created by grobert on 09/11/15.
 */
public interface IRainbowNotifications extends IRainbowService
{
    void sendInvitation(String userId, String email, IAsyncServiceVoidCallback callback);

    void resendEmailInvite(String userId, String invitationId, IAsyncServiceVoidCallback callback);

    void sendSelfRegisterInvite(String email, IAsyncServiceVoidCallback callback);

    void sendResetPwdEmail(String email, IAsyncServiceVoidCallback callback);

    void sendResetUserPwd(String email, String newPassword, String tempToken, IAsyncServiceVoidCallback callback);
}

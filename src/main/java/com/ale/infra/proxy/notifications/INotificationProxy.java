package com.ale.infra.proxy.notifications;

import com.ale.infra.http.adapter.concurrent.RainbowServiceException;

/**
 * Created by grobert on 09/11/15.
 */
public interface INotificationProxy
{
    void sendEmailInvite(String userId, String email, ISendEMailListener listener);

    void resendEmailInvite(String userId, String invitationId, ISendEMailListener listener);

    void sendSelfRegisterInvite(String email, ISendEMailListener listener);

    void sendResetPwdEmail(String email, ISendEMailListener listener);

    void sendResetUserPwd(String email, String newPassword, String tempToken, ISendEMailListener listener);

    interface ISendEMailListener
    {
        void onSuccess();

        void onFailure(RainbowServiceException exception);
    }
}

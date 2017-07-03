package com.ale.infra.proxy.notifications;

import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseVoid;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.rainbow.api.IRainbowNotifications;
import com.ale.infra.rainbow.api.IServicesFactory;
import com.ale.util.log.Log;


/**
 * Created by grobert on 09/11/15.
 */
public class NotificationsProxy implements INotificationProxy
{
    private static final String LOG_TAG = "NotificationsProxy";

    private IRainbowNotifications m_notificationService;

    public NotificationsProxy(IServicesFactory servicesFactory, IRESTAsyncRequest httpClientFactory, IPlatformServices platformService)
    {
        Log.getLogger().info(LOG_TAG, "initialization");
        m_notificationService = servicesFactory.createNotificationsService(httpClientFactory, platformService);
    }

    @Override
    public void sendEmailInvite(String userId, String email, final ISendEMailListener listener)
    {
        Log.getLogger().verbose(LOG_TAG, "sendInvitation with : " + email);

        m_notificationService.sendInvitation(userId, email, new IAsyncServiceVoidCallback()
        {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "Error while trying to sendInvitation :" + asyncResult.getException().getDetailsMessage());

                    if (listener != null)
                        listener.onFailure(asyncResult.getException());
                }
                else
                {
                    if (listener != null)
                        listener.onSuccess();
                }
            }
        });
    }

    @Override
    public void resendEmailInvite(String userId, String invitationId, final ISendEMailListener listener) {
        Log.getLogger().verbose(LOG_TAG, "resendEmailInvite with : " + invitationId);

        m_notificationService.resendEmailInvite(userId, invitationId, new IAsyncServiceVoidCallback()
        {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "Error while trying to resendEmailInvite :" + asyncResult.getException().getDetailsMessage());

                    if (listener != null)
                        listener.onFailure(asyncResult.getException());
                }
                else
                {
                    if (listener != null)
                        listener.onSuccess();
                }
            }
        });
    }

    @Override
    public void sendSelfRegisterInvite(String email, final ISendEMailListener listener)
    {
        Log.getLogger().verbose(LOG_TAG, "sendSelfRegisterInvite with : " + email);

        m_notificationService.sendSelfRegisterInvite(email, new IAsyncServiceVoidCallback() {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "Error while trying to sendSelfRegisterInvite :" + asyncResult.getException().getMessage());

                    if (listener != null) {
                        listener.onFailure(asyncResult.getException());
                    }
                } else {
                    if (listener != null)
                        listener.onSuccess();
                }
            }
        });
    }

    @Override
    public void sendResetPwdEmail(String email, final ISendEMailListener listener) {
        Log.getLogger().verbose(LOG_TAG, "sendResetPwdEmail with : " + email);

        m_notificationService.sendResetPwdEmail(email, new IAsyncServiceVoidCallback() {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "Error while trying to sendResetUserPwd :" + asyncResult.getException().getMessage());

                    if (listener != null) {
                        listener.onFailure(asyncResult.getException());
                    }
                } else {
                    if (listener != null)
                        listener.onSuccess();
                }
            }
        });
    }

    @Override
    public void sendResetUserPwd(String email, String newPassword, String tempToken, final ISendEMailListener listener) {
        Log.getLogger().verbose(LOG_TAG, "sendResetUserPwd with : " + email);

        m_notificationService.sendResetUserPwd(email, newPassword, tempToken, new IAsyncServiceVoidCallback() {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "Error while trying to sendResetUserPwd :" + asyncResult.getException().getMessage());

                    if (listener != null) {
                        listener.onFailure(asyncResult.getException());
                    }
                } else {
                    if (listener != null)
                        listener.onSuccess();
                }
            }
        });
    }
}

package com.ale.infra.proxy.authentication;

import com.ale.infra.http.adapter.concurrent.RainbowServiceException;

/**
 * Created by grobert on 26/10/15.
 */
public interface IAuthentication
{

    void authenticateApplication(String applicationId, String applicationSecret, final IAuthenticationListener listener);

    /**
     * authentication to the Rainbow server
     *
     * @param login
     * @param password
     */
    void authenticate(String login, String password, IAuthenticationListener listener);

    String getToken();

    void disconnectOfRainbowServer(IDisconnectionListener listener);

    interface IAuthenticationErrorListener
    {
        void onAuthenticationError();
    }

    interface IAuthenticationListener
    {
        void onSuccess(AuthenticationResponse response);

        void onFailure(RainbowServiceException exception);
    }

    interface IDisconnectionListener
    {
        void onSuccess();

        void onFailure();
    }
}

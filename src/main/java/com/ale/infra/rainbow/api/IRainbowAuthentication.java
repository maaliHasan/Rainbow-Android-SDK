package com.ale.infra.rainbow.api;

import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.proxy.authentication.AuthenticationResponse;

/**
 * Created by grobert on 26/10/15.
 */
public interface IRainbowAuthentication extends IRainbowService
{

    void authenticate(String login, String password, IAsyncServiceResultCallback<AuthenticationResponse> iAsyncServiceResultCallback);

    void authenticateApplication(IAsyncServiceResultCallback<AuthenticationResponse> iAsyncServiceResultCallback);

    void disconnectOfRainbowServer(String token, IAsyncServiceVoidCallback iAsyncServiceResultCallback);

}

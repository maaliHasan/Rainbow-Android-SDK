package com.ale.infra.http;

import android.content.Context;

import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.proxy.authentication.IAuthentication;
import com.ale.infra.proxy.avatar.GetAvatarResponse;
import com.ale.infra.rainbow.adapter.Range;

import org.json.JSONObject;

import java.io.File;

/**
 * Created by georges on 12/12/2016.
 */

public interface IRESTAsyncRequest
{

    void setContext(Context context);

    void sendGetRequest(String url, IAsyncServiceResultCallback<RESTResult> callback);

    void sendPostRequest(String url, JSONObject jsonBody, IAsyncServiceResultCallback<RESTResult> callback);

    void sendDeleteRequest(String url, IAsyncServiceResultCallback<RESTResult> callback);

    void sendPutRequest(String url, JSONObject jsonBody, IAsyncServiceResultCallback<RESTResult> callback);

    void abort();

    void uploadPhoto(String url, File photoFile, final IAsyncServiceResultCallback<RESTResult> callback);

    void uploadBuffer(String url, byte[] bytesbytes, final IAsyncServiceResultCallback<RESTResult> callback);

    void getAvatarFile(String url, IAsyncServiceResultCallback<GetAvatarResponse> callback);

    void downloadFile(String url, Range range, IAsyncServiceResultCallback<GetFileResponse> callback);

    void getTextFile(String url, IAsyncServiceResultCallback<String> callback);

    void setAuthenticationErrorListenerProxy(IAuthentication.IAuthenticationErrorListener m_authenticationErrorListener);

    void shutdown();
}

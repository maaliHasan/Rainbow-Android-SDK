/******************************************************************************
 * Copyright © 2013 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : cebruckn 16 d�c. 2013
 * *****************************************************************************
 * Defects
 */

package com.ale.infra.http;

import android.content.Context;
import android.os.Build;

import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.proxy.authentication.IAuthentication;
import com.ale.infra.proxy.avatar.GetAvatarResponse;
import com.ale.infra.rainbow.adapter.Range;
import com.ale.security.util.HttpAuthorizationUtil;
import com.ale.security.util.TLSSocketFactory;
import com.ale.util.log.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cebruckn
 */
public class RESTAsyncRequest implements IRESTAsyncRequest {
    private static final String LOG_TAG = "RESTAsyncRequest";

    private static final int MAX_RETRIES = 3;
    private static final int SOCKET_TIMEOUT_MS = 5000;
    private static final float BACKOFF_MULT = 2;

    private RequestQueue m_queue;
    private Context m_context;
    private HurlStack stack;
    private IAuthentication.IAuthenticationErrorListener m_authenticationErrorListener;

    public RESTAsyncRequest(Context context)
    {
        m_context = context;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            try {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    // Use a socket factory that removes sslv3 and add TLS1.2
                    stack = new HurlStack(null, new TLSSocketFactory());
                } else {
                    stack = new HurlStack();
                }
            } catch (Exception e) {
                stack = new HurlStack();
                Log.getLogger().error("RainbowHttpClientFactory", "NetworkClient can no create custom socket factory");
            }
        }

        m_queue = Volley.newRequestQueue(m_context,stack);
    }

    public void setQueue(RequestQueue queue) {
        m_queue = queue;
    }

    @Override
    public void setContext(Context context) {
        m_context = context;
        m_queue = Volley.newRequestQueue(m_context,stack);
    }

    @Override
    public void sendGetRequest(String url, IAsyncServiceResultCallback<RESTResult> callback) {

        sendRequest(Request.Method.GET, url, null, callback);
    }

    @Override
    public void sendPostRequest(String url, JSONObject jsonBody, IAsyncServiceResultCallback<RESTResult> callback)
    {
        sendRequest(Request.Method.POST, url, jsonBody, callback);
    }

    @Override
    public void sendDeleteRequest(String url, IAsyncServiceResultCallback<RESTResult> callback)
    {
        sendRequest(Request.Method.DELETE, url, null, callback);
    }

    @Override
    public void sendPutRequest(String url, JSONObject jsonBody, IAsyncServiceResultCallback<RESTResult> callback)
    {
        sendRequest(Request.Method.PUT, url, jsonBody, callback);
    }

    @Override
    public void abort()
    {
        Log.getLogger().verbose(LOG_TAG, ">abort");

        m_queue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                Log.getLogger().verbose(LOG_TAG, "requests cancelled");
                return true;
            }
        });

        m_queue.stop();
    }

    private void sendRequest(int method, final String url, JSONObject jsonBody, final IAsyncServiceResultCallback<RESTResult> callback)
    {
        // Request a string response from the provided URL.
        JsonObjectRequest jsonRequest = new JsonObjectRequest(method, url, jsonBody,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the first 500 characters of the response string.
                        Log.getLogger().verbose(LOG_TAG, "Response received");
                        if( response.has("token")) {
                            try {
                                if (response.has("loggedInApplication")) {
                                    HttpAuthorizationUtil.setAuthentificationApplicationWithToken(response.getString("token"));
                                } else {
                                    HttpAuthorizationUtil.setAuthentificationWithToken(response.getString("token"));
                                }
                            } catch (JSONException e) {
                                Log.getLogger().error(LOG_TAG, "JSONException: " + e.getMessage());
                            }
                        }
                        notifySuccess(response.toString(), callback);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleError(callback, error, url);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Accept", "application/json");
                params.put("Content-type", "application/json");
                params.put("Authorization", HttpAuthorizationUtil.makeAuthorizationString());
                params.put("X-Rainbow-App-Token", HttpAuthorizationUtil.getAuthenticationApplicationToken());
                params.put("cache-control", "no-cache");
                params.put("X-Rainbow-Client", "android");
                if ( m_context != null ) {
                    try {
                        String packageName = m_context.getPackageName();
                        String version = m_context.getPackageManager().getPackageInfo(packageName,0).versionName;

                        params.put("X-Rainbow-Client-Version", version);
                    } catch (Exception e) {
                        Log.getLogger().verbose(LOG_TAG, "getHeaders no package name");
                        return params;
                    }
                }

                return params;
            }
        };

//        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
//                SOCKET_TIMEOUT_MS,
//                MAX_RETRIES,
//                BACKOFF_MULT));
        // Add the request to the RequestQueue.
        m_queue.add(jsonRequest);
    }

    @Override
    public void uploadBuffer(final String url, final byte[] bytes, final IAsyncServiceResultCallback<RESTResult> callback)
    {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.getLogger().verbose(LOG_TAG, "File uploaded");
                        notifySuccess(response, callback);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleError(callback, error, url);
                    }
                }){
            @Override
            public String getBodyContentType() {
                return "application/octet-stream";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return bytes;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Accept", "application/json");
                params.put("Content-type", "application/octet-stream");
                params.put("Authorization", HttpAuthorizationUtil.makeAuthorizationString());
                params.put("cache-control", "no-cache");
                params.put("x-rainbow-client", "android");
                if (m_context != null) {
                    try {
                        String packageName = m_context.getPackageName();
                        String version = m_context.getPackageManager().getPackageInfo(packageName,0).versionName;

                        params.put("x-rainbow-client-version", version);
                    } catch (Exception e) {
                        Log.getLogger().warn(LOG_TAG, "getHeaders no package name" + e.toString());
                        return params;
                    }
                }
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                SOCKET_TIMEOUT_MS,
                MAX_RETRIES,
                BACKOFF_MULT));

        //Adding request to the queue
        m_queue.add(stringRequest);
    }


    public byte[] readInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream ous = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();

            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        }finally {
            try {
                if (ous != null)
                    ous.close();
            } catch (IOException e) {
                Log.getLogger().error(LOG_TAG, "Exception while closing ous : ", e);
            }

            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                Log.getLogger().error(LOG_TAG, "Exception while closing ios : ", e);
            }
        }
        return ous.toByteArray();
    }


    // TODO remove this method and use uploadFile
    @Override
    public void uploadPhoto(final String url, final File photoFile, final IAsyncServiceResultCallback<RESTResult> callback)
    {

        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.getLogger().verbose(LOG_TAG, "File uploaded");
                        notifySuccess(response, callback);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleError(callback, error, url);
                    }
                }){
            @Override
            public String getBodyContentType() {
                return "image/jpeg";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return readFile(photoFile);
                } catch (IOException error) {
                    Log.getLogger().warn(LOG_TAG, "IOException Error; "+error.getMessage());
                }
                return null;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Accept", "application/json");
                params.put("Content-type", "image/jpeg");
                params.put("Authorization", HttpAuthorizationUtil.makeAuthorizationString());
                params.put("cache-control", "no-cache");
                params.put("x-rainbow-client", "android");
                if (m_context != null) {
                    try {
                        String packageName = m_context.getPackageName();
                        String version = m_context.getPackageManager().getPackageInfo(packageName,0).versionName;

                        params.put("x-rainbow-client-version", version);
                    } catch (Exception e) {
                        Log.getLogger().warn(LOG_TAG, "getHeaders no package name" + e.toString());
                        return params;
                    }
                }
                return params;
            }
        };

        //Adding request to the queue
        m_queue.add(stringRequest);
    }

    public byte[] readFile(File file) throws IOException {
        ByteArrayOutputStream ous = null;
        InputStream ios = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(file);
            int read;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        }finally {
            try {
                if (ous != null)
                    ous.close();
            } catch (IOException e) {
                Log.getLogger().error(LOG_TAG, "Exception while closing ous : ", e);
            }

            try {
                if (ios != null)
                    ios.close();
            } catch (IOException e) {
                Log.getLogger().error(LOG_TAG, "Exception while closing ios : ", e);
            }
        }
        return ous.toByteArray();
    }

    private void handleError(IAsyncServiceResultCallback<RESTResult> callback, VolleyError e, String url)
    {
        Log.getLogger().error(LOG_TAG, "Error while executing REST request : " + url + " Error: " + e);

        if(e instanceof AuthFailureError)
        {
            RainbowServiceException rainbowEx = new RainbowServiceException(e);

            if (e.networkResponse.statusCode == 401)
            {
                switch (rainbowEx.getDetailsCode()) {
                    case 401500:
                    case 401501:
                    case 401520:
                    case 401521:
                        //wrong login or passwd or account deactivated
                        //do not re authenticate
                        break;
                    default:
                        if (m_authenticationErrorListener != null && !rainbowEx.getDetailsMessage().equals("invalid_token")) // To avoid reconnection when token app is invalid
                            m_authenticationErrorListener.onAuthenticationError();
                }
            }
            else if( e.networkResponse.statusCode != 403 && m_authenticationErrorListener != null)
                m_authenticationErrorListener.onAuthenticationError();
        }

        notifyError(e, callback);
    }

    private void notifyError(Exception e, IAsyncServiceResultCallback<RESTResult> callback)
    {
        AsyncServiceResponseResult<RESTResult> result = new AsyncServiceResponseResult<>(new RainbowServiceException(e), null);
        callback.handleResult(result);
    }

    private void notifySuccess(final String response, final IAsyncServiceResultCallback<RESTResult> callback)
    {
        Thread restRespThread = new Thread(new Runnable() {
            @Override
            public void run() {
                RESTResult restResult = new RESTResult(response);
                AsyncServiceResponseResult<RESTResult> result = new AsyncServiceResponseResult<>(null, restResult);
                callback.handleResult(result);
            }
        }, "RestResponse");
        restRespThread.start();
    }

    @Override
    public void getAvatarFile(String url, final IAsyncServiceResultCallback<GetAvatarResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">getAvatarFile");

        final InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, url, new InputStreamVolleyRequest.InputStreamListener<byte[]>() {
            @Override
            public void onResponse(byte[] response,InputStreamVolleyRequest request) {
                Log.getLogger().verbose(LOG_TAG, "getAvatarFile onResponse");
                try {
                    if (response!=null) {
                        GetAvatarResponse avatarResponse = new GetAvatarResponse(response);
                        notifyBitmapSuccess(avatarResponse, callback);
                    }
                } catch (Exception error) {
                    Log.getLogger().warn(LOG_TAG, "UNABLE TO DOWNLOAD Avatar; "+error.getMessage());
                    handleBitmapError(callback, error);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.getLogger().error(LOG_TAG, "getAvatarFile onErrorResponse; "+ error.getMessage());
                handleBitmapError(callback, error);
            }
        }, null){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Accept", "application/json");
                params.put("Authorization", HttpAuthorizationUtil.makeAuthorizationString());
                params.put("cache-control", "no-cache");
                params.put("x-rainbow-client", "android");
                if (m_context != null) {
                    try {
                        String packageName = m_context.getPackageName();
                        String version = m_context.getPackageManager().getPackageInfo(packageName,0).versionName;

                        params.put("x-rainbow-client-version", version);
                    } catch (Exception e) {
                        Log.getLogger().warn(LOG_TAG, "getHeaders no package name" + e.toString());
                        return params;
                    }
                }
                return params;
            }
        };
        m_queue.add(request);
    }

    private void notifyBitmapSuccess(final GetAvatarResponse response, final IAsyncServiceResultCallback<GetAvatarResponse> callback)
    {
        Thread restRespThread = new Thread(new Runnable() {
            @Override
            public void run() {
                AsyncServiceResponseResult<GetAvatarResponse> result = new AsyncServiceResponseResult<>(null, response);
                callback.handleResult(result);
            }
        }, "RestBitmapResponse");
        restRespThread.start();
    }

    private void handleBitmapError(final IAsyncServiceResultCallback<GetAvatarResponse> callback, final Exception e)
    {
        Thread restRespThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.getLogger().error(LOG_TAG, "Error while executing REST request : ", e);
                AsyncServiceResponseResult<GetAvatarResponse> result = new AsyncServiceResponseResult<>(new RainbowServiceException(e), null);
                callback.handleResult(result);
            }
        }, "RestBitmapErrorResponse");
        restRespThread.start();
    }

    @Override
    public void downloadFile(String url, final Range range, final IAsyncServiceResultCallback<GetFileResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">downloadFile");

        final InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, url, new InputStreamVolleyRequest.InputStreamListener<byte[]>() {
            @Override
            public void onResponse(byte[] response,InputStreamVolleyRequest request) {
                Log.getLogger().verbose(LOG_TAG, "getFile onResponse");
                try {
                    if (response!=null) {
                        Map<String, String> responseHeaders = request.getResponseHeaders();
                        String contentRange = responseHeaders.get("Content-Range");

                        GetFileResponse fileResponse = new GetFileResponse(response);
                        fileResponse.setContentRange(contentRange);
                        notifyFileDownloadSuccess(fileResponse, callback);
                    }
                } catch (Exception error) {
                    Log.getLogger().warn(LOG_TAG, "UNABLE TO DOWNLOAD File; "+error.getMessage());
                    notifyFileDownloadError(callback, error);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.getLogger().error(LOG_TAG, "getFile onErrorResponse; "+ error.getMessage());
                notifyFileDownloadError(callback, error);
            }
        }, null){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Accept", "application/json");
                params.put("Authorization", HttpAuthorizationUtil.makeAuthorizationString());
                params.put("cache-control", "no-cache");
                if( range != null) {
                    String rangeStrg = String.format("bytes=%d-%d",range.getStart(),range.getEnd());
                    params.put("range",rangeStrg);
                }
                params.put("x-rainbow-client", "android");
                if (m_context != null) {
                    try {
                        String packageName = m_context.getPackageName();
                        String version = m_context.getPackageManager().getPackageInfo(packageName,0).versionName;

                        params.put("x-rainbow-client-version", version);
                    } catch (Exception e) {
                        Log.getLogger().warn(LOG_TAG, "getHeaders no package name" + e.toString());
                        return params;
                    }
                }
                return params;
            }
        };
        m_queue.add(request);
    }

    private void notifyFileDownloadSuccess(final GetFileResponse response, final IAsyncServiceResultCallback<GetFileResponse> callback)
    {
        Thread restRespThread = new Thread(new Runnable() {
            @Override
            public void run() {
                AsyncServiceResponseResult<GetFileResponse> result = new AsyncServiceResponseResult<>(null, response);
                callback.handleResult(result);
            }
        }, "RestBitmapResponse");
        restRespThread.start();
    }

    private void notifyFileDownloadError(final IAsyncServiceResultCallback<GetFileResponse> callback, final Exception e)
    {
        Thread restRespThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.getLogger().error(LOG_TAG, "Error while executing REST request : ", e);
                AsyncServiceResponseResult<GetFileResponse> result = new AsyncServiceResponseResult<>(new RainbowServiceException(e), null);
                callback.handleResult(result);
            }
        }, "RestBitmapErrorResponse");
        restRespThread.start();
    }

    @Override
    public void getTextFile(String url, final IAsyncServiceResultCallback<String> callback) {
        Log.getLogger().verbose(LOG_TAG, ">getTextFile");

        final InputTextVolleyRequest request = new InputTextVolleyRequest(Request.Method.GET, url, new Response.Listener<GetTextResponse>() {
            @Override
            public void onResponse(GetTextResponse response) {
                Log.getLogger().verbose(LOG_TAG, "getTextFile onResponse");
                try {
                    if (response!=null) {
                        notifGetFileResultSuccess(callback, response.getContent());
                    }
                } catch (Exception error) {
                    Log.getLogger().warn(LOG_TAG, "UNABLE TO DOWNLOAD FILE; "+error.getMessage());
                    notifGetFileResultFailure(callback, error);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.getLogger().error(LOG_TAG, "getTextFile onErrorResponse; "+ error.getMessage());
                notifGetFileResultFailure(callback, error);
            }
        }, null);
        m_queue.add(request);
    }

    @Override
    public void setAuthenticationErrorListenerProxy(IAuthentication.IAuthenticationErrorListener authenticationErrorListener)
    {
        m_authenticationErrorListener = authenticationErrorListener;
    }

    private void notifGetFileResultSuccess(final IAsyncServiceResultCallback<String> callback, final String content)
    {
        Thread restRespThread = new Thread(new Runnable() {
            @Override
            public void run() {
                AsyncServiceResponseResult<String> result = new AsyncServiceResponseResult<>(null, content);
                callback.handleResult(result);
            }
        }, "RestGetFileResponse");
        restRespThread.start();
    }

    private void notifGetFileResultFailure(final IAsyncServiceResultCallback<String> callback, final Exception e)
    {
        Thread restRespThread = new Thread(new Runnable() {
            @Override
            public void run() {
                AsyncServiceResponseResult<String> result = new AsyncServiceResponseResult<>(new RainbowServiceException(e), null);
                callback.handleResult(result);
            }
        }, "RestGetFileFailure");
        restRespThread.start();
    }

}

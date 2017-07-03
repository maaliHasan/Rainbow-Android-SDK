package com.ale.infra.http;


import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by georges on 03/11/2016.
 */

public class InputStreamVolleyRequest extends Request<byte[]> {

    public interface InputStreamListener<T> {
        /** Called when a response is received. */
        public void onResponse(T response,InputStreamVolleyRequest request);
    }


    private final InputStreamListener<byte[]> m_listener;
    //create a static map for directly accessing headers
    public Map<String, String> m_responseHeaders;
    private Map<String, String> m_params;

    public InputStreamVolleyRequest(int post, String mUrl, InputStreamListener<byte[]> listener,
                                    Response.ErrorListener errorListener, HashMap<String, String> params) {
        // TODO Auto-generated constructor stub

        super(post, mUrl, errorListener);
        // this request would never use cache since you are fetching the file m_content from server
        setShouldCache(false);
        m_listener = listener;
        m_params=params;
    }

    @Override
    protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
        return m_params;
    };

    public Map<String, String> getResponseHeaders() {
        return m_responseHeaders;
    }


    @Override
    protected void deliverResponse(byte[] response) {

        try {
            m_listener.onResponse(response, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        //Initialise local responseHeaders map with response headers received
        m_responseHeaders = response.headers;

        //Pass the response data here
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
    }
}

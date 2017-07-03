package com.ale.infra.http;

import com.ale.infra.proxy.avatar.GetAvatarResponse;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by georges on 03/11/2016.
 */

public class InputTextVolleyRequest extends Request<String> {

    private final Response.Listener<GetTextResponse> m_listener;
    //create a static map for directly accessing headers
    public Map<String, String> m_responseHeaders;
    private Map<String, String> m_params;

    public InputTextVolleyRequest(int post, String mUrl, Response.Listener<GetTextResponse> listener,
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


    @Override
    protected void deliverResponse(String response) {

        try {
            GetTextResponse avatarResp = new GetTextResponse(response);

            m_listener.onResponse(avatarResp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        //Initialise local responseHeaders map with response headers received
        m_responseHeaders = response.headers;

        //Pass the response data here
        return Response.success(new String(response.data), HttpHeaderParser.parseCacheHeaders(response));
    }
}

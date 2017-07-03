package com.ale.infra.http;

import com.ale.infra.proxy.framework.RestResponse;


/**
 * Created by wilsius on 25/05/16.
 */
public class GetTextResponse extends RestResponse
{
    private static final String LOG_TAG = "GetTextResponse";

    String m_content;

    public GetTextResponse(String result) throws Exception
    {
        m_content = result;
    }

    public String getContent() {
        return m_content;
    }

    public void setContent(String content) {
        this.m_content = content;
    }

}

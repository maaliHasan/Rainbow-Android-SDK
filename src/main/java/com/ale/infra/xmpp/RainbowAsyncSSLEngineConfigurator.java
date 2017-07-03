package com.ale.infra.xmpp;

import com.ale.util.log.Log;
import com.koushikdutta.async.http.AsyncHttpClientMiddleware;
import com.koushikdutta.async.http.AsyncSSLEngineConfigurator;

import javax.net.ssl.SSLEngine;

/**
 * Created by cebruckn on 27/06/2017.
 */

public class RainbowAsyncSSLEngineConfigurator implements AsyncSSLEngineConfigurator
{
    private static final String LOG_TAG = "RainbowAsyncSSLEngineConfigurator";

    @Override
    public void configureEngine(SSLEngine engine, AsyncHttpClientMiddleware.GetSocketData data, String host, int port)
    {
        try
        {
            String[] enableProtocols = new String[]{"TLSv1.1", "TLSv1.2"};
            engine.setEnabledProtocols(enableProtocols);
        }
        catch (IllegalArgumentException e)
        {
            Log.getLogger().error(LOG_TAG, "Impossible to force use of TLSv1.1 or TLSv1.2 : ", e);
        }
    }
}

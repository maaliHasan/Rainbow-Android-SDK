package com.ale.infra.xmpp;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Created by cebruckn on 27/06/2017.
 */

public class RainbowHostnameVerifier implements HostnameVerifier
{
    @Override
    public boolean verify(String hostname, SSLSession session)
    {
        return hostname.contains("rainbow");
    }
}

/**
 *
 */
package com.ale.security.util;

import com.ale.util.log.Log;

import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * An SSLContext
 */
public class SecurityContext
{
    private static final String LOG_TAG = "SecurityContext";

    private static final String SSLCONTEXT_PROTOCOL_TLS = "TLS";
    private TrustManager[] m_trustManagers = null;
    private SSLContext m_sslContext;

    /**
     * Construct a new SSLContext with the specified store root directory.
     *
     * @throws SSLInitException in case of error.
     */
    SecurityContext() throws SSLInitException
    {

        m_trustManagers = createTrustManagers();
    }

    synchronized private TrustManager[] createTrustManagers() throws SSLInitException
    {
        // Add application trustore in TrustManager
        TrustManagerFactory tmf;
        try
        {
            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];

            return new TrustManager[]{defaultTrustManager};
        }
        catch (Exception e)
        {
            throw new SSLInitException(e);
        }
    }

    public synchronized void initializeSslContext() throws SSLInitException
    {
        try
        {
            m_sslContext = SSLContext.getInstance(SSLCONTEXT_PROTOCOL_TLS);
            m_sslContext.init(null, m_trustManagers, new SecureRandom());

            String[] protocols = m_sslContext.getSupportedSSLParameters().getProtocols();
            for (String protocol : protocols) {
                Log.getLogger().info(LOG_TAG, "Context supported protocol: " + protocol);
            }

        }
        catch (Exception e)
        {
            throw new SSLInitException(e);
        }
    }

    public SSLContext getSslContext()
    {
        return m_sslContext;
    }

    public TrustManager[] getTrustManagers()
    {
        return m_trustManagers;
    }
}

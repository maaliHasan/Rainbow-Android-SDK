/**
 *
 */
package com.ale.security.util;

/**
 * SSL Utility class. Create the trust store for server authentication and the key store for client
 * authentication. These store can be populated with certificates.
 */
public class SSLUtil
{
    private static SecurityContext m_securityContext = null;

    /**
     * Initialize the trust store and the key store.
     *
     * @throws SSLInitException if there is an error during stores initialization, or SSL configuration
     */
    public static void initialize() throws SSLInitException
    {
        if (m_securityContext != null)
        {
            throw new IllegalStateException("SSL already initialized");
        }

        m_securityContext = new SecurityContext();

        m_securityContext.initializeSslContext();
    }

    /**
     * Get the instance of the SSL context.
     *
     * @return
     */
    public static SecurityContext getSecurityContext()
    {
        if (m_securityContext == null)
        {
            throw new IllegalStateException("SSL not initialized");
        }

        return m_securityContext;
    }
}

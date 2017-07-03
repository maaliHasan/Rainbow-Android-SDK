/**
 * 2013/02/26 cebruckn crms00423065 [Migration]-Old URLs considered as faulty
 */
package com.ale.security.util;

/**
 * An authorization context, used to store authorization for BASIC http authentication.<br>
 * This version manage only one basic credential, and not associated with anty realm. To extend the
 * behavior if needed, BasicCredential should be in a Map indexed by the realm.
 */
public class AuthorizationContext
{
    private String m_token = null;
    private String m_tokenApplication = "";
    private BasicCredential credential = null;

    public void setAuthorizationCredential(BasicCredential credential)
    {
        this.credential = credential;
    }

    public void setAuthorizationToken(String token)
    {
        this.m_token = token;
    }

    /**
     * Make a Authorized string
     *
     * @return
     */
    public String makeAuthorizationString()
    {
        if (m_token != null)
        {
            return "Bearer " + m_token;
        }
        else
        {
            if (credential == null)
            {
                return null;
            }
            else
            {
                StringBuilder buffer = new StringBuilder();
                buffer.append(credential.getLogin());
                buffer.append(":");
                buffer.append(credential.getPassword());

                return "Basic " + Base64Encoder.encodeString(buffer.toString());
            }
        }
    }

    public void setAuthorizationApplicationToken(String token) {
        this.m_tokenApplication = token;
    }

    public String getAuthenticationApplicationToken() {
        return "Bearer " + m_tokenApplication;
    }
}

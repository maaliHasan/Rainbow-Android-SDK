/**
 *
 */
package com.ale.security.util;


import com.android.volley.toolbox.Volley;

/**
 * HttpAuthorizationUtil group method to perform http authentication.
 */
public class HttpAuthorizationUtil {
    private static long m_timeExpired = 0;
    private static AuthorizationContext context;
    private static String m_login;
    private static String m_pwd;

    /**
     * Get the AuthorizationContext unique instance.
     */
    private static AuthorizationContext getContext() {
        if (context == null) {
            context = new AuthorizationContext();
        }

        return context;
    }

    public static void setAuthentificationWithToken(String token) {
        getContext().setAuthorizationToken(token);
    }

    public static void setAuthentificationApplicationWithToken(String token) {
        getContext().setAuthorizationApplicationToken(token);
    }

    public static String getAuthenticationApplicationToken() {
        return getContext().getAuthenticationApplicationToken();
    }

    public static void setAuthorizationCredential(String login, String password) {
        m_login = login;
        m_pwd = password;
        if (login == null) {
            getContext().setAuthorizationCredential(null);
        } else {
            BasicCredential credential = new BasicCredential(login, password);
            getContext().setAuthorizationCredential(credential);
        }
    }

    /**
     * Make an authorization string.
     */
    public static String makeAuthorizationString() {
        return getContext().makeAuthorizationString();
    }

    public static long getTimeExpired() {
        return m_timeExpired;
    }

    public static void setTimeExpired(long timeExpired) {
        m_timeExpired = timeExpired;
    }

    public static String getLogin() {
        return m_login;
    }

    public static String getPwd() {
        return m_pwd;
    }
}

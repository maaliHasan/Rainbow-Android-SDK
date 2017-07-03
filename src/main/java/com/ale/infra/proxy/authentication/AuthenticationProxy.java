package com.ale.infra.proxy.authentication;

import android.util.Base64;

import com.ale.infra.application.IApplicationData;
import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseVoid;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.rainbow.api.IRainbowAuthentication;
import com.ale.infra.rainbow.api.IServicesFactory;
import com.ale.security.util.HttpAuthorizationUtil;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;


/**
 * Created by grobert on 26/10/15.
 */
public class AuthenticationProxy implements IAuthentication
{
    private static final String LOG_TAG = "AuthenticationProxy";
    private final Contact m_user;
    private final IRESTAsyncRequest m_restAsyncRequest;
    private IRainbowAuthentication m_authenticationService;
    private String m_token;
    private IAuthenticationErrorListener m_authenticationErrorListener = new IAuthenticationErrorListener()
    {
        @Override
        public void onAuthenticationError()
        {
            reauthenticate();
        }
    };

    public AuthenticationProxy(IServicesFactory servicesFactory, IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService, Contact user)
    {
        Log.getLogger().info(LOG_TAG, "initialization");
        m_authenticationService = servicesFactory.createAuthenticationService(restAsyncRequest, platformService);
        m_user = user;
        m_restAsyncRequest = restAsyncRequest;
        m_restAsyncRequest.setAuthenticationErrorListenerProxy(m_authenticationErrorListener);
    }

    @Override
    public void authenticate(String login, String password, final IAuthenticationListener listener)
    {
        Log.getLogger().verbose(LOG_TAG, "authenticate with login; " + login);

        HttpAuthorizationUtil.setAuthorizationCredential(login, password);
        HttpAuthorizationUtil.setAuthentificationWithToken(null);

        m_authenticationService.authenticate(login, password, new IAsyncServiceResultCallback<AuthenticationResponse>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<AuthenticationResponse> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "Error while trying to authenticate :" + asyncResult.getException().getCause());

                    RainbowServiceException exception = asyncResult.getException();
                    Log.getLogger().verbose(LOG_TAG, "Exception=" + exception.getStatusCode());

                    if (listener != null)
                        listener.onFailure(exception);

                    return;
                }

                AuthenticationResponse response = asyncResult.getResult();
                Log.getLogger().verbose(LOG_TAG, "authentication success:" + response.toString());

                // store credentials into ApplicationData:
                IApplicationData data = RainbowContext.getPlatformServices().getApplicationData();
                data.setUserId(response.getUserId());
                data.setCompanyId(response.getCompanyId());
                data.setUserJidIm(response.getJidIm());
                data.setUserJidTel(response.getJidTel());
                data.setUserJidPassword(response.getJidPwd());

                updateToken(response);

                if (m_user.getDirectoryContact() == null)
                {
                    DirectoryContact directoryContact = new DirectoryContact();
                    directoryContact.setCorporateId(response.getUserId());
                    directoryContact.setCompanyId(response.getCompanyId());
                    directoryContact.setImJabberId(response.getJidIm());
                    directoryContact.setJidTel(response.getJidTel());
                    directoryContact.setIsDefaultCompany(response.isCompanyDefault());
                    m_user.setDirectoryContact(directoryContact);
                }
                m_user.getDirectoryContact().setProfiles(response.getProfiles());

                if (listener != null)
                    listener.onSuccess(response);
            }
        });
    }

    @Override
    public void authenticateApplication(String applicationId, String applicationSecret, final IAuthenticationListener listener) {
        Log.getLogger().verbose(LOG_TAG, "authenticate application with following applicationId: " + applicationId);

        HttpAuthorizationUtil.setAuthorizationCredential(applicationId, applicationSecret);
        HttpAuthorizationUtil.setAuthentificationWithToken(null);

        m_authenticationService.authenticateApplication(new IAsyncServiceResultCallback<AuthenticationResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<AuthenticationResponse> asyncResult) {
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "Error while trying to authenticate the application: " + asyncResult.getException().getCause());

                    RainbowServiceException exception = asyncResult.getException();
                    Log.getLogger().verbose(LOG_TAG, "Exception=" + exception.getStatusCode());

                    if (listener != null)
                        listener.onFailure(exception);

                    return;
                }

                if (listener != null)
                    listener.onSuccess(asyncResult.getResult());
            }
        });
    }

    private void reauthenticate()
    {
        Log.getLogger().verbose(LOG_TAG, "reauthenticate");

        HttpAuthorizationUtil.setAuthentificationWithToken(null);

        m_authenticationService.authenticate(HttpAuthorizationUtil.getLogin(), HttpAuthorizationUtil.getPwd(), new IAsyncServiceResultCallback<AuthenticationResponse>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<AuthenticationResponse> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "Error while trying to reauthenticate :" + asyncResult.getException().getCause());

                    RainbowServiceException exception = asyncResult.getException();
                    Log.getLogger().verbose(LOG_TAG, "Exception=" + exception.getStatusCode());
                }
                else
                {

                    AuthenticationResponse response = asyncResult.getResult();
                    Log.getLogger().verbose(LOG_TAG, "authentication success:" + response.toString());

                    updateToken(response);
                }
            }
        });
    }

    private void updateToken(AuthenticationResponse response)
    {
        String token = response.getToken();
        m_token = token;
        HttpAuthorizationUtil.setAuthentificationWithToken(token);
        long timeExpired = getExpiredTime(token);
        if (timeExpired != 0)
        {
            HttpAuthorizationUtil.setTimeExpired(timeExpired * 1000);
        }
    }

    @Override
    public String getToken()
    {
        return m_token;
    }

    @Override
    public void disconnectOfRainbowServer(final IDisconnectionListener listener)
    {
        Log.getLogger().verbose(LOG_TAG, "Disconnection of the rainbow server");

        m_restAsyncRequest.setAuthenticationErrorListenerProxy(null);

        if (StringsUtil.isNullOrEmpty(m_token))
            return;

        m_authenticationService.disconnectOfRainbowServer(getToken(), new IAsyncServiceVoidCallback()
        {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult)
            {
                if (listener != null)
                {
                    if (asyncResult.exceptionRaised())
                        listener.onFailure();
                    else
                        listener.onSuccess();
                }
            }
        });
    }

    private long getExpiredTime(String token)
    {
        //String value = m_token.split(".")[0];
        if (token.contains("."))
        {
            String[] tokenSplitted = token.split("\\.");
            String payloadCoded = tokenSplitted[1];
            byte[] data = Base64.decode(payloadCoded, Base64.DEFAULT);
            String payloadDecoded = null;
            try
            {
                payloadDecoded = new String(data, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
            if (payloadDecoded != null)
            {
                try
                {
                    JSONObject payloadJson = new JSONObject(payloadDecoded);
                    return Long.parseLong(payloadJson.getString("exp"));
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }
}

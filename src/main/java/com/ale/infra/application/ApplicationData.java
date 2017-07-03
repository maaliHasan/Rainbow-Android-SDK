/******************************************************************************
 * Copyright © 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : geyer2 13 oct. 2011
 * *****************************************************************************
 * Defects
 * 2013/06/11 cebruckn crms00441038 [OXO OBS]user password to be better protected in source code
 * 2013/11/13 cebruckn crms00466774 [Favorites]-Adapt to OT 2.0 favorites
 */

package com.ale.infra.application;

import com.ale.infra.datastorage.IDataStorage;
import com.ale.infra.datastorage.RainbowCredentials;
import com.ale.security.util.CryptUtil;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Get a way to backup and restore internal application data in a persistent store. IApplicationData
 * describes the datas that can be backuped.
 *
 * @author geyer2
 */
public class ApplicationData implements IApplicationData
{
    private static final String LOG_TAG = "ApplicationData";

    // This parameter contains the login used to login the framework
    private static final String USER_LOGIN_PARAM = "rainbow.parameters.login";
    private static final String USER_PASSWORD_PARAM = "rainbow.parameters.password";
    private static final String USER_JID_IM_PARAM = "rainbow.parameters.jid.im";
    private static final String USER_JID_TEL_PARAM = "rainbow.parameters.jid.tel";
    private static final String USER_JID_PASSWORD_PARAM = "rainbow.parameters.jid.password";
    private static final String USER_ID_PARAM = "rainbow.parameters.user.id";
    private static final String COMPANY_ID_PARAM = "rainbow.parameters.company.id";
    private static final String OT_URL_PARAM = "rainbow.parameters.url";
    private static final String OT_SERVER_TYPE_PARAM = "rainbow.parameters.server.type";
    private static final String EULA_ACCEPTED_PARAM = "rainbow.parameters.eula";
    private static final String KEY_PHRASE = "rainbow.parameters.key.phrase";
    private static final String FIRST_LAUNCH_PARAM = "rainbow.parameters.first.launch";
    private static final String SCREEN_FIRST_LAUNCH = "rainbow.parameters.screen.first.launch";
    private static final String LOGOUT = "rainbow.parameters.logout";
    private static final String GOOGLE_PUSH_TOKEN_PARAM = "rainbow.parameters.google.push";
    private static final String ALLOW_PRIVATE_LOG_INFO_ENABLE = "rainbow.parameters.allow.all.log";
    private static final String REFERRER_LOGIN_EMAIL = "rainbow.parameters.referrer.login.email";
    private static final String REFERRER_INVITATION_ID = "rainbow.parameters.referrer.invitation.id";
    private static final String REFERRER_COMPANY_NAME = "rainbow.parameters.referrer.company.name";


    private static final String RAINBOW_SERVER_PROD_TYPE = "PROD";
    private static final String RAINBOW_PROD_DOMAIN = "openrainbow.com";
    private static final String RAINBOW_PROD_URL = "https://" + RAINBOW_PROD_DOMAIN;

    private static final String RAINBOW_VOCAL_READING_MESSAGES = "rainbow.parameters.vocal.reading.messages";

    private IDataStorage m_dataStorage;

    public ApplicationData(IDataStorage dataStorage)
    {
        m_dataStorage = dataStorage;
    }

    @Override
    public String getUserPassword()
    {
        String cryptedPassword = m_dataStorage.getValue(USER_PASSWORD_PARAM, null);
        try
        {
            return CryptUtil.decode(cryptedPassword);
        }
        catch (Exception e)
        {
            Log.getLogger().error(LOG_TAG, "Unable to get password", e);
            return null;
        }
    }

    @Override
    public void setUserPassword(String password)
    {
        try
        {
            if (password == null)
            {
                m_dataStorage.setValue(USER_PASSWORD_PARAM, null);
            }
            else
            {
                m_dataStorage.setValue(USER_PASSWORD_PARAM, CryptUtil.encode(password));
            }
        }
        catch (Exception e)
        {
            Log.getLogger().error(LOG_TAG, "Unable to store password", e);
        }
    }

    @Override
    public String getUserLogin()
    {
        return m_dataStorage.getValue(USER_LOGIN_PARAM, null);
    }

    @Override
    public void setUserLogin(String userLogin)
    {
        Log.getLogger().info(LOG_TAG, "Save login : " + userLogin);
        if (StringsUtil.isNullOrEmpty(userLogin))
            m_dataStorage.setValue(USER_LOGIN_PARAM, userLogin);
        else
            m_dataStorage.setValue(USER_LOGIN_PARAM, userLogin.trim());
    }

    @Override
    public String getServerUrl()
    {
        String url = RAINBOW_PROD_URL;
        if (getHost() != null) {
            url = "https://" + getHost();
        }
        return m_dataStorage.getValue(OT_URL_PARAM, url);
    }

    @Override
    public void setServerUrl(String serverUrl)
    {
        Log.getLogger().info(LOG_TAG, "Server Url : " + serverUrl);
        if (!serverUrl.startsWith("http"))
        {
            Log.getLogger().debug(LOG_TAG, "Add http scheme to Url");
            serverUrl = String.format("https://%s", serverUrl);
        }
        m_dataStorage.setValue(OT_URL_PARAM, serverUrl);
    }

    @Override
    public String getHost()
    {
        return m_dataStorage.getValue("host", null);
    }

    @Override
    public void setHost(String host)
    {
        m_dataStorage.setValue("host", host);
    }

    @Override
    public String getDefaultHost()
    {
        return RAINBOW_PROD_DOMAIN;
    }

    @Override
    public Boolean isServerProdType()
    {
        return (getServerType().equals(RAINBOW_SERVER_PROD_TYPE));
    }

    @Override
    public String getServerType()
    {
        return m_dataStorage.getValue(OT_SERVER_TYPE_PARAM, RAINBOW_SERVER_PROD_TYPE);
    }

    @Override
    public void setServerType(String serverType)
    {
        Log.getLogger().info(LOG_TAG, "Server Type : " + serverType);
        m_dataStorage.setValue(OT_SERVER_TYPE_PARAM, serverType);
    }

    @Override
    public String getUserId()
    {
        return m_dataStorage.getValue(USER_ID_PARAM, null);
    }

    @Override
    public void setUserId(String id)
    {
        m_dataStorage.setValue(USER_ID_PARAM, id);
    }

    @Override
    public String getCompanyId()
    {
        return m_dataStorage.getValue(COMPANY_ID_PARAM, null);
    }

    @Override
    public void setCompanyId(String id)
    {
        m_dataStorage.setValue(COMPANY_ID_PARAM, id);
    }

    @Override
    public String getUserJidIm()
    {
        return m_dataStorage.getValue(USER_JID_IM_PARAM, null);
    }

    @Override
    public void setUserJidIm(String jidIm)
    {
        Log.getLogger().info(LOG_TAG, "Save im jid : " + jidIm);
        m_dataStorage.setValue(USER_JID_IM_PARAM, jidIm);
    }

    @Override
    public String getUserJidTel()
    {
        return m_dataStorage.getValue(USER_JID_TEL_PARAM, null);
    }

    @Override
    public void setUserJidTel(String jidTel)
    {
        Log.getLogger().info(LOG_TAG, "Save tel jid : " + jidTel);
        m_dataStorage.setValue(USER_JID_TEL_PARAM, jidTel);
    }

    @Override
    public String getUserJidPassword()
    {
        String cryptedPassword = m_dataStorage.getValue(USER_JID_PASSWORD_PARAM, null);
        try
        {
            return CryptUtil.decode(cryptedPassword);
        }
        catch (Exception e)
        {
            Log.getLogger().error(LOG_TAG, "Unable to get password", e);
            return null;
        }
    }

    @Override
    public void setUserJidPassword(String password)
    {
        try
        {
            if (password == null)
            {
                m_dataStorage.setValue(USER_JID_PASSWORD_PARAM, null);
            }
            else
            {
                m_dataStorage.setValue(USER_JID_PASSWORD_PARAM, CryptUtil.encode(password));
            }
        }
        catch (Exception e)
        {
            Log.getLogger().error(LOG_TAG, "Unable to store password", e);
        }
    }

    @Override
    public void clear()
    {
        if (m_dataStorage != null)
        {
            Log.getLogger().verbose(LOG_TAG, "clear");
            m_dataStorage.clear();
            setUserLogin(null);
            setUserPassword(null);
        }
    }

    @Override
    public String getKeyPhrase()
    {
        return m_dataStorage.getValue(KEY_PHRASE, null);
    }

    @Override
    public void setKeyPhrase(String keyPhrase)
    {
        m_dataStorage.setValue(KEY_PHRASE, keyPhrase);
    }

    @Override
    public boolean isEulaAccepted()
    {
        return m_dataStorage.getValue(EULA_ACCEPTED_PARAM, false);
    }

    @Override
    public void setEulaAccepted(boolean accepted)
    {
        m_dataStorage.setValue(EULA_ACCEPTED_PARAM, accepted);
    }

    @Override
    public boolean isFirstLaunch()
    {
        return m_dataStorage.getValue(FIRST_LAUNCH_PARAM, false);
    }

    @Override
    public void setFirstLaunch(boolean firstLaunch)
    {
        m_dataStorage.setValue(FIRST_LAUNCH_PARAM, firstLaunch);
    }

    @Override
    public void setGooglePushToken(String token) {
        m_dataStorage.setValue(GOOGLE_PUSH_TOKEN_PARAM, token);
    }

    @Override
    public String getGooglePushToken() {
        return m_dataStorage.getValue(GOOGLE_PUSH_TOKEN_PARAM, null);
    }

    @Override
    public boolean isLoggedOut()
    {
        return m_dataStorage.getValue(LOGOUT, false);
    }

    @Override
    public void setLoggedOut(boolean val)
    {
        m_dataStorage.setValue(LOGOUT, val);
    }

    @Override
    public boolean isPrivateLogEnable() {
        return m_dataStorage.getValue(ALLOW_PRIVATE_LOG_INFO_ENABLE, false);
    };
    @Override
    public void setPrivateLogEnable(boolean val){
        m_dataStorage.setValue(ALLOW_PRIVATE_LOG_INFO_ENABLE, val);
    }

    @Override
    public boolean isFirstScreenDisplayed() {
        return m_dataStorage.getValue(SCREEN_FIRST_LAUNCH, false);
    }

    @Override
    public void setFirstScreenDisplayed(boolean firstScreenDisplayed) {
        m_dataStorage.setValue(SCREEN_FIRST_LAUNCH, firstScreenDisplayed);
    }

    @Override
    public String getReferrerLogin() {
        return m_dataStorage.getValue(REFERRER_LOGIN_EMAIL, null);
    }

    @Override
    public void setReferrerLogin(String loginEmail) {
        m_dataStorage.setValue(REFERRER_LOGIN_EMAIL, loginEmail);
    }

    @Override
    public String getReferrerInvitationId() {
        return m_dataStorage.getValue(REFERRER_INVITATION_ID, null);
    }

    @Override
    public void setReferrerInvitationId(String invitationId) {
        m_dataStorage.setValue(REFERRER_INVITATION_ID, invitationId);
    }

    @Override
    public void setReferrerCompanyName(String companyName) {
        m_dataStorage.setValue(REFERRER_COMPANY_NAME, companyName);

    }

    @Override
    public String getReferrerCompanyName() {
        return m_dataStorage.getValue(REFERRER_COMPANY_NAME, null);
    }

    @Override
    public boolean isVocalReadingMessagesActivated() {
        return m_dataStorage.getValue(RAINBOW_VOCAL_READING_MESSAGES, false);
    }

    @Override
    public void setIsVocalReadingMessagesActivated(boolean value) {
        m_dataStorage.setValue(RAINBOW_VOCAL_READING_MESSAGES, value);
    }

    @Override
    public void setCredentialsList(List<RainbowCredentials> credentialsList)
    {
        m_dataStorage.setCredentialsList(credentialsList);
    }

    @Override
    public List<RainbowCredentials> getCredentialsList()
    {
        return m_dataStorage.getCredentialsList();
    }

    @Override
    public RainbowCredentials getCredentialWithLogin(String login) {
        List<RainbowCredentials> credentials = getCredentialsList();
        if( credentials != null) {
            for(RainbowCredentials cred : credentials) {
                if( cred.getLogin().equals(login))
                    return cred;
            }
        }
        return null;
    }

    @Override
    public List<String> getCredentialLoginList() {
        List<RainbowCredentials> credentials = getCredentialsList();

        List<String> logins = new ArrayList<>();
        if( credentials != null) {
            for (RainbowCredentials cred : credentials) {
                logins.add(cred.getLogin());
            }
        }
        return logins;
    }

    @Override
    public List<String> getPgiOtherPhoneNumberList() {
        return m_dataStorage.getPgiOtherPhoneNumberList();
    }

    @Override
    public void setPgiOtherPhoneNumberList(List<String> pgiOtherPhoneNumberList) {
        m_dataStorage.setPgiOtherPhoneNumberList(pgiOtherPhoneNumberList);
    }

    @Override
    public void addPgiOtherPhoneNumberList(String phoneNb) {
        List<String> otherPhoneNbList = m_dataStorage.getPgiOtherPhoneNumberList();
        if (!otherPhoneNbList.contains(phoneNb))
        {
            otherPhoneNbList.add(phoneNb);
            m_dataStorage.setPgiOtherPhoneNumberList(otherPhoneNbList);
        }
    }

}

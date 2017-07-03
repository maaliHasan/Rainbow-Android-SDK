package com.ale.infra.proxy.authentication;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.Profile;
import com.ale.infra.platformservices.IJSONParser;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.rainbow.JSONParser;
import com.ale.util.log.Log;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by grobert on 27/10/15.
 */
public class AuthenticationResponse extends RestResponse{

    private static final String LOG_TAG = "AuthenticationResponse";

    private String m_token;

    // User
    private String m_login;
    private String m_jidIm;
    private String m_jidTel;
    private String m_jidPwd;
    private String m_userId;
    private String m_companyId;
    private boolean m_initialized;
    private List<Profile> m_profiles = new ArrayList<>();
    private boolean m_isCompanyDefault;

    // App
    private String m_appId;
    private String m_appSecret;
    private String m_appName;
    private String m_dateOfCreation;

    public AuthenticationResponse(String authentication)throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, "Parsing authentication; "+authentication);

        JSONParser json = new JSONParser(authentication);
        m_token = json.getString(TOKEN);
        IJSONParser user = json.getObject(LOGGEDINUSER);
        if (user != null) {
            m_login = user.getString(LOGIN);
            m_jidIm = user.getString(JID_IM);
            m_jidTel = user.getString(JID_TEL);
            m_jidPwd = user.getString(JID_PASSWORD);
            m_userId = user.getString(ID);
            m_companyId = user.getString(COMPANY_ID);
            m_initialized = user.getBoolean(INITIALIZED, false);
            m_isCompanyDefault = user.getBoolean(ISCOMPANYDEFAULT, false);


            JSONArray profiles = user.getJSONArray(PROFILES);
            if (profiles != null) {
                for (int i = 0; i < profiles.length(); i++) {
                    ProfileResponse profileResponse = new ProfileResponse(profiles.get(i).toString());

                    m_profiles.add(profileResponse.getProfile());
                }
            }
        } else {
            IJSONParser app = json.getObject(LOGGEDINAPPLICATION);
            if (app != null) {
                m_appId = app.getString(APP_ID);
                m_appSecret = app.getString(APP_SECRET);
                m_appName = app.getString(APP_NAME);
                m_dateOfCreation = app.getString(DATE_OF_CREATION);
            }
        }
    }

    public String getToken() {
        return m_token;
    }

    public void setToken(String token) {
        this.m_token = token;
    }

    public String getLogin() {
        return m_login;
    }

    public void setLogin(String login) {
        this.m_login = login;
    }

    public String getJidIm() {
        return m_jidIm;
    }

    public void setJidIm(String jidIm) {
        this.m_jidIm = jidIm;
    }

    public String getJidTel() {
        return m_jidTel;
    }

    public void setJidTel(String jidTel) {
        this.m_jidTel = jidTel;
    }

    public String getJidPwd() {
        return m_jidPwd;
    }

    public void setJidPwd(String jidPwd) {
        this.m_jidPwd = jidPwd;
    }

    public String getUserId() {
        return m_userId;
    }

    public void setUserId(String id) {
        m_userId = id;
    }

    public String getCompanyId() {
        return m_companyId;
    }

    public void setCompanyId(String id) {
        m_companyId = id;
    }

    public boolean isInitialized() {
        return m_initialized;
    }

    public void setInitialized(boolean initialized) {
        this.m_initialized = initialized;
    }

    public List<Profile> getProfiles() {
        return m_profiles;
    }

    public String getAppId() {
        return m_appId;
    }

    public void setAppId(String appId) {
        this.m_appId = appId;
    }

    public String getAppSecret() {
        return m_appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.m_appSecret = appSecret;
    }

    public String getAppName() {
        return m_appName;
    }

    public void setAppName(String appName) {
        this.m_appName = appName;
    }

    public String getDateOfCreation() {
        return m_dateOfCreation;
    }

    public void setDateOfCreation(String dateOfCreation) {
        this.m_dateOfCreation = dateOfCreation;
    }

    @Override
    public String toString() {
        if (m_appId != null) {
            return "AuthenticationResponse{" +
                    "token='" + m_token + '\'' +
                    ", appId='" + m_appId + '\'' +
                    ", appSecret='" + m_appSecret + '\'' +
                    ", appName='" + m_appName + '\'' +
                    ", dateOfCreation='" + m_dateOfCreation + '\'' +
                    '}';
        } else {
            return "AuthenticationResponse{" +
                    "token='" + m_token + '\'' +
                    ", login='" + m_login + '\'' +
                    ", user_id='" + m_userId + '\'' +
                    ", company_id='" + m_companyId + '\'' +
                    ", jidIm='" + m_jidIm + '\'' +
                    ", jidTel='" + m_jidTel + '\'' +
                    ", jidPwd='" + m_jidPwd + '\'' +
                    ", initialized=" + m_initialized +
                    '}';
        }
    }

    public boolean isCompanyDefault() {
        return m_isCompanyDefault;
    }

    public void setIsCompanyDefault(boolean companyDefault) {
        this.m_isCompanyDefault = companyDefault;
    }
}

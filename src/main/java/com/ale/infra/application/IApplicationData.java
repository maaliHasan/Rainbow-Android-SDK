/******************************************************************************
 * Copyright © 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : geyer2 3 mars 2011
 * Summary :
 * *****************************************************************************
 * History
 * 3 mars 2011  geyer2
 * Creation
 * 2013/06/11 cebruckn crms00441038 [OXO OBS]user password to be better protected in source code
 * 2013/11/13 cebruckn crms00466774 [Favorites]-Adapt to OT 2.0 favorites
 */

package com.ale.infra.application;

import com.ale.infra.datastorage.RainbowCredentials;

import java.util.List;

/**
 * Description of the internal data the mic application needs to store persistently.
 *
 * @author geyer2
 */
public interface IApplicationData {

    // OT password:
    String getUserPassword();

    void setUserPassword(String password);

    // OT login:
    String getUserLogin();

    void setUserLogin(String login);

    String getServerUrl();

    void setServerUrl(String serverUrl);

    String getHost();

    void setHost(String host);

    String getDefaultHost();

    Boolean isServerProdType();

    String getServerType();

    void setServerType(String serverType);

    String getUserId();

    void setUserId(String id);

    String getCompanyId();

    void setCompanyId(String id);

    // XMPP presence id:
    String getUserJidIm();

    void setUserJidIm(String jidIm);

    // XMPP telephonic id:
    String getUserJidTel();

    void setUserJidTel(String jidTel);

    // XMPP password
    String getUserJidPassword();

    void setUserJidPassword(String password);

    void clear();

    String getKeyPhrase();

    void setKeyPhrase(String keyPhrase);

    boolean isEulaAccepted();

    void setEulaAccepted(boolean accepted);

    boolean isLoggedOut();

    void setLoggedOut(boolean val);

    boolean isFirstLaunch();

    void setFirstLaunch(boolean firstLaunch);

    void setGooglePushToken(String token);

    String getGooglePushToken();

    boolean isPrivateLogEnable();

    void setPrivateLogEnable(boolean val);

    boolean isFirstScreenDisplayed();

    void setFirstScreenDisplayed(boolean firstScreenDisplayed);

    String getReferrerLogin();

    void setReferrerLogin(String loginEmail);

    String getReferrerInvitationId();

    void setReferrerInvitationId(String loginEmail);

    void setReferrerCompanyName(String companyName);

    String getReferrerCompanyName();

    boolean isVocalReadingMessagesActivated();

    void setIsVocalReadingMessagesActivated(boolean value);

    void setCredentialsList(List<RainbowCredentials> credentialsList);

    List<RainbowCredentials> getCredentialsList();

    RainbowCredentials getCredentialWithLogin(String login);

    List<String> getCredentialLoginList();

    List<String> getPgiOtherPhoneNumberList();

    void setPgiOtherPhoneNumberList(List<String> pgiOtherPhoneNumberList);

    void addPgiOtherPhoneNumberList(String phoneNb);
}

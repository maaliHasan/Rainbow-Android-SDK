/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : IInfrastructure.java
 * Summary :
 * *****************************************************************************
 * History
 * 2010/11/24 cebruckn crms00276330 Impossible to use external authentication with different logins
 */
package com.ale.infra;

import android.content.Context;

import com.ale.infra.capabilities.ICapabilities;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.database.IDatabaseMgr;
import com.ale.infra.manager.CallLogMgr;
import com.ale.infra.manager.ChatMgr;
import com.ale.infra.manager.ICompanyInvitationMgr;
import com.ale.infra.manager.IGroupMgr;
import com.ale.infra.manager.IInvitationMgr;
import com.ale.infra.manager.LocationMgr;
import com.ale.infra.manager.MultiUserChatMgr;
import com.ale.infra.manager.XmppContactMgr;
import com.ale.infra.manager.fileserver.IFileMgr;
import com.ale.infra.manager.fileserver.IFileProxy;
import com.ale.infra.manager.pgiconference.IPgiConferenceMgr;
import com.ale.infra.manager.pgiconference.IPgiConferenceProxy;
import com.ale.infra.manager.room.IRoomMgr;
import com.ale.infra.platformservices.IPeriodicWorkerManager;
import com.ale.infra.proxy.EnduserBots.IEnduserBotsProxy;
import com.ale.infra.proxy.authentication.IAuthentication;
import com.ale.infra.proxy.avatar.IAvatarProxy;
import com.ale.infra.proxy.conversation.IConversationProxy;
import com.ale.infra.proxy.directory.IDirectoryProxy;
import com.ale.infra.proxy.group.IGroupProxy;
import com.ale.infra.proxy.notifications.INotificationProxy;
import com.ale.infra.proxy.provisionning.IApkProvisionner;
import com.ale.infra.proxy.room.IRoomProxy;
import com.ale.infra.proxy.users.IUserProxy;
import com.ale.infra.xmpp.XmppConnection;
import com.ale.rainbow.datanetworkmonitor.DataNetworkMonitor;
import com.ale.rainbow.periodicworker.ScreenStateReceiver;
import com.ale.listener.IConnectionListener;

public interface IInfrastructure {
    IApkProvisionner getApkProvisionner();

    void run(Context applicationContext);

    void shutdownConnection();

    boolean isApiSessionStarted();

    boolean startConnectionProcess();

    boolean startLoginApplicationProcess(String applicationId, String applicationSecret, IAuthentication.IAuthenticationListener listener);

    void connectToXMPPServer();

    Infrastructure.InfrastructureState getState();

    void setState(Infrastructure.InfrastructureState state);

    ICapabilities getCapabilities();

    XmppConnection getXmppConnection();

    void setXmppConnection(XmppConnection connection);

    XmppContactMgr getXmppContactMgr();

    ChatMgr getChatMgr();

    MultiUserChatMgr getMultiUserChatMgr();

    IDirectoryProxy getDirectoryProxy();

    IAuthentication getAuthenticationProxy();

    IFileMgr getFileServerMgr();

    IPgiConferenceMgr getPgiConferenceMgr();

    IContactCacheMgr getContactCacheMgr();

    IConversationProxy getConversationProxy();

    INotificationProxy getNotificationsProxy();

    IUserProxy getUsersProxy();

    IAvatarProxy getAvatarProxy();

    IRoomProxy getRoomProxy();

    IEnduserBotsProxy getEnduserBotsProxy();

    IPgiConferenceProxy getPgiConferenceProxy();

    IGroupProxy getGroupProxy();

    IFileProxy getFileProxy();

    IGroupMgr getGroupMgr();

    public LocationMgr getLocationMgr();

    IInvitationMgr getInvitationMgr();
    
    ICompanyInvitationMgr getCompanyInvitationMgr();

    IDatabaseMgr getDatabaseMgr();

    IRoomMgr getRoomMgr();

    boolean isXmppConnected();

    boolean isRestConnected();

    void stopConnection(int milliseconds);

    boolean isInDebugMode (Context context);

    void runMinimalInfrastructure(Context applicationContext);

    boolean isPermissionAllowed(String androidPermission);

    ScreenStateReceiver getScreenStateReceiver();

    IPeriodicWorkerManager getPeriodicWorkerManager();

    DataNetworkMonitor getDataNetworkMonitor();

    void setApplicationContext(Context applicationContext);

    Context getApplicationContext();

    void registerConnectionListener(IConnectionListener listener);

    void unregisterConnectionListener(IConnectionListener listener);

    CallLogMgr getCallLogMgr();

    boolean isUnderUnitTest();
}

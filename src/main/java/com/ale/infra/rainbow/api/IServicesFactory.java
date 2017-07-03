package com.ale.infra.rainbow.api;

import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.platformservices.IPlatformServices;

/**
 * Created by grobert on 26/10/15.
 */
public interface IServicesFactory
{
    IRainbowAuthentication createAuthenticationService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices);

    IDirectoryService createDirectoryService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices);

    IRainbowNotifications createNotificationsService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices);

    IRainbowConversationService createConversationService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService, IContactCacheMgr contactCacheMgr);

    IUsers createUsersService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService);

    IRainbowAvatarService createAvatarService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService);

    IRainbowRoomService createRoomService(IContactCacheMgr contactCacheMgr, IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices);

    IRainbowGroupsService createGroupsService(IContactCacheMgr contactCacheMgr, IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices);

    IRainbowEnduserBotsService createEnduserBotsService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService);

    IPortalVersionService createPortalService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService);

    IRainbowSettingsService createSettingsService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService);
    IRainbowFileService createFileService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService);

    IRainbowCompanyService createCompanyService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices);

    IProfileService createProfileService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService);

    IPgiConferenceService createPgiConferenceService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService);
}


package com.ale.infra.rainbow.api;

import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.rainbow.adapter.ConversationServiceAdapter;
import com.ale.infra.rainbow.adapter.DirectoryService;
import com.ale.infra.rainbow.adapter.PgiConferenceServiceAdapter;
import com.ale.infra.rainbow.adapter.PortalVersionServiceAdapter;
import com.ale.infra.rainbow.adapter.RainbowAuthenticationServiceAdapter;
import com.ale.infra.rainbow.adapter.RainbowAvatarServiceAdapter;
import com.ale.infra.rainbow.adapter.RainbowCompanyServiceAdapter;
import com.ale.infra.rainbow.adapter.RainbowEnduserBotsServiceAdapter;
import com.ale.infra.rainbow.adapter.RainbowFileServiceAdapter;
import com.ale.infra.rainbow.adapter.RainbowGroupsServiceAdapter;
import com.ale.infra.rainbow.adapter.RainbowNotificationsServiceAdapter;
import com.ale.infra.rainbow.adapter.RainbowProfileServiceAdapter;
import com.ale.infra.rainbow.adapter.RainbowRoomServiceAdapter;
import com.ale.infra.rainbow.adapter.RainbowSettingsServiceAdapter;
import com.ale.infra.rainbow.adapter.RainbowUsersServiceAdapter;

/**
 * Created by grobert on 26/10/15.
 */
public class RainbowServicesFactory implements IServicesFactory
{
    @Override
    public IRainbowAuthentication createAuthenticationService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices)
    {
        return new RainbowAuthenticationServiceAdapter(restAsyncRequest, platformServices);
    }

    @Override
    public IDirectoryService createDirectoryService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices)
    {
        return new DirectoryService(restAsyncRequest, platformServices);
    }

    @Override
    public IRainbowNotifications createNotificationsService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices)
    {
        return new RainbowNotificationsServiceAdapter(restAsyncRequest, platformServices);
    }

    @Override
    public IRainbowConversationService createConversationService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService, IContactCacheMgr contactCacheMgr)
    {
        return new ConversationServiceAdapter(restAsyncRequest, platformService, contactCacheMgr);
    }

    @Override
    public IUsers createUsersService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService)
    {
        return new RainbowUsersServiceAdapter(restAsyncRequest, platformService);
    }

    @Override
    public IRainbowAvatarService createAvatarService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService)
    {
        return new RainbowAvatarServiceAdapter(restAsyncRequest, platformService);
    }

    @Override
    public IRainbowRoomService createRoomService(IContactCacheMgr contactCacheMgr, IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices)
    {
        return new RainbowRoomServiceAdapter(contactCacheMgr, restAsyncRequest, platformServices);
    }

    @Override
    public IRainbowGroupsService createGroupsService(IContactCacheMgr contactCacheMgr, IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices)
    {
        return new RainbowGroupsServiceAdapter(contactCacheMgr, restAsyncRequest, platformServices);
    }

    @Override
    public IRainbowEnduserBotsService createEnduserBotsService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService)
    {
        return new RainbowEnduserBotsServiceAdapter(restAsyncRequest, platformService);
    }

    @Override
    public IPortalVersionService createPortalService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService)
    {
        return new PortalVersionServiceAdapter(restAsyncRequest, platformService);
    }

    @Override
    public IRainbowFileService createFileService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService) {
        return new RainbowFileServiceAdapter(restAsyncRequest, platformService);
    }

    @Override
    public IRainbowCompanyService createCompanyService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService) {
        return new RainbowCompanyServiceAdapter(restAsyncRequest, platformService);
    }

    @Override
    public IProfileService createProfileService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService) {
        return new RainbowProfileServiceAdapter(restAsyncRequest, platformService);
    }

    @Override
    public IPgiConferenceService createPgiConferenceService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService) {
        return new PgiConferenceServiceAdapter(restAsyncRequest, platformService);
    }

    public IRainbowSettingsService createSettingsService(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService)
    {
        return new RainbowSettingsServiceAdapter(restAsyncRequest, platformService);
    }
}

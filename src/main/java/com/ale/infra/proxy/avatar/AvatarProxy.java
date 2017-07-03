package com.ale.infra.proxy.avatar;

import com.ale.infra.contact.Contact;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseVoid;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.rainbow.api.IRainbowAvatarService;
import com.ale.infra.rainbow.api.IServicesFactory;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.io.File;

/**
 * Created by wilsius on 25/05/16.
 */
public class AvatarProxy implements IAvatarProxy
{

    private static final String LOG_TAG = "AvatarProxy";
    private IRainbowAvatarService m_avatarService;

    public AvatarProxy(IServicesFactory servicesFactory, IRESTAsyncRequest httpClientFactory, IPlatformServices platformService)
    {
        Log.getLogger().info(LOG_TAG, "initialization");
        m_avatarService = servicesFactory.createAvatarService(httpClientFactory, platformService);
    }

    @Override
    public void getAvatar(final Contact contact, int size, final IAvatarProxy.IAvatarListener listener)
    {
        String corporateId = contact.getCorporateId();
        if (StringsUtil.isNullOrEmpty(corporateId))
        {
            Log.getLogger().warn(LOG_TAG, ">getAvatar; " + contact.getDisplayName4Log("") + " has no CorporateId");
            return;
        }
        Log.getLogger().info(LOG_TAG, ">getAvatar; " + contact.getDisplayName4Log("") + "/" + corporateId);

        String hash =contact.getDirectoryContact().getLastAvatarUpdateDate();
        getAvatar(corporateId, hash, size, listener);
    }

    @Override
    public void getAvatar(String corporateId, String lastUpdateDate, int size, final IAvatarProxy.IAvatarListener listener)
    {
        if (StringsUtil.isNullOrEmpty(corporateId))
        {
            Log.getLogger().warn(LOG_TAG, ">getAvatar has no CorporateId");
            if (listener != null)
                listener.onAvatarFailure();

            return;
        }
        String hash;
        if (StringsUtil.isNullOrEmpty(lastUpdateDate))
            hash = null;
        else
            hash = StringsUtil.getMD5Hash(lastUpdateDate);

        m_avatarService.getAvatar(corporateId, hash, size, new IAsyncServiceResultCallback<GetAvatarResponse>()
        {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetAvatarResponse> asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().info(LOG_TAG, "getAvatar SUCCESS for contact ");

                    if (listener != null)
                        listener.onAvatarSuccess(asyncResult.getResult().getBitmap());
                }
                else
                {
                    if (asyncResult.getException().getStatusCode() != 404) {
                        Log.getLogger().info(LOG_TAG, "getAvatar FAILURE for contact ", asyncResult.getException());
                    }

                    if (listener != null)
                        listener.onAvatarFailure();
                }
            }
        });
    }

    @Override
    public void uploadAvatar(final Contact contact, File photoFile, final IAvatarProxy.IAvatarListener listener)
    {
        String corporateId = contact.getCorporateId();
        if (StringsUtil.isNullOrEmpty(corporateId))
        {
            Log.getLogger().warn(LOG_TAG, ">uploadAvatar; " + contact.getDisplayName4Log("") + " has no CorporateId");
            if (listener != null)
                listener.onAvatarFailure();

            return;
        }
        Log.getLogger().info(LOG_TAG, ">uploadAvatar; " + contact.getDisplayName4Log("") + "/" + corporateId);

        m_avatarService.uploadAvatar(corporateId, photoFile, new IAsyncServiceVoidCallback()
        {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult)
            {
                if (!asyncResult.exceptionRaised())
                {
                    Log.getLogger().info(LOG_TAG, "uploadAvatar SUCCESS for contact " + contact.getDisplayName4Log(""));

                    if (listener != null)
                        listener.onAvatarSuccess(null);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "uploadAvatar FAILURE for contact " + contact.getDisplayName4Log(""), asyncResult.getException());

                    if (listener != null)
                        listener.onAvatarFailure();
                }
            }
        });
    }

}


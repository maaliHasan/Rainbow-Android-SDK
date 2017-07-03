package com.ale.infra.proxy.portal;

import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.rainbow.api.IPortalVersionService;
import com.ale.infra.rainbow.api.IServicesFactory;
import com.ale.util.log.Log;

/**
 * Created by georges on 29/08/2016.
 */
public class PortalVersionProxy implements IPortalVersion {
    private static final String LOG_TAG = "DirectoryProxy";

    private IPortalVersionService m_portalService;

    public PortalVersionProxy(IServicesFactory servicesFactory, IRESTAsyncRequest httpClientFactory, IPlatformServices platformService) {
        Log.getLogger().info(LOG_TAG, "initialization");
        m_portalService = servicesFactory.createPortalService(httpClientFactory, platformService);
    }

    @Override
    public void getEndUserVersion(final IPortalListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">getEndUserVersion");

        m_portalService.getEndUserVersion(new IAsyncServiceResultCallback<GetVersionResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetVersionResponse> asyncResult) {
                Log.getLogger().verbose(LOG_TAG, ">handleResult");
                if (asyncResult.exceptionRaised()) {
                    Log.getLogger().error(LOG_TAG, "Error while trying to get Version :" + asyncResult.getException().getMessage());

                    if (listener != null)
                        listener.onFailure();

                    return;
                }

                String response = asyncResult.getResult().getVersion();
                Log.getLogger().verbose(LOG_TAG, "Get Version success:" + response.toString());

                if (listener != null)
                    listener.onSuccess(response);
            }
        });
    }
}

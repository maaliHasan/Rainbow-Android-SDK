package com.ale.infra.proxy.profile;

import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.rainbow.api.IProfileService;
import com.ale.infra.rainbow.api.IServicesFactory;
import com.ale.util.log.Log;

import java.util.List;

/**
 * Created by georges on 10/02/2017.
 */

public class ProfileProxy implements IProfileProxy {

    private static final String LOG_TAG = "FileProxy";

    private IProfileService m_profileService;

    public ProfileProxy(IServicesFactory servicesFactory, IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService)
    {
        Log.getLogger().info(LOG_TAG, "initialization");
        m_profileService = servicesFactory.createProfileService(restAsyncRequest, platformService);
    }

    @Override
    public void getUserFeatures(String userId, final IGetFeatureListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">getUserFeatures");

        m_profileService.getUserFeatures(userId, new IAsyncServiceResultCallback<FeatureResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<FeatureResponse> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "getUserFeatures SUCCESS");

                    List<Feature> features = asyncResult.getResult().getFeatures();
                    if (listener != null)
                        listener.onGetFeatureSuccess(features);
                } else {
                    Log.getLogger().warn(LOG_TAG, "getUserFeatures FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onGetFeatureFailed();
                }
            }
        });
    }
}

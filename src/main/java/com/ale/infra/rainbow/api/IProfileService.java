package com.ale.infra.rainbow.api;

import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.proxy.profile.FeatureResponse;

/**
 * Created by georges on 20/02/2017.
 */

public interface IProfileService extends IRainbowService {

    void getUserFeatures(String userId, IAsyncServiceResultCallback<FeatureResponse> callback);

}

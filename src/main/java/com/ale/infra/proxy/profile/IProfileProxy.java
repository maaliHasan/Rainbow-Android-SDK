package com.ale.infra.proxy.profile;

import java.util.List;

/**
 * Created by georges on 20/02/2017.
 */
public interface IProfileProxy {


    void getUserFeatures(String userId, IGetFeatureListener listener);

    interface IGetFeatureListener {
        void onGetFeatureSuccess(List<Feature> features);

        void onGetFeatureFailed();
    }

}

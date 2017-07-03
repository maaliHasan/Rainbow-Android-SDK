package com.ale.infra.capabilities;

import com.ale.infra.proxy.profile.Feature;

import java.util.List;

/**
 * Created by georges on 29/08/2016.
 */
public interface ICapabilities {

    void getUserFeatures(String corporateId);

    List<Feature> getFeatures();

    boolean isWebRtcAllowed();

    boolean isVideoWebRtcAllowed();

    int getMaxBubbleParticipants();

    int getFileSharingQuota();
}

package com.ale.infra.capabilities;


import android.content.Context;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.proxy.profile.Feature;
import com.ale.infra.proxy.profile.IProfileProxy;
import com.ale.infra.proxy.profile.ProfileProxy;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.util.List;

/**
 * Created by georges on 29/08/2016.
 */
public class CapabilitiesMgr implements ICapabilities
{

    public static final String LOG_TAG = "CapabilitiesMgr";
    private static final String WEBRTC_FOR_MOBILE = "WEBRTC_FOR_MOBILE";
    private static final String WEBRTC_FOR_MOBILE_VIDEO = "WEBRTC_FOR_MOBILE_VIDEO";
    private static final String BUBBLE_PARTICIPANT_COUNT = "BUBBLE_PARTICIPANT_COUNT";
    private static final String FILE_SHARING_QUOTA_GB = "FILE_SHARING_QUOTA_GB";
    private static final int DEFAULT_MAX_PARTICIPANTS = 20;
    private final ProfileProxy m_profileProxy;
    private List<Feature> m_features;
    private boolean m_isInDebugMode = false;

    public CapabilitiesMgr(ProfileProxy profileProxy, Context context)
    {
        m_profileProxy = profileProxy;
        if (context != null)
        {
            m_isInDebugMode = RainbowContext.getInfrastructure().isInDebugMode(context);
        }
    }

    @Override
    public void getUserFeatures(String corporateId)
    {
        if (m_profileProxy == null)
            return;
        if (StringsUtil.isNullOrEmpty(corporateId))
            return;

        m_profileProxy.getUserFeatures(corporateId, new IProfileProxy.IGetFeatureListener()
        {

            @Override
            public void onGetFeatureSuccess(List<Feature> features)
            {
                Log.getLogger().info(LOG_TAG, ">onGetFeatureSuccess");
                setFeatures(features);
            }

            @Override
            public void onGetFeatureFailed()
            {
                Log.getLogger().warn(LOG_TAG, ">onGetFeatureFailed");
            }
        });
    }

    @Override
    public List<Feature> getFeatures()
    {
        return m_features;
    }

    private void setFeatures(List<Feature> features)
    {
        m_features = features;
    }

    @Override
    public boolean isWebRtcAllowed()
    {
        if (m_isInDebugMode)
            return true;

        if (m_features != null)
        {
            for (Feature feature : m_features)
            {
                if (WEBRTC_FOR_MOBILE.equals(feature.getUniqueRef()))
                    return true;
            }
        }

        return false;
    }

    @Override
    public boolean isVideoWebRtcAllowed()
    {
        if (m_isInDebugMode)
            return true;

        if (m_features != null)
        {
            for (Feature feature : m_features)
            {
                if (WEBRTC_FOR_MOBILE_VIDEO.equals(feature.getUniqueRef()))
                    return true;
            }
        }

        return false;
    }

    @Override
    public int getMaxBubbleParticipants()
    {
        if (m_features != null)
        {
            for (Feature feature : m_features)
            {
                if (BUBBLE_PARTICIPANT_COUNT.equals(feature.getUniqueRef()))
                {
                    return feature.getLimitMax();
                }
            }
        }

        return DEFAULT_MAX_PARTICIPANTS;
    }

    @Override
    public int getFileSharingQuota()
    {
        if (m_features != null)
        {
            for (Feature feature : m_features)
            {
                if (FILE_SHARING_QUOTA_GB.equals(feature.getUniqueRef()))
                {
                    return feature.getLimitMax();
                }
            }
        }

        return 1;
    }
}

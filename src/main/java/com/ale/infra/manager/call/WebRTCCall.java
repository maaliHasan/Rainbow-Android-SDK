package com.ale.infra.manager.call;

import com.ale.infra.capabilities.ICapabilities;
import com.ale.infra.contact.Contact;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQ;

import org.webrtc.EglBase;

/**
 * Created by cebruckn on 13/02/2017.
 */

public class WebRTCCall
{
    private final EglBase m_eglBase;
    private final ICapabilities m_capabilities;
    private long m_startTime;
    private Contact m_distant;
    private MediaState m_state;
    private String m_jid;
    private String m_sid;
    private boolean m_isOutgoing;
    private boolean m_initiatedWithVideo;
    private boolean m_canAddVideo;
    private boolean m_initiatedWithShare;

    public WebRTCCall(Contact contact, ICapabilities capabilities)
    {
        m_distant = contact;
        m_startTime = System.currentTimeMillis();
        m_eglBase = EglBase.create();
        m_isOutgoing = false;
        m_initiatedWithVideo = false;
        m_initiatedWithShare = false;
        m_canAddVideo = true;
        m_sid = JingleIQ.generateSID();
        m_capabilities = capabilities;
    }

    public MediaState getState()
    {
        return m_state;
    }

    public void setState(MediaState state)
    {
        m_state = state;
    }

    public Contact getDistant()
    {
        return m_distant;
    }

    public String getJid()
    {
        return m_jid;
    }

    public void setJid(String jid)
    {
        m_jid = jid;
    }

    public long getStartTime()
    {
        return m_startTime;
    }

    public EglBase.Context getEglBaseContext()
    {
        return m_eglBase.getEglBaseContext();
    }

    public void release()
    {
        m_eglBase.release();
    }

    public String getSid()
    {
        return m_sid;
    }

    public void setSid(String sid)
    {
        m_sid = sid;
    }

    public boolean isOutgoing()
    {
        return m_isOutgoing;
    }

    public void setIsOutgoing(boolean isOutgoing)
    {
        m_isOutgoing = isOutgoing;
    }

    public void setInitiatedWithVideo(boolean withVideo)
    {
        m_initiatedWithVideo = withVideo;
    }

    public boolean wasInitiatedWithVideo()
    {
        return m_capabilities.isVideoWebRtcAllowed() && m_initiatedWithVideo;
    }

    public boolean wasInitiatedWithShare()
    {
        return m_capabilities.isVideoWebRtcAllowed() && m_initiatedWithShare;
    }

    public void setCanAddVideo(boolean canAddVideo)
    {
        m_canAddVideo = canAddVideo;
    }

    public boolean canAddVideo()
    {
        return m_capabilities.isVideoWebRtcAllowed() && m_canAddVideo;
    }

    public void setInitiatedWithShare(boolean initiatedWithShare)
    {
        m_initiatedWithShare = initiatedWithShare;
    }
}

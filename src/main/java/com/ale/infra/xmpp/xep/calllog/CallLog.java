package com.ale.infra.xmpp.xep.calllog;

import android.support.annotation.NonNull;

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.DirectoryContact;
import com.ale.infra.contact.IContactCacheMgr;
import com.ale.infra.data_model.IMultiSelectable;
import com.ale.util.log.Log;

import java.util.Date;

/**
 * Created by cebruckn on 17/05/2017.
 */

public class CallLog implements Comparable<CallLog>, IMultiSelectable
{
    private static final String LOG_TAG = "CallLog";
    private final String m_id;
    private final String m_callId;
    private final Date m_date;
    private final String m_media;
    private final String m_type;
    private final String m_duration;
    private final String m_state;
    private final boolean m_isOutgoing;
    private boolean m_isAck;
    private Contact m_contact;

    public CallLog(ICallLogPacketExtension extension, IContactCacheMgr contactCacheMgr, String m_userJid)
    {
        m_id = extension.getId();
        m_callId = extension.getCallLogEvent().getCallId();
        m_date = new Date(extension.getStampLong());
        m_media = extension.getCallLogEvent().getMedia();
        m_type = extension.getCallLogEvent().getType();
        m_duration = extension.getCallLogEvent().getDuration();
        m_state = extension.getCallLogEvent().getState();
        m_isAck = extension.getCallLogEvent().isAck();

        String otherContactJid = extension.getCallLogEvent().getCallerJid();

        m_isOutgoing = m_userJid.equalsIgnoreCase(extension.getCallLogEvent().getCallerJid());

        if (m_isOutgoing)
            otherContactJid = extension.getCallLogEvent().getCalleeJid();

        m_contact = contactCacheMgr.getContactFromJid(otherContactJid);
        if (m_contact == null)
        {
            Log.getLogger().verbose(LOG_TAG, "Contact for callLog doesn't exist - create it");
            DirectoryContact dirContact = new DirectoryContact();
            dirContact.setImJabberId(otherContactJid);

            m_contact = contactCacheMgr.createContactIfNotExistOrUpdate(dirContact);
        }
    }

    public String getId()
    {
        return m_id;
    }

    public String getCallId()
    {
        return m_callId;
    }

    public Date getDate()
    {
        return m_date;
    }

    public String getMedia()
    {
        return m_media;
    }

    public String getType()
    {
        return m_type;
    }

    public String getDuration()
    {
        return m_duration;
    }

    public String getState()
    {
        return m_state;
    }

    @Override
    public int compareTo(@NonNull CallLog another)
    {
        if (m_date == null && another.getDate() == null)
            return 0;

        if (m_date == null)
            return -1;

        if (another.getDate() == null)
            return 1;

        return m_date.compareTo(another.getDate());
    }

    public Contact getContact()
    {
        return m_contact;
    }

    public boolean isOutgoing()
    {
        return m_isOutgoing;
    }

    @Override
    public int getSelectableType()
    {
        return 0;
    }

    public boolean isMissed()
    {
        return !m_isAck && !"answered".equals(m_state) && !m_isOutgoing;
    }

    public void setIsAck()
    {
        m_isAck = true;
    }
}

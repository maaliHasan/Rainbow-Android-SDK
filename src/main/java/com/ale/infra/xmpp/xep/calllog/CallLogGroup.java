package com.ale.infra.xmpp.xep.calllog;

import android.support.annotation.NonNull;

import com.ale.infra.contact.Contact;
import com.ale.infra.data_model.IMultiSelectable;

import java.util.ArrayList;

/**
 * Created by cebruckn on 17/05/2017.
 */

public class CallLogGroup implements Comparable<CallLogGroup>, IMultiSelectable
{
    private ArrayList<CallLog> m_callLogs = new ArrayList<>();
    private Contact m_contact;

    public CallLogGroup(Contact contact)
    {
        m_contact = contact;
    }

    public ArrayList<CallLog> getCallLogs()
    {
        return m_callLogs;
    }

    @Override
    public int compareTo(@NonNull CallLogGroup another)
    {
        if (m_callLogs.get(0).getDate() == null && another.getCallLogs().get(0).getDate() == null)
            return 0;

        if (m_callLogs.get(0).getDate() == null)
            return -1;

        if (another.getCallLogs().get(0).getDate() == null)
            return 1;

        return m_callLogs.get(0).getDate().compareTo(another.getCallLogs().get(0).getDate());
    }

    public Contact getContact()
    {
        return m_contact;
    }

    @Override
    public int getSelectableType()
    {
        return 0;
    }
}

package com.ale.infra.manager;

import org.jivesoftware.smack.packet.PlainStreamElement;

/**
 * Created by cebruckn on 09/11/2015.
 */
public class MakeCallElement implements PlainStreamElement
{

    private final String m_from;
    private final String m_to;

    public MakeCallElement(String from, String to)
    {
        m_from = from;
        m_to = to;
    }

    @Override
    public CharSequence toXML()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("<iq type='set' to='");
        builder.append(m_from + "/phone");
        builder.append("' xmlns='jabber:client' id='10:sendIQ'><callservice xmlns='com:alcatel:lucent:callservice'><makecall to='");
        builder.append(m_to);
        builder.append("'/></callservice></iq>");
        return builder.toString();
    }
}

package com.ale.infra.contact;

import com.ale.infra.xmpp.xep.calendar.UntilExtension;
import com.ale.util.DateTimeUtil;
import com.ale.util.StringsUtil;

import org.jivesoftware.smack.packet.Presence;

import java.util.Date;

/**
 * Created by cebruckn on 13/07/2017.
 */

public class CalendarPresence
{
    private final Presence m_presence;
    private final Date m_until;
    public enum DndStatus
    {
        BUSY("busy"), OUT_OF_OFFICE("out_of_office"), UNDEFINED("undefined");
        private String value;

        DndStatus(String value) {

            this.value = value;
        }

        @Override
        public String toString()
        {
            return value;
        }

        public static DndStatus fromString(String text) {
            if (text != null) {
                for (DndStatus status : DndStatus.values()) {
                    if (text.equalsIgnoreCase(status.value)) {
                        return status;
                    }
                }
            }
            return UNDEFINED;
        }
    }

    public CalendarPresence(Presence pres, UntilExtension until)
    {
        m_presence = pres;

        if (until != null && !StringsUtil.isNullOrEmpty(until.getUntil()))
            m_until = DateTimeUtil.getDateFromStringStamp(until.getUntil());
        else
            m_until = null;
    }


    public Presence getPresence()
    {
        return m_presence;
    }

    public Date getUntil()
    {
        return m_until;
    }
}

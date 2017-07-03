package com.ale.infra.xmpp.xep.calllog;

/**
 * Created by cebruckn on 08/06/2017.
 */

interface ICallLogPacketExtension
{
    String getId();

    Long getStampLong();

    CallLogPacketExtension getCallLogEvent();
}

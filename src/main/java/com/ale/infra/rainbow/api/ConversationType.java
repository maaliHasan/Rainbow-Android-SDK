package com.ale.infra.rainbow.api;

import com.ale.infra.proxy.framework.RestResponse;

/**
 * Created by cebruckn on 23/05/2016.
 */
public enum ConversationType
{
    USER(RestResponse.TYPE_USER),
    BOT(RestResponse.TYPE_BOT),
    ROOM(RestResponse.TYPE_ROOM);

    protected String type;

    ConversationType(String type)
    {
        this.type = type;
    }

    @Override
    public String toString()
    {
        return type;
    }

    public static ConversationType fromString(String type) {
        if (type != null) {
            for (ConversationType convType : ConversationType.values()) {
                if (type.equalsIgnoreCase(convType.type)) {
                    return convType;
                }
            }
        }
        return null;
    }

    public String getType()
    {
        return this.type;
    }
}

package com.ale.infra.xmpp;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;

/**
 * Created by georges on 23/06/16.
 */
public abstract class AbstractRainbowXMPPConnection extends AbstractXMPPConnection {
    /**
     * Create a new XMPPConnection to an XMPP server.
     *
     * @param configuration The configuration which is used to establish the connection.
     */
    protected AbstractRainbowXMPPConnection(ConnectionConfiguration configuration) {
        super(configuration);
    }

    protected abstract void notifyConnectionError();

    public abstract void shutdown();

    public abstract boolean streamWasResumed();

    public abstract void setUseStreamManagementDefault(boolean useSmDefault);

    public abstract void setUseStreamManagementResumptionDefault(boolean useSmResumptionDefault);

    public abstract void setUseStreamManagement(boolean useSm);

    public abstract boolean isSmResumptionPossible(long twoMinutesInMilliseconds);
}

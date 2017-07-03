/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;


public class AcceptPacketExtension extends AbstractPacketExtension
{
    /**
     * The name of the "bandwidth" element.
     */
    public static final String ELEMENT_NAME = "accept";

    public static final String NAMESPACE = "urn:xmpp:jingle-message:0";

    public static final String ID_ATTR_NAME = "id";

    /**
     * Creates a new {@link AcceptPacketExtension} instance.
     */
    public AcceptPacketExtension()
    {
        super(NAMESPACE, ELEMENT_NAME);
    }

    public String getId()
    {
        return getAttributeAsString(ID_ATTR_NAME);
    }

    public void setId(String id)
    {
        setAttribute(ID_ATTR_NAME, id);
    }
}

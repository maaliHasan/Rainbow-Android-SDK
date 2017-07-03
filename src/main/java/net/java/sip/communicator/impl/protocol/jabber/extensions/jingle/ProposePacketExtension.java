/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;


public class ProposePacketExtension extends AbstractPacketExtension
{
    /**
     * The name of the "bandwidth" element.
     */
    public static final String ELEMENT_NAME = "propose";

    public static final String NAMESPACE = "urn:xmpp:jingle-message:0";

    public static final String ID_ATTR_NAME = "id";
    /**
     * Creates a new {@link ProposePacketExtension} instance.
     */
    public ProposePacketExtension()
    {
        super(NAMESPACE, ELEMENT_NAME);
    }

    public void addDescription(RtpDescriptionPacketExtension parameter)
    {
        addChildExtension(parameter);
    }

    public RtpDescriptionPacketExtension getDescription()
    {
        return getFirstChildOfType(RtpDescriptionPacketExtension.class);
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

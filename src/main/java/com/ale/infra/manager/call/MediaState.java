package com.ale.infra.manager.call;

/**
 * <p>Java class for MediaState.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="MediaState">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="UNKNOWN"/>
 *     &lt;enumeration value="OFF_HOOK"/>
 *     &lt;enumeration value="IDLE"/>
 *     &lt;enumeration value="RELEASING"/>
 *     &lt;enumeration value="DIALING"/>
 *     &lt;enumeration value="HELD"/>
 *     &lt;enumeration value="RINGING_INCOMING"/>
 *     &lt;enumeration value="RINGING_OUTGOING"/>
 *     &lt;enumeration value="ACTIVE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
public enum MediaState
{


    /**
     * Unknown state : aims to support state evolutions for compatibility.
     * 
     */
    UNKNOWN,

    /**
     * The device is off-hook.
     * 
     */
    OFF_HOOK,

    /**
     * Call is in idle state.
     * 
     */
    IDLE,

    /**
     * Call release is in progress.
     * 
     */
    RELEASING,

    /**
     * An attempt to make a call is in progress.
     * 
     */
    DIALING,

    /**
     * The call has been placed on hold.
     * 
     */
    HELD,

    /**
     * The incoming call is ringing.
     * 
     */
    RINGING_INCOMING,

    /**
     * The outgoing call is ringing.
     * 
     */
    RINGING_OUTGOING,

    /**
     * The call is active (means in conversation).
     * 
     */
    ACTIVE;

    public String value() {
        return name();
    }

    public static MediaState fromValue(String v) {
    	
    	try
    	{
    		return valueOf(v);
    	}
    	catch(Exception e)
    	{
    		return UNKNOWN;
    	}
    }

}

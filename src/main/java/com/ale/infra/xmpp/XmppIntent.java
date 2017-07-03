package com.ale.infra.xmpp;

/**
 * Created by jonathanpetit on 09/02/2016.
 */
public final class XmppIntent {

    public static final String NEW_MESSAGE = "com.ale.rainbow.newmessage";
    public static final String SEND_MESSAGE = "com.ale.rainbow.sendmessage";
    public static final String BUNDLE_FROM_JID = "b_from";
    public static final String BUNDLE_MESSAGE_BODY = "b_body";
    public static final String BUNDLE_MESSAGE = "b_message";
    public static final String BUNDLE_TO = "b_to";
    public static final String SEND_MESSAGE_FAILED = "com.ale.rainbow.sendmessage.failed";
    public static final String ROSTER_SUBSCRIBED = "com.ale.rainbow.roster.subscribed";
    public static final String CONNECTION_STATE_CHANGE = "com.ale.rainbow.connection.state.change";
}

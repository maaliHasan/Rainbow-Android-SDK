package com.ale.infra.xmpp.xep.call;

import android.telecom.Call;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.platformservices.IGsmPhone;

import org.jivesoftware.smack.packet.IQ;

public class CallIq extends IQ {

    public static final String NAMESPACE = "urn:xmpp:call";
    public static final String ELEMENT = "call";

    String phoneNumber;
    boolean directCall;

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        return null;
    }

    public CallIq(String childElementName, String childElementNamespace, String phoneNumber, boolean directCall) {
        super(ELEMENT, NAMESPACE);
        this.phoneNumber = phoneNumber;
        this.directCall = directCall;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isDirectCall() {
        return directCall;
    }

    public void setDirectCall(boolean directCall) {
        this.directCall = directCall;
    }

    public IQ result() {
        return createResultIQ(this);
    }
}
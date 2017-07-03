package com.ale.listener;


import com.ale.infra.contact.IRainbowContact;

import java.util.List;

public interface IRainbowInvitationManagementListener {

    void onAcceptSuccess();

    void onDeclineSuccess();

    void onError();

    void onNewReceivedInvitation(List<IRainbowContact> contactInviting);
}

package com.ale.listener;


public interface IRainbowContactManagementListener {

    void OnContactRemoveSuccess(String contactEmail);

    void onContactRemovedError(Exception ex);
}

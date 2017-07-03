package com.ale.listener;

import com.ale.infra.notifier.IRainbowNotifier;

/**
 * Interface for connection class
 */

public interface IConnectionListener {

        void onSigninSuccessed();
        void onSigninFailed(int responseCode, String err);
        void onXmppCreated();

}

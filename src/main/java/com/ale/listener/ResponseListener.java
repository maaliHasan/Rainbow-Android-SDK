package com.ale.listener;

import com.ale.rainbowsdk.RainbowSdk;

/**
 * Created by letrongh on 29/06/2017.
 */

public abstract class ResponseListener {
    public abstract void onSuccess();
    public abstract void onRequestFailed(RainbowSdk.ErrorCode errorCode, String err);
}

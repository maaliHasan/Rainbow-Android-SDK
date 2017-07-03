package com.ale.listener;

import com.ale.rainbowsdk.RainbowSdk;


public abstract class BaseResponseListener
{
    public abstract void onRequestFailed(RainbowSdk.ErrorCode errorCode, String err);
}

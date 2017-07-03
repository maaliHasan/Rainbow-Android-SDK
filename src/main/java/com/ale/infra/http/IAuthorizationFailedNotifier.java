/******************************************************************************
 * Copyright © 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : IAuthorizationFailedNotifier.java
 * Author  : geyer2 15 f�vr. 2011
 * Summary :
 * *****************************************************************************
 * History
 * 15 f�vr. 2011  geyer2
 * Creation
 */

package com.ale.infra.http;

/**
 * @author geyer2
 *
 */
public interface IAuthorizationFailedNotifier
{
    void notifyAuthorizarionFailed(String realm);

    void notifyAuthorizationNotSupported();
}

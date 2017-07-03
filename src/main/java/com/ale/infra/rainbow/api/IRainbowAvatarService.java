package com.ale.infra.rainbow.api;

import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.proxy.avatar.GetAvatarResponse;

import java.io.File;

/**
 * Created by wilsius on 25/05/16.
 */

public interface IRainbowAvatarService extends IRainbowService
{

    void deleteMyAvatar(String userId, final IAsyncServiceVoidCallback callback);

    void getAvatar(String userId,String hash,  int size, IAsyncServiceResultCallback<GetAvatarResponse> callback);

    void uploadAvatar(String userId, File photoFile, IAsyncServiceVoidCallback callback);
}


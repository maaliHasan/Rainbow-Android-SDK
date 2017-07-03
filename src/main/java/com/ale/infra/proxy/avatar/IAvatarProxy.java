package com.ale.infra.proxy.avatar;

import android.graphics.Bitmap;

import com.ale.infra.contact.Contact;

import java.io.File;

/**
 * Created by wilsius on 25/05/16.
 */
public interface IAvatarProxy {
    void getAvatar(Contact contact, int size, IAvatarListener listener);
    void getAvatar(String corporateId, String hash, int size, IAvatarProxy.IAvatarListener listener);
    void uploadAvatar(Contact m_user, File photoFile, IAvatarListener listener);

    interface IAvatarListener
    {
        void onAvatarSuccess(Bitmap bmp);

        void onAvatarFailure();
    }
}

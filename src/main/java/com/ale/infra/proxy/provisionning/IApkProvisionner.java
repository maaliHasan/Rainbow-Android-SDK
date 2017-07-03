package com.ale.infra.proxy.provisionning;

import com.ale.infra.IInfrastructure;

import java.io.File;

/**
 * Created by georges on 02/09/2016.
 */
public interface IApkProvisionner {

    void downloadVersionFile(IDownloadVersionFileCallback callback);

    boolean canProposeUpdate(String currentVersion, String content);

    String getApkPath();

    String getApkServerVersion();

    interface IDownloadVersionFileCallback
    {
        void onSuccess(String content);

        void onFailed();
    }
}

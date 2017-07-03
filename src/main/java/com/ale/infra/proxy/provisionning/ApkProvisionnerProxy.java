package com.ale.infra.proxy.provisionning;

import com.ale.infra.application.IApplicationData;
import com.ale.infra.application.RainbowContext;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

/**
 * Created by georges on 02/09/2016.
 */
public class ApkProvisionnerProxy implements IApkProvisionner {

    private static final String LOG_TAG = "ApkProvisionnerProxy";

    private static final int DOWNLOAD_BUFFER = 1024;
    private static final String APK_FILENAME = "rainbow.apk";
    private static final String APK_STARTINGCONTENT = "version=";
    private final IPlatformServices m_platformService;
    private final IRESTAsyncRequest m_restAsyncRequest;
    private String m_serverApkVersion = null;


    public ApkProvisionnerProxy(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService)
    {
        Log.getLogger().info(LOG_TAG, "initialization");
        m_restAsyncRequest = restAsyncRequest;
        m_platformService = platformService;
    }

    @Override
    public String getApkServerVersion() {
        return m_serverApkVersion;
    }

    @Override
    public void downloadVersionFile(final IDownloadVersionFileCallback callback)
    {
        Log.getLogger().verbose(LOG_TAG, ">downloadVersionFile");

        if(m_serverApkVersion == null) {
            StringBuilder rainbowVersionFileUrl = new StringBuilder();
            IApplicationData appData = RainbowContext.getPlatformServices().getApplicationData();
            rainbowVersionFileUrl.append(appData.getServerUrl());
            rainbowVersionFileUrl.append("/downloads/apk-version.txt");

            downloadServerVersionFile(rainbowVersionFileUrl.toString(), new IAsyncServiceResultCallback<String>() {
                @Override
                public void handleResult(AsyncServiceResponseResult<String> asyncResult) {
                    Log.getLogger().info(LOG_TAG, "download ServerVersionFile result");
                    if (asyncResult.exceptionRaised()) {
                        Log.getLogger().error(LOG_TAG, "downloadVersionFile failed");
                        callback.onFailed();
                    } else {
                        Log.getLogger().info(LOG_TAG, "downloadVersionFile success");
                        callback.onSuccess(asyncResult.getResult());
                    }
                }
            });
        } else {
            callback.onSuccess(m_serverApkVersion);
        }
    }

    @Override
    public boolean canProposeUpdate(String currentVersion, String fileContent)
    {
        if( !StringsUtil.isNullOrEmpty(fileContent) ) {
            if( fileContent.startsWith(APK_STARTINGCONTENT) ) {
                m_serverApkVersion = fileContent.substring(APK_STARTINGCONTENT.length()).trim();
            } else {
                m_serverApkVersion = fileContent.trim();
            }
        }

        if ((currentVersion != null) && (m_serverApkVersion != null) && !currentVersion.equals(m_serverApkVersion))
        {
            String[] splittedCurrentVersion = currentVersion.split("\\.");
            String[] splittedApkVersion = m_serverApkVersion.split("\\.");

            if (splittedCurrentVersion.length != splittedApkVersion.length)
            {
                return true;
            }
            else
            {
                try
                {
                    for (int i = 0; i < splittedCurrentVersion.length; i++)
                    {
                        int currentVersionPart = Integer.parseInt(splittedCurrentVersion[i]);
                        int apkVersionPart = Integer.parseInt(splittedApkVersion[i]);

                        if (currentVersionPart == apkVersionPart) {
                            continue;
                        } else {
                            return (currentVersionPart < apkVersionPart);
                        }
                    }
                    return true;
                }
                catch (Exception e)
                {
                    Log.getLogger().error(LOG_TAG, "Impossible to parse current version " + currentVersion + " or rainbow version ");
                    return true;
                }
            }
        }

        return false;
    }


    protected void downloadServerVersionFile(String fileUrl, final IAsyncServiceResultCallback<String> callback) {
        Log.getLogger().info(LOG_TAG, ">downloadServerVersionFile");

        m_restAsyncRequest.getTextFile(fileUrl, new IAsyncServiceResultCallback<String>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<String> asyncResult) {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "getVersionFile failed");
                    notifyGetVersionFileResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "getVersionFile success");
                    try {
                        notifyGetVersionFileResult(callback, null, asyncResult.getResult());
                    } catch (Exception e) {
                        e.printStackTrace();
                        notifyGetVersionFileResult(callback, new RainbowServiceException(e), null);
                    }
                }
            }
        });
    }

    private void notifyGetVersionFileResult(IAsyncServiceResultCallback<String> callback, RainbowServiceException alcServiceException, String content)
    {
        AsyncServiceResponseResult<String> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, content);
        callback.handleResult(asyncResult);
    }

    @Override
    public String getApkPath()
    {
        StringBuilder rainbowAppFileUrlAppender = new StringBuilder();
        IApplicationData appData = RainbowContext.getPlatformServices().getApplicationData();
        rainbowAppFileUrlAppender.append(appData.getServerUrl());
        rainbowAppFileUrlAppender.append("/downloads/");
        rainbowAppFileUrlAppender.append(APK_FILENAME);

        return rainbowAppFileUrlAppender.toString();
    }
}

package com.ale.infra.rainbow.adapter;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.http.GetFileResponse;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.RESTResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseVoid;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.manager.fileserver.RainbowFileViewer;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.proxy.fileserver.Consumption;
import com.ale.infra.proxy.fileserver.FileDescriptorListResponse;
import com.ale.infra.proxy.fileserver.FileDescriptorResponse;
import com.ale.infra.proxy.framework.RainbowServiceTag;
import com.ale.infra.rainbow.api.ApisConstants;
import com.ale.infra.rainbow.api.IRainbowFileService;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;


public class RainbowFileServiceAdapter implements IRainbowFileService
{
    private static final String LOG_TAG = "RainbowFileServiceAdapter";

    private final IRESTAsyncRequest m_restAsyncRequest;
    private IPlatformServices m_platformServices;

    public RainbowFileServiceAdapter(IRESTAsyncRequest restAsyncRequest, IPlatformServices platformServices)
    {
        m_restAsyncRequest = restAsyncRequest;
        m_platformServices = platformServices;
    }

    private String getUrl()
    {
        String url = RainbowContext.getPlatformServices().getApplicationData().getServerUrl();
        if (url == null)
        {
            url = StringsUtil.EMPTY;
        }
        return url;
    }

    @Override
    public RainbowServiceTag getTag()
    {
        return RainbowServiceTag.USERS;
    }


    @Override
    public void refreshOwnFileDescriptors(String fileNameFilter, Boolean isUploadedFilter, String format,
                                          final IAsyncServiceResultCallback<FileDescriptorListResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">refreshOwnFileDescriptors");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.FILE_STORAGE);


        try
        {
            if( !StringsUtil.isNullOrEmpty(format)) {
                restUrl.append("?format=");
                restUrl.append(format);
            } else
                restUrl.append("?format=full");

            if (!StringsUtil.isNullOrEmpty(fileNameFilter))
                restUrl.append("&fileName="  + fileNameFilter);
            if( isUploadedFilter != null)
                restUrl.append("&isUploaded="  + isUploadedFilter);
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "refreshOwnFileDescriptors FAILURE");
                    Log.getLogger().verbose(LOG_TAG, ">refreshOwnFileDescriptors : " + asyncResult.getException().getDetailsMessage());
                    notifyGetFileDescriptorListResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "refreshOwnFileDescriptors SUCCESS");
                    try {
                        notifyGetFileDescriptorListResult(callback, null, new FileDescriptorListResponse(asyncResult.getResult().getResponse()));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST RainbowFileDescriptor result");
                        notifyGetFileDescriptorListResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void getFileDescriptor(String fileId, final IAsyncServiceResultCallback<FileDescriptorResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">getFileDescriptor");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.FILE_STORAGE);
        restUrl.append("/");
        restUrl.append(fileId);


        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "getFileDescriptor FAILURE");
                    Log.getLogger().verbose(LOG_TAG, ">getFileDescriptor : " + asyncResult.getException().getDetailsMessage());
                    notifyGetFileDescriptorResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "getFileDescriptor SUCCESS");
                    try {
                        notifyGetFileDescriptorResult(callback, null, new FileDescriptorResponse(asyncResult.getResult().getResponse()));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST RainbowFileDescriptor result");
                        notifyGetFileDescriptorResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void deleteFileDescriptor(String fileId, final IAsyncServiceVoidCallback callback) {
        Log.getLogger().verbose(LOG_TAG, ">deleteFileDescriptor");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.FILE_STORAGE);
        restUrl.append("/");
        restUrl.append(fileId);


        m_restAsyncRequest.sendDeleteRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "deleteFileDescriptor FAILURE");
                    Log.getLogger().verbose(LOG_TAG, ">deleteFileDescriptor : " + asyncResult.getException().getDetailsMessage());
                    notifyDeleteFileDescriptorResult(callback, asyncResult.getException());
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "deleteFileDescriptor SUCCESS");
                    try {
                        notifyDeleteFileDescriptorResult(callback, null);
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST deleteFileDescriptor result");
                        notifyDeleteFileDescriptorResult(callback, new RainbowServiceException(error));
                    }
                }
            }
        });
    }

    @Override
    public void deleteViewer(String fileId, String viewerId, final IAsyncServiceVoidCallback callback) {
        Log.getLogger().verbose(LOG_TAG, ">deleteFileDescriptor");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.FILE_STORAGE);
        restUrl.append("/");
        restUrl.append(fileId);
        restUrl.append(ApisConstants.FILE_STORAGE_VIEWERS);
        restUrl.append("/");
        restUrl.append(viewerId);


        m_restAsyncRequest.sendDeleteRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "deleteViewer FAILURE");
                    Log.getLogger().verbose(LOG_TAG, ">deleteViewer : " + asyncResult.getException().getDetailsMessage());
                    notifyDeleteFileDescriptorResult(callback, asyncResult.getException());
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "deleteViewer SUCCESS");
                    try {
                        notifyDeleteFileDescriptorResult(callback, null);
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST deleteViewer result");
                        notifyDeleteFileDescriptorResult(callback, new RainbowServiceException(error));
                    }
                }
            }
        });
    }

    @Override
    public void addViewer(String fileId, RainbowFileViewer viewer, final IAsyncServiceVoidCallback callback) {
        Log.getLogger().verbose(LOG_TAG, ">addViewer");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.FILE_STORAGE);
        restUrl.append("/");
        restUrl.append(fileId);
        restUrl.append(ApisConstants.FILE_STORAGE_VIEWERS);

        JSONObject restBody = new JSONObject();
        try
        {
            if( viewer != null) {
                restBody.put("viewerId", viewer.getId());
                restBody.put("type", viewer.getType().toString());
            }
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        //Log.getLogger().verbose(LOG_TAG, "BODY="+restBody.toString());

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "addViewer FAILURE");
                    Log.getLogger().verbose(LOG_TAG, ">addViewer : " + asyncResult.getException().getDetailsMessage());
                    notifyAddViewerResult(callback, asyncResult.getException());
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "addViewer SUCCESS");
                    notifyAddViewerResult(callback, null);
                }
            }
        });

    }

    @Override
    public void getConsumption(final IAsyncServiceResultCallback<Consumption> callback)
    {
        Log.getLogger().verbose(LOG_TAG, ">getConsumption");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.FILE_STORAGE_CONSUMTION);

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "getConsumption FAILURE");
                    Log.getLogger().verbose(LOG_TAG, ">getConsumption : " + asyncResult.getException().getDetailsMessage());
                    notifyGetConsumptionResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "addViewer SUCCESS");

                    Consumption consumption = new Consumption(asyncResult.getResult());

                    notifyGetConsumptionResult(callback, null, consumption);
                }
            }
        });
    }

    private void notifyGetConsumptionResult(IAsyncServiceResultCallback<Consumption> callback, RainbowServiceException e, Consumption consumption)
    {
        AsyncServiceResponseResult<Consumption> asyncResult = new AsyncServiceResponseResult<>(e, consumption);
        callback.handleResult(asyncResult);
    }


    @Override
    public void createFileDescriptor(String fileName, String extension, Long size, RainbowFileViewer viewer, final IAsyncServiceResultCallback<FileDescriptorResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">createFileDescriptor");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.FILE_STORAGE);

        JSONObject restBody = new JSONObject();
        try
        {
            if( !StringsUtil.isNullOrEmpty(fileName))
                restBody.put("fileName", fileName);
            if( !StringsUtil.isNullOrEmpty(extension))
                restBody.put("extension", extension);
            if( size != null)
                restBody.put("size", size);
            if( viewer != null ) {
                JSONArray viewersJsonArray = new JSONArray();
                JSONObject viewerJsonObj = new JSONObject();
                viewerJsonObj.put("viewerId", viewer.getId());
                viewerJsonObj.put("type", viewer.getType().toString());

                viewersJsonArray.put(viewerJsonObj);
                restBody.put("viewers",viewersJsonArray);
            }
        }
        catch (Exception ex)
        {
            Log.getLogger().error(LOG_TAG, "Error while filling JSON Object");
        }

        //Log.getLogger().verbose(LOG_TAG, "BODY="+restBody.toString());

        m_restAsyncRequest.sendPostRequest(restUrl.toString(), restBody, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "createFileDescriptor FAILURE");
                    Log.getLogger().verbose(LOG_TAG, ">refreshOwnFileDescriptors : " + asyncResult.getException().getDetailsMessage());
                    notifyCreateFileDescriptorResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "createFileDescriptor SUCCESS");
                    try {
                        notifyCreateFileDescriptorResult(callback, null, new FileDescriptorResponse(asyncResult.getResult().getResponse()));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST RainbowFileDescriptor result");
                        notifyCreateFileDescriptorResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void uploadFile(byte[] bytes, String fileId, final IAsyncServiceResultCallback<FileDescriptorResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">uploadFile");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.FILE_SERVER);
        restUrl.append("/");
        restUrl.append(fileId);


        m_restAsyncRequest.uploadBuffer(restUrl.toString(), bytes, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "uploadFile FAILURE : " + asyncResult.getException().getDetailsMessage());
                    notifyUploadFileResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "uploadFile SUCCESS");
                    try {
                        notifyUploadFileResult(callback, null, new FileDescriptorResponse(asyncResult.getResult().getResponse()));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST uploadFile result");
                        notifyUploadFileResult(callback, null, null);
                    }
                }
            }
        });
    }

    @Override
    public void uploadFilePart(boolean isLastPart, int partNumber, byte[] bytes, String fileId, final IAsyncServiceResultCallback<FileDescriptorResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">uploadFilePart");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.FILE_SERVER);
        restUrl.append("/");
        restUrl.append(fileId);
        restUrl.append("/parts/");
        if( !isLastPart )
            restUrl.append(partNumber);
        else
            restUrl.append("end");


        m_restAsyncRequest.uploadBuffer(restUrl.toString(), bytes, new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult) {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "uploadFile FAILURE : " + asyncResult.getException().getDetailsMessage());
                    notifyUploadFileResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "uploadFile SUCCESS");
                    try {
                        notifyUploadFileResult(callback, null, new FileDescriptorResponse(asyncResult.getResult().getResponse()));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST uploadFile result");
                        notifyUploadFileResult(callback, null, null);
                    }
                }
            }
        });

    }

    @Override
    public void refreshOtherFileDescriptors(String userId, final IAsyncServiceResultCallback<FileDescriptorListResponse> callback)
    {
        Log.getLogger().verbose(LOG_TAG, ">refreshOtherFileDescriptors");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.FILE_STORAGE);
        restUrl.append(ApisConstants.FILE_STORAGE_VIEWERS);
        restUrl.append("/");
        restUrl.append(userId);
        restUrl.append("?format=full");

        m_restAsyncRequest.sendGetRequest(restUrl.toString(), new IAsyncServiceResultCallback<RESTResult>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<RESTResult> asyncResult)
            {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "refreshOtherFileDescriptors FAILURE");
                    Log.getLogger().verbose(LOG_TAG, ">refreshOtherFileDescriptors : " + asyncResult.getException().getDetailsMessage());
                    notifyGetFileDescriptorListResult(callback, asyncResult.getException(), null);
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "refreshOtherFileDescriptors SUCCESS");
                    try {
                        notifyGetFileDescriptorListResult(callback, null, new FileDescriptorListResponse(asyncResult.getResult().getResponse()));
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST RainbowFileDescriptor result");
                        notifyGetFileDescriptorListResult(callback, new RainbowServiceException(error), null);
                    }
                }
            }
        });
    }

    @Override
    public void downloadFileByRange(String fileId, Range range, final IAsyncServiceResultCallback<GetFileResponse> callback) {
        Log.getLogger().verbose(LOG_TAG, ">downloadFileByRange");

        StringBuilder restUrl = new StringBuilder(getUrl());
        restUrl.append(ApisConstants.FILE_SERVER);
        restUrl.append("/");
        restUrl.append(fileId);

        m_restAsyncRequest.downloadFile(restUrl.toString(), range, new IAsyncServiceResultCallback<GetFileResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetFileResponse> asyncResult) {
                if (asyncResult.exceptionRaised())
                {
                    Log.getLogger().error(LOG_TAG, "downloadFileByRange FAILURE : " + asyncResult.getException().getDetailsMessage());
                    notifyDownloadFileResult(callback, null, asyncResult.getException());
                }
                else
                {
                    Log.getLogger().info(LOG_TAG, "downloadFileByRange SUCCESS");
                    try {
                        notifyDownloadFileResult(callback, asyncResult.getResult(), null);
                    }
                    catch (Exception error)
                    {
                        Log.getLogger().error(LOG_TAG, "Impossible to parse REST downloadFileByRange result");
                        notifyDownloadFileResult(callback, null, new RainbowServiceException(error));
                    }
                }
            }
        });
    }

    private void notifyAddViewerResult(IAsyncServiceVoidCallback callback, RainbowServiceException exception) {
        AsyncServiceResponseVoid asyncResult = new AsyncServiceResponseVoid(exception);
        callback.handleResult(asyncResult);
    }

    private void notifyDeleteFileDescriptorResult(IAsyncServiceVoidCallback callback, RainbowServiceException exception) {
        AsyncServiceResponseVoid asyncResult = new AsyncServiceResponseVoid(exception);
        callback.handleResult(asyncResult);
    }

    private void notifyDownloadFileResult(IAsyncServiceResultCallback<GetFileResponse> callback, GetFileResponse response, RainbowServiceException exception) {
        AsyncServiceResponseResult<GetFileResponse> asyncResult = new AsyncServiceResponseResult<>(exception, response);
        callback.handleResult(asyncResult);
    }

    private void notifyUploadFileResult(IAsyncServiceResultCallback<FileDescriptorResponse> callback, RainbowServiceException alcServiceException, FileDescriptorResponse response) {
        AsyncServiceResponseResult<FileDescriptorResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyCreateFileDescriptorResult(IAsyncServiceResultCallback<FileDescriptorResponse> callback, RainbowServiceException alcServiceException, FileDescriptorResponse response) {
        AsyncServiceResponseResult<FileDescriptorResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyGetFileDescriptorResult(IAsyncServiceResultCallback<FileDescriptorResponse> callback, RainbowServiceException alcServiceException, FileDescriptorResponse response) {
        AsyncServiceResponseResult<FileDescriptorResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

    private void notifyGetFileDescriptorListResult(IAsyncServiceResultCallback<FileDescriptorListResponse> callback, RainbowServiceException alcServiceException, FileDescriptorListResponse response) {
        AsyncServiceResponseResult<FileDescriptorListResponse> asyncResult = new AsyncServiceResponseResult<>(alcServiceException, response);
        callback.handleResult(asyncResult);
    }

}

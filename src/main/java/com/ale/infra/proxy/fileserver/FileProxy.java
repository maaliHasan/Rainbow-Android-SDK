package com.ale.infra.proxy.fileserver;


import com.ale.infra.http.GetFileResponse;
import com.ale.infra.http.IRESTAsyncRequest;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseResult;
import com.ale.infra.http.adapter.concurrent.AsyncServiceResponseVoid;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.manager.fileserver.IFileProxy;
import com.ale.infra.manager.fileserver.RainbowFileDescriptor;
import com.ale.infra.manager.fileserver.RainbowFileViewer;
import com.ale.infra.platformservices.IPlatformServices;
import com.ale.infra.rainbow.adapter.Range;
import com.ale.infra.rainbow.api.IRainbowFileService;
import com.ale.infra.rainbow.api.IServicesFactory;
import com.ale.util.log.Log;

import java.net.HttpURLConnection;

/**
 * Created by georges on 10/02/2017.
 */

public class FileProxy implements IFileProxy {

    private static final String LOG_TAG = "FileProxy";



    private IRainbowFileService m_fileService;
    private int m_uploadPacketNumber;

    public FileProxy(IServicesFactory servicesFactory, IRESTAsyncRequest restAsyncRequest, IPlatformServices platformService)
    {
        Log.getLogger().info(LOG_TAG, "initialization");
        m_fileService = servicesFactory.createFileService(restAsyncRequest, platformService);
    }

    @Override
    public void refreshOwnFileDescriptors(final IRefreshListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">refreshOwnFileDescriptors");

        m_fileService.refreshOwnFileDescriptors(null, null, null, new IAsyncServiceResultCallback<FileDescriptorListResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<FileDescriptorListResponse> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "refreshOwnFileDescriptors SUCCESS");

                    if (listener != null) {
                        FileDescriptorListResponse result = asyncResult.getResult();
                        listener.onRefreshSuccess(result.getFileDescriptorList());
                    }
                } else {
                    Log.getLogger().info(LOG_TAG, "refreshOwnFileDescriptors FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onRefreshFailed();
                }
            }
        });
    }

    @Override
    public void getFileDescriptor(String fileId, final IGetFileDescriptorListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">getFileDescriptor");

        m_fileService.getFileDescriptor(fileId, new IAsyncServiceResultCallback<FileDescriptorResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<FileDescriptorResponse> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "refreshOwnFileDescriptors SUCCESS");

                    if (listener != null) {
                        FileDescriptorResponse result = asyncResult.getResult();
                        listener.onGetFileDescriptorSuccess(result.getFileDescriptor());
                    }
                } else {
                    Log.getLogger().info(LOG_TAG, "refreshOwnFileDescriptors FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onGetFileDescriptorFailed(asyncResult.getException());
                }
            }
        });
    }

    @Override
    public void deleteFileDescriptor(String fileId, final IDeleteFileListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">deleteFileDescriptor");

        m_fileService.deleteFileDescriptor(fileId, new IAsyncServiceVoidCallback() {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "deleteFileDescriptor SUCCESS");

                    if (listener != null)
                        listener.onDeletionSuccess();
                } else {
                    Log.getLogger().info(LOG_TAG, "deleteFileDescriptor FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onDeletionError();
                }
            }
        });
    }

    @Override
    public void deleteViewer(String id, String viewerId, final IDeleteViewerListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">deleteFileDescriptor");

        m_fileService.deleteViewer(id, viewerId, new IAsyncServiceVoidCallback() {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "deleteFileDescriptor SUCCESS");

                    if (listener != null)
                        listener.onDeletionSuccess();
                } else {
                    Log.getLogger().info(LOG_TAG, "deleteFileDescriptor FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onDeletionError();
                }
            }
        });
    }

    @Override
    public void addViewer(String id, RainbowFileViewer viewer, final IAddViewerListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">addViewer");

        m_fileService.addViewer(id, viewer, new IAsyncServiceVoidCallback() {
            @Override
            public void handleResult(AsyncServiceResponseVoid asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "addViewer SUCCESS");

                    if (listener != null)
                        listener.onAddSuccess();
                } else {
                    Log.getLogger().info(LOG_TAG, "addViewer FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onAddError();
                }
            }
        });
    }

    @Override
    public void getConsumption(final IGetConsumptionListener listener)
    {
        Log.getLogger().verbose(LOG_TAG, ">getConsumption");

        m_fileService.getConsumption(new IAsyncServiceResultCallback<Consumption>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<Consumption> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "getConsumption SUCCESS");

                    if (listener != null)
                        listener.onGetSuccess(asyncResult.getResult());
                } else {
                    Log.getLogger().info(LOG_TAG, "getConsumption FAILURE", asyncResult.getException());

                    if (listener != null)
                        listener.onGetError();
                }
            }
        });
    }

    @Override
    public void refreshOtherFileDescriptors(String userId, final IRefreshListener listener)
    {
        Log.getLogger().verbose(LOG_TAG, ">refreshOtherFileDescriptors");

        m_fileService.refreshOtherFileDescriptors(userId, new IAsyncServiceResultCallback<FileDescriptorListResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<FileDescriptorListResponse> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "refreshOtherFileDescriptors SUCCESS");

                    if (listener != null) {
                        FileDescriptorListResponse result = asyncResult.getResult();
                        listener.onRefreshSuccess(result.getFileDescriptorList());
                    }
                } else {
                    Log.getLogger().info(LOG_TAG, "refreshOtherFileDescriptors FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onRefreshFailed();
                }
            }
        });
    }

    @Override
    public void createFileDescriptor(String fileName, String extension, Long size, RainbowFileViewer viewer, final ICreateFileDescriptorListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">createFileDescriptor");

        m_fileService.createFileDescriptor(fileName, extension, size, viewer, new IAsyncServiceResultCallback<FileDescriptorResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<FileDescriptorResponse> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "createFileDescriptor SUCCESS");

                    RainbowFileDescriptor fileDescriptor = asyncResult.getResult().getFileDescriptor();
                    if (listener != null) {
                        listener.onCreateSuccess(fileDescriptor);
                    }
                } else {
                    Log.getLogger().info(LOG_TAG, "createFileDescriptor FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onCreateFailed(asyncResult.getException());
                }
            }
        });
    }

    @Override
    public void uploadFile(byte[] bytes, String fileId, final IUploadFileListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">uploadFile");

        m_fileService.uploadFile(bytes, fileId, new IAsyncServiceResultCallback<FileDescriptorResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<FileDescriptorResponse> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "uploadFile SUCCESS");

                    if (listener != null) {
                        FileDescriptorResponse result = asyncResult.getResult();
                        if( result != null)
                            listener.onUploadSuccess(result.getFileDescriptor());
                    }
                } else {
                    Log.getLogger().info(LOG_TAG, "uploadFile FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onUploadFailed(asyncResult.getException());
                }
            }
        });
    }

    @Override
    public void uploadFilePart(boolean isLastPart, int partNumber, byte[] bytes, RainbowFileDescriptor fileDescriptor, final IUploadFileListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">uploadFile");

        m_fileService.uploadFilePart(isLastPart, partNumber, bytes, fileDescriptor.getId(), new IAsyncServiceResultCallback<FileDescriptorResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<FileDescriptorResponse> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "uploadFile SUCCESS");

                    if (listener != null) {
                        FileDescriptorResponse result = asyncResult.getResult();
                        if( result != null)
                            listener.onUploadSuccess(result.getFileDescriptor());
                    }
                } else {
                    Log.getLogger().info(LOG_TAG, "uploadFile FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onUploadFailed(asyncResult.getException());
                }
            }
        });
    }

    @Override
    public void downloadFileByRange(String fileId, Range range, final IDownloadFileListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">downloadFileByRange");

        m_fileService.downloadFileByRange(fileId, range, new IAsyncServiceResultCallback<GetFileResponse>() {
            @Override
            public void handleResult(AsyncServiceResponseResult<GetFileResponse> asyncResult) {
                if (!asyncResult.exceptionRaised()) {
                    Log.getLogger().info(LOG_TAG, "downloadFileByRange SUCCESS");

                    if (listener != null) {
                        GetFileResponse result = asyncResult.getResult();

                        if( result.isFileFullyDownloaded() ) {
                            listener.onDownloadSuccess(result);
                        } else {
                            listener.onDownloadInProgress(result);
                        }
                    }
                } else {
                    Log.getLogger().info(LOG_TAG, "downloadFileByRange FAILURE", asyncResult.getException());
                    if (listener != null)
                        listener.onDownloadFailed(asyncResult.getException().getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND || asyncResult.getException().getStatusCode() == HttpURLConnection.HTTP_FORBIDDEN);
                }
            }
        });
    }

}

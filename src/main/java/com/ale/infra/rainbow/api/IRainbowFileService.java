package com.ale.infra.rainbow.api;

import com.ale.infra.http.GetFileResponse;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.http.adapter.concurrent.IAsyncServiceVoidCallback;
import com.ale.infra.manager.fileserver.RainbowFileViewer;
import com.ale.infra.proxy.fileserver.Consumption;
import com.ale.infra.proxy.fileserver.FileDescriptorListResponse;
import com.ale.infra.proxy.fileserver.FileDescriptorResponse;
import com.ale.infra.rainbow.adapter.Range;

/**
 * Created by georges on 10/02/2017.
 */

public interface IRainbowFileService extends IRainbowService {

    void refreshOwnFileDescriptors(String filename, Boolean isUploaded, String format,
                                   IAsyncServiceResultCallback<FileDescriptorListResponse> callback);

    void createFileDescriptor(String fileName, String extension, Long size, RainbowFileViewer viewer, IAsyncServiceResultCallback<FileDescriptorResponse> callback);

    void uploadFile(byte[] bytes, String fileId, IAsyncServiceResultCallback<FileDescriptorResponse> callback);

    void downloadFileByRange(String fileId, Range range, IAsyncServiceResultCallback<GetFileResponse> callback);

    void getFileDescriptor(String fileId, IAsyncServiceResultCallback<FileDescriptorResponse> callback);

    void deleteFileDescriptor(String fileId, IAsyncServiceVoidCallback callback);

    void deleteViewer(String id, String viewerId, IAsyncServiceVoidCallback callback);

    void addViewer(String id, RainbowFileViewer viewer, IAsyncServiceVoidCallback callback);

    void getConsumption(IAsyncServiceResultCallback<Consumption> callback);
    void uploadFilePart(boolean isLastPart, int partNumber, byte[] bytes, String fileId, IAsyncServiceResultCallback<FileDescriptorResponse> callback);

    void refreshOtherFileDescriptors(String userId, IAsyncServiceResultCallback<FileDescriptorListResponse> iAsyncServiceResultCallback);
}

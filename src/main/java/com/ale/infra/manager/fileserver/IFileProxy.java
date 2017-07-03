package com.ale.infra.manager.fileserver;

import com.ale.infra.http.GetFileResponse;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.proxy.fileserver.Consumption;
import com.ale.infra.rainbow.adapter.Range;

import java.util.List;

/**
 * Created by georges on 10/02/2017.
 */

public interface IFileProxy {

    void refreshOwnFileDescriptors(IRefreshListener listener);

    void createFileDescriptor(String fileName, String extension, Long size, RainbowFileViewer viewer, ICreateFileDescriptorListener listener);

    void uploadFile(byte[] bytes, String fileId, IUploadFileListener listener);

    void uploadFilePart(boolean isLastPart, int partNumber, byte[] bytes, RainbowFileDescriptor fileDescriptor, IUploadFileListener listener);

    void downloadFileByRange(String fileId, Range range, final IDownloadFileListener listener);

    void getFileDescriptor(String fileId, IGetFileDescriptorListener listener);

    void deleteFileDescriptor(String fileId, IDeleteFileListener listener);

    void deleteViewer(String id, String viewerId, IDeleteViewerListener listener);

    void addViewer(String id, RainbowFileViewer viewer, IAddViewerListener listener);

    void getConsumption(IGetConsumptionListener listener);

    void refreshOtherFileDescriptors(String userId, IRefreshListener listener);

    interface IRefreshListener
    {
        void onRefreshSuccess(List<RainbowFileDescriptor> fileDescriptorList);

        void onRefreshFailed();
    }

    interface IUploadFileListener {
        void onUploadSuccess(RainbowFileDescriptor fileDescriptor);

        void onUploadInProgress(int percent);

        void onUploadFailed(RainbowServiceException exception);
    }

    interface IDownloadFileListener {
        void onDownloadSuccess(GetFileResponse result);

        void onDownloadInProgress(GetFileResponse result);

        void onDownloadFailed(boolean notFound);
    }

    interface ICreateFileDescriptorListener {
        void onCreateSuccess(RainbowFileDescriptor fileDescriptor);

        void onCreateFailed(RainbowServiceException exception);

        void onUploadInProgress(int percent);
    }

    interface IDeleteFileListener
    {
        void onDeletionSuccess();

        void onDeletionError();
    }

    interface IGetFileDescriptorListener {
        void onGetFileDescriptorSuccess(RainbowFileDescriptor fileDescriptor);

        void onGetFileDescriptorFailed(RainbowServiceException exception);
    }

    interface IDeleteViewerListener {
        void onDeletionSuccess();

        void onDeletionError();
    }

    interface IAddViewerListener {
        void onAddSuccess();

        void onAddError();
    }

    interface IGetConsumptionListener
    {
        void onGetSuccess(Consumption consumption);

        void onGetError();
    }
}

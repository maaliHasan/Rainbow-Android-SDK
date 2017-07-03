package com.ale.infra.manager.fileserver;

import com.ale.infra.list.ArrayItemList;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * Created by georges on 10/02/2017.
 */

public interface IFileMgr {
    void refreshOwnFileDescriptors(IFileProxy.IRefreshListener iRefreshListener);

    void refreshOtherFileDescriptors(IFileProxy.IRefreshListener iRefreshListener);

    void uploadNewFile(InputStream inputStream, String fileName, String mimeType, String extension, Long size, RainbowFileViewer viewer, IFileProxy.ICreateFileDescriptorListener listener);

    void reuploadExistingFile(RainbowFileDescriptor fileDescriptor, File file, IFileProxy.ICreateFileDescriptorListener listener);

    ArrayItemList<RainbowFileDescriptor> getOtherFileDescriptorList();

    ArrayItemList<RainbowFileDescriptor> getOwnFileDescriptorList();

    void deleteFileDescriptorList(List<RainbowFileDescriptor> fileDescList, IFileProxy.IDeleteFileListener listener);

    void deleteFileDescriptor(RainbowFileDescriptor fileDesc, IFileProxy.IDeleteFileListener listener);

    FileServerCacheMgr getFileCache();

    RainbowFileDescriptor getFileDescriptorFromUrl(String url, IFileProxy.IGetFileDescriptorListener listener);

    void downloadFile(RainbowFileDescriptor fileDescriptor, IFileProxy.IDownloadFileListener listener);

    String getIdFromUrl(String url);

    RainbowFileDescriptor getFileDescriptorFromId(String fileId);

    void deleteViewer(RainbowFileDescriptor fileDescriptor, String viewerIdToDelete, IFileProxy.IDeleteViewerListener listener);

    List<RainbowFileDescriptor> getOwnFilesFilteredListWithViewer(String filteredViewerId);

    void addViewer(RainbowFileDescriptor fileDescriptor, RainbowFileViewer viewer, IFileProxy.IAddViewerListener listener);

    void getConsumption(IFileProxy.IGetConsumptionListener listener);

    List<RainbowFileDescriptor> getOtherFilesFilteredListWithViewer(String showOnlyViewerId);

    List<RainbowFileDescriptor> getOwnFilesFilteredListWithRoom(String showOnlyRoomId);

    List<RainbowFileDescriptor> getOtherFilesFilteredListWithRoom(String showOnlyRoomId);
}

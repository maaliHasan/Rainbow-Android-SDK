package com.ale.infra.manager.fileserver;

import android.content.Context;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.http.GetFileResponse;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.proxy.fileserver.Consumption;
import com.ale.infra.rainbow.adapter.Range;
import com.ale.util.FileUtil;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


/**
 * Created by georges on 10/02/2017.
 */

public class FileServerMgr implements IFileMgr {
    private static final String LOG_TAG = "FileServerMgr";

    public static final int DOWNLOADFILE_RANGE_VALUE = 200000;
    public static final int UPLOADFILE_RANGE_VALUE = 200000;

    private final IFileProxy m_fileProxy;
    private final FileServerCacheMgr m_fileServerCache;

    private ArrayItemList<RainbowFileDescriptor> m_fileDescList = new ArrayItemList<>();
    private ArrayItemList<RainbowFileDescriptor> m_ownFileDescList = new ArrayItemList<>();

    // Download By Range Management
    private Map<String, Range> m_ranges = new HashMap<>();

    public FileServerMgr(Context context, IFileProxy fileProxy) {
        m_fileProxy = fileProxy;

        m_fileServerCache = new FileServerCacheMgr(context);
    }

    private String getUserId()
    {
        return RainbowContext.getPlatformServices().getApplicationData().getUserId();
    }

    @Override
    public void refreshOwnFileDescriptors(final IFileProxy.IRefreshListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">refreshOwnFileDescriptors");

        m_fileProxy.refreshOwnFileDescriptors(new IFileProxy.IRefreshListener() {
            @Override
            public void onRefreshSuccess(List<RainbowFileDescriptor> fileDescriptorList) {
                Log.getLogger().info(LOG_TAG, "onRefreshOwnSuccess");

                m_ownFileDescList.replaceAll(fileDescriptorList);

                if( listener != null)
                    listener.onRefreshSuccess(fileDescriptorList);
                for(final RainbowFileDescriptor fileDescriptor : m_ownFileDescList.getCopyOfDataList()) {
                    File fileCached = m_fileServerCache.findFileStartingBy(fileDescriptor.getId());
                    if( fileDescriptor.isUploaded() ) {
                        Log.getLogger().verbose(LOG_TAG, "Download content of file");

                        if( fileCached != null) {
                            fileDescriptor.setFile(fileCached);
                        }
                    }
                }
            }

            @Override
            public void onRefreshFailed() {
                Log.getLogger().warn(LOG_TAG, "onRefreshOwnFailed");
                if( listener != null)
                    listener.onRefreshFailed();
            }
        });
    }

    @Override
    public void refreshOtherFileDescriptors(final IFileProxy.IRefreshListener listener)
    {
        Log.getLogger().verbose(LOG_TAG, ">refreshOtherFileDescriptors");

        m_fileProxy.refreshOtherFileDescriptors(getUserId(), new IFileProxy.IRefreshListener() {
            @Override
            public void onRefreshSuccess(List<RainbowFileDescriptor> fileDescriptorList) {
                Log.getLogger().info(LOG_TAG, "onRefreshOtherSuccess");

                List<RainbowFileDescriptor> descriptorList = new ArrayList<>();

                for(RainbowFileDescriptor descriptor : fileDescriptorList)
                {
                    if(!descriptor.getOwnerId().equals(getUserId()))
                        descriptorList.add(descriptor);
                    else
                        Log.getLogger().verbose(LOG_TAG, "Filtered room file sent by user: " + descriptor.getFileName());
                }

                m_fileDescList.replaceAll(descriptorList);

                if( listener != null)
                    listener.onRefreshSuccess(descriptorList);
                for(final RainbowFileDescriptor fileDescriptor : m_fileDescList.getCopyOfDataList()) {
                    File fileCached = m_fileServerCache.findFileStartingBy(fileDescriptor.getId());
                    if( fileDescriptor.isUploaded() ) {
                        Log.getLogger().verbose(LOG_TAG, "Download content of file");

                        if( fileCached != null) {
                            fileDescriptor.setFile(fileCached);
                        }
                    }
                }
            }

            @Override
            public void onRefreshFailed() {
                Log.getLogger().warn(LOG_TAG, "onRefreshOtherFailed");
                if( listener != null)
                    listener.onRefreshFailed();
            }
        });
    }

    private String computeSavedFileName(RainbowFileDescriptor fileDescriptor) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(fileDescriptor.getId());
        fileName.append("_");
        fileName.append(fileDescriptor.getUploadedDate());
        String extensionForMimeType = FileUtil.getExtensionForMimeType(fileDescriptor.getTypeMIME());
        if( !StringsUtil.isNullOrEmpty(extensionForMimeType) ) {
            fileName.append(".");
            fileName.append(extensionForMimeType);
        }

        return fileName.toString();
    }

    //private long m_startTime;

    @Override
    public void uploadNewFile(final InputStream inputStream, String fileName, String mimeType, String extension, Long size, RainbowFileViewer viewer, final IFileProxy.ICreateFileDescriptorListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">uploadNewFile");

        StringBuilder fileNameWithExt = new StringBuilder(fileName);
        if( !FileUtil.doesFileEndsWithExtension(fileName) ) {
            String fileExt = FileUtil.getExtensionForMimeType(mimeType);
            if (fileExt != null) {
                fileNameWithExt.append(".");
                fileNameWithExt.append(fileExt);
            }
        }

        m_fileProxy.createFileDescriptor(fileNameWithExt.toString(), extension, size, viewer, new IFileProxy.ICreateFileDescriptorListener() {
            @Override
            public void onCreateSuccess(final RainbowFileDescriptor fileDescCreated) {
                Log.getLogger().info(LOG_TAG, "onCreateSuccess");

                m_ownFileDescList.add(fileDescCreated);

                final byte[] fileContent = FileUtil.readInputStream(inputStream);

                //m_startTime = System.currentTimeMillis();

                if( fileDescCreated.getSize() > UPLOADFILE_RANGE_VALUE)
                    uploadFileByParts(0, fileDescCreated, fileContent, listener);
                else
                    uploadFileFull(fileDescCreated, fileContent, listener);
            }

            @Override
            public void onCreateFailed(RainbowServiceException exception) {
                Log.getLogger().warn(LOG_TAG, "onCreateFailed");
                if( listener != null)
                    listener.onCreateFailed(exception);
            }

            @Override
            public void onUploadInProgress(int percent) {

            }
        });
    }

    @Override
    public void reuploadExistingFile(RainbowFileDescriptor fileDescriptor, final File file, final IFileProxy.ICreateFileDescriptorListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">uploadNewFile");

        fileDescriptor.setSize(file.length());

        final byte[] fileContent = FileUtil.readFileContent(file);

        if( file.length() > UPLOADFILE_RANGE_VALUE)
            uploadFileByParts(0, fileDescriptor, fileContent, listener);
        else
            uploadFileFull(fileDescriptor, fileContent, listener);
    }

    private void uploadFileByParts(final int partNumber, final RainbowFileDescriptor fileDesc, final byte[] fileContent, final IFileProxy.ICreateFileDescriptorListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">uploadFileByParts");

        int start = partNumber * UPLOADFILE_RANGE_VALUE;
        int end = start +UPLOADFILE_RANGE_VALUE;
        boolean isLastPart = false;
        if( end >= fileContent.length) {
            end = fileContent.length;
            isLastPart = true;
        }
        byte [] fileContentPart = Arrays.copyOfRange(fileContent, start, end);

        final boolean finalIsLastPart = isLastPart;
        m_fileProxy.uploadFilePart(false, partNumber, fileContentPart, fileDesc, new IFileProxy.IUploadFileListener() {

            @Override
            public void onUploadSuccess(RainbowFileDescriptor fileDescUploaded) {
                Log.getLogger().info(LOG_TAG, "onUploadSuccess");

                if(finalIsLastPart) {
                    // Wrong Size : do not update
                    //fileDesc.update(fileDescUploaded);

//                    long endTime = System.currentTimeMillis();
//                    Log.getLogger().debug(LOG_TAG, "MEASURE UPLOAD = " + (endTime-m_startTime));

                    m_fileProxy.uploadFilePart(true, partNumber, null, fileDesc, new IFileProxy.IUploadFileListener() {
                        @Override
                        public void onUploadSuccess(RainbowFileDescriptor fileDescriptor) {
                            Log.getLogger().info(LOG_TAG, "onUploadSuccess of End Part");

                            fileDesc.update(fileDescriptor);

                            File file = m_fileServerCache.save(computeSavedFileName(fileDesc), fileContent);
                            fileDesc.setFile(file);

                            if (listener != null)
                                listener.onCreateSuccess(fileDescriptor);
                        }

                        @Override
                        public void onUploadInProgress(int percent) {
                            if (listener != null)
                                listener.onUploadInProgress(percent);
                        }

                        @Override
                        public void onUploadFailed(RainbowServiceException exception) {
                            Log.getLogger().warn(LOG_TAG, "onUploadFailed");
                            if (listener != null)
                                listener.onCreateFailed(exception);
                        }
                    });


                    if (listener != null)
                        listener.onUploadInProgress(100);
                } else {
                    int nbParts = (int) (fileDesc.getSize()/UPLOADFILE_RANGE_VALUE);
                    if( fileDesc.getSize()%UPLOADFILE_RANGE_VALUE > 0)
                        nbParts++;
                    int percent = ((partNumber+1)*100)/nbParts;
                    if (listener != null)
                        listener.onUploadInProgress(percent);

                    uploadFileByParts(partNumber+1, fileDesc, fileContent, listener);
                }
            }

            @Override
            public void onUploadInProgress(int percent) {
                if (listener != null)
                    listener.onUploadInProgress(percent);
            }

            @Override
            public void onUploadFailed(RainbowServiceException exception) {
                Log.getLogger().warn(LOG_TAG, "onUploadFailed");
                if (listener != null)
                    listener.onCreateFailed(exception);
            }
        });

    }

    private void uploadFileFull(final RainbowFileDescriptor fileDesc, final byte[] fileContent, final IFileProxy.ICreateFileDescriptorListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">uploadFileFull");

        m_fileProxy.uploadFile(fileContent, fileDesc.getId(), new IFileProxy.IUploadFileListener() {
            @Override
            public void onUploadSuccess(RainbowFileDescriptor fileDescUploaded) {
                Log.getLogger().info(LOG_TAG, "onUploadSuccess");

                fileDesc.update(fileDescUploaded);

                File file = m_fileServerCache.save(computeSavedFileName(fileDesc), fileContent);
                fileDesc.setFile(file);

                if( listener != null)
                    listener.onCreateSuccess(fileDesc);
            }

            @Override
            public void onUploadInProgress(int percent) {

            }

            @Override
            public void onUploadFailed(RainbowServiceException exception) {
                Log.getLogger().warn(LOG_TAG, "onUploadFailed");
                if( listener != null)
                    listener.onCreateFailed(exception);
            }
        });
    }

    @Override
    public ArrayItemList<RainbowFileDescriptor> getOtherFileDescriptorList() {
        return m_fileDescList;
    }

    @Override
    public ArrayItemList<RainbowFileDescriptor> getOwnFileDescriptorList() {
        return m_ownFileDescList;
    }

    @Override
    public void deleteFileDescriptorList(final List<RainbowFileDescriptor> fileDescList, final IFileProxy.IDeleteFileListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">deleteFileDescriptorList");
        if ( fileDescList == null || fileDescList.size() == 0) {
            Log.getLogger().warn(LOG_TAG, "No FileDescriptor to delete given");
            return;
        }
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                final CountDownLatch remainingRequest = new CountDownLatch(fileDescList.size());

                final Integer[] requestSuccessCounter = {0};

                for (RainbowFileDescriptor fileDesc : fileDescList) {

                    deleteFileDescriptor(fileDesc, new IFileProxy.IDeleteFileListener() {
                        @Override
                        public void onDeletionSuccess() {
                            Log.getLogger().verbose(LOG_TAG, ">onDeletionSuccess");
                            requestSuccessCounter[0]++;
                            remainingRequest.countDown();
                        }

                        @Override
                        public void onDeletionError() {
                            Log.getLogger().warn(LOG_TAG, ">onDeletionError");
                            remainingRequest.countDown();
                        }
                    });
                }

                try {
                    Log.getLogger().verbose(LOG_TAG, "before await");
                    remainingRequest.await(10, TimeUnit.SECONDS);
                    Log.getLogger().verbose(LOG_TAG, "after await");
                } catch (InterruptedException e) {
                    Log.getLogger().error(LOG_TAG, "Exception while waiting: "+e.getMessage());
                }

                if(requestSuccessCounter[0] == fileDescList.size()) {
                    if (listener != null)
                        listener.onDeletionSuccess();
                } else {
                    if (listener != null)
                        listener.onDeletionError();
                }
            }
        });
        myThread.start();

    }

    @Override
    public void deleteFileDescriptor(final RainbowFileDescriptor fileDesc, final IFileProxy.IDeleteFileListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">deleteFileDescriptorList");
        if ( fileDesc == null ) {
            Log.getLogger().warn(LOG_TAG, "No FileDescriptor to delete given");
            return;
        }

        m_fileProxy.deleteFileDescriptor(fileDesc.getId(),new IFileProxy.IDeleteFileListener() {
            @Override
            public void onDeletionSuccess() {
                Log.getLogger().info(LOG_TAG, "onDeletionSuccess");
                fileDesc.setState(RainbowFileDescriptor.State.DELETED);
                m_fileDescList.delete(fileDesc);
                m_ownFileDescList.delete(fileDesc);
                if( listener != null)
                    listener.onDeletionSuccess();
            }

            @Override
            public void onDeletionError() {
                Log.getLogger().warn(LOG_TAG, "onDeletionError");
                if( listener != null)
                    listener.onDeletionError();
            }
        });
    }

    @Override
    public FileServerCacheMgr getFileCache() {
        return m_fileServerCache;
    }

    @Override
    public RainbowFileDescriptor getFileDescriptorFromUrl(String url, final IFileProxy.IGetFileDescriptorListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">getFileDescriptorFromUrl");

        String id = getIdFromUrl(url);
        if( id != null) {
            RainbowFileDescriptor fileDescriptor = getFileDescriptorFromId(id);
            if( fileDescriptor != null)
                return fileDescriptor;

            // Create New FileDescriptor
            fileDescriptor = new RainbowFileDescriptor();
            fileDescriptor.setId(id);
            fileDescriptor.setState(RainbowFileDescriptor.State.RESOLVING);

            final RainbowFileDescriptor finalFileDescriptor = fileDescriptor;
            m_fileProxy.getFileDescriptor(id, new IFileProxy.IGetFileDescriptorListener() {
                @Override
                public void onGetFileDescriptorSuccess(final RainbowFileDescriptor fileDescResult) {
                    Log.getLogger().info(LOG_TAG, "onGetFileDescriptorSuccess");

                    // Retrieve Object from cache and update it:
                    finalFileDescriptor.update(fileDescResult);
                    finalFileDescriptor.setState(RainbowFileDescriptor.State.RESOLVED);

                    addFileDescriptor(finalFileDescriptor);

                    if( finalFileDescriptor.isUploaded() ) {
                        Log.getLogger().verbose(LOG_TAG, "Download content of file");

                        File fileCached = m_fileServerCache.findFileStartingBy(finalFileDescriptor.getId());
                        if( fileCached != null) {
                            finalFileDescriptor.setFile(fileCached);
                            if(listener != null)
                                listener.onGetFileDescriptorSuccess(finalFileDescriptor);
                        } else {
                            if( !RainbowContext.getInfrastructure().isPermissionAllowed(WRITE_EXTERNAL_STORAGE) ) {
                                Log.getLogger().warn(LOG_TAG, "Not able to download the File given in Message (Permission on External Storage)");
                                return;
                            }

                            if( !finalFileDescriptor.isImageType() || finalFileDescriptor.getSize() > 512 ) {
                                Log.getLogger().warn(LOG_TAG, "Not an image or bigger than 512kB - skip");
                                if(listener != null)
                                    listener.onGetFileDescriptorSuccess(finalFileDescriptor);
                                return;
                            }

                            m_fileProxy.downloadFileByRange(finalFileDescriptor.getId(), null, new IFileProxy.IDownloadFileListener() {
                                @Override
                                public void onDownloadSuccess(GetFileResponse result) {
                                    Log.getLogger().info(LOG_TAG, "onDownloadSuccess");

                                    File file = m_fileServerCache.save(computeSavedFileName(finalFileDescriptor), result.getFileContent());
                                    finalFileDescriptor.setFile(file);

                                    if(listener != null)
                                        listener.onGetFileDescriptorSuccess(finalFileDescriptor);
                                }

                                @Override
                                public void onDownloadInProgress(GetFileResponse result) {
                                }

                                @Override
                                public void onDownloadFailed(boolean notFound) {
                                    Log.getLogger().warn(LOG_TAG, "onDownloadFailed");
                                }
                            });
                        }
                    } else {
                        if(listener != null)
                            listener.onGetFileDescriptorSuccess(finalFileDescriptor);
                    }
                }

                @Override
                public void onGetFileDescriptorFailed(RainbowServiceException exception) {
                    Log.getLogger().warn(LOG_TAG, "onGetFileDescriptorFailed");

                    if( exception.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND || exception.getStatusCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                        m_fileDescList.delete(finalFileDescriptor);
                        m_ownFileDescList.delete(finalFileDescriptor);
                        finalFileDescriptor.setState(RainbowFileDescriptor.State.DELETED);
                    }

                    if(listener != null)
                        listener.onGetFileDescriptorFailed(exception);
                }
            });

            return fileDescriptor;
        }
        return null;
    }

    private void addFileDescriptor(RainbowFileDescriptor fileDescriptor) {

        if (fileDescriptor == null)
            return;
        
        String ownerId = fileDescriptor.getOwnerId();
        if( ownerId != null && ownerId.equals(getUserId()))
            m_ownFileDescList.add(fileDescriptor);
        else
            m_fileDescList.add(fileDescriptor);
    }


    //private long m_startTime;
    @Override
    public void downloadFile(final RainbowFileDescriptor fileDescriptor, final IFileProxy.IDownloadFileListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">downloadFileByRange");
        if (fileDescriptor == null)
            return;
        Range range = m_ranges.get(fileDescriptor.getId());

        if(range != null)
        {
            Log.getLogger().warn(LOG_TAG, "File already downloading");
        }
        else
        {
            range = new Range(0, DOWNLOADFILE_RANGE_VALUE);

            m_ranges.put(fileDescriptor.getId(),range);

            File file = m_fileServerCache.save(computeSavedFileName(fileDescriptor), null);
            try {
                FileOutputStream fileDownloaded = new FileOutputStream(file);
                downloadFileInternal(fileDescriptor, listener, range, fileDownloaded);
            } catch (FileNotFoundException e) {
                Log.getLogger().error(LOG_TAG, "FileNotFoundException: "+e.getMessage());

                if(listener != null)
                    listener.onDownloadFailed(false);
            }
        }
    }

    private void downloadFileInternal(final RainbowFileDescriptor fileDescriptor, final IFileProxy.IDownloadFileListener listener, Range range, final FileOutputStream fileDownloaded)
    {
        if (fileDescriptor == null)
            return;
        m_fileProxy.downloadFileByRange(fileDescriptor.getId(), range, new IFileProxy.IDownloadFileListener() {
            @Override
            public void onDownloadSuccess(GetFileResponse result) {
                Log.getLogger().info(LOG_TAG, "onDownloadSuccess");

                m_ranges.remove(fileDescriptor.getId());

                try {
                    fileDownloaded.write(result.getFileContent());

                fileDescriptor.setPercentDownloaded(100);
                File file = m_fileServerCache.save(computeSavedFileName(fileDescriptor), null);
                fileDescriptor.setFile(file);

                if(listener != null)
                    listener.onDownloadSuccess(result);

                } catch (IOException e) {
                    Log.getLogger().error(LOG_TAG, "IOException: "+e.getMessage());

                    if(listener != null)
                        listener.onDownloadFailed(false);
                }
            }

            @Override
            public void onDownloadInProgress(GetFileResponse result) {
                Log.getLogger().verbose(LOG_TAG, ">onDownloadInProgress: "+ result.getPercentDownloaded());

                Range downloadRange = m_ranges.get(fileDescriptor.getId());

                int lastEnd = downloadRange.getEnd();
                downloadRange.setStart(lastEnd+1);
                downloadRange.setEnd(lastEnd+DOWNLOADFILE_RANGE_VALUE);

                fileDescriptor.setPercentDownloaded(result.getPercentDownloaded());
                try {
                    fileDownloaded.write(result.getFileContent());
                    downloadFileInternal(fileDescriptor, listener, downloadRange, fileDownloaded);
                } catch (IOException e) {
                    Log.getLogger().error(LOG_TAG, "IOException: "+e.getMessage());

                    if(listener != null)
                        listener.onDownloadFailed(false);
                }
            }

            @Override
            public void onDownloadFailed(boolean notFound) {
                Log.getLogger().warn(LOG_TAG, "onDownloadFailed");

                m_ranges.remove(fileDescriptor.getId());

                if(notFound)
                {
                    fileDescriptor.setState(RainbowFileDescriptor.State.DELETED);
                    fileDescriptor.notifyFileDescriptorUpdated();
                }

                if(listener != null)
                    listener.onDownloadFailed(notFound);
            }
        });
    }

    @Override
    public RainbowFileDescriptor getFileDescriptorFromId(String id) {
        for(RainbowFileDescriptor fileDescriptor : m_fileDescList.getCopyOfDataList()) {
            if( fileDescriptor != null && fileDescriptor.getId().equals(id))
                return fileDescriptor;
        }
        for(RainbowFileDescriptor fileDescriptor : m_ownFileDescList.getCopyOfDataList()) {
            if( fileDescriptor != null && fileDescriptor.getId().equals(id))
                return fileDescriptor;
        }
        return null;
    }

    @Override
    public void deleteViewer(final RainbowFileDescriptor fileDescriptor, final String viewerIdToDelete, final IFileProxy.IDeleteViewerListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">deleteViewer");

        if (fileDescriptor == null)
            return;
        m_fileProxy.deleteViewer(fileDescriptor.getId(), viewerIdToDelete, new IFileProxy.IDeleteViewerListener() {
            @Override
            public void onDeletionSuccess() {
                Log.getLogger().info(LOG_TAG, "onDeletionSuccess");

                fileDescriptor.deleteViewer(viewerIdToDelete);

                if(listener != null)
                    listener.onDeletionSuccess();
            }

            @Override
            public void onDeletionError() {
                Log.getLogger().warn(LOG_TAG, "onDeletionError");
                if(listener != null)
                    listener.onDeletionError();
            }
        });
    }

    @Override
    public List<RainbowFileDescriptor> getOwnFilesFilteredListWithViewer(String filteredViewerId) {
        List<RainbowFileDescriptor> filteredList = new ArrayList<>();
        for(RainbowFileDescriptor fileDescriptor : m_ownFileDescList.getCopyOfDataList()) {
            if( fileDescriptor != null && fileDescriptor.containsViewer(filteredViewerId) ) {
                filteredList.add(fileDescriptor);
            }
        }

        return filteredList;
    }

    @Override
    public void addViewer(final RainbowFileDescriptor fileDescriptor, final RainbowFileViewer viewer, final IFileProxy.IAddViewerListener listener) {
        Log.getLogger().verbose(LOG_TAG, ">addViewer");

        if (fileDescriptor == null)
            return;

        m_fileProxy.addViewer(fileDescriptor.getId(), viewer, new IFileProxy.IAddViewerListener() {
            @Override
            public void onAddSuccess() {
                Log.getLogger().info(LOG_TAG, "onAddSuccess");

                fileDescriptor.addViewer(viewer);

                if(listener != null)
                    listener.onAddSuccess();
            }

            @Override
            public void onAddError() {
                Log.getLogger().warn(LOG_TAG, "onAddError");
                if(listener != null)
                    listener.onAddError();
            }
        });

    }

    @Override
    public void getConsumption(final IFileProxy.IGetConsumptionListener listener)
    {
        Log.getLogger().verbose(LOG_TAG, ">getConsumption");

        m_fileProxy.getConsumption(new IFileProxy.IGetConsumptionListener()
        {
            @Override
            public void onGetSuccess(Consumption consumption)
            {
                Log.getLogger().info(LOG_TAG, "onGetSuccess");
                if(listener != null)
                    listener.onGetSuccess(consumption);
            }

            @Override
            public void onGetError()
            {
                Log.getLogger().warn(LOG_TAG, "onGetError");
                if(listener != null)
                    listener.onGetError();
            }
        });
    }

    @Override
    public List<RainbowFileDescriptor> getOtherFilesFilteredListWithViewer(String showOnlyViewerId)
    {
        List<RainbowFileDescriptor> filteredList = new ArrayList<>();
        for(RainbowFileDescriptor fileDescriptor : m_fileDescList.getCopyOfDataList()) {
            if( fileDescriptor != null && fileDescriptor.getOwnerId().equals(showOnlyViewerId) && fileDescriptor.containsViewer(getUserId())) {
                filteredList.add(fileDescriptor);
            }
        }

        return filteredList;
    }

    @Override
    public List<RainbowFileDescriptor> getOwnFilesFilteredListWithRoom(String showOnlyRoomId)
    {
        return getOwnFilesFilteredListWithViewer(showOnlyRoomId);
    }

    @Override
    public List<RainbowFileDescriptor> getOtherFilesFilteredListWithRoom(String showOnlyRoomId)
    {
        List<RainbowFileDescriptor> filteredList = new ArrayList<>();
        for(RainbowFileDescriptor fileDescriptor : m_fileDescList.getCopyOfDataList()) {
            if( fileDescriptor != null && fileDescriptor.containsViewer(showOnlyRoomId) ) {
                filteredList.add(fileDescriptor);
            }
        }

        return filteredList;
    }

    @Override
    public String getIdFromUrl(String url) {
        String[] urlSplitted = url.split("/");
        if( urlSplitted.length > 0) {
            return urlSplitted[urlSplitted.length-1];
        }
        return null;
    }

}

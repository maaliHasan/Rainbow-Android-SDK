package com.ale.infra.manager.fileserver;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.contact.IBitmapConverter;
import com.ale.infra.data_model.IMultiSelectable;
import com.ale.infra.rainbow.api.ApisConstants;
import com.ale.rainbow.BitmapConverter;
import com.ale.util.FileUtil;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by georges on 10/02/2017.
 */

public class RainbowFileDescriptor implements IMultiSelectable, Comparable<RainbowFileDescriptor>
{
    private static final String LOG_TAG = "RainbowFileDescriptor";

    public static final String MIME_IMAGE_START = "image/";
    public static final String MIME_IMAGE_JPG = "image/jpeg";
    public static final String MIME_IMAGE_PNG = "image/png";
    public static final String MIME_PDF = "application/pdf";

    private IBitmapConverter m_bmpConverter;
    private int percentDownloaded = -1;

    @Override
    public int compareTo(@NonNull RainbowFileDescriptor another)
    {
        if (another == null )
            return 1;
        
        if (getUploadedDate() == null && another.getUploadedDate() == null)
            return 0;

        if(getUploadedDate() == null)
            return -1;

        if(another.getUploadedDate() == null)
            return 1;

        return getUploadedDate().compareTo(another.getUploadedDate());
    }

    public enum State {
        RESOLVING,
        RESOLVED,
        DELETED,
        OTHER
    };


    private String m_fileName;
    private String m_extension;
    private String m_ownerId;
    private List<RainbowFileViewer> m_viewers = new ArrayList<>();
    private boolean m_isUploaded = false;
    private long m_size;
    private String m_typeMIME;
    private String m_id;
    private Date m_uploadedDate;
    private Date m_registrationDate;

    private Bitmap m_image;
    private File m_file;
    private Uri m_fileUri;

    private State m_state = State.OTHER;

    private Set<IFileDescriptorListener> m_changeListeners = new HashSet<>();

    public RainbowFileDescriptor() {
        m_bmpConverter = new BitmapConverter();
    }

    public Bitmap getImage() {
        return m_image;
    }

    public File getFile() {
        return m_file;
    }

    public boolean isImageType() {
        if( StringsUtil.isNullOrEmpty(getTypeMIME()) )
            return false;

        return getTypeMIME().startsWith(MIME_IMAGE_START);
    }

    public boolean isPdfFileType() {
        if( StringsUtil.isNullOrEmpty(getTypeMIME()) )
            return false;

        return getTypeMIME().equals(MIME_PDF);
    }

    public boolean isAudioType() {
        if( StringsUtil.isNullOrEmpty(getTypeMIME()) )
            return false;

        return getTypeMIME().startsWith("audio");
    }

    public void setFile(File file) {
        m_file = file;
        if( isImageType() && file != null) {
            m_image = m_bmpConverter.createBitmapFromFilePath(file.getAbsolutePath());
        }
        else if( isPdfFileType())
            setPdfPreviewImage();

        notifyFileDescriptorUpdated();
    }


    private void setPdfPreviewImage() {
        if( m_file == null ) {
            Log.getLogger().warn(LOG_TAG, "getPdfPreviewImage: file inside fileDescriptor is NULL");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Bitmap pdfPreview = Bitmap.createBitmap(BitmapConverter.DEFAULT_MAX_SIZE, BitmapConverter.DEFAULT_MAX_SIZE, Bitmap.Config.ARGB_4444);

                ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(m_file, ParcelFileDescriptor.MODE_READ_ONLY);
                PdfRenderer renderer = new PdfRenderer(parcelFileDescriptor);

                // let us just render all pages
                if( renderer.getPageCount() > 0 ) {
                    PdfRenderer.Page page = renderer.openPage(0);

                    // say we render for showing on the screen
                    page.render(pdfPreview, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    setImage(pdfPreview);

                    // disconnect the page
                    page.close();
                }

                // disconnect the renderer
                renderer.close();
            } catch (IOException e) {
                Log.getLogger().error(LOG_TAG, "Error while rendering pdf preview: ", e);
            }
        }
    }


    public void setPreviewFile(File file, int bitmapMaxSize) {
        m_file = file;
        if( isImageType()) {
            m_image = m_bmpConverter.createBitmapFromFilePath(file.getPath(), bitmapMaxSize);
        }

        notifyFileDescriptorUpdated();
    }

    public void setImageFile(byte[] fileContent) {
        BitmapConverter converter = new BitmapConverter();

        m_image = converter.createBitmapFromByteArray(fileContent);

        notifyFileDescriptorUpdated();
    }

    public void setImage(Bitmap bitmap) {
        m_image = bitmap;

        notifyFileDescriptorUpdated();
    }


    public Date getRegistrationDate() {
        return m_registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.m_registrationDate = registrationDate;
    }

    public Date getUploadedDate() {
        return m_uploadedDate;
    }

    public void setUploadedDate(Date uploadedDate) {
        this.m_uploadedDate = uploadedDate;
    }

    public String getFileName() {
        return m_fileName;
    }

    public void setFileName(String fileName) {
        this.m_fileName = fileName;
    }

    public String getExtension() {
        return m_extension;
    }

    public void setExtension(String extension) {
        this.m_extension = extension;
    }

    public String getOwnerId() {
        return m_ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.m_ownerId = ownerId;
    }

    public List<RainbowFileViewer> getViewers() {
        return m_viewers;
    }

    public void setViewers(List<RainbowFileViewer> viewers) {
        this.m_viewers = viewers;
    }

    public void addViewer(RainbowFileViewer viewer) {
        m_viewers.add(viewer);
    }

    public void deleteViewer(String viewerIdToDelete) {
        RainbowFileViewer viewer = getViewerFromId(viewerIdToDelete);
        if( viewer != null)
            m_viewers.remove(viewer);
        notifyFileDescriptorUpdated();
    }

    private RainbowFileViewer getViewerFromId(String viewerId) {
        for(RainbowFileViewer fileViewer : m_viewers) {
            if( fileViewer.getId().equals(viewerId))
                return fileViewer;
        }
        return null;
    }


    public boolean isUploaded() {
        return m_isUploaded;
    }

    public void setIsUploaded(boolean isUploaded) {
        this.m_isUploaded = isUploaded;
    }

    public long getSize() {
        return m_size;
    }

    public void setSize(Long size) {
        m_size = size;
    }

    public String getTypeMIME() {
        return m_typeMIME;
    }

    public void setTypeMIME(String typeMIME) {
        this.m_typeMIME = typeMIME;
    }

    public String getShortType(String unknown) {
        String fileExt = FileUtil.getExtensionForMimeType(this.m_typeMIME);
        if( fileExt == null)
            return unknown;
        return fileExt.toUpperCase();
    }

    public void setPercentDownloaded(int percentDownloaded) {
        this.percentDownloaded = percentDownloaded;
        notifyFileDescriptorUpdated();
    }

    public int getPercentDownloaded() {
        return percentDownloaded;
    }

    public String getId() {
        return m_id;
    }

    public void setId(String id) {
        this.m_id = id;
    }

    public String getFileUrl() {
        StringBuilder url = new StringBuilder();
        url.append(RainbowContext.getPlatformServices().getApplicationData().getServerUrl());
        url.append(ApisConstants.FILE_SERVER);
        url.append("/");
        url.append(getId());

        return url.toString();
    }

    @Override
    public int getSelectableType() {
        return 0;
    }

    public boolean containsViewer(String filteredViewerId) {
        for(RainbowFileViewer viewer : m_viewers) {
            if( viewer.getId().equals(filteredViewerId))
                return true;
        }
        return false;
    }

    public synchronized void registerChangeListener(IFileDescriptorListener changeListener)
    {
        if( !m_changeListeners.contains(changeListener)) {
            m_changeListeners.add(changeListener);
        }
    }

    public synchronized void unregisterChangeListener(IFileDescriptorListener changeListener)
    {
        m_changeListeners.remove(changeListener);
    }


    public synchronized void notifyFileDescriptorUpdated()
    {
        for (IFileDescriptorListener listener : m_changeListeners.toArray(new IFileDescriptorListener[m_changeListeners.size()]))
        {
            listener.onFileDescriptorUpdated(this);
        }
    }


    public void dumpInLog(String dumpLogTag) {
        Log.getLogger().info(dumpLogTag, "    ---");
        Log.getLogger().info(dumpLogTag, "    FILE DESCRIPTOR :");
        if (!StringsUtil.isNullOrEmpty(m_fileName)) {
            Log.getLogger().info(dumpLogTag, "    fileName=" + m_fileName);
        }
        if (!StringsUtil.isNullOrEmpty(m_extension)) {
            Log.getLogger().info(dumpLogTag, "    extension=" + m_extension);
        }
        if (!StringsUtil.isNullOrEmpty(m_ownerId)) {
            Log.getLogger().info(dumpLogTag, "    ownerId=" + m_ownerId);
        }
        if (!StringsUtil.isNullOrEmpty(m_typeMIME)) {
            Log.getLogger().info(dumpLogTag, "    typeMIME=" + m_typeMIME);
        }
        if (!StringsUtil.isNullOrEmpty(m_id)) {
            Log.getLogger().info(dumpLogTag, "    id=" + m_id);
        }
        if (m_viewers.size() > 0) {
            Log.getLogger().info(dumpLogTag, "    VIEWERS");
            for (RainbowFileViewer viewer : m_viewers) {
                Log.getLogger().info(dumpLogTag, "      viewerId=" + viewer.getId());
                if (viewer.isUser())
                    Log.getLogger().info(dumpLogTag, "      viewer type User");
                else if (viewer.isRoom())
                    Log.getLogger().info(dumpLogTag, "      viewer type Room");
            }
        }
        Log.getLogger().info(dumpLogTag, "    isUploaded=" + m_isUploaded);
        if (m_uploadedDate != null) {
            Log.getLogger().info(dumpLogTag, "    uploadedDate=" + m_uploadedDate);
        }
        if (m_registrationDate != null) {
            Log.getLogger().info(dumpLogTag, "    registrationDate=" + m_registrationDate);
        }
        if (m_image != null) {
            Log.getLogger().info(dumpLogTag, "    image available");
        }
        if (m_file != null) {
            Log.getLogger().info(dumpLogTag, "    file available: " + m_file.length());
        }
        Log.getLogger().info(dumpLogTag, "    size=" + m_size);
        Log.getLogger().info(dumpLogTag, "    ---");
    }

    public void update(RainbowFileDescriptor fileDescResult) {
        if( fileDescResult == null)
            return;

        if( !StringsUtil.isNullOrEmpty(fileDescResult.getId())) {
            m_id = fileDescResult.getId();
        }
        if( !StringsUtil.isNullOrEmpty(fileDescResult.getFileName())) {
            m_fileName = fileDescResult.getFileName();
        }
        if( !StringsUtil.isNullOrEmpty(fileDescResult.getExtension())) {
            m_extension = fileDescResult.getExtension();
        }
        if( !StringsUtil.isNullOrEmpty(fileDescResult.getOwnerId())) {
            m_ownerId = fileDescResult.getOwnerId();
        }
        if( !StringsUtil.isNullOrEmpty(fileDescResult.getTypeMIME())) {
            m_typeMIME = fileDescResult.getTypeMIME();
        }
        m_isUploaded = fileDescResult.isUploaded();
        m_size = fileDescResult.getSize();

        if( fileDescResult.getUploadedDate() != null) {
            m_uploadedDate = fileDescResult.getUploadedDate();
            m_isUploaded = fileDescResult.isUploaded();
        }
        if( fileDescResult.getRegistrationDate() != null) {
            m_registrationDate = fileDescResult.getRegistrationDate();
        }
        if( fileDescResult.getImage() != null) {
            m_image = fileDescResult.getImage();
        }
        if( fileDescResult.getFile() != null) {
            setFile(fileDescResult.getFile() );
        }
        if( fileDescResult.getViewers() != null) {
            m_viewers = fileDescResult.getViewers();
        }

        notifyFileDescriptorUpdated();
    }

    public Uri getFileUri() {
        return m_fileUri;
    }

    public void setFileUri(Uri fileUri) {
        this.m_fileUri = fileUri;
    }

    public State getState() {
        return m_state;
    }

    public void setState(State state) {
        this.m_state = state;
    }

    public boolean isDeleted() {
        return( m_state.equals(State.DELETED) );
    }
}

package com.ale.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import com.ale.util.log.Log;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by georges on 14/02/2017.
 */

public class FileUtil {

    private static final String LOG_TAG = "FileUtil";

    public static void moveFile(String inputPath, String inputFile, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {
            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }

            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            deleteFile(inputPath, inputFile);
        }

        catch (FileNotFoundException fnfe1) {
            Log.getLogger().error(LOG_TAG, fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.getLogger().error("tag", e.getMessage());
        }
    }

    public static void deleteFile(String inputPath, String inputFile) {
        try {
            // delete the original file
            File file = new File(inputPath + inputFile);
            file.delete();
        }
        catch (Exception e) {
            Log.getLogger().error("tag", e.getMessage());
        }
    }

    public static void copyFile(String inputPath, String inputFileName, String outputPath, String outputFileName) {

        InputStream in = null;
        OutputStream out = null;
        try {
            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }

            in = new FileInputStream(inputPath + "/" + inputFileName);
            out = new FileOutputStream(outputPath + "/" + outputFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;
        }
        catch (FileNotFoundException fnfe1) {
            Log.getLogger().error(LOG_TAG, fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.getLogger().error(LOG_TAG, e.getMessage());
        }
    }

    public static File storeFileFromInputStream(Context context, InputStream inputStream, String fileName) {
        File file = new File(context.getCacheDir(), fileName);
        try {
            OutputStream output = new FileOutputStream(file);
            try {
                byte[] buffer = new byte[4 * 1024];
                int read;

                while ((read = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                output.flush();
            } finally {
                output.close();
                inputStream.close();
            }
        } catch (Exception e) {
            Log.getLogger().error(LOG_TAG, e.getMessage());
        }
        return file;
    }


    public static byte[] readInputStream(InputStream inputStream) {
        ByteArrayOutputStream ous = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();

            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        } catch (IOException e) {
            Log.getLogger().error(LOG_TAG, e.getMessage());
        } finally {
            try {
                if (ous != null)
                    ous.close();
            } catch (IOException e) {
                Log.getLogger().error(LOG_TAG, "Exception while closing ous : ", e);
            }

            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                Log.getLogger().error(LOG_TAG, "Exception while closing ios : ", e);
            }
        }
        return ous.toByteArray();
    }



    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        // DocumentProvider
        if (isKitKat() && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                String id = DocumentsContract.getDocumentId(uri);

                if(id.startsWith("raw:"))
                    return id.substring("raw:".length());
                else
                {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                }
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private static boolean isKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * @return Whether the URI is a local one.
     */
    public static boolean isLocal(String url) {
        if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
            return true;
        }
        return false;
    }

    public static void copyFileStream(File dest, Uri uri, Context context)
            throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if( is != null)
                is.close();
            if( os != null)
                os.close();
        }
    }

    public static File getExternalDownloadFilesDir(String fileName) {
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if( fileName != null )
            return new File(directory, fileName);

        return directory;
    }

    public static String getExtensionForMimeType(String mimeType) {
        if( StringsUtil.isNullOrEmpty(mimeType))
            return null;

        if (mimeType.equalsIgnoreCase("image/jpeg") ) {
            return "jpeg";
        } else if (mimeType.equalsIgnoreCase("image/png") ) {
            return "png";
        } else if (mimeType.equalsIgnoreCase("image/jp2") ) {
            return "jp2";
        } else if (mimeType.equalsIgnoreCase("application/pdf") ) {
            return "pdf";
        } else if (mimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ) {
            return "docx";
        } else if (mimeType.equalsIgnoreCase("application/msword") ) {
            return "doc";
        } else if (mimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.presentationml.presentation") ) {
            return "pptx";
        } else if (mimeType.equalsIgnoreCase("application/vnd.ms-powerpoint") ) {
            return "ppt";
        } else if (mimeType.equalsIgnoreCase("application/vnd.oasis.opendocument.text") ) {
            return "odt";
        } else if (mimeType.equalsIgnoreCase("application/vnd.ms-excel") ) {
            return "xls";
        } else if (mimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ) {
            return "xlsx";
        } else if (mimeType.equalsIgnoreCase("audio/mpeg") ) {
            return "mp3";
        } else if (mimeType.equalsIgnoreCase("application/ogg") ) {
            return "ogg";
        } else if (mimeType.equalsIgnoreCase("application/vnd.android.package-archive") ) {
            return "apk";
        } else  {
            Log.getLogger().warn(LOG_TAG, "Unknown file type: " + mimeType);
            return null;
        }
    }

    public static boolean doesFileEndsWithExtension(String fileName) {
        if( StringsUtil.isNullOrEmpty(fileName))
            return false;

        String extension = FilenameUtils.getExtension(fileName);
        return !StringsUtil.isNullOrEmpty(extension);
    }

    public static String getMimeType(Uri uri, Context context) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public static File getFileFromURI(Uri contentUri, Context context)
    {
        File fileDest = null;
        String filename = null;

        if (!contentUri.getScheme().equals(ContentResolver.SCHEME_CONTENT))
        {
            // Get the File path from the Uri
            String path = FileUtil.getPath(context, contentUri);
            if (path == null)
            {
                fileDest = new File(FilenameUtils.getName(contentUri.toString()));
            }
            else
            {
                fileDest = new File(path);
            }
        }
        else
        {
            Cursor returnCursor = context.getContentResolver().query(contentUri, null, null, null, null);

            if (returnCursor != null)
            {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();

                String path = FileUtil.getPath(context, contentUri);
                try
                {
                    fileDest = new File(path);
                }
                catch (Exception e)
                {
                    Log.getLogger().verbose(LOG_TAG, "Exception when trying to open File : " + e.getMessage());
                }
                filename = returnCursor.getString(nameIndex);
                returnCursor.close();
            }

            if (fileDest == null)
            {
                fileDest = FileUtil.getExternalDownloadFilesDir(filename);
                try
                {
                    FileUtil.copyFileStream(fileDest, contentUri, context);
                }
                catch (Exception e)
                {
                    Log.getLogger().error(LOG_TAG, "Exception when trying to copy File : " + e.getMessage());
                }
            }
        }

        return fileDest;
    }

    public static void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    public static byte[] readFileContent(File file) {
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            Log.getLogger().error(LOG_TAG, "FileNotFoundException : " + e.getMessage());
        } catch (IOException e) {
            Log.getLogger().error(LOG_TAG, "IOException : " + e.getMessage());
        }

        return bytes;
    }
}

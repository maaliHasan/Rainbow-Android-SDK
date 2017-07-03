package com.ale.infra.manager.fileserver;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;

import com.ale.util.log.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by georges on 13/02/2017.
 */

public class FileServerCacheMgr {

    private static final String LOG_TAG = "FileServerCacheMgr";

    private Context m_context;

    public FileServerCacheMgr(Context context)
    {
        m_context = context;
    }

    public File createFile(String fileName) {

        if( !isPermissionAllowed(WRITE_EXTERNAL_STORAGE) ) {
            return null;
        }

        File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File rainbowCacheDirectory = new File(downloadDirectory.getPath()+"/rainbow");
        if( !rainbowCacheDirectory.exists())
            rainbowCacheDirectory.mkdirs();

        return new File(rainbowCacheDirectory, fileName);
    }

    public boolean isPermissionAllowed(String androidPermission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // This Build is < 6 , you can Access to permission
            return true;
        }
        if (m_context.checkSelfPermission(androidPermission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        return false;
    }

    public File save(String fileName, byte[] fileContent) {
        Log.getLogger().verbose(LOG_TAG, ">save");

        FileOutputStream fileOutputStream = null;
        File internalFile = null;
        try {
            internalFile = createFile(fileName);
            if( internalFile != null && fileContent != null) {
                fileOutputStream = new FileOutputStream(internalFile);

                fileOutputStream.write(fileContent);

                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return internalFile;
    }

    public Bitmap load(String fileName) {
        Log.getLogger().verbose(LOG_TAG, ">load; "+ fileName);

        FileInputStream inputStream = null;
        try {
            File file = createFile(fileName);
            if( file != null) {
                inputStream = new FileInputStream(file);
                //Log.getLogger().verbose(LOG_TAG, "InputStream; "+inputStream.available());
                return BitmapFactory.decodeStream(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void deleteFile(String fileName) {
        Log.getLogger().verbose(LOG_TAG, ">delete; "+ fileName);

        if( m_context != null) {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File fileTodelete = new File(directory, fileName);
            fileTodelete.delete();
        }
    }

    public List<String> listInternalFiles() {
        Log.getLogger().verbose(LOG_TAG, ">listInternalFiles");
        List<String> internalFilesName = new ArrayList<>();
        if( m_context != null) {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File[] internalFiles = directory.listFiles();

            for (File intFile : internalFiles) {
                internalFilesName.add(intFile.getName());
            }
        }

        return internalFilesName;
    }

    // Take care with this method because it will delete ALL files into DOWNLOAD directory
//    public void deleteAllFiles() {
//        Log.getLogger().verbose(LOG_TAG, ">deleteAllFiles");
//
//        if( m_context != null) {
//            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//            for (File fileTodelete : directory.listFiles()) {
//                fileTodelete.delete();
//            }
//        }
//    }

    public File findFileStartingBy(String fileName) {
        Log.getLogger().verbose(LOG_TAG, ">findFileStartingBy");

        if( m_context != null) {
            File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File rainbowCacheDirectory = new File(downloadDirectory.getPath()+"/rainbow");
            if( !rainbowCacheDirectory.exists())
                rainbowCacheDirectory.mkdirs();
            File[] internalFiles = rainbowCacheDirectory.listFiles();

            if(internalFiles != null)
            {
                for (File curFile : internalFiles)
                {
                    if (curFile.getName().startsWith(fileName))
                    {
                        return curFile;
                    }
                }
            }
        }
        return null;
    }
}

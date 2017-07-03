package com.ale.infra.contact;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.ale.rainbow.BitmapConverter;
import com.ale.util.log.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by georges on 27/06/16.
 */
public class ContactCacheFileMgr {

    private static final String LOG_TAG = "ContactCacheFileMgr";

    private final IBitmapConverter m_bitmapConverter;
    private Context m_context;
    private String m_directoryName = "images";

    public ContactCacheFileMgr(Context context)
    {
        m_context = context;
        m_bitmapConverter = new BitmapConverter();
    }

    public void initContext(Context context) {
        m_context = context;
    }

    private File createFile(String fileName) {
        File directory = m_context.getDir(m_directoryName, Context.MODE_PRIVATE);
        return new File(directory, fileName);
    }

    public void save(String fileName,Bitmap bitmapImage) {
        Log.getLogger().verbose(LOG_TAG, ">save; "+fileName);

        FileOutputStream fileOutputStream = null;
        try {
            File internalFile = createFile(fileName);
            fileOutputStream = new FileOutputStream(internalFile);
//            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

            byte[] byteArrayFromBitmap = m_bitmapConverter.createByteArrayFromBitmap(bitmapImage);
            fileOutputStream.write(byteArrayFromBitmap);

            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap load(String fileName) {
        Log.getLogger().verbose(LOG_TAG, ">load; "+ fileName);

        FileInputStream inputStream = null;
        try {
            File file = createFile(fileName);
            inputStream = new FileInputStream(file);
            //Log.getLogger().verbose(LOG_TAG, "InputStream; "+inputStream.available());
            return BitmapFactory.decodeStream(inputStream);
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
            File directory = m_context.getDir(m_directoryName, Context.MODE_PRIVATE);
            File fileTodelete = new File(directory, fileName);
            fileTodelete.delete();
        }
    }

    public List<String> listInternalFiles() {
        Log.getLogger().verbose(LOG_TAG, ">listInternalFiles");
        List<String> internalFilesName = new ArrayList<>();
        if( m_context != null) {
            File directory = m_context.getDir(m_directoryName, Context.MODE_PRIVATE);
            File[] internalFiles = directory.listFiles();

            for (File intFile : internalFiles) {
                internalFilesName.add(intFile.getName());
            }
        }

        return internalFilesName;
    }

    public void deleteAllFiles() {
        Log.getLogger().verbose(LOG_TAG, ">listInternalFiles");

        if( m_context != null) {
            File directory = m_context.getDir(m_directoryName, Context.MODE_PRIVATE);
            for (File fileTodelete : directory.listFiles()) {
                fileTodelete.delete();
            }
        }
    }

    public File findFileStartingBy(String fileName) {
        Log.getLogger().verbose(LOG_TAG, ">listInternalFiles");

        if( m_context != null) {
            File directory = m_context.getDir(m_directoryName, Context.MODE_PRIVATE);
            File[] internalFiles = directory.listFiles();

            List<String> internalFilesName = new ArrayList<>();
            for (File curFile : internalFiles) {
                if (curFile.getName().startsWith(fileName)) {
                    return curFile;
                }
            }
        }
        return null;
    }
}

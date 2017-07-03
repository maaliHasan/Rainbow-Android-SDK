package com.ale.infra.proxy.avatar;

import android.graphics.Bitmap;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.rainbow.BitmapConverter;
import com.ale.util.log.Log;


/**
 * Created by wilsius on 25/05/16.
 */
public class GetAvatarResponse extends RestResponse
{
    private static final String LOG_TAG = "GetTextResponse";

    Bitmap bitmap;
    String lastModified;
    String dimscached;
    String date;
    String contentType;

    public GetAvatarResponse(byte[] result) throws Exception
    {
        BitmapConverter converter = new BitmapConverter();

        bitmap = converter.createBitmapFromByteArray(result);

        if( bitmap == null) {
            Log.getLogger().warn(LOG_TAG, "Bitmap decoding return NULL");
        }
    }

    public GetAvatarResponse(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

//    public String getLastModified() {
//        return lastModified;
//    }
//
//    public String getDimscached() {
//        return dimscached;
//    }
//
//    public String getContentType() {
//        return contentType;
//    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

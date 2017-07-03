package com.ale.infra.http;

import com.ale.infra.proxy.framework.RestResponse;
import com.ale.util.StringsUtil;


/**
 * Created by wilsius on 25/05/16.
 */
public class GetFileResponse extends RestResponse
{
    private static final String LOG_TAG = "GetFileResponse";

    private byte[] m_fileContent;
    private String contentRange;

    public GetFileResponse(byte[] result) throws Exception
    {
        m_fileContent = result;
    }

    public byte[] getFileContent() {
        return m_fileContent;
    }

    public void setContentRange(String contentRange) {
        this.contentRange = contentRange;
    }

    public boolean isFileFullyDownloaded() {
        if( !StringsUtil.isNullOrEmpty(this.contentRange) ) {
            String[] contentSplitted = this.contentRange.split("-|/");
            if( contentSplitted.length >= 3) {
                //int start = Integer.valueOf(contentSplitted[0]);
                int end = Integer.valueOf(contentSplitted[1]);
                int fileSize = Integer.valueOf(contentSplitted[2]);

                return (end >= fileSize);
            }
        }

        return false;
    }

    public int getPercentDownloaded() {
        if( !StringsUtil.isNullOrEmpty(this.contentRange) ) {
            String[] contentSplitted = this.contentRange.split("-|/");
            if( contentSplitted.length >= 3) {
                //int start = Integer.valueOf(contentSplitted[0]);
                int end = Integer.valueOf(contentSplitted[1]);
                int fileSize = Integer.valueOf(contentSplitted[2]);

                int percent = Math.min(end*100 / fileSize,100);
                return percent;
            }
        }

        return 0;
    }
}

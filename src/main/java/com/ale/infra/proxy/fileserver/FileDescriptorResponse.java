package com.ale.infra.proxy.fileserver;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.manager.fileserver.RainbowFileDescriptor;
import com.ale.infra.manager.fileserver.RainbowFileViewer;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.TimeZone;


public class FileDescriptorResponse extends RestResponse {
    private static final String LOG_TAG = "FileDescriptorResponse";
    

    private RainbowFileDescriptor m_fileDescriptor;

    public FileDescriptorResponse(String data) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, ">FileDescriptorResponse; "+data);

        m_fileDescriptor = new RainbowFileDescriptor();

        JSONObject obj = new JSONObject(data);
        JSONObject dataObj = obj.optJSONObject(FILE_DESCRIPTOR_DATA);
        if( dataObj == null) {
            dataObj = obj;
        }
        if ( dataObj != null) {
            if (dataObj.has(FILE_DESCRIPTOR_ID))
                m_fileDescriptor.setId(dataObj.optString(FILE_DESCRIPTOR_ID));

            if (dataObj.has(FILE_DESCRIPTOR_FILENAME))
                m_fileDescriptor.setFileName(dataObj.optString(FILE_DESCRIPTOR_FILENAME));

            if (dataObj.has(FILE_DESCRIPTOR_EXTENSION))
                m_fileDescriptor.setExtension(dataObj.optString(FILE_DESCRIPTOR_EXTENSION));

            if (dataObj.has(FILE_DESCRIPTOR_OWNERID))
                m_fileDescriptor.setOwnerId(dataObj.optString(FILE_DESCRIPTOR_OWNERID));

            if (dataObj.has(FILE_DESCRIPTOR_TYPEMIME))
                m_fileDescriptor.setTypeMIME(dataObj.optString(FILE_DESCRIPTOR_TYPEMIME));

            if (dataObj.has(FILE_DESCRIPTOR_SIZE))
                m_fileDescriptor.setSize((long) dataObj.optInt(FILE_DESCRIPTOR_SIZE));

            if (dataObj.has(FILE_DESCRIPTOR_ISUPLOADED))
                m_fileDescriptor.setIsUploaded(dataObj.optBoolean(FILE_DESCRIPTOR_ISUPLOADED));

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            df.setTimeZone(TimeZone.getTimeZone("GMT"));

            if (dataObj.has(FILE_DESCRIPTOR_REGISTRATIONDATE)) {
                String dateString = dataObj.optString(FILE_DESCRIPTOR_REGISTRATIONDATE);
                if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(dateString))
                    m_fileDescriptor.setRegistrationDate(df.parse(dateString));
            }

            if (dataObj.has(FILE_DESCRIPTOR_UPLOADEDDATE)) {
                String dateString = dataObj.optString(FILE_DESCRIPTOR_UPLOADEDDATE);
                if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(dateString))
                    m_fileDescriptor.setUploadedDate(df.parse(dateString));
            }

            if (dataObj.has(FILE_DESCRIPTOR_DATE_SORT)) {
                String dateString = dataObj.optString(FILE_DESCRIPTOR_DATE_SORT);
                if (!StringsUtil.isNullOrEmptyOrSpacesOrEqualsNullString(dateString))
                    m_fileDescriptor.setUploadedDate(df.parse(dateString));
            }

            if (dataObj.has(FILE_DESCRIPTOR_VIEWERS)) {
                JSONArray viewers = dataObj.getJSONArray(FILE_DESCRIPTOR_VIEWERS);
                for (int i= 0; i < viewers.length() ; i++){
                    RainbowFileViewer viewer = parseViewer((JSONObject) viewers.get(i));
                    m_fileDescriptor.addViewer(viewer);
                }
            }
        }
    }

    private RainbowFileViewer parseViewer(JSONObject jsonObject) throws Exception {
        RainbowFileViewer viewer = new RainbowFileViewer();

        if (jsonObject.has(FILE_DESCRIPTOR_VIEWER_ID))
            viewer.setId(jsonObject.getString(FILE_DESCRIPTOR_VIEWER_ID));

        if (jsonObject.has(FILE_DESCRIPTOR_VIEWER_TYPE))
            viewer.setType(jsonObject.getString(FILE_DESCRIPTOR_VIEWER_TYPE));

        return viewer;
    }


    public RainbowFileDescriptor getFileDescriptor() {
        return m_fileDescriptor;
    }
}

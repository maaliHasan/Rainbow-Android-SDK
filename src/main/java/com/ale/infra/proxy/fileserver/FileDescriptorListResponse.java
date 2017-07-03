package com.ale.infra.proxy.fileserver;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.manager.fileserver.RainbowFileDescriptor;
import com.ale.infra.proxy.framework.RestResponse;
import com.ale.util.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class FileDescriptorListResponse extends RestResponse {
    private static final String LOG_TAG = "FileDescriptorListResponse";


    private List<RainbowFileDescriptor> m_fileDescrList;

    public FileDescriptorListResponse(String data) throws Exception {
        if (RainbowContext.getPlatformServices().getApplicationData().isPrivateLogEnable())
            Log.getLogger().verbose(LOG_TAG, ">FileDescriptorResponse; "+data);

        m_fileDescrList = new ArrayList<>();

        JSONObject obj = new JSONObject(data);
        JSONArray fileDescList = obj.getJSONArray(FILE_DESCRIPTOR_DATA);
        if ( fileDescList != null) {
            for (int i = 0; i < fileDescList.length(); i++) {
                FileDescriptorResponse fileDescResp = new FileDescriptorResponse(fileDescList.get(i).toString());

                m_fileDescrList.add(fileDescResp.getFileDescriptor());
            }
        }
    }

    public List<RainbowFileDescriptor> getFileDescriptorList() {
        return m_fileDescrList;
    }
}

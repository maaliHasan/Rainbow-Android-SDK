package com.ale.infra.contact;

import android.net.Uri;

import java.util.List;

/**
 * Created by grobert on 18/05/16.
 */
public class LocalContact extends AbstractContact {

    private String lookupKey;



    private Uri     thumbnailUri;

    @Override
    public void setCorporateId(String companyId) {
    }

    @Override
    public List<IContact.ContactRole> getRole() {
        return null;
    }

    @Override
    public String getCorporateId() {
        return null;
    }

    public String getLookupKey() {
        return lookupKey;
    }

    public void setLookupKey(String lookupKey) {
        this.lookupKey = lookupKey;
    }

    @Override
    public String getId() {
        return lookupKey;
    }


    @Override
    public boolean isNative() {
        return true;
    }

    public Uri getThumbnailUri() {
        return thumbnailUri;
    }

    public void setThumbnailUri(Uri thumbnailUri) {
        this.thumbnailUri = thumbnailUri;
    }
}

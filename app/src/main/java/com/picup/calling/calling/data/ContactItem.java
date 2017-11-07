package com.picup.calling.data;

import android.net.Uri;

import java.util.List;

/**
 * Created by frank.truong on 12/14/2016.
 */

public class ContactItem {
    private Uri thumbnailUri = null;
    private String fullName = null;
    private List<Phone> phoneTypes = null;

    public ContactItem(Uri thumbnailUri, String fullName, List<Phone> phoneTypes) {
        this.thumbnailUri = thumbnailUri;
        this.fullName = fullName;
        this.phoneTypes = phoneTypes;
    }

    public Uri getThumbnailUri() {
        return thumbnailUri;
    }

    public void setThumbnailUri(Uri thumbnailUri) {
        this.thumbnailUri = thumbnailUri;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<Phone> getPhoneTypes() {
        return phoneTypes;
    }

    public void setPhoneTypes(List<Phone> phoneTypes) {
        this.phoneTypes = phoneTypes;
    }

    @Override
    public String toString() {
        return thumbnailUri + "," + fullName + "," + phoneTypes;
    }
}

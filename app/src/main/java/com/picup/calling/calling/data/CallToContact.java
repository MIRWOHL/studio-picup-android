package com.picup.calling.data;

import android.net.Uri;

import java.util.List;

/**
 * Created by frank.truong on 12/13/2016.
 */

public class CallToContact {
    private Uri thumbnailUri = null;
    private String fullName = null;
    private List<Phone> phoneTypes = null;

    public CallToContact(Uri thumbnailUri, String fullName, List<Phone> phoneTypes) {
        this.thumbnailUri = thumbnailUri;
        this.fullName = fullName;
        this.phoneTypes = phoneTypes;
    }

    public void setThumbnailUri(Uri thumbnailUri) {
        this.thumbnailUri = thumbnailUri;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPhoneTypes(List<Phone> phoneTypes) {
        this.phoneTypes = phoneTypes;
    }

    public Uri getThumbnailUri() {
        return thumbnailUri;
    }

    public String getFullName() {
        return fullName;
    }

    public List<Phone> getPhoneTypes() {
        return phoneTypes;
    }

    @Override
    public String toString() {
        return thumbnailUri + "," + fullName + "," + phoneTypes;
    }
}

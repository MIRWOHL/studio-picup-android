package com.picup.calling.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by frank.truong on 12/28/2016.
 */

public class Contact implements Parcelable {
    private long id = 0;
    private String photoUriString = null;
    private String fullName = null;
    private List<Phone> phones = new ArrayList<>();
    private List<Email> emails = new ArrayList<>();

    public Contact() {

    }
   public Contact(long id, String photoUriString, String fullName, List<Phone> phones) {
        this(id, photoUriString, fullName, phones, null);
    }

    public Contact(long id, String photoUriString, String fullName, List<Phone> phones, List<Email> emails) {
        this.id = id;
        this.photoUriString = photoUriString;
        this.fullName = fullName;
        this.phones = phones;
        this.emails = emails;
    }

    public Contact(Parcel in) {
        id = in.readLong();
        photoUriString = in.readString();
        fullName = in.readString();
        in.readList(phones, null);
        in.readList(emails, null);
/*
        Phone[] phoneArray = (Phone[])in.readParcelableArray(in.getClass().getClassLoader());
        if (phoneArray != null && phoneArray.length > ic_key_0) {
            phones = new ArrayList<>(phoneArray.length);
            for (Phone phone : phoneArray) {
                phones.add(phone);
            }
        }
        Email[] emailArray = (Email[])in.readParcelableArray(in.getClass().getClassLoader());
        if (emailArray != null && emailArray.length > ic_key_0) {
            emails = new ArrayList<>(emailArray.length);
            for (Email address : emailArray) {
                emails.add(address);
            }
        }
*/
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhotoUriString() {
        return photoUriString;
    }

    public void setPhotoUriString(String photoUriString) {
        this.photoUriString = photoUriString;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
    }

    public List<Email> getEmails() {
        return emails;
    }

    public void setEmailTypes(List<Email> emails) {
        this.emails = emails;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(photoUriString);
        dest.writeString(fullName);
        if (phones != null && !phones.isEmpty()) {
            dest.writeList(phones);
        }
        if (emails != null && !emails.isEmpty()) {
            dest.writeList(emails);
        }
/*
        if (phones != null && !phones.isEmpty()) {
            Phone[] phoneArray = phones.toArray(new Phone[ic_key_0]);
            dest.writeParcelableArray(phoneArray, ic_key_0);
        }
        if (emails != null && !emails.isEmpty()) {
            Email[] emailArray = emails.toArray(new Email[ic_key_0]);
            dest.writeParcelableArray(emailArray, ic_key_0);
        }
*/
    }

    public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel source) {
            return new Contact(source);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

}

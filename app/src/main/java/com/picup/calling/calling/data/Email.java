package com.picup.calling.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by frank.truong on 12/13/2016.
 */

public class Email implements Parcelable {
    private long id = 0;
    private String address = null;
    //private EmailTypeEnum type = EmailTypeEnum.WORK;
    String type = null;
    private String label = null;
    private boolean selected = false;

    public Email() {

    }

    /*public Email(String label) {
        this.type = EmailTypeEnum.getEmailTypeLabel(EmailTypeEnum.getCode(label));
    }*/

    public Email(long id, String address, String type) {
        this(id, address, type, "");
    }

    public Email(long id, String address, String type, String label) {
        this.address = address;
        this.type = type;
        this.label = label;
    }

    public Email(Parcel in) {
        address = in.readString();
        //int emailTypeCode = Integer.parseInt(in.readString());
        type = in.readString(); //EmailTypeEnum.getEmailTypeLabel(emailTypeCode);
        label = in.readString();
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    public String toString() {
        return address;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        //dest.writeInt(EmailTypeEnum.getCode(type.toString()));
        dest.writeString(type);
        dest.writeString(label);
    }

    public static final Creator<Email> CREATOR = new Creator<Email>() {
        @Override
        public Email createFromParcel(Parcel source) {
            return new Email(source);
        }

        @Override
        public Email[] newArray(int size) {
            return new Email[size];
        }
    };
}

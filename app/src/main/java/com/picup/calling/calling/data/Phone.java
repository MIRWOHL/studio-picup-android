package com.picup.calling.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by frank.truong on 12/13/2016.
 */

public final class Phone implements Parcelable {
    private long id = 0;
    private String number = null;
    //private PhoneTypeEnum type = PhoneTypeEnum.MOBILE;
    private String type;
    private String customLabel;
    private boolean isPrimary = false;
    private boolean preferenceDetermined = false;
    private boolean selected = false;
    private boolean ignoreSelectionForCalling = true;
    private String normalizeNumber;

    public Phone() {
    }

    public Phone(long id, String number, String type) {
        this(id, number, type, false, "");
    }

    public Phone(long id, String number, String type, boolean isPrimary) {this(id, number, type, false, "");}

    public Phone(long id, String number, String type, boolean isPrimary, String customLabel) {
        this.id = id;
        this.number = number;
        this.type = type;
        this.customLabel = customLabel;
        this.isPrimary = isPrimary;
    }

    public Phone(Parcel in) {
        id = in.readLong();
        number = in.readString();
        //int phoneTypeCode = Integer.parseInt(in.readString());
        type = in.readString();
        customLabel = in.readString();
        isPrimary = (in.readInt() == 1 ? true : false);
        preferenceDetermined = (in.readInt() == 1 ? true : false);
        selected = (in.readInt() == 1 ? true : false);
        ignoreSelectionForCalling = (in.readInt() == 1 ? true : false);
        normalizeNumber= in.readString();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setNormalizeNumber(String normalizeNumber) {
        this.normalizeNumber = normalizeNumber;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCustomLabel(String customLabel) {
        this.customLabel = customLabel;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
        preferenceDetermined = true;
    }

    public String getNumber() {
        return number;
    }

    public String getNormalizeNumber() {
        return normalizeNumber;
    }

    public String getType() {
        return type;
    }

    public String getCustomLabel() {
        return customLabel;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public boolean isPreferenceDetermined() {
        return preferenceDetermined;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setIgnoreSelectionForCalling(boolean ignored) {
        this.ignoreSelectionForCalling = ignored;
    }

    public boolean isIgnoreSelectionForCalling() {
        return ignoreSelectionForCalling;
    }

    @Override
    public String toString() {
        return type.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(number);
        dest.writeString(type);
        dest.writeString(customLabel);
        dest.writeInt(isPrimary ? 1 : 0);
        dest.writeInt(preferenceDetermined ? 1 : 0);
        dest.writeInt(selected ? 1 : 0);
        dest.writeInt(ignoreSelectionForCalling ? 1 : 0);
        dest.writeString(normalizeNumber);
    }

    public static final Parcelable.Creator<Phone> CREATOR = new Parcelable.Creator<Phone>() {
        @Override
        public Phone createFromParcel(Parcel source) {
            return new Phone(source);
        }

        @Override
        public Phone[] newArray(int size) {
            return new Phone[size];
        }
    };
}

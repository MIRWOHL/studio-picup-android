package com.picup.calling.data;

/**
 * Created by frank.truong on 2/23/2017.
 */

public class Phone4Keypad {
    private String fullName = null;
    private Phone phone = null;
    private int count = 0;

    public Phone4Keypad(String fullName, Phone phone, int count) {
        this.fullName = fullName;
        this.phone = phone;
        this.count = count;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return fullName;
    }

}

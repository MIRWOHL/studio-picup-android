package com.picup.calling.base;

import android.provider.ContactsContract;

/**
 * Created by frank.truong on 12/14/2016.
 */

@Deprecated
public enum PhoneTypeEnum {
    CUSTOM(ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM, "Custom"),
    HOME(1, "Home"),
    MOBILE(2, "Mobile"),
    OFFICE(3, "Work"),
    FAX_WORK(4, "Fax-Work"),
    FAX_HOME(5,"Fax-Home"),
    PAGER(6,"Pager"),
    OTHER(7,"Other"),
    CALLBACK(8,"Callback"),
    CAR(9,"Car"),
    COMPANY(10,"Company"),
    ISDN(11, "ISDN"),
    MAIN(12,"Main"),
    FAX_OTHER(13,"Fax-Other"),
    RADIO(14,"Radio"),
    TELEX(15,"Telex"),
    TTY(16,"TTY-TDD"),
    MOBILE_WORK(17,"Mobile-Work"),
    PAGER_WORK(18,"Pager-Work"),
    ASSISTANT(19,"Assistant"),
    MMS(20,"MMS");

    private final int code;
    private final String name;

    PhoneTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static PhoneTypeEnum getPhoneTypeLabel(int code) {
        switch (code) {
            case 2:
                return MOBILE;
            case 1:
                return HOME;
            case 3:
                return OFFICE;
            case 4:
                return FAX_WORK;
            default:
                return MOBILE;
        }
    }

    public static int getCode(String label) {
        switch (label) {
            case "Mobile":
                return 2;
            case "Home":
                return 1;
            case "Office":
                return 3;
            case "Fax":
                return 4;
            default:
                return 2;
        }
    }

    public static int getIndexByLabel(String label) {
        switch (label) {
            case "Mobile":
                return 0;
            case "Home":
                return 1;
            case "Office":
                return 2;
            case "Fax":
                return 3;
            default:
                return 0;
        }
    }

    @Override
    public String toString() {
        switch (code) {
            case 2:
                return "Mobile";
            case 1:
                return "Home";
            case 3:
                return "Office";
            case 4:
                return "Fax";
            default:
                return "Mobile";
        }
    }
}

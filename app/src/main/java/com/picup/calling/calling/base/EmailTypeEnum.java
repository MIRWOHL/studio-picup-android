package com.picup.calling.base;

/**
 * Created by frank.truong on 12/14/2016.
 */

public enum EmailTypeEnum {
    PERSONAL(1, "Personal"),
    WORK(2, "Work"),
    OTHER(3, "Other");

    private final int code;
    private final String name;

    EmailTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static EmailTypeEnum getEmailTypeLabel(int code) {
        switch (code) {
            case 1:
                return PERSONAL;
            case 2:
                return WORK;
            case 3:
                return OTHER;
            default:
                return PERSONAL;
        }
    }

    public static int getCode(String label) {
        switch (label) {
            case "Personal":
                return 1;
            case "Work":
                return 2;
            case "Other":
                return 3;
            default:
                return 1;
        }
    }

    public static int getIndexByLabel(String label) {
        switch (label) {
            case "Personal":
                return 0;
            case "Work":
                return 1;
            case "Other":
                return 2;
            default:
                return 0;
        }
    }

    @Override
    public String toString() {
        switch (code) {
            case 1:
                return "Personal";
            case 2:
                return "Work";
            case 3:
                return "Other";
            default:
                return "Personal";
        }
    }
}

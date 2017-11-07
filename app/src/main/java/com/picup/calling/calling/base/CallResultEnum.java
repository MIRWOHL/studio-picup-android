package com.picup.calling.base;

/**
 * Created by frank.truong on 12/14/2016.
 */

public enum CallResultEnum {
    VOICEMAIL(0, "Voice Mail"),
    IN(1, "Incoming"),
    OUT(2, "Outgoing"),
    UNANSWERED(3, "Un-answered"),
    UNKNOWN(4, "Unknown");

    private final int code;
    private final String label;

    CallResultEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CallResultEnum getCallResultByCode(int code) {
        switch (code) {
            case 0:
                return VOICEMAIL;
            case 1:
                return IN;
            case 2:
                return OUT;
            case 3:
                return UNANSWERED;
            default:
                return UNKNOWN;
        }
    }

    public static int getCode(String label) {
        switch (label) {
            case "Voice Mail":
                return 0;
            case "Incoming":
                return 1;
            case "Outgoing":
                return 2;
            case "Un-answered":
                return 3;
            default:
                return 4;
        }
    }

    @Override
    public String toString() {
        switch (code) {
            case 0:
                return "Voice Mail";
            case 1:
                return "Incoming";
            case 2:
                return "Outgoing";
            case 3:
                return "Unanswered";
            default:
                return "Unknown";
        }
    }
}

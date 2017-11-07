package com.picup.calling.network;

/**
 * Created by frank.truong on 4/24/2017.
 */

public class CallAccessForm {
    private String ani = null;
    private String callerIdNum = null;
    private String carrier = null;
    private String dialedNum = null;
    private String picupNum = null;
    private String isoCC = null;
    private int mcc = 0;
    private int mnc = 0;
    private int userId = 0;
    private int tanType = 1;

    public String getAni() {
        return ani;
    }

    public void setAni(String ani) {
        this.ani = ani;
    }

    public String getCallerIdNum() {
        return callerIdNum;
    }

    public void setCallerIdNum(String callerIdNum) {
        this.callerIdNum = callerIdNum;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getDialedNum() {
        return dialedNum;
    }

    public void setDialedNum(String dialedNum) {
        this.dialedNum = dialedNum;
    }

    public String getPicupNumber() {
        return picupNum;
    }

    public void setPicupNum(String picupNum) {
        this.picupNum = picupNum;
    }

    public String getIsoCC() {
        return isoCC;
    }

    public void setIsoCC(String isoCC) {
        this.isoCC = isoCC;
    }

    public int getMcc() {
        return mcc;
    }

    public void setMcc(int mcc) {
        this.mcc = mcc;
    }

    public int getMnc() {
        return mnc;
    }

    public void setMnc(int mnc) {
        this.mnc = mnc;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTanType() {
        return tanType;
    }

    public void setTanType(int tanType) {
        this.tanType = tanType;
    }
}

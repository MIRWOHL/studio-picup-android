package com.picup.calling.network;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

/**
 * Created by frank.truong on 3/29/2017.
 */

public class Cdr implements Comparable<Cdr> {
    private String dnis = null;
    private String deptName = null;
    @SerializedName("result")
    private String callState = null;
    private String accountId = null;
    private String ani = null;
    private int eocReason = 0;
    private int rowId = 0;
    private boolean aniBlocked = false;
    @SerializedName("callDate")
    private Calendar callCalendar = null;
    private String extension = null;
    private int duration = 0;
    private String createdBy = null;
    @SerializedName("billingAcctId")
    private String billingAccountId = null;
    private String userName = null;
    private String dn = null;
    private String origdn = null;
    private String ctype = null;
    private String privacyIndicator = null;
    @SerializedName("createdDt")
    private Calendar createdCalendar = null;

    public static int CDR_INBOUND = 1;
    public static int CDR_OUTBOUND = 2;

    public String getDnis() {
        return dnis;
    }

    public void setDnis(String dnis) {
        this.dnis = dnis;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getCallState() {
        return callState;
    }

    public void setCallState(String callState) {
        this.callState = callState;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAni() {
        return ani;
    }

    public void setAni(String ani) {
        this.ani = ani;
    }

    public int getEocReason() {
        return eocReason;
    }

    public void setEocReason(int eocReason) {
        this.eocReason = eocReason;
    }

    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    public boolean isAniBlocked() {
        return aniBlocked;
    }

    public void setAniBlocked(boolean aniBlocked) {
        this.aniBlocked = aniBlocked;
    }

    public Calendar getCallCalendar() {
        return callCalendar;
    }

    public void setCallCalendar(Calendar callCalendar) {
        this.callCalendar = callCalendar;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getBillingAccountId() {
        return billingAccountId;
    }

    public void setBillingAccountId(String billingAccountId) {
        this.billingAccountId = billingAccountId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public String getOrigdn() {
        return origdn;
    }

    public void setOrigdn(String origdn) {
        this.origdn = origdn;
    }

    public String getCtype() { return ctype; }

    public void setCtype(String ctype) { this.ctype = ctype; }

    public String getPrivacyIndicator() {
        return privacyIndicator;
    }

    public void setPrivacyIndicator(String privacyIndicator) {
        this.privacyIndicator = privacyIndicator;
    }

    public Calendar getCreatedCalendar() {
        return createdCalendar;
    }

    public void setCreatedCalendar(Calendar createdCalendar) {
        this.createdCalendar = createdCalendar;
    }

    @Override
    public int compareTo(@NonNull Cdr other) {
        if (callCalendar != null && other.getCallCalendar() != null) {
            if (callCalendar.after(other.getCallCalendar())) {
                return -1;
            } else if (callCalendar.before(other.getCallCalendar())) {
                return 1;
            }
        }
        return 0;
    }

    public int getDirection() {

        int direction = Cdr.CDR_INBOUND; //default inbound...
        //outbound = 1,2
        //inbound = 3,4
        if (getCtype().equals("1") || getCtype().equals("2"))
            direction = Cdr.CDR_OUTBOUND;

        return direction;
    }

/*    public int getDirection(LineNumbers lineNumbers) {
        int dir = CDR_INBOUND; //default inbound

        if (lineNumbers == null) {
            Logger.log("Cdr - getDirection - missing lineNumbers");
            return dir;
        }

        for (String lineNumber : lineNumbers.getNumbers()) {
            //Logger.log("Cdr - getDirection - lineNumber:"+lineNumber+" ani:"+getAni()+" dnis:"+getDnis()+" dn:"+getDn());
            if (TextUtils.equals(getAni(), lineNumber)) {
                dir = CDR_OUTBOUND;
                break;
            } else if(TextUtils.equals(getDnis(), lineNumber)) {  //may be origDn
                dir = CDR_INBOUND;
                break;
            }
        }
        //Logger.log("Cdr - getDirection - dir:"+dir);
        return dir;
    }
*/

}

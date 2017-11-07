package com.picup.calling.network;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

/**
 * Created by frank.truong on 3/22/2017.
 */

public class AuthenticateInfo {
    private static final String TAG = AuthenticateInfo.class.getSimpleName();

    /* Sample:
    05-07 23:41:18.170 19468-23454/android.idt.net.com.picup.calling D/OkHttp: {"tn":null,"accountId":8068,"next":0,"businessClass":"US",
    "departments":[
      {"destinations":[
        {"accountId":8068,"destinationData":[
          {"posAck":true,"displayAlias":"Miriam Picup","orderBy":"1","status":"A","dataId":33568,"destinationId":17900,"dataStatus":"A","data":"9474","rings":4,"type":"user","displayName":"DestData Billing dept acct 0"}
        ],"status":"A","ownerId":5557,"businessClass":"DP","destinationId":17900,"updatedTS":1493751093000,"displayName":"Billing","createdDt":1493736693000}
       ],"accountId":8068,"status":"A","intName":"dp732588548","emailAddress":"miriampicup@gmail.com","dialingType":"sim","vmPolicyName":"DefaultPolicy","callGreeting":true,"extension":"301","timezone":"EST","screenCall":false,"lineCallerId":false,"rings":4,"updatedTS":36000000,"departmentId":5557,"displayName":"Billing","createdDt":1493736692000}
      ],
    "tokenInfo":{"token":"72eacdff-97c9-436f-be2a-844b365a4293","expiryInSeconds":42976,"refresh_token":"b4b66e3e-1a5d-4a62-a9f8-1cace1a5b25e","expiry":1494257851759},
    "vmPolicyName":"DefaultPolicy","callGreeting":true,"timezone":"EST","screenCall":false,"userId":9474,"role":"admin","rings":4,"firstName":"Miriam","portalLoginFlg":false,"lastName":"Picup","intName":"us173744387","status":"A","emailAddress":"miriampicup@gmail.com","dialingType":"sim","extension":201,"phoneNo":"","callerId":null,"lineCallerId":false,"updatedTS":1493751091000,"createdDt":1493735251000}
     */

    private String tn = null;
    private String lastName = null;
    private int accountId = 0;
    private String status = null;
    private String intName = null;
    private String businessClass = null;
    private int next = 0;
    private String emailAddress = null;
    @SerializedName("tokenInfo")
    private Token token = null;
    private String vmPolicyName = null;
    private String dialingType = null;
    private boolean callGreeting = true;
    @SerializedName("phoneNo")
    private String phoneNumber = null;
    private int extension = 0;
    @SerializedName("timezone")
    private String timeZone = null;
    private String callerId = null;
    private boolean screenCall = false;
    private int userId = 0;
    private boolean lineCallerId = false;
    private String role = null;
    @SerializedName("rings")
    private int ringCount = 0;
    @SerializedName("updatedTS")
    private Calendar updatedTimestamp = null;
    @SerializedName("portalLoginFlg")
    private boolean portalLoginFlag = false;
    private String firstName = null;
    @SerializedName("createdDt")
    private Calendar createdCalendar = null;

    public String getTn() {
        return tn;
    }

    public void setTn(String tn) {
        this.tn = tn;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIntName() {
        return intName;
    }

    public void setIntName(String intName) {
        this.intName = intName;
    }

    public String getBusinessClass() {
        return businessClass;
    }

    public void setBusinessClass(String businessClass) {
        this.businessClass = businessClass;
    }

    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    // No token refreshing attempted
    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public String getVmPolicyName() {
        return vmPolicyName;
    }

    public void setVmPolicyName(String vmPolicyName) {
        this.vmPolicyName = vmPolicyName;
    }

    public String getDialingType() {
        return dialingType;
    }

    public void setDialingType(String dialingType) {
        this.dialingType = dialingType;
    }

    public boolean isCallGreeting() {
        return callGreeting;
    }

    public void setCallGreeting(boolean callGreeting) {
        this.callGreeting = callGreeting;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getExtension() {
        return extension;
    }

    public void setExtension(int extension) {
        this.extension = extension;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getCallerId() {
        return callerId;
    }

    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }

    public boolean isScreenCall() {
        return screenCall;
    }

    public void setScreenCall(boolean screenCall) {
        this.screenCall = screenCall;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isLineCallerId() {
        return lineCallerId;
    }

    public void setLineCallerId(boolean lineCallerId) {
        this.lineCallerId = lineCallerId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getRingCount() {
        return ringCount;
    }

    public void setRingCount(int ringCount) {
        this.ringCount = ringCount;
    }

    public Calendar getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(Calendar updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    public boolean isPortalLoginFlag() {
        return portalLoginFlag;
    }

    public void setPortalLoginFlag(boolean portalLoginFlag) {
        this.portalLoginFlag = portalLoginFlag;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Calendar getCreatedCalendar() {
        return createdCalendar;
    }

    public void setCreatedDate(Calendar createdCalendar) {
        this.createdCalendar = createdCalendar;
    }
}

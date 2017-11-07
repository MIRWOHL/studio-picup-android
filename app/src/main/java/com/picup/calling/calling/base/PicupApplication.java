package com.picup.calling.base;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import com.picup.calling.network.AuthenticateInfo;
import com.picup.calling.network.CallAccessForm;
import com.picup.calling.network.LineNumbers;
import com.picup.calling.network.PicupService;
import com.picup.calling.network.Token;
import com.picup.calling.util.Logger;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.google.common.collect.ImmutableMap;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.IOException;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by frank.truong on 11/23/2016.
 */

public class PicupApplication extends Application {
    private static final String TAG = PicupApplication.class.getSimpleName();
    public static final String appId = "Picup";
    private static String emailAddress = null;
    private static String password = null;
    public static final String apiId = "MobileAPI";
    public static final String baseUrl = "https://picupapp.picup.com/unite/v1/";
    public static final String testUrl = "https://unitesps.net2phone.com/unite/v1/";
    public static AuthenticateInfo authenticateInfo = null;
    public static LineNumbers lineNumbers = null;
    private static PicupService picupService = null;
    private static SharedPreferences prefs = null;
    public static final int pageSize = 100;
    public static CallAccessForm callAccessForm = null;
    public static PhoneNumberUtil phoneNumberUtil = null;
    public static boolean isCrashlyticInitialize = false;
    private static PicupApplication instance = null;
    public static boolean needsRefresh = false;

    public static PicupApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.log("PicupApplication - onCreate");
        picupService = PicupService.retrofit.create(PicupService.class);
        prefs = super.getSharedPreferences(appId, Context.MODE_PRIVATE);

        Fabric.with(this, new Crashlytics());
        isCrashlyticInitialize = true;

        String emailAddress = prefs.getString("emailAddress", null);
        String refreshToken = prefs.getString("refreshToken", null);
        if (!TextUtils.isEmpty(emailAddress) && !TextUtils.isEmpty(refreshToken)) {
            refreshTokenInternally(emailAddress, refreshToken);
        }
        phoneNumberUtil = PhoneNumberUtil.getInstance();

    }

    public static void initCallAccessForm(Context context, TelephonyManager telephonyManager) {
        if (context == null) {
            return;
        }
        if (callAccessForm == null) {
            callAccessForm = new CallAccessForm();
        }
        int userId = -1;
        if (authenticateInfo != null) {
            userId = authenticateInfo.getUserId();
            callAccessForm.setUserId(userId);
        } else {
            Logger.log("PicupApplication - initCallAccessForm - authenticateInfo is null");
        }
        Logger.log("PicupApplication - initCallAccessForm - userId:" + userId);

        if (prefs != null) {
            String ani = prefs.getString("confirmedPhoneNumber","");
            Logger.log("PicupApplication - initCallAccessForm - prefs - ani:"+ani);
            if (TextUtils.isEmpty(ani)) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    try {
                        Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(telephonyManager.getLine1Number(), Locale.getDefault().getCountry());
                        String numberStr = phoneNumber.getCountryCode() + String.valueOf(phoneNumber.getNationalNumber());
                        //per bill no need to strip separtors
                        callAccessForm.setAni(numberStr);
                    } catch (NumberParseException npe) {
                        Logger.log("PicupApplication - initCallAccessForm - Invalid phone number");
                    }
                }
            } else
                callAccessForm.setAni(ani);
            Logger.log("PicupApplication - initCallAccessForm - ani:"+callAccessForm.getAni());
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Logger.log("PicupApplication - initCallAccessForm - NO PERMISSION");
            //PER JEFF, use default values if no permission...until back end can return tan from pool...
            //return;
            callAccessForm.setMcc(310);
            callAccessForm.setMnc(410);
            callAccessForm.setIsoCC("us");
        } else {
            Logger.log("PicupApplication - initCallAccessForm - networkOperatorName:" + telephonyManager.getNetworkOperatorName() + " countryIso:" + telephonyManager.getNetworkCountryIso() +
                    " networkOperator:" + telephonyManager.getNetworkOperator());

            callAccessForm.setCarrier(telephonyManager.getNetworkOperatorName());
            if (TextUtils.isEmpty(telephonyManager.getNetworkCountryIso()))
                callAccessForm.setIsoCC("us");
            else
                callAccessForm.setIsoCC(telephonyManager.getNetworkCountryIso());
            String networkOperator = telephonyManager.getNetworkOperator();
            //PER JEFF, use default values if none available...until back end can return from pool...
            //If you place the call using the phone, then you do use the mcc, mnc of the phone.
            int mcc = 310; //0;
            int mnc = 410; //0;
            Logger.log("PicupApplication - initCallAccessForm - networkOperator:" + networkOperator);
            if (!TextUtils.isEmpty(networkOperator) && networkOperator.length() > 0) {
                mcc = Integer.parseInt(networkOperator.substring(0, 3));
                mnc = Integer.parseInt(networkOperator.substring(3));
            }
            Logger.log("PicupApplication - initCallAccessForm - mcc:" + mcc + " mnc:" + mnc);
            callAccessForm.setMcc(mcc);
            callAccessForm.setMnc(mnc);
        }
        callAccessForm.setTanType(1);
    }

    public static void resetData() {
        prefs.edit().remove("refreshToken").apply();
        authenticateInfo = null;
        lineNumbers = null;
    }

    private static void refreshTokenInternally(String emailAddress, String refreshToken) {
        if (!TextUtils.isEmpty(emailAddress) && !TextUtils.isEmpty(refreshToken)) {
            final ImmutableMap<String, String> authenticateParams = ImmutableMap.of("emailAddress", emailAddress, "refreshToken", refreshToken, "applicationId", apiId);
            final Call<AuthenticateInfo> call = picupService.refresh(authenticateParams);
            call.enqueue(new Callback<AuthenticateInfo>() {
                @Override
                public void onResponse(Call<AuthenticateInfo> call, Response<AuthenticateInfo> response) {
                    if (response.isSuccessful()) {
                        authenticateInfo = response.body();
                        persistRefreshToken();
                    } else {
                        try {
                            Logger.log("PicupApplication - refreshTokenInternally - request refresh - onResponse - failure error:" + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<AuthenticateInfo> call, Throwable t) {
                }
            });
        }
    }

    private static void authenticateTokenInternally(String emailAddress, String password) {
        if (!TextUtils.isEmpty(emailAddress) && !TextUtils.isEmpty(password)) {
            ImmutableMap<String, String> authenticateParams = ImmutableMap.of("emailAddress", emailAddress, "password", password, "applicationId", apiId);
            final Call<AuthenticateInfo> call = picupService.authenticate(authenticateParams);
            call.enqueue(new Callback<AuthenticateInfo>() {
                @Override
                public void onResponse(Call<AuthenticateInfo> call, Response<AuthenticateInfo> response) {
                    authenticateInfo = response.body();
                    persistRefreshToken();
                }

                @Override
                public void onFailure(Call<AuthenticateInfo> call, Throwable t) {

                }
            });
        }
    }

    // Attempt to synchronously refresh token if its time-to-live is less than 5 minutes
    public static String getToken() {
        String tokenId = null;

        if (authenticateInfo == null) {
            authenticateTokenInternally(emailAddress, password);
        } else {
            Token token = authenticateInfo.getToken();
            if (token != null) {
                if (token.isExpired()) {
                    refreshTokenInternally(emailAddress, token.getRefreshId());
                }
            } else {
                authenticateTokenInternally(emailAddress, password);
            }
        }
        if (authenticateInfo != null) {
            Token token = authenticateInfo.getToken();
            if (token != null) {
                tokenId = token.getId();
            }
        }
        if (TextUtils.isEmpty(tokenId))
            return null;

        return "Bearer " + tokenId;
    }

    public static int getAccountId() {
        int accountId = 0;

        if (authenticateInfo == null) {
            authenticateTokenInternally(emailAddress, password);
        }
        if (authenticateInfo != null) {
            accountId = authenticateInfo.getAccountId();
        }
        return accountId;
    }

    public static int getUserId() {
        int userId = 0;

        if (authenticateInfo == null) {
            authenticateTokenInternally(emailAddress, password);
        }
        if (authenticateInfo != null) {
            userId = authenticateInfo.getUserId();
        }
        return userId;
    }

    public static boolean isAdmin() {
        boolean admin = false;
        if (authenticateInfo == null) {
            authenticateTokenInternally(emailAddress, password);
        }
        if (authenticateInfo != null) {
            if (!TextUtils.isEmpty(PicupApplication.authenticateInfo.getRole())
                    && PicupApplication.authenticateInfo.getRole().equalsIgnoreCase("admin"))
                admin = true;
        }
        return admin;
    }

    public static void setEmailAddress(String emailAddress) {
        PicupApplication.emailAddress = emailAddress;
        //prefs.edit().putString("emailAddress", emailAddress).commit();
    }

    public static String getEmailAddress() {
        return PicupApplication.emailAddress;
    }

    public static void setPassword(String password) {
        PicupApplication.password = password;
    }

    private static void persistRefreshToken() {
        if (authenticateInfo != null) {
            Token token = authenticateInfo.getToken();
            if (token != null) {
                String refreshToken = token.getRefreshId();
                if (!TextUtils.isEmpty(refreshToken)) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("refreshToken", refreshToken).commit();
                }
            }
        }
    }


}

package com.picup.calling;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import com.picup.calling.base.PicupApplication;
import com.picup.calling.util.Logger;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.picup.calling.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NumberConfirmActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = NumberConfirmActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_ALL = 1;
    private EditText numberTextView = null;
    private Button approveButton = null;
    private static boolean awaitingForPermissions = false;
    private PhoneNumberUtil phoneNumberUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.number_confirm);
        numberTextView = (EditText) findViewById(R.id.number_textview);
        String number = null;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                number = telephonyManager.getLine1Number();
            }
        }
        if (numberTextView != null) {
            if (!TextUtils.isEmpty(number)) {
                numberTextView.setText(PhoneNumberUtils.formatNumber(number));
            }
            numberTextView.setInputType(InputType.TYPE_CLASS_PHONE|InputType.TYPE_NUMBER_FLAG_SIGNED);
        }

        approveButton = (Button) findViewById(R.id.approve_button);

        try{phoneNumberUtils = PhoneNumberUtil.getInstance();}catch(Throwable t){}
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (approveButton != null) {
            approveButton.setOnClickListener(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (approveButton != null) {
            approveButton.setOnClickListener(null);
        }
        if (isFinishing()) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        String log = "NumberConfirmActivity - onClick";
        if (v == null) {
            log += "  - view is null";
            Logger.log(log);
            return;
        }
        if (v.getId() == approveButton.getId()) {
            SharedPreferences prefs = getSharedPreferences(PicupApplication.appId, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            String numberStr = null;
            if (numberTextView != null) {
                Editable editable = numberTextView.getText();
                if (editable != null) {
                    numberStr = editable.toString();
                }
            }
            if (TextUtils.isEmpty(numberStr)) {
                numberStr = PhoneNumberUtils.formatNumber(numberStr);
            }
            log += " - numberStr:";
            log += numberStr;
            if (TextUtils.isEmpty(numberStr)) {
                log += " _ empty phone";
                Logger.log(log);
                return;
            }
            Phonenumber.PhoneNumber number = null;
            if (phoneNumberUtils != null) {
                log += " - check phoneNumber is valid";
                try {
                    String regionCode = null;
                    Locale defaultLocale = Locale.getDefault();
                    if (defaultLocale != null) {
                        regionCode = defaultLocale.getCountry();
                    }
                    try {
                        number = phoneNumberUtils.parse(numberStr, regionCode);
                    } catch (NumberParseException npe) {
                        log += " - number parse exception";
                    }
                    boolean isValid = false;
                    if (number != null) {
                        isValid = phoneNumberUtils.isValidNumber(number);
                    }
                    log += " - isValid:";
                    log += isValid;
                    if (!isValid) {
                        log += " - invalid number";
                        Logger.log(log);
                        String str = getString(R.string.phone_invalid_input);
                        if (!TextUtils.isEmpty(str)) {
                            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                } catch (Throwable t) {
                    log += " - Throwable";
                    Logger.log(log);
                    Logger.logThrowable(t);
                }
            }
            log += " - number:";
            log += number;
            log += " - numberStr:";
            log += numberStr;

            //Per Bill, no need to strip separators. will be done on backend
            //Change per Larry, YES strip separators

            String confNumber = String.valueOf(number.getCountryCode()) + String.valueOf(number.getNationalNumber());
            if (!TextUtils.isEmpty(confNumber)) {
                PhoneNumberUtils.stripSeparators(confNumber);
                log += " - confNumber:";
                log += confNumber;
                editor.putString("confirmedPhoneNumber", confNumber);
            } else
                editor.putString("confirmedPhoneNumber", numberStr);
            Logger.log(log);

            String currentUserId = prefs.getString("currentUserId", null);
            String inputUserId = getIntent().getStringExtra("inputUserId");
            if (!TextUtils.equals(currentUserId, inputUserId)) {
                editor.putString("currentUserId", inputUserId);
            }
            editor.apply();
            List<String> permissions = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(NumberConfirmActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_CONTACTS);
            }
            if (ContextCompat.checkSelfPermission(NumberConfirmActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CALL_PHONE);
            }
            //For Tan contact
            if (ContextCompat.checkSelfPermission(NumberConfirmActivity.this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_CONTACTS);
            }
            if (!permissions.isEmpty()) {
                String[] requestingPermissions = new String[permissions.size()];
                requestingPermissions = permissions.toArray(requestingPermissions);
                awaitingForPermissions = true;
                ActivityCompat.requestPermissions(NumberConfirmActivity.this, requestingPermissions, PERMISSION_REQUEST_ALL);
            } else {
                Intent mainIntent = new Intent(NumberConfirmActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_ALL:
                if (awaitingForPermissions) {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Intent mainIntent = new Intent(NumberConfirmActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        finish();
                    }
                }
                break;
            default:
        }
    }
}

package com.picup.calling;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import com.picup.calling.base.PicupActivity;
import com.picup.calling.base.PicupApplication;
import com.picup.calling.dialog.ErrorDialogFragment;
import com.picup.calling.helper.AddressBookHelper;
import com.picup.calling.network.CallAccess;
import com.picup.calling.network.CallAccessForm;
import com.picup.calling.network.PicupService;
import com.picup.calling.util.Logger;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.Phonenumber;
import com.picup.calling.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ContactProfileUnknownActivity extends PicupActivity implements View.OnClickListener,
        ContactProfileCallsTabContent.OnContactProfileCallsTabContentListener,
        ErrorDialogFragment.OnErrorDialogFragmentListener,
        ContactProfileContactTabContent.OnContactProfileContactTabContentListener
{
    private static final String TAG = ContactProfileUnknownActivity.class.getSimpleName();
    private static final String KEY_CURRENT_TAB_INDEX = "android.idt.net.com.picup.calling.ContactProfileUnknownActivity.KEY_CURRENT_TAB_INDEX";

    private static final int PERMISSION_REQUEST_CALL_PHONE = 1;

    private String picupNumber = null;
    private static String nonContactPhoneNumber = null;

    private ImageView backImageView = null;
    private TextView phoneNumberTextView = null;

    private ImageView callImageView = null;
    private Button createContactButton = null;
    private Button updateExistingButton = null;

    private static TabLayout tabLayout = null;
    private LocalPagerAdapter pagerAdapter = null;

    private ViewPager viewPager = null;

    private TabLayout.ViewPagerOnTabSelectedListener viewPagerOnTabSelectedHandler = null;
    private ViewPager.SimpleOnPageChangeListener tabLayoutOnPageChangeHandler = null;

    private static boolean needCalling = false;
    private static String callToNumber = null;

    private static CallAccessForm callAccessForm = null;
    private static CallAccess callAccess = null;

    private static PicupService picupService = null;
    private ArrayList<Phonenumber.PhoneNumber> contactNumbers = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.log("ContactProfileUnknownActivity - onCreate");

        setContentView(R.layout.contact_profile_unknown);
        picupNumber = getIntent().getStringExtra("picupNumber");
        nonContactPhoneNumber = getIntent().getStringExtra("nonContactPhoneNumber");

        backImageView = (ImageView)findViewById(R.id.back_imageview);
        backImageView.setOnClickListener(this);

        phoneNumberTextView = (TextView)findViewById(R.id.phone_number_textview);
        contactNumbers = new ArrayList<Phonenumber.PhoneNumber>(1);

        Logger.log("ContactProfileUnknownActivity - onCreate - number:"+nonContactPhoneNumber);
        if (!TextUtils.isEmpty(nonContactPhoneNumber)) {
            try {
                contactNumbers.add(PicupApplication.phoneNumberUtil.parse(PhoneNumberUtils.stripSeparators(nonContactPhoneNumber), Locale.getDefault().getCountry()));
            } catch (NumberParseException e) {
            }

            try {
                nonContactPhoneNumber = PhoneNumberUtils.formatNumber(nonContactPhoneNumber);
            } catch (Throwable t) {
            }
        }
        phoneNumberTextView.setText(nonContactPhoneNumber);

        callImageView = (ImageView)findViewById(R.id.call_imageview);
        if (callImageView != null) {
            callImageView.setOnClickListener(this);
        }

        createContactButton = (Button)findViewById(R.id.add_contact_button);
        createContactButton.setOnClickListener(this);
        updateExistingButton = (Button)findViewById(R.id.add_to_exist_contact_button);
        updateExistingButton.setOnClickListener(this);

        //tabLayout = (TabLayout)findViewById(R.id.tab_layout);

        //TabLayout.Tab callsTab = tabLayout.newTab().setCustomView(R.layout.contact_profile_calls_tab).setTag("Calls_Tab");
        //tabLayout.addTab(callsTab);

        pagerAdapter = new LocalPagerAdapter(getSupportFragmentManager(), this, 1);
        viewPager = (ViewPager)findViewById(R.id.view_pager);
        viewPager.setAdapter(pagerAdapter);

        //viewPagerOnTabSelectedHandler = new ViewPagerOnTabSelectedHandler(viewPager);
        //tabLayout.addOnTabSelectedListener(viewPagerOnTabSelectedHandler);

        //tabLayoutOnPageChangeHandler = new TabLayoutOnPageChangeHandler();
        //viewPager.addOnPageChangeListener(tabLayoutOnPageChangeHandler);

        callAccessForm = PicupApplication.callAccessForm;
        if (callAccessForm == null) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            PicupApplication.initCallAccessForm(this, telephonyManager);
            callAccessForm = PicupApplication.callAccessForm;
        }
        if (callAccessForm != null) {
            callAccessForm.setPicupNum(picupNumber);
            callAccessForm.setCallerIdNum(picupNumber);
        }
        picupService = PicupService.retrofit.create(PicupService.class);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //int currentTabIndex = savedInstanceState.getInt(KEY_CURRENT_TAB_INDEX);
        //tabLayout.getTabAt(currentTabIndex).select();
    }

    public void onResume() {
        super.onResume();

        Logger.log("ContactProfileActivity - onResume - needsRefresh:"+PicupApplication.needsRefresh);
        if (PicupApplication.needsRefresh) {
            //dont reset flag since needed by calls tab
            int selectedTabIndex = viewPager.getCurrentItem();
            switch (selectedTabIndex) {
                case 0:
                    ContactProfileCallsTabContent contactProfileCallsTabContent = (ContactProfileCallsTabContent) pagerAdapter.getItem(selectedTabIndex);
                    contactProfileCallsTabContent.doRefresh();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == backImageView.getId()) {
            super.finish();
        } else if (v.getId() == createContactButton.getId()) {
            Logger.log("ContactProfileUnknownActivity - onClick - addContact");
            String number = phoneNumberTextView.getText().toString();
            PicupApplication.needsRefresh = true;
            AddressBookHelper.initCreateContact(this, number);
            finish(); //need to finish so that regular contact profile opens
        } else if (v.getId() == updateExistingButton.getId()) {
            Logger.log("ContactProfileUnknownActivity - onClick - addTo Existing");
            String number = phoneNumberTextView.getText().toString();
            PicupApplication.needsRefresh = true;
            AddressBookHelper.initUpdateContacts(this, number);
            finish();
        } else if (v.getId() == callImageView.getId()) {
            String callToNumber = phoneNumberTextView.getText().toString();
            Logger.log("ContactProfileUnknownActivity - onClick - call - callToNumber:"+callToNumber);
            if (TextUtils.isEmpty(callToNumber))
                return;
            Phonenumber.PhoneNumber phoneNumber = null;
            try {
                phoneNumber = PicupApplication.phoneNumberUtil.parse(PhoneNumberUtils.stripSeparators(callToNumber), Locale.getDefault().getCountry());
            } catch (Exception e) {
                Logger.log("ContactProfileUnknownActivity - onClick - call - exception");
            }
            if (phoneNumber == null)   //TODO dialog if number formatted incorrectly
                return;
            if (phoneNumber.hasCountryCode()) {
                ContactProfileUnknownActivity.callToNumber = phoneNumber.getCountryCode() + String.valueOf(phoneNumber.getNationalNumber());
            } else { //maybe hardcode +1 here...
                ContactProfileUnknownActivity.callToNumber = String.valueOf(phoneNumber.getNationalNumber());
            }

            if (callAccessForm == null) {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                PicupApplication.initCallAccessForm(this, telephonyManager);
                callAccessForm = PicupApplication.callAccessForm;
            }
            if (callAccessForm == null)
                return;

            String tokenId = PicupApplication.getToken();
            int accountId = PicupApplication.getAccountId();
            int userId = PicupApplication.getUserId();

            callAccessForm.setUserId(userId);
            callAccessForm.setPicupNum(picupNumber);
            callAccessForm.setCallerIdNum(picupNumber);
            callAccessForm.setDialedNum(PhoneNumberUtils.stripSeparators(ContactProfileUnknownActivity.callToNumber));

            Call<CallAccess> accessCall = picupService.makeCall(tokenId, String.valueOf(accountId), callAccessForm);
            accessCall.enqueue(new Callback<CallAccess>() {
                @Override
                public void onResponse(Call<CallAccess> call, Response<CallAccess> response) {
                    if (response.isSuccessful()) {
                        CallAccess callAccess = response.body();
                        if (callAccess != null) {
                            if (ContextCompat.checkSelfPermission(ContactProfileUnknownActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                String tan = callAccess.getTan();
                                AddressBookHelper.insertLogoContact(ContactProfileUnknownActivity.this, tan);
                                Intent phoneCallIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + tan));
                                try {
                                    startActivity(phoneCallIntent);
                                } finally {
                                    needCalling = false;
                                }
                            } else {
                                needCalling = true;
                                ActivityCompat.requestPermissions(ContactProfileUnknownActivity.this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CALL_PHONE);
                            }
                        } else {
                            needCalling = false;
                            ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(3);
                            errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
                        }
                    } else {
                        needCalling = false;
                        if (response.errorBody() != null) {
                            try {
                                String errorString = response.errorBody().string();
                                if (!TextUtils.isEmpty(errorString)) {
                                    Logger.log("MainActivity - makeCall - onResponse - failure error:" + errorString);
                                    String convString = errorString.replace("\"", "\'");
                                    Logger.log("MainActivity - makeCall - onResponse - converted string:" + convString);
                                    JSONObject jObjError = new JSONObject(convString);
                                    Logger.log("MainActivity - makeCall - onResponse - failure json - contains error:" + jObjError.has("error") + " - contains code:" + jObjError.has("code"));
                                    if (jObjError.has("error")) {
                                        String errorDescString = jObjError.getString("error");
                                        if (errorDescString.equalsIgnoreCase("account_suspended")) { //no minutes left
                                            ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(2);
                                            errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
                                            return;
                                        } else if (errorDescString.equalsIgnoreCase("invalid_token")) { //login error
                                            ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(0);
                                            errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
                                            return;
                                        }
                                    } else if (jObjError.has("code")) {
                                        int errorCode = jObjError.getInt("code");
                                        if (errorCode == 33) { //no minutes left
                                            ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(2);
                                            errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
                                            return;
                                        } else if (errorCode == 34) { //TAN not available
                                            ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(3);
                                            errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
                                            return;
                                        }
                                    }
                                }
                            }catch(Exception e){
                                Logger.log("MainActivity - makeCall - onResponse - failure error - Exception:" + e.toString());
                                e.printStackTrace();
                            }
                        }

                        ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(0);
                        errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
                    }
                }
                @Override
                public void onFailure(Call<CallAccess> call, Throwable t) {
                    ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(1);
                    errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CALL_PHONE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (needCalling) {
                        if (callAccess != null) {
                            String tan = callAccess.getTan();
                            AddressBookHelper.insertLogoContact(ContactProfileUnknownActivity.this, tan);
                            Intent phoneCallIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + tan));
                            try {
                                startActivity(phoneCallIntent);
                            } finally {
                                needCalling = false;
                            }
                        }
                    }
                }
                break;
            default:

        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putInt(KEY_CURRENT_TAB_INDEX, tabLayout.getSelectedTabPosition());
    }

    @Override
    public void contactProfileCall(String callToNumber) {
        Logger.log("ContactProfileUnknownActivity - contactProfileCall - callToNumber:" + callToNumber);
        Phonenumber.PhoneNumber phoneNumber = null;
        try {
            phoneNumber = PicupApplication.phoneNumberUtil.parse(PhoneNumberUtils.stripSeparators(callToNumber), Locale.getDefault().getCountry());
        } catch (Exception e) {
            Logger.log("ContactProfileUnknownActivity - contactProfileCall - exception with callToNumber:"+callToNumber);
        }
        if (phoneNumber == null) {
            //TODO add some type of dialog about invalid number
            return;
        }

        if (phoneNumber.hasCountryCode()) {
            ContactProfileUnknownActivity.callToNumber = phoneNumber.getCountryCode() + String.valueOf(phoneNumber.getNationalNumber());
        } else {
            ContactProfileUnknownActivity.callToNumber = String.valueOf(phoneNumber.getNationalNumber());
        }
        String tokenId = PicupApplication.getToken();
        int accountId = PicupApplication.getAccountId();
        int userId = PicupApplication.getUserId();

        if (callAccessForm == null) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            PicupApplication.initCallAccessForm(this, telephonyManager);
            callAccessForm = PicupApplication.callAccessForm;
        }
        if (callAccessForm == null)
            return;

        callAccessForm.setDialedNum(PhoneNumberUtils.stripSeparators(ContactProfileUnknownActivity.callToNumber));
        callAccessForm.setPicupNum(PhoneNumberUtils.stripSeparators(picupNumber));
        callAccessForm.setCallerIdNum(PhoneNumberUtils.stripSeparators(picupNumber));
        callAccessForm.setUserId(userId);

        Call<CallAccess> accessCall = picupService.makeCall(tokenId, String.valueOf(accountId), callAccessForm);
        accessCall.enqueue(new Callback<CallAccess>() {
            @Override
            public void onResponse(Call<CallAccess> call, Response<CallAccess> response) {
                if (response.isSuccessful()) {
                    CallAccess callAccess = response.body();
                    if (callAccess != null) {
                        if (ContextCompat.checkSelfPermission(ContactProfileUnknownActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                            String tan = callAccess.getTan();
                            AddressBookHelper.insertLogoContact(ContactProfileUnknownActivity.this, tan);
                            Intent phoneCallIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + tan));
                            try {
                                startActivity(phoneCallIntent);
                            } finally {
                                needCalling = false;
                            }
                        } else {
                            needCalling = true;
                            ActivityCompat.requestPermissions(ContactProfileUnknownActivity.this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CALL_PHONE);
                        }
                    } else {
                        needCalling = false;
                        ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(3);
                        errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
                    }
                } else {
                    needCalling = false;
                    if (response.errorBody() != null) {
                        try {
                            String errorString = response.errorBody().string();
                            if (!TextUtils.isEmpty(errorString)) {
                                Logger.log("MainActivity - makeCall - onResponse - failure error:" + errorString);
                                String convString = errorString.replace("\"", "\'");
                                Logger.log("MainActivity - makeCall - onResponse - converted string:" + convString);
                                JSONObject jObjError = new JSONObject(convString);
                                Logger.log("MainActivity - makeCall - onResponse - failure json - contains error:" + jObjError.has("error") + " - contains code:" + jObjError.has("code"));
                                if (jObjError.has("error")) {
                                    String errorDescString = jObjError.getString("error");
                                    if (errorDescString.equalsIgnoreCase("account_suspended")) { //no minutes left
                                        ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(2);
                                        errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
                                        return;
                                    } else if (errorDescString.equalsIgnoreCase("invalid_token")) { //login error
                                        ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(0);
                                        errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
                                        return;
                                    }
                                } else if (jObjError.has("code")) {
                                    int errorCode = jObjError.getInt("code");
                                    if (errorCode == 33) { //no minutes left
                                        ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(2);
                                        errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
                                        return;
                                    } else if (errorCode == 34) { //TAN not available
                                        ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(3);
                                        errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
                                        return;
                                    }
                                }
                            }
                        }catch(Exception e){
                            Logger.log("MainActivity - makeCall - onResponse - failure error - Exception:" + e.toString());
                            e.printStackTrace();
                        }
                    }

                    ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(0);
                    errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
                }
            }

            @Override
            public void onFailure(Call<CallAccess> call, Throwable t) {
                needCalling = false;
                ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(1);
                errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
            }
        });
    }

    @Override
    public void reAuthenticate() {
        Intent signOnIntent = new Intent(this, SignOnActivity.class);
        startActivity(signOnIntent);
        super.finish();
    }

    @Override
    public void appSettings() {
        Intent settingsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.picup.com/account/"));
        if (settingsIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(settingsIntent);
        }
    }


    public static class LocalPagerAdapter extends FragmentPagerAdapter {
        private int tabCount = 1;

        private ContactProfileCallsTabContent contactProfileCallsTabContent = null;

        public LocalPagerAdapter(FragmentManager fm, Context context, int tabCount) {
            super(fm);
            this.tabCount = tabCount;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;

            switch (position) {
                case 0:
                    if (contactProfileCallsTabContent ==  null) {
                        contactProfileCallsTabContent = ContactProfileCallsTabContent.newInstance(nonContactPhoneNumber);
                    }
                    fragment = contactProfileCallsTabContent;
                    break;
                default:
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return tabCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            CharSequence title = "";

            switch (position) {
                case 0:
                    title = "";
                    break;
                default:
                    title = super.getPageTitle(position);
            }
            return title;
        }

    }

    public boolean isValidContactNumber(Phonenumber.PhoneNumber tn) {
        boolean found = false;
        Logger.log("ContactProfileUnknownActivity - isValidContactNumber - tn:"+tn.toString());
        if (contactNumbers != null && contactNumbers.size() > 0)
            for (Phonenumber.PhoneNumber num : contactNumbers) {
                Logger.log("ContactProfileUnkownActivity - isValidContactNumber - contactNumber:"+num.toString());
                if (num.equals(tn)) {
                    found = true;
                    break;
                }
            }

        return found;

    }


}

package com.picup.calling;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import com.picup.calling.base.PicupActivity;
import com.picup.calling.base.PicupApplication;
import com.picup.calling.data.Phone;
import com.picup.calling.dialog.ErrorDialogFragment;
import com.picup.calling.helper.AddressBookHelper;
import com.picup.calling.network.CallAccess;
import com.picup.calling.network.CallAccessForm;
import com.picup.calling.network.PicupService;
import com.picup.calling.util.Logger;
import com.picup.calling.util.PicupImageUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.Phonenumber;
import com.picup.calling.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public final class ContactProfileActivity extends PicupActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener,
        ContactProfileContactTabContent.OnContactProfileContactTabContentListener,
        ContactProfileCallsTabContent.OnContactProfileCallsTabContentListener,
        ErrorDialogFragment.OnErrorDialogFragmentListener {
    private static final String TAG = ContactProfileActivity.class.getSimpleName();
    private static final String KEY_CURRENT_TAB_INDEX = "android.idt.net.com.picup.calling.ContactProfileActivity.KEY_CURRENT_TAB_INDEX";

    private static final int PERMISSION_REQUEST_CALL_PHONE = 1;

    private static final int REQUEST_CODE_EDIT_PROFILE = 1;
    private static final int REQUEST_CODE_DELETE_PROFILE = 2;

    private static final String ARG_CONTACT_ID = "android.idt.net.com.picup.calling.ContactProfileActivity.CONTACT_ID";

    private static final int PROFILE_LOADER_ID = 0;
    private static final String[] PROFILE_PROJECTION = {Contacts._ID, Contacts.LOOKUP_KEY, Contacts.PHOTO_THUMBNAIL_URI, Contacts.DISPLAY_NAME_PRIMARY, Contacts.HAS_PHONE_NUMBER, Contacts.DISPLAY_NAME_SOURCE};
    private static final int PROFILE_ID_INDEX = 0;
    private static final int PROFILE_LOOKUP_KEY_INDEX = 1;
    private static final int PROFILE_THUMBNAIL_URI_INDEX = 2;
    private static final int PROFILE_DISPLAY_NAME_PRIMARY_INDEX = 3;
    private static final int PROFILE_HAS_PHONE_INDEX = 4;
    private static final int PROFILE_DISPLAY_NAME_SOURCE_INDEX = 5;

    //private static final String[] PRIMARY_PHONE_PROJECTION = {ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE};
    //private static final int PHONE_NUMBER_INDEX = 2;
    //private static final int PHONE_TYPE_INDEX = 3;

    private static final int PRIMARY_PHONE_LOADER_ID = 1;
    private static final String[] PRIMARY_PHONE_PROJECTION = {CommonDataKinds.Phone._ID, CommonDataKinds.Phone.LOOKUP_KEY,
            CommonDataKinds.Phone.NUMBER, CommonDataKinds.Phone.TYPE,
            CommonDataKinds.Phone.NORMALIZED_NUMBER, CommonDataKinds.Phone.LABEL};

    private static final int PRIMARY_PHONE_ID_INDEX = 0;
    private static final int PRIMARY_PHONE_LOOKUP_KEY_INDEX = 1;
    private static final int PRIMARY_PHONE_NUMBER_INDEX = 2;
    private static final int PRIMARY_PHONE_TYPE_INDEX = 3;
    private static final int PRIMARY_PHONE_NORMALIZED_NUMBER_INDEX = 4;
    private static final int PRIMARY_PHONE_LABEL_INDEX = 5;

    private static final int PHONES_LOADER_ID = 2;
    private static final String[] PHONES_PROJECTION = {CommonDataKinds.Phone._ID, CommonDataKinds.Phone.LOOKUP_KEY,
            CommonDataKinds.Phone.NUMBER, CommonDataKinds.Phone.TYPE, CommonDataKinds.Phone.IS_PRIMARY
            , CommonDataKinds.Phone.NORMALIZED_NUMBER, CommonDataKinds.Phone.LABEL};

    private static final int PHONES_ID_INDEX = 0;
    private static final int PHONES_LOOKUP_KEY_INDEX = 1;
    private static final int PHONES_NUMBER_INDEX = 2;
    private static final int PHONES_TYPE_INDEX = 3;
    private static final int PHONES_IS_PRIMARY_INDEX = 4;
    private static final int PHONES_NORMALIZED_NUMBER_INDEX = 5;
    private static final int PHONES_LABEL_INDEX = 6;

    private static final String[] PHONE_PREFERENCE_PROJECTION = {ContactsContract.Data._ID, ContactsContract.Data.LOOKUP_KEY, ContactsContract.Data.IS_PRIMARY};

    private static int contactId;
    private String picupNumber = null;
    private String nonContactPhoneNumber = null;

    private ImageView backImageView = null;
    private ImageView editImageView = null;
    private ImageView deleteImageView = null;

    private ImageView thumbnailImageView = null;
    private TextView initialTextView = null;
    private TextView fullNameTextView = null;
    private TextView phoneNumberTextView = null;
    private TextView phoneTypeTextView = null;

    private ImageView callImageView = null;

    private static TabLayout tabLayout = null;
    private LocalPagerAdapter pagerAdapter = null;

    private ViewPager viewPager = null;

    private TabLayout.ViewPagerOnTabSelectedListener viewPagerOnTabSelectedHandler = null;
    private ViewPager.SimpleOnPageChangeListener tabLayoutOnPageChangeHandler = null;

    private static boolean needCalling = false;
    private static String callToNumber = null;

    private static String lookupKey = null;
    private static long alreadyDisplayedPhoneId = 0;
    private static String alreadyDisplayedNormalizeNumber;
    private static int selectedTabIndex = 0;

    private static CallAccessForm callAccessForm = null;
    private static CallAccess callAccess = null;

    private static PicupService picupService = null;
    private ArrayList<Phonenumber.PhoneNumber> contactNumbers = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.log("ContactProfileActivity - onCreate");

        setContentView(R.layout.contact_profile);
        contactId = getIntent().getIntExtra("contactId", 0);
        picupNumber = getIntent().getStringExtra("picupNumber");
        nonContactPhoneNumber = getIntent().getStringExtra("nonContactPhoneNumber");
        selectedTabIndex = getIntent().getIntExtra("selectedTabIndex", 0);

        backImageView = (ImageView) findViewById(R.id.back_imageview);
        backImageView.setOnClickListener(this);
        editImageView = (ImageView) findViewById(R.id.edit_imageview);
        editImageView.setOnClickListener(this);
        deleteImageView = (ImageView) findViewById(R.id.delete_imageview);
        deleteImageView.setOnClickListener(this);

        thumbnailImageView = (ImageView) findViewById(R.id.thumbnail_imageview);
        initialTextView = (TextView) findViewById(R.id.initial_textview);
        fullNameTextView = (TextView) findViewById(R.id.duration_textview);
        phoneNumberTextView = (TextView) findViewById(R.id.phone_number_textview);
        phoneTypeTextView = (TextView) findViewById(R.id.phone_type_textview);

        callImageView = (ImageView) findViewById(R.id.call_imageview);
        callImageView.setOnClickListener(this);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        TabLayout.Tab contactTab = tabLayout.newTab().setCustomView(R.layout.contact_profile_contact_tab).setTag("Contact_Tab"); // tag will be used when referring to specific tab
        tabLayout.addTab(contactTab);
        TabLayout.Tab callsTab = tabLayout.newTab().setCustomView(R.layout.contact_profile_calls_tab).setTag("Calls_Tab");
        tabLayout.addTab(callsTab);

        //pagerAdapter = new LocalPagerAdapter(getSupportFragmentManager(), this, tabLayout.getTabCount());
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        //viewPager.setAdapter(pagerAdapter);

        viewPagerOnTabSelectedHandler = new ViewPagerOnTabSelectedHandler(viewPager);
        tabLayout.addOnTabSelectedListener(viewPagerOnTabSelectedHandler);

        tabLayoutOnPageChangeHandler = new TabLayoutOnPageChangeHandler();
        viewPager.addOnPageChangeListener(tabLayoutOnPageChangeHandler);

        Bundle args = new Bundle(1);
        args.putInt(ARG_CONTACT_ID, contactId);
        getSupportLoaderManager().initLoader(PROFILE_LOADER_ID, args, this);

/*
        Bundle args2 = new Bundle(1);
        args2.putInt(ARG_CONTACT_ID, contactId);
        getSupportLoaderManager().initLoader(PRIMARY_PHONE_LOADER_ID, args2, this);
*/

        Bundle args3 = new Bundle(1);
        args3.putInt(ARG_CONTACT_ID, contactId);
        getSupportLoaderManager().initLoader(PHONES_LOADER_ID, args3, this);

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
        int currentTabIndex = savedInstanceState.getInt(KEY_CURRENT_TAB_INDEX);
        tabLayout.getTabAt(currentTabIndex).select();
    }

    public void onResume() {
        super.onResume();

        Logger.log("ContactProfileActivity - onResume - needsRefresh:"+PicupApplication.needsRefresh);
        if (PicupApplication.needsRefresh && pagerAdapter != null) {
            //dont reset flag since needed by calls tab
            int selectedTabIndex = tabLayout.getSelectedTabPosition();
            switch (selectedTabIndex) {
                case 1:
                    ContactProfileCallsTabContent contactProfileCallsTabContent = (ContactProfileCallsTabContent) pagerAdapter.getItem(selectedTabIndex);
                    contactProfileCallsTabContent.doRefresh();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == backImageView.getId()) {
            super.finish();
        } else if (v.getId() == editImageView.getId()) {
            Logger.log("ContactProfileActivity - onClick - edit");/*
            Intent editContactIntent = new Intent(this, EditContactActivity.class);
            editContactIntent.putExtra("contactId", contactId);
            startActivityForResult(editContactIntent, REQUEST_CODE_EDIT_PROFILE);*/
            PicupApplication.needsRefresh = true;
            AddressBookHelper.initEditContact(this, contactId, lookupKey);
        } else if (v.getId() == deleteImageView.getId()) {
            Logger.log("ContactProfileActivity - onClick - delete");
            DeleteFragment deleteFragment = new DeleteFragment();
            deleteFragment.show(getSupportFragmentManager(), "deleteFragment");
        } else if (v.getId() == callImageView.getId()) {
            String callToNumber = phoneNumberTextView.getText().toString();
            Logger.log("ContactProfileActivity - onClick - call - callToNumber:"+callToNumber);
            if (TextUtils.isEmpty(callToNumber))
                return;
            Phonenumber.PhoneNumber phoneNumber = null;
            try {
                phoneNumber = PicupApplication.phoneNumberUtil.parse(PhoneNumberUtils.stripSeparators(callToNumber), Locale.getDefault().getCountry());
            } catch (Exception e) {
                Logger.log("ContactProfileActivity - onClick - call - exception");
            }
            if (phoneNumber == null)   //TODO dialog if number formatted incorrectly
                return;
            if (phoneNumber.hasCountryCode()) {
                ContactProfileActivity.callToNumber = phoneNumber.getCountryCode() + String.valueOf(phoneNumber.getNationalNumber());
            } else { //maybe hardcode +1 here...
                ContactProfileActivity.callToNumber = String.valueOf(phoneNumber.getNationalNumber());
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

            callAccessForm.setUserId(userId);
            callAccessForm.setDialedNum(PhoneNumberUtils.stripSeparators(ContactProfileActivity.callToNumber));
            callAccessForm.setPicupNum(picupNumber);
            callAccessForm.setCallerIdNum(picupNumber);

            Call<CallAccess> accessCall = picupService.makeCall(tokenId, String.valueOf(accountId), callAccessForm);
            accessCall.enqueue(new Callback<CallAccess>() {
                @Override
                public void onResponse(Call<CallAccess> call, Response<CallAccess> response) {
                    if (response.isSuccessful()) {
                        CallAccess callAccess = response.body();
                        if (callAccess != null) {
                            if (ContextCompat.checkSelfPermission(ContactProfileActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                String tan = callAccess.getTan();
                                AddressBookHelper.insertLogoContact(ContactProfileActivity.this, tan);
                                Intent phoneCallIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + tan));
                                try {
                                    startActivity(phoneCallIntent);
                                } finally {
                                    needCalling = false;
                                }
                            } else {
                                needCalling = true;
                                try {
                                    ActivityCompat.requestPermissions(ContactProfileActivity.this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CALL_PHONE);
                                } finally {
                                    needCalling = false;
                                }
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
                            AddressBookHelper.insertLogoContact(ContactProfileActivity.this, tan);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Bundle args = new Bundle(1);
            args.putInt(ARG_CONTACT_ID, contactId);
            if (requestCode == REQUEST_CODE_EDIT_PROFILE) {
                //getSupportLoaderManager().restartLoader(PROFILE_LOADER_ID, args, this);
                //getSupportLoaderManager().restartLoader(PHONES_LOADER_ID, args, this);
                //pagerAdapter.refreshContent(alreadyDisplayedPhoneId);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_TAB_INDEX, tabLayout.getSelectedTabPosition());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = null;
        Logger.log("ContactProfileActivity - onCreateLoader - id:"+id);

        if (id == PROFILE_LOADER_ID) {
            if (args != null) {
                int contactId = args.getInt(ARG_CONTACT_ID);
                // load only specific contact
                if (contactId > 0) {
                    String selection = ContactsContract.Contacts._ID + " = ? ";
                    String[] selectionArgs = new String[]{String.valueOf(contactId)};
                    cursorLoader = new CursorLoader(this, ContactsContract.Contacts.CONTENT_URI, PROFILE_PROJECTION, selection, selectionArgs, null);
                }
            }
        } else if (id == PRIMARY_PHONE_LOADER_ID) {
            if (args != null) {
                int contactId = args.getInt(ARG_CONTACT_ID);
                if (contactId > 0) {
                    // load only specific contact
                    String selection = CommonDataKinds.Phone.CONTACT_ID + " = ? AND " + CommonDataKinds.Phone.IS_PRIMARY + " > ? ";
                    String[] selectionArgs = new String[]{String.valueOf(contactId), String.valueOf(0)};
                    cursorLoader = new CursorLoader(this, CommonDataKinds.Phone.CONTENT_URI, PRIMARY_PHONE_PROJECTION, selection, selectionArgs, null);
                }
            }
        } else if (id == PHONES_LOADER_ID) {
            if (args != null) {
                int contactId = args.getInt(ARG_CONTACT_ID);
                if (contactId > 0) {
                    // load only specific contact
                    String selection = CommonDataKinds.Phone.CONTACT_ID + " = ? ";
                    String[] selectionArgs = new String[]{String.valueOf(contactId)};
                    cursorLoader = new CursorLoader(this, CommonDataKinds.Phone.CONTENT_URI, PHONES_PROJECTION, selection, selectionArgs, null);
                }
            }
        } else {
            // handle more loaders here.
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader == null) {
            return;
        }
        int loaderId = loader.getId();
        Logger.log("ContactProfileActivity - onLoadFinished - loaderId:"+loaderId);
        if (loaderId == PROFILE_LOADER_ID) {
            if (data == null || data.isClosed()) {
                return;
            }
            try {
                if (data.moveToFirst()) {
                    if (initialTextView != null) {
                        initialTextView.setText("");
                    }
                    String photoUriString = null;
                    int photoUriStringIndex = PROFILE_THUMBNAIL_URI_INDEX; //data.getColumnIndex(Contacts.PHOTO_THUMBNAIL_URI);
                    if (photoUriStringIndex != -1) {
                        photoUriString = data.getString(photoUriStringIndex);
                    }
                    String fullName = null;
                    int fullNameIndex = PROFILE_DISPLAY_NAME_PRIMARY_INDEX; //data.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY);
                    if (fullNameIndex != -1) {
                        fullName = data.getString(fullNameIndex);
                    }
                    int lookupKeyIndex = PROFILE_LOOKUP_KEY_INDEX; //data.getColumnIndex(Contacts.LOOKUP_KEY);
                    if (lookupKeyIndex != -1) {
                        lookupKey = data.getString(lookupKeyIndex);
                    }
                    int displayNameSource = 0;
                    int displayNameSourceIndex = PROFILE_DISPLAY_NAME_SOURCE_INDEX; //data.getColumnIndex(Contacts.DISPLAY_NAME_SOURCE);
                    if (displayNameSourceIndex != -1) {
                        displayNameSource = data.getInt(displayNameSourceIndex);
                    }
                    if (displayNameSource != ContactsContract.DisplayNameSources.STRUCTURED_NAME
                            && displayNameSource != ContactsContract.DisplayNameSources.STRUCTURED_PHONETIC_NAME
                            && displayNameSource != ContactsContract.DisplayNameSources.NICKNAME) {
                        //UNITE-1841 name come from diff source
                        if (thumbnailImageView != null) {
                            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.icon_for_no_name_contact);
                            RoundedBitmapDrawable roundedBitmapDrawable = PicupImageUtils.toRoundedBitmapDrawable(this, drawable);
                            thumbnailImageView.setImageDrawable(roundedBitmapDrawable);
                            thumbnailImageView.setVisibility(View.VISIBLE);
                        }
                        if (initialTextView != null) {
                            initialTextView.setVisibility(View.INVISIBLE);
                        }
                        if (fullNameTextView != null) {
                            if (displayNameSource == ContactsContract.DisplayNameSources.PHONE) {
                                fullNameTextView.setText(null);
                            } else {
                                fullNameTextView.setText(fullName);
                            }
                        }
                    } else if (!TextUtils.isEmpty(photoUriString)) {
                        if (thumbnailImageView != null) {
                            thumbnailImageView.setImageURI(Uri.parse(photoUriString));
                            Drawable photoThumbnailDrawable = thumbnailImageView.getDrawable();
                            // round the icon
                            RoundedBitmapDrawable roundedBitmapDrawable = PicupImageUtils.toRoundedBitmapDrawable(ContactProfileActivity.this, photoThumbnailDrawable);
                            thumbnailImageView.setImageDrawable(roundedBitmapDrawable);
                            thumbnailImageView.setVisibility(View.VISIBLE);
                        }
                        if (initialTextView != null) {
                            initialTextView.setVisibility(View.INVISIBLE);
                        }
                        if (fullNameTextView != null) {
                            fullNameTextView.setText(fullName);
                        }
                    } else {
                        if (initialTextView != null) {
                            if (!TextUtils.isEmpty(fullName)) {
                                initialTextView.setText("");
                                String[] fullNameArray = fullName.split(" ");
                                if (fullNameArray.length > 0) {
                                    String firstName = fullNameArray[0];
                                    if (!firstName.isEmpty()) {
                                        initialTextView.append(firstName.subSequence(0, 1));
                                    }
                                }
                                if (fullNameArray.length > 1) {
                                    String lastName = fullNameArray[1];
                                    if (!lastName.isEmpty()) {
                                        initialTextView.append(" ");
                                        initialTextView.append(lastName.subSequence(0, 1));
                                    }
                                }
                            }
                            initialTextView.setVisibility(View.VISIBLE);
                        }
                        if (thumbnailImageView != null) {
                            thumbnailImageView.setVisibility(View.INVISIBLE);
                        }
                        if (fullNameTextView != null) {
                            fullNameTextView.setText(fullName);
                        }
                    }
                }
            } catch (Throwable t) {
            }
        } else if (loader.getId() == PRIMARY_PHONE_LOADER_ID) {
            if (data.moveToFirst()) {
                String number = data.getString(PRIMARY_PHONE_NUMBER_INDEX);
                Logger.log("ContactProfileActivity - onLoadFinished - primary number:" + number);
                if (!TextUtils.isEmpty(number)) {
                    try {
                        number = PhoneNumberUtils.formatNumber(number);
                    } catch (Throwable t) {
                    }
                }
                phoneNumberTextView.setText(number);
                //PhoneTypeEnum type = PhoneTypeEnum.getPhoneTypeLabel(data.getInt(PRIMARY_PHONE_TYPE_INDEX));
                int typeVal = -1;
                try {
                    data.getInt(PRIMARY_PHONE_TYPE_INDEX);
                } catch (Exception e) { Logger.log("ContactProfileActivity - onLoadFinished - Exception:"+e.toString());}
                String type = (String) CommonDataKinds.Phone.getTypeLabel(this.getResources(),typeVal,"");
                String customLabel = data.getString(PHONES_LABEL_INDEX);
                if (typeVal == CommonDataKinds.Phone.TYPE_CUSTOM && !TextUtils.isEmpty(customLabel)){
                    phoneTypeTextView.setText(customLabel);
                } else
                    phoneTypeTextView.setText(type);
            } else {
                phoneNumberTextView.setText("");
                phoneTypeTextView.setText("");
            }
        } else if (loader.getId() == PHONES_LOADER_ID) {
            phoneNumberTextView.setText("");
            phoneTypeTextView.setText("");
            callImageView.setImageResource(R.drawable.ic_phone_gray_rounded); //setVisibility(View.INVISIBLE);
            callImageView.setEnabled(false);
            contactNumbers = new ArrayList<Phonenumber.PhoneNumber>(data.getCount());
            if (data.getCount() > 0) {
                Logger.log("ContactProfileActivity - onLoadFinished - data count:"+data.getCount());
                if (data.moveToFirst()) {
                    List<Phone> phones = new ArrayList<>(data.getCount());
                    do {
                        int id = data.getInt(PHONES_ID_INDEX);
                        String number = data.getString(PHONES_NUMBER_INDEX);
                        //PhoneTypeEnum type = PhoneTypeEnum.getPhoneTypeLabel(data.getInt(PHONES_TYPE_INDEX));
                        int type = -1;
                        try {
                            type = data.getInt(PHONES_TYPE_INDEX);
                        } catch (Exception e) { Logger.log("ContactProfileActivity - onLoadFinished - Exception:"+e.toString());}
                        String typeLabel = (String) CommonDataKinds.Phone.getTypeLabel(this.getResources(),type,"");
                        String customLabel = "";
                        //do not store customLabel if type is not custom
                        if (type == CommonDataKinds.Phone.TYPE_CUSTOM)
                            customLabel = data.getString(PHONES_LABEL_INDEX);
                        Logger.log("ContactProfileActivity - onLoadFinished - type:"+type+" typeLabel:"+typeLabel+" customLabel:"+customLabel);
                        boolean isPrimary = Boolean.valueOf(data.getInt(PHONES_IS_PRIMARY_INDEX) == 1 ? true : false);
                        int normalizeNumberIndex = PHONES_NORMALIZED_NUMBER_INDEX; //data.getColumnIndex(CommonDataKinds.Phone.NORMALIZED_NUMBER);
                        String normalizeNumber = null;
                        if (normalizeNumberIndex != -1) {
                            normalizeNumber = data.getString(normalizeNumberIndex);
                        }
                        Phone phone = new Phone(id, number, typeLabel, isPrimary, customLabel);
                        phone.setNormalizeNumber(normalizeNumber);
                        Logger.log("ContactProfileActivity - onLoadFinished - additional number:" + number);
                        try {
                            contactNumbers.add(PicupApplication.phoneNumberUtil.parse(PhoneNumberUtils.stripSeparators(number), Locale.getDefault().getCountry()));
                        } catch (NumberParseException e) {

                        }
                        phones.add(phone);
                    } while (data.moveToNext());
                    Phone primaryPhone = null;
                    Logger.log("ContactProfileActivity - onLoadFinished - phones size:"+phones.size());
                    for (int i = 0; i < phones.size(); i++) {
                        Phone phone = phones.get(i);
                        if (phone.isPrimary()) {
                            primaryPhone = phones.remove(i);
                            break;
                        }
                    }
                    if (primaryPhone != null) {
                        String number = primaryPhone.getNumber();
                        if (!TextUtils.isEmpty(number)) {
                            try {
                                number = PhoneNumberUtils.formatNumber(number);
                            } catch (Throwable t) {
                            }
                        }
                        phoneNumberTextView.setText(number);
                        // will pass this value to ContactProfileContactTabContent
                        alreadyDisplayedPhoneId = primaryPhone.getId();
                        alreadyDisplayedNormalizeNumber = primaryPhone.getNormalizeNumber();
                        //Log.d(TAG, "***alreadyDisplayedPhoneId=" + alreadyDisplayedPhoneId);

                        if (!TextUtils.isEmpty(primaryPhone.getCustomLabel())){
                            phoneTypeTextView.setText(primaryPhone.getCustomLabel());
                        } else
                            phoneTypeTextView.setText(primaryPhone.getType());

                        //phoneTypeTextView.setText(primaryPhone.getType().toString());
                    } else {
                        Phone firstPhone = phones.remove(0);
                        // will pass this value to ContactProfileContactTabContent
                        String number = firstPhone.getNumber();
                        if (!TextUtils.isEmpty(number)) {
                            try {
                                number = PhoneNumberUtils.formatNumber(number);
                            } catch (Throwable t) {
                            }
                        }
                        phoneNumberTextView.setText(number);
                        alreadyDisplayedPhoneId = firstPhone.getId();
                        alreadyDisplayedNormalizeNumber = firstPhone.getNormalizeNumber();
                        //Log.d(TAG, "***alreadyDisplayedPhoneId=" + alreadyDisplayedPhoneId);
                        if (!TextUtils.isEmpty(firstPhone.getCustomLabel())){
                            phoneTypeTextView.setText(firstPhone.getCustomLabel());
                        } else
                            phoneTypeTextView.setText(firstPhone.getType());
                        //phoneTypeTextView.setText(firstPhone.getType().toString());
                    }
                    Logger.log("ContactProfileActivity - onLoadFinished - phones size after find primary:"+phones.size());
                    callImageView.setImageResource(R.drawable.ic_white_purple_phone); //setVisibility(View.VISIBLE);
                    callImageView.setEnabled(true);
                }
            }
            // wait on setting adapter until having alreadyDisplayedPhoneId
            if (pagerAdapter == null) {
                pagerAdapter = new LocalPagerAdapter(getSupportFragmentManager(), this, tabLayout.getTabCount());
                if (viewPager != null)
                    viewPager.setAdapter(pagerAdapter);
            } else {
                //((ContactProfileContactTabContent)pagerAdapter.getItem(0)).setAlreadyDisplayedPhoneId(alreadyDisplayedPhoneId);
                pagerAdapter.refreshContent(alreadyDisplayedPhoneId, alreadyDisplayedNormalizeNumber);
            }
            if (viewPager != null)
                viewPager.setCurrentItem(selectedTabIndex);
        } else {
            // handle more loaders here.
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == PROFILE_LOADER_ID) {
            thumbnailImageView.setImageDrawable(null);
            fullNameTextView.setText("");
        } else if (loader.getId() == PHONES_LOADER_ID) {
            phoneNumberTextView.setText("");
            phoneTypeTextView.setText("");
        } else {
            // handle more loaders here.
        }

    }

    @Override
    public void contactProfileCall(String callToNumber) {
        Logger.log("ContactProfileActivity - contactProfileCall - callToNumber:" + callToNumber);
        if (TextUtils.isEmpty(callToNumber))
            return;

        Phonenumber.PhoneNumber phoneNumber = null;
        try {
            phoneNumber = PicupApplication.phoneNumberUtil.parse(PhoneNumberUtils.stripSeparators(callToNumber), Locale.getDefault().getCountry());
        } catch (Exception e) {
            Logger.log("ContactProfileActivity - contactProfileCall - exception with callToNumber:"+callToNumber);
        }
        if (phoneNumber == null) {
            //TODO add some type of dialog about invalid number
            return;
        }

        if (phoneNumber.hasCountryCode()) {
            ContactProfileActivity.callToNumber = phoneNumber.getCountryCode() + String.valueOf(phoneNumber.getNationalNumber());
        } else {
            ContactProfileActivity.callToNumber = String.valueOf(phoneNumber.getNationalNumber());
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

        callAccessForm.setUserId(userId);
        callAccessForm.setDialedNum(PhoneNumberUtils.stripSeparators(ContactProfileActivity.callToNumber));
        callAccessForm.setPicupNum(PhoneNumberUtils.stripSeparators(picupNumber));
        callAccessForm.setCallerIdNum(PhoneNumberUtils.stripSeparators(picupNumber));
        Call<CallAccess> accessCall = picupService.makeCall(tokenId, String.valueOf(accountId), callAccessForm);
        accessCall.enqueue(new Callback<CallAccess>() {
            @Override
            public void onResponse(Call<CallAccess> call, Response<CallAccess> response) {
                if (response.isSuccessful()) {
                    CallAccess callAccess = response.body();
                    if (callAccess != null) {
                        if (ContextCompat.checkSelfPermission(ContactProfileActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                            String tan = callAccess.getTan();
                            AddressBookHelper.insertLogoContact(ContactProfileActivity.this, tan);
                            Intent phoneCallIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + tan));
                            try {
                                startActivity(phoneCallIntent);
                            } finally {
                                needCalling = false;
                            }
                        } else {
                            needCalling = true;
                            ActivityCompat.requestPermissions(ContactProfileActivity.this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CALL_PHONE);
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

    public boolean isValidContactNumber(Phonenumber.PhoneNumber tn) {
        boolean found = false;
        //Logger.log("ContactProfileActivity - isValidContactNumber - tn:" + tn.toString());
        if (contactNumbers != null && contactNumbers.size() > 0)
            for (Phonenumber.PhoneNumber num : contactNumbers) {
                //Logger.log("ContactProfileActivity - isValidContactNumber - contactNumber:" + num.toString());
                if (num.equals(tn)) {
                    found = true;
                    break;
                }
            }

        return found;

    }

    public static class LocalPagerAdapter extends FragmentPagerAdapter {
        private int tabCount = 0;

        private ContactProfileContactTabContent contactProfileContactTabContent = null;
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
                    if (contactProfileContactTabContent == null) {
                        contactProfileContactTabContent = ContactProfileContactTabContent.newInstance(contactId, alreadyDisplayedPhoneId, alreadyDisplayedNormalizeNumber);
                    }
                    fragment = contactProfileContactTabContent;
                    break;
                case 1:
                    if (contactProfileCallsTabContent == null) {
                        contactProfileCallsTabContent = ContactProfileCallsTabContent.newInstance(contactId);
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
                    title = "Contact";
                    break;
                case 1:
                    title = "Calls";
                    break;
                default:
                    title = super.getPageTitle(position);
            }
            return title;
        }

        public void refreshContent(long alreadyDisplayedPhoneId, String alreadyDisplayedNormalizeNumber) {
            ContactProfileActivity.alreadyDisplayedPhoneId = alreadyDisplayedPhoneId;
            ContactProfileActivity.alreadyDisplayedNormalizeNumber = alreadyDisplayedNormalizeNumber;
            if (contactProfileContactTabContent != null) {
                contactProfileContactTabContent.restartLoaders(alreadyDisplayedPhoneId, alreadyDisplayedNormalizeNumber);
            }
        }
    }

    /**
     * ViewPager to respond to tab selection event on TabLayout
     */
    private static class ViewPagerOnTabSelectedHandler extends TabLayout.ViewPagerOnTabSelectedListener {
        private ViewPager viewPager = null;

        public ViewPagerOnTabSelectedHandler(ViewPager viewPager) {
            super(viewPager);
            this.viewPager = viewPager;
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            viewPager.setCurrentItem(tab.getPosition());
        }
    }

    /**
     * TabLayout to respond to page change event on ViewPager
     */
    private static class TabLayoutOnPageChangeHandler extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            TabLayout.Tab selectedTab = tabLayout.getTabAt(position);
            selectedTab.select();
        }
    }

    public static class DeleteFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.delete_contact_title);
            builder.setMessage(R.string.delete_contact_message);
            builder.setNegativeButton(R.string.cancel_cap_label, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(R.string.delete_cap_label, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PicupApplication.needsRefresh = true;
                    Uri uri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, "/" + lookupKey + "/" + contactId);
                    int deletedRowCount = getActivity().getContentResolver().delete(uri, null, null);
                    if (deletedRowCount > 0) {
                        Toast.makeText(getActivity(), "Contact deleted", Toast.LENGTH_SHORT).show();
                        ((Activity) getContext()).finish();
                    }
                    dialog.dismiss();
                }
            });
            return builder.create();
        }
    }


}

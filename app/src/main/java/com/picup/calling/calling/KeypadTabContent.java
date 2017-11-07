package com.picup.calling;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import com.picup.calling.base.CheckImageButton;
import com.picup.calling.util.Logger;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.picup.calling.R;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public final class KeypadTabContent extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener, TextWatcher,
        View.OnLongClickListener {
    private static final String TAG = KeypadTabContent.class.getSimpleName();

    private CursorAdapter lookupCursorAdapter = null;

    private static final int LOOKUP_LOADER_ID = 0;
    private static final String[] LOOKUP_PROJECTION = {CommonDataKinds.Phone._ID, CommonDataKinds.Phone.LOOKUP_KEY, CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            CommonDataKinds.Phone.NUMBER, CommonDataKinds.Phone.NORMALIZED_NUMBER, CommonDataKinds.Phone.TYPE};
    private static final int LOOKUP_ID_INDEX = 0;
    private static final int LOOKUP_LOOKUP_KEY_INDEX = 1;
    private static final int LOOKUP_DISPLAY_NAME_PRIMARY_INDEX = 2;
    private static final int LOOKUP_NUMBER_INDEX = 3;
    private static final int LOOKUP_TYPE_INDEX = 4;
    private static final String SEARCH_CRITERIA = "android.idt.net.com.picup.calling.KeypadTabContent.SEARCH_CRITERIA";

    private TextView callToLabelTextView = null;
    private TextView callToValueTextView = null;
    private ImageButton backImageButton = null;

    private Spinner callToResultSpinner = null;
    private Button addContactButton = null;
    private View addContactSeparator = null;
    private View addToExistContactButton = null;
    private TextView matchedFullNameTextView = null;
    private TextView matchedTypeTextView = null;

    private GridLayout gridLayout = null;

    private ImageButton key1ImageButton = null;
    private ImageButton key2ImageButton = null;
    private ImageButton key3ImageButton = null;
    private ImageButton key4ImageButton = null;
    private ImageButton key5ImageButton = null;
    private ImageButton key6ImageButton = null;
    private ImageButton key7ImageButton = null;
    private ImageButton key8ImageButton = null;
    private ImageButton key9ImageButton = null;
    private CheckImageButton key0ImageButton = null;
    private ImageButton keyAsteriskImageButton = null;

    private ImageButton keyPoundImageButton = null;
    private ImageButton keyCallImageButton = null;

    private View callToLayout;
    private View callToResultLayout;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnKeypadTabContentListener listener;

    private static String searchString = "";

    private static AsYouTypeFormatter asYouTypeFormatter = null;
    private static boolean startFormatting = true;
    private Object markup = new Object();
    private static boolean digitRemoved = false;
    //keypad 0 timer
    private static Timer timer;

    public KeypadTabContent() {
        // Required empty public constructor
    }


    public static KeypadTabContent newInstance() {
        KeypadTabContent fragment = new KeypadTabContent();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnKeypadTabContentListener) {
            listener = (OnKeypadTabContentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnKeypadTabContentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        String regionCode = Locale.getDefault().getCountry();
        if (!TextUtils.isEmpty(regionCode)) {
            asYouTypeFormatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(regionCode);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        if (inflater != null) {
            try {
                view = inflater.inflate(R.layout.keypad_tab_content, container, false);
            } catch (Throwable t) {
            }
        }
        if (view != null) {
            //callToLayout = (ViewGroup) view.findViewById(R.id.call_to_layout);
            //callToLayout.setVisibility(View.INVISIBLE);
            callToLabelTextView = (TextView) view.findViewById(R.id.call_to_label_textview);
            callToLabelTextView.setVisibility(View.INVISIBLE);
            callToValueTextView = (TextView) view.findViewById(R.id.call_to_value_textview);
            callToValueTextView.addTextChangedListener(this);
            //callToValueTextView.addTextChangedListener(new LocalPhoneNumberFormattingTextWatcher());

            backImageButton = (ImageButton) view.findViewById(R.id.back_imagebutton);
            backImageButton.setOnClickListener(this);
            backImageButton.setOnLongClickListener(this);
            backImageButton.setVisibility(View.INVISIBLE);

            //callToResultLayout = (RelativeLayout)view.findViewById(R.id.call_to_result_layout);
            //callToResultLayout.setVisibility(View.INVISIBLE);
            callToResultSpinner = (Spinner) view.findViewById(R.id.call_to_result_spinner);
            addContactSeparator = view.findViewById(R.id.add_contact_separator);
            addContactButton = (Button) view.findViewById(R.id.add_contact_button);
            if (addContactButton != null) {
                addContactButton.setOnClickListener(this);
                addContactButton.setVisibility(View.INVISIBLE);
            }
            addToExistContactButton = view.findViewById(R.id.add_to_exist_contact_button);
            if (addToExistContactButton != null) {
                addToExistContactButton.setOnClickListener(this);
                addToExistContactButton.setVisibility(View.INVISIBLE);
            }
            matchedFullNameTextView = (TextView) view.findViewById(R.id.matched_fullname_textview);
            matchedFullNameTextView.setVisibility(View.INVISIBLE);
            matchedTypeTextView = (TextView) view.findViewById(R.id.matched_type_textview);
            matchedTypeTextView.setVisibility(View.INVISIBLE);

            key1ImageButton = (ImageButton) view.findViewById(R.id.key_1_imagebutton);
            key1ImageButton.setOnClickListener(this);
            key2ImageButton = (ImageButton) view.findViewById(R.id.key_2_imagebutton);
            key2ImageButton.setOnClickListener(this);
            key3ImageButton = (ImageButton) view.findViewById(R.id.key_3_imagebutton);
            key3ImageButton.setOnClickListener(this);
            key4ImageButton = (ImageButton) view.findViewById(R.id.key_4_imagebutton);
            key4ImageButton.setOnClickListener(this);
            key5ImageButton = (ImageButton) view.findViewById(R.id.key_5_imagebutton);
            key5ImageButton.setOnClickListener(this);
            key6ImageButton = (ImageButton) view.findViewById(R.id.key_6_imagebutton);
            key6ImageButton.setOnClickListener(this);
            key7ImageButton = (ImageButton) view.findViewById(R.id.key_7_imagebutton);
            key7ImageButton.setOnClickListener(this);
            key8ImageButton = (ImageButton) view.findViewById(R.id.key_8_imagebutton);
            key8ImageButton.setOnClickListener(this);
            key9ImageButton = (ImageButton) view.findViewById(R.id.key_9_imagebutton);
            key9ImageButton.setOnClickListener(this);
            keyAsteriskImageButton = (ImageButton) view.findViewById(R.id.key__asterisk_imagebutton);
            keyAsteriskImageButton.setOnClickListener(this);
            key0ImageButton = (CheckImageButton) view.findViewById(R.id.key_0_imagebutton);
            if (key0ImageButton != null) {
                key0ImageButton.setOnClickListener(this);
                key0ImageButton.setOnLongClickListener(this);
            }
            keyPoundImageButton = (ImageButton) view.findViewById(R.id.key_pound_imagebutton);
            keyPoundImageButton.setOnClickListener(this);
            keyCallImageButton = (ImageButton) view.findViewById(R.id.key_call_imagebutton);
            keyCallImageButton.setOnClickListener(this);

            callToLayout = view.findViewById(R.id.call_to_layout);
            callToResultLayout = view.findViewById(R.id.call_to_result_layout);
        }

        if (asYouTypeFormatter != null) {
            asYouTypeFormatter.clear();
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        lookupCursorAdapter = new LookupCursorAdapter(getActivity(), null, 0);
        callToResultSpinner.setAdapter(lookupCursorAdapter);
        if (savedInstanceState != null) {
            callToValueTextView.setText(savedInstanceState.getString("callToValue"));
        }
        TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = telephonyManager.getNetworkOperator();
        Logger.log("KeypadTabContent - onActivityCreated - networkOperator:"+networkOperator);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (callToLayout != null) {
            callToLayout.setOnLongClickListener(this);
        }
        if (callToResultLayout != null) {
            callToResultLayout.setOnLongClickListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (callToLayout != null) {
            callToLayout.setOnLongClickListener(null);
        }
        if (callToResultLayout != null) {
            callToResultLayout.setOnLongClickListener(null);
        }

        Activity activity = getActivity();
        if (activity != null && activity.isFinishing()) {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("callToValue", callToValueTextView.getText().toString());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onDestroyView() {
        callToValueTextView.removeTextChangedListener(this);
        LoaderManager loaderManager = getLoaderManager();
        if (loaderManager != null) {
            loaderManager.destroyLoader(LOOKUP_LOADER_ID);
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //Log.d(TAG, "s=" + s + " start=" + start + " count=" + count + " after=" + after);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //Log.d(TAG, "s=" + s + " start=" + start + " before=" + before + " count=" + count);
        digitRemoved = (s.length() < before ? true : false);
        if (!TextUtils.isEmpty(s.toString())) {
            if (s instanceof SpannableStringBuilder) {
                ((SpannableStringBuilder) s).setSpan(markup, start, start + count, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        //Log.d(TAG, "s=" + s);
        callToValueTextView.removeTextChangedListener(this);
        int spanStartedAt = s.getSpanStart(markup);
        int spanEndedAt = s.getSpanEnd(markup);
        String formattedString = null;
        if (!TextUtils.isEmpty(s)) {
            if (!digitRemoved) {
                CharSequence newCharSequence = s.subSequence(spanStartedAt, spanEndedAt);
                for (int i = 0; i < newCharSequence.length(); i++) {
                    formattedString = asYouTypeFormatter.inputDigit(newCharSequence.charAt(i));
                }
            } else {
                asYouTypeFormatter.clear();
                String digitOnlyNumber = PhoneNumberUtils.stripSeparators(s.toString());
                for (int i = 0; i < digitOnlyNumber.length(); i++) {
                    formattedString = asYouTypeFormatter.inputDigit(digitOnlyNumber.charAt(i));
                }

            }
            callToValueTextView.setText(formattedString);
            if (!TextUtils.isEmpty(formattedString)) {
                Bundle bundle = new Bundle(1);
                bundle.putString(SEARCH_CRITERIA, formattedString);
                restartLoader(LOOKUP_LOADER_ID, bundle, this);
            }
        } else {
            asYouTypeFormatter.clear();
            if (addContactButton != null) {
                addContactButton.setVisibility(View.INVISIBLE);
            }
            if (addToExistContactButton != null) {
                addToExistContactButton.setVisibility(View.INVISIBLE);
            }
            if (addContactSeparator != null) {
                addContactSeparator.setVisibility(View.INVISIBLE);
            }
            matchedFullNameTextView.setVisibility(View.INVISIBLE);
            matchedTypeTextView.setVisibility(View.INVISIBLE);
        }
        callToValueTextView.addTextChangedListener(this);
        final String criteria = callToValueTextView.getText().toString();
        callToLabelTextView.setVisibility(!TextUtils.isEmpty(criteria) ? View.VISIBLE : View.INVISIBLE);
        backImageButton.setVisibility(!TextUtils.isEmpty(criteria) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String log = "KeypadTabContent - onCreateLoader";
        log += " - id:";
        log += id;
        Activity activity = getActivity();
        if (activity == null) {
            log += " - activity is null";
            Logger.log(log);
            return null;
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            log += " - invalid permission";
            Logger.log(log);
            return null;
        }
        CursorLoader cursorLoader = null;

        if (id == LOOKUP_LOADER_ID) {
            String criteria = args.getString(SEARCH_CRITERIA);
            String criteriaNorm = null;
            if (criteria != null) {
                criteria = criteria.toUpperCase();
                criteriaNorm = criteria.toUpperCase();
            }
            if (!TextUtils.isEmpty(criteriaNorm)) {
                //log += " - replace[";
                //log += criteriaNorm;
                criteriaNorm = criteriaNorm.replaceAll("[\\+\\-\\(\\) ]", "");
                //log += ", " + criteriaNorm + "]";
            }
            log += " - criteria:";
            log += criteria;
            log += " - criteriaNorm:";
            log += criteriaNorm;
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(criteria));
            cursorLoader = new CursorLoader(getActivity(), uri, null, null, null, null);
            Logger.log("***cursorLoader created");
        } else {
            // handle more loaders.
        }
        Logger.log(log);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        String log = "KeypadTabContent - onLoadFinished";
        int loaderId = -1;
        if (loader != null) {
            loaderId = loader.getId();
        }
        log += " - loaderId:";
        log += loaderId;
        if (loaderId == LOOKUP_LOADER_ID) {
            log += " - lookup loader";
            try {
                if (data == null || data.isClosed()) {
                    log += " - invalid cursor";
                    Logger.log(log);
                    return;
                }
                ArrayList<String> contactIdList = new ArrayList<>();
                data.moveToPosition(-1);
                while (data.moveToNext()) {
                    int contactIdIndex = data.getColumnIndex(CommonDataKinds.Phone.CONTACT_ID);
                    String contactId = null;
                    if (contactIdIndex != -1) {
                        contactId = data.getString(contactIdIndex);
                    }
                    if (!TextUtils.isEmpty(contactId) && !contactIdList.contains(contactId)) {
                        contactIdList.add(contactId);
                    }
                }
                int count = contactIdList.size();
                log += " - count:";
                log += count;
                if (data.moveToFirst()) {
                    int dispalyNameIndex = data.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                    String displayName = null;
                    if (dispalyNameIndex != -1) {
                        displayName = data.getString(dispalyNameIndex);
                    }
                    int typeIndex = data.getColumnIndex(ContactsContract.PhoneLookup.TYPE);
                    int type = -1;
                    try {
                        type = data.getInt(typeIndex);
                    } catch (Exception e) { Logger.log("KeypadTabContent - onLoadFinished - Exception:"+e.toString()); }
                    //PhoneTypeEnum type = null;
                    //if (typeIndex != -1) {
                    //    type = PhoneTypeEnum.getPhoneTypeLabel(data.getInt(typeIndex));
                    //}
                    String typeStr = " - " + (String) CommonDataKinds.Phone.getTypeLabel(getActivity().getResources(),type,"");
                    String labelStr = "";
                    if (type == CommonDataKinds.Phone.TYPE_CUSTOM) {
                        int labelIndex = data.getColumnIndex(ContactsContract.PhoneLookup.LABEL);
                        labelStr = data.getString(labelIndex);
                        if (!TextUtils.isEmpty(labelStr))
                            typeStr = " - " + labelStr;
                    }
                    int phoneNumberIndex = data.getColumnIndex(ContactsContract.PhoneLookup.NUMBER);
                    String phoneNumber = null;
                    if (phoneNumberIndex != -1) {
                        phoneNumber = data.getString(phoneNumberIndex);
                    }
                    int phoneNumberNormIndex = data.getColumnIndex(ContactsContract.PhoneLookup.NORMALIZED_NUMBER);
                    String phoneNumberNorm = null;
                    if (phoneNumberNormIndex != -1) {
                        phoneNumberNorm = data.getString(phoneNumberNormIndex);
                    }
                    log += " - displayName:";
                    log += displayName;
                    log += " - type:";
                    log += type;
                    log += " - phoneNumber:";
                    log += phoneNumber;
                    log += " - phoneNumberNorm:";
                    log += phoneNumberNorm;
                    Logger.log(log);
                    matchedFullNameTextView.setText(displayName);
                    matchedTypeTextView.setText(typeStr);
                    Activity activity = getActivity();
                    if (count == 2) {
                        String str = null;
                        if (activity != null) {
                            str = activity.getString(R.string.keypad_matching_contact_1_more);
                        }
                        matchedTypeTextView.append(str);
                    } else if (count > 2) {
                        String str1 = null;
                        String str2 = null;
                        if (activity != null) {
                            str1 = activity.getString(R.string.keypad_matching_contact_more_part_1);
                            str2 = activity.getString(R.string.keypad_matching_contact_more_part_2);
                        }
                        matchedTypeTextView.append(str1 + (count - 1) + str2);
                    }
                    matchedFullNameTextView.setVisibility(View.VISIBLE);
                    matchedTypeTextView.setVisibility(View.VISIBLE);
                    if (addContactButton != null) {
                        addContactButton.setVisibility(View.INVISIBLE);
                    }
                    if (addToExistContactButton != null) {
                        addToExistContactButton.setVisibility(View.INVISIBLE);
                    }
                    if (addContactSeparator != null) {
                        addContactSeparator.setVisibility(View.INVISIBLE);
                    }
                    /*do {
                        String n = data.getString(LOOKUP_DISPLAY_NAME_PRIMARY_INDEX);
                        PhoneTypeEnum t = PhoneTypeEnum.getPhoneTypeLabel(data.getInt(LOOKUP_TYPE_INDEX));
                        //Log.d(TAG, "***" + n + " " + t.toString());
                    } while (data.moveToNext());*/
                } else {
                    log += " - empty result";
                    Logger.log(log);
                    matchedFullNameTextView.setVisibility(View.INVISIBLE);
                    matchedTypeTextView.setVisibility(View.INVISIBLE);
                    if (addContactButton != null) {
                        addContactButton.setVisibility(View.VISIBLE);
                    }
                    if (addToExistContactButton != null) {
                        addToExistContactButton.setVisibility(View.VISIBLE);
                    }
                    if (addContactSeparator != null) {
                        addContactSeparator.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Throwable t) {
                log += " - throwable";
                Logger.log(log);
            }
        } else {
            // handle more loaders here.
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == LOOKUP_LOADER_ID) {
            matchedFullNameTextView.setText("");
        } else {
            // handle more loaders here.
        }
    }

    @Override
    public void onClick(View v) {
        if (v == null) {
            return;
        }
        int id = v.getId();
        if (v.getId() == backImageButton.getId()) {
            CharSequence currentValue = callToValueTextView.getText();
            if (!TextUtils.isEmpty(currentValue)) {
                callToValueTextView.setText(currentValue.subSequence(0, currentValue.length() - 1));
            }
        } else if (id == R.id.add_contact_button) {
            String str = null;
            if (callToValueTextView != null) {
                CharSequence charSequence = callToValueTextView.getText();
                if (charSequence != null) {
                    str = charSequence.toString();
                }
            }
            if (listener != null) {
                listener.keypadAddNewNumber(str);
            }
        } else if (id == R.id.add_to_exist_contact_button) {
            String str = null;
            if (callToValueTextView != null) {
                CharSequence charSequence = callToValueTextView.getText();
                if (charSequence != null) {
                    str = charSequence.toString();
                }
            }
            if (listener != null) {
                listener.keypadAddToExistContact(str);
            }
        } else if (v.getId() == key1ImageButton.getId()) {
            callToValueTextView.append("1");
        } else if (v.getId() == key2ImageButton.getId()) {
            callToValueTextView.append("2");
        } else if (v.getId() == key3ImageButton.getId()) {
            callToValueTextView.append("3");
        } else if (v.getId() == key4ImageButton.getId()) {
            callToValueTextView.append("4");
        } else if (v.getId() == key5ImageButton.getId()) {
            callToValueTextView.append("5");
        } else if (v.getId() == key6ImageButton.getId()) {
            callToValueTextView.append("6");
        } else if (v.getId() == key7ImageButton.getId()) {
            callToValueTextView.append("7");
        } else if (v.getId() == key8ImageButton.getId()) {
            callToValueTextView.append("8");
        } else if (v.getId() == key9ImageButton.getId()) {
            callToValueTextView.append("9");
        } else if (v.getId() == keyAsteriskImageButton.getId()) {
            callToValueTextView.append("*");
        } else if (v.getId() == key0ImageButton.getId()) {
            callToValueTextView.append("0");
        } else if (v.getId() == keyPoundImageButton.getId()) {
            callToValueTextView.append("#");
        } else if (v.getId() == keyCallImageButton.getId()) {
            String callToNumber = callToValueTextView.getText().toString();
            if (!TextUtils.isEmpty(callToNumber)) {
                listener.keypadCall(callToNumber);
            } else {
                Toast.makeText(getActivity(), "Enter number.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        String log = "KeypadTabContent - onLongClick";
        if (v == null) {
            log += " - view is null";
            Logger.log(log);
            return false;
        }
        int id = v.getId();
        if (id == R.id.back_imagebutton) {
            if (callToValueTextView != null) {
                callToValueTextView.setText("");
            }
            return true;
        } else if (id == R.id.key_0_imagebutton) {
            log += " - 0 button";
            Logger.log(log);
            if (callToValueTextView != null) {
                callToValueTextView.append("+");
                initTimerTask();
            }
            return true;
        } else if (id == R.id.call_to_layout || id == R.id.call_to_result_layout) {
            copyFromClipBoard();
            return true;
        }
        return false;
    }

    private class LocalPhoneNumberFormattingTextWatcher extends PhoneNumberFormattingTextWatcher {
        @Override
        public synchronized void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            String criteria = callToValueTextView.getText().toString();
            if (!TextUtils.isEmpty(criteria)) {
                Bundle bundle = new Bundle(1);
                bundle.putString(SEARCH_CRITERIA, criteria);
                restartLoader(LOOKUP_LOADER_ID, bundle, KeypadTabContent.this);
            }
            callToLabelTextView.setVisibility(!TextUtils.isEmpty(criteria) ? View.VISIBLE : View.INVISIBLE);
            backImageButton.setVisibility(!TextUtils.isEmpty(criteria) ? View.VISIBLE : View.INVISIBLE);
            //addContactButton.setVisibility(View.INVISIBLE);
            //matchedFullNameTextView.setVisibility(View.INVISIBLE);
            //matchedTypeTextView.setVisibility(View.INVISIBLE);
        }
    }

    public interface OnKeypadTabContentListener {
        void keypadAddNewNumber(String newNumber);
        void keypadAddToExistContact(String newNumber);
        void keypadCall(String callToNumber);
    }

    private class LookupCursorAdapter extends CursorAdapter {

        LookupCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View lookupRowItemView = LayoutInflater.from(context).inflate(R.layout.keypad_call_to_result, parent, false);
            return lookupRowItemView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // fullname
            TextView fullNameTextView = (TextView) view.findViewById(R.id.duration_textview);
            fullNameTextView.setText("");
            String fullName = cursor.getString(LOOKUP_DISPLAY_NAME_PRIMARY_INDEX);
            fullNameTextView.setText(fullName);
            // number
            TextView numberTextView = (TextView) view.findViewById(R.id.number_textview);
            numberTextView.setText("");
            String number = cursor.getString(LOOKUP_NUMBER_INDEX);
            SpannableStringBuilder builder = highlight(number);
            numberTextView.setText(number, TextView.BufferType.SPANNABLE);
            // count
            TextView countTextView = (TextView) view.findViewById(R.id.count_textview);
            countTextView.setText("");
            countTextView.setText(String.valueOf(cursor.getCount()));
        }

/*
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view;

                view = LayoutInflater.from(getContext()).inflate(R.layout.keypad_call_to_result_spinner_item, null);
                Phone phone = getItem(position);
                TextView type = (TextView)view.findViewById(R.id.type_textview);
                type.setText(phone.getType().toString());
                TextView number = (TextView)view.findViewById(R.id.number_textview);
                number.setText(phone.getNumber());
                if (phone.isSelected()) {
                    type.setTextColor(ContextCompat.getColor(getContext(), R.color.mainPurple));
                    number.setTextColor(ContextCompat.getColor(getContext(), R.color.mainPurple));
                } else {
                    type.setTextColor(ContextCompat.getColor(getContext(), R.color.textColor));
                    number.setTextColor(ContextCompat.getColor(getContext(), R.color.textColor));
                }
                Log.d(TAG, "setIgnoreSeletionForCalling(false**********************");
                phone.setIgnoreSelectionForCalling(false); // start listening to itemSelectionEvent
            }
            return view;
        }
*/

        private SpannableStringBuilder highlight(CharSequence number) {
            SpannableStringBuilder builder = new SpannableStringBuilder();
            String original = number.toString();
            String originalInLowercase = original.toLowerCase();
            String searchStringInLowercase = callToValueTextView.getText().toString().toLowerCase();
            int startAt = 0;
            int foundAt = -1;
            while ((foundAt = originalInLowercase.indexOf(searchStringInLowercase, startAt)) != -1) {
                if (foundAt > startAt) {
                    SpannableString unmatchedSubString = new SpannableString(original.substring(startAt, foundAt));
                    CharacterStyle greySpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.textColor));
                    unmatchedSubString.setSpan(greySpan, 0, unmatchedSubString.length(), 0);
                    builder.append(unmatchedSubString);
                }
                SpannableString matchedSubString = new SpannableString(original.substring(foundAt, foundAt + searchString.length()));
                CharacterStyle purpleSpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.mainPurple));
                matchedSubString.setSpan(purpleSpan, 0, matchedSubString.length(), 0);
                builder.append(matchedSubString);

                startAt = foundAt + searchString.length();
                if (startAt + searchString.length() > original.length()) {
                    break;
                }
            }
            if (startAt < original.length()) {
                SpannableString unmatchedSubString = new SpannableString(original.substring(startAt, original.length()));
                CharacterStyle greySpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.textColor));
                unmatchedSubString.setSpan(greySpan, 0, unmatchedSubString.length(), 0);
                builder.append(unmatchedSubString);
            }
            /*
            while ((foundAt = original.indexOf(searchString, startAt)) != -1) {
                if (foundAt > startAt) {
                    SpannableString unmatchedSubString = new SpannableString(original.substring(startAt, foundAt));
                    CharacterStyle greySpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.textColor));
                    unmatchedSubString.setSpan(greySpan, ic_key_0, unmatchedSubString.length(), ic_key_0);
                    builder.append(unmatchedSubString);
                }
                SpannableString matchedSubString = new SpannableString(original.substring(foundAt, foundAt + searchString.length()));
                CharacterStyle purpleSpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.mainPurple));
                matchedSubString.setSpan(purpleSpan, ic_key_0, matchedSubString.length(), ic_key_0);
                builder.append(matchedSubString);

                startAt = foundAt + searchString.length();
                if (startAt + searchString.length() > original.length()) {
                    break;
                }
            }
            if (startAt < original.length()) {
                SpannableString unmatchedSubString = new SpannableString(original.substring(startAt, original.length()));
                CharacterStyle greySpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.textColor));
                unmatchedSubString.setSpan(greySpan, ic_key_0, unmatchedSubString.length(), ic_key_0);
                builder.append(unmatchedSubString);
            }
            */
            return builder;
        }
    }

    private void copyFromClipBoard() {
        String log = "KeypadTabContent - copyFromClipBoard";
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            log += " - invalid state;";
            Logger.log(log);
            return;
        }
        try {
            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard == null) {
                log += " - clipboardManager is null";
                Logger.log(log);
                return;
            }
            String cpyText = null;
            ClipData clipData = null;
            ClipData.Item item = null;
            CharSequence charSequence = null;
            boolean hasPrimaryClip = clipboard.hasPrimaryClip();
            if (hasPrimaryClip) {
                clipData = clipboard.getPrimaryClip();
            }
            if (clipData != null) {
                try {
                    item = clipData.getItemAt(0);
                } catch (Throwable t) {
                }
            }
            if (item != null) {
                charSequence = item.getText();
            }
            if (charSequence != null) {
                cpyText = charSequence.toString();
            }
            log += " - cpyText:";
            log += cpyText;
            if (cpyText == null || callToValueTextView == null) {
                log += " - cpyText/view is null";
                Logger.log(log); //dals+lds1|1:8;5\9}3-5"1/0{1'9%&*=0\12`~4
                return;
            }
            cpyText = cpyText.replaceAll("[a-zA-Z]", "");
            cpyText = cpyText.replaceAll("[-.^:\\;>\"\'<,\\\\()&=%?@_!`|~\\[\\]{}]", "");
            log += " - remove alphabet:";
            log += cpyText;
            Logger.log(log);
            callToValueTextView.append(cpyText);
        } catch (Throwable t) {
            log += " - throwable";
            Logger.log(log);
        }
    }

    private synchronized void initTimerTask() {
        String log = "KeypadTabContent - initTimerTask";
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            log += " - invalid state";
            Logger.log(log);
            return;
        }
        if (timer != null) {
            log += " - timer exist abort";
            Logger.log(log);
            return;
        }
        log += " - create new timer";
        Logger.log(log);
        timer = new Timer();
        timer.schedule(new KeypadTimerTask(), 0, 1000);
    }

    private class KeypadTimerTask extends TimerTask {
        @Override
        public synchronized void run() {
            String log = "KeypadTabContent - KeypadTimerTask - run";
            Activity activity = getActivity();
            if (activity == null || activity.isFinishing()) {
                log += " - invalid state";
                Logger.log(log);
                return;
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String log = "KeypadTabContent - KeypadTimerTask - runOnUiThread";
                    try {
                        if (key0ImageButton == null) {
                            log += " - button is null";
                            Logger.log(log);
                            return;
                        }
                        boolean currentPress = key0ImageButton.isPressed();
                        log += " - currentPress:";
                        log += currentPress;
                        Logger.log(log);
                        if (currentPress) {
                            key0ImageButton.setChecked(true);
                        } else {
                            key0ImageButton.setChecked(false);
                            cancel();
                            if (timer != null) {
                                timer.cancel();
                                timer.purge();
                            }
                            timer = null;
                        }
                    } catch (Throwable t) {
                        log += " - Throwable";
                        Logger.log(log);
                    }

                }
            });
        }
    }

    private synchronized void restartLoader(int id, Bundle args, LoaderManager.LoaderCallbacks<Cursor> callback) {
        String log = "KeypadTabContent  - restartLoader";
        log += " - id:";
        log += id;
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            log += " - activity is null/invalide state";
            Logger.log(log);
            return;
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            log += " - invalid permission";
            Logger.log(log);
            return;
        }
        LoaderManager loaderManager = getLoaderManager();
        if (loaderManager == null) {
            log += " - loaderManager is null";
            Logger.log(log);
            return;
        }
        try {
            loaderManager.restartLoader(id, args, callback);
        } catch (Throwable t) {
            log += " - Throwable";
            Logger.log(log);
        }
    }
}

package com.picup.calling;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import com.picup.calling.base.ExpandableHeightListView;
import com.picup.calling.util.Logger;
import com.picup.calling.util.Utililites;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.Data;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.picup.calling.R;

import java.util.ArrayList;
import java.util.HashMap;

public final class ContactProfileContactTabContent extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ContactProfileContactTabContent.class.getSimpleName();

    private static final int REQUEST_CODE_EDIT_PROFILE = 1;
    private static final int REQUEST_CODE_DELETE_PROFILE = 2;

    private static final String ARG_CONTACT_ID = "android.idt.net.com.picup.calling.ContactProfileContactTabContent.CONTACT_ID";
    private static final String ARG_ALREADY_DISPLAYED_PHONE_ID = "android.idt.net.com.picup.calling.ContactProfileContactTabContent.ALREADY_DISPLAYED_PHONE_ID";
    private static final String ARG_ALREADY_DISPLAYED_NORMALIZED_NUMBER = "android.idt.net.com.picup.calling.NORMALIZED_NUMBER";

    /*private static final int NON_PRIMARY_PHONES_LOADER_ID = 0;
    private static final String[] NON_PRIMARY_PHONES_PROJECTION = {CommonDataKinds.Phone._ID, CommonDataKinds.Phone.LOOKUP_KEY, CommonDataKinds.Phone.NUMBER, CommonDataKinds.Phone.TYPE, CommonDataKinds.Phone.LABEL};
    //private static final String[] PHONES_FROM_COLUMNS = {CommonDataKinds.Phone._ID, CommonDataKinds.Phone.LOOKUP_KEY, CommonDataKinds.Phone.NUMBER, CommonDataKinds.Phone.TYPE};
    //private static final int[] PHONES_TO_IDS = {R.id.phone_number_textview};
    private static final int NON_PRIMARY_PHONES_INDEX_ID = 0;
    private static final int NON_PRIMARY_PHONES_LOOKUP_INDEX = 1;
    private static final int NON_PRIMARY_PHONES_NUMBER_INDEX = 2;
    private static final int NON_PRIMARY_PHONES_TYPE_INDEX = 3;
    private static final int NON_PRIMARY_PHONES_LABEL_INDEX = 4;
    */

    private static final int EMAIL_LOADER_ID = 1;
    private static final String[] EMAILS_PROJECTION = {CommonDataKinds.Email._ID, CommonDataKinds.Email.LOOKUP_KEY, CommonDataKinds.Email.ADDRESS, CommonDataKinds.Email.TYPE, CommonDataKinds.Email.LABEL};
    private final static String[] EMAILS_FROM_COLUMNS = {CommonDataKinds.Email.ADDRESS};
    private final static int[] EMAILS_TO_IDS = {R.id.address_textview};
    private final static int EMAILS_TYPE_INDEX = 3;
    private final static int EMAILS_LABEL_INDEX = 4;


    private static final int COMPANY_LOADER_ID = 2;
    private static final String[] COMPANIES_PROJECTION = {CommonDataKinds.Organization._ID, CommonDataKinds.Organization.LOOKUP_KEY, Organization.MIMETYPE, CommonDataKinds.Organization.COMPANY};
    private final static String[] COMPANIES_FROM_COLUMNS = {CommonDataKinds.Organization.COMPANY};
    private final static int[] COMPANIES_TO_IDS = {R.id.company_textview};

    private static final int PHONES_LOADER_ID = 3;
    private static final String[] PHONES_PROJECTION = {CommonDataKinds.Phone._ID, CommonDataKinds.Phone.LOOKUP_KEY, CommonDataKinds.Phone.NUMBER, CommonDataKinds.Phone.NORMALIZED_NUMBER, CommonDataKinds.Phone.TYPE, CommonDataKinds.Phone.LABEL};
    //private static final String[] PHONES_FROM_COLUMNS = {CommonDataKinds.Phone._ID, CommonDataKinds.Phone.LOOKUP_KEY, CommonDataKinds.Phone.NUMBER, CommonDataKinds.Phone.TYPE};
    //private static final int[] PHONES_TO_IDS = {R.id.phone_number_textview};
    private static final int PHONES_ID_INDEX = 0;
    private static final int PHONES_LOOKUP_INDEX = 1;
    private static final int PHONES_NUMBER_INDEX = 2;
    private static final int PHONES_TYPE_INDEX = 4;
    private static final int PHONES_LABEL_INDEX = 5;

    private int contactId = 0;
    private long alreadyDisplayedPhoneId = 0;
    private String alreadyDisplayedNormalizeNumber;

    //private LinearLayout additionalNumberLayout = null;
    private TextView additionalNumberTextView = null;
    private ExpandableHeightListView additionalNumberListView = null;
    private CursorAdapter additionalNumberCursorAdapter = null;

    //private LinearLayout additionalEmailLayout = null;
    private TextView additionalemailTextView = null;
    private ExpandableHeightListView additionalEmailListView = null;
    private CursorAdapter additionalEmailCursorAdapter = null;

    private ExpandableHeightListView additionalInfoCompanyListView = null;
    private CursorAdapter additionalInfoCompanyCursorAdapter = null;

    private OnContactProfileContactTabContentListener listener;

    public ContactProfileContactTabContent() {
        // Required empty public constructor
    }

    public static ContactProfileContactTabContent newInstance(int contactId, long alreadyDisplayedPhoneId, String normalizeNumber) {
        ContactProfileContactTabContent fragment = new ContactProfileContactTabContent();
        Bundle args = new Bundle(1);
        args.putInt(ARG_CONTACT_ID, contactId);
        args.putLong(ARG_ALREADY_DISPLAYED_PHONE_ID, alreadyDisplayedPhoneId);
        args.putString(ARG_ALREADY_DISPLAYED_NORMALIZED_NUMBER, normalizeNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Logger.log("ContactProfileContactTabContent - onAttach");
        if (context instanceof OnContactProfileContactTabContentListener) {
            listener = (OnContactProfileContactTabContentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnContactProfileContactTabContentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.log("ContactProfileContactTabContent - onCreate");
        Bundle args = getArguments();
        if (args != null) {
            contactId = args.getInt(ARG_CONTACT_ID);
            alreadyDisplayedPhoneId = args.getLong(ARG_ALREADY_DISPLAYED_PHONE_ID);
            alreadyDisplayedNormalizeNumber = args.getString(ARG_ALREADY_DISPLAYED_NORMALIZED_NUMBER);
            Logger.log("ContactProfileContactTabContent - onCreate - contactId:"+contactId+" alreadyDisplayedPhoneId:"+alreadyDisplayedPhoneId+" alreadyDisplayedNormalizeNumber:"+alreadyDisplayedNormalizeNumber);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.log("ContactProfileContactTabContent - onCreateView");
        View contactProfileContactTabContentView = null;
        if (inflater != null) {
            try {
                contactProfileContactTabContentView = inflater.inflate(R.layout.contact_profile_contact_tab_content, container, false);
            } catch (Throwable t) {
            }
        }
        if (contactProfileContactTabContentView != null) {
            //additionalNumberLayout = (LinearLayout)contactProfileContactTabContentView.findViewById(R.id.additional_numbers_layout);
            additionalNumberTextView = (TextView) contactProfileContactTabContentView.findViewById(R.id.additional_numbers_textview);
            additionalNumberListView = (ExpandableHeightListView) contactProfileContactTabContentView.findViewById(R.id.additional_numbers_listview);
            additionalNumberListView.setExpanded(true);
            //additionalEmailLayout = (LinearLayout)contactProfileContactTabContentView.findViewById(R.id.additional_email_layout);
            additionalemailTextView = (TextView) contactProfileContactTabContentView.findViewById(R.id.email_address_textview);
            additionalEmailListView = (ExpandableHeightListView) contactProfileContactTabContentView.findViewById(R.id.additional_email_listview);
            additionalEmailListView.setExpanded(true);
            //additionalEmailListView.setEmptyView(inflater.inflate(R.layout.missing_view, null));
            //additionalEmailListView.setMinimumHeight(30);
            additionalInfoCompanyListView = (ExpandableHeightListView) contactProfileContactTabContentView.findViewById(R.id.additional_info_company_name_listview);
            additionalInfoCompanyListView.setExpanded(true);
        }
        return contactProfileContactTabContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.log("ContactProfileContactTabContent - onActivityCreated");
        additionalNumberCursorAdapter = new PhoneCursorAdapter(getActivity(), null, 0);
        additionalNumberListView.setAdapter(additionalNumberCursorAdapter);

/*
        Bundle args = new Bundle(1);
        args.putInt(ARG_CONTACT_ID, contactId);
        getLoaderManager().initLoader(NON_PRIMARY_PHONES_LOADER_ID, args, this);
*/

        additionalEmailCursorAdapter = new EmailCursorAdapter(getActivity(), R.layout.contact_profile_additional_email_view, null, EMAILS_FROM_COLUMNS, EMAILS_TO_IDS, 0);
        //additionalEmailListView.setEmptyView(getActivity().findViewById(R.id.missing_textview));
        additionalEmailListView.setAdapter(additionalEmailCursorAdapter);

        Bundle args2 = new Bundle(1);
        args2.putInt(ARG_CONTACT_ID, contactId);
        getLoaderManager().initLoader(EMAIL_LOADER_ID, args2, this);

        additionalInfoCompanyCursorAdapter = new CompanyCursorAdapter(getActivity(), R.layout.contact_profile_additional_company_name_view, null, COMPANIES_FROM_COLUMNS, COMPANIES_TO_IDS, 0);
        additionalInfoCompanyListView.setAdapter(additionalInfoCompanyCursorAdapter);
        Bundle args3 = new Bundle(1);
        args3.putInt(ARG_CONTACT_ID, contactId);
        //getLoaderManager().initLoader(COMPANY_LOADER_ID, args3, this); // postponed

        Bundle args4 = new Bundle(1);
        args4.putInt(ARG_CONTACT_ID, contactId);
        args4.putLong(ARG_ALREADY_DISPLAYED_PHONE_ID, alreadyDisplayedPhoneId);
        args4.putString(ARG_ALREADY_DISPLAYED_NORMALIZED_NUMBER, alreadyDisplayedNormalizeNumber);
        Logger.log("ContactProfileContactTabContent - onActivityCreated - set loader - contactId:"+contactId+" alreadyDisplayedPhoneId:"+alreadyDisplayedPhoneId+" alreadyDisplayedNormalizeNumber:"+alreadyDisplayedNormalizeNumber);
        getLoaderManager().initLoader(PHONES_LOADER_ID, args4, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.log("ContactProfileContactTabContent - onResume");
/*
        Bundle args2 = new Bundle(1);
        args2.putInt(ARG_CONTACT_ID, contactId);
        getLoaderManager().initLoader(EMAIL_LOADER_ID, args2, this);

        Bundle args4 = new Bundle(1);
        args4.putInt(ARG_CONTACT_ID, contactId);
        args4.putLong(ARG_ALREADY_DISPLAYED_PHONE_ID, alreadyDisplayedPhoneId);
        getLoaderManager().initLoader(PHONES_LOADER_ID, args4, this);
*/
    }

    public void setAlreadyDisplayedPhoneId(long alreadyDisplayedPhoneId) {
        this.alreadyDisplayedPhoneId = alreadyDisplayedPhoneId;
    }

    public void restartLoaders(long alreadyDisplayedPhoneId, String alreadyDisplayedNormalizeNumber) {
        Logger.log("ContactProfileContactTabContent - restartLoaders");
        this.alreadyDisplayedPhoneId = alreadyDisplayedPhoneId;
        this.alreadyDisplayedNormalizeNumber = alreadyDisplayedNormalizeNumber;
        Bundle args2 = new Bundle(1);
        args2.putInt(ARG_CONTACT_ID, contactId);
        getLoaderManager().restartLoader(EMAIL_LOADER_ID, args2, this);

        Bundle args4 = new Bundle(1);
        args4.putInt(ARG_CONTACT_ID, contactId);
        args4.putLong(ARG_ALREADY_DISPLAYED_PHONE_ID, alreadyDisplayedPhoneId);
        args4.putString(ARG_ALREADY_DISPLAYED_NORMALIZED_NUMBER, alreadyDisplayedNormalizeNumber);
        getLoaderManager().restartLoader(PHONES_LOADER_ID, args4, this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Logger.log("ContactProfileContactTabContent - onCreateLoader - id:"+id);
        CursorLoader cursorLoader = null;

        switch (id) {
           /* case NON_PRIMARY_PHONES_LOADER_ID:
                if (args != null) {
                    int contactId = args.getInt(ARG_CONTACT_ID);
                    if (contactId > 0) {
                        String selection = CommonDataKinds.Phone.CONTACT_ID + " = ? AND " + CommonDataKinds.Phone.IS_PRIMARY + " = ? "; // non-primary numbers
                        String[] selectionArgs = new String[]{String.valueOf(contactId), String.valueOf(0)};
                        cursorLoader = new CursorLoader(getActivity(), CommonDataKinds.Phone.CONTENT_URI, NON_PRIMARY_PHONES_PROJECTION, selection, selectionArgs, null);
                    }
                }
                break;*/
            case EMAIL_LOADER_ID:
                if (args != null) {
                    int contactId = args.getInt(ARG_CONTACT_ID);
                    if (contactId > 0) {
                        String selection = CommonDataKinds.Email.CONTACT_ID + " = ? ";
                        String[] selectionArgs = new String[]{String.valueOf(contactId)};
                        cursorLoader = new CursorLoader(getActivity(), CommonDataKinds.Email.CONTENT_URI, EMAILS_PROJECTION, selection, selectionArgs, CommonDataKinds.Email.ADDRESS);
                    }
                }
                break;
            case COMPANY_LOADER_ID:
                if (contactId > 0) {
                    String selection = Data.CONTACT_ID + " = ? ";
                    //String selection = CommonDataKinds.Organization.CONTACT_ID + " = ? ";
                    String[] selectionArgs = new String[]{String.valueOf(contactId)};
                    cursorLoader = new CursorLoader(getActivity(), Data.CONTENT_URI, COMPANIES_PROJECTION, selection, selectionArgs, null);
                }
                break;
            case PHONES_LOADER_ID:
                if (args != null) {
                    String selection;
                    int contactId = args.getInt(ARG_CONTACT_ID);
                    if (contactId > 0) {
                        ArrayList<String> selectionArgsArray = new ArrayList<>();
                        selectionArgsArray.add(String.valueOf(contactId));
                        selection = CommonDataKinds.Phone.CONTACT_ID + " = ? ";
                        long alreadyDisplayedPhoneId = args.getLong(ARG_ALREADY_DISPLAYED_PHONE_ID);
                        String alreadyDisplayNormalizeNumber = args.getString(ARG_ALREADY_DISPLAYED_NORMALIZED_NUMBER);
                        if (alreadyDisplayedPhoneId > 0) {
                            selection += (" AND " + CommonDataKinds.Phone._ID + " <> ? "); // except alreadyDisplayedPhoneId
                            selectionArgsArray.add(String.valueOf(alreadyDisplayedPhoneId));
                        }
                        if (!TextUtils.isEmpty(alreadyDisplayNormalizeNumber)) {
                            selection += " AND " + CommonDataKinds.Phone.NORMALIZED_NUMBER + " != ?";
                            selectionArgsArray.add(alreadyDisplayNormalizeNumber);
                        }
                        String[] selectionArgs = Utililites.arrayList2array(selectionArgsArray);
                        cursorLoader = new CursorLoader(getActivity(), CommonDataKinds.Phone.CONTENT_URI, PHONES_PROJECTION, selection, selectionArgs, null);
                    }
                }
                break;
            default:
                // handle more loaders here
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Logger.log("ContactProfileContactTabContent - onLoadFinished");
        int loaderId = -1;
        if (loader != null) {
            loaderId = loader.getId();
        }
        Logger.log("ContactProfileContactTabContent - onLoadFinished - loaderId:"+loaderId);
        /*if (loaderId == NON_PRIMARY_PHONES_LOADER_ID) {
            ArrayList<String> filterColumns = new ArrayList<>();
            filterColumns.add(CommonDataKinds.Phone.NORMALIZED_NUMBER);
            Cursor filterCursor = getFilterCursor(data, filterColumns);
            if (additionalNumberCursorAdapter != null) {
                additionalNumberCursorAdapter.swapCursor(filterCursor);
            }
            int count = -1;
            if (filterCursor != null) {
                count = filterCursor.getCount();
            }
            Logger.log("ContactProfileContactTabContent - onLoadFinished - filter count="+count);
            //additionalNumberLayout.setVisibility(data.getCount() > 0 ? View.VISIBLE : View.GONE);
            if (additionalNumberTextView != null) {
                additionalNumberTextView.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            }
            if (additionalNumberListView != null) {
                additionalNumberListView.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            }
        } else */ if (loaderId == EMAIL_LOADER_ID) {
            if (additionalEmailCursorAdapter != null) {
                additionalEmailCursorAdapter.swapCursor(data);
            }
            //UNITE-1827
            int visibility = View.VISIBLE;
            int size = 0;
            if (data != null) {
                size = data.getCount();
            }
            if (size == 0) {
                visibility = View.GONE;
            }
            if (additionalemailTextView != null) {
                additionalemailTextView.setVisibility(visibility);
            }
            if (additionalEmailListView != null) {
                additionalEmailListView.setVisibility(visibility);
            }
        } else if (loaderId == COMPANY_LOADER_ID) {
            if (additionalInfoCompanyCursorAdapter != null) {
                additionalInfoCompanyCursorAdapter.swapCursor(data);
            }
        } else if (loaderId == PHONES_LOADER_ID) {
            ArrayList<String> filterColumns = new ArrayList<>();
            filterColumns.add(CommonDataKinds.Phone.NORMALIZED_NUMBER);
            Logger.log("ContactProfileContactTabContent - onLoadFinished - data count="+data.getCount());
            Cursor filterCursor = getFilterCursor(data, filterColumns);
            if (additionalNumberCursorAdapter != null) {
                additionalNumberCursorAdapter.swapCursor(filterCursor);
            }
            int count = -1;
            if (filterCursor != null) {
                count = filterCursor.getCount();
            }
            Logger.log("ContactProfileContactTabContent - onLoadFinished - filter count="+count);
            if (additionalNumberTextView != null) {
                additionalNumberTextView.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            }
            if (additionalNumberListView != null) {
                additionalNumberListView.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            }
        } else {
            // handle more loaders here.
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Logger.log("ContactProfileContactTabContent - onLoaderReset");
       /* if (loader.getId() == NON_PRIMARY_PHONES_LOADER_ID) {
            additionalNumberCursorAdapter.swapCursor(null);
        } else */if (loader.getId() == EMAIL_LOADER_ID) {
            additionalEmailCursorAdapter.swapCursor(null);
        } else if (loader.getId() == COMPANY_LOADER_ID) {
            additionalInfoCompanyCursorAdapter.swapCursor(null);
        } else if (loader.getId() == PHONES_LOADER_ID) {
            additionalNumberCursorAdapter.swapCursor(null);
        } else {
            // handle more loaders here.
        }
    }

    @Override
    public void onClick(View v) {
        Logger.log("ContactProfileContactTabContent - onClick");

    }

    public interface OnContactProfileContactTabContentListener {
        void contactProfileCall(String callToNumber);
    }

    private class PhoneCursorAdapter extends CursorAdapter implements View.OnClickListener {
        public PhoneCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view  = LayoutInflater.from(context).inflate(R.layout.contact_profile_additional_number_view, null);
            return view;
        }

        @Override
        public void bindView(View view, Context context, final Cursor cursor) {
            Logger.log("ContactProfileContactTabContent - bindView");
            if (cursor == null || cursor.isClosed()) {
                return;
            }
            Logger.log("ContactProfileContactTabContent - bindView - count:"+cursor.getCount());
            int numberIndex = PHONES_NUMBER_INDEX; //cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER);
            String number = null;
            if (numberIndex != -1) {
                number = cursor.getString(numberIndex);
            }
            if (!TextUtils.isEmpty(number)) {
                try {
                    number = PhoneNumberUtils.formatNumber(number);
                } catch (Throwable t) {
                }
            }
            TextView numberTextView = (TextView)view.findViewById(R.id.phone_number_textview);
            numberTextView.setText(number);
            int typeIndex = PHONES_TYPE_INDEX; //cursor.getColumnIndex(CommonDataKinds.Phone.TYPE);
            int type = -1;
            try {
                if (typeIndex != -1) {
                    type = cursor.getInt(typeIndex);
                }
            } catch (Exception e) { Logger.log("ContactProfileContactTabContent - bindView - exception:"+e.toString());}
            int labelIndex = PHONES_LABEL_INDEX; //cursor.getColumnIndex(CommonDataKinds.Phone.LABEL);
            String customLabel = null;
            if (labelIndex != -1) {
                customLabel = cursor.getString(labelIndex);
            }
            //String label = PhoneTypeEnum.getPhoneTypeLabel(type).toString();
            String typeLabel = (String) CommonDataKinds.Phone.getTypeLabel(getActivity().getResources(),type,"");
            TextView typeTextView = (TextView)view.findViewById(R.id.phone_type_textview);
            if (type == CommonDataKinds.Phone.TYPE_CUSTOM && !TextUtils.isEmpty(customLabel))
                typeTextView.setText(customLabel);
            else
                typeTextView.setText(typeLabel);
            Logger.log("ContactProfileContactTabContent - bindView - typeLabel:"+typeLabel+" customLabel:"+customLabel+" labelIndex:"+labelIndex+" typeIndex:"+typeIndex+" type:"+type);

            ImageView phoneImageView = (ImageView)view.findViewById(R.id.call_imageview);
            phoneImageView.setTag(cursor.getPosition());
            phoneImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Logger.log("ContactProfileContactTabContent - PhoneCursorAdapter - onClick");
            if (v instanceof ImageView) {
                int position = (int)v.getTag();
                Cursor cursor = (Cursor)getItem(position);
                listener.contactProfileCall(cursor.getString(PHONES_NUMBER_INDEX));
            }
        }
    }

    private class EmailCursorAdapter extends SimpleCursorAdapter {

        public EmailCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public void bindView(View view, Context context, final Cursor cursor) {
            super.bindView(view, context, cursor);
            //EmailTypeEnum type = EmailTypeEnum.getEmailTypeLabel(cursor.getInt(EMAILS_TYPE_INDEX));
            int type = -1;
            try {
                type = cursor.getInt(EMAILS_TYPE_INDEX);
            } catch (Exception e) { Logger.log("ContactProfileContactTabContent - bindView - Exception:"+e.toString()); }
            String customLabel = cursor.getString(EMAILS_LABEL_INDEX);
            String typeLabel = (String) CommonDataKinds.Email.getTypeLabel(getActivity().getResources(),type,"");

            TextView typeTextView = (TextView)view.findViewById(R.id.type_textview);
            if (type == CommonDataKinds.Email.TYPE_CUSTOM && !TextUtils.isEmpty(customLabel))
                typeTextView.setText(customLabel);
            else
                typeTextView.setText(typeLabel);
        }
    }

    private class CompanyCursorAdapter extends SimpleCursorAdapter {

        public CompanyCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view  = LayoutInflater.from(context).inflate(R.layout.contact_profile_additional_company_name_view, null);
            return view;
        }

        @Override
        public void bindView(View view, Context context, final Cursor cursor) {
            //Log.d(TAG, "************" + cursor.getString(2));
/*
            String number = cursor.getString(NON_PRIMARY_PHONES_NUMBER_INDEX);
            TextView numberTextView = (TextView)view.findViewById(R.id.phone_number_textview);
            numberTextView.setText(number);
            int type = cursor.getInt(NON_PRIMARY_PHONES_TYPE_INDEX);
            String label = PhoneTypeEnum.getPhoneTypeLabel(type).toString();
            TextView typeTextView = (TextView)view.findViewById(R.id.phone_type_textview);
            typeTextView.setText(label);
            ImageView phoneImageView = (ImageView)view.findViewById(R.id.call_imageview);
            phoneImageView.setTag(cursor.getPosition());
*/
            //phoneImageView.setOnClickListener(this);
        }

/*
        @Override
        public void bindView(View view, Context context, final Cursor cursor) {
            // replace your own logic if having customized bindings other than just textviews
            super.bindView(view, context, cursor);
        }
*/
    }

    private synchronized Cursor getFilterCursor(Cursor cursor, ArrayList<String> uniqueColumns) {
        String log = "ContactProfileContactTabContent - getFilterCursor";
        if (cursor == null || cursor.isClosed()) {
            log += " - cursor is null or close";
            Logger.log(log);
            return cursor;
        }
        HashMap<String, ArrayList<String>> uniqueTable = new HashMap<>();
        if (uniqueColumns != null) {
            for (String column : uniqueColumns) {
                Logger.log("ContactProfileContactTabContent - getFilterCursor - column:"+column);
                if (TextUtils.isEmpty(column)) {
                    continue;
                }
                uniqueTable.put(column, new ArrayList<String>());
            }
        }
        MatrixCursor newCursor = null;
        try {
            String[] columns = cursor.getColumnNames();
            if (columns == null || columns.length < 1) {
                return cursor;
            }

            if (cursor.moveToFirst()) {
                newCursor = new MatrixCursor(columns, 1);

                while (!cursor.isAfterLast()) {
                    boolean isUnique = true;
                    if (uniqueColumns != null && !uniqueColumns.isEmpty()) {
                        isUnique = false;
                        for (String column : uniqueColumns) {
                            if (TextUtils.isEmpty(column)) {
                                continue;
                            }
                            ArrayList<String> list = uniqueTable.get(column);
                            if (list == null) {
                                continue;
                            }
                            int index = cursor.getColumnIndex(column);
                            String value = null;
                            if (index != -1) {
                                value = cursor.getString(index);
                            }
                            if (TextUtils.isEmpty(value)) {
                                //if no value then default to isUnique to true
                                isUnique = true;
                                continue;
                            }
                            if (!list.contains(value)) {
                                list.add(value);
                                uniqueTable.put(column, list);
                                isUnique = true;
                            }
                        }
                    }
                    if (isUnique) {
                        MatrixCursor.RowBuilder b = newCursor.newRow();
                        for (String col : columns) {
                            if (TextUtils.isEmpty(col)) {
                                continue;
                            }
                            Logger.log("ContactProfileContactTabContent - getFilterCursor - col:" + col);
                            int index = cursor.getColumnIndex(col);
                            String value = null;
                            if (index != -1) {
                                value = cursor.getString(index);
                            }
                            b.add(value);
                        }
                    }
                    cursor.moveToNext();
                }
            }
        } catch (Throwable t) {
            log += " - Throwable";
            Logger.log(log);
            Logger.logThrowable(t);
        }
        return newCursor;
    }
}

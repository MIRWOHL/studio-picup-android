package com.picup.calling;

import android.content.Context;
import android.database.Cursor;
import com.picup.calling.base.ExpandableHeightListView;
import com.picup.calling.data.Phone;
import com.picup.calling.util.Logger;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import android.provider.ContactsContract.CommonDataKinds;
import android.widget.TextView;

import com.picup.calling.R;

public class PopupContactDetailPhoneTabContent extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_PHONES = "android.idt.net.com.picup.calling.adapter.PopupContactDetailPhoneTabContent.PHONES";

    private static final String ARG_CONTACT_ID = "android.idt.net.com.picup.calling.PopupContactDetailPhoneTabContent.CONTACT_ID";
    private static final int PHONE_LOADER_ID = 0;
    private static final String[] PHONES_FROM_COLUMNS = {CommonDataKinds.Phone._ID, CommonDataKinds.Phone.LOOKUP_KEY, CommonDataKinds.Phone.NUMBER, CommonDataKinds.Phone.TYPE, CommonDataKinds.Phone.LABEL};
    private static final int[] PHONES_TO_IDS = {R.id.number_textview};
    private static final String[] PHONES_PROJECTION = {CommonDataKinds.Phone._ID, CommonDataKinds.Phone.LOOKUP_KEY, CommonDataKinds.Phone.NUMBER, CommonDataKinds.Phone.TYPE, CommonDataKinds.Phone.LABEL};

    private static final int PHONE_ID_INDEX = 0;
    private static final int PHONE_LOOKUP_KEY_INDEX = 1;
    private static final int PHONE_NUMBER_INDEX = 2;
    private static final int PHONE_TYPE_INDEX = 3;
    private static final int PHONE_LABEL_INDEX = 4;

    private int contactId = 0;

    private ExpandableHeightListView phoneListView = null;
    private CursorAdapter phoneCursorAdapter = null;

    private List<Phone> phones = new ArrayList<>();
    private ArrayAdapter<Phone> adapter = null;

    private OnContactDetailPhoneTabContentListener listener;

    public PopupContactDetailPhoneTabContent() {
        // Required empty public constructor
    }

    public static PopupContactDetailPhoneTabContent newInstance(int contactId) {
        PopupContactDetailPhoneTabContent fragment = new PopupContactDetailPhoneTabContent();
        Bundle args = new Bundle(1);
        args.putInt(ARG_CONTACT_ID, contactId);
        fragment.setArguments(args);
        return fragment;
    }

    public static PopupContactDetailPhoneTabContent newInstance(List<Phone> phones) {
        PopupContactDetailPhoneTabContent fragment = new PopupContactDetailPhoneTabContent();
        if (phones != null && !phones.isEmpty()) {
            Bundle args = new Bundle();
            args.putParcelableArrayList(ARG_PHONES, (ArrayList<Phone>)phones);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            contactId = getArguments().getInt(ARG_CONTACT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contactDetailPhoneTabContentView = inflater.inflate(R.layout.popup_contact_detail_phone_tab_content, container, false);
        phoneListView = (ExpandableHeightListView)contactDetailPhoneTabContentView.findViewById(R.id.contact_detail_phone_listview);
        phoneListView.setExpanded(true);
        return contactDetailPhoneTabContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        phoneCursorAdapter = new PhoneCursorAdapter(getActivity(), null, 0);
        phoneListView.setAdapter(phoneCursorAdapter);
        Bundle args = new Bundle(1);
        args.putInt(ARG_CONTACT_ID, contactId);
        getLoaderManager().initLoader(PHONE_LOADER_ID, args, this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnContactDetailPhoneTabContentListener) {
            listener = (OnContactDetailPhoneTabContentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnContactProfileContactTabContentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        FragmentManager fm = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = null;

        if (id == PHONE_LOADER_ID) {
            if (args != null) {
                long contactId = args.getInt(ARG_CONTACT_ID);
                // don't serve address(s) if contactId is missing
                if (contactId > 0) {
                    String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? ";
                    String[] selectionArgs = new String[]{String.valueOf(contactId)};
                    cursorLoader = new CursorLoader(getActivity(), CommonDataKinds.Phone.CONTENT_URI, PHONES_PROJECTION, selection, selectionArgs, null);
                }
            }
        } else {
            // handle more loaders here
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == PHONE_LOADER_ID) {
            phoneCursorAdapter.swapCursor(data);
        } else {
            // handle more loaders here.
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == PHONE_LOADER_ID) {
            phoneCursorAdapter.swapCursor(null);
        } else {
            // handle more loaders here.
        }
    }

    public interface OnContactDetailPhoneTabContentListener {
        void contactDetailCall(String callToNumber);
    }

    private class PhoneCursorAdapter extends CursorAdapter {
        public PhoneCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.popup_contact_detail_phone_tab_content_item, null);
            return view;
        }

        @Override
        public void bindView(View view, Context context, final Cursor cursor) {
            final String number = cursor.getString(PHONE_NUMBER_INDEX);
            TextView numberTextView = (TextView)view.findViewById(R.id.number_textview);
            numberTextView.setText(number);
            int type = -1;
            try {
                type = cursor.getInt(PHONE_TYPE_INDEX);
            } catch (Exception e) { Logger.log("PopupContactDetailPhoneTabContent - bindView - Exception:"+e.toString()); }
            String typeLabel = (String) CommonDataKinds.Phone.getTypeLabel(getActivity().getResources(),type,"");
            String customLabel = cursor.getString(PHONE_LABEL_INDEX);
            TextView typeTextView = (TextView)view.findViewById(R.id.type_textview);
            if (type == CommonDataKinds.Phone.TYPE_CUSTOM && !TextUtils.isEmpty(customLabel))
                typeTextView.setText(customLabel);
            else
                typeTextView.setText(typeLabel);
            ImageView iconImageView = (ImageView)view.findViewById(R.id.icon_imageview);
            iconImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.contactDetailCall(number);
                }
            });
        }
    }
}

package com.picup.calling;

import android.content.Context;
import android.database.Cursor;
import com.picup.calling.base.ExpandableHeightListView;
import com.picup.calling.data.Email;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.picup.calling.R;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnContactDetailEmailTabContentListener} interface
 * to handle interaction events.
 * Use the {@link PopupContactDetailEmailTabContent#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PopupContactDetailEmailTabContent extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_EMAILS = "android.idt.net.com.picup.calling.PopupContactDetailEmailTabContent.EMAILS";
    private static final String ARG_CONTACT_ID = "android.idt.net.com.picup.calling.PopupContactDetailEmailTabContent.CONTACT_ID";

    private static final int EMAIL_LOADER_ID = 0;

    private final static String[] EMAILS_FROM_COLUMNS = {CommonDataKinds.Email.ADDRESS};
    private final static int[] EMAILS_TO_IDS = {R.id.address_textview};
    private static final String[] EMAILS_PROJECTION = {CommonDataKinds.Email._ID, CommonDataKinds.Email.LOOKUP_KEY, CommonDataKinds.Email.ADDRESS, CommonDataKinds.Email.TYPE, CommonDataKinds.Email.LABEL};

    private static final int EMAIL_ID_INDEX = 0;
    private static final int EMAIL_LOOKUP_KEY_INDEX = 1;
    private static final int EMAIL_ADDRESS_INDEX = 2;
    private static final int EMAIL_TYPE_INDEX = 3;
    private static final int EMAIL_LABEL_INDEX = 4;

    private int contactId = 0;

    private ExpandableHeightListView emailListView = null;
    private CursorAdapter emailCursorAdapter = null;

    private List<Email> emails = new ArrayList<>();

    private OnContactDetailEmailTabContentListener listener;

    public PopupContactDetailEmailTabContent() {
        // Required empty public constructor
    }

    public static PopupContactDetailEmailTabContent newInstance(int contactId) {
        PopupContactDetailEmailTabContent fragment = new PopupContactDetailEmailTabContent();
        Bundle args = new Bundle(1);
        args.putInt(ARG_CONTACT_ID, contactId);
        fragment.setArguments(args);
        return fragment;
    }

    public static PopupContactDetailEmailTabContent newInstance(List<Email> emails) {
        PopupContactDetailEmailTabContent fragment = new PopupContactDetailEmailTabContent();
        if (emails != null && !emails.isEmpty()) {
            Bundle args = new Bundle();
            args.putParcelableArrayList(ARG_EMAILS, (ArrayList<Email>) emails);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //emails = getArguments().getParcelableArrayList(ARG_EMAILS);
            contactId = getArguments().getInt(ARG_CONTACT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contactDetailEmailTabContentView = inflater.inflate(R.layout.popup_contact_detail_email_tab_content, container, false);
        emailListView = (ExpandableHeightListView) contactDetailEmailTabContentView.findViewById(R.id.contact_detail_email_listview);
        emailListView.setExpanded(true);
        return contactDetailEmailTabContentView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //adapter = new ContactDetailEmailArrayAdapter(getActivity(), R.layout.popup_contact_detail_email_tab_content_item, R.id.email_textview, emails);
        emailCursorAdapter = new EmailCursorAdapter(getActivity(), R.layout.popup_contact_detail_email_tab_content_item, null, EMAILS_FROM_COLUMNS, EMAILS_TO_IDS, 0);
        emailListView.setAdapter(emailCursorAdapter);
        Bundle args = new Bundle(1);
        args.putInt(ARG_CONTACT_ID, contactId);
        getLoaderManager().initLoader(EMAIL_LOADER_ID, args, this);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Email emailType) {
        if (listener != null) {
            listener.doContactDetailEmailTabContentInteraction(emailType);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnContactDetailEmailTabContentListener) {
            listener = (OnContactDetailEmailTabContentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnContactProfileContactTabContentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = null;

        if (id == EMAIL_LOADER_ID) {
            if (args != null) {
               long contactId = args.getInt(ARG_CONTACT_ID);
                // don't serve address(s) if contactId not supplied
               if (contactId > 0) {
                   String selection = CommonDataKinds.Email.CONTACT_ID + " = ? ";
                   String[] selectionArgs = new String[]{String.valueOf(contactId)};
                   cursorLoader = new CursorLoader(getActivity(), CommonDataKinds.Email.CONTENT_URI, EMAILS_PROJECTION, selection, selectionArgs, CommonDataKinds.Email.ADDRESS);
               }
            }
        } else {
            // handle more loaders here
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == EMAIL_LOADER_ID) {
            emailCursorAdapter.swapCursor(cursor);
        } else {
            // handle more loaders here
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == EMAIL_LOADER_ID) {
            emailCursorAdapter.swapCursor(null);
        } else {
            // handle more loaders here.
        }
    }

    public interface OnContactDetailEmailTabContentListener {
        // TODO: Update argument type and name
        void doContactDetailEmailTabContentInteraction(Email emailType);
    }

    private class EmailCursorAdapter extends SimpleCursorAdapter {
        public EmailCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public void bindView(View view, Context context, final Cursor cursor) {
            // replace your own logic if having customized bindings other than to textviews
            super.bindView(view, context, cursor);
        }
    }
}

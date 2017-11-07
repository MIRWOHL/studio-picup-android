package com.picup.calling;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import com.picup.calling.adapter.CallToArrayAdapter;
import com.picup.calling.adapter.SectionCursorAdapter;
import com.picup.calling.base.ViewHolder;
import com.picup.calling.data.Phone;
import com.picup.calling.helper.AddressBookHelper;
import com.picup.calling.typefaced.RobotoTextView;
import com.picup.calling.util.Logger;
import com.picup.calling.util.PicupImageUtils;
import com.picup.calling.util.Utililites;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.picup.calling.R;

import java.util.ArrayList;
import java.util.regex.Pattern;

public final class ContactsTabContent extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
                                                            //MenuItemCompat.OnActionExpandListener, // to respond to searchView iconification
                                                            View.OnFocusChangeListener, // watching focus changes on searchView
                                                            TextWatcher,
                                                            SearchView.OnQueryTextListener {  // to respond to text being typed in searchView
                                                            //AdapterView.OnItemClickListener {

    // fragment needs to be informed about the iconfication status of the searchView in the central toolbar.
    // So, it can install the right adapter to its listview
    private static final String TAG = ContactsTabContent.class.getSimpleName();

    private ListView contactsListView = null;
    private View searchResultCountHeaderLayout = null;
    private TextView searchCountTextView = null;

    private static final int CONTACTS_LOADER_ID = 0;
    private static final int SEARCH_CONTACTS_LOADER_ID = 1;

    private static final int PERMISSION_REQUEST_READ_CONTACTS = 0;
    private static final int PERMISSION_REQUEST_CALL_PHONE = 1;

    //private final static String[] CONTACTS_FROM_COLUMNS = {Contacts.PHOTO_THUMBNAIL_URI, Contacts.DISPLAY_NAME_PRIMARY};
    private final static String[] CONTACTS_FROM_COLUMNS = {Contacts.DISPLAY_NAME_PRIMARY};
    //private final static int[] CONTACTS_TO_IDS = {R.id.photo_thumbnail_imageview, R.id.name_textview};
    private final static int[] CONTACTS_TO_IDS = {R.id.duration_textview};

    //private final static String[] SEARCH_CONTACTS_FROM_COLUMNS = {Contacts.PHOTO_THUMBNAIL_URI, Contacts.DISPLAY_NAME_PRIMARY};
    private final static String[] SEARCH_CONTACTS_FROM_COLUMNS = {Contacts.DISPLAY_NAME_PRIMARY};
    //private final static int[] SEARCH_CONTACTS_TO_IDS = {R.id.photo_thumbnail_imageview, R.id.fullname_textview};
    private final static int[] SEARCH_CONTACTS_TO_IDS = {R.id.duration_textview};

    private long contactId = 0;
    private String contactKey = null;
    private Uri contactUri = null;
    private ContactCursorAdapter contactCursorAdapter = null;
    private CursorAdapter searchCursorAdapter = null;
    private DialogFragment contactDetailFragment = null;


    private static final String[] CONTACTS_PROJECTION = {Contacts._ID, Contacts.LOOKUP_KEY, Contacts.PHOTO_THUMBNAIL_URI, Contacts.DISPLAY_NAME_PRIMARY, Contacts.HAS_PHONE_NUMBER, Contacts.DISPLAY_NAME_SOURCE};
    private static final String[] PHONES_PROJECTION = {CommonDataKinds.Phone._ID, CommonDataKinds.Phone.LOOKUP_KEY, CommonDataKinds.Phone.NUMBER, CommonDataKinds.Phone.TYPE, CommonDataKinds.Phone.LABEL};

    private static final String[] PHONE_PREFERENCE_PROJECTION = {Data._ID, Data.LOOKUP_KEY, Data.IS_PRIMARY};

    private static final String[] SEARCH_CONTACTS_PROJECTION = {Contacts._ID, Contacts.LOOKUP_KEY, Contacts.PHOTO_THUMBNAIL_URI, Contacts.DISPLAY_NAME_PRIMARY, Contacts.HAS_PHONE_NUMBER, Contacts.DISPLAY_NAME_SOURCE};
    private static final String[] SEARCH_PHONES_PROJECTION = {CommonDataKinds.Phone._ID, CommonDataKinds.Phone.LOOKUP_KEY, CommonDataKinds.Phone.NUMBER, CommonDataKinds.Phone.TYPE, CommonDataKinds.Phone.LABEL};

    private static final int CONTACT_ID_INDEX = 0;
    private static final int CONTACT_LOOKUP_KEY_INDEX = 1;
    private static final int CONTACT_PHOTO_THUMBNAIL_URI_INDEX = 2;
    private static final int CONTACT_DISPLAY_NAME_PRIMARY_INDEX = 3;
    private static final int CONTACT_HAS_PHONE_NUMBER_INDEX = 4;


    private static final int SEARCH_CONTACT_ID_INDEX = 0;
    private static final int SEARCH_CONTACT_LOOKUP_KEY_INDEX = 1;
    private static final int SEARCH_CONTACT_PHOTO_THUMBNAIL_URI_INDEX = 2;
    private static final int SEARCH_CONTACT_DISPLAY_NAME_PRIMARY_INDEX = 3;
    private static final int SEARCH_CONTACT_HAS_PHONE_NUMBER_INDEX = 4;

    private static final int PHOTO_THUMBNAIL_INDEX = 2;
    private static final int DISPLAY_NAME_PRIMARY_INDEX = 3;
    private static final int HAS_PHONE_NUMBER_INDEX = 4;

    private static final int PHONE_ID_INDEX = 0;
    private static final int PHONE_LOOKUP_KEY_INDEX = 1;
    private static final int PHONE_NUMBER_INDEX = 2;
    private static final int PHONE_TYPE_INDEX = 3;
    private static final int PHONE_LABEL_INDEX = 4;

    private static final int SEARCH_PHONE_ID_INDEX = 0;
    private static final int SEARCH_PHONE_LOOKUP_KEY_INDEX = 1;
    private static final int SEARCH_PHONE_NUMBER_INDEX = 2;
    private static final int SEARCH_PHONE_TYPE_INDEX = 3;
    private static final int SEARCH_PHONE_LABEL_INDEX = 4;

    private static final int PHONE_PREFERENCE_INDEX = 2;

    private static final String SELECTION = Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?";

    private static String searchString = "";
    private String[] selectionArgs = {searchString};
    private String query = null;

    private static final String SEARCHVIEW_VISIBLE = "android.idt.net.com.picup.calling.SEARCHVIEW_VISIBLE";
    private static final String SEARCH_CRITERIA = "android.idt.net.com.picup.calling.SEARCH_CRITERIA";

    private boolean searchViewVisible;
    //private GestureDetectorCompat gestureDetector = null;
    private GestureDetectorCompat gestureDetectorCompat = null;
    private Pattern pattern = null;

    private static boolean needCalling = false;
    private static String selectedCallToNumber = null;
    private static boolean loaderInitialize = false;

    private OnContactsTabContentListener listener;

    public ContactsTabContent() {
        // Required empty public constructor
    }

    public static ContactsTabContent newInstance(boolean searchViewVisible) {
        ContactsTabContent fragment = new ContactsTabContent();
        Bundle args = new Bundle();
        args.putBoolean(SEARCHVIEW_VISIBLE, searchViewVisible);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnContactsTabContentListener) {
            listener = (OnContactsTabContentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnContactsTabContentListener");
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            searchViewVisible = getArguments().getBoolean(SEARCHVIEW_VISIBLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contactTabContentView = inflater.inflate(R.layout.contacts_tab_content, container, false);
        contactsListView = (ListView)contactTabContentView.findViewById(R.id.contacts_listview);
        contactsListView.setItemsCanFocus(true);
        contactsListView.setFocusable(true);
        contactsListView.setClickable(true);
        searchResultCountHeaderLayout = inflater.inflate(R.layout.contacts_search_result_header, null);
        searchCountTextView = (TextView)searchResultCountHeaderLayout.findViewById(R.id.search_result_count_textview);

        return contactTabContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.log("ContactsTabContent - onActivityCreated");
        contactsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_FLING || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    listener.listScrolling();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //listener.listScrolling();
            }
        });
        //contactsListView.setOnItemClickListener(this);
        if (contactCursorAdapter == null) {
            //contactCursorAdapter = new ContactCursorAdapterOld(getActivity(), R.layout.contact_row_item, null, CONTACTS_FROM_COLUMNS, CONTACTS_TO_IDS, ic_key_0);
            contactCursorAdapter = new ContactCursorAdapter(getActivity(), null);
        }
        if (searchCursorAdapter == null) {
            //searchCursorAdapter = new SearchCursorAdapterOld(getActivity(), R.layout.search_contact_row_item, null, SEARCH_CONTACTS_FROM_COLUMNS, SEARCH_CONTACTS_TO_IDS, ic_key_0);
            searchCursorAdapter = new SearchCursorAdapter(getActivity(), null, 0);
            //searchCursorAdapter = new SearchCursorAdapterNew(getActivity(), null);
        }
/*
        if (searchViewVisible) {
            contactsListView.setAdapter(contactCursorAdapter);
        }
*/
        contactsListView.setAdapter(contactCursorAdapter);

        //contactsListView.setAdapter(searchViewVisible ? searchCursorAdapter : contactCursorAdapter);

        if (contactsListView.getAdapter() instanceof SearchCursorAdapter) {
            contactsListView.addHeaderView(searchResultCountHeaderLayout);
        } else {
            contactsListView.removeHeaderView(searchResultCountHeaderLayout);
        }
        // Usually, loader is started here. However, I have to wait until read_contacts_permission is verified
        //getLoaderManager().initLoader(CONTACTS_LOADER_ID, null, this);

        boolean waitingOnPermission = false;
/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_READ_CONTACTS);
                waitingOnPermission = true;
            }
        }
        if (!waitingOnPermission) {
            getLoaderManager().initLoader(CONTACTS_LOADER_ID, null, this);
        }
*/

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            getLoaderManager().initLoader(CONTACTS_LOADER_ID, null, this);
            loaderInitialize = true;
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_READ_CONTACTS);
            loaderInitialize = true;
        }
/*
        gestureDetector = new GestureDetector(getActivity(), new SimpleGestureHandler());
        // install gestureDetector to detect flinging event on fragment's view
        contactsListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
*/
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_CONTACTS:
                if (TextUtils.equals(permissions[0], Manifest.permission.READ_CONTACTS) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted;initialize loader
                    getLoaderManager().initLoader(CONTACTS_LOADER_ID, null, this);
                    loaderInitialize = true;
                }
                break;
            case PERMISSION_REQUEST_CALL_PHONE:
                if (needCalling) {

                }
            // handle more loaders here
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

/*
    @Override
    public void onClick(View view) {
        Log.d(TAG, view.getClass().getName());
        Cursor purple_shape = (Cursor)contactsListView.getSelectedItem();
        Log.d(TAG, "purple_shape index=" + purple_shape.getPosition());
    }
*/

    @Override
    public boolean onQueryTextSubmit(String query) {
        Bundle bundle = null;
        if (!TextUtils.isEmpty(query)) {
            bundle = new Bundle(1);
            bundle.putString(SEARCH_CRITERIA, query);
            // pattern will be used to scanning the matched substrings of search results
            pattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
            searchString = query;
        }
        if (!(contactsListView.getAdapter() instanceof HeaderViewListAdapter)) { // adapter type when header is installed
            contactsListView.setAdapter(searchCursorAdapter);
            contactsListView.addHeaderView(searchResultCountHeaderLayout);
        }
        restartLoader(SEARCH_CONTACTS_LOADER_ID, bundle, this);
        // action search is handled by this listener
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Bundle bundle = null;
        if (!TextUtils.isEmpty(newText)) {
            bundle = new Bundle(1);
            // pattern will be used to scanning the matched substrings of search results
            pattern = Pattern.compile(newText, Pattern.CASE_INSENSITIVE);
            searchString = newText;
            bundle.putString(SEARCH_CRITERIA, newText);
        }
        if (!(contactsListView.getAdapter() instanceof HeaderViewListAdapter)) { // adapter type when header is installed
            contactsListView.setAdapter(searchCursorAdapter);
            contactsListView.addHeaderView(searchResultCountHeaderLayout);
        }
        restartLoader(SEARCH_CONTACTS_LOADER_ID, bundle, this);
        // action search is handle by  this listener
        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v instanceof AutoCompleteTextView) {
            if (hasFocus) {
                // do nothing
            } else {
                // user is leaving the search;re-query all the contacts
                contactsListView.removeHeaderView(searchResultCountHeaderLayout);
                contactsListView.setAdapter(contactCursorAdapter);
                restartLoader(CONTACTS_LOADER_ID, null, this);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        Bundle bundle = null;
        if (contactsListView == null) {
            return;
        }
        String criteria = s.toString();
        if (!TextUtils.isEmpty(criteria)) {
            bundle = new Bundle(1);
            // pattern will be used to scanning the matched substrings of search results
            pattern = Pattern.compile(criteria, Pattern.CASE_INSENSITIVE);
            searchString = s.toString();
            bundle.putString(SEARCH_CRITERIA, criteria);
            if (!(contactsListView.getAdapter() instanceof HeaderViewListAdapter)) { // adapter type when header is installed
                contactsListView.setAdapter(searchCursorAdapter);
                contactsListView.addHeaderView(searchResultCountHeaderLayout);
            }
            restartLoader(SEARCH_CONTACTS_LOADER_ID, bundle, this);
        } else {
            if (!(contactsListView.getAdapter() instanceof ContactCursorAdapter)) {
                contactsListView.removeHeaderView(searchResultCountHeaderLayout);
                contactsListView.setAdapter(contactCursorAdapter);
            }
            restartLoader(CONTACTS_LOADER_ID, bundle, this);
        }
    }

    public interface OnContactsTabContentListener {
        void contactCall(String callToNumber);
        void showContactDetail(int contactId);
        void showContactProfile(Object source, int contactId);
        void listScrolling();
        void explainPermission(String permissionName);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = null;

        String selection = "";
        String picupContact = AddressBookHelper.findPicupContactID(getActivity());
        ArrayList<String> selectionArgs = new ArrayList<>();
        if (!TextUtils.isEmpty(picupContact)) {
            selection += Contacts._ID + " != ?";
            selectionArgs.add(picupContact);
        }
        String orderBy = Contacts.DISPLAY_NAME_SOURCE + " DESC";
        orderBy += ", " + Contacts.DISPLAY_NAME_PRIMARY + " COLLATE LOCALIZED ASC";

        if (id == CONTACTS_LOADER_ID) {
            String[] selectionArgsArray = Utililites.arrayList2array(selectionArgs);
            cursorLoader = new CursorLoader(getActivity(), Contacts.CONTENT_URI, CONTACTS_PROJECTION, selection, selectionArgsArray, orderBy);
        } else if (id == SEARCH_CONTACTS_LOADER_ID) {
            if (args != null) {
                String criteria = args.getString(SEARCH_CRITERIA);
                if (!TextUtils.isEmpty(criteria)) {
                    if (!TextUtils.isEmpty(selection)) {
                        selection += " AND ";
                    }
                    selection += Contacts.DISPLAY_NAME_PRIMARY + " LIKE ? ";
                    selectionArgs.add("%" + criteria + "%");
                    String[] selectionArgsArray = Utililites.arrayList2array(selectionArgs);
                    cursorLoader = new CursorLoader(getActivity(), Contacts.CONTENT_URI, SEARCH_CONTACTS_PROJECTION, selection, selectionArgsArray, orderBy);
                }
            } else {
                String[] selectionArgsArray = Utililites.arrayList2array(selectionArgs);
                cursorLoader = new CursorLoader(getActivity(), Contacts.CONTENT_URI, SEARCH_CONTACTS_PROJECTION, selection, selectionArgsArray, orderBy);
            }
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == CONTACTS_LOADER_ID) {
            contactCursorAdapter.swapCursor(cursor);
        } else if (loader.getId() == SEARCH_CONTACTS_LOADER_ID) {
            searchCursorAdapter.swapCursor(cursor);
            searchCountTextView.setText(searchCursorAdapter.getCount() + " found");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == CONTACTS_LOADER_ID) {
            contactCursorAdapter.swapCursor(null);
        } else if (loader.getId() == SEARCH_CONTACTS_LOADER_ID) {
            searchCursorAdapter.swapCursor(null);
            searchCountTextView.setText(searchCursorAdapter.getCount() + " found");
        }
    }

    private class ContactCursorAdapter extends SectionCursorAdapter<String, SectionViewHolder, ItemViewHolder> implements View.OnClickListener, AdapterView.OnItemSelectedListener {

        ContactCursorAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0, R.layout.contact_row_item_section, R.layout.contact_row_item);
        }

        @Override
        protected String getSectionFromCursor(Cursor cursor) {
            String log = "ContactCursorAdapter - getSectionFromCursor";
            if (cursor == null || cursor.isClosed()) {
                log += " - invalid cursor";
                Logger.log(log);
                return "";
            }
            try {
                int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);
                String fullName = null;
                if (nameIndex != -1) {
                    fullName = cursor.getString(nameIndex);
                }
                int sourceIndex = cursor.getColumnIndex(Contacts.DISPLAY_NAME_SOURCE);
                int source = -1;
                if (sourceIndex != -1) {
                    source = cursor.getInt(sourceIndex);
                }
                log += " - fullName:";
                log += fullName;
                log += " - source:";
                log += source;
                if (source != ContactsContract.DisplayNameSources.STRUCTURED_NAME
                        && source != ContactsContract.DisplayNameSources.STRUCTURED_PHONETIC_NAME
                        && source != ContactsContract.DisplayNameSources.NICKNAME) {
                    //UNITE-1841 name come from diff source
                    return "#";
                }
                if (!TextUtils.isEmpty(fullName)) {
                    fullName = fullName.toUpperCase();
                    //Logger.log(log);
                    String firstChar = fullName.substring(0, 1);
                    if (!TextUtils.isEmpty(firstChar) && containsSpecialCharacter(firstChar)) {
                        return "...";
                    }
                    return firstChar;
                }
            } catch (Throwable t) {
            }
            return ""; // unlabeled section
        }

        @Override
        protected SectionViewHolder createSectionViewHolder(View sectionView, String section) {
            return new SectionViewHolder(sectionView);
        }

        @Override
        protected void bindSectionViewHolder(int position, SectionViewHolder sectionViewHolder, ViewGroup parent, String section) {
            sectionViewHolder.sectionTextView.setText(section);
        }

        @Override
        protected ItemViewHolder createItemViewHolder(Cursor cursor, View itemView) {
            return new ItemViewHolder(itemView);
        }

        @Override
        protected void bindItemViewHolder(ItemViewHolder itemViewHolder, Cursor cursor, ViewGroup parent) {
            if (itemViewHolder == null) {
                return;
            }
            itemViewHolder.reset();

            int _id = -1;
            String thumbnailUriString = null;
            String fullName = null;
            boolean hasPhone = false;
            int displayNameSource = 0;
            if (cursor != null && !cursor.isClosed()) {
                try {
                    int thumbnailUriStringIndex = cursor.getColumnIndex(Contacts.PHOTO_THUMBNAIL_URI);
                    if (thumbnailUriStringIndex != -1) {
                        thumbnailUriString = cursor.getString(thumbnailUriStringIndex);
                    }
                    int fullNameIndex = cursor.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY);
                    if (fullNameIndex != -1) {
                        fullName = cursor.getString(fullNameIndex);
                    }
                    int _idIndex = cursor.getColumnIndex(Contacts._ID);
                    if (_idIndex != -1) {
                        _id = cursor.getInt(_idIndex);
                    }
                    int hasPhoneIndex = cursor.getColumnIndex(Contacts.HAS_PHONE_NUMBER);
                    if (hasPhoneIndex != -1) {
                        int hasPhoneInt = cursor.getInt(hasPhoneIndex);
                        if (hasPhoneInt == 1) {
                            hasPhone = true;
                        }
                    }
                    int displayNameSourceIndex = cursor.getColumnIndex(Contacts.DISPLAY_NAME_SOURCE);
                    if (displayNameSourceIndex != -1) {
                        displayNameSource = cursor.getInt(displayNameSourceIndex);
                    }
                } catch (Throwable t) {
                }
            }
            if (displayNameSource != ContactsContract.DisplayNameSources.STRUCTURED_NAME
                    && displayNameSource != ContactsContract.DisplayNameSources.STRUCTURED_PHONETIC_NAME
                    && displayNameSource != ContactsContract.DisplayNameSources.NICKNAME) {
                //UNITE-1841 name come from diff source
                if (itemViewHolder.thumbnailImageView != null) {
                    itemViewHolder.thumbnailImageView.setImageResource(R.drawable.icon_for_no_name_contact);
                    if (itemViewHolder.thumbnailImageView.getVisibility() != View.VISIBLE) {
                        itemViewHolder.thumbnailImageView.setVisibility(View.VISIBLE);
                    }
                }
                if (itemViewHolder.initialTextView != null) {
                    if (itemViewHolder.initialTextView.getVisibility() == View.VISIBLE) {
                        itemViewHolder.initialTextView.setVisibility(View.INVISIBLE);
                    }
                }
            } else if (!TextUtils.isEmpty(thumbnailUriString)) {
                itemViewHolder.thumbnailImageView.setImageURI(Uri.parse(thumbnailUriString));
                if (itemViewHolder.thumbnailImageView.getVisibility() != View.VISIBLE) {
                    itemViewHolder.thumbnailImageView.setVisibility(View.VISIBLE);
                }
                if (itemViewHolder.initialTextView.getVisibility() == View.VISIBLE) {
                    itemViewHolder.initialTextView.setVisibility(View.INVISIBLE);
                }
            } else {
                if (!TextUtils.isEmpty(fullName)) {
                    String[] fullNameArray = fullName.split(" ");
                    if (fullNameArray.length > 0) {
                        String firstName = fullNameArray[0];
                        if (!firstName.isEmpty()) {
                            itemViewHolder.initialTextView.append(firstName.subSequence(0, 1));
                        }
                    }
                    if (fullNameArray.length > 1) {
                        String lastName = fullNameArray[1];
                        if (!lastName.isEmpty()) {
                            itemViewHolder.initialTextView.append(" ");
                            itemViewHolder.initialTextView.append(lastName.subSequence(0, 1));
                        }
                    }
                    if (itemViewHolder.initialTextView.getVisibility() != View.VISIBLE) {
                        itemViewHolder.initialTextView.setVisibility(View.VISIBLE);
                    }
                    //Typeface typeface = itemViewHolder.initialTextView.getTypeface();
                    if (itemViewHolder.thumbnailImageView.getVisibility() == View.VISIBLE) {
                        itemViewHolder.thumbnailImageView.setVisibility(View.VISIBLE);
                    }
                }
            }
            itemViewHolder.fullNameTextView.setText(fullName);
            // tag contactId on fullName, so it can be passed to caller when fullName is tapped on
            itemViewHolder.fullNameTextView.setTag(_id);
            itemViewHolder.fullNameTextView.setOnClickListener(this);
            // round the image
            Drawable photoThumbnailDrawable = itemViewHolder.thumbnailImageView.getDrawable();
            RoundedBitmapDrawable roundedBitmapDrawable = PicupImageUtils.toRoundedBitmapDrawable(getContext(), photoThumbnailDrawable);
            if (roundedBitmapDrawable != null) { // should be true
                itemViewHolder.thumbnailImageView.setImageDrawable(roundedBitmapDrawable);
            }
            // install listener on thumbnail imageview
            itemViewHolder.thumbnailImageView.setOnClickListener(this);
            // tag contactId on thumbnail, so it can be passed to caller when imageView is tapped on
            itemViewHolder.thumbnailImageView.setTag(_id);
            //final CallToSpinner phoneTypeSpinner = itemViewHolder.phoneTypeSpinner;
            final Spinner phoneTypeSpinner = itemViewHolder.phoneTypeSpinner;
            phoneTypeSpinner.setOnItemSelectedListener(null);
            if (hasPhone) {
                // retrieving phones for spinner!
                long contactId = cursor.getLong(CONTACT_ID_INDEX);
                String selection = CommonDataKinds.Phone.CONTACT_ID + " = ? ";
                String[] selectionArgs = new String[]{String.valueOf(contactId)};
                Cursor phoneCursor = getContext().getContentResolver().query(CommonDataKinds.Phone.CONTENT_URI, PHONES_PROJECTION, selection, selectionArgs, null);
                if (phoneCursor != null) {
                    if (phoneCursor.moveToFirst()) {
                        CallToArrayAdapter phoneTypeAdapter = new CallToArrayAdapter(getContext(), R.layout.call_to_spinner_view, R.id.type_textview);
                        try {
                            do {
                                long phoneId = phoneCursor.getLong(PHONE_ID_INDEX);
                                String number = phoneCursor.getString(PHONE_NUMBER_INDEX);
                                int type = -1;
                                try {
                                    phoneCursor.getInt(PHONE_TYPE_INDEX);
                                } catch (Exception e) {Logger.log("ContactsTabContent - bindItemViewHolder - Exception:"+e.toString()); }
                                String typeLabel = (String) CommonDataKinds.Phone.getTypeLabel(getActivity().getResources(),type,"");
                                String customLabel = phoneCursor.getString(PHONE_LABEL_INDEX);
                                Phone phone = new Phone(phoneId, number, typeLabel, false, customLabel);
                                phone.setIgnoreSelectionForCalling(true); //**********************
                                phoneTypeAdapter.add(phone);
                                selection = CommonDataKinds.Phone._ID + " = ? ";
                                selectionArgs = new String[]{String.valueOf(phoneId)};
                                Cursor phonePreferenceCursor = getContext().getContentResolver().query(Data.CONTENT_URI, PHONE_PREFERENCE_PROJECTION, selection, selectionArgs, null);
                                if (phonePreferenceCursor != null) {
                                    try {
                                        if (phonePreferenceCursor.moveToFirst()) {
                                            int phonePreferenceType = phonePreferenceCursor.getInt(PHONE_PREFERENCE_INDEX);
                                            if (phonePreferenceType > 0) {
                                                phone.setPrimary(true);
                                            }
                                        }
                                    } finally {
                                        phonePreferenceCursor.close();
                                    }
                                }
                            } while (phoneCursor.moveToNext());
                        } finally {
                            phoneCursor.close();
                        }
                        if (!phoneTypeAdapter.isEmpty()) {
                            // install adapter for spinner
                            phoneTypeSpinner.setAdapter(phoneTypeAdapter);
                            // pre-select primary phone for spinner
                            int primaryIndex = 1; // zero is header
                            for (int i = 0; i < phoneTypeAdapter.getCount(); i++) {
                                Phone phone = phoneTypeAdapter.getItem(i);
                                if (phone == null) {
                                    continue;
                                }
                                if (phone.isPrimary()) {
                                    primaryIndex = i;
                                    break;
                                }
                            }
                            //phoneTypeSpinner.setSelection(primaryIndex, 1);
                            phoneTypeSpinner.setSelection(primaryIndex);
                            //phoneTypeSpinner.setOnItemSelectedListener(phoneTypeAdapter);

                            phoneTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    Phone phone = (Phone) parent.getItemAtPosition(position);
                                    if (!phone.isIgnoreSelectionForCalling()) {
                                        // hosting activity will make sure permission is granted
                                        Logger.log("ContactsTabContent - onItemSelected");
                                        listener.contactCall(phone.getNumber());
                                    } else {
                                        Log.d(TAG, "isIgnoreSelectionForCalling=" + phone.isIgnoreSelectionForCalling());
                                    }
                                }
                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });

                        }
                    }
                }
            }
            final RelativeLayout callLayout = itemViewHolder.callLayout;
            callLayout.setVisibility(View.GONE);
            Button callButton = itemViewHolder.callButton;
            // disable contactCallButton when no 'call-from' numbers are available
            callButton.setEnabled(hasPhone);

            callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Phone selectedPhoneType = (Phone)phoneTypeSpinner.getSelectedItem();
                    // fragment invokes contactCall method which is implemented by hosting activity
                    //TODO..if this is ever to be used change implementation...this method calls out directly rather than via TAN
                    listener.contactCall(selectedPhoneType.getNumber());
                    callLayout.setVisibility(View.GONE);
                }
            });
        }

        @Override
        protected int getMaxIndexerLength() {
            return 1;
        }

        @Override
        public void onClick(View v) {
            if (v instanceof ImageView) {
                int contactId = (int)v.getTag();
                // showContactDetail(contactId) is implemented by hosting activity
                listener.showContactDetail(contactId);
            } else if (v instanceof TextView) {
                int contactId = (int)v.getTag();
                listener.showContactProfile(this, contactId);
            }
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Phone phone = (Phone)parent.getSelectedItem();
            // hosting activity should implement this method
            listener.contactCall(phone.getNumber());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public class ItemViewHolder extends ViewHolder {
        public LinearLayout contactItemLayout = null;
        public RelativeLayout contactLayout = null;
        public TextView initialTextView = null;
        public ImageView thumbnailImageView = null;
        public TextView fullNameTextView = null;
        public Spinner phoneTypeSpinner = null;
        //public CallToSpinner phoneTypeSpinner = null;
        public RelativeLayout callLayout = null;
        public Button callButton = null;

        public GestureDetector gestureDetector = null;

        private final Animation leftToRightAnimation;
        private final Animation rightToLeftAnimation;

        public ItemViewHolder(View rootView) {
            super(rootView);
            this.contactItemLayout = findWidgetById(R.id.contact_item_layout);

            this.contactLayout = findWidgetById(R.id.contact_layout);

            this.initialTextView = findWidgetById(R.id.initial_textview);
            this.initialTextView.setFocusable(false);
            this.initialTextView.setClickable(false);
            this.initialTextView.setTextIsSelectable(false);
            this.initialTextView.setFocusableInTouchMode(false);

            this.thumbnailImageView = findWidgetById(R.id.thumbnail_imageview);
            this.thumbnailImageView.setFocusable(false);
            this.thumbnailImageView.setClickable(false);
            this.thumbnailImageView.setFocusableInTouchMode(false);

            this.fullNameTextView = findWidgetById(R.id.duration_textview);
            this.fullNameTextView.setFocusable(false);
            this.fullNameTextView.setClickable(false);
            this.fullNameTextView.setFocusableInTouchMode(false);

            this.phoneTypeSpinner = findWidgetById(R.id.phone_type_spinner);
            this.phoneTypeSpinner.setFocusable(false);
            this.phoneTypeSpinner.setClickable(false);
            this.phoneTypeSpinner.setFocusableInTouchMode(false);

            this.callLayout = findWidgetById(R.id.call_layout);
            this.callLayout.setFocusable(false);
            this.callLayout.setClickable(false);

            this.callButton = findWidgetById(R.id.call_button);
            this.callButton.setFocusable(false);
            this.callButton.setClickable(false);

            this.gestureDetector = new GestureDetector(rootView.getContext(), new GestureHandler());


/*
            this.rootView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });
*/

            // Postponed
            //this.fullNameTextView.setOnTouchListener(new View.OnTouchListener() {
            //    @Override
            //    public boolean onTouch(View v, MotionEvent event) {
            //        gestureDetector.onTouchEvent(event);
            //        return true;
            //    }
            //});


            this.leftToRightAnimation = AnimationUtils.loadAnimation(rootView.getContext(), R.anim.left_to_right);
            this.leftToRightAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    Log.d(TAG, "LeftToRight.onAnimationStart");
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Log.d(TAG, "LeftToRight.onAnimationEnd");
                    callLayout.setVisibility(View.VISIBLE);
/*
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, ic_key_0.2f);
                    callLayout.setLayoutParams(layoutParams);
                    callLayout.setVisibility(View.VISIBLE);
                    LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, ic_key_0.8f);
                    contactLayout.setLayoutParams(layoutParams2);
*/
                    boolean queued = callLayout.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            callButton.performClick(); // run logic provided in OnClickListener
                                        }
                                     }, 1000);
                    //if (queued) {
                    //    callLayout.setVisibility(View.GONE);
                    //}
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    Log.d(TAG, "LeftToRight.onAnimationRepeat");
                }
            });
            this.rightToLeftAnimation = AnimationUtils.loadAnimation(rootView.getContext(), R.anim.right_to_left);
            this.rightToLeftAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    Log.d(TAG, "RightToLeft.onAnimationStart");
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Log.d(TAG, "RightToLeft.onAnimationEnd");
                    callLayout.setVisibility(View.GONE);
                    //LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    //contactLayout.setLayoutParams(layoutParams2);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    Log.d(TAG, "RightToLeft.onAnimationRepeat");
                }
            });
        }

        public void reset() {
            contactLayout.setVisibility(View.VISIBLE);
            initialTextView.setVisibility(View.VISIBLE);
            initialTextView.setText("");
            thumbnailImageView.setImageDrawable(null);
            thumbnailImageView.setOnClickListener(null);
            fullNameTextView.setText(null);
            fullNameTextView.setOnClickListener(null);
            phoneTypeSpinner.setAdapter(null);
            phoneTypeSpinner.setOnItemSelectedListener(null);
            //callLayout.setVisibility(View.GONE);
        }

        public void enableSpinnerItemSelectionForCalling() {
            phoneTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Phone phone = (Phone) parent.getItemAtPosition(position);
                    //if (!phone.isIgnoreSelectionForCalling()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ContextCompat.checkSelfPermission(rootView.getContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                //listener.contactCall(phone.getNumber());
                            } else {
                                needCalling = true;
                                ContactsTabContent.this.requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CALL_PHONE);
                            }
                        }
                    //}
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }

            });
        }

        private class GestureHandler extends GestureDetector.SimpleOnGestureListener {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                final int SWIPE_MIN_DISTANCE = 100;
                final int SWIPE_MAX_OFF_PATH = 250;
                final int SWIPE_THRESHOLD_VELOCITY = 150;
                if ((e1 != null && e2 != null)) {
                    if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                        return false;
                    }
                    if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) { // right to left
                        Log.d(TAG, "start RightToLeft animation");
                        //viewSwitcher.showPrevious();
                        //contact_layout.startAnimation(rightToLeftAnimation);
                        contactItemLayout.startAnimation(rightToLeftAnimation);
/*
                        if (contact_layout.getVisibility() != View.VISIBLE) {
                            contact_layout.setVisibility(View.VISIBLE);
                        }
                        callLayout.setVisibility(View.GONE);
*/
                    } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) { // left to right
                        //callButton.startAnimation(animation);
                        Log.d(TAG, "start LeftToRight animation");
                        //viewSwitcher.showNext();
                        //contact_layout.startAnimation(leftToRightAnimation);
                        contactItemLayout.startAnimation(leftToRightAnimation);
                        //callLayout.setVisibility(View.VISIBLE);
                        //callLayout.startAnimation(animation);
/*
                        if (contact_layout.getVisibility() != View.GONE) {
                            contact_layout.setVisibility(View.GONE);
                        }
                        callLayout.setVisibility(View.VISIBLE);
                        callLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                callButton.performClick(); // run logic provided in OnClickListener
                            }
                        }, 2000);
*/
                    }
                }
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return fullNameTextView.performClick();
            }
        }
    }

    public static class SearchResultItemViewHolder extends ViewHolder {
        public TextView initialTextView = null;
        public ImageView thumbnailImageView = null;
        public RobotoTextView fullNameTextView = null;
        public TextView phoneTypeTextView = null;

        public SearchResultItemViewHolder(View rootView) {
            super(rootView);
            this.initialTextView = findWidgetById(R.id.initial_textview);
            this.thumbnailImageView = findWidgetById(R.id.thumbnail_imageview);
            //this.thumbnailImageView.setFocusable(false);
            this.fullNameTextView = findWidgetById(R.id.duration_textview);
            //this.fullNameTextView.setFocusable(false);
            this.phoneTypeTextView = findWidgetById(R.id.phone_type_textview);
        }
    }

    public class SectionViewHolder extends ViewHolder {
        public TextView sectionTextView = null;

        public SectionViewHolder(View rootView) {
            super(rootView);
            this.sectionTextView = findWidgetById(R.id.section_textview);
            this.sectionTextView.setPadding(60, 0 ,0, 0);
            //Typeface newTypeface = Typeface.create(sectionTextView.getTypeface(), RobotoTextView.ROBOTO_BOLD);
            //this.sectionTextView.setTypeface(newTypeface);
        }
    }

    private class SearchCursorAdapter extends CursorAdapter implements View.OnClickListener {

        public SearchCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View searchRowItemView = LayoutInflater.from(context).inflate(R.layout.search_contact_row_item, parent, false);
            return searchRowItemView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // photo thumbnail
            ImageView photoThumbnailImageView = (ImageView) view.findViewById(R.id.thumbnail_imageview);
            photoThumbnailImageView.setImageDrawable(null);
            String photoUriString = cursor.getString(SEARCH_CONTACT_PHOTO_THUMBNAIL_URI_INDEX);
            // may be missing from Contacts app
            if (photoUriString != null) {
                photoThumbnailImageView.setImageURI(Uri.parse(photoUriString));
            } else {
                TextView initialTextView = (TextView)view.findViewById(R.id.initial_textview);
                initialTextView.setText("");
                String fullName = cursor.getString(SEARCH_CONTACT_DISPLAY_NAME_PRIMARY_INDEX);
                if (!TextUtils.isEmpty(fullName)) {
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
                    if (initialTextView.getVisibility() != View.VISIBLE) {
                        initialTextView.setVisibility(View.VISIBLE);
                    }
                    //Typeface typeface = itemViewHolder.initialTextView.getTypeface();
                    if (photoThumbnailImageView.getVisibility() == View.VISIBLE) {
                        photoThumbnailImageView.setVisibility(View.VISIBLE);
                    }
                }
            }
            // round the icon
            Drawable photoThumbnailDrawable = photoThumbnailImageView.getDrawable();
            RoundedBitmapDrawable roundedBitmapDrawable = PicupImageUtils.toRoundedBitmapDrawable(context, photoThumbnailDrawable);
            if (roundedBitmapDrawable != null) { // should be true
                photoThumbnailImageView.setImageDrawable(roundedBitmapDrawable);
            }
            // fullName
            TextView fullNameTextView = (TextView)view.findViewById(R.id.duration_textview);
            int contactId = cursor.getInt(SEARCH_CONTACT_ID_INDEX);
            fullNameTextView.setTag(contactId); // for contact profile to load selected contact
            fullNameTextView.setOnClickListener(this); // tapping on fullName brings up contact profile
            String fullName = cursor.getString(SEARCH_CONTACT_DISPLAY_NAME_PRIMARY_INDEX);
            // handle foreground color for text here indicating what is currently being searched.
            SpannableStringBuilder builder = highlight(fullName);
            fullNameTextView.setText(builder, TextView.BufferType.SPANNABLE);

            //fullNameTextView.setText(purple_shape.getString(SEARCH_CONTACT_DISPLAY_NAME_PRIMARY_INDEX));

            boolean hasPhone = (cursor.getInt(SEARCH_CONTACT_HAS_PHONE_NUMBER_INDEX) == 1 ? true : false);
            if (hasPhone) {
                // retrieving list of phones
                contactId = cursor.getInt(SEARCH_CONTACT_ID_INDEX);
                Cursor phoneCursor = context.getContentResolver().query(android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI, SEARCH_PHONES_PROJECTION, android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{String.valueOf(contactId)}, null);
                if (phoneCursor != null) {
                    if (phoneCursor.moveToFirst()) {
                        Phone phoneType = null;
                        TextView phoneTypeTextView = (TextView)view.findViewById(R.id.phone_type_textview);
                        do {
                            long id = phoneCursor.getLong(SEARCH_CONTACT_ID_INDEX);
                            String number = phoneCursor.getString(SEARCH_PHONE_NUMBER_INDEX);
                            int type = -1;
                            try {
                                type =phoneCursor.getInt(SEARCH_PHONE_TYPE_INDEX);
                            } catch (Exception e) { Logger.log("ContactsTabContent - bindView - Exception:"+e.toString()); }
                            String typeLabel = (String) CommonDataKinds.Phone.getTypeLabel(getActivity().getResources(),type,"");
                            String customLabel = phoneCursor.getString(SEARCH_PHONE_LABEL_INDEX);
                            phoneType = new Phone(id, number, typeLabel, false, customLabel);
                            if (phoneCursor.getCount() == 1) {
                                // this contact has only one phone; show this phone as primary even though it might not be set primary by Contacts app
                                //phoneTypeTextView.setText(PhoneTypeEnum.getPhoneTypeLabel(type).toString());
                                break;
                            }
                            // this contact has more than one phone; try to determine which phone is primary
                            Cursor phonePreferenceCursor = context.getContentResolver().query(Data.CONTENT_URI, PHONE_PREFERENCE_PROJECTION, Data._ID + " = ?", new String[]{String.valueOf(id)}, null);
                            if (phonePreferenceCursor != null) {
                                try {
                                    if (phonePreferenceCursor.moveToFirst()) {
                                        int phonePreferenceType = phonePreferenceCursor.getInt(PHONE_PREFERENCE_INDEX);
                                        if (phonePreferenceType > 0) {
                                            phoneType.setPrimary(true);
                                            //phoneTypeTextView.setText(PhoneTypeEnum.getPhoneTypeLabel(type).toString());
                                            break;
                                        }
                                    }
                                } finally {
                                    phonePreferenceCursor.close();
                                }
                            }
                        } while (phoneCursor.moveToNext());
                        phoneCursor.close();
                        if (phoneType != null) {
                            if (!TextUtils.isEmpty(phoneType.getCustomLabel()))
                                phoneTypeTextView.setText(phoneType.getCustomLabel().toString());
                            else
                                phoneTypeTextView.setText(phoneType.getType());
                        }
                    }
                }
            }
        }

        private SpannableStringBuilder highlight(CharSequence fullName) {
            SpannableStringBuilder builder = new SpannableStringBuilder();
            String original = fullName.toString();
            String originalInLowercase = fullName.toString().toLowerCase();
            String searchStringInLowercase = searchString.toLowerCase();
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

        @Override
        public void onClick(View v) {
            if (v instanceof TextView) {
                int contactId = (int)((TextView)v).getTag();
                listener.showContactProfile(this, contactId);
            }
        }

        private class SimpleGestureHandler extends GestureDetector.SimpleOnGestureListener {

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                final int SWIPE_MIN_DISTANCE = 120;
                final int SWIPE_MAX_OFF_PATH = 250;
                final int SWIPE_THRESHOLD_VELOCITY = 200;
                if ((e1 != null && e2 != null)) {
                    if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                        return false;
                    }
                    if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) { // right to left
                        //contactCallLayout.setVisibility(View.GONE);
                    } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) { // left to right
                        //contactCallLayout.setVisibility(View.VISIBLE);
                    }
                }
                return true;
            }

        }
    }

    private synchronized void restartLoader(int id, Bundle args, LoaderManager.LoaderCallbacks<Cursor> callback) {
        String log = "ContactsTabContent  - restartLoader";
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

    private boolean containsSpecialCharacter(String s) {
        return s != null && s.matches("[^A-Za-z0-9 ]");
    }

    public boolean isLoaderInitialize() {
        return loaderInitialize;
    }
}
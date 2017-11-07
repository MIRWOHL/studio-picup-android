package com.picup.calling.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import com.picup.calling.PopupContactDetailEmailTabContent;
import com.picup.calling.PopupContactDetailPhoneTabContent;

import com.picup.calling.util.PicupImageUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.picup.calling.R;

public final class PopupContactDetailDialogFragment extends PicupDialogFragment implements LoaderManager.LoaderCallbacks<Cursor>,
                                                                                     View.OnClickListener {
    private static final String ARG_CONTACT_ID = "android.idt.net.com.picup.calling.PopupContactDetailDialogFragment.CONTACT_ID";
    private static final String CURRRENT_TAB_INDEX = "currentTabIndex";

    private static final int PROFILE_LOADER_ID = 0;
    private static final String[] PROFILE_PROJECTION = {Contacts._ID, Contacts.LOOKUP_KEY, Contacts.PHOTO_THUMBNAIL_URI, Contacts.DISPLAY_NAME_PRIMARY, Contacts.DISPLAY_NAME_SOURCE, Contacts.DISPLAY_NAME_SOURCE};
    private static final int PROFILE_LOOKUP_KEY_URI_INDEX = 1;
    private static final int PROFILE_PHOTO_THUMBNAIL_URI_INDEX = 2;
    private static final int PROFILE_DISPLAY_NAME_PRIMARY_INDEX = 3;

    private static int contactId;
    private static String lookupKey;

    private ImageView photoImageView = null;
    private TextView initialTextView = null;
    private TextView fullNameTextView = null;
    private ImageView editImageView = null;

    private static TabLayout tabLayout = null;
    private ViewPager viewPager = null;
    private LocalPagerAdapter pagerAdapter = null;

    private TabLayout.ViewPagerOnTabSelectedListener viewPagerOnTabSelectedHandler = null;
    private ViewPager.SimpleOnPageChangeListener tabLayoutOnPageChangeHandler = null;

    private OnContactDetailFragmentListener listener;

    public PopupContactDetailDialogFragment() {
        // Required empty public constructor
    }

    public static PopupContactDetailDialogFragment newInstance(int contactId) {
        PopupContactDetailDialogFragment fragment = new PopupContactDetailDialogFragment();
        Bundle args = new Bundle(1);
        args.putInt(ARG_CONTACT_ID, contactId);
        fragment.setArguments(args);
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
        View view = inflater.inflate(R.layout.contact_detail, container, false);
        photoImageView = (ImageView)view.findViewById(R.id.thumbnail_imageview);
        initialTextView = (TextView)view.findViewById(R.id.initial_textview);
        fullNameTextView = (TextView)view.findViewById(R.id.duration_textview);

        editImageView = (ImageView)view.findViewById(R.id.edit_imageview);
        editImageView.setOnClickListener(this);
        
        tabLayout = (TabLayout)view.findViewById(R.id.contact_detail_tablayout);
        TabLayout.Tab phoneTab = tabLayout.newTab().setCustomView(R.layout.phone_tab).setTag("Phone_Tab");
        tabLayout.addTab(phoneTab);
        TabLayout.Tab emailTab = tabLayout.newTab().setCustomView(R.layout.email_tab).setTag("Email_Tab");
        tabLayout.addTab(emailTab);

        viewPager = (ViewPager) view.findViewById(R.id.contact_detail_viewpager);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Drawable photoThumbnailDrawable = photoImageView.getDrawable();
        // round the icon
        RoundedBitmapDrawable roundedBitmapDrawable = PicupImageUtils.toRoundedBitmapDrawable(getActivity(), photoThumbnailDrawable);
        if (roundedBitmapDrawable != null) { // should be true
            photoImageView.setImageDrawable(roundedBitmapDrawable);
        }
        // dimmer background activity
        dimmerBy(0.4f);
        pagerAdapter = new LocalPagerAdapter(getChildFragmentManager(), getActivity(), tabLayout.getTabCount());

        viewPager.setAdapter(pagerAdapter);
        //viewPager.storeAdapter(pagerAdapter);

        viewPagerOnTabSelectedHandler = new ViewPagerOnTabSelectedHandler(viewPager);
        tabLayout.addOnTabSelectedListener(viewPagerOnTabSelectedHandler);

        tabLayoutOnPageChangeHandler = new TabLayoutOnPageChangeHandler();
        viewPager.addOnPageChangeListener(tabLayoutOnPageChangeHandler);

        //tabLayout.setupWithViewPager(viewPager);
        Bundle args = new Bundle(1);
        args.putInt(ARG_CONTACT_ID, contactId);
        getLoaderManager().initLoader(PROFILE_LOADER_ID, args, this);

        if (savedInstanceState != null) {
            int currentTabIndex = savedInstanceState.getInt(CURRRENT_TAB_INDEX, 0);
            tabLayout.getTabAt(currentTabIndex).select();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        // set background activity's opacity back to normal
        dimmerBy(0.0f);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnContactDetailFragmentListener) {
            listener = (OnContactDetailFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnContactDetailFragmentListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRRENT_TAB_INDEX, tabLayout.getSelectedTabPosition());
    }

    @Override
    public void onDestroy() {
        viewPager.removeOnPageChangeListener(tabLayoutOnPageChangeHandler);
        tabLayout.removeOnTabSelectedListener(viewPagerOnTabSelectedHandler);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = null;

        if (id == PROFILE_LOADER_ID) {
            if (args != null) {
                int contactId = args.getInt(ARG_CONTACT_ID);
                if (contactId > 0) {
                    String selection = Contacts._ID + " = ? ";
                    String[] selectionArgs = new String[]{String.valueOf(contactId)};
                    cursorLoader = new CursorLoader(getActivity(), Contacts.CONTENT_URI, PROFILE_PROJECTION, selection, selectionArgs, null);
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
        int id = loader.getId();
        if (id == PROFILE_LOADER_ID) {
            if (data == null || data.isClosed()) {
                return;
            }
            try {
                if (data.moveToFirst()) {
                    lookupKey = null;
                    int lookupKeyIndex = data.getColumnIndex(Contacts.LOOKUP_KEY);
                    if (lookupKeyIndex != -1) {
                        lookupKey = data.getString(lookupKeyIndex);
                    }
                    String photoUriString = null;
                    int photoUriStringIndex = data.getColumnIndex(Contacts.PHOTO_THUMBNAIL_URI);
                    if (photoUriStringIndex != -1) {
                        photoUriString = data.getString(photoUriStringIndex);
                    }
                    String displayNamePrimary = null;
                    int displayNamePrimaryIndex = data.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY);
                    if (displayNamePrimaryIndex != -1) {
                        displayNamePrimary = data.getString(displayNamePrimaryIndex);
                    }
                    int displayNameSource = 0;
                    int displayNameSourceIndex = data.getColumnIndex(Contacts.DISPLAY_NAME_SOURCE);
                    if (displayNameSourceIndex != -1) {
                        displayNameSource = data.getInt(displayNameSourceIndex);
                    }
                    if (displayNameSource != ContactsContract.DisplayNameSources.STRUCTURED_NAME
                            && displayNameSource != ContactsContract.DisplayNameSources.STRUCTURED_PHONETIC_NAME
                            && displayNameSource != ContactsContract.DisplayNameSources.NICKNAME) {
                        //UNITE-1841 name come from diff source
                        if (photoImageView != null) {
                            Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.icon_for_no_name_contact);
                            RoundedBitmapDrawable roundedBitmapDrawable = PicupImageUtils.toRoundedBitmapDrawable(getActivity(), drawable);
                            photoImageView.setImageDrawable(roundedBitmapDrawable);
                        }
                    } else if (!TextUtils.isEmpty(photoUriString)) {
                        if (photoImageView != null) {
                            photoImageView.setImageURI(Uri.parse(photoUriString));
                            Drawable photoThumbnailDrawable = photoImageView.getDrawable();
                            // round the icon
                            RoundedBitmapDrawable roundedBitmapDrawable = PicupImageUtils.toRoundedBitmapDrawable(getActivity(), photoThumbnailDrawable);
                            photoImageView.setImageDrawable(roundedBitmapDrawable);
                        }
                    } else {
                        if (initialTextView != null) {
                            initialTextView.setText("");
                            String[] chunks = null;
                            if (displayNamePrimary != null) {
                                chunks = displayNamePrimary.split(" ");
                            }
                            int length = 0;
                            if (chunks != null) {
                                length = chunks.length;
                            }
                            if (length >= 1) {
                                String firstName = chunks[0];
                                if (!TextUtils.isEmpty(firstName)) {
                                    initialTextView.append(firstName.subSequence(0, 1));
                                }
                            }
                            if (length >= 2) {
                                String lastName = chunks[1];
                                if (!TextUtils.isEmpty(lastName)) {
                                    initialTextView.append(lastName.subSequence(0, 1));
                                }
                            }
                        }
                    }
                    if (fullNameTextView != null) {
                        fullNameTextView.setText(displayNamePrimary);
                    }
                }
            } catch (Throwable t) {
            }
        } else {
            // handle more loaders here.
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == PROFILE_LOADER_ID) {
            photoImageView.setImageURI(null);
        } else {
            // handle more loaders here
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == editImageView.getId()) {
            listener.editContact(contactId, lookupKey);
        }
    }

    public interface OnContactDetailFragmentListener {
        void editContact(int contactId, String lookupKey);
    }

    private static class LocalPagerAdapter extends FragmentPagerAdapter {
        private int tabCount = 0;
        private Context context = null;
        private FragmentManager fm = null;

        public LocalPagerAdapter(FragmentManager fm, Context context, int tabCount) {
            super(fm);
            this.fm = fm;
            this.context = context;
            this.tabCount = tabCount;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;

            switch (position) {
                case 0:
                    fragment = PopupContactDetailPhoneTabContent.newInstance(contactId);
                    break;
                case 1:
                    fragment = PopupContactDetailEmailTabContent.newInstance(contactId);
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
            return null;
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
}

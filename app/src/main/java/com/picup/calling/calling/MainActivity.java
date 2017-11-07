package com.picup.calling;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DataSetObserver;
import com.picup.calling.adapter.CallFromListArrayAdapter;
import com.picup.calling.adapter.CallFromSpinnerArrayAdapter;
import com.picup.calling.adapter.CallFromTypeSpinnerArrayAdapter;
import com.picup.calling.base.PicupActivity;
import com.picup.calling.base.PicupApplication;
import com.picup.calling.base.PicupViewPager;
import com.picup.calling.data.Email;
import com.picup.calling.dialog.AddContactOptionDialogFragment;
import com.picup.calling.dialog.BaseDialogFragment;
import com.picup.calling.dialog.ErrorDialogFragment;
import com.picup.calling.dialog.LogCatDialogFragment;
import com.picup.calling.dialog.PopupContactDetailDialogFragment;
import com.picup.calling.helper.AddressBookHelper;
import com.picup.calling.network.AuthenticateInfo;
import com.picup.calling.network.CallAccess;
import com.picup.calling.network.CallAccessForm;
import com.picup.calling.network.LineNumbers;
import com.picup.calling.network.PicupService;
import com.picup.calling.network.Token;
import com.picup.calling.util.Logger;
import com.picup.calling.util.PicupImageUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.ImmutableMap;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.Phonenumber;
import com.picup.calling.R;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.picup.calling.BuildConfig.LOGCAT_ENABLED;

public final class MainActivity extends PicupActivity implements NavigationView.OnNavigationItemSelectedListener,
                                                           CallsTabContent.OnCallsTabContentListener,
                                                           KeypadTabContent.OnKeypadTabContentListener,
                                                           ContactsTabContent.OnContactsTabContentListener,
                                                           PopupContactDetailDialogFragment.OnContactDetailFragmentListener,
                                                           PopupContactDetailPhoneTabContent.OnContactDetailPhoneTabContentListener,
                                                           PopupContactDetailEmailTabContent.OnContactDetailEmailTabContentListener,
                                                           CallsTabCompanyCallsFragment.OnCallsTabCompanyCallsFragmentListener,
                                                           CallsTabDepartmentCallsFragment.OnCallsTabDepartmentCallsFragmentListener,
                                                           CallsTabYourCallsFragment.OnCallsTabYourCallsFragmentListener,
                                                           AddContactOptionDialogFragment.OnAddContactOptionDialogFragmentListener,
                                                           View.OnClickListener, View.OnFocusChangeListener, TextWatcher,
                                                           ErrorDialogFragment.OnErrorDialogFragmentListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CALL_PHONE = 1;
    private static final String KEY_CURRENT_TAB_INDEX = "android.idt.net.com.picup.calling.MainActivity.KEY_CURRENT_TAB_INDEX";

    private static TabLayout tabLayout = null;
    private static Toolbar toolbar = null;
    private DrawerLayout drawer = null;
    private ActionBarDrawerToggle toggle = null;

    private static LinearLayout callFromLayout = null;
    private static Spinner callFromSpinner = null;
    private static TextView callFromSpinnerMask = null;
    private static ArrayAdapter<String> callFromArrayAdapter = null;

    private static LinearLayout callFromTypeLayout = null;
    private Spinner callFromTypeSpinner = null;
    private static ArrayAdapter<CharSequence> callFromTypeArrayAdapter = null;

    private static RelativeLayout searchLayout = null;
    private static SearchView searchView = null;
    private static EditText searchEditText = null;
    private ImageView magnifierImageView = null;
    private TextView hintTextView = null;
    private ImageView clearSearchImageView = null;

    private PicupViewPager viewPager = null;
    private static LocalPagerAdapter pagerAdapter = null;

    private static FloatingActionButton addContactButton = null;

    private TabLayout.ViewPagerOnTabSelectedListener viewPagerOnTabSelectedHandler = null;
    private TabLayout.OnTabSelectedListener menuOnTabSelectedHandler = null;
    private ViewPager.SimpleOnPageChangeListener menuOnPageChangeHandler = null;
    private ViewPager.SimpleOnPageChangeListener tabLayoutOnPageChangeHandler = null;

    private static boolean needCalling = false;
    private static String callToNumber = null;

    private User user = null;
    // For navigation header
    private static TextView fullNameTextView = null;
    private static ListView callFromListView = null;
    private static ImageView photoImageView = null;
    private static TextView initialTextView = null;
    private static TextView logoutTextView = null;

    private static final String[] DRAWER_CONTACT_PROJECTION = {Contacts._ID, Contacts.LOOKUP_KEY, Contacts.DISPLAY_NAME_PRIMARY, Contacts.HAS_PHONE_NUMBER, Contacts.PHOTO_URI};

    private static Stack<Integer> previouslySelectedTabIndices = new Stack<>();
    private static boolean backKeyHandling = false;

    private static DataSetObserver callFromDataSetObserver = new LocalDataSetObserver();

    private static PicupService picupService = null;

    private float x = 0;
    private float y = 0;

    private static CallAccessForm callAccessForm = null;

    //PhoneStateListener to bring User to "Call" tab when hangup.
    private CustomPhoneStateListener customPhoneStateListener;

    //Locat dialog
    private LogCatDialogFragment logCatDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.log("MainActivity - onCreate");
        setContentView(R.layout.activity_main);

        tabLayout = (TabLayout)findViewById(R.id.tab_layout);
        TabLayout.Tab callsTab = tabLayout.newTab().setCustomView(R.layout.calls_tab).setTag("Calls_Tab"); // tag will be used when referring to specific tab
        tabLayout.addTab(callsTab);
        TabLayout.Tab contactsTab = tabLayout.newTab().setCustomView(R.layout.contacts_tab).setTag("Contacts_Tab");
        tabLayout.addTab(contactsTab);
        TabLayout.Tab keypadTab = tabLayout.newTab().setCustomView(R.layout.keypad_tab).setTag("Keypad_Tab");
        tabLayout.addTab(keypadTab);

        toolbar = (Toolbar)findViewById(R.id.toolbar);

        // so there is no inset between 'hamburger' icon and toolbar_content
        toolbar.setContentInsetStartWithNavigation(0);

        callFromLayout = (LinearLayout)toolbar.findViewById(R.id.call_from_layout);
        callFromSpinner = (Spinner)toolbar.findViewById(R.id.call_from_spinner);
        callFromSpinner.setVisibility(View.INVISIBLE);
        callFromSpinnerMask = (TextView) toolbar.findViewById(R.id.call_from_spinner_mask);
        //String[] callFromNumbers = getResources().getStringArray(R.array.faked_call_from_numbers);
        //callFromArrayAdapter = new CallFromSpinnerArrayAdapter(this, R.layout.call_from_spinner_view, R.id.phone_textview, callFromNumbers);
        //ArrayAdapter<CharSequence> callFromArrayAdapter = ArrayAdapter.createFromResource(this, R.array.faked_call_from_numbers, android.R.layout.simple_list_item_1);
        //callFromSpinner.setAdapter(callFromArrayAdapter);
        //callFromSpinner.setSelection(1);

        // searchView
        searchLayout = (RelativeLayout)findViewById(R.id.search_layout);
        searchLayout.setVisibility(View.GONE);
        searchView = (SearchView)findViewById(R.id.search_view);
        searchView.setQueryHint("Who are you looking for?");
        searchView.setIconifiedByDefault(false);
        //searchView.setBackgroundResource(R.color.lightGray);

        //searchView.setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT));

        int searchEditFrameId = getResources().getIdentifier("android:id/search_edit_frame", null, null);
        LinearLayout searchEditFrameLayout = (LinearLayout)searchView.findViewById(searchEditFrameId);
        //searchEditFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        searchEditFrameLayout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
/*
        // set background color of search plate to white
        final int searchPlateId = getResources().getIdentifier("android:id/search_plate", null, null);
        final View searchPlate = searchView.findViewById(searchPlateId);
        searchPlate.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
*/
        // set foreground color of magnifier icon to text color
        final int searchMagIconId = getResources().getIdentifier("android:id/search_button", null, null);
        final ImageView searchMagIcon = (ImageView)searchView.findViewById(searchMagIconId);
        searchMagIcon.setColorFilter(ContextCompat.getColor(this, R.color.textColor));

        // set foreground color of search text to text color
        final int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        final TextView searchSrcTextView = (TextView)searchView.findViewById(searchSrcTextId);
        searchSrcTextView.setFocusable(true);
        searchSrcTextView.setFocusableInTouchMode(true);
        searchSrcTextView.setTextColor(ContextCompat.getColor(this, R.color.darkGreen));
        // framework doesn't hide soft keyboard when navigation button is tapped
        // I have to explicitly do it here
        searchSrcTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.getId() == searchSrcTextId) {
                    if (!hasFocus) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(searchSrcTextView.getWindowToken(), 0);
                    }
                }
            }
        });
        // set foreground color of hint to text color
        // SearchView shows hint text withing a private drawable; no access to this drawable in order to twist its text color
        // ???????????????????????????????????

        // set foreground color of close button to text color
        int closeImageViewId = getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeImageView = (ImageView)searchView.findViewById(closeImageViewId);
        closeImageView.setColorFilter(ContextCompat.getColor(this, R.color.textColor));
/*
        closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tabLayout.getSelectedTabPosition() == 1) {
                    ContactsTabContent contactsTabContent = (ContactsTabContent)pagerAdapter.getItem(1);
                    contactsTabContent.leaveSearch();
                }
                Log.d(TAG, "onClick");
            }
        });
*/

        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setBackgroundResource(R.color.lightGray);

        searchEditText = (EditText)findViewById(R.id.search_edittext);
        searchEditText.addTextChangedListener(this);

        magnifierImageView = (ImageView)findViewById(R.id.magnifier_imageview);
        magnifierImageView.setOnClickListener(this);

        hintTextView = (TextView)findViewById(R.id.hint_textview);

        clearSearchImageView = (ImageView)findViewById(R.id.clear_search_imageview);
        clearSearchImageView.setOnClickListener(this);
        clearSearchImageView.setVisibility(View.INVISIBLE);

        callFromTypeLayout = (LinearLayout)toolbar.findViewById(R.id.call_from_type_layout);
        callFromTypeSpinner = (Spinner)toolbar.findViewById(R.id.call_from_type_spinner);
        callFromTypeArrayAdapter = new CallFromTypeSpinnerArrayAdapter(this, R.layout.call_from_type_spinner_view, R.id.call_from_type_textview);
        callFromTypeArrayAdapter.addAll(getResources().getStringArray(R.array.call_from_types));
        callFromTypeSpinner.setAdapter(callFromTypeArrayAdapter);
        callFromTypeLayout.setVisibility(View.GONE);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        viewPager = (PicupViewPager)findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(2);

        menuOnPageChangeHandler = new MenuOnPageChangeHandler();
        //viewPager.addOnPageChangeListener(menuOnPageChangeHandler);

        RelativeLayout contentMain = (RelativeLayout)findViewById(R.id.content_main);

        addContactButton = (FloatingActionButton)findViewById(R.id.add_contact_floating_action_button);
        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent addContactIntent = new Intent(MainActivity.this, NewContactActivity.class);
                startActivity(addContactIntent);*/
                //Intent newOrUpdateContactIntent = new Intent(MainActivity.this, NewOrUpdateContactActivity.class);
                //startActivity(newOrUpdateContactIntent);
                Logger.log("MainActivity - onCreate - addContactButton - onClick");
                if (this == null || isFinishing()) {
                    return;
                }
                AddressBookHelper.initCreateContact(MainActivity.this, "");
            }
        });
        //addContactButton.setPadding(ic_key_0,ic_key_0,ic_key_0,ic_key_0);
        //addContactButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add_contact));
/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle = new LocalActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.setDrawerListener(toggle); // deprecated
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            Menu menu = navigationView.getMenu();
            MenuItem logcat = null;
            if (menu != null) {
                logcat = menu.findItem(R.id.nav_log);
            }
            if (logcat != null) {
                if (LOGCAT_ENABLED) {
                    logcat.setVisible(true);
                } else {
                    logcat.setVisible(false);
                }
            }
        }
        View headerLayout = navigationView.getHeaderView(0);
        fullNameTextView = (TextView)headerLayout.findViewById(R.id.duration_textview);
        callFromListView = (ListView)headerLayout.findViewById(R.id.call_from_listview);
        callFromListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        initialTextView = (TextView)headerLayout.findViewById(R.id.initial_textview);
        photoImageView = (ImageView)headerLayout.findViewById(R.id.thumbnail_imageview);
        logoutTextView = (TextView)headerLayout.findViewById(R.id.logout_textview);
        logoutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PicupApplication.resetData();
                Intent signOnIntent = new Intent(MainActivity.this, SignOnActivity.class);
                startActivity(signOnIntent);
                finish();
            }
        });

        navigationView.setNavigationItemSelectedListener(this);

        //addContactButton.setVisibility(viewPager.getCurrentItem() == 2 ? View.VISIBLE : View.GONE);
        picupService = PicupService.retrofit.create(PicupService.class);
        if (PicupApplication.callAccessForm == null) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            PicupApplication.initCallAccessForm(this, telephonyManager);
        }
        callAccessForm = PicupApplication.callAccessForm;

        setPhoneListener(this, true);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Logger.log("MainActivity - onPostCreate");
        // moved down from onCreate(bundle)
        // otherwise, adb crashes - Following instantiations seem to be too fast to Android framework.
        pagerAdapter = new LocalPagerAdapter(getSupportFragmentManager(), this, tabLayout.getTabCount());

        viewPager.setAdapter(pagerAdapter);

        final ContactsTabContent contactsTabContent = (ContactsTabContent)pagerAdapter.getItem(1);
        // ContactsTabContent is interested in listening to the input text being typed into searchView for searching
        searchView.setOnQueryTextListener(contactsTabContent);
        // notifying contactsTabContent about the searchLayout's visibility change
        searchEditText.addTextChangedListener(contactsTabContent);
        // this call removes the contents of the tabs
        //tabLayout.setupWithViewPager(viewPager);

        // tabLayout informing viewPager about tab selection (touching, maybe)
        viewPagerOnTabSelectedHandler = new ViewPagerOnTabSelectedHandler(viewPager);
        tabLayout.addOnTabSelectedListener(viewPagerOnTabSelectedHandler);
        // tabLayout informing action menu about tab selection (touching, maybe)
        menuOnTabSelectedHandler = new MenuOnTabSelectedHandler();
        tabLayout.addOnTabSelectedListener(menuOnTabSelectedHandler);
        // viewPager informing tabLayout about content page selection (swiping, maybe)
        //tabLayoutOnPageChangeHandler = new TabLayoutOnPageChangeHandler();
        // viewPager informing action menu about content page selection (swiping, maybe)
        //viewPager.addOnPageChangeListener(tabLayoutOnPageChangeHandler);

        addContactButton.setVisibility(viewPager.getCurrentItem() == 1 ? View.VISIBLE : View.GONE);

        final int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        final TextView searchSrcTextView = (TextView)searchView.findViewById(searchSrcTextId);
        searchSrcTextView.setTextColor(ContextCompat.getColor(this, R.color.textColor));
        // contactsTabContent interested in focus changes of searchView
        searchSrcTextView.setOnFocusChangeListener(contactsTabContent);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Logger.log("MainActivity - onRestoreInstanceState");
        int currentTabIndex = savedInstanceState.getInt(KEY_CURRENT_TAB_INDEX);
        tabLayout.getTabAt(currentTabIndex).select();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.log("MainActivity - onStart");

        LineNumbers lineNumbers = PicupApplication.lineNumbers;
        if (lineNumbers != null) {
            callFromArrayAdapter = new CallFromSpinnerArrayAdapter(this, R.layout.call_from_spinner_view, R.id.phone_textview);
            callFromArrayAdapter.registerDataSetObserver(callFromDataSetObserver);
            List<String> numbers = lineNumbers.getNumbers();
            if (numbers != null) {
                for (String lineNumber : numbers) {
                    callFromArrayAdapter.add(lineNumber);
                }
                //UNITE-1739 - disable spinner if lineNumber is only 1
                if (numbers.size() == 1) {
                    callFromSpinner.setEnabled(false);
                    if (callFromSpinnerMask != null) {
                        callFromSpinnerMask.setVisibility(View.VISIBLE);
                        String line0 = numbers.get(0);
                        if (!TextUtils.isEmpty(line0)) {
                            line0 = PhoneNumberUtils.formatNumber(line0);
                        }
                        callFromSpinnerMask.setText(line0);
                    }
                } else {
                    callFromSpinner.setEnabled(true);
                    if (callFromSpinnerMask != null) {
                        callFromSpinnerMask.setVisibility(View.GONE);
                    }
                }
            }
            callFromSpinner.setAdapter(callFromArrayAdapter);
            if (callFromSpinner.getCount() > 1) {
                callFromSpinner.setSelection(1);
            }
        } else {
            int userId = PicupApplication.getUserId();
            String tokenId = PicupApplication.getToken();
            ImmutableMap<String, String> userIdParams = ImmutableMap.of("userId", String.valueOf(userId), "applicationId", PicupApplication.apiId);
            final Call<LineNumbers> call = picupService.picupNumbers(tokenId, userIdParams);
            call.enqueue(new Callback<LineNumbers>() {
                @Override
                public void onResponse(Call<LineNumbers> call, Response<LineNumbers> response) {
                    Logger.log("MainActivity - onStart - request picupNumbers - onResponse");
                    if (response.isSuccessful()) {
                        Logger.log("MainActivity - onStart - request picupNumbers - onResponse - successful");
                        PicupApplication.lineNumbers = response.body();
                        if (PicupApplication.lineNumbers != null) {
                            callFromArrayAdapter = new CallFromSpinnerArrayAdapter(MainActivity.this, R.layout.call_from_spinner_view, R.id.phone_textview);
                            callFromArrayAdapter.registerDataSetObserver(callFromDataSetObserver);
                            List<String> numbers = PicupApplication.lineNumbers.getNumbers();
                            if (numbers != null) {
                                for (String lineNumber : numbers) {
                                    callFromArrayAdapter.add(lineNumber);
                                }
                                //UNITE-1739 - disable spinner if lineNumber is only 1
                                if (numbers.size() == 1) {
                                    callFromSpinner.setEnabled(false);
                                    if (callFromSpinnerMask != null) {
                                        callFromSpinnerMask.setVisibility(View.VISIBLE);
                                        String line0 = numbers.get(0);
                                        if (!TextUtils.isEmpty(line0)) {
                                            line0 = PhoneNumberUtils.formatNumber(line0);
                                        }
                                        callFromSpinnerMask.setText(line0);
                                    }
                                } else {
                                    callFromSpinner.setEnabled(true);
                                    if (callFromSpinnerMask != null) {
                                        callFromSpinnerMask.setVisibility(View.GONE);
                                    }
                                }
                            }
                            callFromSpinner.setAdapter(callFromArrayAdapter);
                            if (callFromSpinner.getCount() > 1) {
                                callFromSpinner.setSelection(1);
                            }
                        } //else {
                          //  ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(0);
                          //  errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
                        //}
                    } else {
                        try {
                            Logger.log("MainActivity - onStart - request picupNumbers - onResponse - failure error:" + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(0);
                        errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
                    }
                }
                @Override
                public void onFailure(Call<LineNumbers> call, Throwable t) {
                    Logger.log("MainActivity - onStart - request picupNumbers - onFailure");
                    ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(1);
                    errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        Logger.log("MainActivity - onBackPressed");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            backKeyHandling = true;
            if (!previouslySelectedTabIndices.isEmpty()) {
                int previouslySelectedTabIndex = previouslySelectedTabIndices.pop();
                TabLayout.Tab tab = tabLayout.getTabAt(previouslySelectedTabIndex);
                tab.select();
            } else {
                super.onBackPressed();
            }
            backKeyHandling = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        // Search MenuItem to inform FragmentPager adapter about the searchView iconification events
        //SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        if (searchItem != null) {
/*
            MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
                String actionSearchTitle = getString(R.string.action_search);
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    if (TextUtils.equals(item.getTitle(), actionSearchTitle)) {
                        // searchView has expanded. Give up the space to it.
                        callFromTypeLayout.setVisibility(View.GONE);
                    }
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    if (TextUtils.equals(item.getTitle(), actionSearchTitle)) {
                        //searchView has collapsed. Take the space back.
                        callFromTypeLayout.setVisibility(View.VISIBLE);
                    }
                    return true;
                }
            });
*/
/*
            ContactsTabContent contactsTabContent = (ContactsTabContent)pagerAdapter.getItem(1);
            // ContactsTabContent is interested in listening to searchView's visibility
            MenuItemCompat.setOnActionExpandListener(searchItem, contactsTabContent);
            // ContactsTabContent is interested in listening to the input text being typed into searchView for searching
            searchView.setOnQueryTextListener(contactsTabContent);
*/
        }
/*
        if (searchView != null) {
            searchView.setBackgroundResource(R.color.lightGray);

            searchView.setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT));

            int searchEditFrameId = getResources().getIdentifier("android:id/search_edit_frame", null, null);
            LinearLayout searchEditFrameLayout = (LinearLayout)searchView.findViewById(searchEditFrameId);
            searchEditFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

            // twist foreground color of search text
            final int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
            final TextView searchSrcTextView = (TextView)searchView.findViewById(searchSrcTextId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                searchSrcTextView.setTextColor(getResources().getColor(R.color.textColor, null));
            } else {
                searchSrcTextView.setTextColor(getResources().getColor(R.color.textColor));
            }
            // framework doesn't hide soft keyboard when navigation button is tapped
            // I have to explicitly do it here
            searchSrcTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (v.getId() == searchSrcTextId) {
                        if (!hasFocus) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(searchSrcTextView.getWindowToken(), 0);
                        }
                    }
                }
            });
            // twist foreground color of close button
            int closeImageViewId = getResources().getIdentifier("android:id/search_close_btn", null, null);
            ImageView closeImageView = (ImageView)searchView.findViewById(closeImageViewId);
            closeImageView.setColorFilter(ContextCompat.getColor(this, R.color.textColor));

            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setBackgroundResource(R.color.lightGray);
        }
*/
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu == null) {
            return super.onPrepareOptionsMenu(menu);
        }
        if (tabLayout.getSelectedTabPosition() == 1) {
            MenuItem deleteMenuItem = menu.findItem(R.id.action_delete);
            // show 'Delete' menu item (within action menu) for 'Contacts' tab only
            deleteMenuItem.setVisible(tabLayout.getSelectedTabPosition() == 1 ? true : false);
        } else {
            // completely hide the action menu
            for (int i=0; i<menu.size(); i++) {
                menu.getItem(i).setVisible(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            //searchView.setIconified(false);
            return true;
        } else if (id == R.id.action_delete) {
            Intent deleteIntent = new Intent(this, DeleteActivity.class);
            startActivity(deleteIntent);
            return true;
        } else if (id == R.id.action_logout) {
            super.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.log("MainActivity - onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.log("MainActivity - onPause");

        if (isFinishing()) {
            setPhoneListener(this, false);

            if (logCatDialog != null) {
                try {
                    logCatDialog.dismiss();
                    logCatDialog = null;
                } catch (Throwable t) {
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_CURRENT_TAB_INDEX, tabLayout.getSelectedTabPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Logger.log("MainActivity - onPostResume");
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Logger.log("MainActivity - onNavigationItemSelected");
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Logger.log("MainActivity - onNavigationItemSelected - settings");
            appSettings();
            /*Intent settingsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.picup.com"));
            if (settingsIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(settingsIntent);
            }*/
        } else if (id == R.id.nav_about_the_app) {
            Logger.log("MainActivity - onNavigationItemSelected - about");
            Intent aboutTheAppIntent = new Intent(MainActivity.this, AboutAppActivity.class);
            startActivity(aboutTheAppIntent);
        } else if (id == R.id.nav_t_and_c) {
            Logger.log("MainActivity - onNavigationItemSelected - t&c");
            Intent termsIntent = new Intent(MainActivity.this, TermsActivity.class);
            startActivity(termsIntent);
        } else if (id == R.id.nav_log) {
            initLogcatDialog();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    protected void onDestroy() {
        Logger.log("MainActivity - onDestroy");
        drawer.removeDrawerListener(toggle);
        //viewPager.removeOnPageChangeListener(tabLayoutOnPageChangeHandler);
        //viewPager.removeOnPageChangeListener(menuOnPageChangeHandler);
        tabLayout.removeOnTabSelectedListener(menuOnTabSelectedHandler);
        tabLayout.removeOnTabSelectedListener(viewPagerOnTabSelectedHandler);

        setPhoneListener(this, false);

        super.onDestroy();
    }

    @Override
    public void keypadCall(String callToNumber) {
        String log = "MainActivity - keypadCall";
        log += " - callToNumber:";
        log += callToNumber;
        MainActivity.callToNumber = callToNumber;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            log += " - check permission";
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                try {
                    makeCall();
                } catch (NumberParseException npe) {
                    Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                }
            } else {
                MainActivity.needCalling = true;
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CALL_PHONE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                try {
                    makeCall();
                } catch (NumberParseException npe) {
                    Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void keypadAddNewNumber(String newNumber) {
        /*Intent addContactIntent = new Intent(this, NewContactActivity.class);
        addContactIntent.putExtra(NewContactActivity.ARGS_NEW_NUMBER, newNumber);
        startActivity(addContactIntent);*/
        if (this == null || isFinishing()) {
            return;
        }
        AddressBookHelper.initCreateContact(this, newNumber);
    }

    @Override
    public void keypadAddToExistContact(String newNumber) {
        if (this == null || isFinishing()) {
            return;
        }
        AddressBookHelper.initUpdateContacts(this, newNumber);
    }

    private void makeCall() throws NumberParseException {
        String log = "MainActivity - makeCall";
        Phonenumber.PhoneNumber phoneNumber = PicupApplication.phoneNumberUtil.parse(PhoneNumberUtils.stripSeparators(callToNumber), Locale.getDefault().getCountry());
        if (phoneNumber.hasCountryCode()) {
            MainActivity.callToNumber = phoneNumber.getCountryCode() + String.valueOf(phoneNumber.getNationalNumber());
        } else {
            MainActivity.callToNumber = String.valueOf(phoneNumber.getNationalNumber());
        }
        log += " - phoneNumber:";
        log += phoneNumber;
        String tokenId = PicupApplication.getToken();
        int accountId = PicupApplication.getAccountId();
        int userId = PicupApplication.getUserId();
        String picupNumber = (String)callFromSpinner.getSelectedItem();
        if (callAccessForm == null) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            PicupApplication.initCallAccessForm(this, telephonyManager);
            callAccessForm = PicupApplication.callAccessForm;
        }
        if (callAccessForm == null) {
            log += " - callAccessForm is null";
            Logger.log(log);
            return;
        }
        callAccessForm.setPicupNum(picupNumber);
        callAccessForm.setCallerIdNum(picupNumber);
        callAccessForm.setDialedNum(PhoneNumberUtils.stripSeparators(MainActivity.callToNumber));
        callAccessForm.setUserId(userId);
        log += " - accountId:";
        log += accountId;
        log += " - tokenId:";
        log += tokenId;
        Logger.log(log);
        Call<CallAccess> accessCall = picupService.makeCall(tokenId, String.valueOf(accountId), callAccessForm);
        accessCall.enqueue(new Callback<CallAccess>() {
            @Override
            public void onResponse(Call<CallAccess> call, Response<CallAccess> response) {
                if (response.isSuccessful()) {
                    Logger.log("MainActivity - makeCall - request makeCall - onResponse - successful");
                    CallAccess callAccess = response.body();
                    if (callAccess != null) {
                        String tan = callAccess.getTan();
                        AddressBookHelper.insertLogoContact(MainActivity.this, tan);
                        Intent phoneCallIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + tan));
                        try {
                            startActivity(phoneCallIntent);
                        } finally {
                            needCalling = false;
                        }
                    } else {
                        ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(3);
                        errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
                    }
                } else {
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
                //network issues
                ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(1);
                errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
            }
        });
    }

    @Override
    // ContactsTabContent.OnContactsTabContentListener
    //DO NOT USE AS IS...
    //TODO..if this is ever to be used change implementation...this method calls out directly rather than via TAN
    public void contactCall(String callToNumber) {
        Logger.log("MainActivity - contactCall");
        MainActivity.callToNumber = callToNumber;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                String callFromNumber = (String) callFromSpinner.getSelectedItem();
                Log.d(TAG, "contactCall():callFromNumber=" + callFromNumber + " callToNumber=" + callToNumber);
                Intent phoneCallIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + callToNumber));
                startActivity(phoneCallIntent);
            } else {
                MainActivity.needCalling = true;
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CALL_PHONE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Logger.log("MainActivity - onRequestPermissionsResult");
        switch (requestCode) {
            case PERMISSION_REQUEST_CALL_PHONE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (needCalling) {
                        try {
                            makeCall();
                        } catch (NumberParseException npe) {
                            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            default:
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    // ContactstabContent.OnContactsTabContentListener
    public void showContactDetail(int contactId) {
        Logger.log("MainActivity - showContactDetail");
        PopupContactDetailDialogFragment contactDetailDialogFragment = PopupContactDetailDialogFragment.newInstance(contactId);
        contactDetailDialogFragment.show(getSupportFragmentManager(), "PopupContactDetailDialogFragment");
    }

    @Override
    // ContactsTabContent.OnContactsTabContentListener
    // CallsTabContent.OnCallsTabCompanyCallsFragment
    // CallsTabContent.OnCallsTabDeartmentCallsFragment
    // CallsTabContent.OnCallsTabYourCallsFragment
    public void showContactProfile(Object source, int contactId) {
        Logger.log("MainActivity - showContactProfile");
        Intent contactProfileIntent = new Intent(this, ContactProfileActivity.class);
        contactProfileIntent.putExtra("contactId", contactId);
        contactProfileIntent.putExtra("picupNumber", PhoneNumberUtils.stripSeparators((String)callFromSpinner.getSelectedItem()));
        //if (source instanceof CallsTabCompanyCallsFragment.CallArrayAdapter || source instanceof CallsTabDepartmentCallsFragment.CallArrayAdapter || source instanceof CallsTabYourCallsFragment.CallArrayAdapter) {
        if (!(source instanceof CursorAdapter)) {
            contactProfileIntent.putExtra("selectedTabIndex", 1);
        }
        startActivity(contactProfileIntent);
    }

    @Override
    // CallsTabContent.OnCallsTabCompanyCallsFragment
    // CallsTabContent.OnCallsTabDeartmentCallsFragment
    // CallsTabContent.OnCallsTabYourCallsFragment
    public void showContactProfileForNonContact(Object source, String phoneNumber) {
        Logger.log("MainActivity - showContactProfile");
        Intent contactProfileIntent = new Intent(this, ContactProfileUnknownActivity.class);
        //contactProfileIntent.putExtra("contactId", contactId);
        contactProfileIntent.putExtra("nonContactPhoneNumber",phoneNumber);
        contactProfileIntent.putExtra("picupNumber", PhoneNumberUtils.stripSeparators((String)callFromSpinner.getSelectedItem()));
        //if (source instanceof CallsTabCompanyCallsFragment.CallArrayAdapter || source instanceof CallsTabDepartmentCallsFragment.CallArrayAdapter || source instanceof CallsTabYourCallsFragment.CallArrayAdapter) {
        if (!(source instanceof CursorAdapter)) {
            contactProfileIntent.putExtra("selectedTabIndex", 1);
        }
        startActivity(contactProfileIntent);
    }

    @Override
    public void showAddContactOption(final View sourceView, String phoneNumber) {
//        View addContactOptionView = getLayoutInflater().inflate(R.layout.add_contact_option_layout, null);
//        final ListView addContactOptionListView = (ListView)addContactOptionView.findViewById(R.id.add_contact_option_listview);
//        ArrayAdapter<String> addContactOptionAdapter = new ArrayAdapter(this, R.layout.add_contact_option_item, R.id.add_contact_option_textview, new String[]{"Update Existing", "Create Contact"});
//        addContactOptionListView.setAdapter(addContactOptionAdapter);
//        final PopupWindow popupWindow = new PopupWindow(addContactOptionView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        addContactOptionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String selectedOption = (String)parent.getItemAtPosition(position);
//                if (TextUtils.equals(selectedOption, "Update Existing")) {
//                    Log.d(TAG, "Update Existing");
//                } else if (TextUtils.equals(selectedOption, "Create Contact")) {
//                    Log.d(TAG, "Create Contact");
//                }
//                popupWindow.dismiss();
//            }
//        });
//        //popupWindow.showAtLocation(sourceView, Gravity.CENTER, (int)sourceView.getX(), (int)sourceView.getY());
//        int anchorX = popupWindow.getMaxAvailableHeight(sourceView) / (int)getResources().getDisplayMetrics().density;
//        //popupWindow.showAsDropDown(sourceView);
//        popupWindow.showAtLocation(sourceView, Gravity.CENTER, 0, 0);
//        //popupWindow.showAsDropDown(sourceView, Gravity.CENTER, (int) sourceView.getX(), (int) sourceView.getY());
//        popupWindow.setFocusable(true);
//        popupWindow.update();

        Logger.log("MainActivity - showAddContactOption");
        if (this == null || isFinishing()) {
            return;
        }
       AddContactOptionDialogFragment addContactOptionDialogFragment = AddContactOptionDialogFragment.newInstance(phoneNumber);
        addContactOptionDialogFragment.show(getSupportFragmentManager(), "AddContactOptionDialogFragment");
    }

    @Override
    public void listScrolling() {
        View focusedView = getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    // ContactsTabContent.OnContactsTabContentListener
    // CallTabCompanyCallsFragment.OnCallsTabCompanyCallsFragmentListener
    // CallTabDepartmentCallsFragment.OnCallsTabDepartmentCallsFragmentListener
    // CallTabYourCallsFragment.OnCallsTabYourCallsFragmentListener
    @Override
    public void explainPermission(String permissionName) {
        DialogFragment explainFragment = new PermissionExplainFragment();
        explainFragment.show(getSupportFragmentManager(), "explainFragment");
/*
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                DialogFragment explainFragment = new DialogFragment() {

                };
                return null;
            }
        };
*/
    }

    @Override
    public void call(String callToNumber) {
        MainActivity.callToNumber = callToNumber;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            try {
                makeCall();
            } catch (NumberParseException npe) {
                Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            }
        } else {
            needCalling = true;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CALL_PHONE);
        }
    }


/*
    @Override
    public void fetch(String callType) {
        CallsTabContent callsTabContent = (CallsTabContent)pagerAdapter.getItem(0);
        callsTabContent.fetch(callType);
    }
*/

    @Override
    // PopupContactDetailPhoneTabContent.OnContactProfileContactTabContentListener
    public void contactDetailCall(String callToNumber) {
        Logger.log("MainActivity - contactDetailCall - callToNumber:"+callToNumber);
        MainActivity.callToNumber = callToNumber;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            try {
                makeCall();
            } catch (NumberParseException npe) {
                Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            }
        } else {
            needCalling = true;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CALL_PHONE);
        }
    }

    @Override
    // PopupContactDetailEmailTabContent.OnContactDetailEmailTabContentListener
    public void doContactDetailEmailTabContentInteraction(Email emailType) {

    }

    @Override
    // PopupContactDetailDialogFragment.OnContactDetailFragmentListener,
    public void editContact(int contactId, String lookupKey) {
        Logger.log("MainActivity - editContact - contactId:"+contactId+" lookupKey:"+lookupKey);
        PicupApplication.needsRefresh = true;
        AddressBookHelper.initEditContact(this, contactId, lookupKey);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == clearSearchImageView.getId()) {
            searchEditText.setText("");
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        boolean isEmpty = searchEditText.length() == 0;
        magnifierImageView.setVisibility(isEmpty ? View.VISIBLE : View.INVISIBLE);
        hintTextView.setVisibility(isEmpty ? View.VISIBLE : View.INVISIBLE);
        clearSearchImageView.setVisibility(isEmpty ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void reAuthenticate() {
        Logger.log("MainActivity - reAuthenticate");
        Intent signOnIntent = new Intent(this, SignOnActivity.class);
        startActivity(signOnIntent);
        super.finish();
    }

    @Override
    public void appSettings() {
        Logger.log("MainActivity - appSettings");
        Intent settingsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.picup.com/account/"));
        if (settingsIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(settingsIntent);
        }
    }

    // AddContactOptionDialogFragment.AddContactOptionDialogFragment
    @Override
    public void updateExisting(String phoneNumber) {
        Logger.log("MainActivity - updateExisting - phoneNumber:"+phoneNumber);
        if (this == null || isFinishing()) {
            return;
        }
        PicupApplication.needsRefresh = true;
        AddressBookHelper.initUpdateContacts(this, phoneNumber);
    }

    // AddContactOptionDialogFragment.AddContactOptionDialogFragment
    @Override
    public void createContact(String phoneNumber) {
        /*Intent addContactIntent = new Intent(this, NewContactActivity.class);
        addContactIntent.putExtra(NewContactActivity.ARGS_NEW_NUMBER, phoneNumber);
        startActivity(addContactIntent);*/
        Logger.log("MainActivity - createContact - phoneNumber:"+phoneNumber);
        if (this == null || isFinishing()) {
            return;
        }
        PicupApplication.needsRefresh = true;
        AddressBookHelper.initCreateContact(this, phoneNumber);
    }

    private static class LocalDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            callFromSpinner.setVisibility(callFromArrayAdapter.getCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        }

        @Override
        public void onInvalidated() {
            callFromSpinner.setVisibility(View.INVISIBLE);
        }
    }

    public static class LocalPagerAdapter extends FragmentPagerAdapter {
        private int tabCount = 0;
        private Context context = null;

        private CallsTabContent callsTabContent = null;
        private ContactsTabContent contactsTabContent = null;
        private KeypadTabContent keypadTabContent = null;

        public LocalPagerAdapter(FragmentManager fm, Context context, int tabCount) {
            super(fm);
            this.context = context;
            this.tabCount = tabCount;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;

            switch (position) {
                case 0:
                    if (callsTabContent == null) {
                        callsTabContent = CallsTabContent.newInstance();
                    }
                    fragment = callsTabContent;
                    break;
                case 1:
                    if (contactsTabContent == null) {
                        contactsTabContent = ContactsTabContent.newInstance(searchLayout.getVisibility() == View.VISIBLE);
                    }
                    fragment = contactsTabContent;
                    break;
                case 2:
                    if (keypadTabContent == null) {
                        keypadTabContent = KeypadTabContent.newInstance();
                    }
                    fragment = keypadTabContent;
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
                    title = "Calls";
                    break;
                case 1:
                    title = "Contacts";
                    break;
                case 2:
                    title = "Keypad";
                    break;
                default:
                    title = super.getPageTitle(position);
            }
            return title;
        }
    }

    /**
     * Handler responding to tab selection event on TabLayout
     */
    private static class ViewPagerOnTabSelectedHandler extends TabLayout.ViewPagerOnTabSelectedListener {
        private ViewPager viewPager = null;
        public ViewPagerOnTabSelectedHandler(ViewPager viewPager) {
            super(viewPager);
            this.viewPager = viewPager;
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            Logger.log("MainActivity - ViewPagerOnTabSelectedHandler - onTabSelected - tab:"+tab.getTag().toString());
            viewPager.setCurrentItem(tab.getPosition());
            if (TextUtils.equals(tab.getTag().toString(), "Contacts_Tab")) {
                searchLayout.setVisibility(View.VISIBLE);
                addContactButton.setVisibility(View.VISIBLE);
                // show 'delete' menu item
                toolbar.getMenu().findItem(R.id.action_delete).setVisible(true);
            } else {
                searchLayout.setVisibility(View.GONE);
                addContactButton.setVisibility(View.GONE);
                // completely hide action menu
                Menu menu = toolbar.getMenu();
                for (int i=0; i<menu.size(); i++) {
                    menu.getItem(i).setVisible(false);
                }
            }
            // hide soft keyboard on tab switch
            InputMethodManager imm = (InputMethodManager)viewPager.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

/*
            boolean contactTabSelected = TextUtils.equals(tab.getTag().toString(), "Contacts_Tab");
            if (!contactTabSelected) {
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                    searchView.setIconifiedByDefault(true);
                    MenuItem searchMenuItem = toolbar.getMenu().findItem(R.id.action_search);
                    if (searchMenuItem != null) {
                        if (searchMenuItem.collapseActionView()) {
                            callFromTypeLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }
            } else {
                if (ActivityCompat.checkSelfPermission(viewPager.getContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {

                }
            }
*/
            //boolean keypadTabSelected = TextUtils.equals(tab.getTag().toString(), "Keypad_Tab");
            //addContactButton.setVisibility(contactTabSelected ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            if (!backKeyHandling) {
                previouslySelectedTabIndices.add(tab.getPosition());
            }
        }
    }

    /**
     * TabLayout to respond to page change event on ViewPager
     */
    private static class TabLayoutOnPageChangeHandler extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            Logger.log("MainActivity - TabLayoutOnPageChangeHandler - onPageSelected");
            TabLayout.Tab selectedTab = tabLayout.getTabAt(position);
            selectedTab.select();
            boolean contactTabSelected = TextUtils.equals(selectedTab.getTag().toString(), "Contacts_Tab");
            if (!contactTabSelected) {
                // 'push' searchView back into toolbar's action menu
                if (!searchView.isIconified()) {
                    MenuItem searchMenuItem = toolbar.getMenu().findItem(R.id.action_search);
                    if (searchMenuItem != null) {
                        if (!searchMenuItem.collapseActionView()) {
                            callFromTypeLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
            //boolean keypadTabSelected = TextUtils.equals(selectedTab.getTag().toString(), "Keypad_Tab");
            addContactButton.setVisibility(contactTabSelected ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Menu to respond to tab selection event on TabLayout
     */
    private static class MenuOnTabSelectedHandler implements TabLayout.OnTabSelectedListener {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
    /*
            MenuItem searchMenuItem = toolbar.getMenu().findItem(R.id.action_search);
            if (searchMenuItem != null) {
                if (!TextUtils.equals(tab.getTag().toString(), "Contacts_Tab")) {
                    if (searchMenuItem.isVisible()) {
                        searchMenuItem.setVisible(false);
                    }
                } else {
                    if (searchView.isIconified()) {
                        searchMenuItem.setVisible(true);
                    }
                }
            }
    */
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
        }
    }

    /**
     * Menu to response to page change event on ViewPager
     */
    private static class MenuOnPageChangeHandler extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            Logger.log("MainActivity - MenuOnPageChangeHandler - onPageSelected");
            TabLayout.Tab selectedTab = tabLayout.getTabAt(position);
            MenuItem searchMenuItem = toolbar.getMenu().findItem(R.id.action_search);
            if (searchMenuItem != null) {
                if (!TextUtils.equals(selectedTab.getTag().toString(), "Contacts_Tab")) {
                    if (searchMenuItem.isVisible()) {
                        searchMenuItem.setVisible(false);
                    }
                } else {
                    if (searchView.isIconified()) {
                        searchMenuItem.setVisible(true);
                    }
                }
            }
        }
    }

    private class LocalActionBarDrawerToggle extends ActionBarDrawerToggle {
        private Context context = null;

        public LocalActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, @StringRes int openDrawerContentDescRes, @StringRes int closeDrawerContentDescRes) {
            super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
            this.context = activity;
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            Logger.log("MainActivity - onDrawerOpened");
            if (PicupApplication.authenticateInfo != null) {
                AuthenticateInfo authInfo = PicupApplication.authenticateInfo;
                Logger.log("MainActivity - onDrawerOpened - authenticateInfo - accountId:"+ authInfo.getAccountId()+ " role="+ authInfo.getRole() + " businessClass:"+ authInfo.getBusinessClass() + " tn:"+authInfo.getTn());
            }

            String firstName = PicupApplication.authenticateInfo.getFirstName();
            String lastName = PicupApplication.authenticateInfo.getLastName();
            fullNameTextView.setText(firstName + " " + lastName);
            LineNumbers lineNumbers = PicupApplication.lineNumbers;
            if (lineNumbers != null) {
                ArrayAdapter<String> callFromArrayAdapter = new CallFromListArrayAdapter(context, R.layout.centered_call_from_view, R.id.phone_textview, lineNumbers.getNumbers());
                callFromListView.setAdapter(callFromArrayAdapter);
            } else {
                AuthenticateInfo authenticateInfo = PicupApplication.authenticateInfo;
                Token token = PicupApplication.authenticateInfo.getToken();
                ImmutableMap<String, String> userIdParams = ImmutableMap.of("userId", String.valueOf(authenticateInfo.getUserId()), "applicationId", PicupApplication.apiId);
                final Call<LineNumbers> call = picupService.picupNumbers(token.getId(), userIdParams);
                call.enqueue(new Callback<LineNumbers>() {
                    @Override
                    public void onResponse(Call<LineNumbers> call, Response<LineNumbers> response) {
                        String log = "MainActivity - onDrawerOpened - request picupNumbers - onResponse";

                        if (response.isSuccessful()) {
                            log += " - isSuccessful";
                            log += " - body:"+response.body();
                            Logger.log(log);

                            PicupApplication.lineNumbers = response.body();
                            if (PicupApplication.lineNumbers != null) {
                                ArrayAdapter<String> callFromArrayAdapter = new CallFromListArrayAdapter(context, R.layout.centered_call_from_view, R.id.phone_textview, PicupApplication.lineNumbers.getNumbers());
                                callFromListView.setAdapter(callFromArrayAdapter);
                            } //else {
                              //  ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(0);
                              //  errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
                            //}
                        } else {
                            try {
                                Logger.log("MainActivity - onDrawerOpened - request picupNumbers - onResponse - failure error:" + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<LineNumbers> call, Throwable t) {
                        ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(1);
                        errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
                    }
                });
            }
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            String[] projection = {Contacts._ID, Contacts.LOOKUP_KEY, Contacts.PHOTO_URI};
            String selection = ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME + " = ? AND " +
                               ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME + " = ? ";
            String[] selectionArgs = {PicupApplication.authenticateInfo.getFirstName(), PicupApplication.authenticateInfo.getLastName()};
            Cursor cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, projection, selection, selectionArgs, null);
            try {
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        // photo
                        String photoUriString = cursor.getString(2);
                        if (!TextUtils.isEmpty(photoUriString)) {
                            photoImageView.setImageURI(Uri.parse(photoUriString));
                            RoundedBitmapDrawable roundedBitmapDrawable = PicupImageUtils.toRoundedBitmapDrawable(context, photoImageView.getDrawable());
                            if (roundedBitmapDrawable != null) {
                                photoImageView.setImageDrawable(roundedBitmapDrawable);
                            }
                            photoImageView.setVisibility(View.VISIBLE);
                            initialTextView.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        if (!TextUtils.isEmpty(firstName)) {
                            initialTextView.setText(firstName.subSequence(0, 1));
                        }
                        if (!TextUtils.isEmpty(lastName)) {
                           initialTextView.append(lastName.subSequence(0, 1));
                        }
                        initialTextView.setVisibility(View.VISIBLE);
                        photoImageView.setVisibility(View.INVISIBLE);
                    }
                }
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        }
    }

    public static class PermissionExplainFragment extends DialogFragment {
        private static final int PERMISSION_REQUEST_READ_CONTACTS = 5;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.read_contacts_permission_explanation);
            builder.setNegativeButton(R.string.cancel_cap_label, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(R.string.ok_cap_label, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_READ_CONTACTS);
                    dialog.dismiss();
                }
            });
            return builder.create();
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            Log.d(TAG, "onRequestPermissioinsResult");
        }
    }

    private synchronized void setPhoneListener(Context context, boolean listen) {
        String log = "MainActivity - setPhoneListener";
        if (context == null) {
            log += " - context is null";
            Logger.log(log);
            return;
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            log += " - invalid permission";
            Logger.log(log);
            return;
        }
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            log += " - manager is null";
            Logger.log(log);
            return;
        }
        if (listen) {
            if (customPhoneStateListener == null) {
                customPhoneStateListener = new CustomPhoneStateListener();
            }
            telephonyManager.listen(customPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        } else {
            if (customPhoneStateListener == null) {
                return;
            }
            telephonyManager.listen(customPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    private class CustomPhoneStateListener extends PhoneStateListener {
        private int prevState = -1;
        @Override
        public void onCallStateChanged(int currentState, String incomingNumber) {
            super.onCallStateChanged(currentState, incomingNumber);
            onHandleOnCallStateChange(currentState, incomingNumber);
        }

        private synchronized void onHandleOnCallStateChange(int currentState, String incomingNumber) {
            String log = "MainActivity - CustomPhoneStateListener - onHandleOnCallStateChange";
            log += " - prevState:";
            log += prevState;
            log += " - currentState:";
            log += currentState;
            log += " - incomingNumber:";
            log += incomingNumber;

            if (currentState == TelephonyManager.CALL_STATE_IDLE) {
                log += " - call_state_idle";
                if (prevState != -1) {
                    if (prevState != currentState) {
                        final MainActivity activity = MainActivity.this;
                        if (activity != null && !activity.isFinishing()) {
                            PicupApplication.needsRefresh = true;
                            log += " - runnable";
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String log = "MainActivity - CustomPhoneStateListener - Runnable";
                                    TabLayout.Tab selectedTab = null;
                                    if (tabLayout != null) {
                                        selectedTab = tabLayout.getTabAt(0);
                                    }
                                    if (selectedTab == null) {
                                        log += " - tab is null";
                                        Logger.log(log);
                                        return;
                                    }
                                    log += " - select";
                                    try {
                                        selectedTab.select();
                                    } catch (IllegalArgumentException e) {
                                        log += " - IllegalArgumentException";
                                        Logger.log(log);
                                    }
                                }
                            });
                        }

                    }
                }
            }
            Logger.log(log);
            prevState = currentState;
        }
    }

    private synchronized void initLogcatDialog() {
        String log = "MainActivity - initLogcatDialog";
        if (this == null || isFinishing() || logCatDialog != null) {
            log += " - invalid state";
            Logger.log(log);
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager == null || fragmentManager.isDestroyed()) {
            log += " - invalid fragmentManager";
            Logger.log(log);
            return;
        }
        logCatDialog = new LogCatDialogFragment();
        logCatDialog.setOnDetachListener(new BaseDialogFragment.OnDetachListener() {
            @Override
            public void onDetach(int requestCode, int resultCode, Bundle data) {
                logCatDialog = null;
            }
        });
        try {
            logCatDialog.show(fragmentManager, LogCatDialogFragment.Tag);
        } catch (IllegalStateException e) {
        }
    }
}

package com.picup.calling;

import android.content.Context;
import android.database.DataSetObserver;
import com.picup.calling.adapter.ViewCallsForSpinnerArrayAdapter;
import com.picup.calling.base.PicupApplication;
import com.picup.calling.dialog.ErrorDialogFragment;
import com.picup.calling.network.CallHistory;
import com.picup.calling.network.Cdr;
import com.picup.calling.network.LineNumbers;
import com.picup.calling.network.PicupService;
import com.picup.calling.util.Logger;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.collect.ImmutableMap;
import com.picup.calling.R;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallsTabContent extends Fragment {
    private static final String TAG = CallsTabContent.class.getSimpleName();
    private static TabLayout tabLayout = null;
    private TabLayout.ViewPagerOnTabSelectedListener viewPagerOnTabSelectedHandler = null;
    private ViewPager.SimpleOnPageChangeListener tabLayoutOnPageChangeHandler = null;
    private static ViewPager viewPager = null;
    private static LocalPagerAdapter pagerAdapter = null;
    private static Spinner viewCallsForSpinner = null;
    private static TextView viewCallsForMask = null;
    private static ArrayAdapter<String> viewCallsForAdapter = null;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static DataSetObserver viewCallsForDataSetObserver = new LocalDataSetObserver();
    private static boolean initialized = true;
    private OnCallsTabContentListener listener;
    private static PicupService picupService = PicupService.retrofit.create(PicupService.class);
/*
    private NavigableMap<Calendar, List<Cdr>> data = new TreeMap<Calendar, List<Cdr>>(new Comparator<Calendar>() {
        @Override
        public int compare(Calendar cal1, Calendar cal2) {
            if (cal1.after(cal2)) {
                return -1;
            } else if (cal1.before(cal2)) {
                return 1;
            }
            return 0;
        }
    });
*/
    private static SortedSet<Cdr> cdrs = null;
    //private static CallHistory callHistory = null;

    public CallsTabContent() {
        // Required empty public constructor
    }

    public static CallsTabContent newInstance() {
        CallsTabContent fragment = new CallsTabContent();
        return fragment;
    }

    public static CallsTabContent newInstance(String param1, String param2) {
        CallsTabContent fragment = new CallsTabContent();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Logger.log("CallsTabContent - onAttach");
        if (context instanceof OnCallsTabContentListener) {
            listener = (OnCallsTabContentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnCallsTabContentListener ");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.log("CallsTabContent - onCreate");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        initialized = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.log("CallsTabContent - onCreateView");
        View callsTabContentView = inflater.inflate(R.layout.calls_tab_content, container, false);
        tabLayout = (TabLayout)callsTabContentView.findViewById(R.id.tab_layout);

        //UNITE...ONLY all calls for v1

        //UNITE 1820 - only 2 tabs Department and Your...using Company as Department for now
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.calls_company_calls_tab).setTag("CompanyCallsTab"));
        //tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.calls_department_calls_tab).setTag("DepartmentCallsTab"));
        //tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.calls_your_calls_tab).setTag("YourCallsTab"));
        /*
        //UNITE 1490
        if (PicupApplication.isAdmin())
            tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.calls_company_calls_tab).setTag("CompanyCallsTab"));
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.calls_department_calls_tab).setTag("DepartmentCallsTab"));
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.calls_your_calls_tab).setTag("YourCallsTab"));
*/
        viewPager = (ViewPager)callsTabContentView.findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(2);

        viewCallsForSpinner = (Spinner)callsTabContentView.findViewById(R.id.view_calls_for_spinner);
        viewCallsForMask = (TextView) callsTabContentView.findViewById(R.id.view_calls_for_spinner_mask);

/*
        viewCallsForSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
*/

        //viewCallsForSpinner.setOnItemSelectedListener(pagerAdapter);
        return callsTabContentView;
    }

    public void onResume() {
        super.onResume();
        Logger.log("CallsTabContent - onResume - getUserVisibleHint:" + getUserVisibleHint());

        if (getUserVisibleHint()) {
                  Logger.log("CallsTabContent - onResume - needsRefresh:"+PicupApplication.needsRefresh);
                if (PicupApplication.needsRefresh) {
                    PicupApplication.needsRefresh = false;
                    int selectedTabIndex = tabLayout.getSelectedTabPosition();
                    //UNITE -- only company calls for v1
                    switch (selectedTabIndex) {
                        case 0:
                            CallsTabCompanyCallsFragment callsTabCompanyCallsFragment = (CallsTabCompanyCallsFragment) pagerAdapter.getItem(selectedTabIndex);
                            callsTabCompanyCallsFragment.doRefresh();
                    }
                }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.log("CallsTabContent - onActivityCreated");
        
        picupService = PicupService.retrofit.create(PicupService.class);

        LineNumbers lineNumbers = PicupApplication.lineNumbers;
        if (lineNumbers != null) {
            Logger.log("CallsTabContent - onActivityCreated - lineNumbers count:"+lineNumbers.getNumbers().size());
            viewCallsForAdapter = new ViewCallsForSpinnerArrayAdapter(getActivity(), R.layout.view_calls_for_spinner_view, R.id.phone_textview);
            viewCallsForAdapter.registerDataSetObserver(viewCallsForDataSetObserver);
            List<String> numbers = lineNumbers.getNumbers();
            if (numbers != null) {
                for (String lineNumber : numbers) {
                    viewCallsForAdapter.add(lineNumber);
                }
                //UNITE-1739 - disable spinner if lineNumber is only 1
                if (numbers.size() == 1) {
                    viewCallsForSpinner.setEnabled(false);
                    if (viewCallsForMask != null) {
                        String line0 = numbers.get(0);
                        if (!TextUtils.isEmpty(line0)) {
                            line0 = PhoneNumberUtils.formatNumber(line0);
                        }
                        viewCallsForMask.setText(line0);
                        viewCallsForMask.setVisibility(View.VISIBLE);
                    }
                } else {
                    viewCallsForSpinner.setEnabled(true);
                    if (viewCallsForMask != null) {
                        viewCallsForMask.setVisibility(View.GONE);
                    }
                }
            }
            viewCallsForSpinner.setAdapter(viewCallsForAdapter);
            if (viewCallsForSpinner.getCount() > 2) {
                viewCallsForSpinner.setSelection(1);
            }
            pagerAdapter = new LocalPagerAdapter(getChildFragmentManager(), getActivity(), tabLayout.getTabCount());
            viewPager.setAdapter(pagerAdapter);
            viewCallsForSpinner.setOnItemSelectedListener(pagerAdapter);
        } else {
            int userId = PicupApplication.getUserId();
            String tokenId = PicupApplication.getToken();
            ImmutableMap<String, String> userIdParams = ImmutableMap.of("userId", String.valueOf(userId), "applicationId", PicupApplication.apiId);
            final Call<LineNumbers> call = picupService.picupNumbers(tokenId, userIdParams);
            call.enqueue(new Callback<LineNumbers>() {
                @Override
                public void onResponse(Call<LineNumbers> call, Response<LineNumbers> response) {
                    Logger.log("CallsTabContent - onActivityCreated - request picupNumbers - onResponse");
                    if (response.isSuccessful()) {
                        PicupApplication.lineNumbers = response.body();
                        if (PicupApplication.lineNumbers != null) {
                            Logger.log("CallsTabContent - onActivityCreated - request picupNumbers - onResponse - isSuccessful - lineNumbers count:"+PicupApplication.lineNumbers.getNumbers().size());
                            viewCallsForAdapter = new ViewCallsForSpinnerArrayAdapter(getActivity(), R.layout.view_calls_for_spinner_view, R.id.phone_textview);
                            List<String> numbers = PicupApplication.lineNumbers.getNumbers();
                            if (numbers != null) {
                                for (String lineNumber : numbers) {
                                    viewCallsForAdapter.add(PhoneNumberUtils.formatNumber(lineNumber));
                                }
                                //UNITE-1739 - disable spinner if lineNumber is only 1
                                if (numbers.size() == 1) {
                                    viewCallsForSpinner.setEnabled(false);
                                    if (viewCallsForMask != null) {
                                        String line0 = numbers.get(0);
                                        if (!TextUtils.isEmpty(line0)) {
                                            line0 = PhoneNumberUtils.formatNumber(line0);
                                        }
                                        viewCallsForMask.setText(line0);
                                        viewCallsForMask.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    viewCallsForSpinner.setEnabled(true);
                                    if (viewCallsForMask != null) {
                                        viewCallsForMask.setVisibility(View.GONE);
                                    }
                                }
                            }
                            viewCallsForSpinner.setAdapter(viewCallsForAdapter);
                            if (viewCallsForSpinner.getCount() > 2) {
                                viewCallsForSpinner.setSelection(1);
                            }
                        }
                        pagerAdapter = new LocalPagerAdapter(getChildFragmentManager(), getActivity(), tabLayout.getTabCount());
                        viewPager.setAdapter(pagerAdapter);
                        viewCallsForSpinner.setOnItemSelectedListener(pagerAdapter);
                    } else {
                        if (response.errorBody() != null)
                            try {
                                Logger.log("CallsTabContent - onActivityCreated - request picupNumbers - failed - response:"+response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(0);
                        if (errorDialogFragment != null && getFragmentManager() != null)
                            errorDialogFragment.show(getFragmentManager(), "errorDialogFragment");
                    }
                }

                @Override
                public void onFailure(Call<LineNumbers> call, Throwable t) {
                    ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(1);
                    if (errorDialogFragment != null && getFragmentManager() != null)
                        errorDialogFragment.show(getFragmentManager(), "errorDialogFragment");
                }
            });
        }

        //pagerAdapter = new LocalPagerAdapter(getChildFragmentManager(), getActivity(), tabLayout.getTabCount());
       // viewPager.setAdapter(pagerAdapter);
        // tabLayout informing viewPager about tab selection
        viewPagerOnTabSelectedHandler = new ViewPagerOnTabSelectedHandler(viewPager);
        tabLayout.addOnTabSelectedListener(viewPagerOnTabSelectedHandler);

        //viewPager informing tablayout about content page selection (swiping, maybe)
        tabLayoutOnPageChangeHandler = new TabLayoutOnPageChangeHandler();
        viewPager.addOnPageChangeListener(tabLayoutOnPageChangeHandler);

        //viewCallsForSpinner.setOnItemSelectedListener(pagerAdapter);

        //loadCalls();
/*
        if (cdrs == null) {
            cdrs = new TreeSet<>();
            fetch(null);
        }
*/
    }

    public void fetch(String callType) {
        String tokenId = PicupApplication.getToken();
        if (TextUtils.isEmpty(tokenId)) {
            listener.reAuthenticate();
            return;
        }
        int lastId = 0;
        if (!cdrs.isEmpty()) {
            lastId = cdrs.first().getRowId();
        }
        int fetchSize = !TextUtils.isEmpty(callType) ? 10 : PicupApplication.pageSize;
/*
        final ImmutableMap<String, String> callHistoryParams = ImmutableMap.of("applicationId", PicupApplication.apiId, "userId", String.valueOf(PicupApplication.getUserId()),
                                                                               "lastId", String.valueOf(lastId), "maxRecordsBack", String.valueOf(fetchSize));
*/
        //final ImmutableMap<String, String> callHistoryParams = ImmutableMap.of("applicationId", PicupApplication.apiId,
        //                                                                       "maxRecordsBack", String.valueOf(fetchSize));

        int accountId = PicupApplication.getAccountId();
        String userId = String.valueOf(PicupApplication.getUserId());
        ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<String, String>();
        builder.put("maxRecordsBack", String.valueOf(PicupApplication.pageSize))
                .put("applicationId", PicupApplication.apiId);
        if (!PicupApplication.isAdmin())
            builder.put("userId", userId);
        final ImmutableMap<String, String> callHistoryParams = builder.build();

        final Call<CallHistory> call = picupService.callHistory(tokenId, String.valueOf(accountId), callHistoryParams);
        call.enqueue(new Callback<CallHistory>() {
            @Override
            public void onResponse(Call<CallHistory> call, Response<CallHistory> response) {
                Logger.log("CallsTabContent - fetch - request callHistory - onResponse");
                if (response.isSuccessful()) {
                    CallHistory callHistory = response.body();
                    SortedSet<Cdr> latestCdrs = callHistory.getCdrs();
                    cdrs.addAll(latestCdrs);
                    int selectedTabIndex = tabLayout.getSelectedTabPosition();

                    //UNITE -- only company calls for v1
                    //UNITE 1820 - only 2 tabs department and your, using company as department for now
                    switch (selectedTabIndex) {
                        case 0:
                            CallsTabCompanyCallsFragment callsTabCompanyCallsFragment = (CallsTabCompanyCallsFragment) pagerAdapter.getItem(selectedTabIndex);
                            callsTabCompanyCallsFragment.update(latestCdrs);
                            break;
                        //case 1:
                        //    CallsTabYourCallsFragment callsTabYourCallsFragment = (CallsTabYourCallsFragment) pagerAdapter.getItem(selectedTabIndex);
                        //    callsTabYourCallsFragment.update(latestCdrs);
                    }
                    /*
                    //UNITE 1490
                    if (PicupApplication.isAdmin()) {
                        switch (selectedTabIndex) {
                            case 0:
                                CallsTabCompanyCallsFragment callsTabCompanyCallsFragment = (CallsTabCompanyCallsFragment) pagerAdapter.getItem(selectedTabIndex);
                                callsTabCompanyCallsFragment.update(latestCdrs);
                                break;
                            case 1:
                                CallsTabDepartmentCallsFragment callsTabDepartmentCallsFragment = (CallsTabDepartmentCallsFragment) pagerAdapter.getItem(selectedTabIndex);
                                callsTabDepartmentCallsFragment.update(latestCdrs);
                                break;
                            case 2:
                                CallsTabYourCallsFragment callsTabYourCallsFragment = (CallsTabYourCallsFragment) pagerAdapter.getItem(selectedTabIndex);
                                callsTabYourCallsFragment.update(latestCdrs);
                        }
                    } else {
                        switch (selectedTabIndex) {
                            case 0:
                                CallsTabDepartmentCallsFragment callsTabDepartmentCallsFragment = (CallsTabDepartmentCallsFragment) pagerAdapter.getItem(selectedTabIndex);
                                callsTabDepartmentCallsFragment.update(latestCdrs);
                                break;
                            case 1:
                                CallsTabYourCallsFragment callsTabYourCallsFragment = (CallsTabYourCallsFragment) pagerAdapter.getItem(selectedTabIndex);
                                callsTabYourCallsFragment.update(latestCdrs);
                        }
                    }*/
                } else {
                    if (response.errorBody() != null)
                        try {
                            Logger.log("CallsTabContent - fetch - failed - response:"+response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
            @Override
            public void onFailure(Call<CallHistory> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        viewPager.removeOnPageChangeListener(tabLayoutOnPageChangeHandler);
        tabLayout.removeOnTabSelectedListener(viewPagerOnTabSelectedHandler);
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

    private static class LocalDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            viewCallsForSpinner.setVisibility(viewCallsForAdapter.getCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        }

        @Override
        public void onInvalidated() {
            viewCallsForSpinner.setVisibility(View.INVISIBLE);
        }
    }

    public interface OnCallsTabContentListener {
        void reAuthenticate();
    }

    public static class LocalPagerAdapter extends FragmentPagerAdapter implements AdapterView.OnItemSelectedListener {
        private int tabCount = 0;
        private Context context = null;

        private CallsTabCompanyCallsFragment callsTabCompanyCallsFragment = null;
        //private CallsTabDepartmentCallsFragment callsTabDepartmentCallsFragment = null;
        //private CallsTabYourCallsFragment callsTabYourCallsFragment = null;

        public LocalPagerAdapter(FragmentManager fm, Context context, int tabCount) {
            super(fm);
            this.context = context;
            this.tabCount = tabCount;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            String picupNumber = null;

            Logger.log("CallsTabContent - LocalPagerAdapter - getItem");

            //UNITE...only ALl calls for v1

            //UNITE 1820 - only 2 tabs (department and your...using company as department for now)
            switch (position) {
                case 0:
                    if (callsTabCompanyCallsFragment == null) {
                        picupNumber = (String) viewCallsForSpinner.getSelectedItem();
                        if (TextUtils.equals(picupNumber, "All Numbers")) {
                            picupNumber = "";
                        }
                        callsTabCompanyCallsFragment = CallsTabCompanyCallsFragment.newInstance(PhoneNumberUtils.stripSeparators(picupNumber));
                    }
                    fragment = callsTabCompanyCallsFragment;
                    break;
                //case 1:
                //    if (callsTabYourCallsFragment == null) {
                //        picupNumber = (String) viewCallsForSpinner.getSelectedItem();
                //        if (TextUtils.equals(picupNumber, "All Numbers")) {
                //            picupNumber = "";
                //        }
                //        callsTabYourCallsFragment = CallsTabYourCallsFragment.newInstance(PhoneNumberUtils.stripSeparators(picupNumber));
                //    }
                //    fragment = callsTabYourCallsFragment;
                //    break;
                default:
            }
/*
            //UNITE 1490
            if (PicupApplication.isAdmin()) {
                switch (position) {
                    case 0:
                        if (callsTabCompanyCallsFragment == null) {
                            picupNumber = (String) viewCallsForSpinner.getSelectedItem();
                            if (TextUtils.equals(picupNumber, "All Numbers")) {
                                picupNumber = "";
                            }
                            callsTabCompanyCallsFragment = CallsTabCompanyCallsFragment.newInstance(PhoneNumberUtils.stripSeparators(picupNumber));
                        }
                        fragment = callsTabCompanyCallsFragment;
                        break;
                    case 1:
                        if (callsTabDepartmentCallsFragment == null) {
                            picupNumber = (String) viewCallsForSpinner.getSelectedItem();
                            if (TextUtils.equals(picupNumber, "All Numbers")) {
                                picupNumber = "";
                            }
                            callsTabDepartmentCallsFragment = CallsTabDepartmentCallsFragment.newInstance(PhoneNumberUtils.stripSeparators(picupNumber));
                        }
                        fragment = callsTabDepartmentCallsFragment;
                        break;
                    case 2:
                        if (callsTabYourCallsFragment == null) {
                            picupNumber = (String) viewCallsForSpinner.getSelectedItem();
                            if (TextUtils.equals(picupNumber, "All Numbers")) {
                                picupNumber = "";
                            }
                            callsTabYourCallsFragment = CallsTabYourCallsFragment.newInstance(PhoneNumberUtils.stripSeparators(picupNumber));
                        }
                        fragment = callsTabYourCallsFragment;
                        break;
                    default:
                }
            } else {
                switch (position) {
                    case 0:
                        if (callsTabDepartmentCallsFragment == null) {
                            picupNumber = (String) viewCallsForSpinner.getSelectedItem();
                            if (TextUtils.equals(picupNumber, "All Numbers")) {
                                picupNumber = "";
                            }
                            callsTabDepartmentCallsFragment = CallsTabDepartmentCallsFragment.newInstance(PhoneNumberUtils.stripSeparators(picupNumber));
                        }
                        fragment = callsTabDepartmentCallsFragment;
                        break;
                    case 1:
                        if (callsTabYourCallsFragment == null) {
                            picupNumber = (String) viewCallsForSpinner.getSelectedItem();
                            if (TextUtils.equals(picupNumber, "All Numbers")) {
                                picupNumber = "";
                            }
                            callsTabYourCallsFragment = CallsTabYourCallsFragment.newInstance(PhoneNumberUtils.stripSeparators(picupNumber));
                        }
                        fragment = callsTabYourCallsFragment;
                        break;
                    default:
                }
            }
            */

            return fragment;
        }

        @Override
        public int getCount() {
            return tabCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            CharSequence title = "";

            //UNITE...only all calls for v1

            //UNITE 1820 - only 2 tabs: department and your (for now using company as department)
            switch (position) {
                case 0:
                    title = context.getString(R.string.company_calls_label);
                    break;
                //case 1:
                //    title = context.getString(R.string.your_calls_label);
                //    break;
                default:
                    title = super.getPageTitle(position);
            }
            /*
            //UNITE 1490
            if (PicupApplication.isAdmin()) {
                switch (position) {
                    case 0:
                        title = context.getString(R.string.tab_title_company);
                        break;
                    case 1:
                        title = context.getString(R.string.tab_title_department);
                        break;
                    case 2:
                        title = context.getString(R.string.tab_title_your);
                        break;
                    default:
                        title = super.getPageTitle(position);
                }
            } else {
                switch (position) {
                    case 0:
                        title = context.getString(R.string.tab_title_department);
                        break;
                    case 1:
                        title = context.getString(R.string.tab_title_your);
                        break;
                    default:
                        title = super.getPageTitle(position);
                }
            }*/
            return title;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Logger.log("CallsTabContent - LocalPagerAdapter - onItemSelected");
            if (initialized) {
                initialized = false;
                return;
            }
            String selectedPicupNumber = (String)parent.getItemAtPosition(position);
            if (parent == viewCallsForSpinner) {

                //UNITE..only All calls for v1
                //UNITE 1820 - only 2 tabs
                ((CallsTabCompanyCallsFragment) getItem(0)).filter(selectedPicupNumber);
                //((CallsTabYourCallsFragment) getItem(1)).filter(selectedPicupNumber);
                /*
                //UNITE 1490
                if (PicupApplication.isAdmin()) {
                    ((CallsTabCompanyCallsFragment) getItem(0)).filter(selectedPicupNumber);
                    ((CallsTabDepartmentCallsFragment) getItem(1)).filter(selectedPicupNumber);
                    ((CallsTabYourCallsFragment) getItem(2)).filter(selectedPicupNumber);
                } else {
                    ((CallsTabDepartmentCallsFragment) getItem(0)).filter(selectedPicupNumber);
                    ((CallsTabYourCallsFragment) getItem(1)).filter(selectedPicupNumber);
                }*/
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            notifyDataSetChanged();
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
            Logger.log("CallsTabContent - ViewPagerOnTabSelectedHandler - onTabSelected..possibly force a refresh here");
            viewPager.setCurrentItem(tab.getPosition());
/*
            //String selectedPicupNumber = (String)viewCallsForSpinner.getSelectedItem();
            final int selectedIndex = viewPager.getCurrentItem();
            Fragment currentFragment = pagerAdapter.getItem(selectedIndex);
            switch (selectedIndex) {
                case 0:
                    CallsTabCompanyCallsFragment callsTabCompanyCallsFragment = (CallsTabCompanyCallsFragment)currentFragment;
                    if (!TextUtils.equals(callsTabCompanyCallsFragment.getPicupNumber(), selectedPicupNumber)) {
                        callsTabCompanyCallsFragment.setPicupNumber(selectedPicupNumber);
                        callsTabCompanyCallsFragment.loadData();
                    }
                    break;
                case 1:
                    CallsTabDepartmentCallsFragment callsTabDepartmentCallsFragment = (CallsTabDepartmentCallsFragment)currentFragment;
                    if (!TextUtils.equals(callsTabDepartmentCallsFragment.getPicupNumber(), selectedPicupNumber)) {
                        callsTabDepartmentCallsFragment.setPicupNumber(selectedPicupNumber);
                        callsTabDepartmentCallsFragment.loadData();
                    }
                    break;
                case 2:
                    CallsTabYourCallsFragment callsTabYourCallsFragment = (CallsTabYourCallsFragment)currentFragment;
                    if (!TextUtils.equals(callsTabYourCallsFragment.getPicupNumber(), selectedPicupNumber)) {
                        callsTabYourCallsFragment.setPicupNumber(selectedPicupNumber);
                        callsTabYourCallsFragment.loadData();
                    }
                    break;
                default:
             }
*/
       }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
        }
    }

    /**
     * TabLayout to respond to page change event on ViewPager
     */
    private static class TabLayoutOnPageChangeHandler extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            Logger.log("CallsTabContent - TabLayoutOnPageChangeHandler - onPageSelected");
            TabLayout.Tab selectedTab = tabLayout.getTabAt(position);
            selectedTab.select();
        }
    }
}

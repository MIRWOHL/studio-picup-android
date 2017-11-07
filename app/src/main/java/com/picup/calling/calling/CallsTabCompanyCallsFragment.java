package com.picup.calling;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import com.picup.calling.adapter.SectionArrayAdapter;
import com.picup.calling.base.PicupApplication;
import com.picup.calling.dialog.ErrorDialogFragment;
import com.picup.calling.network.CallHistory;
import com.picup.calling.network.Cdr;
import com.picup.calling.network.PicupService;
import com.picup.calling.util.Logger;
import com.picup.calling.util.PicupImageUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.collect.ImmutableMap;
import com.picup.calling.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallsTabCompanyCallsFragment extends Fragment implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
    private static final String TAG = CallsTabCompanyCallsFragment.class.getSimpleName();
    private static final int PERMISSION_REQUEST_READ_CONTACTS = 3;

    private static ListView callListView = null;
    private static SwipeRefreshLayout swipeRefreshLayout = null;
    private SwipeRefreshLayout.OnRefreshListener swipeRefreshListener;

    private SectionArrayAdapter adapter = null;

    private static final String ARG_PICUP_NUMBER = "android.idt.net.com.picup.calling.CallsTabCompanyCallsFragment.PICUP_NUMBER";

    private String picupNumber;
    private static boolean permissionChecked = false;

    private static OnCallsTabCompanyCallsFragmentListener listener;

    private static SortedSet<Cdr> cdrs = new TreeSet<>();

    private static int prevLastVisibleItem = 0;

    private NavigableMap<Calendar, List<Cdr>> sections = new TreeMap<Calendar, List<Cdr>>(new Comparator<Calendar>() {
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

    private static PicupService picupService = PicupService.retrofit.create(PicupService.class);

    public CallsTabCompanyCallsFragment() {
        // Required empty public constructor
    }

    public static CallsTabCompanyCallsFragment newInstance(String picupNumber) {
        CallsTabCompanyCallsFragment fragment = new CallsTabCompanyCallsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PICUP_NUMBER, picupNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Logger.log("CallsTabCompanyCallsFragment - onAttach");
        if (context instanceof OnCallsTabCompanyCallsFragmentListener) {
            listener = (OnCallsTabCompanyCallsFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnCallsTabCompanyCallsFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.log("CallsTabCompanyCallsFragment - onCreate");
        if (getArguments() != null) {
            picupNumber = getArguments().getString(ARG_PICUP_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.log("CallsTabCompanyCallsFragment - onCreateView");
        View callsTabCompanyCallsView = inflater.inflate(R.layout.calls_tab_company_calls, container, false);
        callListView = (ListView)callsTabCompanyCallsView.findViewById(R.id.company_calls_listview);
        callListView.setOnItemClickListener(this);

        swipeRefreshLayout = (SwipeRefreshLayout)callsTabCompanyCallsView.findViewById(R.id.swiperefresh);

        View emptyView = inflater.inflate(R.layout.call_empty_view, null);
        ((ViewGroup)callListView.getParent()).addView(emptyView);
        callListView.setEmptyView(emptyView);

        swipeRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Logger.log("CallsTabCompanyCallsFragment - swipeRefreshListener - onRefresh");

                // This method performs the actual data-refresh operation.
                // The method calls setRefreshing(false) when it's finished.
                loadData();
            }
        };

        swipeRefreshLayout.setOnRefreshListener(swipeRefreshListener);

        return callsTabCompanyCallsView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Logger.log("CallsTabCompanyCallsFragment - onHiddenChanged - hidden:"+hidden);
        super.onHiddenChanged(hidden);
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        Logger.log("CallsTabCompanyCallsFragment - setMenuVisibility - visible:"+visible);
        super.setMenuVisibility(visible);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.log("CallsTabCompanyCallsFragment - onActivityCreated");
        loadData();
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        Logger.log("CallsTabCompanyCallsFragment - setUserVisibleHint - visible:"+visible);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

        @Override
    public void onResume() {
        super.onResume();
        Logger.log("CallsTabCompanyCallsFragment - onResume - getUserVisibleHint:"+getUserVisibleHint());
    }

    public void doRefresh() {
        Logger.log("CallsTabCompanyCallsFragment - doRefresh");
        swipeRefreshLayout.post(new Runnable() {
            @Override public void run() {
                swipeRefreshLayout.setRefreshing(true);
                // directly call onRefresh() method as there is a bug in the library that won't call the method
                swipeRefreshListener.onRefresh();
            }
        });
    }

    public void setPicupNumber(String picupNumber) {
        this.picupNumber = picupNumber;
    }

    public String getPicupNumber() {
        return picupNumber;
    }

    public void update(SortedSet<Cdr> newCdrs) {
        Logger.log("CallsTabCompanyCallsFragment - update - count newCdrs:"+newCdrs.size());
        int currentSize = callListView.getCount();

        if (newCdrs != null && !newCdrs.isEmpty()) {
            Calendar lastCallCalendar = newCdrs.first().getCallCalendar();
            Calendar firstCallCalendar = newCdrs.last().getCallCalendar();
            if (sections.isEmpty()) {
                lastCallCalendar.add(Calendar.DAY_OF_MONTH, 1);
                firstCallCalendar.add(Calendar.DAY_OF_MONTH, -1);
            }
            Calendar currentCallCalendar = lastCallCalendar;
            do {
                Calendar midnightCalendar = Calendar.getInstance();
                midnightCalendar.setTime(currentCallCalendar.getTime());
                midnightCalendar.set(Calendar.HOUR_OF_DAY, 0);
                midnightCalendar.set(Calendar.MINUTE, 0);
                midnightCalendar.set(Calendar.SECOND, 0);
                midnightCalendar.set(Calendar.MILLISECOND, 1);
                if (!sections.containsKey(midnightCalendar)) {
                    sections.put(midnightCalendar, new ArrayList<Cdr>());
                }
                currentCallCalendar.add(Calendar.DAY_OF_MONTH, -1);
            } while (currentCallCalendar.after(firstCallCalendar));
            for (Cdr cdr : newCdrs) {
                Map.Entry<Calendar, List<Cdr>> entry = sections.floorEntry(cdr.getCallCalendar());
                entry.getValue().add(cdr);
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
                callListView.smoothScrollToPosition(newCdrs.size() < 7 ? adapter.getCount() : currentSize + 7);
            } else {
                adapter = new SectionArrayAdapter(getActivity());
                Iterator<Map.Entry<Calendar, List<Cdr>>> entries = sections.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry entry = entries.next();
                    Calendar key = (Calendar)entry.getKey();
                    List<Cdr> value = (List<Cdr>)entry.getValue();
                    if (value.isEmpty()) {
                        continue;
                    }
                    ArrayAdapter sectionAdapter = new CallArrayAdapter(getActivity(), R.layout.call_row_item, R.id.duration_textview, value);
                    adapter.addSection(key, sectionAdapter);
                }
                callListView.setAdapter(adapter);
            }
        }
        callListView.setOnScrollListener(this);
    }

    public void loadData() {
        Logger.log("CallsTabCompanyCallsFragment - loadData");
        String tokenId = PicupApplication.getToken();
        if (TextUtils.isEmpty(tokenId)) {
            swipeRefreshLayout.setRefreshing(false);
            listener.reAuthenticate();
            return;
        }
        cdrs.clear();
        sections.clear();
        //ImmutableMap<String, String> callHistoryParams = ImmutableMap.of("maxRecordsBack", String.valueOf(PicupApplication.pageSize), "applicationId", PicupApplication.apiId);
        //ImmutableMap<String, String> callHistoryParams = ImmutableMap.of("applicationId", PicupApplication.apiId, "userId", String.valueOf(PicupApplication.getUserId()));
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
                String log = "CallsTabCompanyCallsFragment - loadData - request callHistory - onResponse";
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful()) {
                    log += " - isSuccessful";
                    Logger.log(log);

                    CallHistory callHistory = response.body();

                    if (callHistory != null) {
                        Logger.log("CallsTabCompanyCallsFragment - loadData - callHistory - count:"+callHistory.getActualCDRCount());
                        cdrs = callHistory.getCdrs();
                        for (Cdr cdr : cdrs) {
                            Logger.log("CallsTabCompanyCallsFragment - loadData - cdr - ani:"+cdr.getAni()+" dnis:"+cdr.getDnis() + " dn:" + cdr.getDn() + " origDn:" + cdr.getOrigdn() + " callstate:" + cdr.getCallState() + " deptName:" + cdr.getDeptName());
                        }

                        SortedSet<Cdr> filteredCdrs = new TreeSet<Cdr>();
                        Logger.log("CallsTabCompanyCallsFragment - loadData - picupNumber="+picupNumber);
                        //UNITE 1820...do not filter by com.picup.calling number, presume all
                        filteredCdrs = cdrs;
                        /*
                        if (TextUtils.equals(picupNumber, "All Numbers")) {
                            filteredCdrs = cdrs;
                        } else {
                            Logger.log("CallsTabCompanyCallsFragment - filter picupNumber:"+picupNumber);
                            for (Cdr cdr : cdrs) {
                                //UNITE 1490
                                if (TextUtils.equals(cdr.getAni(), picupNumber) ||
                                        TextUtils.equals(cdr.getDnis(), picupNumber)) {
                                    filteredCdrs.add(cdr);
                                }
                            }
                        }*/
                        if (filteredCdrs.isEmpty()) {
                            Logger.log("CallsTabCompanyCallsFragment - loadData - filteredCdrs is Empty");
                            return;
                        }
                        SortedSet<Calendar> midnightCalendarKeys = new TreeSet<Calendar>(new Comparator<Calendar>() {
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
                        Calendar lastCallCalendar = Calendar.getInstance();
                        lastCallCalendar.setTime(filteredCdrs.first().getCallCalendar().getTime());
                        lastCallCalendar.add(Calendar.DAY_OF_MONTH, 1);
                        Calendar firstCallCalendar = Calendar.getInstance();
                        firstCallCalendar.setTime(filteredCdrs.last().getCallCalendar().getTime());
                        firstCallCalendar.add(Calendar.DAY_OF_MONTH, -1);
                        Calendar currentCallCalendar = lastCallCalendar;
                        do {
                            Calendar midnightCalendar = Calendar.getInstance();
                            midnightCalendar.setTime(currentCallCalendar.getTime());
                            midnightCalendar.set(Calendar.HOUR_OF_DAY, 0);
                            midnightCalendar.set(Calendar.MINUTE, 0);
                            midnightCalendar.set(Calendar.SECOND, 0);
                            midnightCalendar.set(Calendar.MILLISECOND, 1);
                            midnightCalendarKeys.add(midnightCalendar);
                            currentCallCalendar.add(Calendar.DAY_OF_MONTH, -1);
                        } while (currentCallCalendar.after(firstCallCalendar));
                        for (Calendar midnightCalendarKey : midnightCalendarKeys) {
                            sections.put(midnightCalendarKey, new ArrayList<Cdr>());
                        }
                        for (Cdr filteredCdr : filteredCdrs) {
                            Map.Entry<Calendar, List<Cdr>> entry = sections.ceilingEntry(filteredCdr.getCallCalendar());
                            entry.getValue().add(filteredCdr);
                        }
                        Iterator<Map.Entry<Calendar, List<Cdr>>> entries = sections.entrySet().iterator();
                        if (adapter == null) {
                            adapter = new SectionArrayAdapter(getActivity());
                        } else {
                            adapter.clearSections();
                        }
                        while (entries.hasNext()) {
                            Map.Entry entry = entries.next();
                            Calendar key = (Calendar) entry.getKey();
                            List<Cdr> value = (List<Cdr>) entry.getValue();
                            if (value.isEmpty()) {
                                continue;
                            }
                            ArrayAdapter sectionAdapter = new CallArrayAdapter(getActivity(), R.layout.call_row_item, R.id.duration_textview, value);
                            adapter.addSection(key, sectionAdapter);
                        }
                        callListView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        //callListView.setOnScrollListener(CallsTabCompanyCallsFragment.this);
                    } //else {
                       // ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(0);
                       // errorDialogFragment.show(getFragmentManager(), "errorDialogFragment");
                    //}
                } else {
                    if (response.errorBody() != null)
                        try {
                            Logger.log("CallsTabCompanyCallsFragment - loadData - failed - response:"+response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
            @Override
            public void onFailure(Call<CallHistory> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(1);
                errorDialogFragment.show(getFragmentManager(), "errorDialogFragment");
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Logger.log("CallsTabCompanyCallsFragment - onDetach");
        listener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Logger.log("CallsTabCompanyCallsFragment - onItemClick");
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            int lastVisibleItem = firstVisibleItem + visibleItemCount;
            if (lastVisibleItem == totalItemCount) {
                if (prevLastVisibleItem != lastVisibleItem) {
                    callListView.setOnScrollListener(null);
                    //fetchMore();
                }
            }
    }

    public void filter(String picupNumber) {
        Logger.log("CallsTabCompanyCallsFragment - filter");
        this.picupNumber = picupNumber;
        sections.clear();
        SortedSet<Cdr> filteredCdrs = new TreeSet<Cdr>();
        if (TextUtils.equals(this.picupNumber, "All Numbers")) {
            filteredCdrs = cdrs;
        } else {
            this.picupNumber = PhoneNumberUtils.stripSeparators(this.picupNumber);
            for (Cdr cdr : cdrs) {
                //UNITE 1490
                //TODO..may need to convert format to really compare...
                if (TextUtils.equals(cdr.getAni(), picupNumber) ||
                        TextUtils.equals(cdr.getDnis(), picupNumber)) {
                    filteredCdrs.add(cdr);
                }
            }
        }
        if (!filteredCdrs.isEmpty()) {
            SortedSet<Calendar> midnightCalendarKeys = new TreeSet<Calendar>(new Comparator<Calendar>() {
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
            Calendar lastCallCalendar = Calendar.getInstance();
            lastCallCalendar.setTime(filteredCdrs.first().getCallCalendar().getTime());
            lastCallCalendar.add(Calendar.DAY_OF_MONTH, 1);
            Calendar firstCallCalendar = Calendar.getInstance();
            firstCallCalendar.setTime(filteredCdrs.last().getCallCalendar().getTime());
            firstCallCalendar.add(Calendar.DAY_OF_MONTH, -1);
            Calendar currentCallCalendar = lastCallCalendar;
            do {
                Calendar midnightCalendar = Calendar.getInstance();
                midnightCalendar.setTime(currentCallCalendar.getTime());
                midnightCalendar.set(Calendar.HOUR_OF_DAY, 0);
                midnightCalendar.set(Calendar.MINUTE, 0);
                midnightCalendar.set(Calendar.SECOND, 0);
                midnightCalendar.set(Calendar.MILLISECOND, 1);
                midnightCalendarKeys.add(midnightCalendar);
                currentCallCalendar.add(Calendar.DAY_OF_MONTH, -1);
            } while (currentCallCalendar.after(firstCallCalendar));
            for (Calendar midnightCalendarKey : midnightCalendarKeys) {
                sections.put(midnightCalendarKey, new ArrayList<Cdr>());
            }
            for (Cdr filteredCdr : filteredCdrs) {
                Map.Entry<Calendar, List<Cdr>> entry = sections.ceilingEntry(filteredCdr.getCallCalendar());
                entry.getValue().add(filteredCdr);
            }
            Iterator<Map.Entry<Calendar, List<Cdr>>> entries = sections.entrySet().iterator();
            if (adapter == null) {
                adapter = new SectionArrayAdapter(getActivity());
            } else {
                adapter.clearSections();
            }
            while (entries.hasNext()) {
                Map.Entry entry = entries.next();
                Calendar key = (Calendar)entry.getKey();
                List<Cdr> value = (List<Cdr>)entry.getValue();
                if (value.isEmpty()) {
                    continue;
                }
                ArrayAdapter sectionAdapter = new CallArrayAdapter(getActivity(), R.layout.call_row_item, R.id.duration_textview, value);
                adapter.addSection(key, sectionAdapter);
            }
            callListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            //callListView.setOnScrollListener(CallsTabCompanyCallsFragment.this);
        }
    }

    public void fetchMore() {
        String tokenId = PicupApplication.getToken();
        if (TextUtils.isEmpty(tokenId)) {
            listener.reAuthenticate();
            return;
        }
        int lastId = 0;
        if (!cdrs.isEmpty()) {
            lastId = cdrs.first().getRowId();
        }
        int fetchSize = PicupApplication.pageSize;
        int accountId = PicupApplication.getAccountId();
        String userId = String.valueOf(PicupApplication.getUserId());
        ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<String, String>();
        builder.put("maxRecordsBack", String.valueOf(fetchSize))
                .put("applicationId", PicupApplication.apiId)
                .put("lastId", String.valueOf(lastId));
        if (!PicupApplication.isAdmin())
            builder.put("userId", userId);
        final ImmutableMap<String, String> callHistoryParams = builder.build();

        //ImmutableMap.of("maxRecordsBack", String.valueOf(fetchSize), "applicationId", PicupApplication.apiId, "userId", userId);
        //final ImmutableMap<String, String> callHistoryParams = ImmutableMap.of("lastId", String.valueOf(lastId),  "maxRecordsBack", String.valueOf(fetchSize), "applicationId", PicupApplication.apiId);

        final Call<CallHistory> call = picupService.callHistory(tokenId, String.valueOf(accountId), callHistoryParams);

        call.enqueue(new Callback<CallHistory>() {
            @Override
            public void onResponse(Call<CallHistory> call, Response<CallHistory> response) {
                Logger.log("CallsTabCompanyCallsFragment - fetchMore - request callHistory - onResponse");
                if (response.isSuccessful()) {
                    CallHistory callHistory = response.body();
                    SortedSet<Cdr> latestCdrs = callHistory.getCdrs();
                    if (!latestCdrs.isEmpty()) {
                        int currentSize = callListView.getCount();
                        Calendar lastCallCalendar = latestCdrs.first().getCallCalendar();
                        Calendar firstCallCalendar = latestCdrs.last().getCallCalendar();
                        if (sections.isEmpty()) {
                            lastCallCalendar.add(Calendar.DAY_OF_MONTH, 1);
                            firstCallCalendar.add(Calendar.DAY_OF_MONTH, -1);
                        }
                        Calendar currentCallCalendar = lastCallCalendar;
                        do {
                            Calendar midnightCalendar = Calendar.getInstance();
                            midnightCalendar.setTime(currentCallCalendar.getTime());
                            midnightCalendar.set(Calendar.HOUR_OF_DAY, 0);
                            midnightCalendar.set(Calendar.MINUTE, 0);
                            midnightCalendar.set(Calendar.SECOND, 0);
                            midnightCalendar.set(Calendar.MILLISECOND, 1);
                            if (!sections.containsKey(midnightCalendar)) {
                                sections.put(midnightCalendar, new ArrayList<Cdr>());
                            }
                            currentCallCalendar.add(Calendar.DAY_OF_MONTH, -1);
                        } while (currentCallCalendar.after(firstCallCalendar));
                        for (Cdr latestCdr : latestCdrs) {
                            Map.Entry<Calendar, List<Cdr>> entry = sections.floorEntry(latestCdr.getCallCalendar());
                            entry.getValue().add(latestCdr);
                        }
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                            callListView.smoothScrollToPosition(latestCdrs.size() < 7 ? adapter.getCount() : currentSize + 7);
                        } else {
                            adapter = new SectionArrayAdapter(getActivity());
                            Iterator<Map.Entry<Calendar, List<Cdr>>> entries = sections.entrySet().iterator();
                            while (entries.hasNext()) {
                                Map.Entry entry = entries.next();
                                Calendar key = (Calendar) entry.getKey();
                                List<Cdr> value = (List<Cdr>) entry.getValue();
                                if (value.isEmpty()) {
                                    continue;
                                }
                                ArrayAdapter sectionAdapter = new CallArrayAdapter(getActivity(), R.layout.call_row_item, R.id.duration_textview, value);
                                adapter.addSection(key, sectionAdapter);
                            }
                            callListView.setAdapter(adapter);
                        }
                        prevLastVisibleItem = callListView.getLastVisiblePosition();
                        callListView.setOnScrollListener(CallsTabCompanyCallsFragment.this);
                        cdrs.addAll(latestCdrs);
                    }
                } else {
                    if (response.errorBody() != null)
                        try {
                            Logger.log("CallsTabCompanyCallsFragment - fetchMore - failed - response:"+response.errorBody().string());
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

    public interface OnCallsTabCompanyCallsFragmentListener {
        void reAuthenticate();
        void showContactDetail(int contactId);
        void showContactProfile(Object source, int contactId);
        void showContactProfileForNonContact(Object source, String phoneNumber);
        //void showAddContactOption(float x, float y, String phoneNumber);
        void showAddContactOption(View sourceView, String phoneNumber);
        void explainPermission(String permissionName);
        void call(String callToNumber);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_CONTACTS:
                permissionChecked = true;
                if (grantResults.length > 0 && grantResults.length > 0) { // permission request not interrupted
                    if (TextUtils.equals(permissions[0], Manifest.permission.READ_CONTACTS) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        loadData();
                    }
                }
            default:
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public class CallArrayAdapter extends ArrayAdapter<Cdr> {
        private final String TAG = CallArrayAdapter.class.getSimpleName();
        private final int TYPE_SECTION_ITEM = 1;
        private LayoutInflater inflater = null;
        private SimpleDateFormat timeSdf = new SimpleDateFormat("hh:mm");
        private SimpleDateFormat markerDdf = new SimpleDateFormat("a");

        private static final int PROFILE_LOADER_ID = 0;
        private final String[] PROFILE_PROJECTION = {ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY};
        private static final int PROFILE_ID_INDEX = 0;
        private static final int PROFILE_LOOKUP_KEY_INDEX = 1;
        private static final int PROFILE_THUMBNAIL_URI_INDEX = 2;
        private static final int PROFILE_DISPLAY_NAME_PRIMARY_INDEX = 3;

        private static final String SEARCH_CRITERIA = "android.idt.net.com.picup.calling.CallArrayAdapter.SEARCH_CRITERIA";

        public CallArrayAdapter(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
            inflater = LayoutInflater.from(context);
        }

        public CallArrayAdapter(Context context, int resource, int textViewResourceId, List<Cdr> cdrs) {
            super(context, resource, textViewResourceId, cdrs);
            inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            View rowView = convertView;

            if (rowView == null) {
                rowView = inflater.inflate(R.layout.call_row_item, parent, false);
                final ViewHolder viewHolder = new ViewHolder();
                viewHolder.thumbnailImageView = (ImageView)rowView.findViewById(R.id.thumbnail_imageview);
                viewHolder.initialTextView = (TextView)rowView.findViewById(R.id.initial_textview);
                viewHolder.callDirectionImageView = (ImageView)rowView.findViewById(R.id.call_direction_imageview);
                viewHolder.callResultImageView = (ImageView)rowView.findViewById(R.id.call_result_imageview);
                viewHolder.callerInfoLayout = (LinearLayout)rowView.findViewById(R.id.call_info_layout);
                viewHolder.callerTextView = (TextView)rowView.findViewById(R.id.caller_textview);
                viewHolder.departmentTextView = (TextView)rowView.findViewById(R.id.department_textview);
                viewHolder.timeTextView = (TextView)rowView.findViewById(R.id.time_textview);
                viewHolder.markerTextView = (TextView)rowView.findViewById(R.id.marker_textview);
                viewHolder.infoIconView = (ImageView) rowView.findViewById(R.id.info_button);
                rowView.setTag(viewHolder);
            }
            final ViewHolder holder = (ViewHolder)rowView.getTag();
            Cdr cdr = super.getItem(position);
            Cursor cursor = null;
            String thumbnailUriString = null;
            String fullName = null;

            int direction = cdr.getDirection();
            /*if (PicupApplication.lineNumbers != null) {
                direction = cdr.getDirection(PicupApplication.lineNumbers);
            }*/

            //ani ONLY applies on inbound calls...for outbound need dn
            String numToCheck;
            if (direction == Cdr.CDR_INBOUND) {
                numToCheck = cdr.getAni();
                holder.callerTextView.setTag(cdr.getAni());
                holder.callerInfoLayout.setTag(cdr.getAni());
                holder.callerTextView.setText(PhoneNumberUtils.formatNumber(cdr.getAni()));
            } else {
                numToCheck = cdr.getDn(); //unclear if it is maybe dnis but from latest cdrs looks like should be dn
                holder.callerTextView.setTag(cdr.getDn());
                holder.callerInfoLayout.setTag(cdr.getDn());
                holder.callerTextView.setText(PhoneNumberUtils.formatNumber(cdr.getDn()));
            }

            Logger.log("CallsTabCompanyCallsFragment - getView - numToCheck:"+numToCheck);
           if (!TextUtils.isEmpty(numToCheck)) {
               holder.thumbnailImageView.setImageURI(null);
               holder.initialTextView.setText("");
               Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(numToCheck));
               if (!permissionChecked) {
                   if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                       requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_READ_CONTACTS);
                       permissionChecked = true;
                   }
               }

               try {
                   cursor = getContext().getContentResolver().query(uri, PROFILE_PROJECTION, null, null, null);
               } catch (Exception e) {
                   Logger.log("CallsTabCompanyCallsFragment - getView - exception:"+e.getMessage());
               }
               if (cursor != null) {
                   if (cursor.moveToFirst()) {
                       thumbnailUriString = cursor.getString(PROFILE_THUMBNAIL_URI_INDEX);
                       fullName = cursor.getString(PROFILE_DISPLAY_NAME_PRIMARY_INDEX);
                   }
               }
           }
               Logger.log("CallsTabCompanyCallsFragment - getView - thumbnailUriString:"+thumbnailUriString+" fullName:"+fullName);
               if (!TextUtils.isEmpty(thumbnailUriString)) {
                   holder.thumbnailImageView.setImageURI(Uri.parse(thumbnailUriString));
                   Drawable thumbnailDrawable = holder.thumbnailImageView.getDrawable();
                   RoundedBitmapDrawable roundedBitmapDrawable = PicupImageUtils.toRoundedBitmapDrawable(getContext(), thumbnailDrawable);
                   if (roundedBitmapDrawable != null) {
                       holder.thumbnailImageView.setImageDrawable(roundedBitmapDrawable);
                   }
                   holder.thumbnailImageView.setPadding(0,0,0,0);
                   holder.thumbnailImageView.setVisibility(View.VISIBLE);
                   final Cursor cursorRef = cursor;
                   holder.thumbnailImageView.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           Logger.log("CallsTabCompanyCallsFragment - thumbnail - onClick");
                           listener.showContactDetail(cursorRef.getInt(PROFILE_ID_INDEX));
                       }
                   });
                   holder.initialTextView.setVisibility(View.INVISIBLE);
                   holder.infoIconView.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(final View v) {
                           Logger.log("CallsTabCompanyCallsFragment - infoIcon - onClick");
                           listener.showContactProfile(v, cursorRef.getInt(PROFILE_ID_INDEX));
                       }
                   });
               } else if (!TextUtils.isEmpty(fullName)) {
                   String[] chunks = fullName.split(" ");
                   if (chunks.length >= 1) {
                       if (chunks[0].length() > 0) {
                           holder.initialTextView.append(chunks[0].subSequence(0, 1));
                       }
                   }
                   if (chunks.length >= 2) {
                       if (chunks[1].length() > 0) {
                           holder.initialTextView.append(chunks[1].subSequence(0, 1));
                       }
                   }
                   final Cursor cursorRef = cursor;
                   holder.initialTextView.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           Logger.log("CallsTabCompanyCallsFragment - thumbnail - onClick");
                           listener.showContactDetail(cursorRef.getInt(PROFILE_ID_INDEX));
                       }
                   });
                   holder.initialTextView.setVisibility(View.VISIBLE);
                   holder.thumbnailImageView.setVisibility(View.INVISIBLE);

                   holder.infoIconView.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(final View v) {
                           Logger.log("CallsTabCompanyCallsFragment - infoIcon - onClick");
                           listener.showContactProfile(v, cursorRef.getInt(PROFILE_ID_INDEX));
                       }
                   });
               } else {
                   Logger.log("CallsTabCompanyCallsFragment - getView - missing contact");
                   holder.thumbnailImageView.setImageResource(R.drawable.ic_add_black_24dp);
                   //holder.thumbnailImageView.setImageResource(R.drawable.ic_plus_black);
                   holder.thumbnailImageView.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(final View v) {
                           Logger.log("CallsTabCompanyCallsFragment - thumbnail - onClick");
                           String phoneNumber = holder.callerTextView.getText().toString();
                           //AddContactOptionDialogFragment addContactOptionDialogFragment = AddContactOptionDialogFragment.newInstance(event.getX(), event.getY(), phoneNumber);
                           //addContactOptionDialogFragment.show(getActivity().getSupportFragmentManager(), "AddContactOptionDialogFragment");
                           //listener.showAddContactOption(event.getX(), event.getY(), phoneNumber);
                           listener.showAddContactOption(v, phoneNumber);
                       }
                   });
                   //holder.thumbnailImageView.setLayoutParams(new RelativeLayout.LayoutParams(2,2));
                   holder.thumbnailImageView.setPadding(30,30,30,30);
                   holder.thumbnailImageView.setVisibility(View.VISIBLE);
                   holder.initialTextView.setVisibility(View.INVISIBLE);
                   //holder.infoIconView.setVisibility(View.INVISIBLE);  //no profile detail if no contact
                   holder.infoIconView.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(final View v) {
                           Logger.log("CallsTabCompanyCallsFragment - infoIcon - onClick");
                           String phoneNumber = holder.callerTextView.getText().toString();
                           listener.showContactProfileForNonContact(v, phoneNumber);
                       }
                   });
               }


               if (!TextUtils.isEmpty(fullName)) {
                   holder.callerTextView.setText(fullName);
               }
               //final Cursor cursorRef = cursor;
               /*holder.callerTextView.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           //listener.showContactProfile(this, cursorRef.getInt(PROFILE_ID_INDEX));
                           listener.call((String)v.getTag());
                       }
               });*/
               holder.callerInfoLayout.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       //listener.showContactProfile(this, cursorRef.getInt(PROFILE_ID_INDEX));
                       Logger.log("CallsTabCompanyCallsFragment - onClick - call - tag:"+(String)v.getTag());
                       listener.call((String)v.getTag());
                   }
               });

            holder.callResultImageView.setImageBitmap(null);
            holder.callerTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.textColor));

            if (direction == Cdr.CDR_OUTBOUND) {
                holder.callDirectionImageView.setImageResource(R.drawable.ic_outbound_call);
                if (TextUtils.equals("Not Answered", cdr.getCallState())) {//duration has value so not good enoughcdr.getDuration() == 0) {
                    holder.callResultImageView.setImageResource(R.drawable.ic_outbound_cancelled);
                }
            } else { //inbound show state in direction
                if (TextUtils.equals("Not Answered", cdr.getCallState()) && !TextUtils.equals("Voicemail", cdr.getCallState())) {
                    holder.callerTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.errorRed));
                }
                if (TextUtils.equals("Call Answered", cdr.getCallState())) {
                    holder.callDirectionImageView.setImageResource(R.drawable.ic_inbound_call);
                } else if (TextUtils.equals("Not Answered", cdr.getCallState())) {
                    holder.callDirectionImageView.setImageResource(R.drawable.ic_missed_call);
                } else if (TextUtils.equals("Voicemail", cdr.getCallState())) {
                    holder.callDirectionImageView.setImageResource(R.drawable.ic_voice_mail);
                }
            }


            holder.departmentTextView.setText("");
            boolean hasUser = false;
            if (!TextUtils.isEmpty(cdr.getUserName())) {
                hasUser = true;
                if (direction == Cdr.CDR_OUTBOUND) {
                    holder.departmentTextView.setText(getString(R.string.label_caller)+cdr.getUserName());
                } else {
                    holder.departmentTextView.setText(cdr.getUserName());
                }
            }
            if (!TextUtils.isEmpty(cdr.getDeptName()) && hasUser) {
                holder.departmentTextView.append(" (" + cdr.getDeptName() + ")");
            } else if (!TextUtils.isEmpty(cdr.getDeptName())) {
                holder.departmentTextView.setText(cdr.getDeptName());
            }

            if (TextUtils.equals("Call Not Allowed", cdr.getCallState())) {
                //for inbound or outbound not allowed
                holder.callResultImageView.setImageResource(R.drawable.ic_inbound_not_allowed);

                //change text as follows
                if (direction == Cdr.CDR_OUTBOUND)
                    holder.departmentTextView.setText("Outbound, Not allowed");
                else
                    holder.departmentTextView.setText("Inbound, Not allowed");

                //change all the fonts to gray
                holder.departmentTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.lightGray));
                holder.callerTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.lightGray));
                holder.timeTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.lightGray));
                holder.markerTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.lightGray));

            }

            holder.timeTextView.setText(timeSdf.format(cdr.getCallCalendar().getTime()));
            holder.markerTextView.setText(markerDdf.format(cdr.getCallCalendar().getTime()).toLowerCase());

            return rowView;
        }

        public class ViewHolder {
            public ImageView thumbnailImageView;
            public TextView initialTextView;
            public ImageView callDirectionImageView;
            public ImageView callResultImageView;
            public TextView callerTextView;
            public LinearLayout callerInfoLayout;
            public TextView departmentTextView;
            public TextView timeTextView;
            public TextView markerTextView;
            public ImageView infoIconView;
        }
    }
}

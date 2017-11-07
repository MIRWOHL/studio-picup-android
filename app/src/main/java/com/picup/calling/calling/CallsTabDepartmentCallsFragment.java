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

@Deprecated
public class CallsTabDepartmentCallsFragment extends Fragment implements AbsListView.OnScrollListener {
    private static final String TAG = CallsTabDepartmentCallsFragment.class.getSimpleName();
    private static final int PERMISSION_REQUEST_READ_CONTACTS = 3;

    private ListView callListView = null;
    private static SwipeRefreshLayout swipeRefreshLayout = null;
    private SectionArrayAdapter adapter = null;

    private boolean needsRefresh = false;

    private static final String ARG_PICUP_NUMBER = "android.idt.net.com.picup.calling.CallsTabDepartCallsFragment.PICUP_NUMBER";

    // TODO: Rename and change types of parameters
    private String picupNumber;
    private static boolean permissionChecked = false;

    private OnCallsTabDepartmentCallsFragmentListener listener;

    private static SortedSet<Cdr> cdrs = new TreeSet<>();

    private static PicupService picupService = PicupService.retrofit.create(PicupService.class);
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

    public CallsTabDepartmentCallsFragment() {
        // Required empty public constructor
    }

    public static CallsTabDepartmentCallsFragment newInstance(String picupNumber) {
        CallsTabDepartmentCallsFragment fragment = new CallsTabDepartmentCallsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PICUP_NUMBER, picupNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCallsTabDepartmentCallsFragmentListener) {
            listener = (OnCallsTabDepartmentCallsFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnCallsTabCompanyCallsFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            picupNumber = getArguments().getString(ARG_PICUP_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View callsTabDepartmentCallsView = inflater.inflate(R.layout.calls_tab_department_calls, container, false);
        callListView = (ListView) callsTabDepartmentCallsView.findViewById(R.id.department_calls_listview);
        swipeRefreshLayout = (SwipeRefreshLayout)callsTabDepartmentCallsView.findViewById(R.id.swiperefresh);

        View emptyView = inflater.inflate(R.layout.call_empty_view, null);
        ((ViewGroup)callListView.getParent()).addView(emptyView);
        callListView.setEmptyView(emptyView);

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Logger.log("CallsTabDepartmentCallsFragment - onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        loadData();
                    }
                });

        return callsTabDepartmentCallsView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        Logger.log("CallsTabDepartmentCallsFragment - setUserVisibleHint - visible:"+visible);
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.log("CallsTabDepartmentCallsFragment - onResume - getUserVisibleHint:"+getUserVisibleHint());
        if (getActivity() instanceof  MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (needsRefresh) {
                Logger.log("CallsTabDepartmentCallsFragment - onResume - needsRefresh");
                needsRefresh = false;
                loadData();
            }
        }

    }

    public void setPicupNumber(String picupNumber) {
        this.picupNumber = picupNumber;
    }

    public String getPicupNumber() {
        return picupNumber;
    }

    public void update(SortedSet<Cdr> newCdrs) {
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
        String tokenId = PicupApplication.getToken();
        if(TextUtils.isEmpty(tokenId)) {
            swipeRefreshLayout.setRefreshing(false);
            listener.reAuthenticate();
            return;
        }
        cdrs.clear();
        sections.clear();
        //ImmutableMap<String, String> callHistoryParams = ImmutableMap.of("deptName", "%_%", "maxRecordsBack", String.valueOf(PicupApplication.pageSize), "applicationId", PicupApplication.apiId);
        ImmutableMap<String, String> callHistoryParams = ImmutableMap.of("maxRecordsBack", String.valueOf(PicupApplication.pageSize), "applicationId", PicupApplication.apiId);
        //ImmutableMap<String, String> callHistoryParams = ImmutableMap.of("applicationId", PicupApplication.apiId, "userId", String.valueOf(PicupApplication.getUserId()));
        //callHistoryParams = ImmutableMap.of("applicationId", PicupApplication.apiId);
        int accountId = PicupApplication.getAccountId();
        final Call<CallHistory> call = picupService.callHistory(tokenId, String.valueOf(accountId), callHistoryParams);
                call.enqueue(new Callback<CallHistory>() {
                @Override
                public void onResponse (Call < CallHistory > call, Response < CallHistory > response) {
                    Logger.log("CallsTabDepartmentCallsFragment - loadData - request callHistory - onResponse");
                    swipeRefreshLayout.setRefreshing(false);
                    if (response.isSuccessful()) {
                    CallHistory callHistory = response.body();
                    if (callHistory != null) {
                        cdrs = callHistory.getCdrs();
                        SortedSet<Cdr> filteredCdrs = new TreeSet<Cdr>();
                        //UNITE 1490 - need to filter based on department such that all of the department calls that the user is a member of
                        if (TextUtils.equals(picupNumber, "All Numbers")) {
                            filteredCdrs = cdrs;
                        } else {
                            for (Cdr cdr : cdrs) {
                                Logger.log("CallsTabDepartmentCallsFragment - loadData - cdr - dept:"+cdr.getDeptName()+" ani:"+cdr.getAni()+" dnis:"+cdr.getDnis()+" callstate:"+cdr.getCallState());

                                if (TextUtils.equals(cdr.getDnis(), picupNumber)) {
                                    filteredCdrs.add(cdr);
                                }
                            }
                        }
                        if (filteredCdrs.isEmpty()) {
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
                            //sections.put(midnightCalendar, new ArrayList<Cdr>());
                            currentCallCalendar.add(Calendar.DAY_OF_MONTH, -1);
                        } while (currentCallCalendar.after(firstCallCalendar));
                        for (Calendar midnightCalendarKey : midnightCalendarKeys) {
                            sections.put(midnightCalendarKey, new ArrayList<Cdr>());
                        }
                        for (Cdr filteredCdr : filteredCdrs) {
                            Map.Entry<Calendar, List<Cdr>> entry = sections.ceilingEntry(filteredCdr.getCallCalendar());
                            entry.getValue().add(filteredCdr);
                        }
                        //NavigableMap<Calendar, List<Cdr>> descendingData = data.descendingMap();
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
                            ArrayAdapter valueAdapter = new CallArrayAdapter(getActivity(), R.layout.call_row_item, R.id.duration_textview, value);
                            adapter.addSection(key, valueAdapter);
                        }
                        callListView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        //callListView.setOnScrollListener(CallsTabDepartmentCallsFragment.this);
                    }
                } else {
                        if (response.errorBody() != null)
                            try {
                                Logger.log("CallsTabDepartmentCallsFragment - loadData - failed - response:"+response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(0);
                    errorDialogFragment.show(getFragmentManager(), "errorDialogFragment");

                }
            }
            @Override
            public void onFailure (Call < CallHistory > call, Throwable t){
                ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(1);
                errorDialogFragment.show(getFragmentManager(), "errorDialogFragment");
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem > 0) {
            int lastVisibleItem = firstVisibleItem + visibleItemCount;
            if (lastVisibleItem == totalItemCount) {
                callListView.setOnScrollListener(null);
                //fetchMore();
            }
        }
    }

    public void filter(String picupNumber) {
        this.picupNumber = picupNumber;
        sections.clear();
        SortedSet<Cdr> filteredCdrs = new TreeSet<Cdr>();
        if (TextUtils.equals(picupNumber, "All Numbers")) {
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
        //final ImmutableMap<String, String> callHistoryParams = ImmutableMap.of("lastId", String.valueOf(lastId), "maxRecordsBack", String.valueOf(fetchSize), "applicationId", PicupApplication.apiId, "userId", String.valueOf(PicupApplication.getUserId()));
        final ImmutableMap<String, String> callHistoryParams = ImmutableMap.of("lastId", String.valueOf(lastId), "maxRecordsBack", String.valueOf(fetchSize), "applicationId", PicupApplication.apiId);

        int accountId = PicupApplication.getAccountId();
        final Call<CallHistory> call = picupService.callHistory(tokenId, String.valueOf(accountId), callHistoryParams);
        call.enqueue(new Callback<CallHistory>() {
            @Override
            public void onResponse(Call<CallHistory> call, Response<CallHistory> response) {
                Logger.log("CallsTabDepartmentCallsFragment - fetchMore - request callHistory - onResponse");
                if (response.isSuccessful()) {
                    CallHistory callHistory = response.body();
                    SortedSet<Cdr> latestCdrs = callHistory.getCdrs();
                    if (latestCdrs.isEmpty()) {
                        return;
                    }
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
                    callListView.setOnScrollListener(CallsTabDepartmentCallsFragment.this);
                    cdrs.addAll(latestCdrs);
                } else {
                    if (response.errorBody() != null)
                        try {
                            Logger.log("CallsTabDepartmentCallsFragment - fetchMore - failed - response:"+response.errorBody().string());
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

    public interface OnCallsTabDepartmentCallsFragmentListener {
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
        private final String TAG = com.picup.calling.adapter.CallArrayAdapter.class.getSimpleName();
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
                viewHolder.callerTextView = (TextView)rowView.findViewById(R.id.caller_textview);
                viewHolder.callerInfoLayout = (LinearLayout)rowView.findViewById(R.id.call_info_layout);
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

            if (!TextUtils.isEmpty(cdr.getAni())) {
                holder.thumbnailImageView.setImageURI(null);
                holder.initialTextView.setText("");
                Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(cdr.getAni()));
                if (!permissionChecked) {
/*
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_READ_CONTACTS);
                            return rowView;
                        }
                    }
*/
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_READ_CONTACTS);
                        permissionChecked = true;
                    }
                }
                try {
                    cursor = getContext().getContentResolver().query(uri, PROFILE_PROJECTION, null, null, null);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        thumbnailUriString = cursor.getString(PROFILE_THUMBNAIL_URI_INDEX);
                        fullName = cursor.getString(PROFILE_DISPLAY_NAME_PRIMARY_INDEX);
                    }
                }
                if (!TextUtils.isEmpty(thumbnailUriString)) {
                    holder.thumbnailImageView.setImageURI(Uri.parse(thumbnailUriString));
                    Drawable thumbnailDrawable = holder.thumbnailImageView.getDrawable();
                    RoundedBitmapDrawable roundedBitmapDrawable = PicupImageUtils.toRoundedBitmapDrawable(getContext(), thumbnailDrawable);
                    if (roundedBitmapDrawable != null) {
                        holder.thumbnailImageView.setImageDrawable(roundedBitmapDrawable);
                    }
                    holder.thumbnailImageView.setVisibility(View.VISIBLE);
                    holder.thumbnailImageView.setPadding(0,0,0,0);
                    final Cursor cursorRef = cursor;
                    holder.thumbnailImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.showContactDetail(cursorRef.getInt(PROFILE_ID_INDEX));
                        }
                    });
                    //holder.thumbnailImageView.setLayoutParams(new RelativeLayout.LayoutParams(0,0));
                    holder.initialTextView.setVisibility(View.INVISIBLE);
                    holder.infoIconView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
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
                            listener.showContactDetail(cursorRef.getInt(PROFILE_ID_INDEX));
                        }
                    });
                    holder.initialTextView.setVisibility(View.VISIBLE);
                    holder.thumbnailImageView.setVisibility(View.INVISIBLE);
                    holder.infoIconView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            listener.showContactProfile(v, cursorRef.getInt(PROFILE_ID_INDEX));
                        }
                    });
                } else {
                    holder.thumbnailImageView.setImageResource(R.drawable.ic_add_black_24dp);
                    holder.thumbnailImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            String phoneNumber = holder.callerTextView.getText().toString();
                            //AddContactOptionDialogFragment addContactOptionDialogFragment = AddContactOptionDialogFragment.newInstance(event.getX(), event.getY(), phoneNumber);
                            //addContactOptionDialogFragment.show(getActivity().getSupportFragmentManager(), "AddContactOptionDialogFragment");
                            //listener.showAddContactOption(event.getX(), event.getY(), phoneNumber);
                            listener.showAddContactOption(v, phoneNumber);
                        }
                    });
                    holder.thumbnailImageView.setPadding(30,30,30,30);
                    holder.thumbnailImageView.setVisibility(View.VISIBLE);
                    holder.initialTextView.setVisibility(View.INVISIBLE);
                    //holder.infoIconView.setVisibility(View.INVISIBLE);  //no profile detail if no contact
                    holder.infoIconView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            String phoneNumber = holder.callerTextView.getText().toString();
                            listener.showContactProfileForNonContact(v, phoneNumber);
                        }
                    });
                }
                holder.callerTextView.setTag(cdr.getAni());
                holder.callerInfoLayout.setTag(cdr.getAni());

                if (!TextUtils.isEmpty(fullName)) {
                    holder.callerTextView.setText(fullName);
                } else {
                    if (TextUtils.equals(cdr.getCallState(), "Call Answered")) {
                        holder.callerTextView.setText(PhoneNumberUtils.formatNumber(cdr.getDn()));
                    } else {
                        holder.callerTextView.setText(PhoneNumberUtils.formatNumber(cdr.getAni()));
                    }
                }
                //final Cursor cursorRef = cursor;
                /*holder.callerTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        //listener.showContactProfile(this, cursorRef.getInt(PROFILE_ID_INDEX));
                        listener.call((String)v.getTag());
                    }
                    }); */
                holder.callerInfoLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //listener.showContactProfile(this, cursorRef.getInt(PROFILE_ID_INDEX));
                        listener.call((String)v.getTag());
                    }
                });
            }
            if (TextUtils.isEmpty(cdr.getExtension())) {
                if (TextUtils.equals("Call Answered", cdr.getCallState())) {
                    holder.callDirectionImageView.setImageResource(R.drawable.ic_outbound_call);
                } else if (TextUtils.equals("Not Answered", cdr.getCallState())) {
                    holder.callDirectionImageView.setImageResource(R.drawable.ic_missed_call);
                } else if (TextUtils.equals("Voicemail", cdr.getCallState())) {
                    holder.callDirectionImageView.setImageResource(R.drawable.ic_voice_mail);
                }
            } else {
                if (TextUtils.equals("Call Answered", cdr.getCallState())) {
                    holder.callDirectionImageView.setImageResource(R.drawable.ic_inbound_call);
                } else if (TextUtils.equals("Not Answered", cdr.getCallState())) {
                    holder.callDirectionImageView.setImageResource(R.drawable.ic_missed_call);
                } else if (TextUtils.equals("Voicemail", cdr.getCallState())) {
                    holder.callDirectionImageView.setImageResource(R.drawable.ic_voice_mail);
                }
            }
            if (cdr.getDuration() == 0) {
                holder.callResultImageView.setImageResource(R.drawable.ic_outbound_cancelled);
            } else {
                holder.callResultImageView.setImageBitmap(null);
            }
            if (TextUtils.equals("Not Answered", cdr.getCallState()) && !TextUtils.equals("Voicemail", cdr.getCallState())) {
                holder.callerTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.errorRed));
            } else {
                holder.callerTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.textColor));
            }
            holder.departmentTextView.setText(cdr.getDeptName()); // ???
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

package com.picup.calling;

import android.content.Context;
import com.picup.calling.adapter.SectionArrayAdapter;
import com.picup.calling.base.ExpandableHeightListView;
import com.picup.calling.base.PicupApplication;
import com.picup.calling.data.Phone;
import com.picup.calling.dialog.ErrorDialogFragment;
import com.picup.calling.network.CallHistory;
import com.picup.calling.network.Cdr;
import com.picup.calling.network.PicupService;
import com.picup.calling.util.Logger;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.ImmutableMap;
import com.google.i18n.phonenumbers.NumberParseException;
import com.picup.calling.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactProfileCallsTabContent extends Fragment  {
    private static final String TAG = ContactProfileCallsTabContent.class.getSimpleName();
    private static final String ARG_CONTACT_ID = "android.idt.net.com.picup.calling.ContactProfileCallsTabContent.CONTACT_ID";
    private static final String ARG_PHONE_NUM = "android.idt.net.com.picup.calling.ContactProfileCallsTabContent.PHONE_NUM";

    private TextView dummyTextView = null;

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
    private String nonContactPhoneNumber = null;

    private ExpandableHeightListView callListView = null;
    private SectionArrayAdapter adapter = null;

    private List<Phone> phones = new ArrayList<>();

    private static SwipeRefreshLayout swipeRefreshLayout = null;
    private SwipeRefreshLayout.OnRefreshListener swipeRefreshListener;


    private static PicupService picupService = PicupService.retrofit.create(PicupService.class);

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

    private CallHistory callHistory = null;

    private OnContactProfileCallsTabContentListener listener;

    public ContactProfileCallsTabContent() {
        // Required empty public constructor
    }

    public static ContactProfileCallsTabContent newInstance(int contactId) {
        ContactProfileCallsTabContent fragment = new ContactProfileCallsTabContent();
        Bundle args = new Bundle(1);
        args.putInt(ARG_CONTACT_ID, contactId);
        fragment.setArguments(args);
        return fragment;
    }

    public static ContactProfileCallsTabContent newInstance(String nonContactPhoneNumber) {
        ContactProfileCallsTabContent fragment = new ContactProfileCallsTabContent();
        Bundle args = new Bundle(1);
        args.putString(ARG_PHONE_NUM, nonContactPhoneNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            contactId = getArguments().getInt(ARG_CONTACT_ID);
            nonContactPhoneNumber = getArguments().getString(ARG_PHONE_NUM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contactProfileContactCallsContentView = inflater.inflate(R.layout.contact_profile_calls_tab_content, container, false);
        callListView = (ExpandableHeightListView)contactProfileContactCallsContentView.findViewById(R.id.contact_profile_call_listview);
        swipeRefreshLayout = (SwipeRefreshLayout)contactProfileContactCallsContentView.findViewById(R.id.swiperefresh);

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

        return contactProfileContactCallsContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.log("ContactProfileCallsTabContent - onActivityCreated");
        loadData();
        //phoneCursorAdapter = new PhoneCursorAdapter(getActivity(), null, ic_key_0);
        //phoneListView.setAdapter(phoneCursorAdapter);
        Bundle args = new Bundle(1);
        args.putInt(ARG_CONTACT_ID, contactId);
        //getLoaderManager().initLoader(PHONE_LOADER_ID, args, this);
    }

    public void loadData() {
        String tokenId = PicupApplication.getToken();
        if (TextUtils.isEmpty(tokenId)) {
            swipeRefreshLayout.setRefreshing(false);
            listener.reAuthenticate();
            return;
        }
        //final ImmutableMap<String, String> callHistoryParams = ImmutableMap.of("applicationId", PicupApplication.apiId);
        int accountId = PicupApplication.getAccountId();
        String userId = String.valueOf(PicupApplication.getUserId());
        ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<String, String>();
        builder.put("applicationId", PicupApplication.apiId);
        if (!PicupApplication.isAdmin())
            builder.put("userId", userId);
        final ImmutableMap<String, String> callHistoryParams = builder.build();

        final Call<CallHistory> call = picupService.callHistory(tokenId, String.valueOf(accountId), callHistoryParams);
        call.enqueue(new Callback<CallHistory>() {
            @Override
            public void onResponse(Call<CallHistory> call, Response<CallHistory> response) {
                Logger.log("ContactProfileCallsTabContent - loadData - request callHistory - onResponse");
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful()) {
                    callHistory = response.body();
                    if (callHistory != null) {
                        //only cdrs with the numbers of the contact should display..filtering should be at the query level but not available now
                        SortedSet<Cdr> initCdrs = callHistory.getCdrs();
                        SortedSet<Cdr> filteredCdrs = new TreeSet<Cdr>();

                        ContactProfileActivity activity = null;
                        ContactProfileUnknownActivity unknownActivity = null;
                        if (getActivity()!= null && !getActivity().isFinishing()) {
                            if (getActivity() instanceof ContactProfileActivity)
                                activity = (ContactProfileActivity) getActivity();
                            else if (getActivity() instanceof ContactProfileUnknownActivity)
                                unknownActivity = (ContactProfileUnknownActivity) getActivity();
                        }
                        if ((activity == null && unknownActivity == null) || PicupApplication.phoneNumberUtil == null)
                            return;

                        Logger.log("ContactProfileCallsTabContent - loadData - initCdrs size:"+initCdrs.size());
                        if (activity != null) {
                        for (Cdr cdr : initCdrs) {
                            Logger.log("ContactProfileCallsTabContent - loadData - cdr - ani:"+cdr.getAni()+" dnis:"+cdr.getDnis() + " dn:" + cdr.getDn() + " origDn:" + cdr.getOrigdn() + " callstate:" + cdr.getCallState() + " deptName:" + cdr.getDeptName());
                            boolean matches = false;
                            try { //check on ani

                                if (activity.isValidContactNumber(PicupApplication.phoneNumberUtil.parse(PhoneNumberUtils.stripSeparators(cdr.getAni()), Locale.getDefault().getCountry())))
                                {
                                    matches = true;
                                    filteredCdrs.add(cdr);
                                }
                            } catch (NumberParseException e) {
                                Logger.log("ContactProfileCallsTabContent - loadData - ani:"+cdr.getAni()+" NumberParseException - "+e.toString());
                            }
                            if (!matches) { //check on dn
                                try {
                                    if (activity.isValidContactNumber(PicupApplication.phoneNumberUtil.parse(PhoneNumberUtils.stripSeparators(cdr.getDn()), Locale.getDefault().getCountry())))
                                    {
                                        filteredCdrs.add(cdr);
                                    }
                                } catch (NumberParseException e) {
                                    Logger.log("ContactProfileCallsTabContent - loadData - dn:"+cdr.getDn()+" NumberParseException - "+e.toString());
                                }
                            } }

                        } else if (unknownActivity != null) {
                            {
                                for (Cdr cdr : initCdrs) {
                                    Logger.log("ContactProfileCallsTabContent - loadData - cdr - ani:"+cdr.getAni()+" dnis:"+cdr.getDnis() + " dn:" + cdr.getDn() + " origDn:" + cdr.getOrigdn() + " callstate:" + cdr.getCallState() + " deptName:" + cdr.getDeptName());
                                    boolean matches = false;
                                    try { //check on ani

                                        if (unknownActivity.isValidContactNumber(PicupApplication.phoneNumberUtil.parse(PhoneNumberUtils.stripSeparators(cdr.getAni()), Locale.getDefault().getCountry())))
                                        {
                                            matches = true;
                                            filteredCdrs.add(cdr);
                                        }
                                    } catch (NumberParseException e) {
                                        Logger.log("ContactProfileCallsTabContent - loadData - ani:"+cdr.getAni()+" NumberParseException - "+e.toString());
                                    }
                                    if (!matches) { //check on dn
                                        try {
                                            if (unknownActivity.isValidContactNumber(PicupApplication.phoneNumberUtil.parse(PhoneNumberUtils.stripSeparators(cdr.getDn()), Locale.getDefault().getCountry())))
                                            {
                                                filteredCdrs.add(cdr);
                                            }
                                        } catch (NumberParseException e) {
                                            Logger.log("ContactProfileCallsTabContent - loadData - dn:"+cdr.getDn()+" NumberParseException - "+e.toString());
                                        }
                                    } }

                            }
                        }
                        Logger.log("ContactProfileCallsTabContent - loadData - filteredCdrs size:"+filteredCdrs.size());
                        if (filteredCdrs.isEmpty()) {
                            return;
                        }

                        SortedSet<Cdr> cdrs = filteredCdrs;
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
                        if (cdrs.size()>0)
                            lastCallCalendar.setTime(cdrs.first().getCallCalendar().getTime());
                        Calendar firstCallCalendar = Calendar.getInstance();
                        if (cdrs.size()>0) {
                            firstCallCalendar.setTime(cdrs.last().getCallCalendar().getTime());
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
                            data.put(midnightCalendar, new ArrayList<Cdr>());
                            currentCallCalendar.add(Calendar.DAY_OF_MONTH, -1);
                            midnightCalendarKeys.add(midnightCalendar);
                        } while (currentCallCalendar.after(firstCallCalendar));
                        TreeMap<Calendar, List<Cdr>> data = new TreeMap<Calendar, List<Cdr>>();
                        for (Calendar midnightCalendarKey : midnightCalendarKeys) {
                            data.put(midnightCalendarKey, new ArrayList<Cdr>());
                        }
                        for (Cdr cdr : cdrs) {
                            Map.Entry<Calendar, List<Cdr>> entry = data.floorEntry(cdr.getCallCalendar());
                            entry.getValue().add(cdr);
                        }
                        NavigableMap<Calendar, List<Cdr>> descendingData = data.descendingMap();
                        Iterator<Map.Entry<Calendar, List<Cdr>>> entries = descendingData.entrySet().iterator();

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
                    } else {
                        ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(0);
                        errorDialogFragment.show(getFragmentManager(), "errorDialogFragment");
                    }
                } else {
                    try {
                        Logger.log("ContactProfileCallsTabContent - loadData - onResponse - failure error:" + response.errorBody().string());
                        ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(0);
                        errorDialogFragment.show(getFragmentManager(), "errorDialogFragment");
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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnContactProfileCallsTabContentListener) {
            listener = (OnContactProfileCallsTabContentListener) context;
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

    public void doRefresh() {
        Logger.log("ContactProfileallsTabContent - doRefresh");
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                    // directly call onRefresh() method as there is a bug in the library that won't call the method
                    swipeRefreshListener.onRefresh();
                }
            });
        }
    }

    public interface OnContactProfileCallsTabContentListener {
        void reAuthenticate();
    }

    public static class CallArrayAdapter extends ArrayAdapter<Cdr> {
        private final String TAG = CallArrayAdapter.class.getSimpleName();
        private final int TYPE_SECTION_ITEM = 1;
        private LayoutInflater inflater = null;
        private static SimpleDateFormat time1Sdf = new SimpleDateFormat("h:mm a");
        private static SimpleDateFormat time2Sdf = new SimpleDateFormat("hh:mm:ss");

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
                rowView = inflater.inflate(R.layout.contact_profile_call_row_item, parent, false);
                final ViewHolder viewHolder = new ViewHolder();
                viewHolder.callDirectionImageView = (ImageView)rowView.findViewById(R.id.call_direction_imageview);
                viewHolder.callResultImageView = (ImageView)rowView.findViewById(R.id.call_result_imageview);
                viewHolder.timestampTextView = (TextView) rowView.findViewById(R.id.timestamp_textview);
                viewHolder.calleeTextView = (TextView) rowView.findViewById(R.id.callee_textview);
                viewHolder.departmentTextView = (TextView)rowView.findViewById(R.id.department_textview);
                viewHolder.durationTextView = (TextView)rowView.findViewById(R.id.duration_textview);
                rowView.setTag(viewHolder);
            }
            final ViewHolder holder = (ViewHolder)rowView.getTag();

            Cdr cdr = super.getItem(position);

            int direction = cdr.getDirection();

            holder.callResultImageView.setImageBitmap(null);

            if (direction == Cdr.CDR_OUTBOUND) {
                holder.callDirectionImageView.setImageResource(R.drawable.ic_outbound_call);
                if (TextUtils.equals("Not Answered", cdr.getCallState())) {//duration has value so not good enoughcdr.getDuration() == 0) {
                    holder.callResultImageView.setImageResource(R.drawable.ic_outbound_cancelled);
                }
            } else { //inbound show state in direction
                if (TextUtils.equals("Call Answered", cdr.getCallState())) {
                    holder.callDirectionImageView.setImageResource(R.drawable.ic_inbound_call);
                } else if (TextUtils.equals("Not Answered", cdr.getCallState())) {
                    holder.callDirectionImageView.setImageResource(R.drawable.ic_missed_call);
                } else if (TextUtils.equals("Voicemail", cdr.getCallState())) {
                    holder.callDirectionImageView.setImageResource(R.drawable.ic_voice_mail);
                }
            }
            //for inbound or outbound not allowed
            if (TextUtils.equals("Call Not Allowed", cdr.getCallState())) {
                holder.callResultImageView.setImageResource(R.drawable.ic_inbound_not_allowed);
            }
            /*
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
            */

            holder.timestampTextView.setText(time1Sdf.format(cdr.getCallCalendar().getTime()));
            if (direction == Cdr.CDR_INBOUND) {
                holder.calleeTextView.setText("To: " + PhoneNumberUtils.formatNumber(cdr.getDnis()));  //maybe origDn
            } else {
                holder.calleeTextView.setText("From: " + PhoneNumberUtils.formatNumber(cdr.getAni()));
            }
            //holder.departmentTextView.setText(cdr.getDeptName());
            holder.departmentTextView.setText("");
            boolean hasUser = false;
            if (!TextUtils.isEmpty(cdr.getUserName())) {
                hasUser = true;
                holder.departmentTextView.setText(cdr.getUserName());
            }
            if (!TextUtils.isEmpty(cdr.getDeptName()) && hasUser) {
                holder.departmentTextView.append(" (" + cdr.getDeptName() + ")");
            } else if (!TextUtils.isEmpty(cdr.getDeptName())) {
                holder.departmentTextView.setText(cdr.getDeptName());
            }
            long duration = cdr.getDuration();
            String hhmmss = String.format("%02d:%02d:%02d", TimeUnit.SECONDS.toHours(duration),
                    TimeUnit.SECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(duration)),
                    TimeUnit.SECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(duration)));
            holder.durationTextView.setText(hhmmss);

            return rowView;
        }

        public class ViewHolder {
            public ImageView callDirectionImageView;
            public ImageView callResultImageView;
            public TextView timestampTextView;
            public TextView calleeTextView;
            public TextView departmentTextView;
            public TextView durationTextView;
        }
    }
}

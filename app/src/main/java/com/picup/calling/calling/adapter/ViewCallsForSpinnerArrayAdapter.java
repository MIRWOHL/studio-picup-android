package com.picup.calling.adapter;

import android.content.Context;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.picup.calling.R;

import java.util.List;

/**
 * Created by frank.truong on 12/13/2016.
 */

public class ViewCallsForSpinnerArrayAdapter extends ArrayAdapter<String> {
    private static final String TAG = ViewCallsForSpinnerArrayAdapter.class.getSimpleName();
    private static LayoutInflater inflater = null;

    public ViewCallsForSpinnerArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        inflater = LayoutInflater.from(context);
    }

    public ViewCallsForSpinnerArrayAdapter(Context context, int resource, int textViewResourceId, List<String> objects) {
        super(context, resource, textViewResourceId, objects);
        inflater = LayoutInflater.from(context);
    }

    public ViewCallsForSpinnerArrayAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
        super(context, resource, textViewResourceId, objects);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView phoneTextView = (TextView)view.findViewById(R.id.phone_textview);
        phoneTextView.setText(PhoneNumberUtils.formatNumber(phoneTextView.getText().toString()));
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View rowView = null;

        if (position == 0) {
            rowView = inflater.inflate(R.layout.view_calls_for_spinner_dropdown_header, parent, false);
        } else if (position == this.getCount() - 1) {
            rowView = inflater.inflate(R.layout.view_calls_for_spinner_dropdown_footer, parent, false);
        } else {
            rowView = inflater.inflate(R.layout.view_calls_for_spinner_dropdown_view, parent, false);
            TextView phoneTextView = (TextView)rowView.findViewById(R.id.phone_textview);
            phoneTextView.setText(PhoneNumberUtils.formatNumber(getItem(position)));
        }
        return rowView;
    }

    static class ViewHolder {
        public TextView phone = null;
    }

    @Override
    public int getCount() {
        return super.getCount() + 2;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Nullable
    @Override
    public String getItem(int position) {
        if (position == 0) {
            return getContext().getString(R.string.view_calls_for_number_label);
        } else if (position == this.getCount() - 1) {
            return getContext().getString(R.string.all_numbers_label);
        } else {
            return super.getItem(position - 1);
        }
    }

    public boolean isEnabled(int position) {
        return (position != 0);
    }
}

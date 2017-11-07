package com.picup.calling.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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

public class CallFromSpinnerArrayAdapter extends ArrayAdapter<String> {
    private static final String TAG = CallFromSpinnerArrayAdapter.class.getSimpleName();
    private static LayoutInflater inflater = null;

    public CallFromSpinnerArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        inflater = LayoutInflater.from(context);
    }

    public CallFromSpinnerArrayAdapter(Context context, int resource, int textViewResourceId, List<String> objects) {
        super(context, resource, textViewResourceId, objects);
        inflater = LayoutInflater.from(context);
    }

    public CallFromSpinnerArrayAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
        super(context, resource, textViewResourceId, objects);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView phoneTextView = (TextView)view.findViewById(R.id.phone_textview);
        phoneTextView.setText(PhoneNumberUtils.formatNumber(phoneTextView.getText().toString()));
/*
        if (this.getCount() > 1) {
            if (this.isEnabled(position)) {
                view = super.getView(position, convertView, parent);
                TextView phoneTextView = (TextView) view.findViewById(R.id.phone_textview);
                phoneTextView.setText(this.getItem(position));
            }
        }
*/
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View rowView = null;

        if (this.getCount() > 1) {
            if (position == 0) {
                rowView = inflater.inflate(R.layout.call_from_spinner_dropdown_header, parent, false);
            } else {
                rowView = inflater.inflate(R.layout.call_from_spinner_dropdown_view, parent, false);
                TextView phoneTextView = (TextView) rowView.findViewById(R.id.phone_textview);
                phoneTextView.setText(PhoneNumberUtils.formatNumber(this.getItem(position)));
            }
        } else {
            rowView = inflater.inflate(R.layout.call_from_spinner_view, parent, false);
            rowView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
            TextView phoneTextView = (TextView)rowView.findViewById(R.id.phone_textview);
            phoneTextView.setText("");
        }
        return rowView;
    }

    static class ViewHolder {
        public TextView phone = null;
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

    @Nullable
    @Override
    public String getItem(int position) {
        if (position == 0) {
            return getContext().getString(R.string.make_outbound_call_from_label);
        } else {
            return super.getItem(position - 1);
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    public boolean isEnabled(int position) {
        return position != 0;
    }
}

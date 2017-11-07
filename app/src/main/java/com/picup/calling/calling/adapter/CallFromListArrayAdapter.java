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

public class CallFromListArrayAdapter extends ArrayAdapter<String> {
    private static final String TAG = CallFromListArrayAdapter.class.getSimpleName();
    private static LayoutInflater inflater = null;

    public CallFromListArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        inflater = LayoutInflater.from(context);
    }

    public CallFromListArrayAdapter(Context context, int resource, int textViewResourceId, List<String> objects) {
        super(context, resource, textViewResourceId, objects);
        inflater = LayoutInflater.from(context);
    }

    public CallFromListArrayAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
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

/*
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
*/

    static class ViewHolder {
        public TextView phone = null;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return super.getItem(position);
    }
}


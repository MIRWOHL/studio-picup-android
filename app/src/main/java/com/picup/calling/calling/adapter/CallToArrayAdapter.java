package com.picup.calling.adapter;

import android.content.Context;
import com.picup.calling.data.Phone;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.picup.calling.R;

import java.util.List;

/**
 * Created by frank.truong on 12/14/2016.
 */

public class CallToArrayAdapter extends ArrayAdapter<Phone> implements AdapterView.OnItemSelectedListener {
    private static final String TAG = CallToArrayAdapter.class.getSimpleName();
    private static LayoutInflater inflater = null;

    public static final int ITEM_TYPE = 0;
    public static final int HEADER_TYPE = 1;

    public CallToArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        inflater = LayoutInflater.from(context);
    }

    public CallToArrayAdapter(Context context, int resource, int textViewResourceId, List<Phone> objects) {
        super(context, resource, textViewResourceId, objects);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view;

        if (position == 0) {
            view = inflater.inflate(R.layout.call_to_spinner_dropdown_header, null);
        } else {
            view = inflater.inflate(R.layout.call_to_spinner_dropdown_view, null);
            Phone phone = getItem(position);
            TextView type = (TextView)view.findViewById(R.id.type_textview);
            type.setText(phone.getType().toString());
            TextView number = (TextView)view.findViewById(R.id.number_textview);
            number.setText(phone.getNumber());
            if (phone.isSelected()) {
                type.setTextColor(ContextCompat.getColor(getContext(), R.color.mainPurple));
                number.setTextColor(ContextCompat.getColor(getContext(), R.color.mainPurple));
            } else {
                type.setTextColor(ContextCompat.getColor(getContext(), R.color.textColor));
                number.setTextColor(ContextCompat.getColor(getContext(), R.color.textColor));
            }
            Log.d(TAG, "setIgnoreSeletionForCalling(false**********************");
            phone.setIgnoreSelectionForCalling(false); // start listening to itemSelectionEvent
        }
        return view;
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

    @Nullable
    @Override
    public Phone getItem(int position) {
        if (position == 0) {
            return new Phone();
        }
        return super.getItem(position - 1);
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 ? 1 : 0);
    }

    @Override
    public boolean isEnabled(int position) {
        return (position != 0);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        for (int i=0; i<getCount(); i++) {
            Phone phone = getItem(i);
            phone.setSelected(i == parent.getSelectedItemPosition() ? true : false);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    static class ViewHolder {
        public TextView typeTextView = null;
    }

    static class DropdownViewHolder {
        public TextView type = null;
        public TextView number = null;
        public ImageView icon = null;
    }
}

package com.picup.calling.adapter;

import android.content.Context;

import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.picup.calling.R;

import java.util.List;

/**
 * Created by frank.truong on 12/13/2016.
 */

public class CallFromTypeSpinnerArrayAdapter extends ArrayAdapter<CharSequence> {
    private static final String TAG = CallFromTypeSpinnerArrayAdapter.class.getSimpleName();
    private static LayoutInflater inflater = null;

    public CallFromTypeSpinnerArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        inflater = LayoutInflater.from(context);
    }

    public CallFromTypeSpinnerArrayAdapter(Context context, int resource, List<CharSequence> objects) {
        super(context, resource, objects);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            rowView = inflater.inflate(R.layout.call_from_type_spinner_dropdown_view, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.typeTextview = (TextView) rowView.findViewById(R.id.call_from_type_dropdown_textview);
            rowView.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder)rowView.getTag();
        CharSequence type = getItem(position);
        holder.typeTextview.setText(type);
        if (parent instanceof Spinner) {
            Spinner spinner = (Spinner)parent;
            if (spinner.getSelectedItemPosition() == position) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.typeTextview.setTextColor(ContextCompat.getColor(getContext(), R.color.mainPurple));
                } else {
                    holder.typeTextview.setTextColor(getContext().getResources().getColor(R.color.mainPurple));
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.typeTextview.setTextColor(ContextCompat.getColor(getContext(), R.color.textColor));
                } else {
                    holder.typeTextview.setTextColor(getContext().getResources().getColor(R.color.textColor));
                }
            }
        }
        return rowView;
    }

    static class ViewHolder {
        public TextView typeTextview = null;
    }
}

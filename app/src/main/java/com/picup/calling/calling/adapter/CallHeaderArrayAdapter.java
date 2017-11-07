package com.picup.calling.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.picup.calling.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by frank.truong on 12/14/2016.
 */

public class CallHeaderArrayAdapter extends ArrayAdapter<Calendar> {
    private static final String TAG = CallHeaderArrayAdapter.class.getSimpleName();
    private static LayoutInflater inflater = null;
    private static SimpleDateFormat headerSdf = new SimpleDateFormat("MM/dd/yyyy");

    public CallHeaderArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        inflater = LayoutInflater.from(context);
    }

    public CallHeaderArrayAdapter(Context context, int resource, int textViewResourceId, List<Calendar> calendars) {
        super(context, resource, textViewResourceId, calendars);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            rowView = inflater.inflate(R.layout.call_row_item_header, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.headerTextView = (TextView)rowView.findViewById(R.id.header_textview);
            rowView.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder)rowView.getTag();
        Calendar calendar = super.getItem(position);
        Calendar rightNow = Calendar.getInstance();
        rightNow.set(Calendar.HOUR_OF_DAY, 0);
        rightNow.set(Calendar.MINUTE, 0);
        rightNow.set(Calendar.SECOND, 0);
        rightNow.set(Calendar.MILLISECOND, -1); // 1 second before midnight
        if (calendar.after(rightNow)) {
            holder.headerTextView.setText("Today");
        } else {
            rightNow.add(Calendar.DAY_OF_MONTH, -1);
            if (calendar.after(rightNow)) {
                holder.headerTextView.setText("Yesterday");
            } else {
                holder.headerTextView.setText(headerSdf.format(calendar.getTime()));
            }
        }
        return rowView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    static class ViewHolder {
        public TextView headerTextView;
    }
}

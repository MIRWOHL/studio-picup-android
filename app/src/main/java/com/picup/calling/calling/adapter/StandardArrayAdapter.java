package com.picup.calling.adapter;

import android.content.Context;
import com.picup.calling.data.SectionListItem;
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
 * Created by frank.truong on 4/5/2017.
 */

public class StandardArrayAdapter extends ArrayAdapter<SectionListItem> {
    private final List<SectionListItem> items;
    private static SimpleDateFormat headerSdf = new SimpleDateFormat("MM/dd/yyyy");

    public StandardArrayAdapter(final Context context, final int resource, final int textViewResourceId, final List<SectionListItem> items) {
        super(context, resource, textViewResourceId, items);
        this.items = items;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            final LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.call_row_item_header, null);
        }
        final SectionListItem currentItem = items.get(position);
        if (currentItem != null) {
            Calendar section = currentItem.section;
            final TextView textView = (TextView) view.findViewById(R.id.header_textview);
            Calendar rightNow = Calendar.getInstance();
            rightNow.set(Calendar.HOUR_OF_DAY, 0);
            rightNow.set(Calendar.MINUTE, 0);
            rightNow.set(Calendar.SECOND, 0);
            rightNow.set(Calendar.MILLISECOND, -1); // 1 second before midnight
            if (section.after(rightNow)) {
                textView.setText("Today");
            } else {
                rightNow.add(Calendar.DAY_OF_MONTH, -1);
                if (section.after(rightNow)) {
                    textView.setText("Yesterday");
                } else {
                    textView.setText(headerSdf.format(section.getTime()));
                }
            }
        }
        return view;
    }
}

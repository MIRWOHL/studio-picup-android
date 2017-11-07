package com.picup.calling.adapter;

import android.content.Context;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.picup.calling.R;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by frank.truong on 3/31/2017.
 */

public class SectionArrayAdapter extends BaseAdapter {
    private static final String TAG = SectionArrayAdapter.class.getSimpleName();

    private final Map<Calendar, Adapter> sections = new LinkedHashMap<>();
    private final CallHeaderArrayAdapter headers;
    private final static int TYPE_SECTION_HEADER = 0;
    private final static int TYPE_SECTION_ITEM = 1;

    public SectionArrayAdapter(Context context) {
        headers = new CallHeaderArrayAdapter(context, R.layout.call_row_item_header, R.id.header_textview);
    }

    public void addSection(Calendar header, Adapter adapter) {
        headers.add(header);
        sections.put(header, adapter);
    }

    public void clearSections() {
        if (headers != null && !headers.isEmpty()) {
            headers.clear();
        }
        if (sections != null && !sections.isEmpty()) {
            sections.clear();
        }
    }

    @Override
    public Object getItem(int position) {
        Object object = null;
        //Log.d(TAG, "*****getItem" + " position=" + position);
        for (Object section : this.sections.keySet()) {
            Adapter adapter = sections.get(section);
            int size = adapter.getCount() + 1;
            if (position == 0) {
                //return section;
                object = section;
                break;
            }
            if (position < size) {
                //return adapter.getItem(position - 1);
                object = adapter.getItem(position - 1);
                break;
            }
            position -= size;
        }
        //return null;
        return object;
    }

    @Override
    public int getCount() {
        int total = 0;
        for (Adapter adapter : this.sections.values()) {
            total += (adapter.getCount() + 1);
        }
        return total;
    }

    @Override
    public int getViewTypeCount() {

        int total = 1;
        for (Adapter adapter : this.sections.values()) {
            total += adapter.getViewTypeCount();
        }
        return total;

        //return 2;
    }

    @Override
    public int getItemViewType(int position) {
        int type = 1;
        for (Object section : this.sections.keySet()) {
            Adapter adapter = sections.get(section);
            int size = adapter.getCount() + 1;
            if (position == 0) {
                return TYPE_SECTION_HEADER;
                //type = TYPE_SECTION_HEADER;
                //break;
            }
            if (position < size) {
                return type + adapter.getItemViewType(position - 1);
                //return adapter.getItemViewType(position - 1);
                //type = adapter.getItemViewType(position - 1);
                //break;
            }
            position -= size;
            type += adapter.getViewTypeCount();
        }
        return -1;
    }

/*
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }
*/

    @Override
    public boolean isEnabled(int position) {
        return (getItemViewType(position) != TYPE_SECTION_HEADER);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        int sectionNumber = 0;
        View view = null;
        for (Object section : this.sections.keySet()) {
            Adapter adapter = sections.get(section);
            int size = adapter.getCount() + 1;
            if (position == 0) {
                return headers.getView(sectionNumber, convertView, parent);
                //break;
            }
            if (position < size) {
                return adapter.getView(position - 1, convertView, parent);
                //break;
            }
            position -= size;
            sectionNumber++;
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class HeaderViewHolder {
        public TextView dayTextView;
    }

    public static class ItemViewHolder {
        public ImageView thumbnailImageView;
        public TextView initialTextView;
        public ImageView callDirectionImageView;
        public ImageView callResultImageView;
        public TextView recipient1TextView;
        public TextView recipient2TextView;
        public TextView timeTextView;
        public TextView markerTextView;
    }

}

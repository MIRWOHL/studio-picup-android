package com.picup.calling.adapter;

import android.content.Context;
import com.picup.calling.data.Email;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.picup.calling.R;

import java.util.List;

/**
 * Created by frank.truong on 12/14/2016.
 */

public class ContactDetailEmailArrayAdapter extends ArrayAdapter<Email> {
    private static final String TAG = ContactDetailEmailArrayAdapter.class.getSimpleName();
    private static LayoutInflater inflater = null;

    public ContactDetailEmailArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        inflater = LayoutInflater.from(context);
    }

    public ContactDetailEmailArrayAdapter(Context context, int resource, int textViewResourceId, List<Email> objects) {
        super(context, resource, textViewResourceId, objects);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            rowView = inflater.inflate(R.layout.popup_contact_detail_phone_tab_content_item, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.email = (TextView)rowView.findViewById(R.id.address_textview);
            rowView.setTag(viewHolder);
        }
        Email emailType = getItem(position);
        ViewHolder holder = (ViewHolder)rowView.getTag();
        holder.email.setText(emailType.getAddress());

        return rowView;
    }

    static class ViewHolder {
        public TextView email = null;
    }
}

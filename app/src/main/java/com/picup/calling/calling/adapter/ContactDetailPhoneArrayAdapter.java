package com.picup.calling.adapter;

import android.content.Context;
import com.picup.calling.data.Phone;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.picup.calling.R;

import java.util.List;

/**
 * Created by frank.truong on 12/14/2016.
 */

public class ContactDetailPhoneArrayAdapter extends ArrayAdapter<Phone> {
    private static final String TAG = ContactDetailPhoneArrayAdapter.class.getSimpleName();
    private static LayoutInflater inflater = null;

    public ContactDetailPhoneArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        inflater = LayoutInflater.from(context);
    }

    public ContactDetailPhoneArrayAdapter(Context context, int resource, int textViewResourceId, List<Phone> objects) {
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
            viewHolder.number = (TextView)rowView.findViewById(R.id.number_textview);
            viewHolder.type = (TextView)rowView.findViewById(R.id.type_textview);
            viewHolder.icon = (ImageView)rowView.findViewById(R.id.icon_imageview);
            rowView.setTag(viewHolder);
        }
        Phone phoneType = getItem(position);
        ViewHolder holder = (ViewHolder)rowView.getTag();
        holder.number.setText(phoneType.getNumber());
        holder.type.setText(phoneType.getType().toString());

        return rowView;
    }

    static class ViewHolder {
        public TextView number = null;
        public TextView type = null;
        public ImageView icon = null;
    }
}

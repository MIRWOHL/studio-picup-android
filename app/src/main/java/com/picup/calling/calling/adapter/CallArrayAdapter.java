package com.picup.calling.adapter;

import android.content.Context;
import android.database.Cursor;
import com.picup.calling.network.Cdr;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.picup.calling.R;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by frank.truong on 12/14/2016.
 */

@Deprecated
public class CallArrayAdapter extends ArrayAdapter<Cdr> {
    private static final String TAG = CallArrayAdapter.class.getSimpleName();
    private static final int TYPE_SECTION_ITEM = 1;
    private static LayoutInflater inflater = null;
    private static SimpleDateFormat timeSdf = new SimpleDateFormat("hh:mm");
    private static SimpleDateFormat markerDdf = new SimpleDateFormat("a");

    private static final int PROFILE_LOADER_ID = 0;
    private static final String[] PROFILE_PROJECTION = {ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY};
    private static final int PROFILE_ID_INDEX = 0;
    private static final int PROFILE_LOOKUP_KEY_INDEX = 1;
    private static final int PROFILE_THUMBNAIL_URI_INDEX = 2;
    private static final int PROFILE_DISPLAY_NAME_PRIMARY_INDEX = 3;

    private static final String SEARCH_CRITERIA = "android.idt.net.com.picup.calling.CallArrayAdapter.SEARCH_CRITERIA";

    public CallArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        inflater = LayoutInflater.from(context);
    }

    public CallArrayAdapter(Context context, int resource, int textViewResourceId, List<Cdr> cdrs) {
        super(context, resource, textViewResourceId, cdrs);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            rowView = inflater.inflate(R.layout.call_row_item, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.thumbnailImageView = (ImageView)rowView.findViewById(R.id.thumbnail_imageview);
            viewHolder.initialTextView = (TextView)rowView.findViewById(R.id.initial_textview);
            viewHolder.callDirectionImageView = (ImageView)rowView.findViewById(R.id.call_direction_imageview);
            viewHolder.callResultImageView = (ImageView)rowView.findViewById(R.id.call_result_imageview);
            viewHolder.recipient1TextView = (TextView)rowView.findViewById(R.id.duration_textview);
            viewHolder.recipient2TextView = (TextView)rowView.findViewById(R.id.department_textview);
            viewHolder.timeTextView = (TextView)rowView.findViewById(R.id.time_textview);
            viewHolder.markerTextView = (TextView)rowView.findViewById(R.id.marker_textview);
            viewHolder.infoIconView = (ImageView) rowView.findViewById(R.id.info_button);
            rowView.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder)rowView.getTag();
        Cdr cdr = super.getItem(position);
        Cursor cursor = null;
        String thumbnailUriString = null;
        String fullName = null;
/*
        if (!TextUtils.isEmpty(cdr.getAni())) {
            holder.thumbnailImageView.setImageURI(null);
            holder.initialTextView.setText("");
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(cdr.getAni()));
            //Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, "5052185500");
            cursor = getContext().getContentResolver().query(uri, PROFILE_PROJECTION, null, null, null);
            if (cursor.moveToFirst()) {
                thumbnailUriString = cursor.getString(PROFILE_THUMBNAIL_URI_INDEX);
                fullName = cursor.getString(PROFILE_DISPLAY_NAME_PRIMARY_INDEX);
            }
            if (!TextUtils.isEmpty(thumbnailUriString)) {
                holder.thumbnailImageView.setImageURI(Uri.parse(thumbnailUriString));
                Drawable thumbnailDrawable = holder.thumbnailImageView.getDrawable();
                RoundedBitmapDrawable roundedBitmapDrawable = PicupImageUtils.toRoundedBitmapDrawable(getContext(), thumbnailDrawable);
                if (roundedBitmapDrawable != null) {
                    holder.thumbnailImageView.setImageDrawable(roundedBitmapDrawable);
                }
                holder.thumbnailImageView.setVisibility(View.VISIBLE);
                //holder.thumbnailImageView.setLayoutParams(new RelativeLayout.LayoutParams(0,0));
                holder.initialTextView.setVisibility(View.INVISIBLE);
            } else if (!TextUtils.isEmpty(fullName)) {
                String[] chunks = fullName.split(" ");
                if (chunks.length >= 1) {
                    if (chunks[0].length() > 0) {
                        holder.initialTextView.append(chunks[0].subSequence(0, 1));
                    }
                }
                if (chunks.length >= 2) {
                    if (chunks[1].length() > 0) {
                        holder.initialTextView.append(chunks[1].subSequence(0, 1));
                    }
                }
                holder.initialTextView.setVisibility(View.VISIBLE);
                holder.thumbnailImageView.setVisibility(View.INVISIBLE);
            } else {
                holder.thumbnailImageView.setImageResource(R.drawable.ic_add_black_24dp);
                //holder.thumbnailImageView.setLayoutParams(new RelativeLayout.LayoutParams(1,1));
                holder.thumbnailImageView.setVisibility(View.VISIBLE);
                holder.initialTextView.setVisibility(View.INVISIBLE);
            }
            if (!TextUtils.isEmpty(fullName)) {
                holder.recipient1TextView.setText(fullName);
            } else {
                holder.recipient1TextView.setText(PhoneNumberUtils.formatNumber(cdr.getAni()));
            }
        }
*/

        if (!TextUtils.isEmpty(cdr.getExtension())) {
            if (TextUtils.equals("Call Answered", cdr.getCallState())) {
                holder.callDirectionImageView.setImageResource(R.drawable.ic_outbound_call);
                if (cdr.getDuration() == 0) {
                    holder.callResultImageView.setImageResource(R.drawable.ic_outbound_cancelled);
                }
            } else if (TextUtils.equals("Not Answered", cdr.getCallState())) {
                holder.callDirectionImageView.setImageResource(R.drawable.ic_missed_call);
            } else if (TextUtils.equals("Voicemail", cdr.getCallState())) {
                holder.callDirectionImageView.setImageResource(R.drawable.ic_voice_mail);
            }
        } else {
            if (TextUtils.equals("Call Answered", cdr.getCallState())) {
                holder.callDirectionImageView.setImageResource(R.drawable.ic_inbound_call);
            } else if (TextUtils.equals("Not Answered", cdr.getCallState())) {
                holder.callDirectionImageView.setImageResource(R.drawable.ic_missed_call);
            } else if (TextUtils.equals("Voicemail", cdr.getCallState())) {
                holder.callDirectionImageView.setImageResource(R.drawable.ic_voice_mail);
            }
        }
        if (TextUtils.equals("Call Answered", cdr.getCallState())) {
            holder.recipient1TextView.setTextColor(ContextCompat.getColor(getContext(), R.color.textColor));
        } else {
            holder.recipient1TextView.setTextColor(ContextCompat.getColor(getContext(), R.color.errorRed));
        }
        //holder.departmentTextView.setText(""); // TBD
        holder.timeTextView.setText(timeSdf.format(cdr.getCallCalendar().getTime()));
        holder.markerTextView.setText(markerDdf.format(cdr.getCallCalendar().getTime()));
        //cursor.close();

        return rowView;
    }

/*
    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public int getViewTypeCount() {
        return super.getCount();
    }
*/

/*
    @Override
    public int getItemViewType(int position) {
        return 1;
    }

*/
    static class ViewHolder {
        public ImageView thumbnailImageView;
        public TextView initialTextView;
        public ImageView callDirectionImageView;
        public ImageView callResultImageView;
        public TextView recipient1TextView;
        public TextView recipient2TextView;
        public TextView timeTextView = null;
        public TextView markerTextView = null;
        public ImageView infoIconView;
}
}

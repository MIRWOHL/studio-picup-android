package com.picup.calling.adapter;

import android.database.DataSetObserver;
import com.picup.calling.data.SectionListItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.picup.calling.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by frank.truong on 4/5/2017.
 */

public class SectionListAdapter extends BaseAdapter implements ListAdapter, AdapterView.OnItemClickListener {
    private static SimpleDateFormat headerSdf = new SimpleDateFormat("MM/dd/yyyy");
    private static SimpleDateFormat timeSdf = new SimpleDateFormat("h:mm");
    private static SimpleDateFormat markerSdf = new SimpleDateFormat("a");

    private final DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            updateSessionCache();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            updateSessionCache();
        };
    };

    private final ListAdapter linkedAdapter;
    private final Map<Integer, Calendar> sectionPositions = new LinkedHashMap<>();
    private final Map<Integer, Integer> itemPositions = new LinkedHashMap<Integer, Integer>();
    private final Map<View, Calendar> currentViewSections = new HashMap<>();
    private int viewTypeCount;
    protected final LayoutInflater inflater;

    private View transparentSectionView;

    private AdapterView.OnItemClickListener linkedListener;

    public SectionListAdapter(final LayoutInflater inflater, final ListAdapter linkedAdapter) {
        this.linkedAdapter = linkedAdapter;
        this.inflater = inflater;
        linkedAdapter.registerDataSetObserver(dataSetObserver);
        updateSessionCache();
    }

    private boolean isTheSame(final Calendar previousSection, final Calendar newSection) {
        if (previousSection == null) {
            return newSection == null;
        } else {
            return previousSection.equals(newSection);
        }
    }

    private synchronized void updateSessionCache() {
        int currentPosition = 0;
        sectionPositions.clear();
        itemPositions.clear();
        viewTypeCount = linkedAdapter.getViewTypeCount() + 1;
        Calendar currentSection = null;
        final int count = linkedAdapter.getCount();
        for (int i = 0; i < count; i++) {
            final SectionListItem item = (SectionListItem) linkedAdapter.getItem(i);
            if (!isTheSame(currentSection, item.section)) {
                sectionPositions.put(currentPosition, item.section);
                currentSection = item.section;
                currentPosition++;
            }
            itemPositions.put(currentPosition, i);
            currentPosition++;
        }
    }

    @Override
    public synchronized int getCount() {
        return sectionPositions.size() + itemPositions.size();
    }

    @Override
    public synchronized Object getItem(final int position) {
        if (isSection(position)) {
            return sectionPositions.get(position);
        } else {
            final int linkedItemPosition = getLinkedPosition(position);
            return linkedAdapter.getItem(linkedItemPosition);
        }
    }

    public synchronized boolean isSection(final int position) {
        return sectionPositions.containsKey(position);
    }

    public synchronized Calendar getSectionName(final int position) {
        if (isSection(position)) {
            return sectionPositions.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(final int position) {
        if (isSection(position)) {
            return sectionPositions.get(position).hashCode();
        } else {
            return linkedAdapter.getItemId(getLinkedPosition(position));
        }
    }

    protected Integer getLinkedPosition(final int position) {
        return itemPositions.get(position);
    }

    @Override
    public int getItemViewType(final int position) {
        if (isSection(position)) {
            return viewTypeCount - 1;
        }
        return linkedAdapter.getItemViewType(getLinkedPosition(position));
    }

    private View getSectionView(final View convertView, final Calendar section) {
        View theView = convertView;
        if (theView == null) {
            theView = createNewSectionView();
        }
        setSectionText(section, theView);
        replaceSectionViewsInMaps(section, theView);
        return theView;
    }

    protected void setSectionText(final Calendar section, final View sectionView) {

        final TextView textView = (TextView) sectionView.findViewById(R.id.header_textview);
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
/*
        final SectionListItem currentItem = items.get(position);
        if (currentItem != null) {
            if (currentItem.item instanceof Cdr) {
                Cdr cdr = (Cdr)currentItem.item;
                final ImageView thumbnailImageView = (ImageView)view.findViewById(R.id.thumbnail_imageview);
                //thumbnailImageView.setImageURI(null); // TBD
                final TextView initialTextView = (TextView)view.findViewById(R.id.initial_textview);
                initialTextView.setText("FT"); // TBD
                final ImageView callDirectionImageView = (ImageView)view.findViewById(R.id.call_direction_imageview);
                callDirectionImageView.setImageResource(R.drawable.ic_call_made_black);
                final ImageView callResultImageView = (ImageView)view.findViewById(R.id.call_result_imageview);
                callResultImageView.setImageResource(TextUtils.equals(cdr.getCallState(), "Call Answered") ? R.drawable.ic_call_received_black : R.drawable.ic_call_missed_black);
                final TextView calleeTextView = (TextView) view.findViewById(R.id.recipient1_textview);
                calleeTextView.setText(cdr.getUserName()); // TBD
                final TextView departmentTextView = (TextView)view.findViewById(R.id.recipient2_textview);
                departmentTextView.setText(cdr.getAccountId()); // TBD
                final TextView timeTextView = (TextView)view.findViewById(R.id.time_textview);
                timeTextView.setText(timeSdf.format(cdr.getCallCalendar()));
                final TextView markerView = (TextView)view.findViewById(R.id.marker_textview);
                markerView.setText(markerSdf.format(cdr.getCallCalendar()));
            }
        }
*/
    }

    protected synchronized void replaceSectionViewsInMaps(final Calendar section, final View theView) {
        if (currentViewSections.containsKey(theView)) {
            currentViewSections.remove(theView);
        }
        currentViewSections.put(theView, section);
    }

    protected View createNewSectionView() {
        return inflater.inflate(R.layout.call_row_item_header, null);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        if (isSection(position)) {
            return getSectionView(convertView, sectionPositions.get(position));
        }
        return linkedAdapter.getView(getLinkedPosition(position), convertView, parent);
    }

    @Override
    public int getViewTypeCount() {
        return viewTypeCount;
    }

    @Override
    public boolean hasStableIds() {
        return linkedAdapter.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
        return linkedAdapter.isEmpty();
    }

    @Override
    public void registerDataSetObserver(final DataSetObserver observer) {
        linkedAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(final DataSetObserver observer) {
        linkedAdapter.unregisterDataSetObserver(observer);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return linkedAdapter.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(final int position) {
        if (isSection(position)) {
            return true;
        }
        return linkedAdapter.isEnabled(getLinkedPosition(position));
    }

    public void makeSectionInvisibleIfFirstInList(final int firstVisibleItem) {
        final Calendar section = getSectionName(firstVisibleItem);
        // only make invisible the first section with that name in case there
        // are more with the same name
        boolean alreadySetFirstSectionIvisible = false;
        for (final Map.Entry<View, Calendar> itemView : currentViewSections.entrySet()) {
            if (itemView.getValue().equals(section) && !alreadySetFirstSectionIvisible) {
                itemView.getKey().setVisibility(View.INVISIBLE);
                alreadySetFirstSectionIvisible = true;
            } else {
                itemView.getKey().setVisibility(View.VISIBLE);
            }
        }
        for (final Map.Entry<Integer, Calendar> entry : sectionPositions.entrySet()) {
            if (entry.getKey() > firstVisibleItem + 1) {
                break;
            }
            setSectionText(entry.getValue(), getTransparentSectionView());
        }
    }

    public synchronized View getTransparentSectionView() {
        if (transparentSectionView == null) {
            transparentSectionView = createNewSectionView();
        }
        return transparentSectionView;
    }

    protected void sectionClicked(final Calendar section) {
        // do nothing
    }

    @Override
    public void onItemClick(final AdapterView< ? > parent, final View view, final int position, final long id) {
        if (isSection(position)) {
            sectionClicked(getSectionName(position));
        } else if (linkedListener != null) {
            linkedListener.onItemClick(parent, view, getLinkedPosition(position), id);
        }
    }

    public void setOnItemClickListener(final AdapterView.OnItemClickListener linkedListener) {
        this.linkedListener = linkedListener;
    }

}

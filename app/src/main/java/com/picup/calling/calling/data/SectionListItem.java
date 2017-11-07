package com.picup.calling.data;

import java.util.Calendar;

/**
 * Created by frank.truong on 4/5/2017.
 */

public class SectionListItem {
    public Calendar section = null;
    public Object item = null;

    public SectionListItem(Calendar section, final Object item) {
        this.section = section;
        this.item = item;
    }

    @Override
    public String toString() {
        return item.toString();
    }
}

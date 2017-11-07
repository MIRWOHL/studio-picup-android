package com.picup.calling.data;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.picup.calling.R;

/**
 * Created by frank.truong on 12/27/2016.
 */

public class FirstClassLinearLayout extends LinearLayout {
    private static ListView owningListView = null;

    public FirstClassLinearLayout(Context context) {
        super(context);
    }

    public FirstClassLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FirstClassLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FirstClassLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (owningListView == null) {
            owningListView = (ListView) super.findViewById(R.id.contacts_listview);
        }
        if (owningListView != null) {
            int position = getId();
            owningListView.setSelection(position);
        }

        return false;
    }
}

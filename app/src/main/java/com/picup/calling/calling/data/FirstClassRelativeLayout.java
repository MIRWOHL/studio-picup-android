package com.picup.calling.data;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.picup.calling.R;
//import android.idt.net.com.picup.calling.R;
/**
 * Created by frank.truong on 12/27/2016.
 */

public class FirstClassRelativeLayout extends RelativeLayout {
    private ListView listView = null;

    public FirstClassRelativeLayout(Context context) {
        super(context);
    }

    public FirstClassRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FirstClassRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FirstClassRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (listView == null) {
            listView = (ListView) super.findViewById(R.id.contacts_listview);
        }
        if (listView != null) {
            int position = getId();
            listView.setSelection(position);
        }
        return false;
    }
}

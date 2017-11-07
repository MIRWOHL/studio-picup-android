package com.picup.calling.base;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by frank.truong on 12/22/2016.
 */

public class PicupViewPager extends ViewPager {

    private PagerAdapter pagerAdapter = null;

    public PicupViewPager(Context context) {
        super(context);
    }

    public PicupViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

/*
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (pagerAdapter != null) {
            super.setAdapter(pagerAdapter);

        }
    }
*/

    /*
     * This logic will make ViewPager wrap its height to WRAP_CONTENT. Otherwise, it will act like FILL_PARENT.
     * Hoever, this method will cause the app to failing to load the contact list!
     */
/*
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = ic_key_0;
        for(int i = ic_key_0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(ic_key_0, MeasureSpec.UNSPECIFIED));
            int h = child.getMeasuredHeight();
            if(h > height) {
                height = h;
            }
        }
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
*/

    /*
        ViewPager not to handle any touch screen motion events
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    /*
        ViewPager not to handle any touch screen motion events
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    /*
    @Override
    public void setAdapter(PagerAdapter adapter) {
        this.pagerAdapter = adapter;
    }

    public void storeAdapter(PagerAdapter pagerAdapter) {
        this.pagerAdapter = pagerAdapter;
    }
*/
}

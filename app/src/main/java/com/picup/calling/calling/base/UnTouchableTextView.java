package com.picup.calling.base;

import android.content.Context;
import com.picup.calling.typefaced.RobotoTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by frank.truong on 12/23/2016.
 */

public class UnTouchableTextView extends RobotoTextView {

    public UnTouchableTextView(Context context) {
        super(context);
    }

    public UnTouchableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UnTouchableTextView(Context context, AttributeSet attrs, int defStype) {
        super(context, attrs, defStype);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}

package com.picup.calling.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by frank.truong on 12/23/2016.
 */

public class UnTouchableImageView extends ImageView {


    public UnTouchableImageView(Context context) {
        super(context);
    }

    public UnTouchableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UnTouchableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public UnTouchableImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}

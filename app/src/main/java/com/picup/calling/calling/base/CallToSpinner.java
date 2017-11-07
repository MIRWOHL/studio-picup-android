package com.picup.calling.base;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.Spinner;

/**
 * Created by frank.truong on 1/25/2017.
 */

public class CallToSpinner extends Spinner {

    private boolean ignoreItemSelectionEvent = true;

    public CallToSpinner(Context context) {
        super(context);
    }

    public CallToSpinner(Context context, int mode) {
        super(context, mode);
    }

    public CallToSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CallToSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CallToSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
    }

    public CallToSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode) {
        super(context, attrs, defStyleAttr, defStyleRes, mode);
    }

    public CallToSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode, Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, defStyleRes, mode, popupTheme);
    }

    public void setSelection(int position, int ignoreItemSelectionEventFlag) {
        if (ignoreItemSelectionEventFlag != 0) {
            if (super.getOnItemSelectedListener() != null) {
                OnItemSelectedListener onItemSelectedListener = super.getOnItemSelectedListener();
                super.setOnItemSelectedListener(null);
                super.setSelection(position);
                super.setOnItemSelectedListener(onItemSelectedListener);
                return;
            }
        }
        super.setSelection(position);
    }

    @Override
    public void setSelection(int position) {
        this.setSelection(position, 1);
    }

    public void setIgnoreItemSelectionEvent(boolean state) {
        this.ignoreItemSelectionEvent = state;
    }

    public boolean isIgnoreItemSelectionEvent() {
        return ignoreItemSelectionEvent;
    }
}




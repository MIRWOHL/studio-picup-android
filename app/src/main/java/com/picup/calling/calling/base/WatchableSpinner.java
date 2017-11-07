package com.picup.calling.base;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

/**
 * Created by frank.truong on 1/11/2017.
 */

public class WatchableSpinner extends Spinner implements AdapterView.OnItemSelectedListener {

    public WatchableSpinner(Context context) {
        super(context);
        super.setOnItemSelectedListener(this);
    }

    public WatchableSpinner(Context context, int mode) {
        super(context, mode);
        super.setOnItemSelectedListener(this);
    }

    public WatchableSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setOnItemSelectedListener(this);
    }

    public WatchableSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setOnItemSelectedListener(this);
    }

    public WatchableSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
        super.setOnItemSelectedListener(this);
    }

    public WatchableSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode) {
        super(context, attrs, defStyleAttr, defStyleRes, mode);
        super.setOnItemSelectedListener(this);
    }

    public WatchableSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode, Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, defStyleRes, mode, popupTheme);
        super.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d("WatchableSpinner", "XXX");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

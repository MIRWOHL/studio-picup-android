package com.picup.calling.base;

import android.view.View;

/**
 * Created by frank.truong on 1/10/2017.
 */

public abstract class ViewHolder {
    public final View rootView;

    public ViewHolder(View rootView) {
        this.rootView = rootView;
    }

    protected final <T extends View> T findWidgetById(int resId) {
        return (T) rootView.findViewById(resId);
    }
 }

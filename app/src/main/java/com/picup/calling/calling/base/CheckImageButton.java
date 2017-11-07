/*
 * Copyright (c) 2015
 *
 * IDT Corporation. All rights reserved.
 *
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY
 * KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 */
/**
 * @author ychang
 */
package com.picup.calling.base;

import android.content.Context;
import android.content.res.TypedArray;
import com.picup.calling.listener.OnSingleClickListener;
import android.os.Handler;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.View;

import com.picup.calling.R;

/** Image Button with checked state implemented*/
public final class CheckImageButton extends AppCompatImageButton {
    /** Value - additional state*/
    private final int additional_spaces = 1;
    /** Value - internal flag*/
    private boolean mIsChecked = false;
    /** Value - enable check change*/
    private boolean mEnableChecked = true;
    /** Value - Custom State being added*/
    private final int[] CUSTOM_STATE = {android.R.attr.state_checked};
    /** Click listener - set by Function setOnClickListener*/
    private OnClickListener inputOnClickListener;
    /** CheckChange Listener - set by function setOnCheckChangeListener*/
    private OnCheckedChangeListener inputOnCheckChangeListener;
    /** Value - isDetched*/
    private boolean isDetached = false;

    /** Constructor*/
    public CheckImageButton(Context context) {
        super(context);
        init(null);
    }

    /** Constructor*/
    public CheckImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    /** Constructor*/
    public CheckImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    /**Method to initialize variable and retrieve attributeSet*/
    private void init(AttributeSet attrs) {
        //get Attributes;
        Context context = getContext();
        if (attrs != null && context != null) {
            //BaseView attr
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BaseCheckView);
            if (typedArray != null) {
                mIsChecked = typedArray.getBoolean(R.styleable.BaseCheckView_buttonChecked, false);
                mEnableChecked = typedArray.getBoolean(R.styleable.BaseCheckView_enableButtonChecked, true);
                typedArray.recycle();
            }
        }
        super.setOnClickListener(internalOnClickListener);
    }

    /** {@inheritDoc}*/
    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        //extraSpace adding amount of additional custom drawable state
        final int[] drawableState = super.onCreateDrawableState(extraSpace + additional_spaces);
        if (mIsChecked) {
            mergeDrawableStates(drawableState, CUSTOM_STATE);
        }
        return drawableState;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //Logger.log("CheckImageButton - onAttachedToWindow", Logger.LOG_VERBOSE);
        isDetached = false;
    }

    /** {@inheritDoc}*/
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //Logger.log("CheckImageButton - onDetachedFromWindow", Logger.LOG_VERBOSE);
        // clean variables
        isDetached = true;
        //inputOnClickListener = null;
    }

    /** Method - to retrieve button state - isChecked */
    public boolean isChecked() {
        return mIsChecked;
    }

    /** Method - to set button state - isChecked */
    public void setChecked(boolean newState) {
        setCheckValueOnThread(newState, true);
    }

    /** Method - to set button state - WITHOUT NOTIFY ONCHECKEDCHANGELISTENER*/
    public void setCheckWithoutNotify(boolean newState) {
        setCheckValueOnThread(newState, false);
    }

    /** Method - to set OnCheckedChangedListener*/
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.inputOnCheckChangeListener = listener;
    }

    /** {@inheritDoc}*/
    @Override
    public void setOnClickListener(OnClickListener l) {
        this.inputOnClickListener = l;
    }

    /** Method - internal use - to set checked state - on UI Thread*/
    private synchronized void setCheckValueOnThread(final boolean newValue, final boolean notify) {
        if (isDetached) {
            return;
        }
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                setCheckValue(newValue, notify);
            }
        });
    }

    /** Method - internal use - to set checked state - please run in UI thread so drawable can refresh*/
    private void setCheckValue(boolean newValue, boolean notify) {
        if (mIsChecked != newValue) {
            mIsChecked = newValue;
            if (mEnableChecked && inputOnCheckChangeListener != null && notify) {
                inputOnCheckChangeListener.onCheckedChanged(this, newValue);
            }
            refreshDrawableState();
        }
    }

    /** internal use onclick listener to update drawable state and notify External set onClickListener*/
    private OnSingleClickListener internalOnClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            if (v == null || isDetached) {
                return;
            }
            if (mEnableChecked) {
                // onClick() should be already in UI Thread
                setCheckValue(!mIsChecked, true);
                //refreshDrawableState();
            }
            if (inputOnClickListener != null) {
                inputOnClickListener.onClick(v);
            }
        }
    };

    /** OnCheckChangeListener Interface for CheckImageButton*/
    public interface OnCheckedChangeListener {
        void onCheckedChanged(CheckImageButton view, boolean isChecked);
    }
}

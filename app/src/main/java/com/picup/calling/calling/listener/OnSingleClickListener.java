package com.picup.calling.listener;

import android.view.View;
import android.os.SystemClock;

/**
 * Single click listener is to provide some time interval to prevent double or triple click issue
 */
public abstract class OnSingleClickListener implements View.OnClickListener {
	/**
	 * Minimum time interval 
	 */
	private static final long MIN_CLICK_INTERVAL=200;

	/** Desire Interval*/
	private static long DES_CLICK_INTERVAL = -1;
	/**
	 * last click time stamp
	 */
	private long mLastClickTime;

	/** Constructor */
	public OnSingleClickListener(){
	}
	
	/** Constructor with overwrite interval*/
	public OnSingleClickListener(int INTERVAL){
		DES_CLICK_INTERVAL = INTERVAL;
	}

	/**
	 * abstract class for implement to define
	 * @param v The view that was clicked.
	 */
	public abstract void onSingleClick(View v);

	@Override
	public final void onClick(View v) {
		long currentClickTime=SystemClock.uptimeMillis();
		long elapsedTime=currentClickTime-mLastClickTime;
		mLastClickTime=currentClickTime;

		// check desire interval first then minimum click interval
		if (DES_CLICK_INTERVAL==-1){
			if (elapsedTime<=MIN_CLICK_INTERVAL) {
				return;
			}
		} else {
			if (elapsedTime<=DES_CLICK_INTERVAL) {
				return;
			}
		}

		onSingleClick(v);        
	}

}
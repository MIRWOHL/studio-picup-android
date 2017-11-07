package com.picup.calling;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by frank.truong on 1/16/2017.
 */

public class SwipeableListView extends ListView implements AdapterView.OnItemClickListener {
    private static final String TAG = SwipeableListView.class.getSimpleName();
    private GestureDetectorCompat gestureDetectorCompat = null;

    public SwipeableListView(Context context) {
        super(context);
        initGestureDetector();
        setOnItemClickListener(this);
    }

    public SwipeableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initGestureDetector();
        setOnItemClickListener(this);
    }

    public SwipeableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initGestureDetector();
        setOnItemClickListener(this);
    }

    public SwipeableListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initGestureDetector();
        setOnItemClickListener(this);
    }

    private void initGestureDetector() {
        this.gestureDetectorCompat = new GestureDetectorCompat(getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }
            @Override
            public void onShowPress(MotionEvent e) {

            }
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }
            @Override
            public void onLongPress(MotionEvent e) {
            }
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                final int SWIPE_MIN_DISTANCE = 120;
                final int SWIPE_MAX_OFF_PATH = 250;
                final int SWIPE_THRESHOLD_VELOCITY = 200;
                if ((e1 != null && e2 != null)) {
                    if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                        return false;
                    }
                    if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) { // right to left
                        //contactCallLayout.setVisibility(View.GONE);
                    } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) { // left to right
                        //contactCallLayout.setVisibility(View.VISIBLE);
                    }
                }
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetectorCompat.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick");
    }
}

package com.picup.calling;

import android.view.View;

/**
 * Created by frank.truong on 12/5/2016.
 */

public class SystemUiVisibilityChangeListener implements View.OnSystemUiVisibilityChangeListener {
    private static View decorView = null;
    private static SystemUiVisibilityChangeListener instance = null;

    public static SystemUiVisibilityChangeListener getInstance(View decorView) {
        if (instance == null) {
            instance = new SystemUiVisibilityChangeListener(decorView);
        }
        return instance;
    }

    private SystemUiVisibilityChangeListener(View decorView) {
        this.decorView = decorView;
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}

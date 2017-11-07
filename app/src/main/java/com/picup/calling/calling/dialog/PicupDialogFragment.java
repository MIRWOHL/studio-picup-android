package com.picup.calling.dialog;


import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class PicupDialogFragment extends DialogFragment {

    public PicupDialogFragment() {
        // Required empty public constructor
        super();
    }

    @Override
    public void onResume() {
        super.onResume();
        final View decorView = getDialog().getWindow().getDecorView();
        //decorView.setOnSystemUiVisibilityChangeListener(SystemUiVisibilityChangeListener.getInstance(decorView));
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void dimmerBy(float dimAmount) {
        if (getDialog() != null) {
            Window dialogWindow = getDialog().getWindow();
            WindowManager.LayoutParams layoutParams = dialogWindow.getAttributes();
            layoutParams.dimAmount = dimAmount;
            dialogWindow.setAttributes(layoutParams);
            dialogWindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }

}

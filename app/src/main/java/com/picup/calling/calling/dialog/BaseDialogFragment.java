package com.picup.calling.dialog;

import android.app.Activity;
import com.picup.calling.util.Logger;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

/**
 * Created by ychang on 5/3/17.
 */
public class BaseDialogFragment extends DialogFragment {

    protected boolean isOnSaveInstance = false;

    private OnDetachListener detachListener;
    private boolean detachListenerActivated = false;
    Bundle detachData = null;

    /**
     * Internal Use Value - override by child - send request back to DetachListener
     */
    protected int detachRequestCode = 0;
    /**
     * Internal Use Value - override by child - send result back to DetachListener
     */
    protected int detachResultCode = Activity.RESULT_CANCELED;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isOnSaveInstance = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isOnSaveInstance = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        isOnSaveInstance = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (detachListener != null && !detachListenerActivated) {
            detachListenerActivated = true;
            detachListener.onDetach(detachRequestCode, detachResultCode, detachData);
        }
    }

    public interface OnDetachListener {
        void onDetach(int requestCode, int resultCode, Bundle data);
    }

    public void setOnDetachListener(OnDetachListener listener) {
        setOnDetachListener(0, listener);
    }

    public void setOnDetachListener(int requestCode, OnDetachListener listener) {
        this.detachRequestCode = requestCode;
        this.detachListener = listener;
    }

    private synchronized void notifyOnDetached() {
        if (detachListener == null || detachListenerActivated) {
            return;
        }
        detachListenerActivated = true;
        detachListener.onDetach(detachRequestCode, detachResultCode, detachData);
    }

    /**
     * Method to dismiss dialog, it does state check
     */
    synchronized void remove() {
        String log = "BaseDialogFragment - remove";
        log += " - isOnSaveInstance:";
        log += isOnSaveInstance;
        if (isOnSaveInstance) {
            try {
                dismissWithoutState();
            } catch(Exception e) {
                log += " - dismissWithoutState";
                Logger.log(log);
                Logger.logThrowable(e);
            }
        } else {
            try {
                dismiss();
            } catch (IllegalStateException e) {
                log += " - dismiss";
                Logger.log(log);
                Logger.logThrowable(e);
                dismissWithoutState();
            }
        }
        notifyOnDetached();
    }

    private void dismissWithoutState() {
        String log = "BaseActivity - dismissWithoutState";
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager == null || fragmentManager.isDestroyed()) {
            log += " - invalid fragmentManager";
            Logger.log(log);
            return;
        }
        try {
            dismissAllowingStateLoss();
        } catch (Throwable t) {
            log += " - Throwable";
            Logger.log(log);
            Logger.logThrowable(t);
        }
    }
}

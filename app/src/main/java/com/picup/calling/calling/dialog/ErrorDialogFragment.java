package com.picup.calling.dialog;

import android.content.Context;
import com.picup.calling.util.Logger;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.picup.calling.R;

public class ErrorDialogFragment extends DialogFragment implements View.OnClickListener {
    private static final String ARG_ERROR_TYPE = "android.idt.net.com.picup.calling.ErrorDialogFragment.ERROR_TYPE";
    private int errorType;
    private Button cancelButton = null;
    private Button loginButton = null;

    private OnErrorDialogFragmentListener listener;

    public ErrorDialogFragment() {
        // Required empty public constructor
    }

    public static ErrorDialogFragment newInstance(int errorType) {
        ErrorDialogFragment fragment = new ErrorDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ERROR_TYPE, errorType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnErrorDialogFragmentListener) {
            listener = (OnErrorDialogFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnErrorDialogFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            errorType = getArguments().getInt(ARG_ERROR_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View errorView = inflater.inflate(R.layout.error_dialog, container, false);
        ImageView errorIconImageView = (ImageView)errorView.findViewById(R.id.error_icon_imageview);
        TextView errorTitleTextView = (TextView)errorView.findViewById(R.id.error_title_textview);
        TextView errorDescriptionTextView = (TextView)errorView.findViewById(R.id.error_description_textview);
        cancelButton = (Button)errorView.findViewById(R.id.cancel_button);
        loginButton = (Button)errorView.findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);

        if (errorIconImageView != null && errorTitleTextView != null && errorDescriptionTextView != null && cancelButton != null && loginButton != null) {
            Logger.log("ErrorDialogFragment - onCreateView - errorType:"+errorType);
            switch (errorType) {
                case 0:
                    errorIconImageView.setImageResource(R.drawable.ic_oops);
                    errorTitleTextView.setText(R.string.oops_error_title);
                    break;
                case 1:
                    errorIconImageView.setImageResource(R.drawable.ic_network);
                    errorTitleTextView.setText(R.string.network_error_title);
                    errorDescriptionTextView.setText(R.string.network_error_message);
                    loginButton.setText(R.string.ok_cap_label);
                    break;
                case 2:
                    errorIconImageView.setImageResource(R.drawable.ic_oops);
                    errorTitleTextView.setText(R.string.account_suspended_error_title);
                    errorDescriptionTextView.setText(R.string.account_suspended_error_message);
                    loginButton.setText(R.string.reactivate_button);
                    cancelButton.setVisibility(View.VISIBLE);
                    cancelButton.setOnClickListener(this);
                    break;
                case 3:
                    errorIconImageView.setImageResource(R.drawable.ic_outbound_phone);
                    errorTitleTextView.setText(R.string.outbound_unavailable_title);
                    errorDescriptionTextView.setText(R.string.outbound_unavailable_message);
                    loginButton.setText(R.string.ok_cap_label);
                    break;

            }
        }

        return errorView;
    }

    @Override
    public void onClick(View v) {
        Logger.log("ErrorDialogFragment - onClick - errorType:"+errorType);

        if (v.getId() == loginButton.getId()) {
            switch(errorType) {
                case 2:
                    //open web browser to com.picup.calling
                    listener.appSettings();
                    dismissAllowingStateLoss();
                    break;
                case 1:
                    dismissAllowingStateLoss();
                    break;
                default:
                    listener.reAuthenticate();
            }
        } else if (v.getId() == cancelButton.getId()) {
            dismissAllowingStateLoss();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnErrorDialogFragmentListener {
        void reAuthenticate();

        void appSettings();
    }
}

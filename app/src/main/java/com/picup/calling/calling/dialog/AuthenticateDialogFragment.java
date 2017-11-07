package com.picup.calling.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;

import com.picup.calling.base.PicupActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.picup.calling.R;

public class AuthenticateDialogFragment extends PicupDialogFragment {
    private static final String TAG = AuthenticateDialogFragment.class.getSimpleName();
    private static final String ARG_USERNAME = "com.picup.calling.dialog.AuthenticateDialogFragment.USERNAME";
    private static final String ARG_PASSWORD = "com.picup.calling.dialog.AuthenticateDialogFragment.PASSWORD";
    private static final String ARG_GUI = "com.picup.calling.dialog.AuthenticateDialogFragment.GUI";

    private static String username = null;
    private static String password = null;
    private static boolean gui = true;

    private ImageView cupImageView = null;
    private static TextView messageTextView = null;

    public AuthenticateDialogFragment() {
        super();
        super.setCancelable(false);
    }

    public static AuthenticateDialogFragment newInstance() {
        AuthenticateDialogFragment fragment = new AuthenticateDialogFragment();
        return fragment;
    }

    public static AuthenticateDialogFragment newInstance(String emailAddress, String password, boolean gui) {
        AuthenticateDialogFragment fragment = new AuthenticateDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, emailAddress);
        args.putString(ARG_PASSWORD, password);
        args.putBoolean(ARG_GUI, gui);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString(ARG_USERNAME);
            password = getArguments().getString(ARG_PASSWORD);
            gui = getArguments().getBoolean(ARG_GUI);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        if (gui) {
            view = inflater.inflate(R.layout.cup_filling_dialog, container, false);
            cupImageView = (ImageView) view.findViewById(R.id.cup_imageview);
            cupImageView.setBackgroundResource(R.drawable.cup_filling);
            messageTextView = (TextView) view.findViewById(R.id.message_textview);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (gui) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // dimmer background activity
            dimmerBy(0.8f);
        }
        // set background activity untouchable
        ((PicupActivity) getActivity()).setTouchable(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (gui) {
            ((AnimationDrawable) cupImageView.getBackground()).start();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        // set background activity back to touchable
        ((PicupActivity)getActivity()).setTouchable(true);
        if (gui) {
            // set background activity's opacity back to normal
            dimmerBy(0.0f);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (gui) {
            ((AnimationDrawable) cupImageView.getBackground()).stop();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}

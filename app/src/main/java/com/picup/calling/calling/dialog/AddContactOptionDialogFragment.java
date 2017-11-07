package com.picup.calling.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.Window;
import android.view.WindowManager;

import com.picup.calling.R;

public class AddContactOptionDialogFragment extends DialogFragment {
    private static final String TAG = AddContactOptionDialogFragment.class.getSimpleName();
    private static final String ARG_PHONE_NUMBER = "android.idt.net.com.picup.calling.dialog.AddContactOptionDialogFragment.PHONE_NUMBER";
    private static final String ARG_ANCHOR_X = "android.idt.net.com.picup.calling.dialog.AddContactOptionDialogFragment.anchorX";
    private static final String ARG_ANCHOR_Y = "android.idt.net.com.picup.calling.dialog.AddContactOptionDialogFragment.anchorY";

    private String phoneNumber = null;
    private float anchorX = 0f;
    private float anchorY = 0f;

    private OnAddContactOptionDialogFragmentListener listener;

    public AddContactOptionDialogFragment() {
        // Required empty public constructor
    }

    public static AddContactOptionDialogFragment newInstance(String phoneNumber) {
        AddContactOptionDialogFragment fragment = new AddContactOptionDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHONE_NUMBER, phoneNumber);
        fragment.setArguments(args);

        return fragment;
    }

    public static AddContactOptionDialogFragment newInstance(float anchorX, float anchorY, String phoneNumber) {
        AddContactOptionDialogFragment fragment = new AddContactOptionDialogFragment();
        Bundle args = new Bundle();
        args.putFloat(ARG_ANCHOR_X, anchorX);
        args.putFloat(ARG_ANCHOR_Y, anchorY);
        args.putString(ARG_PHONE_NUMBER, phoneNumber);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAddContactOptionDialogFragmentListener) {
            listener = (OnAddContactOptionDialogFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnAddContactDialogFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            anchorX = getArguments().getFloat(ARG_ANCHOR_X);
            anchorY = getArguments().getFloat(ARG_ANCHOR_Y);
            phoneNumber = getArguments().getString(ARG_PHONE_NUMBER);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            //builder.setItems(new CharSequence[]{"Update Existing", "Create Contact"}, new DialogInterface.OnClickListener() {
                              builder.setItems(new CharSequence[]{activity.getString(R.string.dialog_menu_create_contact), activity.getString(R.string.dialog_menu_update_existing)}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            listener.createContact(phoneNumber);
                                            dialog.dismiss();
                                            break;

                                        case 1:
                                            listener.updateExisting(phoneNumber);
                                            dialog.dismiss();
                                            break;

                                        default:
                                    }
                                }
                            });
        Dialog dialog = builder.create();
        super.setCancelable(true);
        //dialog.setCanceledOnTouchOutside(true);

        Window window = dialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        //WindowManager.LayoutParams newLayoutParams = new WindowManager.LayoutParams((int)anchorX, (int)anchorY);
        layoutParams.x = (int)anchorX;
        layoutParams.y = (int)anchorY;
        layoutParams.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        //window.setAttributes(layoutParams);
        window.setAttributes(layoutParams);

        return dialog;
    }

/*
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.additional_info_dialog, container, false);
    }
*/

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnAddContactOptionDialogFragmentListener {
        void updateExisting(String phoneNumber);
        void createContact(String phoneNumber);
    }
}

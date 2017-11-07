package com.picup.calling.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.picup.calling.R;

public class AdditionalInfoDialogFragment extends DialogFragment {
    private static final String ARG_ANCHOR_X = "anchorX";
    private static final String ARG_ANCHOR_Y = "anchorY";

    // TODO: Rename and change types of parameters
    private float anchorX = 0f;
    private float anchorY = 0f;

    private AdditionalInfoDialogFragmentListener listener;

    public AdditionalInfoDialogFragment() {
        // Required empty public constructor
    }

    public static AdditionalInfoDialogFragment newInstance(float anchorX, float anchorY) {
        AdditionalInfoDialogFragment fragment = new AdditionalInfoDialogFragment();
        Bundle args = new Bundle();
        args.putFloat(ARG_ANCHOR_X, anchorX);
        args.putFloat(ARG_ANCHOR_Y, anchorY);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            anchorX = getArguments().getFloat(ARG_ANCHOR_X);
            anchorY = getArguments().getFloat(ARG_ANCHOR_Y);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setItems(new CharSequence[]{"Email", "Company Name"}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            listener.additionalInfoPicked(0);
                                            dialog.dismiss();
                                            break;
                                        case 1:
                                            listener.additionalInfoPicked(1);
                                            dismiss();
                                            break;
                                        default:
                                    }
                                }
                            });
        Dialog dialog = builder.create();
        Window window = dialog.getWindow();
        window.setGravity(Gravity.TOP | Gravity.LEFT);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.x = (int)anchorX;
        layoutParams.y = (int)anchorY - 200;
        layoutParams.dimAmount = 0.0f;
        //layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setBackgroundDrawableResource(R.color.lightGray);
        //window.setAttributes(layoutParams);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AdditionalInfoDialogFragmentListener) {
            listener = (AdditionalInfoDialogFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement AddContactOptionsDialogFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface AdditionalInfoDialogFragmentListener {
        void additionalInfoPicked(int additionalInfoType);
    }
}

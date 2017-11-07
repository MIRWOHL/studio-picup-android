package com.picup.calling.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.picup.calling.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PhotoRetrievalOptionDialogFragmentListener} interface
 * to handle interaction events.
 * Use the {@link PhotoRetrievalOptionDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhotoRetrievalOptionDialogFragment extends DialogFragment implements View.OnClickListener {
    private static final String TAG = PhotoRetrievalOptionDialogFragment.class.getSimpleName();
    private static final int EXISTING_PHOTO_OPTION = 1;
    private static final int NEW_PHOTO_OPTION = 2;
    private Button retrieveExistingPhotoButton = null;
    private Button createNewPhotoButton = null;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;



    private PhotoRetrievalOptionDialogFragmentListener listener;

    public PhotoRetrievalOptionDialogFragment() {
        // Required empty public constructor
    }

    public static PhotoRetrievalOptionDialogFragment newInstance() {
        PhotoRetrievalOptionDialogFragment fragment = new PhotoRetrievalOptionDialogFragment();
        return fragment;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PhotoRetrievalOptionDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PhotoRetrievalOptionDialogFragment newInstance(String param1, String param2) {
        PhotoRetrievalOptionDialogFragment fragment = new PhotoRetrievalOptionDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.get_photo_option_layout, container, false);
        retrieveExistingPhotoButton = (Button)view.findViewById(R.id.retrieve_existing_photo_button);
        retrieveExistingPhotoButton.setOnClickListener(this);
        createNewPhotoButton = (Button)view.findViewById(R.id.create_new_photo_button);
        createNewPhotoButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PhotoRetrievalOptionDialogFragmentListener) {
            listener = (PhotoRetrievalOptionDialogFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PhotoRetrievalOptionDialogFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == retrieveExistingPhotoButton.getId()) {
            Log.d(TAG, "Retrieve existing photo");
            listener.photoOptionPicked(1);
            super.dismiss();
            // launch Gallery app
        } else if (v.getId() == createNewPhotoButton.getId()) {
            Log.d(TAG, "Create new photo");
            listener.photoOptionPicked(2);
            super.dismiss();
            // launch Camera app
        }
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
    public interface PhotoRetrievalOptionDialogFragmentListener {
        void photoOptionPicked(int photoOption);
    }
}

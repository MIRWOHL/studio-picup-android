package com.picup.calling.base;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.picup.calling.R;

import java.util.List;

/**
 * Created by frank.truong on 12/12/2016.
 */

public class PicupToolbar extends Toolbar implements AdapterView.OnItemSelectedListener {
    private static final String TAG = PicupToolbar.class.getSimpleName();
    private Spinner callFromTypeSpinner = null;
    private SpinnerAdapter adapter = null;

    public PicupToolbar(Context context) {
        super(context);
        initView();
    }

    public PicupToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PicupToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View toolbarContentView = layoutInflater.inflate(R.layout.toolbar_content, null);
        addView(toolbarContentView);
        callFromTypeSpinner = (Spinner)findViewById(R.id.call_from_type_spinner);
        callFromTypeSpinner.setOnItemSelectedListener(this);
    }

    public void setPhoneNumbers(List<String> phoneNumbers) {
        if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
            adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, phoneNumbers);
            callFromTypeSpinner.setAdapter(adapter);
        }

    }
    public String getSelectedPhoneNumber() {
        String selectedPhoneNumber = null;

        if (!adapter.isEmpty()) {
            selectedPhoneNumber = (String)adapter.getItem(callFromTypeSpinner.getSelectedItemPosition());
        }
        return selectedPhoneNumber;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        Log.d(TAG, (String)callFromTypeSpinner.getSelectedItem());
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}

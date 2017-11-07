package com.picup.calling;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import com.picup.calling.base.PicupActivity;
import com.picup.calling.dialog.BaseDialogFragment;
import com.picup.calling.helper.AddressBookHelper;
import com.picup.calling.util.Logger;
import com.picup.calling.util.PicupImageUtils;
import com.picup.calling.util.Utililites;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.picup.calling.R;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public final class DeleteActivity extends PicupActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener,
        TextWatcher, CompoundButton.OnCheckedChangeListener {
    private static final String SEARCH_CRITERIA = "android.idt.net.com.picup.calling.DeleteActivity.SEARCH_CRITERIA";

    private static final String TAG = DeleteActivity.class.getSimpleName();
    private CheckBox selectAllCheckbox = null;
    private TextView selectionCountTextView = null;
    private Button doneButton = null;
    private EditText searchEditText = null;
    private ImageView magnifierImageView = null;
    private TextView hintTextView = null;
    private ImageView clearImageView = null;

    private ListView deleteListView = null;
    private static DeleteCursorAdapter deleteCursorAdapter = null;

    private static final int CONTACTS_LOADER_ID = 0;
    private static final String[] CONTACTS_PROJECTION = {ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY};

    private DeleteFragment deleteFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_layout);
        selectAllCheckbox = (CheckBox) findViewById(R.id.select_all_checkbox);
        selectionCountTextView = (TextView) findViewById(R.id.selection_count_textview);
        doneButton = (Button) findViewById(R.id.done_button);
        if (doneButton != null) {
            doneButton.setOnClickListener(this);
        }
        searchEditText = (EditText) findViewById(R.id.search_edittext);
        magnifierImageView = (ImageView) findViewById(R.id.magnifier_imageview);
        hintTextView = (TextView) findViewById(R.id.hint_textview);
        clearImageView = (ImageView) findViewById(R.id.clear_imageview);
        if (clearImageView != null) {
            clearImageView.setOnClickListener(this);
            clearImageView.setVisibility(View.INVISIBLE);
        }

        deleteListView = (ListView) findViewById(R.id.delete_listview);
        deleteCursorAdapter = new DeleteCursorAdapter(this, null, 0);
        if (deleteListView != null) {
            deleteListView.setAdapter(deleteCursorAdapter);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            LoaderManager loaderManager = getSupportLoaderManager();
            if (loaderManager != null) {
                loaderManager.initLoader(CONTACTS_LOADER_ID, null, this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selectAllCheckbox != null) {
            selectAllCheckbox.setOnCheckedChangeListener(this);
        }
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (selectAllCheckbox != null) {
            selectAllCheckbox.setOnCheckedChangeListener(null);
        }
        if (searchEditText != null) {
            searchEditText.removeTextChangedListener(this);
        }
        if (isFinishing()) {
            if (deleteFragment != null) {
                try {
                    deleteFragment.dismiss();
                    deleteFragment = null;
                } catch (Throwable t) {
                }
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = null;

        String selection = "";
        String picupContact = AddressBookHelper.findPicupContactID(this);
        ArrayList<String> selectionArray = new ArrayList<>();
        if (!TextUtils.isEmpty(picupContact)) {
            selection += ContactsContract.Contacts._ID + " != ?";
            selectionArray.add(picupContact);
        }
        String orderBy = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " COLLATE LOCALIZED ASC";
        if (id == CONTACTS_LOADER_ID) {
            if (args != null) {
                String criteria = args.getString(SEARCH_CRITERIA);
                if (!TextUtils.isEmpty(criteria)) {
                    if (!TextUtils.isEmpty(selection)) {
                        selection += " AND ";
                    }
                    selection += ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ? ";
                    selectionArray.add("%" + criteria + "%");
                }
            }
            String[] selectionArgs = Utililites.arrayList2array(selectionArray);
            cursorLoader = new CursorLoader(this, ContactsContract.Contacts.CONTENT_URI, CONTACTS_PROJECTION, selection, selectionArgs, orderBy);
        } else {
            // handle more loaders here
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == CONTACTS_LOADER_ID) {
            deleteCursorAdapter.swapCursor(data);
        } else {
            // handle more loaders here.
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == CONTACTS_LOADER_ID) {
            deleteCursorAdapter.swapCursor(null);
        } else {
            // handle more loaders here.
        }
    }

    @Override
    public void onClick(View v) {
        String log = "DelectActivity - onClick";
        if (v == null) {
            log += " - invalid args";
            Logger.log(log);
            return;
        }
        int id = v.getId();
        if (id == R.id.done_button) {
            log += " - done";
            Logger.log(log);
            showDeleteDialog();
        } else if (v.getId() == R.id.clear_imageview) {
            if (searchEditText != null) {
                searchEditText.setText("");
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        Bundle bundle = null;
        if (s == null) {
            return;
        }
        String criteria = s.toString();
        boolean empty = (criteria.length() > 0 ? false : true);
        if (magnifierImageView != null) {
            magnifierImageView.setVisibility(empty ? View.VISIBLE : View.INVISIBLE);
        }
        if (hintTextView != null) {
            hintTextView.setVisibility(empty ? View.VISIBLE : View.INVISIBLE);
        }
        if (clearImageView != null) {
            clearImageView.setVisibility(empty ? View.INVISIBLE : View.VISIBLE);
        }
        if (!TextUtils.isEmpty(criteria)) {
            bundle = new Bundle();
            bundle.putString(SEARCH_CRITERIA, criteria);
        }
        restartLoader(CONTACTS_LOADER_ID, bundle, this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == null) {
            return;
        }
        int id = buttonView.getId();
        if (id == R.id.select_all_checkbox) {
            if (deleteCursorAdapter == null) {
                return;
            }
            deleteCursorAdapter.setSelectAll(isChecked);
        }
    }

    private synchronized void showDeleteDialog() {
        String log = "DelectActivity - showDeleteDialog";
        if (deleteFragment != null) {
            log += " - frag exist";
            Logger.log(log);
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager == null || fragmentManager.isDestroyed()) {
            log += " - fragment is invalid";
            Logger.log(log);
            return;
        }
        Hashtable<String, Integer> selectTable = null;
        if (deleteCursorAdapter != null) {
            selectTable = deleteCursorAdapter.getSelectData();
        }
        if (selectTable == null || selectTable.isEmpty()) {
            log += " - selectTable is empty or null";
            Logger.log(log);
            return;
        }
        DeleteFragment newFrag = new DeleteFragment();
        Bundle args = new Bundle();
        args.putSerializable(DeleteFragment.ArgsTag, selectTable);
        newFrag.setArguments(args);
        newFrag.setOnDetachListener(new BaseDialogFragment.OnDetachListener() {
            @Override
            public void onDetach(int requestCode, int resultCode, Bundle data) {
                deleteFragment = null;
            }
        });
        try {
            newFrag.show(fragmentManager, "deleteFragment");
            this.deleteFragment = newFrag;
        } catch (Throwable t) {
        }
    }

    private synchronized void refreshCount() {
        if (deleteCursorAdapter == null || selectionCountTextView == null || doneButton == null) {
            return;
        }
        int count = deleteCursorAdapter.getDeleteSelectCount();
        selectionCountTextView.setText(String.valueOf(count));
        doneButton.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);
    }

    private class DeleteCursorAdapter extends CursorAdapter {
        private Hashtable<String, Integer> selectTable = new Hashtable<>();
        private boolean selectAll = false;

        DeleteCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater layoutInflater = null;
            if (context != null) {
                layoutInflater = LayoutInflater.from(context);
            }
            View view = null;
            if (layoutInflater != null) {
                try {
                    view = layoutInflater.inflate(R.layout.delete_contact_row_item, parent, false);
                } catch (Throwable t) {
                }
            }
            if (view != null) {
                ContactHolder contactHolder = new ContactHolder();
                contactHolder.deleteCheckBox = (CheckBox) view.findViewById(R.id.delete_checkbox);
                contactHolder.photoThumbnailImageView = (ImageView) view.findViewById(R.id.thumbnail_imageview);
                contactHolder.initialTextView = (TextView) view.findViewById(R.id.initial_textview);
                contactHolder.fullNameTextView = (TextView) view.findViewById(R.id.duration_textview);
                view.setTag(R.id.base_view_tag_id, contactHolder);
            }
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ContactHolder holder = null;
            if (view != null) {
                Object tag = view.getTag(R.id.base_view_tag_id);
                if (tag != null && tag instanceof ContactHolder) {
                    holder = (ContactHolder) tag;
                }
            }
            if (holder == null || cursor == null || cursor.isClosed()) {
                return;
            }
            int id = -1;
            String lookupKey = null;
            String fullName = null;
            String photoThumbnailUriString = null;
            try {
                int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                if (idIndex != -1) {
                    id = cursor.getInt(idIndex);
                }
                int lookupKeyIndex = cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);
                if (lookupKeyIndex != -1) {
                    lookupKey = cursor.getString(lookupKeyIndex);
                }
                int fullNameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);
                if (fullNameIndex != -1) {
                    fullName = cursor.getString(fullNameIndex);
                }
                int photoThumbnailUriStringIndex = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
                if (photoThumbnailUriStringIndex != -1) {
                    photoThumbnailUriString = cursor.getString(photoThumbnailUriStringIndex);
                }
            } catch (Throwable t) {
            }
            // photo thumbnail
            if (holder.photoThumbnailImageView != null && holder.initialTextView != null) {
                holder.photoThumbnailImageView.setImageDrawable(null);
                // may be missing from Contacts app
                if (photoThumbnailUriString != null) {
                    holder.photoThumbnailImageView.setImageURI(Uri.parse(photoThumbnailUriString));
                    // round the icon
                    Drawable photoThumbnailDrawable = holder.photoThumbnailImageView.getDrawable();
                    RoundedBitmapDrawable roundedBitmapDrawable = PicupImageUtils.toRoundedBitmapDrawable(context, photoThumbnailDrawable);
                    if (roundedBitmapDrawable != null) { // should be true
                        holder.photoThumbnailImageView.setImageDrawable(roundedBitmapDrawable);
                    }
                } else {
                    holder.initialTextView.setText("");
                    if (!TextUtils.isEmpty(fullName)) {
                        String[] fullNameArray = fullName.split(" ");
                        if (fullNameArray.length > 0) {
                            String firstName = fullNameArray[0];
                            if (!firstName.isEmpty()) {
                                holder.initialTextView.append(firstName.subSequence(0, 1));
                            }
                        }
                        if (fullNameArray.length > 1) {
                            String lastName = fullNameArray[1];
                            if (!lastName.isEmpty()) {
                                holder.initialTextView.append(" ");
                                holder.initialTextView.append(lastName.subSequence(0, 1));
                            }
                        }
                        holder.initialTextView.setVisibility(View.VISIBLE);
                        holder.photoThumbnailImageView.setVisibility(View.VISIBLE);
                    }
                }
            }
            // fullName
            if (holder.fullNameTextView != null) {
                holder.fullNameTextView.setText(fullName);
            }
            if (holder.deleteCheckBox != null) {
                ItemOnCheckedChangedListener checkedChangedListener = new ItemOnCheckedChangedListener(lookupKey, id);
                holder.deleteCheckBox.setOnCheckedChangeListener(null);
                if ((selectTable != null && selectTable.containsKey(lookupKey)) || selectAll) {
                    holder.deleteCheckBox.setChecked(true);
                } else {
                    holder.deleteCheckBox.setChecked(false);
                }
                holder.deleteCheckBox.setOnCheckedChangeListener(checkedChangedListener);
            }
        }

        int getDeleteSelectCount() {
            if (selectAll) {
                return getCount();
            } else {
                if (selectTable != null) {
                    return selectTable.size();
                }
            }
            return 0;
        }

        synchronized Hashtable<String, Integer> getSelectData() {
            Hashtable<String, Integer> data = new Hashtable<>();
            if (selectAll) {
                Cursor cursor = getCursor();
                try {
                    if (cursor != null && !cursor.isClosed()) {
                        if (cursor.moveToFirst()) {
                            while (cursor != null && !cursor.isClosed() || cursor.isAfterLast()) {
                                int id = -1;
                                String lookupKey = null;
                                int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                                if (idIndex != -1) {
                                    id = cursor.getInt(idIndex);
                                }
                                int lookupKeyIndex = cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);
                                if (lookupKeyIndex != -1) {
                                    lookupKey = cursor.getString(lookupKeyIndex);
                                }
                                if (!TextUtils.isEmpty(lookupKey)) {
                                    data.put(lookupKey, id);
                                }
                                cursor.moveToNext();
                            }
                        }
                    }
                } catch (Throwable t) {
                    Logger.logThrowable(t);
                }
            } else {
                return selectTable;
            }
            return data;
        }

        void setSelectAll(boolean selectAll) {
            this.selectAll = selectAll;
            notifyDataSetChanged();
            refreshCount();
        }

        private class ItemOnCheckedChangedListener implements CompoundButton.OnCheckedChangeListener {
            String lookupKey;
            int id;

            ItemOnCheckedChangedListener(String lookupKey, int id) {
                this.lookupKey = lookupKey;
                this.id = id;
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView == null) {
                    return;
                }
                int id = buttonView.getId();
                if (id == R.id.delete_checkbox) {
                    if (selectTable == null || TextUtils.isEmpty(lookupKey)) {
                        return;
                    }
                    if (isChecked) {
                        selectTable.put(lookupKey, id);
                    } else {
                        selectTable.remove(lookupKey);
                    }
                    refreshCount();
                }
            }
        }

        private class ContactHolder {
            CheckBox deleteCheckBox;
            ImageView photoThumbnailImageView;
            TextView initialTextView;
            TextView fullNameTextView;
        }
    }

    private synchronized void restartLoader(int id, Bundle args, LoaderManager.LoaderCallbacks<Cursor> callback) {
        String log = "DeleteActivity  - restartLoader";
        log += " - id:";
        log += id;
        if (this == null || isFinishing()) {
            log += " - activity is null/invalide state";
            Logger.log(log);
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            log += " - invalid permission";
            Logger.log(log);
            return;
        }
        LoaderManager loaderManager = getSupportLoaderManager();
        if (loaderManager == null) {
            log += " - loaderManager is null";
            Logger.log(log);
            return;
        }
        try {
            loaderManager.restartLoader(id, args, callback);
        } catch (Throwable t) {
            log += " - Throwable";
            Logger.log(log);
        }
    }

    public static class DeleteFragment extends BaseDialogFragment {
        public static String ArgsTag = "table";
        private int deletedCountSum = 0;
        private Hashtable<String, Integer> selectData;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle args = getArguments();
            if (args != null && !args.isEmpty()) {
                if (args.containsKey(ArgsTag)) {
                    selectData = (Hashtable<String, Integer>) args.getSerializable(ArgsTag);
                }
            }
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.delete_contact_title);
            builder.setMessage(R.string.delete_selected_message);
            builder.setNegativeButton(R.string.cancel_cap_label, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(R.string.delete_cap_label, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String log = "DeleteFragment - onClick";
                    Activity activity = getActivity();
                    if (activity == null || activity.isFinishing()) {
                        log += " - invalid state";
                        Logger.log(log);
                        return;
                    }
                    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                        log += " - no write permission";
                        Logger.log(log);
                        return;
                    }
                    ContentResolver resolver = activity.getContentResolver();
                    if (selectData != null && !selectData.isEmpty() && resolver != null) {
                        log += " - size:";
                        log += selectData.size();
                        log += "\n";
                        for (Map.Entry<String, Integer> entry : selectData.entrySet()) {
                            if (entry == null) {
                                continue;
                            }
                            String lookupKey = entry.getKey();
                            int id = entry.getValue();
                            log += " - lookupKey:";
                            log += lookupKey;
                            log += " - id:";
                            log += id;
                            int deletedRowCount = 0;
                            try {
                                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, "/" + lookupKey);
                                log += " - uri:";
                                log += uri;
                                log += "\n";
                                deletedRowCount = resolver.delete(uri, null, null);
                                deletedCountSum += deletedRowCount;
                            } catch (Throwable t) {
                                log += " - Delete Throwable";
                                Logger.log(log);
                                Logger.logThrowable(t);
                            }
                        }
                    }
                    if (deletedCountSum > 0) {
                        Toast.makeText(activity, deletedCountSum + " deleted", Toast.LENGTH_SHORT).show();
                    }
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    activity.finish();
                }
            });
            return builder.create();
        }
    }
}

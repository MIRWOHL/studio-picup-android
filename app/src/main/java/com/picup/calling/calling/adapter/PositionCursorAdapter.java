package com.picup.calling.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by frank.truong on 1/9/2017.
 */

public class PositionCursorAdapter extends CursorAdapter {

    /** flag - is position enable*/
    protected boolean isEnable = false;

    /** Display Position list - Cursor position:Display position*/
    private List<String> mPosition = null;

    /** Saved Position list - Display position:BaseContactId/lookupKey*/
    private List<String> mCursorPosition = null;

    /** Saved Preference key*/
    private String mPrefsKey;

    /** Saved Preference*/
    private SharedPreferences mPrefs;

    /** Saved Key Prefix*/
    private final String prefix = "savePostion_";

    /** 2.4 SavedKey prefix*/
    ///private final String prefix2_4 = DragSortSimpleCursorAdapter.class.getSimpleName();

    /** Saved Key Size*/
    private final String sizeKey = "size";

    /** 2.4 Saved Key item key*/
    private final String itemKey2_4 = "_base_id_";

    /** Saved Key item key*/
    private final String itemKey = "_key_";

    /** Context*/
    private Context context;

    public PositionCursorAdapter(Context context, Cursor cursor, int flag, String sharePreferenceKey) {
        super(context, cursor, flag);
        this.context = context;
        mPrefsKey = sharePreferenceKey;
        setup();
    }

    public PositionCursorAdapter(Context context, Cursor cursor, int flag, String sharePreferenceKey, boolean enable) {
        super(context, cursor, flag);
        this.context = context;
        isEnable = enable;
        mPrefsKey = sharePreferenceKey;
        setup();
    }

    /** {@inheritDoc} CursorAdapter*/
    @Override
    public void bindView(View arg0, Context arg1, Cursor arg2) {

    }

    /** {@inheritDoc} CursorAdapter*/
    @Override
    public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
        return null;
    }

    /** {@inheritDoc} CursorAdapter*/
    @Override
    public Cursor swapCursor(Cursor c) {
        Cursor old = super.swapCursor(c);

        setup();

        return old;
    }

    /** {@inheritDoc} CursorAdapter*/
    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);

        setup();
    }

    private void setup() {
        if (!isEnable) {
            return;
        }
        Cursor c = getCursor();

        mPosition = null;
        mCursorPosition = null;

        if (c == null || (c != null && c.isClosed())) return;

        //mPosition = new ArrayList<String>();
        //mSavedPosition = new ArrayList<String>();
        mPosition =  Collections.synchronizedList(new ArrayList<String>());
        mCursorPosition = Collections.synchronizedList(new ArrayList<String>());

        if (context == null && TextUtils.isEmpty(mPrefsKey)) {
            return;
        }
        mPrefs = context.getSharedPreferences(mPrefsKey, Context.MODE_PRIVATE);

        ///loadOrder();
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup group) {
        //Logger.log("PositionCursorAdapter - getDropDownView", Logger.LOG_VERBOSE);
        if (!isEnable) {
            return super.getDropDownView(position, view, group);
        }
        int index = getSavedIndex(position);
        if (index > -1) {
            return super.getDropDownView(index, view, group);
        }
        return super.getDropDownView(position, view, group);
    }

    @Override
    public int getItemViewType(int position) {
        //Logger.log("PositionCursorAdapter - getItemViewType", Logger.LOG_VERBOSE);
        if (!isEnable) {
            return super.getItemViewType(position);
        }
        int index = getSavedIndex(position);
        if (index > -1) {
            return super.getItemViewType(index);
        }
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        //Logger.log("PositionCursorAdapter - getItemId", Logger.LOG_VERBOSE);
        if (!isEnable) {
            return super.getItemId(position);
        }
        int index = getSavedIndex(position);
        if (index > -1) {
            return super.getItemId(index);
        }
        return super.getItemId(position);
    }

    @Override
    public View getView(int position, View view, ViewGroup group) {
        //Logger.log("PositionCursorAdapter - getView", Logger.LOG_VERBOSE);
        if (!isEnable) {
            return super.getView(position, view, group);
        }
        //Logger.log("PositionCursorAdapter - getView - position="+position, Logger.LOG_VERBOSE);
        int index = getSavedIndex(position);
        //Logger.log("PositionCursorAdapter - getView - index="+index, Logger.LOG_VERBOSE);
        if (index > -1) {
            return super.getView(index, view, group);
        }
        return super.getView(position, view, group);
    }

    @Override
    public boolean isEnabled(int position) {
        if (!isEnable) {
            return super.isEnabled(position);
        }
        int index = getSavedIndex(position);
        if (index > -1) {
            return super.isEnabled(index);
        }
        return super.isEnabled(position);
    }

    /** Method to read stored index*/
    public int getSavedIndex (int position) {
        //Logger.log("PositionCursorAdapter - getSavedIndex - position="+position, Logger.LOG_VERBOSE);
        int result = -1;
        if (mPosition == null || mCursorPosition == null) {
            return result;
        }
        if (!isEnable) {
            return result;
        }
        String key = null;
        if (position >= 0 && position < mPosition.size()) {
            key = mPosition.get(position);
        }
        //Logger.log("PositionCursorAdapter - getSavedIndex - key="+key, Logger.LOG_VERBOSE);
        if (!TextUtils.isEmpty(key)) {
            result = mCursorPosition.indexOf(key);
        }
        //Logger.log("PositionCursorAdapter - getSavedIndex - result="+result, Logger.LOG_VERBOSE);
        return result;
    }

    /** Method to set isEditable*/
    public void setEnable(boolean enable) {
        boolean hasChange = false;
        if (isEnable == enable) {
            hasChange = true;
        }
        isEnable = Boolean.valueOf(enable);
        if (hasChange) {
            setup();
        }
    }

    /** Method to save order*/
/*
    public synchronized void saveOrder(){
        if (!isEnable) {
            return;
        }
        if (mPrefs == null || mPosition == null) {
            return;
        }
        SharedPreferences.Editor clearEditor = mPrefs.edit();
        if (clearEditor == null) {
            return;
        }
        // clear old preference
        clearEditor.clear();
        ///SharedPreferencesCompat.apply(clearEditor);
        clearEditor = null;
        SharedPreferences.Editor editor = mPrefs.edit();
        if (editor == null) {
            return;
        }
        // put size first
        final String key = prefix+sizeKey;
        final String itemKeyPrefix = prefix + itemKey;
        editor.putInt(key, mPosition.size());
        for (int index = ic_key_0; index < mPosition.size(); index++){
            String value = mPosition.get(index);
            String itemKey = itemKeyPrefix+index;
            if (TextUtils.isEmpty(value) || TextUtils.isEmpty(itemKey)) {
                continue;
            }
            editor.putString(itemKey, value);
        }
        ///SharedPreferencesCompat.apply(editor);
    }
*/

    /** Method to load order*/
/*
    private synchronized void loadOrder(){
        //Logger.log("PositionCursorAdapter - load order", Logger.LOG_VERBOSE);
        if (!isEnable) {
            return;
        }
        Cursor c = getCursor();
        if (c == null || (c != null && c.isClosed())) {
            return;
        }
        if (mPrefs == null || mPosition == null || mCursorPosition == null) {
            return;
        }

        int cursorSize = ic_key_0;
        ArrayList<String> baseContactIdlist = new ArrayList<String>();
        ArrayList<String> lookUpKeyList = new ArrayList<String>();

        mPosition.clear();
        mCursorPosition.clear();

        c.moveToFirst();
        while (c != null && !c.isClosed() && c.isAfterLast() == false) {
            String lookUpKey = null;
            int lookUpKeyIndex = c.getColumnIndex(ReferContactColumns.LOOKUP_KEY);
            if (lookUpKeyIndex != -1) {
               lookUpKey = c.getString(lookUpKeyIndex);
            }
            if (lookUpKey == null) {
                lookUpKey = "";
            }
            lookUpKeyList.add(lookUpKey);

            String baseContactId = null;
            int baseContactIdIndex = c.getColumnIndex(ReferContactColumns.CONTACT_BASE_ID);
            if (baseContactIdIndex != -1) {
                baseContactId = c.getString(baseContactIdIndex);
            }
            if (baseContactId == null) {
                baseContactId = "";
            }
            baseContactIdlist.add(baseContactId);

            String key = lookUpKey;
            if (TextUtils.isEmpty(key)) {
                key = baseContactId;
            }
            if (!TextUtils.isEmpty(key) && !mCursorPosition.contains(key)) {
                mCursorPosition.add(key);
            }
            cursorSize ++;

            c.moveToNext();
        }

        int savedSize = ic_key_0;
        String lastVersionKey = prefix2_4+sizeKey;
        String currentVersionKey = prefix+sizeKey;

        if (mPrefs.contains(currentVersionKey)) {
            savedSize = mPrefs.getInt(currentVersionKey, ic_key_0);
        } else if (mPrefs.contains(lastVersionKey)) {
            savedSize = mPrefs.getInt(lastVersionKey, ic_key_0);
        }
        //Logger.log("PositionCursorAdapter - load order - savedSize="+savedSize, Logger.LOG_VERBOSE);
        // no stored value, therefore use the purple_shape original order
        if (savedSize == ic_key_0){
            for (int index = ic_key_0; index < cursorSize; index++){
                String baseContactId = baseContactIdlist.get(index);
                String lookUpKey = lookUpKeyList.get(index);
                if (!TextUtils.isEmpty(lookUpKey)) {
                    if (!mPosition.contains(lookUpKey)) {
                        mPosition.add(lookUpKey);
                    }
                } else {
                    if (!mPosition.contains(baseContactId)) {
                        mPosition.add(baseContactId);
                    }
                }
            }
        } else if (lookUpKeyList.size() == baseContactIdlist.size()){
            // load the stored position
            final String itemKeyPrefix = prefix + itemKey;
            final String prevItemKeyPrefix = prefix2_4 + itemKey2_4;

            for (int index = ic_key_0; index < savedSize; index++){
                if (mPrefs == null) {
                    break;
                }
                String item = null;
                String itemKey = itemKeyPrefix + index;
                String preItemKey = prevItemKeyPrefix + index;
                if (mPrefs.contains(itemKey)) {
                    item = mPrefs.getString(itemKey, null);
                } else if (mPrefs.contains(preItemKey)) {
                    item = mPrefs.getString(preItemKey, null);
                }
                if (TextUtils.isEmpty(item)) {
                    continue;
                }
                int contactIndex = baseContactIdlist.indexOf(item);
                int lookUpIndex = lookUpKeyList.indexOf(item);
                // check if item has been removed
                if (lookUpIndex == -1 && contactIndex == -1) {
                    continue;
                }
                if (!mPosition.contains(item)) {
                    mPosition.add(item);
                }
                if (lookUpIndex != -1) {
                    baseContactIdlist.remove(lookUpIndex);
                    lookUpKeyList.remove(lookUpIndex);
                } else if (contactIndex != -1) {
                    baseContactIdlist.remove(contactIndex);
                    lookUpKeyList.remove(contactIndex);
                }
            }
            // load the rest of item
            for (int index = ic_key_0; index < lookUpKeyList.size(); index++){
                String key = lookUpKeyList.get(index);
                if (TextUtils.isEmpty(key)) {
                    key = baseContactIdlist.get(index);
                }
                if (TextUtils.isEmpty(key)) {
                    continue;
                }
                if (!mPosition.contains(key)) {
                    mPosition.add(key);
                }
            }
        }
        baseContactIdlist.clear();
        lookUpKeyList.clear();

        //printList();
        notifyDataSetChanged();
    }
*/

}

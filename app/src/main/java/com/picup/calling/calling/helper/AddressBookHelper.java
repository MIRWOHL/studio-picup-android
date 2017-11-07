package com.picup.calling.helper;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.picup.calling.base.PicupApplication;
import com.picup.calling.util.Logger;
import android.net.Uri;
import android.os.DeadObjectException;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.picup.calling.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by ychang on 5/3/17.
 */

public final class AddressBookHelper {

    private static final String PICUP_CONTACT_SHARE_KEY = "picup_contact_id";

    /**
     * Method to start Native Edit Contact(s) Intent, user can select which contact to edit
     */
    public static void initUpdateContacts(Context context, String phoneNumber) {
        String log = "AddressBookHelper - initUpdateContacts";
        log += " - phoneNumber:";
        log += phoneNumber;
        // check state
        if (context == null) {
            log += " - context is null";
            Logger.log(log);
            return;
        }
        // Creates a new Intent to insert or edit a contact
        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        // Sets the MIME type
        intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        if (!TextUtils.isEmpty(phoneNumber)) {
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
        }
        intent.putExtra("finishActivityOnSaveCompleted", true);
        // Sends the Intent with an request ID
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException ANE) {
            log += " - ActivityNotFoundException";
        }
        Logger.log(log);
    }

    public static synchronized void insertLogoContact(Context context, String phoneNumber) {
        String log = "AddressBookHelper - insertLogoContact";
        log += " - phoneNumber:";
        log += phoneNumber;
        if (context == null) {
            log += " - context is null";
            Logger.log(log);
            return;
        }
        try {
            String name = context.getString(R.string.app_name);
            /*String[] res_str_array = context.getResources().getStringArray(res_array_id);
            ArrayList<Integer> res_id_array = new ArrayList<>();
            if (context != null && res_str_array != null && res_str_array.length > 0) {
                for (String item : res_str_array) {
                    if (item != null && !item.isEmpty()) {
                        int id = -1;
                        try {
                            id = context.getResources().getIdentifier(item, "drawable", context.getPackageName());
                        } catch (Throwable t) {
                        }
                        if (id != -1 && res_id_array != null)
                            res_id_array.add(id);
                    }
                }
            }*/
            ArrayList<Integer> res_id_array = new ArrayList<>();
            res_id_array.add(R.drawable.cup_1);
            insertContact(context, null, name, phoneNumber, res_id_array, R.drawable.cup_1);
        } catch (Throwable t) {
            log += " - Throwable";
            Logger.log(log);
            Logger.logThrowable(t);
        }
    }


    /**
     * Method to start Native Insert Contact Intent
     */
    public static void initCreateContact(Context context, String phoneNumber) {
        String log = "AddressBookHelper - initCreateContact";
        log += " - phoneNumber:";
        log += phoneNumber;
        if (context == null) {
            log += " - context is null";
            Logger.log(log);
            return;
        }
        // Creates a new Intent to insert a contact
        Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
        // Sets the MIME type to match the Contacts Provider
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        if (!TextUtils.isEmpty(phoneNumber))
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
        intent.putExtra("finishActivityOnSaveCompleted", true);

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            log += " - ActivityNotFoundException";
        }
        Logger.log(log);
    }

    /**
     * Method to start Native Edit Contact Intent
     */
    public static void initEditContact(Context context, int contactId, String lookupKey) {
        String log = "AddressBookHelper - initEditContact";
        log += " - contactId:";
        log += contactId;
        log += " - lookupKey:";
        log += lookupKey;
        if (context == null) {
            log += " - context is null";
            Logger.log(log);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_EDIT);
        Uri uri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey);
        if (uri == null) {
            log += " - lookupUri is null";
            Logger.log(log);
            return;
        }
        intent.setDataAndType(uri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        intent.putExtra("finishActivityOnSaveCompleted", true);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            log += " - ActivityNotFoundException";
        }
        Logger.log(log);
    }

    // return Picup Contact's CONTACT_ID in String format
    // return null if Picup Contact does not exist
    // if Picup Contact exisit it will return it's CONTACT_ID and write to SharePreference
    public static String findPicupContactID(Context context) {
        String log = "AddressBookHelper - findPicupContactID";
        if (context == null) {
            log += " - context is null";
            Logger.log(log);
            return null;
        }
        String picupContactId = null;
        SharedPreferences prefs = context.getSharedPreferences(PicupApplication.appId, Context.MODE_PRIVATE);
        if (prefs != null) {
            picupContactId = prefs.getString(PICUP_CONTACT_SHARE_KEY, null);
        }
        if (!TextUtils.isEmpty(picupContactId)) {
            log += " - picupContactId:";
            log += picupContactId;
            Logger.log(log);
            return picupContactId;
        }
        log += " - empty in sharePref";
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            log += " - invalid permission";
            Logger.log(log);
            return null;
        }
        Cursor cursor = null;
        try {
            String selection = null;
            String mimetypeKey = null;
            try {
                mimetypeKey = context.getString(R.string.APP_MIMETYPE_CONTACT);
            } catch (Throwable t) {
            }
            log += " - mimetypeKey:";
            log += mimetypeKey;
            if (!TextUtils.isEmpty(mimetypeKey)) {
                selection = ContactsContract.Data.MIMETYPE + " = '" + mimetypeKey + "'";
            }
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) {
                log += " - resolver is null";
                Logger.log(log);
                return null;
            }
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID};
            cursor = resolver.query(ContactsContract.Data.CONTENT_URI, projection, selection, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
                if (index != -1) {
                    picupContactId = cursor.getString(index);
                }
                SharedPreferences.Editor editor = null;
                if (prefs != null) {
                    editor = prefs.edit();
                }
                if (editor != null) {
                    if (!TextUtils.isEmpty(picupContactId)) {
                        editor.putString(PICUP_CONTACT_SHARE_KEY, picupContactId);
                    } else {
                        editor.remove(PICUP_CONTACT_SHARE_KEY);
                    }
                    editor.apply();
                }
                return picupContactId;
            }
        } catch (SecurityException se) {
        } catch (Throwable t) {
            log += " - Throwable";
            Logger.log(log);
            Logger.logThrowable(t);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return null;
    }

    private static void insertContact(Context context, String lastName, String firstName, String phoneNumber,
                                      final ArrayList<Integer> res_id_array, final int default_image_id) {
        String log = "AddressBookHelper - insertContact";
        log += " - firstName:";
        log += firstName;
        log += " - lastName:";
        log += lastName;
        log += " - phoneNumber:";
        log += phoneNumber;

        if (context == null) {
            log += " - context is null";
            Logger.log(log);
            return;
        }
        //check permission
        boolean writePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        boolean readPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        log += " - writePermission:";
        log += writePermission;
        log += " - readPermission:";
        log += readPermission;

        if (!writePermission) {
            log += " - permission issue";
            Logger.log(log);
            return;
        }

        ContentResolver resolver = context.getContentResolver();
        if (resolver == null) {
            log += " - resolver is null";
            Logger.log(log);
            return;
        }
        try {
            // check for contact id, in case cache (data) has been clear but Picup contact still exist
            String PicupContactId = findPicupContactID(context);
            long PicupRAWContactId = -1;
            log += " - PicupContactId:";
            log += PicupContactId;

            if (!TextUtils.isEmpty(PicupContactId) && readPermission) {

                boolean hasPicuPContact = false;
                String selection = ContactsContract.Contacts._ID + " = '" + PicupContactId + "'";
                String[] projection = new String[]{ContactsContract.Contacts._ID};
                Cursor cursor = null;
                try {
                    cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, projection,
                            selection, null, null);
                    if (cursor != null && cursor.getCount() > 0) {
                        hasPicuPContact = true;
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
                log += " - hasPicuPContact:";
                log += hasPicuPContact;

                if (hasPicuPContact) {
                    //need to find raw contact id using the contact_id
                    Cursor c = null;
                    try {
                        c = resolver.query(ContactsContract.RawContacts.CONTENT_URI,
                                new String[]{ContactsContract.RawContacts._ID},
                                ContactsContract.RawContacts.CONTACT_ID + "=?",
                                new String[]{String.valueOf(PicupContactId)}, null);
                        if (c != null && !c.isClosed() && c.moveToFirst()) {
                            PicupRAWContactId = c.getLong(0);
                        }
                    } finally {
                        if (c != null && !c.isClosed()) {
                            c.close();
                        }
                    }

                    // To keep number store in UM CONTACT to a maximum amount of number
                    Cursor phone_cursor = null;
                    try {
                        String phone_selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = '" + PicupContactId + "'";
                        String[] phone_projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                        phone_cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, phone_projection,
                                phone_selection, null, null);
                        if (phone_cursor != null && phone_cursor.getCount() > 0) {
                            ArrayList<ContentProviderOperation> opsDelete = new ArrayList<>();
                            phone_cursor.move(-1);
                            while (phone_cursor.moveToNext()) {
                                ContentProviderOperation.Builder builder = ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI);
                                String phoneSelection = ContactsContract.CommonDataKinds.Phone.NUMBER + " = ? AND " + ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
                                String[] phoneSelectionArgs = new String[]{phone_cursor.getString(phone_cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)), PicupContactId};
                                builder.withSelection(phoneSelection, phoneSelectionArgs);
                                opsDelete.add(builder.build());
                            }
                            if (!opsDelete.isEmpty()) {
                                resolver.applyBatch(ContactsContract.AUTHORITY, opsDelete);
                            }
                        }
                    } catch (DeadObjectException de) {
                    } catch (Throwable t) {
                        Logger.logThrowable(t);
                    } finally {
                        if (phone_cursor != null && !phone_cursor.isClosed()) {
                            phone_cursor.close();
                        }
                    }
                }
            }
            if (TextUtils.isEmpty(phoneNumber)) {
                log += " - phoneNumber is empty";
                Logger.log(log);
                return;
            }

            log += " - PicupRAWContactId:";
            log += PicupRAWContactId;

            if (PicupRAWContactId != -1) {
                log += " - update";
                // perform update not insert
                try {
                    ContentValues values = new ContentValues();
                    values.put(ContactsContract.Data.RAW_CONTACT_ID, PicupRAWContactId);
                    values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                    values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);
                    values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_OTHER);
                    resolver.insert(ContactsContract.Data.CONTENT_URI, values);
                } catch (Exception e) {
                    Logger.logThrowable(e);
                }

            } else {
                log += " - insert";
                // perform insert not update
                // contact is not exist create one
                ArrayList<ContentProviderOperation> ops = new ArrayList<>();

                ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI);
                builder.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null);
                builder.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null);
                ops.add(builder.build());

                // Name
                builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
                builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
                builder.withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
                if (lastName != null) {
                    builder.withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName);
                }
                builder.withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName);
                ops.add(builder.build());

                // Number
                builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
                builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
                builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);
                builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_OTHER);
                ops.add(builder.build());

                // Image
                //Default width device can support
                int default_support_size = getMaxContactPhotoSize(context);
                final int contactImageId = findSuitableContactImageResoucesId(context, res_id_array, default_support_size);
                log += " - contactImageId:";
                log += contactImageId;
                log += " - default_support_width:";
                log += default_support_size;
                if (contactImageId > 0) {
                    try {
                        System.gc();
                        Bitmap mBitmap = null;
                        try {
                            mBitmap = ImageHelper.decodeSampledBitmapFromResource(context.getResources(), contactImageId, default_support_size);
                        } catch (OutOfMemoryError E) {
                            if (mBitmap != null) {
                                mBitmap.recycle();
                            }
                            System.gc();
                            try {
                                mBitmap = ImageHelper.decodeSampledBitmapFromResource(context.getResources(), default_image_id, default_support_size);
                            } catch (OutOfMemoryError E2) {
                                if (mBitmap != null) {
                                    mBitmap.recycle();
                                }
                                System.gc();
                                mBitmap = null;
                            }
                        }
                        if (mBitmap != null) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            mBitmap.recycle();
                            System.gc();
                            // Adding insert operation to operations list
                            // to insert Photo in the table ContactsContract.Data
                            ops.add(ContentProviderOperation
                                    .newInsert(ContactsContract.Data.CONTENT_URI)
                                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                    .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                                    .withValue(ContactsContract.Data.MIMETYPE,
                                            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, stream.toByteArray()).build());

                            try {
                                stream.flush();
                                System.gc();
                            } catch (IOException e) {
                                Logger.logThrowable(e);
                            }
                        }
                    } catch (Throwable t) {
                        Logger.logThrowable(t);
                    }
                }

                // add unique key
                String mimetypeKey = null;

                try {
                    mimetypeKey = context.getString(R.string.APP_MIMETYPE_CONTACT);
                } catch (Throwable t) {
                }

                log += " - mimetype:";
                log += mimetypeKey;

                if (!TextUtils.isEmpty(mimetypeKey)) {
                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, mimetypeKey)
                            .build());
                }

                // Add the new contact
                ContentProviderResult[] res = null;
                try {
                    ContentResolver applyResolver = context.getContentResolver();
                    if (applyResolver != null && !ops.isEmpty()) {
                        res = applyResolver.applyBatch(ContactsContract.AUTHORITY, ops);
                    }
                    if (res != null && res.length > 0 && readPermission) {
                        // contact id
                        Uri contactLookupUri = null;
                        ContentProviderResult result = res[0];
                        if (result != null) {
                            contactLookupUri = result.uri;
                        }
                        ContentResolver contentResolver = context.getContentResolver();
                        long contactId = -1;
                        if (contentResolver != null && contactLookupUri != null) {
                            try {
                                Cursor c = null;
                                boolean lookupResult = true;
                                String[] lookupProjection = new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID};
                                try {
                                    c = contentResolver.query(contactLookupUri, lookupProjection, null, null, null);
                                    if (c == null || c.isClosed() || !c.moveToFirst()) {
                                        lookupResult = false;
                                    }
                                    if (lookupResult) {
                                        contactId = c.getLong(0);
                                    }
                                } catch (Throwable t) {
                                    lookupResult = false;
                                } finally {
                                    if (c != null && !c.isClosed()) {
                                        c.close();
                                    }
                                }
                                if (!lookupResult) {
                                    return;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }
                        }
                        SharedPreferences prefs = context.getSharedPreferences(PicupApplication.appId, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = null;
                        if (prefs != null) {
                            editor = prefs.edit();
                        }
                        if (editor != null) {
                            editor.putString(PICUP_CONTACT_SHARE_KEY, Long.toString(contactId));
                            editor.apply();
                        }
                        setDisplayPhotoByRawContactId(context, contactId, contactImageId);
                    }
                } catch (DeadObjectException de) {
                } catch (Exception e) {
                    Logger.logThrowable(e);
                }
            }
        } catch (Exception e2) {
            Logger.logThrowable(e2);
        }
        Logger.log(log);
    }

    private static int getMaxContactPhotoSize(final Context context) {
        int defaultSize = 96;
        String log = "AddressBookHelper - getMaxContactPhotoSize";
        if (context == null) {
            log += " - context null";
            Logger.log(log);
            return defaultSize;
        }
        try {
            // Note that this URI is safe to call on the UI thread.
            final Uri uri = ContactsContract.DisplayPhoto.CONTENT_MAX_DIMENSIONS_URI;
            final String[] projection = new String[]{ContactsContract.DisplayPhoto.DISPLAY_MAX_DIM};
            ContentResolver resolver = context.getContentResolver();
            if (uri == null || resolver == null) {
                log += " - uri or resolver is null";
                Logger.log(log);
                return defaultSize;
            }
            final Cursor c = context.getContentResolver().query(uri, projection, null, null, null);
            try {
                if (c != null && !c.isClosed() && c.getCount() > 0) {
                    c.moveToFirst();
                    int index = c.getColumnIndex(ContactsContract.DisplayPhoto.DISPLAY_MAX_DIM);
                    int size = defaultSize;
                    if (index != -1) {
                        size = c.getInt(index);
                    }
                    log += " - index:";
                    log += index;
                    log += " - size:";
                    log += size;
                    Logger.log(log);
                    return size;
                }
            } catch (CursorIndexOutOfBoundsException boundsException) {
                log += " - CursorIndexOutOfBoundsException";
            } finally {
                if (c != null && !c.isClosed()) {
                    c.close();
                }
            }
        } catch (Throwable t) {
            log += " - throwable";
            Logger.log(log);
            Logger.logThrowable(t);
        }
        // fallback: 96x96 is the max contact photo size for pre-ICS versions
        log += " - error - set default";
        //Logger.log(sb.toString(), Logger.LOG_VERBOSE);
        return defaultSize;
    }

    /***
     * Method to determine suitable contact image resources <br/>
     * MaxSize is consider as square
     */
    private static int findSuitableContactImageResoucesId(Context context, ArrayList<Integer> list, int maxSize) {
        if (context == null || list == null || list.isEmpty() || maxSize < 1) {
            return 0;
        }
        try {
            int resultId = 0;
            double ratio = -1;
            int min_size = maxSize;
            int min_size_id = 0;
            for (Integer resId : list) {
                if (resId == null) {
                    continue;
                }
                int[] tempSize = ImageHelper.getImageSize(context, resId);
                if (tempSize == null || tempSize.length != 2) {
                    continue;
                }
                //size of image has too be smaller than targetSize to qualify
                if (tempSize[0] <= maxSize && tempSize[1] <= maxSize) {
                    //Ratio calculation (actual size/targetSize)
                    // 	96/720 = 0.13333333333
                    //	256/720= 0.35555555555
                    //	96/96 = 1
                    //  256/96 = 2.66666666667 (larger than target Size, not qualify by default)
                    double temp_ratio = (double) tempSize[0] / (double) maxSize;
                    if (temp_ratio > ratio || ratio == -1) {
                        ratio = temp_ratio;
                        resultId = resId;
                    }
                }
                //find smallest size for default value
                if (tempSize[0] == tempSize[1]) {
                    if (tempSize[0] < min_size || min_size_id == 0) {
                        min_size = tempSize[0];
                        min_size_id = resId;
                    }
                }
            }
            if (resultId == 0 && min_size_id != 0) {
                resultId = min_size_id;
            }
            return resultId;
        } catch (Throwable t) {
            Logger.logThrowable(t);
        }
        return 0;
    }

    /**
     * @return true if picture was changed false otherwise.
     */
    private static boolean setDisplayPhotoByRawContactId(Context context, long rawContactId, int resId) {
        String log = "AddressBookHelper - setDisplayPhotoByRawContactId";
        log += " - rawContactId:";
        log += rawContactId;
        log += " - resId:";
        log += resId;

        if (context == null || rawContactId == -1 || resId == -1) {
            log += " - invalid argument";
            Logger.log(log);
            return false;
        }
        try {
            Bitmap bmp = null;
            if (context.getResources() != null) {
                bmp = BitmapFactory.decodeResource(context.getResources(), resId);
            }
            ContentResolver resolver = context.getContentResolver();
            if (bmp == null || resolver == null) {
                log += " - resolver or bitmap is null";
                Logger.log(log);
                return false;
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            Uri pictureUri = Uri.withAppendedPath(ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI,
                    rawContactId), ContactsContract.RawContacts.DisplayPhoto.CONTENT_DIRECTORY);
            AssetFileDescriptor afd = resolver.openAssetFileDescriptor(pictureUri, "rw");
            if (afd != null) {
                OutputStream os = afd.createOutputStream();
                os.write(byteArray);
                os.close();
                afd.close();
                log += " - write finish";
                Logger.log(log);
                return true;
            }
        } catch (IOException e) {
            log += " - exception";
            Logger.log(log);
            Logger.logThrowable(e);
        }
        Logger.log(log);
        return false;
    }

}
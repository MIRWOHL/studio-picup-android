package com.picup.calling.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

/**
 * Created by frank.truong on 2/6/2017.
 */
public final class Utililites {
    private static final int PERMISSION_REQUEST_CODE = 1;
    public static void checkPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                if (context instanceof Activity) {
                    ((Activity) context).requestPermissions(new String[]{permission}, PERMISSION_REQUEST_CODE);
                }
            }
        }
    }

    public static  String[] arrayList2array(ArrayList<String> arrayList) {
        if (arrayList == null || arrayList.isEmpty()) {
            return null;
        }
        return arrayList.toArray(new String[arrayList.size()]);
    }
}

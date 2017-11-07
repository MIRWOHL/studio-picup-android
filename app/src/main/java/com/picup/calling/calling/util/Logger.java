package com.picup.calling.util;

import com.picup.calling.base.PicupApplication;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.picup.calling.BuildConfig.LOGCAT_ENABLED;

/**
 * Created by ychang on 4/28/17.
 */
public final class Logger {

    private static String Tag = "com.picup.calling";

    public static void log(String log) {
        if (TextUtils.isEmpty(log)) {
            return;
        }
        if (PicupApplication.isCrashlyticInitialize) {
            Crashlytics.log(log);
        }
        if (LOGCAT_ENABLED) {
            Log.i(Tag, log);
        }
    }

    public static void logThrowable(Throwable t) {
        if (t == null) {
            return;
        }
        try {
            if (PicupApplication.isCrashlyticInitialize) {
                Crashlytics.logException(t);
            }
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            if (!TextUtils.isEmpty(exceptionAsString)) {
                log(exceptionAsString);
            } else {
                log(t.toString());
            }
        } catch (Throwable throwable) {
        }
    }
}

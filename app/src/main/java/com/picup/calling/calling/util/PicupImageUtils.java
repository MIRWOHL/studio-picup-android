package com.picup.calling.util;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

/**
 * Created by frank.truong on 12/15/2016.
 */

public class PicupImageUtils {

    public static RoundedBitmapDrawable toRoundedBitmapDrawable(Context context, Drawable drawable) {
        RoundedBitmapDrawable roundedBitmapDrawable = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable)drawable;
            roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmapDrawable.getBitmap());
            roundedBitmapDrawable.setCircular(true);
            roundedBitmapDrawable.setAntiAlias(true);
        }
        return roundedBitmapDrawable;
    }
}

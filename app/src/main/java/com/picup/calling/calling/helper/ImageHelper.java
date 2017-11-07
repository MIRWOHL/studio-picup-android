package com.picup.calling.helper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.picup.calling.util.Logger;

/**
 * Created by ychang on 5/3/17.
 */
 final class ImageHelper {

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            int heightRatio = 1;
            int widthRatio = 1;

            //this code with round only return 1, too large for gingerbread phone, it required more aggressive reduce size
            heightRatio = Math.round((float) height / (float) reqHeight);
            widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    /**
     * Image decode only using width as restriction
     * Image ratio is kept, so the height will be automatically adjusted
     */
    static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int maxWidth) {
        return decodeSampledBitmapFromResource(res, resId, -1, maxWidth);
    }

    /**
     * Overload for {@link #decodeSampledBitmapFromResource(Resources, int, int, int, int, int)}
     * Image ratio is kept, so the height will be automatically adjusted
     */
    private static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int maxWidth) {
        return decodeSampledBitmapFromResource(res, resId, reqWidth, -1, maxWidth, -1);
    }

    private static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight, int maxWidth, int maxHeight) {
        String log = "ImageHelper - decodeSampledBitmapFromResource";
        Bitmap result = null;
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            //inPurgeable option
            options.inPurgeable = true;
            //below required to get measurement
            //with inJustDecodeBound = true, it query the bitmap without having to allocate the memory for its pixels.
            try {
                BitmapFactory.decodeResource(res, resId, options);
            } catch (Throwable t) {
                //if above fail, retry again by increase inSampleSize (1/4, the original image)
                try {
                    options.inSampleSize = options.inSampleSize * 4;
                    BitmapFactory.decodeResource(res, resId, options);
                } catch (Throwable t2) {
                }
            }
            ;
            int height = options.outHeight;
            int width = options.outWidth;
            double ratio = (double) height / (double) width;
            //determine minimum width
            int min_width = width;
            if (reqWidth > 0)
                min_width = Math.min(reqWidth, min_width);
            if (maxWidth > 0)
                min_width = Math.min(maxWidth, min_width);
            //determine minimum height
            int min_height = height;
            if (reqHeight > 0)
                min_height = Math.min(reqHeight, min_height);
            if (maxHeight > 0)
                min_height = Math.min(maxHeight, min_height);
            //Ratio
            double ratio_height = -1;
            if (min_height > 0)
                ratio_height = ((double) height) / ((double) min_height);
            double ratio_width = -1;
            if (min_width > 0)
                ratio_width = ((double) width) / ((double) min_width);
            int sample_height = height;
            int sample_width = width;
            if (ratio_width >= ratio_height) {
                sample_height = (int) ((double) min_width * ratio);
                sample_width = min_width;
            } else {
                sample_height = min_height;
                sample_width = (int) ((double) min_height / ratio);
            }
            // Calculate inSampleSize
            if (options.inSampleSize <= 1) {
                if (sample_height > 0 && sample_width > 0)
                    options.inSampleSize = calculateInSampleSize(options, sample_width, sample_height);
                else
                    options.inSampleSize = calculateInSampleSize(options, width, height);
            }
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            try {
                System.gc();
                result = BitmapFactory.decodeResource(res, resId, options);
            } catch (Throwable t) {
                log += " - OOM";
                Logger.log(log);
                if (result != null) {
                    if (!result.isRecycled())
                        result.recycle();
                    result = null;
                }
                System.gc();
                options.inSampleSize = options.inSampleSize * 4;
                result = BitmapFactory.decodeResource(res, resId, options);
            }
        } catch (Throwable t) {
            log += " - OOM2";
            Logger.log(log);
            if (result != null) {
                if (!result.isRecycled())
                    result.recycle();
                result = null;
            }
            System.gc();

            Logger.logThrowable(t);
        }
        return result;
    }

    static int[] getImageSize(final Context context, int imageID) {
        int[] size = new int[2];
        if (context == null) {
            return size;
        }
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            //inPurgeable option
            options.inPurgeable = true;
            //below required to get measurement
            //with inJustDecodeBound = true, it query the bitmap without having to allocate the memory for its pixels.
            Resources resources = context.getResources();
            if (resources != null) {
                BitmapFactory.decodeResource(resources, imageID, options);
                //get Width
                size[0] = options.outWidth;
                //get Height
                size[1] = options.outHeight;
            }
        } catch (Throwable t) {
            Logger.logThrowable(t);
        }
        return size;
    }
}

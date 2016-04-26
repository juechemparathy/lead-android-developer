package com.gaborbiro.marveldemo.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.gaborbiro.marveldemo.App;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class BitmapUtils {

    public static Bitmap getBitmapFromFile(String path, int idealWidth, int idealHeight) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(photoW / idealWidth, photoH / idealHeight);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(path, bmOptions);
    }

    public static Bitmap decodeStream(InputStream in, int targetMaxSizeDp) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            return BitmapFactory.decodeStream(is, null, null);
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap flip(Bitmap source) {
        Matrix m = new Matrix();
        m.preScale(-1, 1);
        Bitmap dst =
                Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                        m, false);
        dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        return dst;
    }
}

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
        int scaleFactor = Math.min(photoW / idealWidth, photoH / idealHeight);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return flip(BitmapFactory.decodeFile(path, bmOptions));
    }

    public static Bitmap getBitmapFromStream(InputStream is, int idealWidth,
            int idealHeight) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        float scaleFactor =
                Math.min(photoW / (float) idealWidth, photoH / (float) idealHeight);

        Bitmap bm = BitmapFactory.decodeStream(is);
        bm = Bitmap.createScaledBitmap(bm, (int) (photoW / scaleFactor),
                (int) (photoH / scaleFactor), false);
        return flip(bm);
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
            InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
            InputStream is2 = new ByteArrayInputStream(baos.toByteArray());

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is1, null, o);

            int scale = 1;
            int maxSizePx = dpToPx(targetMaxSizeDp);
            if (o.outHeight > maxSizePx || o.outWidth > maxSizePx) {
                scale = (int) Math.pow(2, (int) Math.round(
                        Math.log(maxSizePx / (double) Math.max(o.outHeight, o.outWidth)) /
                                Math.log(0.5)));
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = 8;
            return flip(BitmapFactory.decodeStream(is2, null, o2));
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

    public static int dpToPx(int dp) {
        Resources r = App.getAppContext()
                .getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                r.getDisplayMetrics());
    }
}

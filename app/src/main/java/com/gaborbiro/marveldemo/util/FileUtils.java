package com.gaborbiro.marveldemo.util;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtils {

    public static String getNewCoverPhotoName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return "JPEG_" + timeStamp + ".jpg";
    }

    public static File createTempJpeg() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File marvelStorageDir = new File(storageDir, "Marvel");

        if (!marvelStorageDir.exists() || !marvelStorageDir.isDirectory()) {
            marvelStorageDir.mkdir();
        }
        File image = File.createTempFile(imageFileName, ".jpg", marvelStorageDir);
        return image;
    }
}

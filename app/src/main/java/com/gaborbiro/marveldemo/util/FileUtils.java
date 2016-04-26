package com.gaborbiro.marveldemo.util;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtils {

    public static File createTempJpeg(String postfix) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File marvelStorageDir = new File(storageDir, "Marvel");

        if (!marvelStorageDir.exists() || !marvelStorageDir.isDirectory()) {
            marvelStorageDir.mkdir();
        }
        File image = File.createTempFile(imageFileName, "_" + postfix + ".jpg", marvelStorageDir);
        return image;
    }
}

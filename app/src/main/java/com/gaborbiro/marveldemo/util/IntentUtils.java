package com.gaborbiro.marveldemo.util;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.TypedValue;

import com.gaborbiro.marveldemo.App;

import java.io.File;

public class IntentUtils {

    public static Intent generateTakePictureIntent(File targetFile) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(App.getAppContext().getPackageManager()) != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(targetFile));
            return takePictureIntent;
        }
        return null;
    }
}

package com.gaborbiro.marveldemo.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.gaborbiro.marveldemo.App;

public class NetUtils {

    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) App.getAppContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

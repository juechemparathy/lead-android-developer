package com.gaborbiro.marveldemo.provider.api;

import android.content.Context;
import android.os.Environment;

import com.gaborbiro.marveldemo.App;
import com.gaborbiro.marveldemo.util.NetUtils;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUtil {

    private static final int CACHE_SIZE = 10 * 1024 * 1024;
    private static final String CACHE_FOLDER_NAME = "comics";
    private static final int CACHE_EXPIRY = 60 * 60 * 24 * 1; // tolerate 1-day stale

    public static Retrofit getRetrofit(String url) {
        Cache cache = new Cache(getDiskCacheDir(App.getAppContext(), CACHE_FOLDER_NAME),
                CACHE_SIZE);
        OkHttpClient okHttpClient = new OkHttpClient.Builder().cache(cache)
                .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
                .build();
        return new Retrofit.Builder().baseUrl(url)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private static final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR =
            new Interceptor() {

                @Override public okhttp3.Response intercept(Chain chain)
                        throws IOException {
                    okhttp3.Response originalResponse = chain.proceed(chain.request());

                    if (!NetUtils.isNetworkAvailable()) {
                        return originalResponse.newBuilder()
                                .header("Cache-Control",
                                        "public, only-if-cached, max-stale=" +
                                                CACHE_EXPIRY)
                                .build();
                    }
                    return originalResponse;
                }
            };

    /**
     * Creates a unique subdirectory of the designated app cache directory. Tries to use
     * external but if not mounted,
     * falls back on internal storage.
     */
    private static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use
        // external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Environment.isExternalStorageRemovable()
                ? context.getExternalCacheDir()
                        .getPath() : context.getCacheDir()
                        .getPath();

        return new File(cachePath + File.separator + uniqueName);
    }
}

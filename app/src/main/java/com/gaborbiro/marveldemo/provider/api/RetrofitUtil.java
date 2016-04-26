package com.gaborbiro.marveldemo.provider.api;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.gaborbiro.marveldemo.App;
import com.gaborbiro.marveldemo.util.NetUtils;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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

    /**
     * Forcing retrofit to cache for 1 day, regardless of what the server says the
     * stale-ness is
     */
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
     * external but if not mounted, falls back on internal storage.
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

    public static <T> void enqueueWithRetry(Call<T> call, final Callback<T> callback) {
        call.enqueue(new CallbackWithRetry<T>(call) {
            @Override public void onResponse(Call<T> call, Response<T> response) {
                callback.onResponse(call, response);
            }

            @Override public void onFailure(Call<T> call, Throwable t) {
                super.onFailure(call, t);
                callback.onFailure(call, t);
            }
        });
    }

    public static abstract class CallbackWithRetry<T> implements Callback<T> {

        private static final int TOTAL_RETRIES = 3;
        private final String TAG = CallbackWithRetry.class.getSimpleName();
        private final Call<T> call;
        private int retryCount = 0;

        public CallbackWithRetry(Call<T> call) {
            this.call = call;
        }

        @Override public void onFailure(Call<T> call, Throwable t) {
            Log.e(TAG, t.getLocalizedMessage());
            if (retryCount++ < TOTAL_RETRIES) {
                Log.v(TAG,
                        "Retrying... (" + retryCount + " out of " + TOTAL_RETRIES + ")");
                retry();
            }
        }

        private void retry() {
            call.clone()
                    .enqueue(this);
        }
    }
}

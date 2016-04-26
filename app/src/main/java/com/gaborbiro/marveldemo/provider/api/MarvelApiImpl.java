package com.gaborbiro.marveldemo.provider.api;

import android.text.TextUtils;
import android.util.Log;

import com.gaborbiro.marveldemo.provider.api.model.Comics;
import com.gaborbiro.marveldemo.util.CryptoUtils;

import java.io.IOException;

import javax.inject.Provider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MarvelApiImpl implements MarvelApi {

    private enum OrderBy {
        focDate, onsaleDate, title, issueNumber
    }

    private static final String PUBLIC_KEY = "09c15bec8403ca33078140ff47816bdb";
    private static final String PRIVATE_KEY = "da729d48b3862ca12b80ba6dc3dcf1b3a0f6381d";

    private static final String TAG = "MarvelApi";

    public Provider<MarvelApiRequestInterface> apiProvider;

    public MarvelApiImpl(Provider<MarvelApiRequestInterface> apiProvider) {
        this.apiProvider = apiProvider;
    }


    @Override public void getComics(int page, int pageSize, Callback<Comics> callback)
            throws IOException, ComicsFetchingException {
        int limit = pageSize;
        int offset = page * pageSize;
        String ts = "1";
        String hash = CryptoUtils.md5(ts + PRIVATE_KEY + PUBLIC_KEY);
        Call<Comics> call = apiProvider.get()
                .getComics(ts, OrderBy.focDate.name(), limit, offset, PUBLIC_KEY, hash);
        Log.d(TAG, call.request()
                .url()
                .toString());
        try {
            RetrofitUtil.enqueueWithRetry(call, callback);
//            Response<Comics> response = call.execute();
//
//            if (response.isSuccessful() && response.body() != null) {
//                return response.body();
//            } else {
//                String message = response.message();
//
//                if (TextUtils.isEmpty(message)) {
//                    message = response.raw()
//                            .toString();
//                }
//                throw new ComicsFetchingException(message);
//            }
        } catch (RuntimeException e) {
            throw new ComicsFetchingException(e);
        }
    }
}

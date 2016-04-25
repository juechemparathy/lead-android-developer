package com.gaborbiro.marveldemo.provider.api;

import com.gaborbiro.marveldemo.provider.api.model.Comics;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MarvelApiRequestInterface {

    @GET("public/comics") public Call<Comics> getComics(@Query("ts") String ts,
            @Query("orderBy") String orderBy, @Query("limit") int limit,
            @Query("offset") int offset, @Query("apikey") String apiKey,
            @Query("hash") String hash);

}

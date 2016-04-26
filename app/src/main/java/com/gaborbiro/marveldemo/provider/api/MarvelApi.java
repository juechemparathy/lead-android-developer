package com.gaborbiro.marveldemo.provider.api;

import com.gaborbiro.marveldemo.provider.api.model.Comics;

import java.io.IOException;

import retrofit2.Callback;

public interface MarvelApi {
    void getComics(int page, int pageSize, Callback<Comics> callback) throws IOException, ComicsFetchingException;
}

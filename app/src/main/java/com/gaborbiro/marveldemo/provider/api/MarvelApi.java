package com.gaborbiro.marveldemo.provider.api;

import com.gaborbiro.marveldemo.provider.api.model.Comics;

import java.io.IOException;

public interface MarvelApi {
    public Comics getComics(int page, int pageSize) throws IOException, ComicsFetchingException;
}

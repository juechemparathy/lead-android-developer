package com.gaborbiro.marveldemo.provider.api;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

@Module public class MarvelApiModule {

    private static String MARVEL_API_BASE_URL = "http://gateway.marvel.com:80/v1/";

    @Provides @Singleton
    public MarvelApi provideMarvelApi(Provider<MarvelApiRequestInterface> apiProvider) {
        return new MarvelApiImpl(apiProvider);
    }

    @Provides public MarvelApiRequestInterface provideMarvelApiRequestInterface() {
        Retrofit retrofit = RetrofitUtil.getRetrofit(MARVEL_API_BASE_URL);
        return retrofit.create(MarvelApiRequestInterface.class);
    }
}

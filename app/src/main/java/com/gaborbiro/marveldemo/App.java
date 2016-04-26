package com.gaborbiro.marveldemo;

import android.app.Application;
import android.content.Context;

import com.gaborbiro.marveldemo.provider.api.MarvelApiModule;
import com.gaborbiro.marveldemo.provider.dropbox.DropboxApiModule;

public class App extends Application {

    private MarvelApiComponent mMarvelApiComponent;
    private DropboxApiComponent mDropboxApiComponent;

    private static Context sAppContext;

    @Override public void onCreate() {
        super.onCreate();
        sAppContext = this;

		/* DaggerAppComponent */
        mMarvelApiComponent = DaggerMarvelApiComponent.builder()
                .marvelApiModule(new MarvelApiModule())
                .build();
        mDropboxApiComponent = DaggerDropboxApiComponent.builder()
                .dropboxApiModule(new DropboxApiModule())
                .build();
    }

    public static Context getAppContext() {
        return sAppContext;
    }

    public MarvelApiComponent getAppComponent() {
        return mMarvelApiComponent;
    }

    public DropboxApiComponent getDropboxApiComponent() {
        return mDropboxApiComponent;
    }
}

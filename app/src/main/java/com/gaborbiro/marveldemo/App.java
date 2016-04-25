package com.gaborbiro.marveldemo;

import android.app.Application;
import android.content.Context;

import com.gaborbiro.marveldemo.provider.api.MarvelApiModule;

public class App extends Application {

    private com.gaborbiro.marveldemo.AppComponent mAppComponent;

    private static Context sAppContext;

    @Override public void onCreate() {
        super.onCreate();
        sAppContext = this;

		/* DaggerAppComponent */
        mAppComponent = DaggerAppComponent.builder()
                .marvelApiModule(new MarvelApiModule())
                .build();
    }

    public static Context getAppContext() {
        return sAppContext;
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }
}

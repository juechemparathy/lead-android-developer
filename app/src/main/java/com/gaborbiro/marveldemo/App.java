package com.gaborbiro.marveldemo;

import android.app.Application;
import android.content.Context;

import com.gaborbiro.marveldemo.provider.dropbox.DropboxApiModule;

public class App extends Application {

    private AppComponent mAppComponent;

    private static Context sAppContext;

    @Override public void onCreate() {
        super.onCreate();
        sAppContext = this;

        mAppComponent = DaggerAppComponent.builder()
                .dropboxApiModule(new DropboxApiModule())
                .build();
    }

    public static Context getAppContext() {
        return sAppContext;
    }

    public AppComponent getDropboxApiComponent() {
        return mAppComponent;
    }
}

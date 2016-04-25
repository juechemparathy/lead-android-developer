package com.gaborbiro.marveldemo;

import com.gaborbiro.marveldemo.provider.api.MarvelApiModule;
import com.gaborbiro.marveldemo.ui.ComicListActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton @Component(modules = {MarvelApiModule.class}) public interface AppComponent {

    void inject(ComicListActivity comicListActivity);
}

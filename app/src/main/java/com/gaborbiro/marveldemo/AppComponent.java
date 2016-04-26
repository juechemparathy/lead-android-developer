package com.gaborbiro.marveldemo;

import com.gaborbiro.marveldemo.provider.api.MarvelApiModule;
import com.gaborbiro.marveldemo.provider.dropbox.DropboxApiModule;
import com.gaborbiro.marveldemo.ui.ComicDetailActivity;
import com.gaborbiro.marveldemo.ui.ComicListActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton @Component(modules = {DropboxApiModule.class, MarvelApiModule.class})
public interface AppComponent {
    void inject(ComicDetailActivity comicDetailActivity);

    void inject(ComicListActivity comicListActivity);
}

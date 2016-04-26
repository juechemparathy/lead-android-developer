package com.gaborbiro.marveldemo.provider.dropbox;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxDatastoreManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFileSystem;
import com.gaborbiro.marveldemo.App;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module public class DropboxApiModule {

    private static final String APP_KEY = "6wkhhsbqoxta9mq";
    private static final String APP_SECRET = "osyxphd50gn14k2";

    @Provides @Singleton DbxAccountManager provideAccountManager() {
        return DbxAccountManager.getInstance(App.getAppContext(), APP_KEY, APP_SECRET);
    }

    @Provides @Singleton DbxDatastore provideDatastore(
            Provider<DbxAccountManager> accountManagerProvider) {
        try {
            DbxDatastoreManager datastoreManager = DbxDatastoreManager.forAccount(
                    accountManagerProvider.get()
                            .getLinkedAccount());
            return datastoreManager.openDefaultDatastore();
        } catch (DbxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Provides @Singleton DropboxApi provideDropboxApi(
            Provider<DbxDatastore> datastoreProvider,
            Provider<DbxFileSystem> fileSystemProvider) {
        return new DropboxApiImpl(datastoreProvider, fileSystemProvider);
    }

    @Provides DbxFileSystem provideFileSystem(
            Provider<DbxAccountManager> accountManagerProvider) {
        try {
            return DbxFileSystem.forAccount(accountManagerProvider.get()
                    .getLinkedAccount());
        } catch (DbxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Provides DropboxDownloader provideDropboxDownloader(
            Provider<DbxFileSystem> fileSystemProvider) {
        return new DropboxDownloader(fileSystemProvider);
    }
}

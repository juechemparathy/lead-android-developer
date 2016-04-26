package com.gaborbiro.marveldemo.provider.dropbox;

import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;

import java.io.File;
import java.io.IOException;

import javax.inject.Provider;

public class DropboxApiImpl implements DropboxApi {

    private Provider<DbxDatastore> mDatastoreProvider;
    private Provider<DbxFileSystem> mFileSystemProvider;

    public DropboxApiImpl(Provider<DbxDatastore> datastoreProvider,
            Provider<DbxFileSystem> fileSystemProvider) {
        mDatastoreProvider = datastoreProvider;
        mFileSystemProvider = fileSystemProvider;
    }

    @Override public void setCover(int comicId, String coverPath) throws DbxException {
        DbxDatastore datastore = mDatastoreProvider.get();
        DbxTable table = datastore.getTable("user-settings");
        DbxRecord map = table.getOrInsert("cover-map");
        map.set(Integer.toString(comicId), coverPath);
        datastore.sync();
    }

    @Override public String getCover(int comicId) throws DbxException {
        DbxDatastore datastore = mDatastoreProvider.get();
        datastore.sync();
        DbxTable table = datastore.getTable("user-settings");
        DbxRecord map = table.getOrInsert("cover-map");
        if (map.hasField(Integer.toString(comicId))) {
            return map.getString(Integer.toString(comicId));
        } else {
            return null;
        }
    }

    @Override public void uploadFile(String fileName) {
        try {
            DbxFileSystem fileSystem = mFileSystemProvider.get();
            File photoFile = new File(fileName);
            DbxPath dbPath =
                    new DbxPath(DbxPath.ROOT.getChild(DropboxUtils.COVER_BACKUP_FOLDER), photoFile.getName());

            DbxFile testFile = fileSystem.create(dbPath);
            try {
                testFile.writeFromExistingFile(photoFile, false);
            } finally {
                testFile.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

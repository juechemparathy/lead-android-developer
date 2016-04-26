package com.gaborbiro.marveldemo.provider.dropbox;

import android.graphics.Bitmap;

import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;
import com.gaborbiro.marveldemo.util.FileUtils;

import java.io.IOException;
import java.io.OutputStream;

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

    @Override public boolean hasCover(int comicId) throws DbxException {
        DbxDatastore datastore = mDatastoreProvider.get();
        datastore.sync();
        DbxTable table = datastore.getTable("user-settings");
        DbxRecord map = table.get("cover-map");
        return map != null && map.hasField(Integer.toString(comicId));
    }

    @Override public void removeCover(int comicId) throws DbxException {
        DbxDatastore datastore = mDatastoreProvider.get();
        DbxTable table = datastore.getTable("user-settings");
        DbxRecord map = table.getOrInsert("cover-map");
        map.deleteField(Integer.toString(comicId));
        datastore.sync();
    }

    @Override public String uploadCoverPhoto(Bitmap bitmap) {
        try {
            String fileName = FileUtils.getNewCoverPhotoName();
            DbxFileSystem fileSystem = mFileSystemProvider.get();
            DbxPath dbPath =
                    new DbxPath(DbxPath.ROOT.getChild(DropboxUtils.COVER_BACKUP_FOLDER),
                            fileName);

            DbxFile testFile = fileSystem.create(dbPath);
            OutputStream out = testFile.getWriteStream();
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            } finally {
                out.close();
                testFile.close();
            }
            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override public void deleteCoverPhoto(String name) {
        DbxFileSystem fileSystem = mFileSystemProvider.get();
        DbxPath dbPath =
                new DbxPath(DbxPath.ROOT.getChild(DropboxUtils.COVER_BACKUP_FOLDER),
                        name);
        try {
            fileSystem.delete(dbPath);
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }
}

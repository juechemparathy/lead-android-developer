package com.gaborbiro.marveldemo.provider.dropbox;

import android.graphics.Bitmap;
import android.net.Uri;

import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.gaborbiro.marveldemo.util.BitmapUtils;
import com.squareup.picasso.Downloader;

import java.io.FileInputStream;
import java.io.IOException;

import javax.inject.Provider;

public class DropboxDownloader implements Downloader {

    private Provider<DbxFileSystem> mFileSystemProvider;

    public DropboxDownloader(Provider<DbxFileSystem> fileSystemProvider) {
        mFileSystemProvider = fileSystemProvider;
    }

    @Override public Response load(Uri uri, int networkPolicy) throws IOException {
        DbxPath dbPath =
                new DbxPath(DbxPath.ROOT.getChild(DropboxUtils.COVER_BACKUP_FOLDER),
                        uri.toString()
                                .replace("http:\\", ""));
        DbxFile file = null;
        Bitmap bitmap = null;
        FileInputStream fis = null;
        try {
            file = mFileSystemProvider.get()
                    .open(dbPath);
            fis = file.getReadStream();
            bitmap = BitmapUtils.decodeStream(fis, 10);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (file != null) {
                file.close();
            }
        }
        return new Response(bitmap, false, -1);
    }

    @Override public void shutdown() {
    }
}

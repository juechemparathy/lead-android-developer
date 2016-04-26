package com.gaborbiro.marveldemo.provider.dropbox;

import android.graphics.Bitmap;

import com.dropbox.sync.android.DbxException;

public interface DropboxApi {
    void setCover(int comicId, String coverPath) throws DbxException;
    String getCover(int comicId) throws DbxException;
    boolean hasCover(int comicId) throws DbxException;
    void removeCover(int comicId) throws DbxException;

    String uploadCoverPhoto(Bitmap bitmap);
    void deleteCoverPhoto(String name);
}

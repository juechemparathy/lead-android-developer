package com.gaborbiro.marveldemo.provider.dropbox;

import com.dropbox.sync.android.DbxException;

public interface DropboxApi {
    void setCover(int comicId, String coverPath) throws DbxException;
    String getCover(int comicId) throws DbxException;

    void uploadFile(String fileName);
}

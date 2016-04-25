package com.gaborbiro.marveldemo.provider.api;

public class ComicsFetchingException extends Exception {

    public ComicsFetchingException(Throwable t) {
        super("Error fetching comics from Marvel", t);
    }

    public ComicsFetchingException(String detailMessage) {
        super(detailMessage);
    }

    public ComicsFetchingException(String detailMessage, Throwable t) {
        super(detailMessage, t);
    }
}

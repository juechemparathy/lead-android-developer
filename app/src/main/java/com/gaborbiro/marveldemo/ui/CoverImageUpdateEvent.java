package com.gaborbiro.marveldemo.ui;

/**
 * Used with EventBus. The ComicDetailActivity is sending it to ComicListActivity,
 * whenever a cover image has been updated.
 */
public class CoverImageUpdateEvent {
    public int mPosition;

    public CoverImageUpdateEvent(int mPosition) {
        this.mPosition = mPosition;
    }
}

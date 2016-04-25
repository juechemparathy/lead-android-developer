package com.gaborbiro.marveldemo.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;

import com.jakewharton.disklrucache.DiskLruCache;

public class BitmapCache {

	public interface Listener {

		void onBitmapLoaded(String key, Bitmap bitmap);

		void onBitmapLoadingFailed(String key, Exception e);
	}

	private DiskLruCache mDiskLruCache;
	private final Object mDiskCacheLock = new Object();
	private boolean mDiskCacheStarting = true;
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
	private static final String DISK_CACHE_SUBDIR = "bitmaps";

	private BitmapCache(Context context) {
		File cacheDir = getDiskCacheDir(context, DISK_CACHE_SUBDIR);
		new InitDiskCacheTask().execute(cacheDir);
	}

	public static BitmapCache attach(Context context) {
		return new BitmapCache(context);
	}

	class InitDiskCacheTask extends AsyncTask<File, Void, Void> {

		@Override
		protected Void doInBackground(File... params) {
			synchronized (mDiskCacheLock) {
				File cacheDir = params[0];
				try {
					mDiskLruCache = DiskLruCache.open(cacheDir, 0, 1, DISK_CACHE_SIZE);
				} catch (IOException e) {
					// null-check mDiskLruCache!
				}
				mDiskCacheStarting = false; // Finished initialization
				mDiskCacheLock.notifyAll(); // Wake any waiting threads
			}
			return null;
		}
	}

	public void loadBitmap(String key, Listener listener) {
		if (key == null) {
			throw new NullPointerException("Key must not be null");
		}
		if (listener == null) {
			throw new NullPointerException("Listener must not be null");
		}
		new BitmapSyncTask(key, SyncMode.READ, null, listener).execute();
	}

	public void saveBitmap(String key, Bitmap bitmap) {
		if (key == null) {
			throw new NullPointerException("Key must not be null");
		}
		if (bitmap == null) {
			throw new NullPointerException("Bitmap must not be null");
		}
		new BitmapSyncTask(key, SyncMode.WRITE, bitmap, null).execute();
	}

	public void deleteBitmap(String key) {
		if (key == null) {
			throw new NullPointerException("Key must not be null");
		}
		new BitmapSyncTask(key, SyncMode.DELETE, null, null).execute();
	}

	public void clearCache() {
		new BitmapSyncTask(null, SyncMode.CLEAR, null, null).execute();
	}

	private enum SyncMode {
		READ, WRITE, DELETE, CLEAR;
	}

	private class BitmapSyncTask extends AsyncTask<Void, Void, Bitmap> {

		private SyncMode mSyncMode;
		private String mKey;
		private Bitmap mBitmap;
		private Listener mListener;
		private IOException e;

		private BitmapSyncTask(String key, SyncMode syncMode, Bitmap bitmap, Listener listener) {
			mSyncMode = syncMode;
			mKey = key;
			mBitmap = bitmap;
			mListener = listener;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			synchronized (mDiskCacheLock) {
				// Wait while disk cache is started from background thread
				while (mDiskCacheStarting) {
					try {
						mDiskCacheLock.wait();
					} catch (InterruptedException e) {
					}
				}
				if (mDiskLruCache != null) {
					try {
						switch (mSyncMode) {
						case READ:
							return readBitmapFromDiskCacheBlocking(mKey);
						case WRITE:
							writeBitmapToDiskCacheBlocking(mKey, mBitmap);
							break;
						case DELETE:
							deleteBitmapFromDiskCacheBlocking(mKey);
							break;
						case CLEAR:
							deleteAllFromDiskCacheBlocking();
							break;
						}
					} catch (IOException e) {
						this.e = e;
					}
					return null;
				} else {
					return null;
				}
			}
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			switch (mSyncMode) {
			case READ:
				if (e != null) {
					mListener.onBitmapLoadingFailed(mKey, e);
				} else if (bitmap == null) {
					mListener.onBitmapLoadingFailed(mKey, null);
				} else {
					mListener.onBitmapLoaded(mKey, bitmap);
				}
				break;
			case WRITE:
				// nothing to do
				break;
			case DELETE:
				// nothing to do
				break;
			}
		}
	}

	private Bitmap readBitmapFromDiskCacheBlocking(String key) throws IOException {
		DiskLruCache.Snapshot snapthot = mDiskLruCache.get(key);
		if (snapthot != null) {
			InputStream in = snapthot.getInputStream(0);
			return BitmapFactory.decodeStream(new BufferedInputStream(in));
		} else {
			return null;
		}
	}

	private void writeBitmapToDiskCacheBlocking(String key, Bitmap bitmap) throws IOException {
		DiskLruCache.Editor editor = mDiskLruCache.edit(key);
		OutputStream out = editor.newOutputStream(0);
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
		out.flush();
		editor.commit();
	}

	private boolean deleteBitmapFromDiskCacheBlocking(String key) throws IOException {
		return mDiskLruCache.remove(key);
	}

	private void deleteAllFromDiskCacheBlocking() throws IOException {
		mDiskLruCache.delete();
	}

	/**
	 * Creates a unique subdirectory of the designated app cache directory. Tries to use external but if not mounted,
	 * falls back on internal storage.
	 */
	private static File getDiskCacheDir(Context context, String uniqueName) {
		// Check if media is mounted or storage is built-in, if so, try and use external cache dir
		// otherwise use internal cache dir
		final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState()) || !Environment.isExternalStorageRemovable() ? context
				.getExternalCacheDir().getPath() : context.getCacheDir().getPath();

		return new File(cachePath + File.separator + uniqueName);
	}
}

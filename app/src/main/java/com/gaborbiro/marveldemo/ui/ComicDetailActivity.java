package com.gaborbiro.marveldemo.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dropbox.sync.android.DbxException;
import com.gaborbiro.marveldemo.App;
import com.gaborbiro.marveldemo.R;
import com.gaborbiro.marveldemo.provider.api.model.Comic;
import com.gaborbiro.marveldemo.provider.dropbox.DropboxApi;
import com.gaborbiro.marveldemo.provider.dropbox.DropboxDownloader;
import com.gaborbiro.marveldemo.util.BitmapUtils;
import com.gaborbiro.marveldemo.util.FileUtils;
import com.gaborbiro.marveldemo.util.IntentUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

/**
 * An activity representing a single Comic detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ComicListActivity}.
 */
public class ComicDetailActivity extends AppCompatActivity
        implements View.OnClickListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    /**
     * Bundle argument representing the list-position of the Comic represented by this
     * ComicDetailActivity
     */
    public static final String ARG_POSITION = "position";

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private ImageView mActionBarBackdropImage;
    private ImageView mCameraButton;
    private ImageView mDeleteButton;
    private ProgressBar mProgressBar;

    private Comic mComic;
    private String mCurrentPhotoPath;

    @Inject public DropboxApi mDropboxApi;
    @Inject public DropboxDownloader mDropboxDownloader;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_detail);
        ((App) getApplication()).getDropboxApiComponent()
                .inject(this);
        mActionBarBackdropImage = (ImageView) findViewById(R.id.cover);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mCollapsingToolbarLayout =
                (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        mCollapsingToolbarLayout.setExpandedTitleColor(
                getResources().getColor(android.R.color.transparent));
        mCameraButton = (ImageView) findViewById(R.id.camera);
        mCameraButton.setOnClickListener(this);
        mDeleteButton = (ImageView) findViewById(R.id.delete);
        mDeleteButton.setOnClickListener(this);
        mProgressBar = (ProgressBar) findViewById(android.R.id.progress);

        mComic = getIntent().getParcelableExtra(ComicDetailFragment.ARG_ITEM);

        loadCoverImage();
        updateDeleteButton();

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(ComicDetailFragment.ARG_ITEM, mComic);
            ComicDetailFragment fragment = new ComicDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.comic_detail_container, fragment)
                    .commit();
        }
    }

    private void loadCoverImage() {
        PicassoHandler handler = new PicassoHandler(mComic);
        Picasso.Builder builder =
                new Picasso.Builder(App.getAppContext()).listener(handler);
        RequestCreator requestCreator;
        try {
            String path = mDropboxApi.getCover(mComic.id);

            if (!TextUtils.isEmpty(path)) {
                builder.downloader(mDropboxDownloader);
                // The http bit is an ugly hack to make Picasso believe this can be
                // handled by a NetworkRequestHandler. This way I can use a simple
                // Downloader instead of needing to write a whole RequestHandler.
                requestCreator = builder.build()
                        .load(Uri.parse("http:\\" + path));
            } else {
                requestCreator = builder.build()
                        .load(mComic.getCoverImageUri());
            }
        } catch (DbxException e) {
            e.printStackTrace();
            requestCreator = builder.build()
                    .load(mComic.getCoverImageUri());
        }
        requestCreator.into(handler);
    }

    private void updateDeleteButton() {
        try {
            mDeleteButton.setVisibility(
                    mDropboxApi.hasCover(mComic.id) ? View.VISIBLE : View.INVISIBLE);
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    private class PicassoHandler implements Target, Picasso.Listener {

        private Comic mComic;

        public PicassoHandler(Comic mComic) {
            this.mComic = mComic;
        }

        @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            if (mActionBarBackdropImage != null) {
                mProgressBar.setVisibility(View.GONE);
                mActionBarBackdropImage.setImageBitmap(bitmap);
                applyPalette(bitmap);
                notifyParentList();
                updateDeleteButton();
            }
        }

        @Override public void onBitmapFailed(Drawable errorDrawable) {
            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
            String message;
            if (exception instanceof DbxException.DiskSpace) {
                message = "Disk space error. Free up some space on your device.";
            } else if (exception instanceof DbxException.Network ||
                    exception instanceof DbxException.NetworkConnection ||
                    exception instanceof DbxException.NetworkTimeout) {
                message = "Networking error. Please verify your internet connection.";
            } else if (exception instanceof DbxException.Quota) {
                message = "Dropbox traffic limit reached. Please try again tomorrow.";
            } else if (exception instanceof DbxException.Ssl ||
                    exception instanceof DbxException.Unauthorized) {
                message = "Server authorization error. Please try again later.";
            } else if (exception instanceof DbxException.NotFound) {
                message =
                        "The cover has been deleted from dropbox. Resetting the default" +
                                " cover.";
                try {
                    mDropboxApi.removeCover(mComic.id);
                    loadCoverImage();
                    updateDeleteButton();
                    notifyParentList();
                } catch (DbxException e) {
                    e.printStackTrace();
                }
            } else {
                message = "Error: " + exception.getClass()
                        .getSimpleName();
            }
            Toast.makeText(ComicDetailActivity.this, message, Toast.LENGTH_LONG)
                    .show();
        }

        @Override public void onPrepareLoad(Drawable placeHolderDrawable) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void applyPalette(Bitmap bitmap) {
        Palette.from(bitmap)
                .generate(new Palette.PaletteAsyncListener() {

                    @Override public void onGenerated(Palette palette) {
                        int primaryDark =
                                getResources().getColor(R.color.colorPrimaryDark);
                        int primary = getResources().getColor(R.color.colorPrimary);
                        mCollapsingToolbarLayout.setContentScrimColor(
                                palette.getMutedColor(primary));
                        mCollapsingToolbarLayout.setStatusBarScrimColor(
                                palette.getDarkMutedColor(primaryDark));
                        int vibrantColor = palette.getVibrantColor(primary);
                        int negative = Color.rgb(255 - Color.red(vibrantColor),
                                255 - Color.green(vibrantColor),
                                255 - Color.blue(vibrantColor));
                        mCameraButton.setColorFilter(negative);
                        mDeleteButton.setColorFilter(negative);
                        supportStartPostponedEnterTransition();
                    }
                });
    }

    @Override public void onBackPressed() {
        if (!getSupportFragmentManager().popBackStackImmediate()) {
            finish();
        }
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onClick(View v) {
        if (v.getId() == R.id.camera) {
            try {
                File targetFile = FileUtils.createTempJpeg();
                mCurrentPhotoPath = targetFile.getAbsolutePath();
                Intent i = IntentUtils.generateTakePictureIntent(targetFile);
                if (i != null) {
                    startActivityForResult(i, REQUEST_IMAGE_CAPTURE);
                } else {
                    Toast.makeText(ComicDetailActivity.this, "No camera app detected",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (v.getId() == R.id.delete) {
            try {
                mDropboxApi.deleteCoverPhoto(mDropboxApi.getCover(mComic.id));
                mDropboxApi.removeCover(mComic.id);
                loadCoverImage();
                notifyParentList();
                updateDeleteButton();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = BitmapUtils.getBitmapFromFile(mCurrentPhotoPath,
                        mActionBarBackdropImage.getWidth(),
                        mActionBarBackdropImage.getHeight());
                bitmap = BitmapUtils.flip(bitmap);
                mActionBarBackdropImage.setImageBitmap(bitmap);
                saveImageToDropbox(bitmap);
                new File(mCurrentPhotoPath).delete();
                mCurrentPhotoPath = null;
                notifyParentList();
                updateDeleteButton();
            }
        }
    }

    private void notifyParentList() {
        EventBus.getDefault()
                .post(new CoverImageUpdateEvent(
                        getIntent().getIntExtra(ARG_POSITION, -1)));
    }

    private void saveImageToDropbox(Bitmap bitmap) {
        try {
            String name = mDropboxApi.uploadCoverPhoto(bitmap);
            mDropboxApi.setCover(mComic.id, name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

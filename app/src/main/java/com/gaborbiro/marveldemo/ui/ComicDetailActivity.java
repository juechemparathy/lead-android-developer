package com.gaborbiro.marveldemo.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

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
     * The cache key of the thumbnail bitmap of the selected comic. Used to display a
     * placeholder cover really fast, until the high res cover is fetched
     */
    public static final String ARG_SELECTED_THUMB_CACHE_KEY = "selected_thumb_cache_key";

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private ImageView mActionBarBackdropImage;
    private ImageView mCameraView;

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
        mCameraView = (ImageView) findViewById(R.id.camera);
        mCameraView.setOnClickListener(this);

        mComic = getIntent().getParcelableExtra(ComicDetailFragment.ARG_ITEM);

        Picasso.Builder builder = new Picasso.Builder(App.getAppContext());
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
        requestCreator.into(mBitmapLoadedHandler);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle arguments = new Bundle();
            arguments.putParcelable(ComicDetailFragment.ARG_ITEM, mComic);
            ComicDetailFragment fragment = new ComicDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.comic_detail_container, fragment)
                    .commit();
        }
        //        postponeEnterTransition();
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

    private Target mBitmapLoadedHandler = new Target() {

        @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            if (mActionBarBackdropImage != null) {
                mActionBarBackdropImage.setImageBitmap(bitmap);
                applyPalette(bitmap);
            }
            Log.d("test", "cover loaded");
        }

        @Override public void onBitmapFailed(Drawable errorDrawable) {
            Log.d("test", "error loading cover");
        }

        @Override public void onPrepareLoad(Drawable placeHolderDrawable) {
            Log.d("test", "prepare loading cover");
        }
    };

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
                        mCameraView.setColorFilter(palette.getVibrantColor(primary));
                        supportStartPostponedEnterTransition();
                    }
                });
    }

    @Override public void onClick(View v) {
        if (v.getId() == R.id.camera) {
            try {
                File targetFile = FileUtils.createTempJpeg(Integer.toString(mComic.id));
                mCurrentPhotoPath = targetFile.getAbsolutePath();
                Intent i = IntentUtils.generateTakePictureIntent(targetFile);
                startActivityForResult(i, REQUEST_IMAGE_CAPTURE);
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
                mActionBarBackdropImage.setImageBitmap(bitmap);
                saveImageToDropbox();
            }
        }
    }

    private void saveImageToDropbox() {
        try {
            mDropboxApi.uploadFile(mCurrentPhotoPath);
            mDropboxApi.setCover(mComic.id, new File(mCurrentPhotoPath).getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

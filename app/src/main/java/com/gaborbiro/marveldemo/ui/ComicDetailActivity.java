package com.gaborbiro.marveldemo.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.gaborbiro.marveldemo.R;
import com.gaborbiro.marveldemo.util.BitmapCache;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * An activity representing a single Comic detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ComicListActivity}.
 */
public class ComicDetailActivity extends AppCompatActivity
        implements ComicDetailFragment.Listener, View.OnClickListener {

    /**
     * The cache key of the thumbnail bitmap of the selected comic. Used to display a
     * placeholder cover really fast, until the high res cover is fetched
     */
    public static final String ARG_SELECTED_THUMB_CACHE_KEY = "selected_thumb_cache_key";

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private ImageView mActionBarBackdropImage;
    private ImageView mCameraView;

    private boolean mHighResCoverImageLoaded = false;

    private BitmapCache mBitmapCache;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_detail);
        mBitmapCache = BitmapCache.attach(getApplicationContext());
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
        mCameraView = (ImageView) findViewById(R.id.camera);
        mCameraView.setOnClickListener(this);
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
            arguments.putParcelable(ComicDetailFragment.ARG_ITEM,
                    getIntent().getParcelableExtra(ComicDetailFragment.ARG_ITEM));
            if (getIntent().hasExtra(ARG_SELECTED_THUMB_CACHE_KEY)) {
                String cacheKey =
                        getIntent().getStringExtra(ARG_SELECTED_THUMB_CACHE_KEY);
                mBitmapCache.loadBitmap(cacheKey, mThumbnailCacheLoaderListener);
            }

            ComicDetailFragment fragment = new ComicDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.comic_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (!getSupportFragmentManager().popBackStackImmediate()) {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private BitmapCache.Listener mThumbnailCacheLoaderListener =
            new BitmapCache.Listener() {

                @Override public void onBitmapLoaded(String key, Bitmap bitmap) {
                    if (bitmap != null && !mHighResCoverImageLoaded) {
                        mActionBarBackdropImage.setImageBitmap(bitmap);
                        mBitmapCache.deleteBitmap(key);
                        applyPalette(bitmap);
                    }
                }

                @Override public void onBitmapLoadingFailed(String key, Exception e) {
                    // nothing to do, must wait until the cover image is loaded
                }
            };

    @Override public void onActionBarBackdropImageRequested(String url) {
        if (!TextUtils.isEmpty(url)) {
            Picasso.with(this)
                    .load(url)
                    .into(mBitmapLoadedHandler);
        }
    }

    private Target mBitmapLoadedHandler = new Target() {

        @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mHighResCoverImageLoaded = true;
            mActionBarBackdropImage.setImageBitmap(bitmap);
            applyPalette(bitmap);
        }

        @Override public void onBitmapFailed(Drawable errorDrawable) {
        }

        @Override public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };

    private void applyPalette(Bitmap bitmap) {
        Palette.from(bitmap)
                .generate(new Palette.PaletteAsyncListener() {

                    public void onGenerated(Palette palette) {
                        int primaryDark =
                                getResources().getColor(R.color.colorPrimaryDark);
                        int primary = getResources().getColor(R.color.colorPrimary);
                        mCollapsingToolbarLayout.setContentScrimColor(
                                palette.getMutedColor(primary));
                        mCollapsingToolbarLayout.setStatusBarScrimColor(
                                palette.getDarkMutedColor(primaryDark));
                        supportStartPostponedEnterTransition();
                    }
                });
    }

    @Override public void onClick(View v) {
        if (v.getId() == R.id.camera) {
            Toast.makeText(ComicDetailActivity.this, "Click", Toast.LENGTH_SHORT)
                    .show();
        }
    }
}

package com.gaborbiro.marveldemo.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.gaborbiro.marveldemo.App;
import com.gaborbiro.marveldemo.R;
import com.gaborbiro.marveldemo.provider.api.ComicsFetchingException;
import com.gaborbiro.marveldemo.provider.api.MarvelApi;
import com.gaborbiro.marveldemo.provider.api.model.Comic;
import com.gaborbiro.marveldemo.provider.api.model.Comics;
import com.gaborbiro.marveldemo.provider.dropbox.DropboxApi;
import com.gaborbiro.marveldemo.provider.dropbox.DropboxDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * An activity representing a list of Comics. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ComicDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ComicListActivity extends AppCompatActivity {

    public static final String KEY_CACHE_SELECTED_THUMB = "selected_thumb";

    private static final int REQUEST_LINK_TO_DBX = 1;

    private static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private int mSelectedPosition = -1;

    @Inject public MarvelApi mMarvelApi;
    @Inject public DbxAccountManager mAccountManager;
    @Inject public DropboxApi mDropboxApi;
    @Inject public DropboxDownloader mDropboxDownloader;

    private RecyclerView mList;
    private EndlessRecyclerViewScrollListener mScrollListener;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_list);
        ((App) getApplication()).getDropboxApiComponent()
                .inject(this);
        EventBus.getDefault()
                .register(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setTitle(getTitle());
        }

        mList = (RecyclerView) findViewById(R.id.comic_list);
        assert mList != null;

        mList.setAdapter(new ComicsAdapter(new Comic[0]));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mList.setLayoutManager(linearLayoutManager);
        mScrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {

            @Override public void onLoadMore(int page, int totalItemsCount) {
                loadPage(page + 1);
            }
        };
        mList.addOnScrollListener(mScrollListener);

        if (findViewById(R.id.comic_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w400dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        if (!mAccountManager.hasLinkedAccount()) {
            mAccountManager.startLink(this, REQUEST_LINK_TO_DBX);
        } else {
            setupUI(savedInstanceState);
        }
    }

    private void setupUI(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            loadPage(0);
        } else {
            // configuration change
            SavedState savedState = SavedState.load(savedInstanceState);

            if (savedState != null) {
                mSelectedPosition = savedState.selectedPosition;

                if (savedState.comics != null) {
                    mList.setAdapter(new ComicsAdapter(savedState.comics));
                    mList.getLayoutManager()
                            .scrollToPosition(savedState.scrollPosition);
                } else {
                    loadPage(0);
                }
            } else {
                loadPage(0);
            }
        }
    }

    private void loadPage(int page) {
        try {
            ((ComicsAdapter) mList.getAdapter()).setProgressIndicatorVisibility(true);
            mMarvelApi.getComics(page, DEFAULT_PAGE_SIZE, retrofitCallback);
        } catch (IOException | ComicsFetchingException e) {
            e.printStackTrace();
            Toast.makeText(ComicListActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LINK_TO_DBX) {
            if (resultCode == RESULT_OK) {
                setupUI(null);
            } else {
                Toast.makeText(this, "Error linking to dropbox", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int firstVisibleItemPosition =
                ((LinearLayoutManager) mList.getLayoutManager())
                        .findFirstCompletelyVisibleItemPosition();

        Comic[] comics = null;

        if (mList != null && mList.getAdapter() != null) {
            comics = ((ComicsAdapter) mList.getAdapter()).getComics();
        }
        new SavedState(firstVisibleItemPosition, comics, mSelectedPosition).save(
                outState);
    }

    private static class SavedState implements Parcelable {

        private static final String PARCEL_SAVED_STATE = "parcel_saved_state";

        int scrollPosition;
        Comic[] comics;
        int selectedPosition;

        public SavedState(int scrollPosition, Comic[] comics, int selectedPosition) {
            this.scrollPosition = scrollPosition;
            this.comics = comics;
            this.selectedPosition = selectedPosition;
        }

        SavedState(Parcel in) {
            scrollPosition = in.readInt();
            comics = in.createTypedArray(Comic.CREATOR);
            selectedPosition = in.readInt();
        }

        @Override public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(scrollPosition);
            dest.writeTypedArray(comics, 0);
            dest.writeInt(selectedPosition);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

                    @Override public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        @Override public int describeContents() {
            return 0;
        }

        public void save(Bundle outState) {
            outState.putParcelable(PARCEL_SAVED_STATE, this);
        }

        public static @Nullable SavedState load(Bundle savedInstanceState) {
            return savedInstanceState.getParcelable(PARCEL_SAVED_STATE);
        }
    }

    @Subscribe public void onEvent(CoverImageUpdateEvent event) {
        if (event.mPosition > -1 && event.mPosition < mList.getAdapter()
                .getItemCount()) {
            mList.getAdapter()
                    .notifyItemChanged(event.mPosition);
        }
    }

    private void loadThumbImage(ImageView target, Comic comic, int placeholderResId) {
        Picasso.Builder builder = new Picasso.Builder(App.getAppContext());
        RequestCreator requestCreator;
        target.setImageResource(placeholderResId);
        try {
            String path = mDropboxApi.getCover(comic.id);

            if (!TextUtils.isEmpty(path)) {
                builder.downloader(mDropboxDownloader);
                // The http bit is an ugly hack to make Picasso believe this can be
                // handled by a NetworkRequestHandler. This way I can use a simple
                // Downloader instead of needing to write a whole RequestHandler.
                requestCreator = builder.build()
                        .load(Uri.parse("http:\\" + path));
            } else {
                requestCreator = builder.build()
                        .load(comic.getThumbImageUri());
            }
        } catch (DbxException e) {
            e.printStackTrace();
            requestCreator = builder.build()
                    .load(comic.getThumbImageUri());
        }
        requestCreator.placeholder(placeholderResId)
                .into(target);
    }

    public class ComicsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_COMIC = 1;
        private static final int VIEW_TYPE_PROGRESS = 2;

        private Comic[] mComics;
        private boolean mProgressIndicatorVisible;

        public ComicsAdapter(Comic[] items) {
            mComics = items;
        }

        public Comic[] getComics() {
            return mComics;
        }

        @Override public int getItemCount() {
            return mComics.length + 1;
        }

        @Override public int getItemViewType(int position) {
            return position < mComics.length ? VIEW_TYPE_COMIC : VIEW_TYPE_PROGRESS;
        }

        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                int viewType) {
            View view;

            switch (viewType) {
                case VIEW_TYPE_COMIC:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.comic_list_content, parent, false);
                    return new ComicViewHolder(view);
                case VIEW_TYPE_PROGRESS:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.comic_list_progress, parent, false);
                    return new ProgressViewHolder(view);
            }
            return null;
        }

        public void addItems(Comic[] moreItems) {
            mProgressIndicatorVisible = false;
            mComics = Arrays.copyOf(mComics, mComics.length + moreItems.length);
            System.arraycopy(moreItems, 0, mComics, mComics.length - moreItems.length,
                    moreItems.length);
        }

        public void setProgressIndicatorVisibility(boolean visible) {
            mProgressIndicatorVisible = visible;
            notifyItemChanged(mComics.length);
        }

        @Override public void onBindViewHolder(final RecyclerView.ViewHolder holder,
                final int position) {
            if (position < mComics.length) {
                final ComicViewHolder comicViewHolder = (ComicViewHolder) holder;
                Comic comic = mComics[position];
                loadThumbImage(comicViewHolder.mThumbView, comic,
                        R.drawable.ic_book_white_48dp);

                if (!TextUtils.isEmpty(comic.title)) {
                    comicViewHolder.mTitleView.setText(comic.title);
                    comicViewHolder.mTitleView.setVisibility(View.VISIBLE);
                } else {
                    comicViewHolder.mTitleView.setVisibility(View.GONE);
                }
                comicViewHolder.mItem = comic;
                comicViewHolder.mView.setOnClickListener(new View.OnClickListener() {

                    @Override public void onClick(View v) {
                        Comic comic = comicViewHolder.mItem;
                        if (mTwoPane) {
                            if (position != mSelectedPosition) {
                                v.setSelected(true);
                                int oldSelectedPosition = mSelectedPosition;
                                mSelectedPosition = position;
                                // unselect the old selection
                                notifyItemChanged(oldSelectedPosition);

                                Bundle arguments = new Bundle();
                                arguments.putParcelable(ComicDetailFragment.ARG_ITEM,
                                        comic);
                                ComicDetailFragment fragment = new ComicDetailFragment();
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.comic_detail_container, fragment)
                                        .commit();
                            }
                        } else {
                            startDetailActivity(comic, position,
                                    comicViewHolder.mThumbView);
                        }
                    }
                });
                comicViewHolder.mView.setSelected(
                        mTwoPane && mSelectedPosition == position);
            } else {
                ProgressViewHolder progressViewHolder = (ProgressViewHolder) holder;
                progressViewHolder.mProgressBar.setVisibility(
                        mProgressIndicatorVisible ? View.VISIBLE : View.GONE);
            }
        }

        private void startDetailActivity(Comic comic, int position, ImageView thumbView) {
            Intent intent = new Intent(ComicListActivity.this, ComicDetailActivity.class);
            intent.putExtra(ComicDetailFragment.ARG_ITEM, comic);
            intent.putExtra(ComicDetailActivity.ARG_SELECTED_THUMB_CACHE_KEY,
                    KEY_CACHE_SELECTED_THUMB);
            intent.putExtra(ComicDetailActivity.ARG_POSITION, position);

            if (android.os.Build.VERSION.SDK_INT >=
                    android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                View decor = getWindow().getDecorView();
                View navBar = decor.findViewById(android.R.id.navigationBarBackground);
                Pair<View, String> p1 = Pair.create((View) thumbView, "thumb");
                Pair<View, String> p2 = Pair.create(navBar, "navBar");
                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                                ComicListActivity.this, p1, p2);
                startActivity(intent, options.toBundle());
            } else {
                startActivity(intent);
            }
        }

        public class ComicViewHolder extends RecyclerView.ViewHolder {

            public final View mView;
            public final ImageView mThumbView;
            public final TextView mTitleView;
            public Comic mItem;

            public ComicViewHolder(View view) {
                super(view);
                mView = view;
                mThumbView = (ImageView) view.findViewById(R.id.cover);
                mTitleView = (TextView) view.findViewById(R.id.title);
            }

            @Override public String toString() {
                return super.toString() + " '" + mTitleView.getText() + "'";
            }
        }

        public class ProgressViewHolder extends RecyclerView.ViewHolder {

            public final ProgressBar mProgressBar;

            public ProgressViewHolder(View view) {
                super(view);
                mProgressBar = (ProgressBar) view.findViewById(android.R.id.progress);
            }
        }
    }

    private Callback<Comics> retrofitCallback = new Callback<Comics>() {
        @Override public void onResponse(Call<Comics> call, Response<Comics> response) {
            ((ComicsAdapter) mList.getAdapter()).setProgressIndicatorVisibility(false);

            if (response != null && response.body() != null &&
                    response.body().data != null &&
                    response.body().data.results != null) {
                List<Comic> comics = response.body().data.results;
                if (comics.size() == 0) {
                    mScrollListener.unblock();
                } else {
                    ((ComicsAdapter) mList.getAdapter()).addItems(
                            comics.toArray(new Comic[comics.size()]));
                }
            }
        }

        @Override public void onFailure(Call<Comics> call, Throwable t) {
            ((ComicsAdapter) mList.getAdapter()).setProgressIndicatorVisibility(false);
            Toast.makeText(ComicListActivity.this, t.getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }
    };
}

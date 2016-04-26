package com.gaborbiro.marveldemo.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
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
import com.gaborbiro.marveldemo.App;
import com.gaborbiro.marveldemo.R;
import com.gaborbiro.marveldemo.provider.api.ComicsFetchingException;
import com.gaborbiro.marveldemo.provider.api.MarvelApi;
import com.gaborbiro.marveldemo.provider.api.model.Comic;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

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

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private int mSelectedPosition = -1;

    @Inject public MarvelApi mMarvelApi;
    @Inject public DbxAccountManager mAccountManager;

    private RecyclerView mList;
    private EndlessRecyclerViewScrollListener mScrollListener;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_list);
        ((App) getApplication()).getAppComponent()
                .inject(this);
        ((App) getApplication()).getDropboxApiComponent()
                .inject(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mList = (RecyclerView) findViewById(R.id.comic_list);
        assert mList != null;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mList.setLayoutManager(linearLayoutManager);
        mScrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {

            @Override public void onLoadMore(int page, int totalItemsCount) {
                // this will be automatically invoked when the app is started
                new ComicsLoaderTask().execute(new ComicsLoaderParams(page + 1));
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
            new ComicsLoaderTask().execute(new ComicsLoaderParams(0));
        } else {
            // configuration change
            SavedState savedState = SavedState.load(savedInstanceState);
            mSelectedPosition = savedState.selectedPosition;

            if (savedState != null && savedState.comics != null) {
                mList.setAdapter(new ComicsAdapter(savedState.comics));
                mList.getLayoutManager()
                        .scrollToPosition(savedState.scrollPosition);
            } else {
                new ComicsLoaderTask().execute(new ComicsLoaderParams(0));
            }
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

        public static SavedState load(Bundle savedInstanceState) {
            return savedInstanceState.getParcelable(PARCEL_SAVED_STATE);
        }
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
                String thumbPath = comic.getThumbnailImageUri();

                if (!TextUtils.isEmpty(thumbPath)) {
                    // Caching:
                    // - LRU memory cache of 15% the available application RAM
                    // - Disk cache of 2% storage space up to 50MB but no less than 5MB.
                    Picasso.with(App.getAppContext())
                            .load(thumbPath)
                            .placeholder(R.drawable.ic_book_white_48dp)
                            .into(comicViewHolder.mThumbView);
                } else {
                    comicViewHolder.mThumbView.setImageResource(
                            R.drawable.ic_book_white_48dp);
                }
                // preload cover
                String coverPath = comic.getCoverImageUri();

                if (!TextUtils.isEmpty(coverPath)) {
                    Picasso.with(App.getAppContext())
                            .load(coverPath);
                }

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
                            startDetailActivity(comic, comicViewHolder.mThumbView);
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

        private void startDetailActivity(Comic comic, ImageView thumbView) {
            Intent intent = new Intent(ComicListActivity.this, ComicDetailActivity.class);
            intent.putExtra(ComicDetailFragment.ARG_ITEM, comic);
            intent.putExtra(ComicDetailActivity.ARG_SELECTED_THUMB_CACHE_KEY,
                    KEY_CACHE_SELECTED_THUMB);

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

    private class ComicsLoaderParams {

        static final int DEFAULT_PAGE_SIZE = 20;

        int page;
        int pageSize;

        public ComicsLoaderParams(int page) {
            this(page, DEFAULT_PAGE_SIZE);
        }

        public ComicsLoaderParams(int page, int pageSize) {
            this.page = page;
            this.pageSize = pageSize;
        }
    }

    private class ComicsLoaderTask extends AsyncTask<ComicsLoaderParams, Void, Comic[]> {

        private Exception e;

        @Override protected void onPreExecute() {
            if (mList.getAdapter() == null) {
                ComicsAdapter adapter = new ComicsAdapter(new Comic[0]);
                mList.setAdapter(adapter);
            }
            ((ComicsAdapter) mList.getAdapter()).setProgressIndicatorVisibility(true);
        }

        @Override protected Comic[] doInBackground(ComicsLoaderParams... params) {
            try {
                List<Comic> result = mMarvelApi.getComics(params[0].page,
                        params[0].pageSize).data.results;
                return result.toArray(new Comic[result.size()]);
            } catch (ComicsFetchingException | IOException e) {
                this.e = e;
            }
            return null;
        }

        @Override protected void onPostExecute(Comic[] comics) {
            ((ComicsAdapter) mList.getAdapter()).setProgressIndicatorVisibility(false);
            if (e != null) {
                e.printStackTrace();
                Toast.makeText(ComicListActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            } else if (comics != null) {
                if (comics.length == 0) {
                    mScrollListener.unblock();
                } else {
                    ((ComicsAdapter) mList.getAdapter()).addItems(comics);
                }
            }
        }
    }
}

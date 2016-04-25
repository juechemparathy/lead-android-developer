package com.gaborbiro.marveldemo.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gaborbiro.marveldemo.R;
import com.gaborbiro.marveldemo.provider.api.model.Comic;

/**
 * A fragment representing a single Comic detail screen.
 * This fragment is either contained in a {@link ComicListActivity}
 * in two-pane mode (on tablets) or a {@link ComicDetailActivity}
 * on handsets.
 */
public class ComicDetailFragment extends Fragment {

    public interface Listener {

        /**
         * BookDetailFragment is requesting the parent activity to display the book
         * cover image. This should only be
         * implemented by Activities with large enough App Toolbar
         *
         * @param url of the image
         */
        void onActionBarBackdropImageRequested(String url);
    }

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM = "item";

    private Listener mListener;

    private Comic mComic;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ComicDetailFragment() {
    }

    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (Listener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    activity.toString() + " must implement " + Listener.class.toString());
        }
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mComic = getArguments().getParcelable(ARG_ITEM);

            if (mComic != null) {
                mListener.onActionBarBackdropImageRequested(mComic.getCoverImageUri());
            }
        }
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.comic_detail, container, false);

        if (mComic != null) {
            String description = mComic.title + "\n\n" + mComic.description;
            ((TextView) rootView.findViewById(R.id.comic_detail)).setText(
                    Html.fromHtml(description));
        }
        return rootView;
    }
}

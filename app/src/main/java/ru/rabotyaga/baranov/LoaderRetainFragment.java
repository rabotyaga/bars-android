package ru.rabotyaga.baranov;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;

import java.util.List;

public class LoaderRetainFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Article>>{

    public final static String TAG = LoaderRetainFragment.class.getSimpleName();

    private LoaderManager.LoaderCallbacks<List<Article>> mCallbacks;

    private ArticleLoader mArticleLoader;
    private List<Article> mArticleList;
    private MyDatabase mDatabase;

    private String mQuery;
    private boolean mExactSearch = false;

    private boolean mLoadFinished = false;

    public LoaderRetainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        mDatabase = MyDatabase.getInstance(getActivity());

        // init loader with null args
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            //noinspection unchecked
            mCallbacks = (LoaderManager.LoaderCallbacks<List<Article>>) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement LoaderManager.LoaderCallbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mLoadFinished) {
            mCallbacks.onLoadFinished(mArticleLoader, mArticleList);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy!");
        super.onDestroy();
        if (mDatabase != null) {
            mDatabase.close();
            Log.d(TAG, "mDatabase closed!");
        }
    }

    void setExactSearch(boolean exactSearch) {
        setQuery(mQuery, exactSearch);
    }

    void setQuery(String query, boolean exactSearch) {
        Log.d(TAG, String.format("called setQuery(str,bool) : (%s, %b)", query, exactSearch));
        if (query == null || !query.equals(mQuery) || exactSearch != mExactSearch) {
            mQuery = query;
            mExactSearch = exactSearch;
            Bundle args = new Bundle();
            args.putString(ArticleLoader.ARG_QUERY, mQuery);
            args.putBoolean(ArticleLoader.ARG_EXACT_SEARCH, mExactSearch);
            getLoaderManager().restartLoader(0, args, this);
        }
    }

    boolean getExactSearch() {
        return mExactSearch;
    }

    public Loader<List<Article>> onCreateLoader(int id, Bundle args) {
        mLoadFinished = false;
        mArticleList = null;
        mArticleLoader = null;
        return mCallbacks.onCreateLoader(id, args);
    }

    public void onLoadFinished(Loader<List<Article>> loader, List<Article> articleList) {
        mLoadFinished = true;
        mArticleLoader = (ArticleLoader) loader;
        mArticleList = articleList;
        mCallbacks.onLoadFinished(loader, articleList);
    }

    public void onLoaderReset(Loader<List<Article>> loader) {
        mCallbacks.onLoaderReset(loader);
    }

}

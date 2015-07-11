package ru.rabotyaga.baranov;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

class ArticleLoader extends AsyncTaskLoader<List<Article>> {

    private static final String TAG = ArticleLoader.class.getSimpleName();

    public final static String ARG_QUERY = "query";
    public final static String ARG_EXACT_SEARCH = "exact_search";
    public final static String ARG_ARTICLE_NR = "article_nr";
    public final static String ARG_ROOT = "root";
    public final static String ARG_LOAD_NEXT_ROOT = "load_next_root";
    public final static String ARG_LOAD_PREVIOUS_ROOT = "load_previous_root";

    private final String query;
    private final MyDatabase db;
    private final boolean exactSearch;
    private List<Article> articles;
    private final int articleNr;
    private final String articleRoot;
    private final boolean loadNextRoot;
    private final boolean loadPreviousRoot;

    public ArticleLoader(Context context, Bundle args, MyDatabase db) {
        super(context);

        if (args != null) {
            query = args.getString(ARG_QUERY);
            exactSearch = args.getBoolean(ARG_EXACT_SEARCH);
            articleNr = args.getInt(ARG_ARTICLE_NR, -1);
            articleRoot = args.getString(ARG_ROOT);
            loadNextRoot = args.getBoolean(ARG_LOAD_NEXT_ROOT);
            loadPreviousRoot = args.getBoolean(ARG_LOAD_PREVIOUS_ROOT);
            Log.d(TAG, "ArticleLoader constructor, args.query = " + query + ", args.article_nr = " + articleNr);
        } else {
            query = null;
            exactSearch = false;
            articleNr = -1;
            articleRoot = null;
            loadNextRoot = false;
            loadPreviousRoot = false;
            Log.d(TAG, "ArticleLoader constructor, null args");
        }

        this.db = db;
    }

    public String getQuery() {
        return query;
    }

    public boolean isNavigatingRoots() {
        return loadNextRoot || loadPreviousRoot;
    }

    @Override
    public List<Article> loadInBackground() {
        Log.d(TAG, "LoadInBackground!");
        if (loadNextRoot) {
            Log.d(TAG, "load next root");
            String nextRoot = db.getNextRootByNr(articleNr, articleRoot);
            return db.getArticlesByRoot(nextRoot);
        } else {
            if (loadPreviousRoot) {
                Log.d(TAG, "load previous root");
                String previousRoot = db.getPreviousRootByNr(articleNr, articleRoot);
                return db.getArticlesByRoot(previousRoot);
            } else {
                if (articleNr != -1) {
                    if (articleRoot != null && !articleRoot.isEmpty()) {
                        Log.d(TAG, "loading article_root");
                        return db.getArticlesByRoot(articleRoot);
                    } else {
                        Log.d(TAG, "loading article_nr");
                        return db.getArticlesByNr(articleNr);
                    }
                } else {
                    if (query != null && !query.isEmpty()) {
                        Log.d(TAG, "loading search query");
                        return db.getArticlesByQuery(query, exactSearch);
                    } else {
                        Log.d(TAG, "did not found any parameters, returning empty list");
                        return new ArrayList<>();
                    }
                }
            }
        }
    }

    @Override
    public void deliverResult(List<Article> articles) {
        this.articles = articles;
        if (isStarted()) {
            super.deliverResult(articles);
        }
    }

    @Override
    protected void onStartLoading() {
        if (articles != null) {
            deliverResult(articles);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }



}

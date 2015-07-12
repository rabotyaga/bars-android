package ru.rabotyaga.baranov;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Loader;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.text.BidiFormatter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class ArticleFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Article>>  {

    public static final String TAG = ArticleFragment.class.getSimpleName();

    public static final String ARG_ARTICLE_NR = ArticleLoader.ARG_ARTICLE_NR;
    public static final String ARG_ROOT = ArticleLoader.ARG_ROOT;

    private int mArticleNr;
    private String mRoot;

    private MyDatabase db;

    private RecyclerView listView;
    private TextView listHeader;
    private LinearLayout progressBar;
    private ArticleAdapter articleAdapter;
    private ImageButton backButton;
    private ImageButton forwardButton;

    private boolean scrollOnFinish = true;

    private boolean mDualPane;

    public static ArticleFragment newInstance(int articleNr, String root) {
        ArticleFragment fragment = new ArticleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ARTICLE_NR, articleNr);
        args.putString(ARG_ROOT, root);
        fragment.setArguments(args);
        return fragment;
    }

    public ArticleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = MyDatabase.getInstance(getActivity());
        if (getArguments() != null) {
            mArticleNr = getArguments().getInt(ARG_ARTICLE_NR);
            mRoot = getArguments().getString(ARG_ROOT);
        }

        //create adapter with empty list, do not bind onClick to views
        //ArticleAdapter(List<Article> articleList, boolean setOnClickListener)
        articleAdapter = new ArticleAdapter(new ArrayList<Article>(), false);

        mDualPane = getResources().getBoolean(R.bool.dual_pane);

        if (savedInstanceState != null) {
            // do not scroll to selected article
            // because it's not first launch
            // just configuration change
            scrollOnFinish = false;
            Log.d(TAG, "savedInstanceState not null, removing scrollOnFinish!");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(0, getArguments(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_article, container, false);

        listView = (RecyclerView) view.findViewById(R.id.cardlist);
        listHeader = (TextView) view.findViewById(R.id.textview);
        progressBar = (LinearLayout) view.findViewById(R.id.pbLL);

        listView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(llm);

        registerForContextMenu(listView);

        listView.setAdapter(articleAdapter);
        if (mDualPane) {
            setHeader();
        } else {
            listHeader.setVisibility(View.GONE);

        }

        progressBar.setVisibility(View.VISIBLE);

        backButton = (ImageButton) view.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPreviousRoot();
            }
        });

        forwardButton = (ImageButton) view.findViewById(R.id.forwardButton);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNextRoot();
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }

    public String getRoot() { return mRoot; }

    private void setNr(int nr) {
        mArticleNr = nr;
    }

    private void setNShowNR(int nr) {
        setNr(nr);
        showNr();
    }

    private void showNr() {
        final int pos = articleAdapter.findByNr(mArticleNr);
        articleAdapter.setSelectedNHighlight(pos);
        if (scrollOnFinish) {
            listView.post(new Runnable() {
                @Override
                public void run() {
                    listView.smoothScrollToPosition(pos);
                }
            });
        }
    }

    public void showArticle(int nr, String root) {
        scrollOnFinish = true;
        if (root != null && root.equals(mRoot)) {
            setNShowNR(nr);
        } else {
            mRoot = root;
            mArticleNr = nr;
            setHeader();
            Bundle args = new Bundle();
            args.putString(ARG_ROOT, root);
            args.putInt(ARG_ARTICLE_NR, nr);
            getLoaderManager().restartLoader(0, args, this);
        }
    }

    private void loadPreviousRoot() {
        Log.d(TAG, "back");
        Article firstArticle = articleAdapter.getArticle(0);
        if (firstArticle != null) {
            Bundle args = new Bundle();
            args.putBoolean(ArticleLoader.ARG_LOAD_PREVIOUS_ROOT, true);
            args.putString(ARG_ROOT, firstArticle.root);
            args.putInt(ARG_ARTICLE_NR, firstArticle.nr);
            getLoaderManager().restartLoader(0, args, this);
        }
    }

    private void loadNextRoot() {
        Log.d(TAG, "forward");
        Article firstArticle = articleAdapter.getArticle(0);
        if (firstArticle != null) {
            Bundle args = new Bundle();
            args.putBoolean(ArticleLoader.ARG_LOAD_NEXT_ROOT, true);
            args.putString(ARG_ROOT, firstArticle.root);
            args.putInt(ARG_ARTICLE_NR, firstArticle.nr);
            getLoaderManager().restartLoader(0, args, this);
        }
    }

    private void setHeader() {
        if (listHeader == null) {
            return;
        }
        if (mRoot != null) {
            String title = getResources().getString(R.string.details_activity_title) + "  ";
            title = title.concat(BidiFormatter.getInstance().unicodeWrap(mRoot));
            listHeader.setText(title);
        } else {
        //    listHeader.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
        }
    }

    private void updateHeader() {
        if (mDualPane) {
            setHeader();
        } else {
            ((DetailsActivity)getActivity()).updateHeader(mRoot);
        }
    }

    public Loader<List<Article>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, String.format("onCreateLoader! args.%s = %d", ArticleLoader.ARG_ARTICLE_NR, (args != null ? args.getInt(ArticleLoader.ARG_ARTICLE_NR) : -1)));
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.requestFocus();
        }
        if (listView != null) {
            listView.setVisibility(View.GONE);
            listView.scrollToPosition(0);
        }
        articleAdapter.removeSelection();
        return new ArticleLoader(getActivity(), args, db);
    }

    public void onLoadFinished(Loader<List<Article>> loader, List<Article> articleList) {

        Log.d(TAG, String.format("onLoadFinished %d! got %d articles", hashCode(), articleList.size()));
        articleAdapter.setList(articleList);

        if (articleList.size() > 0 && listView != null) {

            listView.setVisibility(View.VISIBLE);
            listView.requestFocus();
            if (((ArticleLoader) loader).isNavigatingRoots()) {
                Log.d(TAG, "loader finished, navigating roots");
                mRoot = articleAdapter.getRoot(0);
                mArticleNr = articleAdapter.getFirstNr();
                updateHeader();
                if (mDualPane) {
                    ((MainActivity) getActivity()).removeSelection();
                }
            } else {
                Log.d(TAG, "loader finished, 'normal' mode");
                showNr();
            }
            if (articleAdapter.getFirstNr() == 1) {
                if (backButton != null) {
                    backButton.setEnabled(false);
                }
            } else {
                if (backButton != null && !backButton.isEnabled()) {
                    backButton.setEnabled(true);
                }
                if (articleAdapter.getLastNr() == db.lastArticleNr) {
                    if (forwardButton != null) {
                        forwardButton.setEnabled(false);
                    }
                } else {
                    if (forwardButton != null && !forwardButton.isEnabled()) {
                        forwardButton.setEnabled(true);
                    }
                }
            }

        }
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

    }

    public void onLoaderReset(Loader<List<Article>> loader) {
        Log.d(TAG, "onLoaderReset");
        articleAdapter.dropList();
        progressBar.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo
            menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        //CustomRecyclerView.RecyclerContextMenuInfo info = (CustomRecyclerView
        //        .RecyclerContextMenuInfo) menuInfo;

        if (!mDualPane) {
            getActivity().getMenuInflater().inflate(R.menu.context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        CustomRecyclerView.RecyclerContextMenuInfo info = (CustomRecyclerView
                .RecyclerContextMenuInfo) item
                .getMenuInfo();

        Activity activity = getActivity();
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE);
        ClipData clip;
        String message;

        switch (item.getItemId()) {
            case R.id.action_copy_translation:
                clip = ClipData.newHtmlText(Article.LABEL, info.article.translation, info.article.translationToHtml());
                message = getResources().getString(R.string.translation_copied);
                break;
            case R.id.action_copy_all:
                clip = ClipData.newHtmlText(Article.LABEL, info.article.toString(), info.article.toHtml());
                message = getResources().getString(R.string.whole_article_copied);
                break;
            default:
                return false;
        }

        if(!mDualPane) {
            //dimming status bar
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
            decorView.setSystemUiVisibility(uiOptions);
        }

        clipboard.setPrimaryClip(clip);

        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        return true;
    }
}

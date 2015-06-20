package ru.rabotyaga.baranov;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.support.v7.app.ActionBar;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.support.v4.text.BidiFormatter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Article>> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final static String ARG_QUERY = "query";
    private final static String ARG_EXACT_SEARCH = "exact_search";
    private final static String ARG_OPTIONS_PANE_VISIBLE = "options_pane_visible";
    private final static String ARG_SELECTED = "selected";
    private final static String ARG_DETAILS_NR = "details_nr";
    private final static String ARG_DETAILS_ROOT = "details_root";

    public static String PACKAGE_NAME;

    private MyDatabase db;

    private RecyclerView listView;
    private LinearLayout progressBar;
    private ArticleAdapter aa;
    private RadioButton rbSearchExact;
    private RadioButton rbSearchLike;
    private RelativeLayout optionsPane;
    private SearchView searchView;

    private TextView resultsCount;

    private TextView listHeader;

    private Button bClearSearchHistory;

    private String query;

    private SearchRecentSuggestions suggestions;

    private AlertDialog alertDialog;

    private boolean optionsPaneShown = false;

    private int mSelected = -1;
    private int mDetailsNr = -1;
    private String mDetailsRoot = null;

    private boolean mDualPane;

    private ArticleFragment articleFragment;

    private LoaderRetainFragment loaderRetainFragment;

    private LinearLayout details_ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PACKAGE_NAME = getApplicationContext().getPackageName();

        listView = (RecyclerView) findViewById(R.id.cardlist);
        listHeader = (TextView) findViewById(R.id.textview);
        progressBar = (LinearLayout) findViewById(R.id.pbLL);
        resultsCount = (TextView) findViewById(R.id.resultsCount);

        rbSearchExact = (RadioButton) findViewById(R.id.rbSearchExact);
        rbSearchLike = (RadioButton) findViewById(R.id.rbSearchLike);
        optionsPane = (RelativeLayout) findViewById(R.id.optionsRL);
        bClearSearchHistory = (Button) findViewById(R.id.bClearSearchHistory);

        listView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(llm);

        registerForContextMenu(listView);

        aa = new ArticleAdapter(new ArrayList<Article>());
        listView.setAdapter(aa);

        db = MyDatabase.getInstance(this);

        mDualPane = getResources().getBoolean(R.bool.dual_pane);

        if (savedInstanceState != null) {
            query = savedInstanceState.getString(ARG_QUERY);
            if (savedInstanceState.getBoolean(ARG_EXACT_SEARCH) != rbSearchExact.isChecked()) {
                rbSearchLike.setChecked(false);
                rbSearchExact.setChecked(true);
            }
            mSelected = savedInstanceState.getInt(ARG_SELECTED, -1);
            if (mSelected >= 0) {
                aa.setSelected(mSelected);
            }
            mDetailsNr = savedInstanceState.getInt(ARG_DETAILS_NR, -1);
            mDetailsRoot = savedInstanceState.getString(ARG_DETAILS_ROOT);

            if(savedInstanceState.getBoolean(ARG_OPTIONS_PANE_VISIBLE)) {
                showOptionsPane();
            }
        }

        FragmentManager fm = getFragmentManager();

        // adding fragment for retaining main loader
        loaderRetainFragment = (LoaderRetainFragment) fm.findFragmentByTag(LoaderRetainFragment.TAG);
        if (loaderRetainFragment == null) {
            Log.d(TAG, "creating new loaderRetainFragment!");
            loaderRetainFragment = new LoaderRetainFragment();
            fm.beginTransaction().add(loaderRetainFragment, LoaderRetainFragment.TAG).commit();
        }

        //adding fragment for displaying details
        articleFragment = (ArticleFragment) fm.findFragmentByTag(ArticleFragment.TAG);
        if (mDualPane) {
            details_ll = (LinearLayout) findViewById(R.id.detailsLL);
            if (articleFragment == null) {
                Log.d(TAG, String.format("invoking newInstance with %d, %s", mDetailsNr, mDetailsRoot));
                articleFragment = ArticleFragment.newInstance(mDetailsNr, mDetailsRoot);
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.details_frame, articleFragment, ArticleFragment.TAG);
                ft.commit();
            } else {
                if(mDetailsNr != -1 && mDetailsRoot != null) {
                    Log.d(TAG, String.format("old articleFragment, showing %d, %s", mDetailsNr, mDetailsRoot));
                    articleFragment.showArticle(mDetailsNr, mDetailsRoot);
                }
            }
            if (mDetailsNr != -1 && mDetailsRoot != null && !optionsPaneShown) {
                details_ll.setVisibility(View.VISIBLE);
            }

        } else {
            if (articleFragment != null) {
                fm.beginTransaction().remove(articleFragment).commit();
                Log.d(TAG, "removed old articleFragment!!!");
            }
        }

        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                //possible to store layoutManager in globals
                Integer i = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                if (i >= 0) {
                    listHeader.setText(aa.getRoot(i));
                }
                if (resultsCount.getVisibility() == View.VISIBLE && i > 0) {
                    resultsCount.setVisibility(View.GONE);

                }

            }
        });

        suggestions = new SearchRecentSuggestions(this, ArticleSuggestionProvider.AUTHORITY, ArticleSuggestionProvider.MODE);

        handleIntent(getIntent());

    }

    @Override
    public void onResume() {
        super.onResume();

        //dimming status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_QUERY, searchView.getQuery().toString());
        outState.putBoolean(ARG_EXACT_SEARCH, rbSearchExact.isChecked());
        outState.putBoolean(ARG_OPTIONS_PANE_VISIBLE, optionsPaneShown);
        outState.putInt(ARG_SELECTED, mSelected);
        outState.putInt(ARG_DETAILS_NR, mDetailsNr);
        outState.putString(ARG_DETAILS_ROOT, mDetailsRoot);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Log.d(TAG, "handleIntent start");
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            //hide options pane for the case search is made directly from options pane
            //this is needed here mainly for simulation with back button
            hideOptionsPane();

            String mQuery = intent.getStringExtra(SearchManager.QUERY);
            if (mQuery == null) {
                if (searchView != null)
                    searchView.requestFocus();
                return;
            }
            Log.d(TAG, "search: " + mQuery);

            if (!mQuery.matches("[\\p{InARABIC}\\p{InCyrillic}]+")) {
                Resources resources = getResources();
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(resources.getString(R.string.enter_arabic_or_russian_word));
                alert.setPositiveButton(resources.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (searchView != null) {
                            searchView.setQuery("", false);
                            searchView.requestFocus();
                        }
                    }
                });

                alertDialog = alert.show();
                intent.removeExtra(SearchManager.QUERY);
                return;
            }

            if (mQuery.length() < 2 && rbSearchLike.isChecked()) {
                Resources resources = getResources();
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(resources.getString(R.string.you_entered_only_one_character));
                alert.setMessage(resources.getString(R.string.the_search_will_be_made_in_exact_mode));
                alert.setPositiveButton(resources.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                });

                alertDialog = alert.show();
                rbSearchExact.setChecked(true);
                rbSearchLike.setChecked(false);
            }

            loaderRetainFragment.setQuery(mQuery, rbSearchExact.isChecked());

        } else {
            Log.d(TAG, "No search. First launch.");
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(
                new ComponentName(getApplicationContext(), MainActivity.class)));
        searchView.setIconifiedByDefault(false);

        //Set the search query if we are restored and previously it was here
        if (query != null) {
            searchView.setQuery(query, false);
        }

        if (optionsPane != null && optionsPane.getVisibility() == View.VISIBLE) {
            // remove action_settings button
            MenuItem actionSettings = menu.findItem(R.id.action_settings);
            actionSettings.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.action_settings:
                showOptionsPane();
                break;
            case android.R.id.home:
                hideOptionsPane();
                break;
            case R.id.action_alphabet:
                showAlphabet();
                break;
            case R.id.action_about:
                showAbout();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        if (db != null) {
            db.close();
        }
    }

    @Override
    public void onBackPressed() {
        //back stack hack for options pane
        if(optionsPaneShown) {
            hideOptionsPane();
        } else {
            //if we are showing results - go back to 'home' page
            if (listView.getVisibility() == View.VISIBLE && searchView.getQuery() != null && !searchView.getQuery().toString().isEmpty()) {

                loaderRetainFragment.setQuery(null, false);

                searchView.setQuery("", false);

                searchView.requestFocus();

            } else {
                this.finish();
            }
        }
    }

    @SuppressWarnings("unused")
    public void onRbSearchClick(View v) {
        boolean checked = ((RadioButton) v).isChecked();

        switch(v.getId()) {
            case R.id.rbSearchExact:
                if (checked) {
                    rbSearchLike.setChecked(false);
                }
                break;
            case R.id.rbSearchLike:
                if (checked) {
                    rbSearchExact.setChecked(false);

                }
                break;
        }
    }

    @SuppressWarnings({"unused", "UnusedParameters"})
    public void onBClearSearchHistoryClick(View v) {
        Resources resources = getResources();
        AlertDialog.Builder confirm = new AlertDialog.Builder(this);
        confirm.setTitle(resources.getString(R.string.clear_search_history_dialog_title));
        confirm.setPositiveButton(resources.getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                suggestions.clearHistory();
                bClearSearchHistory.setEnabled(false);
            }
        });
        confirm.setNegativeButton(resources.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //nothing to do
            }
        });
        alertDialog = confirm.show();


    }

    public Loader<List<Article>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader! args.query = " + (args != null ? args.getString(ARG_QUERY) : "null"));
        progressBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        listView.scrollToPosition(0);
        optionsPane.setVisibility(View.GONE);
        resultsCount.setVisibility(View.GONE);
        progressBar.requestFocus();
        setSelected(-1);
        mDetailsRoot = null;
        mDetailsNr = -1;
        if (mDualPane && articleFragment !=null && articleFragment.getRoot() != null) {
            articleFragment.showArticle(-1, null);
            if (details_ll != null && details_ll.getVisibility() == View.VISIBLE) {
                details_ll.setVisibility(View.GONE);
            }
        }
        return new ArticleLoader(this, args, db);
    }

    public void onLoadFinished(Loader<List<Article>> loader, List<Article> articleList) {
        Log.d(TAG, String.format("onLoadFinished %d! got %d articles", hashCode(), articleList.size()));
        String loader_query = ((ArticleLoader) loader).getQuery();
        aa.setList(articleList);

        String res = String.format(getResources().getString(R.string.results), articleList.size());
        resultsCount.setText(res);

        if ((articleList.size() > 0) && !optionsPaneShown) {

            listView.setVisibility(View.VISIBLE);
            optionsPane.setVisibility(View.GONE);
            listView.requestFocus();

            //fill in recent query provider
            Article firstResult = articleList.get(0);
            String secondLine = BidiFormatter.getInstance().unicodeWrap(firstResult.ar_inf_wo_vowels).concat(": ");
            //String secondLine = firstResult.ar_inf_wo_vowels.concat(": ");
            if (firstResult.translation.length() > 20) {
                secondLine = secondLine.concat(firstResult.translation.substring(0, 20).concat(" ..."));
            } else {
                secondLine = secondLine.concat(firstResult.translation);
            }
            suggestions.saveRecentQuery(loader_query, secondLine);

            if (!bClearSearchHistory.isEnabled()) {
                bClearSearchHistory.setEnabled(true);
            }

            if (searchView != null) {
                //if user selected from suggestions we should manually fill in searchView to keep it in sync
                searchView.setQuery(loader_query, false);
            }

            listHeader.setText(firstResult.root);

        } else {
            optionsPane.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }
        progressBar.setVisibility(View.GONE);

        //show results count
        //ignore null query - it may be first start
        //ignore opened (by menu) options pane
        if (((ArticleLoader) loader).getQuery() != null && !optionsPaneShown) {
            resultsCount.setVisibility(View.VISIBLE);
        }

        invalidateOptionsMenu();
    }
    public void onLoaderReset(Loader<List<Article>> loader) {
        Log.d(TAG, "onLoaderReset");
    }

    private void showOptionsPane() {

        optionsPaneShown = true;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        progressBar.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        resultsCount.setVisibility(View.GONE);
        if (mDualPane) {
            if (details_ll != null && details_ll.getVisibility() == View.VISIBLE) {
                details_ll.setVisibility(View.GONE);
            }
        }
        optionsPane.setVisibility(View.VISIBLE);
        optionsPane.requestFocus();
        invalidateOptionsMenu();
    }

    private void hideOptionsPane() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        listView.setVisibility(View.VISIBLE);
        if (mDualPane && articleFragment !=null && articleFragment.getRoot() != null) {
            if (details_ll != null && details_ll.getVisibility() == View.GONE) {
                details_ll.setVisibility(View.VISIBLE);
            }
        }
        optionsPane.setVisibility(View.GONE);
        optionsPaneShown = false;
        invalidateOptionsMenu();

        if (loaderRetainFragment.getExactSearch() != rbSearchExact.isChecked()) {
            loaderRetainFragment.setExactSearch(rbSearchExact.isChecked());
        }

        //dimming status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void articleClicked(Integer nr, String root, int adapterPosition) {

        setSelected(adapterPosition);

        if (mDualPane) {
            if (details_ll.getVisibility() == View.GONE) {
                details_ll.setVisibility(View.VISIBLE);
            }
            articleFragment.showArticle(nr, root);
        } else {
            mDetailsNr = nr;
            mDetailsRoot = root;
            Intent intent = new Intent();
            intent.setClass(this, DetailsActivity.class);
            intent.putExtra(ArticleFragment.ARG_ARTICLE_NR, nr.intValue());
            intent.putExtra(ArticleFragment.ARG_ROOT, root);
            startActivity(intent);
            overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_top);
        }
    }

    private void setSelected(int adapterPosition) {
        aa.setSelected(adapterPosition);
        mSelected = adapterPosition;
    }

    private void showAlphabet() {
        Intent intent = new Intent();
        intent.setClass(this, AlphabetActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_top);
    }

    private void showAbout() {
        Intent intent = new Intent();
        intent.setClass(this, AboutActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_top);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo
            menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        getMenuInflater().inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        CustomRecyclerView.RecyclerContextMenuInfo info = (CustomRecyclerView
                .RecyclerContextMenuInfo) item
                .getMenuInfo();

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
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

        //dimming status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
        decorView.setSystemUiVisibility(uiOptions);

        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        return true;
    }
}

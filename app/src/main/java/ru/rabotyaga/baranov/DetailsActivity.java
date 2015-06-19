package ru.rabotyaga.baranov;

import android.os.Bundle;
import android.support.v4.text.BidiFormatter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;


public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getBoolean(R.bool.dual_pane)) {
            //no need in this activity
            //in dual pane mode
            finish();
            return;
        }

        Bundle args = getIntent().getExtras();

        if (savedInstanceState == null) {
            ArticleFragment af = new ArticleFragment();
            af.setArguments(args);
            getFragmentManager().beginTransaction().add(android.R.id.content, af, ArticleFragment.TAG).commit();
        }

        if (args.containsKey(ArticleFragment.ARG_ROOT)) {
            String title = getResources().getString(R.string.details_activity_title) + "  ";
            title = title.concat(BidiFormatter.getInstance().unicodeWrap(args.getString(ArticleFragment.ARG_ROOT)));
            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setTitle(title);
            }
        }
    }

    @Override public void onResume() {
        super.onResume();

        //dimming status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.abc_slide_in_top, R.anim.abc_slide_out_bottom);
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

}

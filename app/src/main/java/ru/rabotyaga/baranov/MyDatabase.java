package ru.rabotyaga.baranov;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteException;
import android.support.v4.text.BidiFormatter;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


final class MyDatabase extends SQLiteOpenHelper {

    private static final String TAG = MyDatabase.class.getSimpleName();

    private static final String DATABASE_NAME = "articles.db";
    private static final int DATABASE_VERSION = 2;

    private static final String ARTICLE_TABLE = "articles";

    private static final String COLUMN_NR = "nr";
    private static final String COLUMN_AR_INF = "ar_inf";
    private static final String COLUMN_AR_INF_WO_VOWELS = "ar_inf_wo_vowels";
    private static final String COLUMN_TRANSCRIPTION = "transcription";
    private static final String COLUMN_TRANSLATION = "translation";
    private static final String COLUMN_ROOT = "root";
    private static final String COLUMN_FORM = "form";
    private static final String COLUMN_VOCALIZATION = "vocalization";
    private static final String COLUMN_HOMONYM_NR = "homonym_nr";
    private static final String COLUMN_OPT = "opt";
    private static final String COLUMN_MN1 = "mn1";
    private static final String COLUMN_AR1 = "ar1";
    private static final String COLUMN_MN2 = "mn2";
    private static final String COLUMN_AR2 = "ar2";
    private static final String COLUMN_MN3 = "mn3";
    private static final String COLUMN_AR3 = "ar3";

    private static final String[] ALL_COLUMNS = {
            COLUMN_NR,
            COLUMN_AR_INF,
            COLUMN_AR_INF_WO_VOWELS,
            COLUMN_TRANSCRIPTION,
            COLUMN_TRANSLATION,
            COLUMN_ROOT,
            COLUMN_FORM,
            COLUMN_VOCALIZATION,
            COLUMN_HOMONYM_NR,
            COLUMN_OPT,
            COLUMN_MN1,
            COLUMN_AR1,
            COLUMN_MN2,
            COLUMN_AR2,
            COLUMN_MN3,
            COLUMN_AR3,

    };

    private static final String ORDER_BY = "nr";

    private static final String SQL_LIKE = " LIKE ?";
    private static final String SQL_EQUAL = " = ?";

    private static final String PRAGMA_USER_VERSION = "PRAGMA user_version";

    private static final String ASSET_DB_PATH = "databases/" + DATABASE_NAME;

    private final Context mContext;

    private SQLiteDatabase mDatabase = null;

    private final String mDatabasePath;

    private boolean mCopyIsNeeded = false;

    private static int mOpenConnections;

    private static MyDatabase mInstance;

    synchronized static public MyDatabase getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MyDatabase(context.getApplicationContext());
            Log.d(TAG, "getInstance called constructor!");
        }
        mOpenConnections++;
        Log.d(TAG, String.format("getInstance, open connections: %d", mOpenConnections));
        return mInstance;
    }

    private MyDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;

        mDatabasePath = mContext.getDatabasePath(DATABASE_NAME).getPath();

        SQLiteDatabase db = null;
        try {
            db = getReadableDatabase();
            if (db != null) {
                db.close();
            }
            if (mCopyIsNeeded) {
                copyDatabaseFromAssets();
            }
        } catch (SQLiteException e) {
            //do nothing
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        mDatabase = getWritableDatabase();

        if (mDatabase == null) {
            throw new MyDatabaseException("Failed to open database " + mDatabasePath);
        }
    }

    public List<Article> getArticlesByQuery(String query, boolean exactSearch) {

        List<Article> list = new ArrayList<>();
        List<Root> rootList = new ArrayList<>();

        boolean ru_search = false;
        String selection;
        String sel_arg;

        if (query.matches("[\\p{InARABIC}]+")) {
            selection = COLUMN_AR_INF_WO_VOWELS + SQL_LIKE;
            if (exactSearch) {
                sel_arg = query;
            } else {
                sel_arg = "%" + query + "%";
            }
        } else {
            ru_search = true;
            selection = COLUMN_TRANSLATION + SQL_LIKE;
            if (exactSearch) {
                sel_arg = "% " + query + " %";
            } else {
                sel_arg = "%" + query + "%";
            }
        }

        String[] sel_args = {sel_arg};
        Cursor c = mDatabase.query(ARTICLE_TABLE, ALL_COLUMNS, selection, sel_args,
                /*group by*/ null, /*having*/ null, ORDER_BY);

        int matchHighlightColor = mContext.getResources().getColor(R.color.background_match_highlight);
        int arabicTextColor = mContext.getResources().getColor(R.color.arabic_text);

        Root current_root = new Root();
        current_root.articles = new ArrayList<>();
        Integer current_article_match_score;

        while (c.moveToNext()) {
            Article a = fillInArticleFromCursor(c, arabicTextColor);

            if (ru_search) {
                if (exactSearch) {
                    current_article_match_score = a.setHighlightedTranslation(matchHighlightColor, " " + query + " ");
                } else {
                    current_article_match_score = a.setHighlightedTranslation(matchHighlightColor, query);
                }
                current_article_match_score = Math.round(current_article_match_score / (float) a.translation.length() * 100);
            } else {
                current_article_match_score = a.setHighlightedArInf(matchHighlightColor, query);
                current_article_match_score = Math.round(current_article_match_score / (float) a.ar_inf.length() * 100);
            }

            if (current_root.root != null && current_root.root.equals(a.root)) {
                current_root.articles.add(a);
                if (current_root.matchScore < current_article_match_score) {
                    current_root.matchScore = current_article_match_score;
                }
            } else {
                if (c.getPosition() > 0) {
                    rootList.add(current_root);
                    current_root = new Root();
                    current_root.articles = new ArrayList<>();
                }
                current_root.root = a.root;
                current_root.articles.add(a);
                current_root.matchScore = current_article_match_score;
            }
            //a.translation = current_article_match_score.toString() + " nr " + a.nr.toString();
            //a.highlighted_translation = Spannable.Factory.getInstance().newSpannable(a.translation);
          //  list.add(a);
        }
        c.close();

        if (!current_root.articles.isEmpty()) {
            rootList.add(current_root);
        }

        Collections.sort(rootList, new RootMatchScoreComparator());

        for (Root root : rootList) {
            for (Article article : root.articles) {
                list.add(article);
            }
        }

        return list;
    }

    private Article getArticleByNr(int articleNr) {

        Article a = null;

        String selection = COLUMN_NR + SQL_EQUAL;
        String[] sel_args = {String.format("%d", articleNr)};
        Cursor c = mDatabase.query(ARTICLE_TABLE, ALL_COLUMNS, selection, sel_args,
                /*group by*/ null, /*having*/ null, ORDER_BY, "1");


        int arabicTextColor = mContext.getResources().getColor(R.color.arabic_text);

        if (c.moveToNext()) {
            a = fillInArticleFromCursor(c, arabicTextColor);
        }

        c.close();

        return a;
    }

    public List<Article> getArticlesByNr(int articleNr) {

        Article article = getArticleByNr(articleNr);

        return getArticlesByRoot(article.root);
    }

    public List<Article> getArticlesByRoot(String root) {

        List<Article> list = new ArrayList<>();

        String selection = COLUMN_ROOT + SQL_EQUAL;
        String[] sel_args = {root};
        Cursor c = mDatabase.query(ARTICLE_TABLE, ALL_COLUMNS, selection, sel_args,
                /*group by*/ null, /*having*/ null, ORDER_BY);

        int arabicTextColor = mContext.getResources().getColor(R.color.arabic_text);

        while (c.moveToNext()) {
            Article a = fillInArticleFromCursor(c, arabicTextColor);

            list.add(a);
        }
        c.close();

        return list;
    }


    private String unescape(String str) {
        return str.replaceAll("\\\\n", "\\\n").replaceAll("\\\\r", "");
    }

    private String bidiWrap(String str) {
        String wrapped = "";

        BidiFormatter bidi = BidiFormatter.getInstance();

        Pattern p = Pattern.compile(Article.ARABIC_REGEXP);

        List<String> ru_parts = new ArrayList<>();
        List<String> ar_parts = new ArrayList<>();
        int s = 0;

        Matcher m = p.matcher(str);
        while (m.find()) {
            ru_parts.add(str.substring(s, m.start()));
            ar_parts.add(m.group());
            s = m.end();
            //Log.d(LOG_TAG, "pattern matched, group: " + m.group());
        }
        if (s < str.length()) {
            ru_parts.add(str.substring(s));
        }

        int i;
        for (i = 0; i < ar_parts.size(); i++) {
            wrapped = wrapped.concat(ru_parts.get(i));
            wrapped = wrapped.concat(bidi.unicodeWrap(ar_parts.get(i)));
        }
        if (ru_parts.size() > i) {
            wrapped = wrapped.concat(ru_parts.get(i));
        }

        return wrapped;
    }

    private Article fillInArticleFromCursor(Cursor c, int arabicTextColor) {
        Article a = new Article();

        BidiFormatter bidi = BidiFormatter.getInstance();

        a.nr = c.getInt(c.getColumnIndex(COLUMN_NR));
        a.ar_inf = c.getString(c.getColumnIndex(COLUMN_AR_INF));
        a.ar_inf_wo_vowels = c.getString(c.getColumnIndex(COLUMN_AR_INF_WO_VOWELS));
        a.transcription = c.getString(c.getColumnIndex(COLUMN_TRANSCRIPTION));
        a.translation = unescape(c.getString(c.getColumnIndex(COLUMN_TRANSLATION)));
        a.root = c.getString(c.getColumnIndex(COLUMN_ROOT));
        a.form = c.getString(c.getColumnIndex(COLUMN_FORM));
        a.vocalization = c.getString(c.getColumnIndex(COLUMN_VOCALIZATION));
        if (a.vocalization.equals("\\N")) {
            a.vocalization = null;
        }
        a.homonym_nr = c.getInt(c.getColumnIndex(COLUMN_HOMONYM_NR));
        if (a.homonym_nr == 0) {
            a.homonym_nr = null;
        }
        String opt1 = c.getString(c.getColumnIndex(COLUMN_OPT));
        String mn1 = c.getString(c.getColumnIndex(COLUMN_MN1));

        SpannableStringBuilder opts = new SpannableStringBuilder();
        int start;

        if (!opt1.isEmpty()) {
            opts.append(opt1);
            opts.append(" ");
        }

        opts.append(mn1);
        if (opts.length() > 0 && opts.charAt(opts.length() - 1) != ' ') {
            opts.append(" ");
        }

        start = opts.length();
        opts.append(bidi.unicodeWrap(c.getString(c.getColumnIndex(COLUMN_AR1))));
        if (opts.length() > start) {
            opts.setSpan(new ForegroundColorSpan(arabicTextColor), start, opts.length(),
                    SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (opts.length() > 0 && opts.charAt(opts.length() - 1) != ' ') {
            opts.append(" ");
        }
        opts.append(c.getString(c.getColumnIndex(COLUMN_MN2)));
        if (opts.length() > 0 && opts.charAt(opts.length() - 1) != ' ') {
            opts.append(" ");
        }

        start = opts.length();
        opts.append(bidi.unicodeWrap(c.getString(c.getColumnIndex(COLUMN_AR2))));
        if (opts.length() > start) {
            opts.setSpan(new ForegroundColorSpan(arabicTextColor), start, opts.length(),
                    SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (opts.length() > 0 && opts.charAt(opts.length() - 1) != ' ') {
            opts.append(" ");
        }
        opts.append(c.getString(c.getColumnIndex(COLUMN_MN3)));
        if (opts.length() > 0 && opts.charAt(opts.length() - 1) != ' ') {
            opts.append(" ");
        }

        start = opts.length();
        opts.append(bidi.unicodeWrap(c.getString(c.getColumnIndex(COLUMN_AR3))));
        if (opts.length() > start) {
            opts.setSpan(new ForegroundColorSpan(arabicTextColor), start, opts.length(),
                    SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        a.optsSpannable = opts;
        a.opts = opts.toString();

        //a.translation = bidiWrap(a.translation);

        a.stubHighlightedFieldsAndColorArabicInTranslation(arabicTextColor);

        return a;
    }

    @Override
    public final void onCreate(SQLiteDatabase db) {
        mCopyIsNeeded = true;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        mCopyIsNeeded = true;
    }

    @Override
    public synchronized void close() {
        mOpenConnections--;
        Log.d(TAG, String.format("close, open connections: %d", mOpenConnections));
        if (mOpenConnections == 0) {
            super.close();
            mInstance = null;
            Log.d(TAG, "database closed!");
        }
    }

    private void copyDatabaseFromAssets() throws MyDatabaseException {
        Log.d(TAG, "copying database from assets...");

        InputStream is;

        try {
            // try plain
            is = mContext.getAssets().open(ASSET_DB_PATH);
        } catch (IOException e) {
            // try gzip
            try {
                is = mContext.getAssets().open(ASSET_DB_PATH + ".gz");
            } catch (IOException e2) {
                MyDatabaseException me = new MyDatabaseException("Missing " + ASSET_DB_PATH + " file (or .gz) in assets");
                me.setStackTrace(e2.getStackTrace());
                throw me;
            }
        }

        try {
            copyFile(is, new FileOutputStream(mDatabasePath));
            Log.d(TAG, "database copy complete");
        } catch (IOException e) {
            MyDatabaseException me = new MyDatabaseException("Unable to write " + mDatabasePath + " to data directory");
            me.setStackTrace(e.getStackTrace());
            throw me;
        }
        setNewVersionAfterCopy();
        mCopyIsNeeded = false;
    }

    private void setNewVersionAfterCopy() {
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(mDatabasePath, null, SQLiteDatabase.OPEN_READWRITE);
            db.execSQL(PRAGMA_USER_VERSION + " = " + DATABASE_VERSION);
        } catch (SQLiteException e ) {
            //do nothing
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    private static void copyFile(InputStream in, OutputStream outs) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer))>0) {
            outs.write(buffer, 0, length);
        }
        outs.flush();
        outs.close();
        in.close();
    }

    public static class MyDatabaseException extends SQLiteException {

        public MyDatabaseException(String error) {
            super(error);
        }
    }
}
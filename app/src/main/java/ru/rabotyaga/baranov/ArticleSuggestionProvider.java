package ru.rabotyaga.baranov;


import android.content.SearchRecentSuggestionsProvider;

public class ArticleSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "ru.rabotyaga.baranov.ArticleSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;

    public ArticleSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}

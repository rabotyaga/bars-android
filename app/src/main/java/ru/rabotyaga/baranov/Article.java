package ru.rabotyaga.baranov;


import android.support.v4.text.BidiFormatter;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Article {
    Integer nr;
    String ar_inf;
    String ar_inf_wo_vowels;
    String root;
    String translation;
    String form;
    String opts;
    Integer homonymNum;
    String vocalization;

    Spannable highlighted_ar_inf;
    Spannable highlighted_translation;
    Spannable optsSpannable;

    public final static String LABEL = "Словарная статья";

    private final static String ARABIC_VOWELS_REGEXP = "[\\u064b\\u064c\\u064d\\u064e\\u064f\\u0650\\u0651\\u0652\\u0653\\u0670]*";
    public final static String ARABIC_REGEXP = "[\\p{InARABIC}]+((\\s*~)*(\\s*[\\p{InARABIC}]+)+)*";

    public void stubHighlightedFields() {
        highlighted_ar_inf = Spannable.Factory.getInstance().newSpannable(this.ar_inf);

        highlighted_translation = Spannable.Factory.getInstance().newSpannable(this.translation);
    }

    public void stubHighlightedFieldsAndColorArabicInTranslation(int color) {
        stubHighlightedFields();

        //coloring arabic text in translation
        Pattern p = Pattern.compile(ARABIC_REGEXP);
        Matcher m = p.matcher(this.translation);
        while (m.find()) {
            highlighted_translation.setSpan(new ForegroundColorSpan(color), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public void setHighlightedArInf(int color, String query) {

        if(highlighted_ar_inf == null || highlighted_translation == null) {
            stubHighlightedFields();
        }
        //make regexp to skip vowels and match query, find start and end, highlight
        String regex = "";

        for(int i = 0; i < query.length(); i++) {
            regex = regex + query.charAt(i) + ARABIC_VOWELS_REGEXP;
        }

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(this.ar_inf);

        while (m.find()) {
            highlighted_ar_inf.setSpan(new BackgroundColorSpan(color), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

    }

    public void setHighlightedTranslation(int color, String query) {

        if(highlighted_ar_inf == null || highlighted_translation == null) {
            stubHighlightedFields();
        }

        Pattern p = Pattern.compile(query);
        Matcher m = p.matcher(this.translation);

        while (m.find()) {
            highlighted_translation.setSpan(new BackgroundColorSpan(color), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public String translationToHtml() {
        return Html.toHtml(highlighted_translation);
    }

    public String toString() {
        BidiFormatter bidi = BidiFormatter.getInstance();
        StringBuilder str = new StringBuilder();

        if (!form.isEmpty()) {
            str.append(form).append(" ");
        }

        str.append(bidi.unicodeWrap(ar_inf)).append(" ");

        if (homonymNum != null) {
            str.append(homonymNum.toString()).append(" ");
        }

        if (!vocalization.isEmpty()) {
            str.append(vocalization).append(" ");
        }

        if (!opts.isEmpty()) {
            str.append(opts).append(" ");
        }

        str.append(translation);

        return str.toString();
    }

    public String toHtml() {
        BidiFormatter bidi = BidiFormatter.getInstance();
        StringBuilder str = new StringBuilder();

        if (!form.isEmpty()) {
            str.append(form).append(" ");
        }

        str.append(bidi.unicodeWrap(ar_inf)).append(" ");

        if (homonymNum != null) {
            str.append(homonymNum.toString()).append(" ");
        }

        if (!vocalization.isEmpty()) {
            str.append(vocalization).append(" ");
        }

        if (!opts.isEmpty()) {
            str.append(Html.toHtml(optsSpannable)).append(" ");
        }

        str.append(Html.toHtml(highlighted_translation));

        return str.toString();
    }

}

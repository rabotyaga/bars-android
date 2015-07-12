package ru.rabotyaga.baranov;

import android.content.Context;
import android.graphics.Typeface;
import java.util.Hashtable;

class FontCache {
    private static final Hashtable<String, Typeface> fontCache = new Hashtable<>();

    public static Typeface get(@SuppressWarnings("SameParameterValue") String name, Context context) {
        Typeface tf = fontCache.get(name);
        if(tf == null) {
            try {
                tf = Typeface.createFromAsset(context.getAssets(), name);
            }
            catch (Exception e) {
                return null;
            }
            fontCache.put(name, tf);
        }
        return tf;
    }
}
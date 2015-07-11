package ru.rabotyaga.baranov;


import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

public class CustomTextView extends TextView {

    Typeface customTypeface;

    public CustomTextView(Context context) {
        super(context);
        setCustomTypeface(context);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomTypeface(context);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomTypeface(context);
    }

    public void setCustomTypeface(Context context) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            customTypeface = FontCache.get("fonts/NotoSans-Italic.ttf", context);
            setTypeface(customTypeface);
        }
    }
}

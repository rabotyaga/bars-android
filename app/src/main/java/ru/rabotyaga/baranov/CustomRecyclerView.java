package ru.rabotyaga.baranov;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;

public class CustomRecyclerView extends RecyclerView {

    public CustomRecyclerView(Context context) {
        super(context);
    }

    public CustomRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void scrollTo(int x, int y) {
        //just do nothing
        //overriding super
        //because the only thing it does
        //it throws UnsupportedOperationException
        //if the parent layout has the flag
        //android:animateLayoutChanges="true"
    }

    private ContextMenu.ContextMenuInfo mContextMenuInfo = null;

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {
        final int position = getChildAdapterPosition(originalView);
        if (position >= 0) {
            mContextMenuInfo = new RecyclerContextMenuInfo(((ArticleAdapter) getAdapter()).getArticle(position));
            return super.showContextMenuForChild(originalView);
        }
        return false;
    }

    public static class RecyclerContextMenuInfo implements ContextMenu.ContextMenuInfo {

        final public Article article;

        public RecyclerContextMenuInfo(Article article) {
            this.article = article;
        }
    }
}
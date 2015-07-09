package ru.rabotyaga.baranov;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    private List<Article> articleList;

    private int mSelected = -1;
    private boolean mSetOnClickListener = true;

    public ArticleAdapter(List<Article> articleList) {
        this.articleList = articleList;
    }

    public ArticleAdapter(List<Article> articleList, boolean setOnClickListener) {
        this(articleList);
        this.mSetOnClickListener = setOnClickListener;
    }

    public void setList(List<Article> articleList) {
        this.articleList = articleList;
        notifyDataSetChanged();
    }

    public void dropList() {
        this.articleList = new ArrayList<> ();
        notifyDataSetChanged();
    }

    public String getRoot(int position) {
        if (position < articleList.size()) {
            return articleList.get(position).root;
        }
        return null;
    }

    public Article getArticle(int position) {
        if (position < articleList.size()) {
            return articleList.get(position);
        }
        return null;
    }

    public int findByNr(int nr) {
        for (int i = 0; i < articleList.size(); i++) {
            if (articleList.get(i).nr == nr) {
                return i;
            }
        }
        return -1;
    }

    public void setSelected(int position) {
        int oldSelected = mSelected;
        mSelected = position;
        if (oldSelected != -1) {
            notifyItemChanged(oldSelected);
        }
    }

    public void setSelectedNHighlight(int position) {
        setSelected(position);
        notifyItemChanged(mSelected);
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    @Override
    public void onBindViewHolder(ArticleViewHolder articleViewHolder, int i) {

        Article a = articleList.get(i);

        articleViewHolder.vArInf.setText(a.highlighted_ar_inf);

        articleViewHolder.vTranslation.setText(a.highlighted_translation);

        articleViewHolder.vRoot.setText(a.root);
        if (i == 0 || (a.root.equals(articleList.get(i - 1).root))) {
            articleViewHolder.vRoot.setVisibility(View.GONE);
        } else {
            articleViewHolder.vRoot.setVisibility(View.VISIBLE);
        }

        articleViewHolder.vTranscription.setText(a.transcription);

        if (a.opts.isEmpty()) {
            articleViewHolder.vOpts.setVisibility(View.GONE);
        } else {
            articleViewHolder.vOpts.setText(a.optsSpannable);
            articleViewHolder.vOpts.setVisibility(View.VISIBLE);
        }
        if (a.form.isEmpty()) {
            articleViewHolder.vForm.setVisibility(View.GONE);
        } else {
            articleViewHolder.vForm.setText(a.form);
            articleViewHolder.vForm.setVisibility(View.VISIBLE);
        }

        if (a.homonym_nr != null) {
            articleViewHolder.vHomonymNum.setText(a.homonym_nr.toString());
        } else {
            articleViewHolder.vHomonymNum.setText("");
        }

        if (a.vocalization != null) {
            articleViewHolder.vVocalization.setText(a.vocalization);
        } else {
            articleViewHolder.vVocalization.setText("");
        }

        articleViewHolder.setNr(a.nr);

        if (mSelected == i) {
            articleViewHolder.highlightAsSelected();
        } else {
            articleViewHolder.removeHighlight();
        }
    }

    @Override
    public ArticleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ArticleViewHolder avh;
        View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.card_layout, parent, false);
        avh = new ArticleViewHolder(itemView);
        if (mSetOnClickListener) {
            avh.setOnClickListener(itemView);
        }
        avh.setOnLongClickListener(itemView);
        return avh;
    }

    static class ArticleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        final TextView vArInf;
        final TextView vTranslation;
        final TextView vRoot;
        final TextView vOpts;
        final TextView vForm;
        final TextView vHomonymNum;
        final TextView vVocalization;
        final TextView vTranscription;
        final CardView cardView;
        Integer nr = null;


        public ArticleViewHolder(View v) {
            super(v);
            vArInf = (TextView) v.findViewById(R.id.txtArInf);
            vTranslation = (TextView) v.findViewById(R.id.txtTranslation);
            vRoot = (TextView) v.findViewById(R.id.txtRoot);
            vOpts = (TextView) v.findViewById(R.id.txtOpts);
            vForm = (TextView) v.findViewById(R.id.txtForm);
            vHomonymNum = (TextView) v.findViewById(R.id.txtHomonymNum);
            vVocalization = (TextView) v.findViewById(R.id.txtVocalization);
            vTranscription = (TextView) v.findViewById(R.id.txtTranscription);

            cardView = (CardView) v;
        }

        public void setOnClickListener(View v) {
            v.setOnClickListener(this);
        }

        public void setOnLongClickListener(View v) {
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            try {
                MainActivity mainActivity = (MainActivity) v.getContext();
                mainActivity.articleClicked(nr, vRoot.getText().toString(), getAdapterPosition());
                highlightAsSelected();

            } catch (ClassCastException e) {
                // do nothing
                // it means onClick was called for view not in main activity
                // (in details activity)
            }
        }

        @Override
        public boolean onLongClick(View v) {
            v.showContextMenu();
            return true;
        }

        void setNr(Integer nr) {
            this.nr = nr;
        }

        void highlightAsSelected() {
            cardView.setCardBackgroundColor(cardView.getContext().getResources().getColor(R.color.selected_article));
        }

        void removeHighlight() {
            cardView.setCardBackgroundColor(cardView.getContext().getResources().getColor(R.color.card_background));
        }

    }
}

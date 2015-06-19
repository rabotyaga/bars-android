package ru.rabotyaga.baranov;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

class LetterAdapter extends RecyclerView.Adapter<LetterAdapter.LetterViewHolder> {

    private final static int VIEW_TYPE_FULL = 0;
    private final static int VIEW_TYPE_SHORT = 1;

    private final List<Letter> letterList;

    public LetterAdapter(List<Letter> letterList) {
        this.letterList = letterList;
    }

    @Override
    public int getItemCount() {
        return letterList.size();
    }

    @Override
    public void onBindViewHolder(LetterViewHolder letterViewHolder, int i) {

        Letter l = letterList.get(i);

        letterViewHolder.vLetter.setText(String.format("%c", l.letter));
        if (l.has_nr) {
            letterViewHolder.vNr.setText(l.nr.toString());
            letterViewHolder.vNv.setText(l.nv.toString());
        }
        if (l.has_all_writings) {
            letterViewHolder.vInTheBeginning.setText(String.format("%cـ", l.letter));
            letterViewHolder.vInTheMiddle.setText(String.format("ـ%cـ", l.letter));
        } else {
            letterViewHolder.vInTheBeginning.setText(String.format("%c", l.letter));
            letterViewHolder.vInTheMiddle.setText(String.format("ـ%c", l.letter));

        }
        letterViewHolder.vInTheEnd.setText(String.format("ـ%c", l.letter));
        letterViewHolder.vNotes.setText(l.notes);
    }

    @Override
    public LetterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;

        if (viewType == VIEW_TYPE_FULL) {
            itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.letter_card_layout, parent, false);
        } else {
            itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.letter_card_layout_short, parent, false);
        }
        return new LetterViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        Letter l = letterList.get(position);
        if (l.has_nr) {
            return VIEW_TYPE_FULL;
        } else {
            return VIEW_TYPE_SHORT;
        }
    }

    static class LetterViewHolder extends RecyclerView.ViewHolder {

        final TextView vLetter;
        final TextView vNr;
        final TextView vNv;
        final TextView vInTheBeginning;
        final TextView vInTheMiddle;
        final TextView vInTheEnd;
        final TextView vNotes;
        final CardView cardView;


        public LetterViewHolder(View v) {
            super(v);
            vLetter = (TextView) v.findViewById(R.id.txtLetter);
            vNr = (TextView) v.findViewById(R.id.txtNr);
            vNv = (TextView) v.findViewById(R.id.txtNv);
            vInTheBeginning = (TextView) v.findViewById(R.id.txtInTheBeginning);
            vInTheMiddle = (TextView) v.findViewById(R.id.txtInTheMiddle);
            vInTheEnd = (TextView) v.findViewById(R.id.txtInTheEnd);
            vNotes = (TextView) v.findViewById(R.id.txtNotes);
            cardView = (CardView) v;
        }

    }
}

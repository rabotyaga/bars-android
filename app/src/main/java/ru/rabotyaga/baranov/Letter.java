package ru.rabotyaga.baranov;

class Letter {
    final Integer nr;
    final Integer nv;
    final char letter;
    final String notes;
    final boolean has_nr;
    final boolean has_all_writings;

    public Letter(int nr, int nv, char letter, String notes) {
        this(nr, nv, letter, notes, true, true);
    }

    public Letter(int nr, int nv, char letter, String notes, boolean has_all_writings) {
        this(nr, nv, letter, notes, has_all_writings, true);
    }

    public Letter(int nr, int nv, char letter, String notes, boolean has_all_writings, boolean has_nr) {
        this.nr = nr;
        this.nv = nv;
        this.letter = letter;
        this.notes = notes;
        this.has_nr = has_nr;
        this.has_all_writings = has_all_writings;
    }

}


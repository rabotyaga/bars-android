package ru.rabotyaga.baranov;

import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


public class AlphabetFragment extends Fragment  {

    public final static String TAG = AlphabetFragment.class.getSimpleName();

    private final List<Letter> letters = new ArrayList<>();

    private RecyclerView lettersList;

    public AlphabetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        letters.add(new Letter(1, 1, 'ا', "Алиф\nслужит знаком долготы для фатхи\nявляется носителем хамзы", false));
        letters.add(new Letter(0, 0, 'ى', "Алиф максура", false, false));
        letters.add(new Letter(2, 2, 'ب', "Ба"));
        letters.add(new Letter(3, 400, 'ت', "Та"));
        letters.add(new Letter(4, 500, 'ث', "Са"));
        letters.add(new Letter(5, 3, 'ج', "Джим"));
        letters.add(new Letter(6, 8, 'ح', "Ха"));
        letters.add(new Letter(7, 600, 'خ', "Ха"));
        letters.add(new Letter(8, 4, 'د', "Даль", false));
        letters.add(new Letter(9, 700, 'ذ', "Заль", false));
        letters.add(new Letter(10, 200, 'ر', "Ра", false));
        letters.add(new Letter(11, 7, 'ز', "Зайн", false));
        letters.add(new Letter(12, 60, 'س', "Син"));
        letters.add(new Letter(13, 300, 'ش', "Шин"));
        letters.add(new Letter(14, 90, 'ص', "Сад"));
        letters.add(new Letter(15, 800, 'ض', "Дад"));
        letters.add(new Letter(16, 9, 'ط', "Та"));
        letters.add(new Letter(17, 900, 'ظ', "За"));
        letters.add(new Letter(18, 70, 'ع', "Ъайн"));
        letters.add(new Letter(19, 1000, 'غ', "Гайн"));
        letters.add(new Letter(20, 80, 'ف', "Фа"));
        letters.add(new Letter(21, 100, 'ق', "Каф"));
        letters.add(new Letter(22, 20, 'ك', "Кяф"));
        letters.add(new Letter(23, 30, 'ل', "Лям"));
        letters.add(new Letter(24, 40, 'م', "Мим"));
        letters.add(new Letter(25, 50, 'ن', "Нун"));
        letters.add(new Letter(26, 5, 'ه', "Ха"));
        letters.add(new Letter(0, 0, 'ة', "Та марбута", false, false));
        letters.add(new Letter(27, 6, 'و', "Вав\nслужит знаком долготы для даммы\nявляется носителем хамзы", false));
        letters.add(new Letter(28, 10, 'ي', "Йа\nслужит знаком долготы для кясры\nявляется носителем хамзы"));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_alphabet, container, false);

        lettersList = (RecyclerView) v.findViewById(R.id.letters_list);

        lettersList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        lettersList.setLayoutManager(llm);

        lettersList.setAdapter(new LetterAdapter(letters));

        return v;
    }


}

package ru.rabotyaga.baranov;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class AboutFragment extends Fragment {

    public final static String TAG = AboutFragment.class.getSimpleName();

    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_about, container, false);

        ImageView imageView = (ImageView) v.findViewById(R.id.imgLogo);
        imageView.setImageResource(R.drawable.logo);

        String version = String.format(getResources().getString(R.string.version), BuildConfig.VERSION_NAME);
        TextView txtVersion = (TextView) v.findViewById(R.id.txtVersion);
        txtVersion.setText(version);

        TextView bLeaveComment = (TextView) v.findViewById(R.id.bLeaveComment);
        bLeaveComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + MainActivity.PACKAGE_NAME)));
            }
        });

        TextView bGoToSite = (TextView) v.findViewById(R.id.bGoToSite);
        bGoToSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.site_url))));
            }
        });


        return v;
    }


}

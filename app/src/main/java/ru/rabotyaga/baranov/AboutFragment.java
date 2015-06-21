package ru.rabotyaga.baranov;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


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
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + MainActivity.PACKAGE_NAME));
                if(!safeStartActivity(intent)) {
                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + MainActivity.PACKAGE_NAME));
                    if (!safeStartActivity(intent)) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.unable_to_open_market) , Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        TextView bGoToSite = (TextView) v.findViewById(R.id.bGoToSite);
        bGoToSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!safeStartActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.site_url))))) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.unable_to_open_site) , Toast.LENGTH_SHORT).show();
                }
            }
        });


        return v;
    }

    private boolean safeStartActivity(Intent intent) {
        try {
            startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }


}

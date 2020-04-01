package com.example.anhquoc.mycustom;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentOne extends Fragment {

    public static String TAG = FragmentOne.class.getSimpleName();

    public static FragmentOne newInstance() {
        FragmentOne fragment = new FragmentOne();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_one, container, false);

        View textView = v.findViewById(R.id.textView1);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment to = FragmentTwo.newInstance();
                NavController.add(getChildFragmentManager(), R.id.nav_host, to, FragmentTwo.TAG, true, true);
            }
        });

        return v;
    }
}

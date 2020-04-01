package com.example.anhquoc.mycustom;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentTwo extends Fragment {

    public static String TAG = FragmentTwo.class.getSimpleName();

    public static FragmentTwo newInstance() {
        FragmentTwo fragment = new FragmentTwo();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_two, container, false);

        View textView = v.findViewById(R.id.textView2);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment to = FragmentThree.newInstance();
                NavController.add(getFragmentManager(), R.id.nav_host, to, FragmentThree.TAG,true, true);
            }
        });

        return v;
    }
}

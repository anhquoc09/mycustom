package com.example.anhquoc.mycustom;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentThree extends Fragment {

    public static String TAG = FragmentThree.class.getSimpleName();

    public static FragmentThree newInstance() {
        FragmentThree fragment = new FragmentThree();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_three, container, false);

        View textView = v.findViewById(R.id.textView3);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment to = FragmentFour.newInstance();
                NavController.add(getFragmentManager(), R.id.nav_host, to, FragmentFour.TAG, true, true);
            }
        });

        return v;
    }
}

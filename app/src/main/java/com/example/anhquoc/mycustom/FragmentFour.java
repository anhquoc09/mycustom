package com.example.anhquoc.mycustom;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentFour extends Fragment {

    public static String TAG = FragmentFour.class.getSimpleName();

    public static FragmentFour newInstance() {
        FragmentFour fragment = new FragmentFour();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_four, container, false);

        View textView = v.findViewById(R.id.textView4);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController.popBackStack(getFragmentManager(), FragmentThree.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        return v;
    }
}

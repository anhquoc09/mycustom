package com.example.anhquoc.mycustom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.anhquoc.mycustom.navigation.BackPressedListenerFragment;
import com.example.anhquoc.mycustom.navigation.Interfaces;


public class FragmentC extends BackPressedListenerFragment {

    public static final String TAG = FragmentC.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_c, null);

        root.findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Injector.getNavigator().toFragmentD(getFragmentManager(), "Custom Text");
            }
        });
        return root;
    }

    @Override
    public void onBackPressed() {
        Injector.getNavigator().toFragmentB(getFragmentManager());
    }
}

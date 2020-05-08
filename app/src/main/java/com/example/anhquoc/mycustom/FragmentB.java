package com.example.anhquoc.mycustom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.anhquoc.mycustom.navigation.Interfaces;


public class FragmentB extends Fragment implements Interfaces.OnBackPressedListener {

    public static final String TAG = FragmentB.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_b, null);

        root.findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Injector.getNavigator().toFragmentC(getFragmentManager());
            }
        });
        return root;
    }

    @Override
    public boolean onBackPressed() {
        Injector.getNavigator().toFragmentA(getFragmentManager());
        return true;
    }
}

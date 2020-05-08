package com.example.anhquoc.mycustom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.anhquoc.mycustom.navigation.Interfaces;

public class FragmentD extends Fragment implements Interfaces.OnBackPressedListener {

    public static final String TAG = FragmentD.class.getSimpleName();

    private static final String KEY_CUSTOM_TEXT = "key_text";

    public static Fragment newInstance(String customText) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_CUSTOM_TEXT, customText);
        Fragment fragment = new FragmentD();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_d, null);

        Bundle arguments = getArguments();
        if (arguments != null) {
            String customText = arguments.getString(KEY_CUSTOM_TEXT);
            TextView textView = root.findViewById(R.id.fragment_d_title);
            textView.setText(customText);
        }

        root.findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Injector.getNavigator().toFragmentA(getFragmentManager());
                Log.d("haq", "Number of fragments: " + getFragmentManager().getBackStackEntryCount());
            }
        });

        return root;
    }

    @Override
    public boolean onBackPressed() {
        Injector.getNavigator().toFragmentC(getFragmentManager());
        return true;
    }
}

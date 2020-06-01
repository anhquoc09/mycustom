package com.example.anhquoc.mycustom.navigation;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;

/**
 * Copyright (C) 2020, VNG Corporation.
 * Created by quocha2
 * On 08/05/2020
 */

public abstract class BackPressedListenerFragment extends Fragment {

    @CallSuper
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.setFocusableInTouchMode(true);
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    onBackPressed();
                    return true;
                }
                return false;
            }
        });
        view.requestFocus();
    }

    protected abstract void onBackPressed();
}

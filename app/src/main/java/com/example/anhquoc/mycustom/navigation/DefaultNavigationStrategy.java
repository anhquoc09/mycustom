package com.example.anhquoc.mycustom.navigation;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.example.anhquoc.mycustom.R;

public class DefaultNavigationStrategy extends NavigationStrategy {

    public DefaultNavigationStrategy(int containerId) {
        super(containerId);
    }

    @Override
    public boolean add(FragmentManager fragmentManager, Fragment target) {
        return add(fragmentManager,
                target,
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out);
    }

    @Override
    public boolean replace(FragmentManager fragmentManager, Fragment target) {
        return replace(fragmentManager,
                target,
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out);
    }
}
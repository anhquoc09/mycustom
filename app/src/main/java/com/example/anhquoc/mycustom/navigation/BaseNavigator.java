package com.example.anhquoc.mycustom.navigation;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

public class BaseNavigator {

    protected NavigationStrategy mFragmentNavigationStrategy;

    public BaseNavigator(NavigationStrategy navigationStrategy) {
        mFragmentNavigationStrategy = navigationStrategy;
    }

    public boolean navigate(FragmentManager fragmentManager, Fragment target, boolean removeCurrentFragment) {
        if (mFragmentNavigationStrategy == null || fragmentManager == null || target == null) {
            return false;
        }
        Log.d("haq", "BackStackCount: " + fragmentManager.getBackStackEntryCount());
        if (mFragmentNavigationStrategy.popBackStack(fragmentManager, target.getClass().getSimpleName())) {
            Log.d("haq", "PopBackStack: " + target.getClass().getSimpleName());
            return true;
        }

        Log.d("haq", "Add/Replace: " + target.getClass().getSimpleName());
        return removeCurrentFragment ? mFragmentNavigationStrategy.replace(fragmentManager, target)
                : mFragmentNavigationStrategy.add(fragmentManager, target);
    }
}

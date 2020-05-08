package com.example.anhquoc.mycustom;

import android.support.v4.app.FragmentManager;

import com.example.anhquoc.mycustom.navigation.BaseNavigator;
import com.example.anhquoc.mycustom.navigation.NavigationStrategy;

public class SimpleNavigator extends BaseNavigator {

    public SimpleNavigator(NavigationStrategy fragmentNavigationStrategy) {
        super(fragmentNavigationStrategy);
    }

    public void toFragmentA(FragmentManager fragmentManager) {
        navigate(fragmentManager, new FragmentA(), true);
    }

    public void toFragmentB(FragmentManager fragmentManager) {
        navigate(fragmentManager, new FragmentB(), false);
    }

    public void toFragmentC(FragmentManager fragmentManager) {
        navigate(fragmentManager, new FragmentC(), true);
    }

    public void toFragmentD(FragmentManager fragmentManager, String customText) {
        navigate(fragmentManager, FragmentD.newInstance(customText), true);
    }
}

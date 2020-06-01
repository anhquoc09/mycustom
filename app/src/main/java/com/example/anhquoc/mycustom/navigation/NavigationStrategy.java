package com.example.anhquoc.mycustom.navigation;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import static android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;

public class NavigationStrategy {

    int mContainerId;

    public NavigationStrategy(int containerId) {
        mContainerId = containerId;
    }

    public boolean isSameFragment(Fragment currentFragment, Fragment target) {
        return target.getClass().isInstance(currentFragment);
    }

    public boolean replace(FragmentManager fragmentManager, Fragment target) {
        return replace(fragmentManager, target, 0, 0, 0, 0);
    }

    public boolean replace(FragmentManager fragmentManager, Fragment target, int enterAnimId, int exitAnimId, int popEnterAnimId, int popExitAnimId) {
        if (fragmentManager == null) {
            return false;
        }

        Fragment currentFragment = fragmentManager.findFragmentById(mContainerId);
        if (target == null || isSameFragment(currentFragment, target)) {
            return false;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(enterAnimId, exitAnimId, popEnterAnimId, popExitAnimId);
        transaction.replace(mContainerId, target);
        transaction.addToBackStack(target.getClass().getSimpleName());
        transaction.commit();
        return true;
    }

    public boolean add(FragmentManager fragmentManager, Fragment target) {
        return add(fragmentManager, target, 0, 0, 0, 0);
    }

    public boolean add(FragmentManager fragmentManager, Fragment target, int enterAnimId, int exitAnimId, int popEnterAnimId, int popExitAnimId) {
        if (fragmentManager == null) {
            return false;
        }

        Fragment currentFragment = fragmentManager.findFragmentById(mContainerId);

        if (target == null || isSameFragment(currentFragment, target)) {
            return false;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(enterAnimId, exitAnimId, popEnterAnimId, popExitAnimId);
        transaction.add(mContainerId, target);
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        transaction.addToBackStack(target.getClass().getSimpleName());
        transaction.commit();
        return true;
    }

    public boolean popBackStack(FragmentManager fragmentManager) {
        return popBackStack(fragmentManager, null, 0);
    }

    public boolean popBackStack(FragmentManager fragmentManager, String tag) {
        return popBackStack(fragmentManager, tag, 0);
    }

    public boolean popBackStackInclusive(FragmentManager fragmentManager, String tag) {
        return popBackStack(fragmentManager, tag, POP_BACK_STACK_INCLUSIVE);
    }

    public boolean popBackStack(FragmentManager fragmentManager, String tag, int flag) {
        if (fragmentManager == null || fragmentManager.getBackStackEntryCount() <= 0) {
            return false;
        }

        if (tag != null) {
            int numOfBackStack = fragmentManager.getBackStackEntryCount();
            for (int i = 0; i < numOfBackStack; i++) {
                if (tag.equals(fragmentManager.getBackStackEntryAt(i).getName())) {
                    fragmentManager.popBackStack(tag, flag);
                    return true;
                }
            }
            return false;
        }

        fragmentManager.popBackStack();
        return true;
    }
}

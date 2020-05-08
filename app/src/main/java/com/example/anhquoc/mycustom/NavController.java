package com.example.anhquoc.mycustom;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import static android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;

public class NavController {

    public static void add(FragmentManager fragmentManager,
                           int containerId,
                           Fragment desFragment,
                           String tag,
                           boolean hidePrevious,
                           boolean addToBackStack) {

        if (desFragment == null) {
            return;
        }

        final Fragment previousFragment = fragmentManager.findFragmentById(containerId);

        final FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (previousFragment != null && hidePrevious) {
            transaction.hide(previousFragment);
        }

        transaction.add(containerId, desFragment, tag);

        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }

        transaction.commit();
    }

    public static void add(FragmentManager fragmentManager,
                           int containerId,
                           Fragment desFragment,
                           String tag,
                           boolean hidePrevious,
                           boolean addToBackStack,
                           int enterAnim,
                           int exitAnim,
                           int popEnterAnim,
                           int popExitAnim) {

        if (desFragment == null) {
            return;
        }

        final Fragment previousFragment = fragmentManager.findFragmentById(containerId);

        final FragmentTransaction transaction = fragmentManager.beginTransaction()
                .setCustomAnimations(enterAnim,
                        exitAnim,
                        popEnterAnim,
                        popExitAnim);
        if (previousFragment != null && hidePrevious) {
            transaction.hide(previousFragment);
        }

        transaction.add(containerId, desFragment, tag);

        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }

        transaction.commit();
    }

    public static void replace(FragmentManager fragmentManager, int containerId, Fragment desFragment, String tag) {

        if (desFragment == null) {
            return;
        }

        fragmentManager.beginTransaction()
                .replace(containerId, desFragment, tag)
                .addToBackStack(tag)
                .commit();
    }

    public static void replace(FragmentManager fragmentManager,
                               int containerId,
                               Fragment desFragment,
                               String tag,
                               int enterAnim,
                               int exitAnim,
                               int popEnterAnim,
                               int popExitAnim) {

        if (desFragment == null) {
            return;
        }

        fragmentManager.beginTransaction()
                .setCustomAnimations(enterAnim,
                        exitAnim,
                        popEnterAnim,
                        popExitAnim)
                .replace(containerId, desFragment, tag)
                .addToBackStack(tag)
                .commit();
    }

    public static boolean popBackStack(FragmentManager fragmentManager, String tag) {
        return popBackStack(fragmentManager, tag, 0);
    }

    public static boolean popBackStack(FragmentManager fragmentManager) {
        return popBackStack(fragmentManager, null, 0);
    }

    public static boolean popBackStackInclusive(FragmentManager fragmentManager, String tag) {
        return popBackStack(fragmentManager, tag, POP_BACK_STACK_INCLUSIVE);
    }

    public static boolean popBackStack(FragmentManager fragmentManager, int flag) {
        return popBackStack(fragmentManager, null, flag);
    }

    public static boolean popBackStack(FragmentManager fragmentManager, String tag, int flag) {

        if (fragmentManager == null || fragmentManager.getBackStackEntryCount() <= 0) {
            return false;
        }

        if (tag != null && fragmentManager.findFragmentByTag(tag) == null) {
            return false;
        }

        fragmentManager.popBackStack(tag, flag);
        return true;
    }

    public static boolean popBackStackImmediate(FragmentManager fragmentManager, String tag) {
        return popBackStackImmediate(fragmentManager, tag, 0);
    }

    public static boolean popBackStackImmediate(FragmentManager fragmentManager, String tag, int flag) {
        if (fragmentManager == null || fragmentManager.getBackStackEntryCount() <= 0) {
            return false;
        }

        if (tag != null && fragmentManager.findFragmentByTag(tag) == null) {
            return false;
        }

        fragmentManager.popBackStackImmediate(tag, flag);
        return true;
    }
}

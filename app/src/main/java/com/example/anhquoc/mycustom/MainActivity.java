package com.example.anhquoc.mycustom;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.example.anhquoc.mycustom.navigation.Interfaces;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Injector.setContainerId(R.id.fragmentContainer);
        Injector.getNavigator().toFragmentA(getSupportFragmentManager());
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            super.onBackPressed();
        }
        Interfaces.OnBackPressedListener currentPage = getCurrentPage();
        if (currentPage == null || !currentPage.onBackPressed()) super.onBackPressed();
    }

    private Interfaces.OnBackPressedListener getCurrentPage() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

        if (fragment instanceof Interfaces.OnBackPressedListener) {
            return (Interfaces.OnBackPressedListener) fragment;
        }
        return null;
    }
}

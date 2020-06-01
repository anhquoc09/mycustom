package com.example.anhquoc.mycustom;

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

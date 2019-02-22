package com.example.anhquoc.mycustom;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class CustomSwipeRefreshLayout extends ViewGroup {

    private static final String TAG = SwipeRefreshLayout.class.getSimpleName();

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    public CustomSwipeRefreshLayout(Context context) {
        super(context);
    }

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}

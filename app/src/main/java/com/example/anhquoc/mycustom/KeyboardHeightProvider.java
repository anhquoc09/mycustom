package com.example.anhquoc.mycustom;

/**
 * Copyright (C) 2019, VNG Corporation.
 * Created by quocha2
 * On 25/10/2019
 */

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.PopupWindow;

public class KeyboardHeightProvider extends PopupWindow {

    private final static String TAG = "sample_KeyboardHeightProvider";
    private KeyboardHeightObserver observer;
    private int keyboardLandscapeHeight;
    private int keyboardPortraitHeight;
    private View popupView;
    private View parentView;
    private FragmentActivity activity;

    public KeyboardHeightProvider(FragmentActivity activity) {
        super(activity);
        this.activity = activity;

        LayoutInflater inflator = (LayoutInflater) activity.getSystemService(FragmentActivity.LAYOUT_INFLATER_SERVICE);
        this.popupView = inflator.inflate(R.layout.popupwindow, null, false);
        setContentView(popupView);

        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);

        parentView = activity.findViewById(android.R.id.content);

        setWidth(0);
        setHeight(WindowManager.LayoutParams.MATCH_PARENT);

        popupView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                if (popupView != null) {
                    handleOnGlobalLayout();
                }
            }
        });
    }

    /**
     * Start the KeyboardHeightProvider, this must be called after the onResume of the Activity.
     * PopupWindows are not allowed to be registered before the onResume has finished
     * of the Activity.
     */
    public void start() {

        if (!isShowing() && parentView.getWindowToken() != null) {
            setBackgroundDrawable(new ColorDrawable(0));
            showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0);
        }
    }

    public void close() {
        this.observer = null;
        dismiss();
    }

    public void setKeyboardHeightObserver(KeyboardHeightObserver observer) {
        this.observer = observer;
    }

    private int getScreenOrientation() {
        return activity.getResources().getConfiguration().orientation;
    }

    private void handleOnGlobalLayout() {

        Point screenSize = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(screenSize);

        Rect rect = new Rect();
        popupView.getWindowVisibleDisplayFrame(rect);

        int orientation = getScreenOrientation();
        int keyboardHeight = screenSize.y - rect.bottom;

        if (keyboardHeight > 100) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                this.keyboardPortraitHeight = keyboardHeight;
                notifyKeyboardHeightChanged(keyboardPortraitHeight, orientation);
            } else {
                this.keyboardLandscapeHeight = keyboardHeight;
                notifyKeyboardHeightChanged(keyboardLandscapeHeight, orientation);
            }
        }
    }

    private void notifyKeyboardHeightChanged(int height, int orientation) {
        if (observer != null) {
            observer.onKeyboardHeightChanged(height, orientation);
        }
    }
}
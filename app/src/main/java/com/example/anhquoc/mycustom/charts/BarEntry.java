package com.example.anhquoc.mycustom.charts;

import android.graphics.Rect;

public class BarEntry {
    private String mXAxisName;

    private int mValue;

    private Rect mRect;

    private boolean mIsSelected = false;

    public BarEntry(String XAxisName, int value) {
        mXAxisName = XAxisName;
        mValue = value;
    }

    public String getXAxisName() {
        return mXAxisName;
    }

    public void setXAxisName(String XAxisName) {
        mXAxisName = XAxisName;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        mValue = value;
    }

    public void setSelected(boolean isSelected) {
        mIsSelected = isSelected;
    }

    public Rect getRect() {
        return mRect;
    }

    public void setRect(Rect mRect) {
        this.mRect = mRect;
    }

    public boolean isIsSelected() {
        return mIsSelected;
    }
}

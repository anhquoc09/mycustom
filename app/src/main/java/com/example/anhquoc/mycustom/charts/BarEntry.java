package com.example.anhquoc.mycustom.charts;

import android.graphics.RectF;

public class BarEntry {
    private String mXAxisName;

    private int mValue;

    private RectF mRect;

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

    public RectF getRectF() {
        return mRect;
    }

    public void setRect(RectF mRect) {
        this.mRect = mRect;
    }

    public boolean isIsSelected() {
        return mIsSelected;
    }
}

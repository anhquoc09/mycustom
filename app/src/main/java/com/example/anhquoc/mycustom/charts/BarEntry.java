package com.example.anhquoc.mycustom.charts;

import android.graphics.RectF;

public class BarEntry extends Entry {
    private String mXAxisName;

    private RectF mBound;

    private boolean mIsSelected = false;

    public BarEntry(String XAxisName, float value) {
        mXAxisName = XAxisName;
        mValue = value;
        mBound = new RectF();
    }

    public String getXAxisName() {
        return mXAxisName;
    }

    public void setXAxisName(String XAxisName) {
        mXAxisName = XAxisName;
    }

    public void setSelected(boolean isSelected) {
        mIsSelected = isSelected;
    }

    public RectF getBound() {
        return mBound;
    }

    public void setBound(RectF mRect) {
        this.mBound = mRect;
    }

    public boolean isSelected() {
        return mIsSelected;
    }
}

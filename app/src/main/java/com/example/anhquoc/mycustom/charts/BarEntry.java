package com.example.anhquoc.mycustom.charts;

import android.graphics.Canvas;
import android.graphics.RectF;

public class BarEntry extends Entry {

    private RectF mBound;

    private boolean mIsSelected = false;

    public BarEntry(float value) {
        mValue = value;
        mBound = new RectF();
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

    public void draw(Canvas c) {

    }
}

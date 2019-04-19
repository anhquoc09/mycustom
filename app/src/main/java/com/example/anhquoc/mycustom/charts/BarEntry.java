package com.example.anhquoc.mycustom.charts;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

public class BarEntry extends Entry {

    private RectF mBound;

    private float mBarWidth;

    private float mBarDistance;

    private boolean mIsSelected = false;

    private float mScale;

    public BarEntry(float value, float barWidth, float barDistance) {
        mValue = value;
        mBarWidth = barWidth;
        mBarDistance = barDistance;
        mBound = new RectF();

        initPaint();
    }

    private void initPaint() {
        mEntryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        LinearGradient gradient = new LinearGradient(0, 0, 0, 800,
                Color.CYAN, Color.BLUE, Shader.TileMode.CLAMP);
        mEntryPaint.setShader(gradient);
    }

    public void setSelected(boolean isSelected) {
        mIsSelected = isSelected;
    }

    public RectF getBound() {
        return mBound;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void draw(Canvas c, RectF entriesBound, float valueRatio, int i, float scale) {
        mScale = scale;

        calcBound(entriesBound, valueRatio, i);

        c.drawRect(mBound, mEntryPaint);
    }

    private void calcBound(RectF entriesBound, float valueRatio, int i) {
        mBound.bottom = entriesBound.bottom;
        mBound.left = entriesBound.left + mBarDistance * mScale / 2 + (mBarWidth + mBarDistance) * mScale * i;
        mBound.right = mBound.left + mBarWidth * mScale;
        mBound.top = mBound.bottom - mValue * valueRatio;
    }

    public boolean isContain(float x, float y) {
        return mBound.contains(x, y);
    }

}

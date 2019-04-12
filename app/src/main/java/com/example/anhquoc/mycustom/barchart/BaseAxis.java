package com.example.anhquoc.mycustom.barchart;

import android.graphics.Paint;

public class BaseAxis {

    protected float mLabelAndLinePadding;

    protected float mTextSize;

    protected int mAlpha;

    protected int mColor;

    protected Paint mLinePaint;

    protected Paint mLabelPaint;

    public BaseAxis(float textSize, int alpha, int color, float labelAndLinePadding) {
        mTextSize = textSize;
        mAlpha = alpha;
        mColor = color;
        mLabelAndLinePadding = labelAndLinePadding;

        initPaint();
    }

    public void initPaint() {
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(mColor);
        mLinePaint.setStrokeWidth(1f);

        mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLabelPaint.setColor(mColor);
        mLabelPaint.setTextSize(mTextSize);
    }

    private void setColor(int color) {
        mColor = color;
        mLabelPaint.setColor(mColor);
        mLinePaint.setColor(mColor);
    }

    private void setTextSize(int textSize) {
        mTextSize = textSize;
        mLabelPaint.setTextSize(mTextSize);
        mLinePaint.setTextSize(mTextSize);
    }

    public void setAlpha(int alpha) {
        mAlpha = alpha;
        mLabelPaint.setAlpha(mAlpha);
        mLinePaint.setAlpha(mAlpha);
    }
}

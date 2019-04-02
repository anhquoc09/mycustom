package com.example.anhquoc.mycustom.barchart;

import android.graphics.Paint;

public class BaseAxis {

    public static final float LABEL_AND_AXIS_PADDING = 10;

    private float mTextSize;

    private int mAlpha;

    private int mColor;

    public Paint mAxisLinePaint;

    public Paint mAxisLabelPaint;

    public BaseAxis(float textSize, int alpha, int color) {
        mTextSize = textSize;
        mAlpha = alpha;
        mColor = color;

        initPaint();
    }

    public void initPaint() {
        mAxisLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAxisLinePaint.setColor(mColor);
        mAxisLinePaint.setStrokeWidth(1f);

        mAxisLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAxisLabelPaint.setColor(mColor);
        mAxisLabelPaint.setTextSize(mTextSize);
    }

    private void setColor(int color) {
        mColor = color;
        mAxisLabelPaint.setColor(mColor);
        mAxisLinePaint.setColor(mColor);
    }

    private void setTextSize(float textSize) {
        mTextSize = textSize;
        mAxisLabelPaint.setTextSize(mTextSize);
        mAxisLinePaint.setTextSize(mTextSize);
    }

    public void setAlpha(int alpha) {
        mAlpha = alpha;
        mAxisLabelPaint.setAlpha(mAlpha);
        mAxisLinePaint.setAlpha(mAlpha);
    }
}

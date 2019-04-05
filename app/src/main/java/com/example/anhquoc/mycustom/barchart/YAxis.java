package com.example.anhquoc.mycustom.barchart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.Locale;

public class YAxis extends BaseAxis {

    private float mMaxValue = 100;

    private float mExtraLineSpace;

    private float mTranslateY;

    private float mScaleY;

    private RectF mContentRect = new RectF();

    private Rect mTextBound = new Rect();

    private Paint mDotPaint;

    public YAxis(float textSize, int alpha, int color, float labelAndLinePadding) {
        super(textSize, alpha, color, labelAndLinePadding);

        mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDotPaint.setColor(mColor);
    }

    private void calculateMaxTextBound() {
        String text = String.format(Locale.US, "%.1f", mMaxValue);
        mLabelPaint.getTextBounds(text, 0, text.length(), mTextBound);
    }

    private void calculateContentRect(RectF originRect) {
        mContentRect.left = originRect.left;
        mContentRect.top = originRect.top;
        mContentRect.right = originRect.right;
        mContentRect.bottom = originRect.bottom - mTextBound.height() + mLabelAndLindPadding * 2;
    }

    public void drawYAxis(Canvas c, RectF originRect, float translateY, float scale) {
        mTranslateY = translateY;
        mScaleY = scale;
        calculateMaxTextBound();
        calculateContentRect(originRect);

        mExtraLineSpace = (mContentRect.height() - mTextBound.height()) / 4 * mScaleY;

        mLinePaint.setAlpha(mAlpha);
        mLabelPaint.setAlpha(mAlpha);

        c.save();
        c.translate(0, mTranslateY);

        float lineStopX = mContentRect.right - mTextBound.width() - mLabelAndLindPadding * 2;
        float labelX = lineStopX + mLabelAndLindPadding;

        for (int i = 0; i < 5; i++) {
            float y = mContentRect.bottom - (float) mTextBound.height() / 2 - mExtraLineSpace * i;
            c.drawLine(mContentRect.left, y, lineStopX, y, mLinePaint);

            float labelY = y + (float) mTextBound.height() / 2;
            String label = String.valueOf(mMaxValue / 4 * i);
            c.drawText(label, labelX, labelY, mLabelPaint);
        }

        c.restore();
    }

    public void drawSelected(Canvas canvas, float x, float y) {
        mLinePaint.setAlpha(255);
        canvas.drawLine(mContentRect.left, y, mContentRect.right, y, mLinePaint);

        mDotPaint.setAlpha(150);
        canvas.drawCircle(x, y, 5, mDotPaint);
        mDotPaint.setAlpha(50);
        canvas.drawCircle(x, y, 10, mDotPaint);
    }

    public float getTop() {
        return mContentRect.top + (float) mTextBound.height() / 2;
    }

    public float getBottom() {
        return mContentRect.bottom - (float) mTextBound.height() / 2;
    }

    public void setMaxValue(float maxValue) {
        mMaxValue = maxValue;
    }

    public float getMaxValue() {
        return mMaxValue;
    }
}

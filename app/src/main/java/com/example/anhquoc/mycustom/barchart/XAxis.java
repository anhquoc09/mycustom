package com.example.anhquoc.mycustom.barchart;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

public class XAxis extends BaseAxis {

    private String[] mListLabel = {
            "JANUARY",
            "FEBRUARY",
            "MARCH",
            "APRIL",
            "MAY",
            "JANE",
            "JULY",
            "AUGUST",
            "SEPTEMBER",
            "OCTOBER",
            "NOVEMBER",
            "DECEMBER"};

    private float mBarWidth;

    private float mBarDistance;

    private float mLabelWidth;

    private float mTranslateX;

    private float mScaleX = 1f;

    private Rect mMaxValueTextBound = new Rect();

    private Rect mLabelTextBound = new Rect();

    private RectF mContentRect = new RectF();

    public XAxis(float textSize, int alpha, int color, float labelAndAxisPadding, float barWidth, float barDistance) {
        super(textSize, alpha, color, labelAndAxisPadding);

        mBarWidth = barWidth;
        mBarDistance = barDistance;
    }

    private void calculateContentRect(RectF originRect) {
        mContentRect.left = originRect.left;
        mContentRect.top = originRect.top;
        mContentRect.right = originRect.right - mMaxValueTextBound.width() - mLabelAndLinePadding * 2;
        mContentRect.bottom = originRect.bottom;
    }

    public void drawXAxis(Canvas c, RectF originRect, Rect textBound, float translateX, float scaleX) {
        mTranslateX = translateX;
        mScaleX = scaleX;
        mMaxValueTextBound = textBound;
        mLabelWidth = (mBarWidth * 3 + mBarDistance * 2) * mScaleX;

        calculateContentRect(originRect);
        mLabelPaint.setAlpha(mAlpha);

        c.save();
        c.clipRect(mContentRect);
        c.translate(-mTranslateX, 0);

        float labelY = mContentRect.bottom - mLabelAndLinePadding;

        for (int i = 0; i < 12; i++) {
            mLabelPaint.getTextBounds(mListLabel[i], 0, mListLabel[i].length(), mLabelTextBound);
            float x = getStartEntries(i) + (mLabelWidth - mLabelTextBound.width()) / 2;

            c.drawText(mListLabel[i], x, labelY, mLabelPaint);
        }

        c.restore();
    }

    public float getLeft() {
        return mContentRect.left;
    }

    public float getRight() {
        return mContentRect.right;
    }

    public float getXAxisHeight() {
        return mMaxValueTextBound.height() + mLabelAndLinePadding * 2;
    }

    public float getMaxContentWidth() {
        return mLabelWidth * 12 + mBarDistance * mScaleX * 12;
    }

    public float getStartEntries(int i) {
        return mContentRect.left + (mLabelWidth + mBarDistance * mScaleX) * i;
    }
}

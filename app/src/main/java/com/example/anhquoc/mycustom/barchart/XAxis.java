package com.example.anhquoc.mycustom.barchart;

import android.graphics.Canvas;
import android.graphics.RectF;

public class XAxis extends BaseAxis {

    private float mLabelWidth;

    private float mTranslateX;

    private float mScaleX;

    private RectF mContentRect = new RectF();

    public XAxis(float textSize, int alpha, int color, float labelAndAxisPadding, float labelWidth) {
        super(textSize, alpha, color, labelAndAxisPadding);

        mLabelWidth = labelWidth;
    }

    public void drawXAxis(Canvas c, RectF originRect, float translateX, float scaleX) {
        mTranslateX = translateX;
        mScaleX = scaleX;


    }
}

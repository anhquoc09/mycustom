package com.example.anhquoc.mycustom.barchart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

public class YAxis extends BaseAxis {

    private float mMaxValue;

    private float mMinValue;

    private float mTextSize;

    private int mAlpha;

    private int mColor;

    private Paint mLinePaint;

    private Paint mTextPaint;

    public YAxis(float textSize, int alpha, int color) {
        super(textSize, alpha, color);
    }

    public void drawLine(Canvas c, Point start, Point stop) {
        c.save();
        c.drawLine(start.x, start.y, stop.x, stop.y, mAxisLinePaint);
        c.restore();
    }

    public void drawLine(Canvas c, String text, float x, float y) {
        c.save();
        c.drawText(text, x, y, mAxisLinePaint);
        c.restore();
    }
}

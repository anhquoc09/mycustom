package com.example.anhquoc.mycustom.barchart;

import android.graphics.Canvas;

public class XAxis extends BaseAxis {

    public XAxis(float textSize, int alpha, int color) {
        super(textSize, alpha, color);
    }

    public void drawLine(Canvas c, float startX, float startY, float stopX, float stopY, float translateX, float translateY) {
        c.save();
        c.translate(translateX, translateY);
        c.drawLine(startX, startY, stopX, stopY, mAxisLinePaint);
        c.restore();
    }
}

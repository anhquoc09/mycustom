package com.example.anhquoc.mycustom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class TouchExample extends View {
    final static int MAX_POINTERS = 5;
    private Pointer[] mPointers = new Pointer[MAX_POINTERS];

    private Paint mPaint;
    private float mFontSize;

    public TouchExample(Context context) {
        super(context);
        mFontSize = 16 * getResources().getDisplayMetrics().density;
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(mFontSize);
        for (int i = 0; i<MAX_POINTERS; i++) {
            mPointers[i] = new Pointer();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Pointer p : mPointers) {
            if (p.index != -1) {
                String text = "Index: " + p.index + " ID: " + p.id;
                canvas.drawText(text, p.x, p.y, mPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerCount = Math.min(event.getPointerCount(),MAX_POINTERS);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_MOVE:
                // Clear previous pointers
                for (int id = 0; id<MAX_POINTERS; id++)
                mPointers[id].index = -1;
                // Now fill in the current pointers
                for (int i = 0; i<pointerCount; i++) {
                int id = event.getPointerId(i);
                Pointer pointer = mPointers[id];
                pointer.index = i;
                pointer.id = id;
                pointer.x = event.getX(i);
                pointer.y = event.getY(i);
            }
            invalidate();
            break;
            case MotionEvent.ACTION_CANCEL:
                for (int i = 0; i<pointerCount; i++) {
                int id = event.getPointerId(i);
                mPointers[id].index = -1;
            }
            invalidate();
            break;
        }
        return true;
    }
}

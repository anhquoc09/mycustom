package com.example.anhquoc.mycustom.charts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.example.anhquoc.mycustom.Pointer;
import com.example.anhquoc.mycustom.R;

import java.util.ArrayList;
import java.util.List;

public class BarChart extends View {

    private Context mContext;

    private static final long DEFAULT_ANIMATE_DURATION = 200;

    private static final int ALPHA_SELECTED = 255;

    private static final int ALPHA_UNSELECTED = 100;

    private static final int DEFAULT_BAR_DISTANCE = 20;

    private static final int DEFAULT_BAR_WIDTH = 40;

    private static final int DEFAULT_TEXT_SIZE = 12;

    private static final int LABEL_AND_AXIS_PADDING = 40;

    private static final int DEFAULT_MIN_Y_VALUE = 0;

    private static final int DEFAULT_MAX_Y_VALUE = 100;

    private static final int NUMOF_BASELINE = 5;

    private static final float DEFAULT_SCROLL_DISTANCE = 0;

    private static final int MAX_POINTER = 2;

    private List<BarEntry> mEntries;

    private Paint mBarPaint;

    private Paint mLinePaint;

    private Paint mLabelPaint;

    private Pointer[] mPointers = new Pointer[MAX_POINTER];

    private GestureDetector mGestureDetector;

    private int mBarDistance = DEFAULT_BAR_DISTANCE;

    private int mBarWidth = DEFAULT_BAR_WIDTH;

    private int mTextSize = DEFAULT_TEXT_SIZE;

    private int mMaxYValue = DEFAULT_MAX_Y_VALUE;

    private int mMinYValue = DEFAULT_MIN_Y_VALUE;

    private float mXScrollDistance = DEFAULT_SCROLL_DISTANCE;

    private float mYScrollDistance = DEFAULT_SCROLL_DISTANCE;

    private int mScale = 1;

    private int mColor;

    public BarChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        mEntries = new ArrayList<>();

        mGestureDetector = new GestureDetector(context, new CustomGesture());

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BarChart);

        mColor = typedArray.getColor(R.styleable.BarChart_bar_color, getResources().getColor(R.color.colorAccent));

        mBarDistance = typedArray.getInt(R.styleable.BarChart_bar_distance, DEFAULT_BAR_DISTANCE);

        typedArray.recycle();

        initPaint();
    }

    private void initPaint() {
        mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPaint.setColor(mColor);
        mBarPaint.setAlpha(ALPHA_UNSELECTED);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(mColor);
        mLinePaint.setStrokeWidth(1f);
        mLinePaint.setAlpha(ALPHA_UNSELECTED);

        mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLabelPaint.setColor(mColor);
        mLabelPaint.setTextSize(dpToPixels(mContext, mTextSize));
        mLabelPaint.setAlpha(ALPHA_UNSELECTED);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(canvas);

        drawBarEntries(canvas);
    }

    private void drawBackground(Canvas canvas) {

        float mBaseLineDistance = (getOriginRect().bottom - getOriginRect().top) / (NUMOF_BASELINE - 1);

        String text = "0%";
        float y = getOriginRect().bottom;
        canvas.drawLine(getOriginRect().left, y, getOriginRect().right, y, mLinePaint);
        canvas.drawText(text, getOriginRect().right + LABEL_AND_AXIS_PADDING, y + mTextSize, mLabelPaint);

        for (int i = 0; i < NUMOF_BASELINE - 1; i++) {
            y = getOriginRect().top + i * mBaseLineDistance;

            text = "" + (mMaxYValue - i * ((mMaxYValue - mMinYValue) / (NUMOF_BASELINE - 1)));

            canvas.drawLine(getOriginRect().left, y, getOriginRect().right, y, mLinePaint);
            canvas.drawText(text, getOriginRect().right + LABEL_AND_AXIS_PADDING, y + mTextSize, mLabelPaint);
        }
    }

    private void drawBarEntries(Canvas canvas) {

        for (int i = 0; i < mEntries.size(); i++) {

            float left = Math.max(getOriginRect().left, getOriginRect().left + i * (mBarWidth + mBarDistance) + mXScrollDistance);

            if (left >= getOriginRect().right) {
                continue;
            }

            float right = Math.min(getOriginRect().left + i * (mBarWidth + mBarDistance) + mXScrollDistance + mBarWidth, getOriginRect().right);

            if (right <= getOriginRect().left) {
                continue;
            }

            float bottom = getOriginRect().bottom;

            float top = getOriginRect().bottom - mEntries.get(i).getValue() * (getOriginRect().bottom - getOriginRect().top) / (mMaxYValue - mMinYValue);

            mEntries.get(i).setRect(new RectF(left, top, right, bottom));

            if (mEntries.get(i).isIsSelected()) {
                mBarPaint.setAlpha(ALPHA_SELECTED);
            } else {
                mBarPaint.setAlpha(ALPHA_UNSELECTED);
            }

            canvas.drawRect(mEntries.get(i).getRect(), mBarPaint);
            canvas.drawText(mEntries.get(i).getXAxisName(), (right + left) / 2 - mTextSize, getOriginRect().bottom + LABEL_AND_AXIS_PADDING, mLabelPaint);
        }
    }

    private RectF getOriginRect() {
        return new RectF(0,
                LABEL_AND_AXIS_PADDING,
                getWidth() - LABEL_AND_AXIS_PADDING * 2 - String.valueOf(mMaxYValue).length() * mTextSize,
                getHeight() - (mTextSize + LABEL_AND_AXIS_PADDING * 2));

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    public void setData(List<BarEntry> entries) {
        if (mEntries == null) {
            mEntries = new ArrayList<>();
        }
        mEntries.clear();
        add(entries);
    }

    public void add(List<BarEntry> entries) {
        if (mEntries == null) {
            mEntries = new ArrayList<>();
        }
        for (int i = 0 ;i < entries.size(); i++) {
            add(entries.get(i).getXAxisName(), entries.get(i).getValue());
        }
    }

    public void add(String name, int value) {
        if (mEntries == null) {
            mEntries = new ArrayList<>();
        }
        mEntries.add(new BarEntry(name, value));
        if (value > mMaxYValue) {
            mMaxYValue = value;
        }

        invalidate();
    }

    public static float dpToPixels(Context context, float dpValue) {

        if (context != null) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, metrics);
        }
        return 0;
    }

    public class CustomGesture extends GestureDetector.SimpleOnGestureListener {
        private boolean normal = true;

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            mScale = normal ? 3 : 1;

            normal = !normal;

            invalidate();
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            float x = getX();

            float y = getY();

            invalidate();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            mXScrollDistance = mXScrollDistance - distanceX;

            invalidate();
            return true;
        }
    }
}

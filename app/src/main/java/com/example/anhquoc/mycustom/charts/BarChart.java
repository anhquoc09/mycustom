package com.example.anhquoc.mycustom.charts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
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

    private static final float DEFAULT_BAR_DISTANCE = 10;

    private static final float DEFAULT_BAR_WIDTH = 20;

    private static final int DEFAULT_TEXT_SIZE = 14;

    private static final float LABEL_AND_AXIS_PADDING = 10;

    private static final float DEFAULT_MIN_Y_AXIS = 0;

    private static final float DEFAULT_MAX_Y_AXIS = 100;

    private static final int NUMOF_BASELINE = 5;

    private List<BarEntry> mEntries;

    private int selectedPosition = -1;

    private float mMaxValue = 0;

    private Paint mBarPaint;

    private Paint mLinePaint;

    private Paint mLabelPaint;

    private RectF mLimitRect;

    private GestureDetector mGestureDetector;

    private float mBarDistance = DEFAULT_BAR_DISTANCE;

    private float mBarWidth = DEFAULT_BAR_WIDTH;

    private int mTextSize = DEFAULT_TEXT_SIZE;

    private float mMaxYAxis = DEFAULT_MAX_Y_AXIS;

    private float mMinYAxis = DEFAULT_MIN_Y_AXIS;

    private float mScrollDistanceX;

    private float mScrollDistanceY;

    private float mMaxScrollDistanceX;

    private float mMaxScrollDistanceY;

    private float mScale = 1f;

    private int mColor;

    public BarChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        mEntries = new ArrayList<>();

        mGestureDetector = new GestureDetector(context, new CustomGesture());

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BarChart);

        mColor = typedArray.getColor(R.styleable.BarChart_bar_color, getResources().getColor(R.color.colorAccent));

        mBarDistance = typedArray.getFloat(R.styleable.BarChart_bar_distance, DEFAULT_BAR_DISTANCE);

        typedArray.recycle();

        initPaint();
    }

    private void initPaint() {
        mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPaint.setColor(mColor);
        mBarPaint.setAlpha(ALPHA_UNSELECTED);
//        mBarPaint.setShadowLayer(30, 10, 10, Color.BLACK);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(mColor);
        mLinePaint.setStrokeWidth(1f);
        mLinePaint.setAlpha(ALPHA_UNSELECTED);

        mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLabelPaint.setColor(mColor);
        mLabelPaint.setTextSize(getTextSize());
        mLabelPaint.setAlpha(ALPHA_UNSELECTED);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mLimitRect = getOriginRect();
        mMaxScrollDistanceX = getMaxScrollDistanceX();
        mMaxScrollDistanceY = getMaxScrollDistanceY();
        calcScale();

        drawBackground(canvas);

        drawBarEntries(canvas);
    }

    private void drawBackground(Canvas canvas) {
//        mMaxYAxis = mMaxValue / mScale + mScrollDistanceY * mMaxValue / (mLimitRect.bottom - mLimitRect.top);
//        mMinYAxis = 0 + mScrollDistanceY * mMaxValue / (mLimitRect.bottom - mLimitRect.top);

        float mBaseLineDistance = (mLimitRect.bottom - mLimitRect.top) / (NUMOF_BASELINE - 1);

        String text = "" + mMinYAxis;
//        String text = String.format("%.01f", mMinYAxis);
        float y = mLimitRect.bottom;
        canvas.drawLine(mLimitRect.left, y, mLimitRect.right, y, mLinePaint);
        canvas.drawText(text, mLimitRect.right + getLabelAndAxisPadding(), y + getTextSize() / 2, mLabelPaint);

        for (int i = 0; i < NUMOF_BASELINE - 1; i++) {
            y = mLimitRect.top + i * mBaseLineDistance;
            text = "" + (mMaxYAxis - i * ((mMaxYAxis - mMinYAxis) / (NUMOF_BASELINE - 1)));
//            text = String.format("%.01f", mMaxYAxis - i * ((mMaxYAxis - mMinYAxis) / (NUMOF_BASELINE - 1)));
            canvas.drawLine(mLimitRect.left, y, mLimitRect.right, y, mLinePaint);
            canvas.drawText(text, mLimitRect.right + getLabelAndAxisPadding(), y + getTextSize() / 4, mLabelPaint);
        }
    }

    private void drawBarEntries(Canvas canvas) {

        float valueRatio = (mLimitRect.bottom - mLimitRect.top) / (mMaxYAxis - mMinYAxis);

        for (int i = 0; i < mEntries.size(); i++) {

            float left = mLimitRect.left + i * (getBarWidth() + getBarDistance()) - mScrollDistanceX;
            float right = left + getBarWidth();
            left = Math.max(left, mLimitRect.left);
            right = Math.min(right, mLimitRect.right);
            if (left >= mLimitRect.right || right <= mLimitRect.left) {
                continue;
            }

            float xText = (left + right) / 2 - getTextSize() / 4;

            float bottom = mLimitRect.bottom + mScrollDistanceY;
            float barHeight = mEntries.get(i).getValue() * mScale;
            float top = bottom - barHeight * valueRatio;
            bottom = Math.min(bottom, mLimitRect.bottom);
            top = Math.max(top, mLimitRect.top);

            mEntries.get(i).setRect(new RectF(left, top, right, bottom));
            if (mEntries.get(i).isIsSelected()) {
                mBarPaint.setAlpha(ALPHA_SELECTED);
                mLabelPaint.setAlpha(ALPHA_SELECTED);
            } else {
                mBarPaint.setAlpha(ALPHA_UNSELECTED);
                mLabelPaint.setAlpha(ALPHA_UNSELECTED);
            }

            if (top <= mLimitRect.bottom) {
                canvas.drawRect(mEntries.get(i).getRect(), mBarPaint);
            }
            canvas.drawText(mEntries.get(i).getXAxisName(), xText, mLimitRect.bottom + getLabelAndAxisPadding() * 2, mLabelPaint);
        }
    }

    private void calcScale() {
        mBarDistance = DEFAULT_BAR_DISTANCE * mScale;

        mBarWidth = DEFAULT_BAR_WIDTH * mScale;
    }

    private float getMaxScrollDistanceX() {
        float maxDistance = 0;
        float distance = mEntries.size() * (getBarWidth() + getBarDistance()) - (mLimitRect.right - mLimitRect.left);
        if (distance > 0) {
            maxDistance = distance - getBarDistance();
        }
        return maxDistance;
    }

    private float getMaxScrollDistanceY() {
        float maxDistance = 0;
        float distance = mMaxValue * mScale * (mLimitRect.bottom - mLimitRect.top) / (mMaxYAxis - mMinYAxis) - (mLimitRect.bottom - mLimitRect.top);
        if (distance > 0) {
            maxDistance = distance;
        }
        return maxDistance;
    }

    private RectF getOriginRect() {
        return new RectF(0,
                getLabelAndAxisPadding(),
                getWidth() - String.valueOf(mMaxYAxis).length() * getTextSize(),
                getHeight() - (getTextSize() + getLabelAndAxisPadding() * 2));
    }

    private float getBarWidth() {
        return dpToPixels(mContext, mBarWidth);
    }

    private float getBarDistance() {
        return dpToPixels(mContext, mBarDistance);
    }

    private float getLabelAndAxisPadding() {
        return dpToPixels(mContext, LABEL_AND_AXIS_PADDING);
    }

    private float getTextSize() {
        return spToPixels(mContext, mTextSize);
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
        for (int i = 0; i < entries.size(); i++) {
            add(entries.get(i).getXAxisName(), entries.get(i).getValue());
        }
    }

    public void add(String name, int value) {
        if (mEntries == null) {
            mEntries = new ArrayList<>();
        }
        mEntries.add(new BarEntry(name, value));
        if (value > mMaxValue) {
            mMaxYAxis = value;
            mMaxValue = value;
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

    public static float spToPixels(Context context, float spValue) {
        if (context != null) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, metrics);
        }
        return 0;
    }

    public class CustomGesture extends GestureDetector.SimpleOnGestureListener {
        private boolean normal = true;

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (normal) {
                mScale = 3f;
            } else {
                mScale = 1f;
                mScrollDistanceX = 0;
                mScrollDistanceY = 0;
            }
            normal = !normal;

            invalidate();
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();

            int position = (int) ((x + mScrollDistanceX) / (getBarDistance() + getBarWidth()));
            float mod = ((x + mScrollDistanceX) - position * (getBarDistance() + getBarWidth()));

            if (mod <= getBarWidth() && y >= mEntries.get(position).getRect().top) {
                if (selectedPosition >= 0) {
                    mEntries.get(selectedPosition).setSelected(false);
                }

                if (position != selectedPosition) {
                    mEntries.get(position).setSelected(true);
                    selectedPosition = position;
                } else {
                    selectedPosition = -1;
                }
            }
            invalidate();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            //Scroll from left to right
            if (distanceX < 0) {
                mScrollDistanceX = Math.max(0, mScrollDistanceX + distanceX);
            }
            //Scroll from right to left
            else {
                mScrollDistanceX = Math.min(mMaxScrollDistanceX, mScrollDistanceX + distanceX);
            }
            //Scroll bottom up
            if (distanceY > 0) {
                mScrollDistanceY = Math.max(0, mScrollDistanceY - distanceY);
            }
            //Scroll top down
            else {
                mScrollDistanceY = Math.min(mMaxScrollDistanceY, mScrollDistanceY - distanceY);
            }

            invalidate();
            return true;
        }
    }

    public class ScaleGesture extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScale *= detector.getScaleFactor();

            invalidate();
            return true;
        }
    }
}

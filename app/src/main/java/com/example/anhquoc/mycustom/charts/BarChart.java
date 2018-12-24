package com.example.anhquoc.mycustom.charts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
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

    private static final int DEFAULT_BAR_DISTANCE = 10;

    private static final int DEFAULT_BAR_WIDTH = 20;

    private static final int DEFAULT_TEXT_SIZE = 14;

    private static final int LABEL_AND_AXIS_PADDING = 10;

    private static final int DEFAULT_MIN_Y_VALUE = 0;

    private static final int DEFAULT_MAX_Y_VALUE = 100;

    private static final int NUMOF_BASELINE = 5;

    private static final float LIMIT_LEFT_CROLL_DISTANCE = 0;

    private static final int MAX_POINTER = 2;

    private float mWidth;

    private float mHeight;

    private List<BarEntry> mEntries;

    private Paint mBarPaint;

    private Paint mLinePaint;

    private Paint mLabelPaint;

    private Pointer[] mPointers = new Pointer[MAX_POINTER];

    private GestureDetector mGestureDetector;

    private float mBarDistance = DEFAULT_BAR_DISTANCE;

    private float mBarWidth = DEFAULT_BAR_WIDTH;

    private int mTextSize = DEFAULT_TEXT_SIZE;

    private int mMaxYValue = DEFAULT_MAX_Y_VALUE;

    private int mMinYValue = DEFAULT_MIN_Y_VALUE;

    private float mXScrollDistance;

    private float mLimitRightScrollDistance;

    private float mYScrollDistance;

    private float mScale = 1f;

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
        mLabelPaint.setTextSize(getTextSize());
        mLabelPaint.setAlpha(ALPHA_UNSELECTED);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        float width = getWidth();
        getHeight();
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
        canvas.drawText(text, getOriginRect().right + getLabelAndAxisPadding(), y + getTextSize() / 2, mLabelPaint);

        for (int i = 0; i < NUMOF_BASELINE - 1; i++) {
            y = getOriginRect().top + i * mBaseLineDistance;

            text = "" + (mMaxYValue - i * ((mMaxYValue - mMinYValue) / (NUMOF_BASELINE - 1)));

            canvas.drawLine(getOriginRect().left, y, getOriginRect().right, y, mLinePaint);
            canvas.drawText(text, getOriginRect().right + getLabelAndAxisPadding(), y + getTextSize() / 4, mLabelPaint);
        }
    }

    private void drawBarEntries(Canvas canvas) {

        calcLimitRightScrollDistance();

        calcScale();

        for (int i = 0; i < mEntries.size(); i++) {

            float left = getOriginRect().left + i * (getBarWidth() + getBarDistance()) + mXScrollDistance;
            float right = left + getBarWidth();

            left = Math.max(left, getOriginRect().left);
            right = Math.min(right, getOriginRect().right);

            if (left >= getOriginRect().right || right <= getOriginRect().left) {
                continue;
            }

            float xText = (left + right) / 2 - getTextSize() / 4;

            float bottom = getOriginRect().bottom;

            float top = getOriginRect().bottom - mEntries.get(i).getValue() * (getOriginRect().bottom - getOriginRect().top) / (mMaxYValue - mMinYValue);

            mEntries.get(i).setRect(new RectF(left, top, right, bottom));

            if (mEntries.get(i).isIsSelected()) {
                mBarPaint.setAlpha(ALPHA_SELECTED);
            } else {
                mBarPaint.setAlpha(ALPHA_UNSELECTED);
            }

            canvas.drawRect(mEntries.get(i).getRect(), mBarPaint);

            canvas.drawText(mEntries.get(i).getXAxisName(), xText, getOriginRect().bottom + getLabelAndAxisPadding() * 2, mLabelPaint);
        }
    }

    private void calcScale() {
        mBarDistance = DEFAULT_BAR_DISTANCE * mScale;

        mBarWidth = DEFAULT_BAR_WIDTH * mScale;
    }

    private void calcLimitRightScrollDistance() {
        mLimitRightScrollDistance = LIMIT_LEFT_CROLL_DISTANCE;
        float distance = mEntries.size() * (getBarWidth() + getBarDistance()) - (getOriginRect().right - getOriginRect().left);
        if (distance > 0) {
            mLimitRightScrollDistance = LIMIT_LEFT_CROLL_DISTANCE + distance - getBarDistance();
        }
    }

    private RectF getOriginRect() {
        return new RectF(0,
                getLabelAndAxisPadding(),
                getWidth() - String.valueOf(mMaxYValue).length() * getTextSize(),
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
            mScale = normal ? 3f : 1f;

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

            //Scroll from left to right
            if (distanceX < 0) {
                mXScrollDistance = Math.min(LIMIT_LEFT_CROLL_DISTANCE, mXScrollDistance - distanceX);
            }
            //Scroll from right to left
            else {
                mXScrollDistance = Math.max(LIMIT_LEFT_CROLL_DISTANCE - mLimitRightScrollDistance, mXScrollDistance - distanceX);
            }
            invalidate();
            return true;
        }
    }
}

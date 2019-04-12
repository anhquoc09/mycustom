package com.example.anhquoc.mycustom.charts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.example.anhquoc.mycustom.OnItemSelectedListener;
import com.example.anhquoc.mycustom.R;
import com.example.anhquoc.mycustom.barchart.XAxis;
import com.example.anhquoc.mycustom.barchart.YAxis;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;

public class BarChart extends View {

    private Context mContext;

    private static final long DEFAULT_ANIMATE_DURATION = 200;

    public static final int ALPHA_SELECTED = 255;

    public static final int ALPHA_UNSELECTED = 150;

    public static final int ALPHA_BACKGROUND = 50;

    private static final float DEFAULT_BAR_DISTANCE = 10;

    private static final float DEFAULT_BAR_WIDTH = 20;

    public static final int DEFAULT_TEXT_SIZE = 12;

    private static final float LABEL_AND_AXIS_PADDING = 10;

    private static final float DEFAULT_MIN_Y_AXIS = 0;

    private static final float DEFAULT_MAX_Y_AXIS = 100;

    private static final int NUMOF_BASELINE = 5;

    private static final int SELECTED_DOT_RADIUS = 5;

    private final List<BarEntry> mEntries = new ArrayList<>();

    private OnItemSelectedListener mItemSelectedListener;

    private GestureDetector mTapDetector;

    private GestureDetector mScrollDetector;

    private ScaleGestureDetector mScaleDetector;

    private Paint mPaint;

    private LinearGradient mGradient;

    private RectF mOriginRect;

    private int selectedPosition = -1;

    private int mColor;

    private float mMaxValue = 100;

    private float mTextSize = DEFAULT_TEXT_SIZE;

    private float mBarDistance = DEFAULT_BAR_DISTANCE;

    private float mBarWidth = DEFAULT_BAR_WIDTH;

    private float mMaxYAxis = DEFAULT_MAX_Y_AXIS;

    private float mMinYAxis = DEFAULT_MIN_Y_AXIS;

    private float mScrollDistanceX;

    private float mScrollDistanceY;

    private float mMaxScrollDistanceX;

    private float mMaxScrollDistanceY;

    private float mScale = 1f;

    private YAxis mYAxis;

    private XAxis mXAxis;

    private Rect mTextBound = new Rect();

    public BarChart(Context context) {
        super(context);
        init(context);
    }

    public BarChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BarChart);
        mColor = typedArray.getColor(R.styleable.BarChart_text_color, Color.LTGRAY);
        mBarDistance = typedArray.getFloat(R.styleable.BarChart_bar_distance, DEFAULT_BAR_DISTANCE);
        typedArray.recycle();

        init(context);
    }

    private void init(Context context) {
        mContext = context;

        mTapDetector = new GestureDetector(context, new TapListener());
        mScrollDetector = new GestureDetector(context, new ScrollListener());
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mOriginRect = new RectF();
        mColor = Color.LTGRAY;

        mYAxis = new YAxis(dpToPixels(mContext, mTextSize),
                100,
                mColor,
                dpToPixels(mContext, 4));

        mXAxis = new XAxis(dpToPixels(mContext, mTextSize),
                100,
                mColor,
                dpToPixels(mContext, 4),
                dpToPixels(mContext, mBarWidth),
                dpToPixels(mContext, mBarDistance));
        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mColor);
        mGradient = new LinearGradient(0, 0, 0, 800,
                Color.CYAN, Color.BLUE, Shader.TileMode.REPEAT);
        mPaint.setShader(mGradient);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mOriginRect = getOriginRect();
        mMaxScrollDistanceX = getMaxScrollDistanceX();
        mMaxScrollDistanceY = getMaxScrollDistanceY();
        calMaxValueTextBound();
        calcScale();
        mYAxis.drawYAxis(canvas, mOriginRect, mTextBound, mScrollDistanceY, mScale);

        mXAxis.drawXAxis(canvas, mOriginRect, mTextBound, mScrollDistanceX, mScale);

//        drawEntries(canvas);

    }

    private void calMaxValueTextBound() {
        String text = String.format(Locale.US, "%.1f", mMaxValue);
        mPaint.setTextSize(dpToPixels(mContext, mTextSize));
        mPaint.getTextBounds(text, 0, text.length(), mTextBound);
    }

    private void drawEntries(Canvas canvas) {

        for (int i = 0; i < mEntries.size(); i++) {

            BarEntry entry = mEntries.get(i);
            RectF barRect = getBarRect(i);

            if (entry.isSelected()) {
                mPaint.setAlpha(ALPHA_SELECTED);

            } else {
                mPaint.setAlpha(ALPHA_UNSELECTED);
            }

            drawContent(canvas, entry, barRect);
        }
    }

    private void drawContent(Canvas canvas, BarEntry entry, RectF barRect) {
        //Out of Origin Rectangle
        if (barRect.left > mOriginRect.right || barRect.right < mOriginRect.left) {
            return;
        }

        entry.getBound().set(Math.max(barRect.left, mOriginRect.left),
                Math.max(barRect.top, mOriginRect.top),
                Math.min(barRect.right, mOriginRect.right),
                Math.min(barRect.bottom, mOriginRect.bottom));

        RectF bound = entry.getBound();

        if (barRect.top <= mOriginRect.bottom) {
            canvas.drawRect(bound, mPaint);
        }

        float centerBar = bound.left + bound.width() / 2;

        if (entry.isSelected()) {
            mYAxis.drawSelected(canvas, centerBar, barRect.top);
        }
    }

    private void calcScale() {
        mBarDistance = DEFAULT_BAR_DISTANCE * mScale;
        mBarWidth = DEFAULT_BAR_WIDTH * mScale;
    }

    private RectF getBarRect(int position) {
        BarEntry entry = mEntries.get(position);
        float valueRatio = (mOriginRect.height()) / (mMaxYAxis - mMinYAxis);

        float left = mOriginRect.left + position * (getBarWidth() + getBarDistance()) - mScrollDistanceX;
        float right = left + getBarWidth();
        float bottom = mOriginRect.bottom + mScrollDistanceY;
        float top = bottom - entry.getValue() * valueRatio;

        RectF rectF = entry.getBound();
        rectF.set(left, top, right, bottom);
        return rectF;
    }

    private float getMaxScrollDistanceX() {
        float maxDistance = mXAxis.getMaxContentWidth() - (mXAxis.getRight() - mXAxis.getLeft());
        return maxDistance > 0 ? maxDistance : 0;
    }

    private float getMaxScrollDistanceY() {
        float maxDistance = 0;
        float distance = mMaxValue * mScale * (mOriginRect.bottom - mOriginRect.top) / (mMaxYAxis - mMinYAxis) - (mOriginRect.bottom - mOriginRect.top);
        if (distance > 0) {
            maxDistance = distance;
        }
        return maxDistance;
    }

    private RectF getOriginRect() {
        mOriginRect.set(getPaddingLeft(),
                getPaddingTop(),
                getWidth() - getPaddingLeft() - getPaddingRight(),
                getHeight() - getPaddingTop() - getPaddingBottom());
        return mOriginRect;
    }

    private float getBarWidth() {
        return dpToPixels(mContext, mBarWidth);
    }

    private float getBarDistance() {
        return dpToPixels(mContext, mBarDistance);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mTapDetector.onTouchEvent(event);
        mScrollDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);
        return true;
    }

    public void setData(List<BarEntry> entries) {
        mEntries.clear();
        if (entries != null && !entries.isEmpty()) {
            add(entries);
        }
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mItemSelectedListener = listener;
    }

    public void add(List<BarEntry> entries) {
        for (int i = 0; i < entries.size(); i++) {
            add(entries.get(i).getXAxisName(), entries.get(i).getValue());
        }
    }

    public void add(String name, float value) {
        BarEntry entry = new BarEntry(name, value);
        mEntries.add(entry);
        if (value > mMaxValue) {
            mMaxYAxis = value;
            mMaxValue = value;
        }

        invalidate();
    }

    public boolean isEmpty() {
        return mEntries.isEmpty();
    }

    public float getMaxValue() {
        return mMaxValue;
    }

    public static float dpToPixels(Context context, float dpValue) {
        if (context != null) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, metrics);
        }
        return 0;
    }

    /**
     * {{@link TapListener}}
     */
    public class TapListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mScale == 1) {
                mScale = 3;
            } else {
                mScale = 1;
                mScrollDistanceX = 0;
                mScrollDistanceY = 0;
            }

            invalidate();
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();

            int position = (int) ((x + mScrollDistanceX) / (getBarDistance() + getBarWidth()));
            float mod = ((x + mScrollDistanceX) - position * (getBarDistance() + getBarWidth()));

            if (selectedPosition >= 0) {
                mEntries.get(selectedPosition).setSelected(false);
            }

            BarEntry entry = mEntries.get(position);
            if (mod <= getBarWidth() && y >= entry.getBound().top) {
                entry = mEntries.get(position);
                entry.setSelected(true);
                selectedPosition = position;
            } else {
                selectedPosition = -1;
            }
            invalidate();

            if (mItemSelectedListener != null) {
                if (selectedPosition != -1) {
                    mItemSelectedListener.onItemSelected(entry);
                } else {
                    mItemSelectedListener.onNothingSelected();
                }
            }
            return true;
        }
    }

    /**
     * {{@link ScrollListener}}
     */
    public class ScrollListener extends GestureDetector.SimpleOnGestureListener {
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

    /**
     * {{@link ScaleListener}}
     */
    public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScale *= detector.getScaleFactor();

            //Max scale = 5, min scale = 1/2
            mScale = Math.max(1f, Math.min(mScale, 3f));
            invalidate();
            return true;
        }
    }
}

package com.example.anhquoc.mycustom.charts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.example.anhquoc.mycustom.Entries.BarEntry;
import com.example.anhquoc.mycustom.R;

public class BarChart extends View {

    private Context mContext;

    private static final long DEFAULT_ANIMATE_DURATION = 200;

    private static final int DEFAULT_ALPHA_SELECTED = 255;

    private static final int DEFAULT_ALPHA_UNSELECTED = 100;

    private static final int DEFAULT_BAR_DISTANCE = 20;

    private static final int DEFAULT_BAR_WIDTH = 40;

    private static final int DEFAULT_TEXT_SIZE = 12;

    private static final int LABEL_AND_AXIS_PADDING = 40;

    private static final int DEFAULT_MIN_Y_VALUE = 0;

    private static final int DEFAULT_MAX_Y_VALUE = 100;

    private static final int NUMOF_BASELINE = 5;

    private BarEntry[] mEntries;

    private Paint mBarPaint;

    private Paint mLinePaint;

    private Paint mXAxisLabelPaint;

    private Paint mYAxisLabelPaint;

    private long mAnimateDuration = DEFAULT_ANIMATE_DURATION;

    private int mAlphaSelected = DEFAULT_ALPHA_SELECTED;

    private int mAlphaUnselected = DEFAULT_ALPHA_UNSELECTED;

    private int mBarDistance = DEFAULT_BAR_DISTANCE;

    private int mBarWidth = DEFAULT_BAR_WIDTH;

    private int mTextSize = DEFAULT_TEXT_SIZE;

    private int mMaxYValue = DEFAULT_MAX_Y_VALUE;

    private int mMinYValue = DEFAULT_MIN_Y_VALUE;

    private int mNumOfBaseLine = NUMOF_BASELINE;

    private int mBaseLineDistance;

    private int mColor;

    public BarChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BarChart);

        mColor = typedArray.getColor(R.styleable.BarChart_bar_color, getResources().getColor(R.color.colorAccent));

        mBarDistance = typedArray.getInt(R.styleable.BarChart_bar_distance, DEFAULT_BAR_DISTANCE);

        typedArray.recycle();

        initPaint();
    }

    private void initPaint() {
        mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPaint.setColor(mColor);
        mBarPaint.setAlpha(mAlphaUnselected);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(mColor);
        mLinePaint.setStrokeWidth(1f);

        mXAxisLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mXAxisLabelPaint.setColor(mColor);
        mXAxisLabelPaint.setTextSize(dpToPixels(mContext, mTextSize));

        mYAxisLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mYAxisLabelPaint.setColor(mColor);
        mYAxisLabelPaint.setTextSize(dpToPixels(mContext , mTextSize));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(canvas);

        drawBarEntries(canvas);
    }

    private void drawBackground(Canvas canvas) {

        mBaseLineDistance = (getHeight() - (mTextSize + LABEL_AND_AXIS_PADDING * 3)) / (mNumOfBaseLine - 1);

        int stopXBaseLine = getWidth() - LABEL_AND_AXIS_PADDING * 2 - String.valueOf(mMaxYValue).length() * mTextSize;

        for (int i = 0; i < mNumOfBaseLine; i++) {
            int y = LABEL_AND_AXIS_PADDING + i * mBaseLineDistance;

            String text = "" + (mMaxYValue - i * ((mMaxYValue - mMinYValue) / (mNumOfBaseLine - 1)));
            text = text + (i == mNumOfBaseLine - 1 ? "%" : "");


            canvas.drawLine(0, y, stopXBaseLine, y, mLinePaint);
            canvas.drawText(text, stopXBaseLine + LABEL_AND_AXIS_PADDING, y + mTextSize, mYAxisLabelPaint);
        }
    }

    private void drawBarEntries(Canvas canvas) {

    }

    public static float dpToPixels(Context context, float dpValue) {

        if (context != null) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, metrics);
        }
        return 0;
    }
}

package com.example.anhquoc.mycustom.charts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.example.anhquoc.mycustom.Entries.BarEntry;
import com.example.anhquoc.mycustom.R;

import java.util.ArrayList;
import java.util.List;

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

    private List<BarEntry> mEntries;

    private Paint mBarPaint;

    private Paint mLinePaint;

    private Paint mLabelPaint;

    private int mAlphaSelected = DEFAULT_ALPHA_SELECTED;

    private int mAlphaUnselected = DEFAULT_ALPHA_UNSELECTED;

    private int mBarDistance = DEFAULT_BAR_DISTANCE;

    private int mBarWidth = DEFAULT_BAR_WIDTH;

    private int mTextSize = DEFAULT_TEXT_SIZE;

    private int mMaxYValue = DEFAULT_MAX_Y_VALUE;

    private int mMinYValue = DEFAULT_MIN_Y_VALUE;

    private int mNumOfBaseLine = NUMOF_BASELINE;

    private int mXAxisBaseLine;

    private int mColor;

    public BarChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        mEntries = new ArrayList<>();

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

        mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLabelPaint.setColor(mColor);
        mLabelPaint.setTextSize(dpToPixels(mContext, mTextSize));
        mLabelPaint.setAlpha(mAlphaUnselected);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(canvas);

        drawBarEntries(canvas);
    }

    private void drawBackground(Canvas canvas) {

        int mBaseLineDistance = (getOriginRect().bottom - getOriginRect().top) / (mNumOfBaseLine - 1);

        for (int i = 0; i < mNumOfBaseLine; i++) {
            int y = getOriginRect().top + i * mBaseLineDistance;

            String text = "" + (mMaxYValue - i * ((mMaxYValue - mMinYValue) / (mNumOfBaseLine - 1)));
            text = text + (i == mNumOfBaseLine - 1 ? "%" : "");


            canvas.drawLine(getOriginRect().left, y, getOriginRect().right, y, mLinePaint);
            canvas.drawText(text, getOriginRect().right + LABEL_AND_AXIS_PADDING, y + mTextSize, mLabelPaint);
        }
    }

    private void drawBarEntries(Canvas canvas) {

        for (int i = 0; i < mEntries.size(); i++) {
            int left = getOriginRect().left + i * (mBarWidth + mBarDistance);
            int right = Math.min(left + mBarWidth, getOriginRect().right);
            int top = getOriginRect().bottom - mEntries.get(i).getValue() * (getOriginRect().bottom - getOriginRect().top) / (mMaxYValue - mMinYValue);

            canvas.drawRect(left, top, right, getOriginRect().bottom, mBarPaint);
            canvas.drawText(mEntries.get(i).getXAxisName(), (right + left) / 2 - mTextSize, getOriginRect().bottom + LABEL_AND_AXIS_PADDING, mLabelPaint);
        }
    }

    private Rect getOriginRect() {
        return new Rect(0,
                LABEL_AND_AXIS_PADDING,
                getWidth() - LABEL_AND_AXIS_PADDING * 2 - String.valueOf(mMaxYValue).length() * mTextSize,
                getHeight() - (mTextSize + LABEL_AND_AXIS_PADDING * 2));

    }

    public void add(String name, int value) {
        mEntries.add(new BarEntry(name, value));
        invalidate();
    }

    public static float dpToPixels(Context context, float dpValue) {

        if (context != null) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, metrics);
        }
        return 0;
    }
}

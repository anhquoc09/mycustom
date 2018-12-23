package com.example.anhquoc.mycustom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.example.anhquoc.mycustom.charts.BarEntry;

import java.text.DecimalFormat;

public class Chart extends View{

        private Paint mPaint;

        private Context mContext;

        private BarEntry[] mDataArray;

        private float mMaxValueOfData;

        private final int mStrokeWidth = 2;

        private int mAxisFontSize = 14;

        private int mMaxValueCountOnYAxis = 9;

        private int mDistanceAxisAndValue;

        private int mMaxWidthOfYAxisText;

        private int mMaxHeightOfXAxisText;

        public Chart(Context context, AttributeSet attributeSet) {

            super(context, attributeSet);
            mContext = context;
            mPaint = new Paint();
            init();
        }

        private void init() {

            mDistanceAxisAndValue = (int) dpToPixels(mContext, 14);
        }

        public void setYAxisData(BarEntry[] barData) {

            mDataArray = barData;
            mMaxValueOfData = Float.MIN_VALUE;
            for (int index = 0; index < mDataArray.length; index++) {
                if (mMaxValueOfData < mDataArray[index].getValue())
                    mMaxValueOfData = mDataArray[index].getValue();
            }
            findMaxWidthOfText(barData);
            invalidate();
        }

        public float getMaxValueOfData() {

            return mMaxValueOfData;
        }

        private int getMaxWidthOfYAxisText() {

            return mMaxWidthOfYAxisText;
        }

        private void findMaxWidthOfText(BarEntry[] barDatas) {

            mMaxWidthOfYAxisText = Integer.MIN_VALUE;
            mMaxHeightOfXAxisText = Integer.MIN_VALUE;

            Paint paint = new Paint();
            paint.setTypeface(Typeface.DEFAULT);
            paint.setTextSize(dpToPixels(mContext, mAxisFontSize));

            Rect bounds = new Rect();

            for (int index = 0; index < mDataArray.length; index++) {
                int currentTextWidth =
                        (int) paint.measureText(Float.toString(barDatas[index].getValue()));
                if (mMaxWidthOfYAxisText < currentTextWidth)
                    mMaxWidthOfYAxisText = currentTextWidth;

                mPaint.getTextBounds(barDatas[index].getXAxisName(), 0,
                        barDatas[index].getXAxisName().length(), bounds);
                if (mMaxHeightOfXAxisText < bounds.height())
                    mMaxHeightOfXAxisText = bounds.height();
            }
        }

        public int getMaxHeightOfXAxisText() {

            return mMaxHeightOfXAxisText;
        }

        @Override
        protected void onDraw(Canvas canvas) {

            int usableViewHeight = getHeight() - getPaddingBottom() - getPaddingTop();
            int usableViewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            Point origin = getOrigin();
            mPaint.setColor(Color.CYAN);
            mPaint.setStrokeWidth(mStrokeWidth);
            //draw y axis
            canvas.drawLine(origin.x, origin.y, origin.x,
                    origin.y - (usableViewHeight - getXAxisLabelAndMargin()), mPaint);
            //draw x axis
            mPaint.setStrokeWidth(mStrokeWidth + 1);
            canvas.drawLine(origin.x, origin.y,
                    origin.x + usableViewWidth -
                            (getMaxWidthOfYAxisText() +
                                    mDistanceAxisAndValue), origin.y, mPaint);

            if (mDataArray == null || mDataArray.length == 0)
                return;
            //draw bar chart
            int barAndVacantSpaceCount = (mDataArray.length << 1) + 1;
            int widthFactor = (usableViewWidth - getMaxWidthOfYAxisText()) / barAndVacantSpaceCount;
            int x1, x2, y1, y2;
            float maxValue = getMaxValueOfData();
            for (int index = 0; index < mDataArray.length; index++) {
                x1 = origin.x + ((index << 1) + 1) * widthFactor;
                x2 = origin.x + ((index << 1) + 2) * widthFactor;
                int barHeight = (int) ((usableViewHeight - getXAxisLabelAndMargin()) *
                        mDataArray[index].getValue() / maxValue);
                y1 = origin.y - barHeight;
                y2 = origin.y;
                canvas.drawRect(x1, y1, x2, y2, mPaint);
                showXAxisLabel(origin, mDataArray[index].getXAxisName(), x1 + (x2 - x1) / 2, canvas);
            }
            showYAxisLabels(origin, (usableViewHeight - getXAxisLabelAndMargin()), canvas);
        }

        private String getFormattedValue(float value) {

            DecimalFormat precision = new DecimalFormat("0.0");
            return precision.format(value);
        }

        public void showYAxisLabels(Point origin, int usableViewHeight, Canvas canvas) {

            float maxValueOfData = (int) getMaxValueOfData();
            float yAxisValueInterval = usableViewHeight / mMaxValueCountOnYAxis;
            float dataInterval = maxValueOfData / mMaxValueCountOnYAxis;
            float valueToBeShown = maxValueOfData;
            mPaint.setTypeface(Typeface.DEFAULT);
            mPaint.setTextSize(dpToPixels(mContext, mAxisFontSize));

            //draw all texts from top to bottom
            for (int index = 0; index < mMaxValueCountOnYAxis; index++) {
                String string = getFormattedValue(valueToBeShown);

                Rect bounds = new Rect();
                mPaint.getTextBounds(string, 0, string.length(), bounds);
                int y = (int) ((origin.y - usableViewHeight) + yAxisValueInterval * index);
                canvas.drawLine(origin.x - (mDistanceAxisAndValue >> 1), y, origin.x, y, mPaint);
                y = y + (bounds.height() >> 1);
                canvas.drawText(string, origin.x - bounds.width() - mDistanceAxisAndValue, y, mPaint);
                valueToBeShown = valueToBeShown - dataInterval;
            }
        }

        public void showXAxisLabel(Point origin, String label, int centerX, Canvas canvas) {

            Rect bounds = new Rect();
            mPaint.getTextBounds(label, 0, label.length(), bounds);
            int y = origin.y + mDistanceAxisAndValue + getMaxHeightOfXAxisText();
            int x = centerX - bounds.width() / 2;
            mPaint.setTextSize(dpToPixels(mContext, mAxisFontSize));
            mPaint.setTypeface(Typeface.DEFAULT);
            canvas.drawText(label, x, y, mPaint);
        }

        private int getXAxisLabelAndMargin() {

            return getMaxHeightOfXAxisText() + mDistanceAxisAndValue;
        }

        public Point getOrigin() {

            if (mDataArray != null) {

                return new Point(getPaddingLeft() + getMaxWidthOfYAxisText() + mDistanceAxisAndValue,
                        getHeight() - getPaddingBottom() - getXAxisLabelAndMargin());
            } else {

                return new Point(getPaddingLeft() + getMaxWidthOfYAxisText() + mDistanceAxisAndValue,
                        getHeight() - getPaddingBottom());
            }
        }

        public static float dpToPixels(Context context, float dpValue) {

            if (context != null) {
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, metrics);
            }
            return 0;
        }
}

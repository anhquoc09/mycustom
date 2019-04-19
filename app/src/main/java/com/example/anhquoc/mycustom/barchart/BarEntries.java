package com.example.anhquoc.mycustom.barchart;

import android.graphics.Canvas;
import android.graphics.RectF;

import com.example.anhquoc.mycustom.charts.BarEntry;

import java.util.ArrayList;
import java.util.List;

public class BarEntries {
    public static final int MAX_NUM_OF_ENTRIES = 3;

    private List<BarEntry> mEntries = new ArrayList<>();

    private RectF mEntriesBound = new RectF();

    private int mLabel;

    private float mBarWidth;

    private float mBarDistance;

    private float mScale;

    public BarEntries(int xLabel, float barWidth, float barDistance) {
        mLabel = xLabel;
        mBarWidth = barWidth;
        mBarDistance = barDistance;
    }

    public int getLabel() {
        return mLabel;
    }

    public List<BarEntry> getEntries() {
        return mEntries;
    }

    public void setEntries(List<BarEntry> entries) {
        mEntries = entries;
    }

    public RectF getEntriesBound() {
        return mEntriesBound;
    }

    public boolean isFull() {
        return mEntries.size() == MAX_NUM_OF_ENTRIES;
    }

    public void set(List<BarEntry> list) {
        mEntries.clear();

        if (list != null && list.size() > 0) {
            if (list.size() > MAX_NUM_OF_ENTRIES) {
                mEntries = list.subList(0, 2);
            } else {
                mEntries.addAll(list);
            }
        }
    }

    public void add(BarEntry entry) {
        if (!isFull()) {
            mEntries.add(entry);
        }
    }

    public int getMaxNumOfEntries() {
        return MAX_NUM_OF_ENTRIES;
    }

    public void drawEntries(Canvas c, RectF originRect, float valueRatio, float scale) {
        mScale = scale;

        calcEntriesBound(originRect);
        for (int i = 0; i < mEntries.size(); i++) {
            mEntries.get(i).draw(c, mEntriesBound, valueRatio, i, scale);
        }
    }

    private void calcEntriesBound(RectF originRect) {
        float entriesWidth = (mBarWidth + mBarDistance) * MAX_NUM_OF_ENTRIES * mScale;
        mEntriesBound.left = originRect.left + entriesWidth * mLabel;
        mEntriesBound.right = mEntriesBound.left + entriesWidth;
        mEntriesBound.top = originRect.top;
        mEntriesBound.bottom = originRect.bottom;
    }

}
package com.example.anhquoc.mycustom.barchart;

import android.graphics.Canvas;
import android.graphics.RectF;

import com.example.anhquoc.mycustom.charts.BarEntry;

import java.util.ArrayList;
import java.util.List;

public class BarEntries {
    private int mMaxNumOfEntries = 3;

    private List<BarEntry> mEntries = new ArrayList<>();

    private int xLabel;

    public BarEntries(int xLabel) {
        this.xLabel = xLabel;
    }

    public boolean isFull() {
        return mEntries.size() == 3;
    }

    public void set(List<BarEntry> list) {
        mEntries.clear();

        if (list != null && list.size() > 0) {
            if (list.size() > mMaxNumOfEntries) {
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
        return mMaxNumOfEntries;
    }

    public void setMaxNumOfEntries(int maxNumOfEntries) {
        mMaxNumOfEntries = maxNumOfEntries;
    }

    public void drawEntries(Canvas c, RectF originRect) {

        for (int i = 0; i < mEntries.size(); i++) {
            mEntries.get(i).draw(c);
        }
    }
}
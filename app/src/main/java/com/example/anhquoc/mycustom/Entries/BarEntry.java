package com.example.anhquoc.mycustom.Entries;

public class BarEntry {
    private String mXAxisName;

    private int mValue;

    public BarEntry(String XAxisName, int value) {
        mXAxisName = XAxisName;
        mValue = value;
    }

    public String getXAxisName() {
        return mXAxisName;
    }

    public void setXAxisName(String XAxisName) {
        mXAxisName = XAxisName;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        mValue = value;
    }
}

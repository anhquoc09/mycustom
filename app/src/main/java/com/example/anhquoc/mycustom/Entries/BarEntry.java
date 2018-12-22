package com.example.anhquoc.mycustom.Entries;

public class BarEntry {
    private String mXAxisName;

    private float mValue;

    public BarEntry(String XAxisName, float value) {
        mXAxisName = XAxisName;
        mValue = value;
    }

    public String getXAxisName() {
        return mXAxisName;
    }

    public void setXAxisName(String XAxisName) {
        mXAxisName = XAxisName;
    }

    public float getValue() {
        return mValue;
    }

    public void setValue(float value) {
        mValue = value;
    }
}

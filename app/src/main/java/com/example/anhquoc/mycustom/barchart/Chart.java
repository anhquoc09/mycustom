package com.example.anhquoc.mycustom.barchart;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class Chart extends View {


    public Chart(Context context) {
        super(context);
        init();
    }

    public Chart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Chart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        setWillNotDraw(false);


    }
}

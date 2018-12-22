package com.example.anhquoc.mycustom;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.anhquoc.mycustom.charts.BarChart;
import com.example.anhquoc.mycustom.charts.Chart;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BarChart barChart = (BarChart) findViewById(R.id.barChart);

/*float[] arr =
  new float[]{11.1f, 8f, 100f, 7.5f, 3.5f, 2.5f, 10f*//*, 100f, 7.5f, 3.5f, 2.5f, 10f,
    100f, 7.5f, 3.5f,75f,50f,25f, 7.5f, 3.5f*//*};*/

    }
}

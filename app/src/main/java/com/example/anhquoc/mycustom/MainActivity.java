package com.example.anhquoc.mycustom;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.anhquoc.mycustom.charts.BarChart;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        TouchExample view = new TouchExample(this);
//        setContentView(view);

        BarChart barChart = (BarChart) findViewById(R.id.barChart);

        barChart.add("1", 20);
        barChart.add("2", 30);
        barChart.add("3", 40);
        barChart.add("4", 100);
        barChart.add("5", 50);
        barChart.add("6", 25);
        barChart.add("1", 20);
        barChart.add("2", 30);
        barChart.add("3", 40);
        barChart.add("4", 100);
        barChart.add("5", 50);
        barChart.add("6", 25);
        barChart.add("1", 20);
        barChart.add("2", 30);
        barChart.add("3", 40);
        barChart.add("4", 100);
        barChart.add("5", 50);
        barChart.add("6", 25);

/*float[] arr =
  new float[]{11.1f, 8f, 100f, 7.5f, 3.5f, 2.5f, 10f*//*, 100f, 7.5f, 3.5f, 2.5f, 10f,
    100f, 7.5f, 3.5f,75f,50f,25f, 7.5f, 3.5f*//*};*/

    }
}

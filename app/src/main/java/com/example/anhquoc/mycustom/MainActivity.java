package com.example.anhquoc.mycustom;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.anhquoc.mycustom.Entries.BarEntry;
import com.example.anhquoc.mycustom.charts.BarChart;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BarChart barChart = (BarChart) findViewById(R.id.barChart);
        ArrayList arrayList = new ArrayList<>();
        BarEntry barData = new BarEntry("Jan", 11.1f);
        arrayList.add(barData);
        barData = new BarEntry("Feb", 8f);
        arrayList.add(barData);
        barData = new BarEntry("Mar", 20f);
        arrayList.add(barData);
        barData = new BarEntry("Apr", 43f);
        arrayList.add(barData);
        barData = new BarEntry("May", 99f);
        arrayList.add(barData);
        barData = new BarEntry("Jun", 12.5f);
        arrayList.add(barData);
/*float[] arr =
  new float[]{11.1f, 8f, 100f, 7.5f, 3.5f, 2.5f, 10f*//*, 100f, 7.5f, 3.5f, 2.5f, 10f,
    100f, 7.5f, 3.5f,75f,50f,25f, 7.5f, 3.5f*//*};*/
        BarEntry[] arrayBarData = (BarEntry[]) arrayList.toArray(new BarEntry[arrayList.size()]);
        barChart.setYAxisData(arrayBarData);
    }
}

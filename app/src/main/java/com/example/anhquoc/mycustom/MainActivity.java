package com.example.anhquoc.mycustom;

import android.os.Bundle;
import android.widget.TextView;

import com.example.anhquoc.mycustom.charts.BarChart;
import com.example.anhquoc.mycustom.charts.Entry;

import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements OnItemSelectedListener{

    @BindView(R.id.barChart)
    BarChart mBarChart;

    @BindView(R.id.text_max)
    TextView mTextMax;

    @BindView(R.id.text_selected)
    TextView mTextSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mBarChart.setOnItemSelectedListener(this);

        mBarChart.add(20);
        mBarChart.add(30);
        mBarChart.add(40);
        mBarChart.add(100);
        mBarChart.add(50);
        mBarChart.add(25);
        mBarChart.add(120);
        mBarChart.add(30);
        mBarChart.add(40);
        mBarChart.add(100);
        mBarChart.add(150);
        mBarChart.add(25);
        mBarChart.add(20);
        mBarChart.add(30);
        mBarChart.add(40);
        mBarChart.add(100);
        mBarChart.add(50);
        mBarChart.add(25);

        mTextMax.setText(String.format(Locale.US, "Max: %.1f", mBarChart.getMaxValue()));
    }

    @Override
    public void onItemSelected(Entry entry) {
        mTextSelected.setText(String.format(Locale.US, "Selected: %.1f", entry.getValue()));
    }

    @Override
    public void onNothingSelected() {
        mTextSelected.setText("Selected: ");
    }
}

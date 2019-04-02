package com.example.anhquoc.mycustom;

import android.os.Bundle;
import android.widget.TextView;

import com.example.anhquoc.mycustom.charts.BarChart;
import com.example.anhquoc.mycustom.charts.BarEntry;
import com.example.anhquoc.mycustom.charts.Entry;

import java.util.ArrayList;
import java.util.List;
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

    private final List<BarEntry> mEntries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mBarChart.setOnItemSelectedListener(this);

        mEntries.add(new BarEntry("1", 20));
        mEntries.add(new BarEntry("2", 30));
        mEntries.add(new BarEntry("3", 40));
        mEntries.add(new BarEntry("4", 100));
        mEntries.add(new BarEntry("5", 50));
        mEntries.add(new BarEntry("6", 25));
        mEntries.add(new BarEntry("1", 120));
        mEntries.add(new BarEntry("2", 30));
        mEntries.add(new BarEntry("3", 40));
        mEntries.add(new BarEntry("4", 100));
        mEntries.add(new BarEntry("5", 150));
        mEntries.add(new BarEntry("6", 25));
        mEntries.add(new BarEntry("1", 20));
        mEntries.add(new BarEntry("2", 30));
        mEntries.add(new BarEntry("3", 40));
        mEntries.add(new BarEntry("4", 100));
        mEntries.add(new BarEntry("5", 50));
        mEntries.add(new BarEntry("6", 25));

//        mBarChart.add(mEntries);

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

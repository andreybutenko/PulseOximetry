package edu.washington.cs.sensor.pulseoximetry;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;

import edu.washington.cs.sensor.pulseoximetry.models.Measurement;
import edu.washington.cs.sensor.pulseoximetry.util.DataAnalyzer;
import edu.washington.cs.sensor.pulseoximetry.util.PreviewHelper;

public class MeasurementDetailActivity extends AppCompatActivity {
    public static final String DATE_TIME_EXTRA = "DATE_TIME_EXTRA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement_detail);

        String dateTime = getIntent().getStringExtra(DATE_TIME_EXTRA);
        final Measurement measurement = Measurement.find(Measurement.class, "time = ?", dateTime).get(0);

        LineChart irChart = (LineChart) findViewById(R.id.ir_chart);
        LineChart redChart = (LineChart) findViewById(R.id.red_chart);

        chartConfig(irChart, "IR");
        chartConfig(redChart, "Red");

        refreshChart(irChart, PreviewHelper.getFiltered(measurement.getIrValues()), Color.BLUE);
        refreshChart(redChart, PreviewHelper.getFiltered(measurement.getRdValues()), Color.RED);

        int bpm = (int) DataAnalyzer.getBpm(PreviewHelper.getFiltered(measurement.getIrValues()), PreviewHelper.BPM_THRESHOLD);
        int so2 = (int) DataAnalyzer.getSO2(PreviewHelper.getFiltered(measurement.getIrValues()), PreviewHelper.getFiltered(measurement.getRdValues()));

        Log.d("MDA", bpm + ", " + so2);

        TextView bpmText = (TextView) findViewById(R.id.bpm_value);
        bpmText.setText(String.valueOf(bpm));

        TextView so2Text = (TextView) findViewById(R.id.so2_value);
        so2Text.setText(String.valueOf(so2));

        setupBackButton(measurement.getFormattedTime());
    }

    private void setupBackButton(String title) {
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void chartConfig(LineChart chart, String desc) {
        Description description = new Description();
        description.setText(desc);
        description.setTextColor(Color.BLACK);
        description.setTextSize(10f);
        chart.setDescription(description);

        chart.getLegend().setEnabled(false);
        chart.setNoDataText("");
        chart.setAutoScaleMinMaxEnabled(true);
        chart.setDrawBorders(true);

        chart.getAxisRight().setEnabled(false);
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setEnabled(true);
        yAxis.setTextColor(Color.BLACK);
        yAxis.setLabelCount(7, true);
        yAxis.setDrawGridLines(false);
        //yAxis.setAxisMinimum(0);

        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        chart.invalidate();
    }

    private void refreshChart(LineChart chart, List<Entry> entries, int color) {
        LineDataSet dataSet = new LineDataSet(entries, "Data");
        dataSet.setColor(color);
        dataSet.setDrawCircles(false);

        LineData lineData = new LineData(dataSet);
        lineData.setDrawValues(false);
        chart.setData(lineData);

        chart.notifyDataSetChanged();
        chart.moveViewToX(lineData.getEntryCount());
    }
}

package edu.washington.cs.sensor.pulseoximetry;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;

import edu.washington.cs.sensor.pulseoximetry.models.Measurement;

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

        refreshChart(irChart, measurement.getIrValues(), Color.BLUE);
        refreshChart(redChart, measurement.getRdValues(), Color.RED);

        Button computeButton = findViewById(R.id.compute_button);
        computeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayResults(measurement);
            }
        });
    }

    private void displayResults(Measurement measurement) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Result");
        alertDialog.setMessage("TODO"); // TODO
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public static String getByteArrayAsString(byte[] bytes) {
        String result = "";
        for(byte aByte : bytes) {
            result += String.valueOf(aByte) + ",";
        }
        return result;
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
        yAxis.setAxisMinimum(0);

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

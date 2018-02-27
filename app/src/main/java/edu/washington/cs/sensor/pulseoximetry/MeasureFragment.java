package edu.washington.cs.sensor.pulseoximetry;


import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import edu.washington.cs.sensor.pulseoximetry.models.Measurement;
import edu.washington.cs.sensor.pulseoximetry.util.DataAnalyzer;


public class MeasureFragment extends Fragment {
    TextView statusTextView;
    LineChart irChart;
    LineChart redChart;
    List<Entry> irEntries = new ArrayList<Entry>();
    List<Entry> redEntries = new ArrayList<Entry>();
    byte[] irRawData;
    byte[] redRawData;

    public MeasureFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();

        Button saveButton = (Button) getActivity().findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCurrentEntry();
            }
        });

        Button clearButton = (Button) getActivity().findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irEntries.clear();
                irChart.clear();

                redEntries.clear();
                redChart.clear();
            }
        });

        Button calculateButton = (Button) getActivity().findViewById(R.id.compute_button);
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
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
        });

        statusTextView = (TextView) getActivity().findViewById(R.id.status_text);

        irChart = (LineChart) getActivity().findViewById(R.id.ir_chart);
        redChart = (LineChart) getActivity().findViewById(R.id.red_chart);

        chartConfig(irChart, "IR");
        chartConfig(redChart, "Red");
    }

    private void saveCurrentEntry() {
        Measurement newMeasurement = new Measurement(
                irRawData,
                redRawData,
                irEntries,
                redEntries
        );
        newMeasurement.save();

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Saved!");
        alertDialog.setMessage("Awesome :)");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_measure, container, false);
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
        chart.setVisibleXRangeMaximum(50);
        chart.moveViewToX(lineData.getEntryCount());
    }

    public void setStatus(String newStatus) {
        statusTextView.setText(newStatus);
    }

    public void setRawData(ArrayList<Byte> irData, ArrayList<Byte> redData) {
        irRawData = new byte[irData.size()];
        redRawData = new byte[redData.size()];

        try {
            for(int i = 0; i < irData.size() - 1; i++) {
                irRawData[i] =
                        irData.get(i);
            }

            for(int i = 0; i < redData.size() - 1; i++) {
                redRawData[i] =
                        redData.get(i);
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }

    }

    public void addIREntry(float[] irData) {
        for(int i = 0; i < irData.length; i++) {
            Entry newEntry = new Entry(irEntries.size(), irData[i]);
            irEntries.add(newEntry);
        }
        refreshChart(irChart, irEntries, Color.BLUE);
    }

    public void addRedEntry(float[] redData) {
        for(int i = 0; i < redData.length; i++) {
            Entry newEntry = new Entry(redEntries.size(), redData[i]);
            redEntries.add(newEntry);
        }
        refreshChart(redChart, redEntries, Color.RED);
    }
}

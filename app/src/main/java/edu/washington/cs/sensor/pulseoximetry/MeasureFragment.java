package edu.washington.cs.sensor.pulseoximetry;


import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.AppCompatDrawableManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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
import edu.washington.cs.sensor.pulseoximetry.models.MeasurementUpdate;
import edu.washington.cs.sensor.pulseoximetry.util.DataAnalyzer;
import edu.washington.cs.sensor.pulseoximetry.util.PreviewHelper;


public class MeasureFragment extends Fragment {
    LineChart irChart;
    LineChart redChart;
    List<Entry> irEntries = new ArrayList<Entry>();
    List<Entry> redEntries = new ArrayList<Entry>();
    MaterialDialog dialog = null;
    boolean showingProgressDialog = false;
    boolean introMode = true;

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

        Button calculateButton = (Button) getActivity().findViewById(R.id.compute_button);
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMeasurementSuccessDialog();
            }
        });

        Button clearButton = (Button) getActivity().findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetFragment();
            }
        });

        irChart = (LineChart) getActivity().findViewById(R.id.ir_chart);
        redChart = (LineChart) getActivity().findViewById(R.id.red_chart);

        chartConfig(irChart, "IR");
        chartConfig(redChart, "Red");
    }

    private void resetFragment() {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content, new MeasureFragment())
                .commit();
    }

    private void saveCurrentEntry() {
        showSaveProgressDialog();

        final Measurement newMeasurement = new Measurement(
                irEntries,
                redEntries
        );

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new MeasurementSaveAsyncTask(MeasureFragment.this, newMeasurement).execute();
            }
        }, 100);


//        newMeasurement.save();
//
//        showSaveSuccessDialog();
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

    public void sendUpdate(MeasurementUpdate update) {
        if(introMode) {
            disableIntroMode();
        }

        if(update.isError()) {
            showMeasurementErrorDialog();
        }
        else if(update.isSuccess()) {
            showMeasurementSuccessDialog();
        }
        else {
            addIREntry(update.getIrData());
            addRedEntry(update.getRdData());
            updateMeasurementProgressDialog(update);
        }
    }

    public void disableIntroMode() {
        introMode = false;
        getActivity().findViewById(R.id.empty_screen).setVisibility(View.GONE);
        getActivity().findViewById(R.id.active_screen).setVisibility(View.VISIBLE);
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

    // Dialog methods below this point

    public void updateMeasurementProgressDialog(MeasurementUpdate update) {
        if(!showingProgressDialog) {
            dialog = new MaterialDialog.Builder(getActivity())
                .title("Measuring...")
                .content("Hold for at least 20 seconds for accurate results...")
                .progress(false, 100, false)
                .autoDismiss(false)
                .cancelable(false)
                .show();
        }

        dialog.setProgress((int) (100 * update.getTimeElapsed() / MeasurementAsyncTask.MEASURE_TIME));
        dialog.setContent("Hold for " + update.getTimeRemaining() + " more seconds...");

        showingProgressDialog = true;
    }

    public void showMeasurementSuccessDialog() {
        showingProgressDialog = false;
        if(dialog != null) {
            dialog.dismiss();
        }

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        dialog = new MaterialDialog.Builder(getActivity())
                .title("All done!")
                .content(PreviewHelper.getResults(irEntries, redEntries))
                .positiveText("Save")
                .negativeText("Discard")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        saveCurrentEntry();
                    }
                })
                .icon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_sentiment_satisfied_black_24dp, null))
                .show();
    }

    public void showMeasurementErrorDialog() {
        showingProgressDialog = false;
        if(dialog != null) {
            dialog.dismiss();
        }

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        dialog = new MaterialDialog.Builder(getActivity())
            .title("Contact lost")
            .content("Make sure you hold the devices securely together for 20 seconds.")
            .positiveText("Ok")
            .icon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_sentiment_dissatisfied_black_24dp, null))
            .show();
    }

    public void showSaveProgressDialog() {
        if(dialog != null) {
            dialog.dismiss();
        }

        dialog = new MaterialDialog.Builder(getActivity())
                .title("Saving...")
                .content("This should only take a few seconds.")
                .progress(true, 0)
                .autoDismiss(false)
                .cancelable(false)
                .show();
    }

    public void showSaveSuccessDialog() {
        if(dialog != null) {
            dialog.dismiss();
        }

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        dialog = new MaterialDialog.Builder(getActivity())
                .title("Saved!")
                .content("Access saved measurements through history tab.")
                .positiveText("Ok")
                .icon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_sentiment_very_satisfied_black_24dp, null))
                .show();
    }
}

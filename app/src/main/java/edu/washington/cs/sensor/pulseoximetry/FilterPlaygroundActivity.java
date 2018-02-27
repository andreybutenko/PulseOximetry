package edu.washington.cs.sensor.pulseoximetry;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
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
import edu.washington.cs.sensor.pulseoximetry.util.EntryHelper;
import edu.washington.cs.sensor.pulseoximetry.util.Filter;

public class FilterPlaygroundActivity extends AppCompatActivity {
    public static final String DATE_TIME_EXTRA = "DATE_TIME_EXTRA";

    LineChart irChart;
    LineChart rdChart;

    TextView estimateText;

    TextView frequencyText;
    SeekBar frequencyBar;

    TextView sampleText;
    SeekBar sampleBar;

    TextView resonanceText;
    SeekBar resonanceBar;

    TextView boostText;
    SeekBar boostBar;

    TextView thresholdText;
    SeekBar thresholdBar;

    boolean enable = true;
    float[] irValues;
    float[] rdValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_playground);

        String dateTime = getIntent().getStringExtra(DATE_TIME_EXTRA);
        Measurement measurement = Measurement.find(Measurement.class, "time = ?", dateTime).get(0);
        irValues = measurement.getIrValuesRaw();
        rdValues = measurement.getRdValuesRaw();

        estimateText = (TextView) findViewById(R.id.estimate);

        irChart = (LineChart) findViewById(R.id.playground_ir_chart);
        chartConfig(irChart, "IR");

        rdChart = (LineChart) findViewById(R.id.playground_rd_chart);
        chartConfig(rdChart, "Red");

        Button applyButton = findViewById(R.id.apply_button);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayResults();
            }
        });

        final Button toggle = findViewById(R.id.toggle_low);
        if(enable) {
            toggle.setText("Disable");
        }
        else {
            toggle.setText("Enable");
        }
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enable = !enable;
                displayResults();
                if(enable) {
                    toggle.setText("Disable");
                }
                else {
                    toggle.setText("Enable");
                }
            }
        });

        TextView entriesCount = (TextView) findViewById(R.id.entries_count);
        entriesCount.setText("n = " + irValues.length);

        frequencyText = (TextView) findViewById(R.id.frequency_text);
        frequencyBar = (SeekBar) findViewById(R.id.frequency_bar);
        setupListener("Frequency", frequencyText, frequencyBar);

        sampleText = (TextView) findViewById(R.id.sample_text);
        sampleBar = (SeekBar) findViewById(R.id.sample_bar);
        setupListener("Sample Rate", sampleText, sampleBar);

        resonanceText = (TextView) findViewById(R.id.resonance_text);
        resonanceBar = (SeekBar) findViewById(R.id.resonance_bar);
        setupListener("Resonance", resonanceText, resonanceBar, 1f / 100f);

        boostText = (TextView) findViewById(R.id.boost_text);
        boostBar = (SeekBar) findViewById(R.id.boost_bar);
        setupListener("Low pass freq boost", boostText, boostBar);

        thresholdText = (TextView) findViewById(R.id.threshold_text);
        thresholdBar = (SeekBar) findViewById(R.id.threshold_bar);
        setupListener("Threshold", thresholdText, thresholdBar);

        displayResults();
    }

    private void setupListener(String label, TextView textView, SeekBar seekBar) {
        setupListener(label, textView, seekBar, 1);
    }

    private void setupListener(final String label, final TextView textView, SeekBar seekBar, final float factor) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d("PROGRESS", "p = " + progress + "\nf = " + factor + "\nr = " + (progress * factor));
                textView.setText(label + ": " + (progress * factor));
                displayResults();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    //        entries = EntryHelper.applyFilter(entries, frequencyBar.getProgress(), sampleBar.getProgress(), Filter.PassType.Lowpass, resonanceBar.getProgress() / 100);
//        entries = EntryHelper.applyFilter(entries, 100, 100, Filter.PassType.Highpass, 1f);

    private void displayResults() {
        int threshold = thresholdBar.getProgress();

        List<Entry> irEntries = EntryHelper.inflateEntries(irValues);
        List<Entry> rdEntries = EntryHelper.inflateEntries(rdValues);

        if(enable) {
            irEntries = EntryHelper.applyFilter(irEntries, frequencyBar.getProgress() + boostBar.getProgress(), sampleBar.getProgress(), Filter.PassType.Lowpass, resonanceBar.getProgress() / 100f);
            rdEntries = EntryHelper.applyFilter(rdEntries, frequencyBar.getProgress() + boostBar.getProgress(), sampleBar.getProgress(), Filter.PassType.Lowpass, resonanceBar.getProgress() / 100f);

            irEntries = EntryHelper.applyFilter(irEntries, frequencyBar.getProgress(), sampleBar.getProgress(), Filter.PassType.Highpass, resonanceBar.getProgress() / 100f);
            rdEntries = EntryHelper.applyFilter(rdEntries, frequencyBar.getProgress(), sampleBar.getProgress(), Filter.PassType.Highpass, resonanceBar.getProgress() / 100f);
        }

        String result4 = String.valueOf(DataAnalyzer.getBpm(irEntries, threshold)) + " bpm // "
                + String.valueOf(DataAnalyzer.getSO2(irEntries, rdEntries)) + "% O2";
        estimateText.setText(result4);

        refreshChart(irChart, irEntries, Color.BLUE);
        refreshChart(rdChart, rdEntries, Color.RED);

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

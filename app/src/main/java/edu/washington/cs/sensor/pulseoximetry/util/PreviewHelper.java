package edu.washington.cs.sensor.pulseoximetry.util;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrey on 2/27/2018.
 */

public class PreviewHelper {
    public static int BPM_THRESHOLD = 0;
    public static float FREQUENCY_LOW = 101;
    public static float FREQUENCY_HIGH = 103;
    public static int SAMPLE_RATE = 100;
    public static float RESONANCE = 1;

    public static String getResults(List<Entry> irEntries, List<Entry> rdEntries) {
        float bpm = DataAnalyzer.getBpm(getFiltered(irEntries), BPM_THRESHOLD);
        float so2 = DataAnalyzer.getSO2(getFiltered(irEntries), getFiltered(rdEntries));
        return String.valueOf(bpm) + " bpm \n"
            + String.valueOf(so2) + "%SpO2";
    }

    public static List<Entry> getFiltered(List<Entry> entries) {
        List<Entry> result = new ArrayList<>();

        result = EntryHelper.applyFilter(entries, FREQUENCY_LOW, SAMPLE_RATE, Filter.PassType.Lowpass, RESONANCE);
        result = EntryHelper.applyFilter(result, FREQUENCY_HIGH, SAMPLE_RATE, Filter.PassType.Highpass, RESONANCE);

        return result;
    }
}

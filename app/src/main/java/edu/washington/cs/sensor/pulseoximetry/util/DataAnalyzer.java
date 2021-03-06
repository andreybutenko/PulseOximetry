package edu.washington.cs.sensor.pulseoximetry.util;

import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.util.List;

/**
 * Created by Andrey on 2/20/2018.
 */

public class DataAnalyzer {
    private static final String TAG = "DATA_ANALYZER";

    // Calculate bpm from set of entries by finding number of peaks
    // A peak is detected where entries turn around from increasing to decreasing
    // Threshold determines how much higher a peak must be from the previous valley
    public static float getBpm(List<Entry> entries, int threshold) {
        float[] data = EntryHelper.entriesToFloats(entries);

        int peaks = 0;
        boolean increasing = true;
        float prevPoint = 0;
        float prevPeak = 0;
        float prevLow = 0;

        // BPMS: 73 / 73 / 80 / 93 / 98 / 98
        // OXY: ? / ? / ? / 99 / 99 / 99
        for(float point : data) {
            //Log.d("DA4", "curr = " + point + "\n prev = " + prevPoint + "\n increasing = " + increasing + "\n peaks = " + peaks);
            if(point < prevPoint && increasing) {
                increasing = false;
                prevPeak = point;
                if(point > prevLow + threshold) {
                    peaks++;
                }
            }
            else if(point > prevPoint && !increasing) {
                increasing = true;
                prevLow = point;
            }

            prevPoint = point;
        }

        return peaks * 60f / 20f;
    }

    public static float getSO2(List<Entry> irEntries, List<Entry> rdEntries) {
        float[] irData = EntryHelper.entriesToFloats(irEntries);
        float[] rdData = EntryHelper.entriesToFloats(rdEntries);

        float irMax = 0;
        float irMin = 1;
        float rdMax = 0;
        float rdMin = 1;

        for(float value : irData) {
            irMax = Math.max(value, irMax);
            if(value != 0)
                irMin = Math.min(value, irMin);
        }

        for(float value : rdData) {
            rdMax = Math.max(value, rdMax);
            if(value != 0)
                rdMin = Math.min(value, rdMin);
        }

        double Ros = Math.log(irMax) / Math.log(rdMax);

//        Log.d("DA4", "irMax = " + irMax + "\n" +
//                "irMin = " + irMin + "\n" +
//                "rdMax = " + rdMax + "\n" +
//                "rdMin = " + rdMin + "\n" +
//                "Ros   = " + Ros);

        return (float) (110 - 12 * Ros);
    }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    private static float getRangeAverage(List<Entry> entries, int from, int to) {
        int max = clamp(to,   0, entries.size());
        int min = clamp(from, 0, entries.size());

        float sum = 0;
        for(int i = min; i < max; i++) {
            sum += entries.get(i).getY();
        }

        return sum / (max - min);
    }

    public static void removeZeroes(List<Entry> entries) {
        //removeZeroes(entries, entries.size());
    }

    private static void removeZeroes(List<Entry> entries, int maxRange) {
        for(int i = 0; i < maxRange; i++) {
            if(entries.get(i).getY() < 5000 &&
                    getRangeAverage(entries, i - 5, i - 2) > 20000 ||
                    getRangeAverage(entries, i + 2,  i  + 6) > 20000) {
                Log.d(TAG, "Zero at " + i + "/" + entries.get(i).getX());

                entries.get(i).setY(getRangeAverage(entries, i - 4, i + 4));
            }
        }
    }

}

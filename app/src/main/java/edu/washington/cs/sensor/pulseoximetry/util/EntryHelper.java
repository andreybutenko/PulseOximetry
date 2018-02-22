package edu.washington.cs.sensor.pulseoximetry.util;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrey on 1/23/2018.
 */

public class EntryHelper {
    public static float[] entriesToFloats(List<Entry> entries) {
        float[] result = new float[entries.size()];
        for(int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            float value = entry.getY();
            result[i] = value;
        }
        return result;
    }
//
//    public static List<Entry> floatsToEntries(float[] entries) {
//        List<Entry> result = new ArrayList<Entry>();
//        for(int i = 0; i < entries.length; i++) {
//            Entry newEntry = new Entry(i, entries[i]);
//            result.add(newEntry);
//        }
//        return result;
//    }aS

    public static String deflateEntries(List<Entry> entries) {
        String result = "";

        for(int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            float value = entry.getY();

            if(i == 0) {
                result += value;
            }
            else {
                result += "," + value;
            }
        }

        return result;
    }

    public static List<Entry> inflateEntries(String entries) {
        List<Entry> result = new ArrayList<Entry>();
        float[] values = csvToArray(entries);

        for(int i = 0; i < values.length; i++) {
            float value = values[i];
            Entry newEntry = new Entry(i, value);
            result.add(newEntry);
        }

        return result;
    }

    public static List<Entry> inflateEntries(float[] values) {
        List<Entry> result = new ArrayList<Entry>();

        for(int i = 0; i < values.length; i++) {
            float value = values[i];
            Entry newEntry = new Entry(i, value);
            result.add(newEntry);
        }

        return result;
    }

    public static List<Entry> inflateEntriesFiltered(String entries) {
        List<Entry> result = new ArrayList<Entry>();
        String[] splitString = entries.split(",");

        Filter lowPassFilter = new Filter(40, splitString.length, Filter.PassType.Highpass, 1);
        Filter highPassFilter = new Filter(40, splitString.length, Filter.PassType.Highpass, 1);

        for(int i = 0; i < splitString.length; i++) {
            float value = Float.parseFloat(splitString[i]);

            lowPassFilter.Update(value);
            value = lowPassFilter.getValue();

//            highPassFilter.Update(value);
//            value = highPassFilter.getValue();

            Entry newEntry = new Entry(i, value);
            result.add(newEntry);
        }

        return result;
    }

    public static List<Entry> applyFilter(float[] entries, float frequency, int sampleRate, Filter.PassType passType, float resonance) {
        List<Entry> result = new ArrayList<Entry>();
        Filter filter = new Filter(frequency, sampleRate,  passType, resonance);

        for(int i = 0; i < entries.length; i++) {
            float value = entries[i];

            filter.Update(value);
            value = filter.getValue();

            Entry newEntry = new Entry(i, value);
            result.add(newEntry);
        }

        return result;
    }

    public static List<Entry> applyFilter(List<Entry> entries, float frequency, int sampleRate, Filter.PassType passType, float resonance) {
        List<Entry> result = new ArrayList<Entry>();
        Filter filter = new Filter(frequency, sampleRate,  passType, resonance);

        for(int i = 0; i < entries.size(); i++) {
            float value = entries.get(i).getY();

            filter.Update(value);
            value = filter.getValue();

            Entry newEntry = new Entry(i, value);
            result.add(newEntry);
        }

        return result;
    }

    public static float[] csvToArray(String entries) {
        String[] splitString = entries.split(",");
        float[] result = new float[splitString.length];

        for(int i = 0; i < splitString.length; i++) {
            result[i] = Float.parseFloat(splitString[i]);
        }

        return result;
    }
}

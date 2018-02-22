package edu.washington.cs.sensor.pulseoximetry.util;

import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.util.List;

/**
 * Created by Andrey on 2/12/2018.
 */

public class DataAnalyzer3 {
    public static float getBpm(List<Entry> entries, float thresholdProportion) {
        float[] data = EntryHelper.entriesToFloats(entries);

        float max = 0f;
        for(int i = 0; i < data.length; i++) {
            max = Math.max(max, data[i]);
        }

        float threshold = max * thresholdProportion;

        int timeout = 30;
        int count = 0;

        for(int i = 0; i < data.length; i++) {
            if(timeout == 0 && data[i] > threshold) {
                count++;
                timeout = 20;
            }
            else if(timeout > 0) {
                timeout--;
            }
        }

        Log.d("DA3", "count = " + count);

        return count / (20f / 60f);
    }

    public static List<Entry> debugShowThresh(List<Entry> entries, float thresholdProportion) {
        float[] data = EntryHelper.entriesToFloats(entries);

        float max = 0f;
        for(int i = 0; i < data.length; i++) {
            max = Math.max(max, data[i]);
        }

        float threshold = max * thresholdProportion;
        int i = 0;
        boolean prevMatch = false;
        int inARow = 0;

        for(Entry entry : entries) {
            if(i < 30) {
                entry.setY(0);
            }

            if(entry.getY() <= threshold) {
                entry.setY(0);

                prevMatch = false;
                inARow = 0;
            }
            else {
                if(!prevMatch) {
                    prevMatch = true;
                }

                inARow++;

                if(inARow >= 20) {
                    inARow = 0;
                    entry.setY(0);
                }
            }

            i++;
        }

        return entries;
    }
}

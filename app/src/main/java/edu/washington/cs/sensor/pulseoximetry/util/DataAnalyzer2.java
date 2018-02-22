package edu.washington.cs.sensor.pulseoximetry.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Andrey on 1/16/2018.
 */

public class DataAnalyzer2 {
    private static int CONVOLVE_SIZE = 10;
    private float IR_min;
    private float RED_min;

    private ArrayList<Float> IR_max_arr;
    private ArrayList<Float> IR_min_arr;
    private ArrayList<Float> RED_max_arr;
    private ArrayList<Float> RED_min_arr;

    private enum LightType {
        RED,
        IR
    }

    private String result = "not calculated";

//    public DataAnalyzer2(byte[] m_ir_data, byte[] m_rd_data) {
//        float[] dst_ir = testNFCTag(m_ir_data, LightType.IR);
//        float[] dst_red = testNFCTag(m_rd_data, LightType.RED);
    public DataAnalyzer2(float[] dst_ir, float[] dst_red) {

//        levelUpArray(dst_ir);
//        levelUpArray(dst_red);

        //double SO2 = getOxygenLevel(dst_ir, dst_red);
        processData(dst_ir, LightType.IR);
        double BPM = calculateBPM(dst_ir);

        result = String.valueOf((int) BPM);
    }

    public String getResult() {
        return result;
    }


    private void levelUpArray(float[] filtered) {
        if(IR_min < 0 || RED_min < 0) {
            float min = Math.min(IR_min, RED_min);
            for (int i = 0; i < filtered.length; i++) {
                filtered[i] -= (min - 1);
            }
        }
    }

    private void processData(float[] filtered, LightType ir_red) {

        AMPD ampd = new AMPD(filtered);

        int[] max_indices = ampd.getMaxima();
        Log.d("Debug", "max_indices = " + Arrays.toString(max_indices));
        int[] min_indices = ampd.getMinima();
        Log.d("Debug", "min_indices = " + Arrays.toString(min_indices));


        ArrayList<Float> max_arr = new ArrayList<>();
        ArrayList<Float> min_arr = new ArrayList<>();
        ArrayList<Integer> max_index_arr = new ArrayList<>();
        ArrayList<Integer> min_index_arr = new ArrayList<>();


        for(int i = 0; i < max_indices.length; i++) {
            int index = max_indices[i];
            max_arr.add(filtered[index]);
            max_index_arr.add(index);
        }

        for(int i = 0; i < min_indices.length; i++) {
            int index = min_indices[i];
            min_arr.add(filtered[index]);
            min_index_arr.add(index);
        }

        double[] energy_vec = new double[filtered.length];
        for(int i = 1; i < filtered.length - 1; i++) {
            energy_vec[i] = (double) (filtered[i] * filtered[i]) - (double) (filtered[i - 1] * filtered[i + 1]);
        }

        switch (ir_red) {
            case IR:
                IR_max_arr = max_arr;
//                IR_max_index_arr = max_index_arr;
                IR_min_arr = min_arr;
//                IR_min_index_arr = min_index_arr;
//                IR_energy_vec = energy_vec;
                break;
            case RED:
                RED_max_arr = max_arr;
//                RED_max_index_arr = max_index_arr;
                RED_min_arr = min_arr;
//                RED_min_index_arr = min_index_arr;
//                RED_energy_vec = energy_vec;
                break;
        }
    }


    private double getOxygenLevel(float[] dst_ir, float[] dst_rd) {
        processData(dst_ir, LightType.IR);
        processData(dst_rd, LightType.RED);
        double ir_max = 0, ir_min = 0, red_max = 0, red_min = 0;
        //int ir_max_len = 0, ir_min_len = 0, red_max_len = 0, red_min_len = 0;


        double[] ir_max_arr = new double[IR_max_arr.size()];
        for(int i = 0; i < IR_max_arr.size(); i++) {
            ir_max_arr[i] = IR_max_arr.get(i);
        }
        Arrays.sort(ir_max_arr);
        ir_max = ir_max_arr[ir_max_arr.length / 2];

        double[] ir_min_arr = new double[IR_min_arr.size()];
        for(int i = 0; i < IR_min_arr.size(); i++) {
            ir_min_arr[i] = IR_min_arr.get(i);
        }
        Arrays.sort(ir_min_arr);
        ir_min = ir_min_arr[ir_min_arr.length / 2];

        double[] red_max_arr = new double[RED_max_arr.size()];
        for(int i = 0; i < RED_max_arr.size(); i++) {
            red_max_arr[i] = RED_max_arr.get(i);
        }
        Arrays.sort(red_max_arr);
        red_max = red_max_arr[red_max_arr.length / 2];

        double[] red_min_arr = new double[RED_min_arr.size()];
        for(int i = 0; i < RED_min_arr.size(); i++) {
            red_min_arr[i] = RED_min_arr.get(i);
        }
        Arrays.sort(red_min_arr);
        red_min = red_min_arr[red_min_arr.length / 2];




        /*double[] sorted_IR_energy_vec = new double[IR_energy_vec.length];
        double[] sorted_RED_energy_vec = new double[RED_energy_vec.length];
        for(int i = 0; i < IR_energy_vec.length; i++)
            sorted_IR_energy_vec[i] = IR_energy_vec[i];
        for(int i = 0; i < RED_energy_vec.length; i++)
            sorted_RED_energy_vec[i] = RED_energy_vec[i];
        Arrays.sort(sorted_IR_energy_vec);
        Arrays.sort(sorted_RED_energy_vec);

        double IR_threshold = sorted_IR_energy_vec[((sorted_IR_energy_vec.length - 1) * 96 / 100)];
        double RED_threshold = sorted_RED_energy_vec[((sorted_RED_energy_vec.length - 1) * 96 / 100)];

        for(int i = 3; i < IR_max_arr.size() - 1; i++) {
            double energy_level = IR_energy_vec[IR_max_index_arr.get(i)];
            if(energy_level < IR_threshold) {
            //if(true){
                ir_max += IR_max_arr.get(i);
                ir_max_len++;
            }
        }
        Log.d("Debug", "IR_max_arr.size() = " + Integer.toString(IR_max_arr.size()));
        Log.d("Debug", "ir_max_len = " + Integer.toString(ir_max_len));
        Log.d("Debug", "ir_max sum = " + String.format("%.2f", ir_max));
        Log.d("Debug", "ir_max = " + String.format("%.2f", ir_max / ir_max_len));
        ir_max /= ir_max_len;
        for(int i = 3; i < IR_min_arr.size() - 1; i++) {
            double energy_level = IR_energy_vec[IR_min_index_arr.get(i)];
            if(energy_level < IR_threshold) {
            //if(true) {
                ir_min += IR_min_arr.get(i);
                ir_min_len++;
            }
        }
        Log.d("Debug", "IR_min_arr.size() = " + Integer.toString(IR_min_arr.size()));
        Log.d("Debug", "ir_min_len = " + Integer.toString(ir_min_len));
        Log.d("Debug", "ir_min sum = " + String.format("%.2f", ir_min));
        Log.d("Debug", "ir_min = " + String.format("%.2f", ir_min / ir_min_len));
        ir_min /= ir_min_len;
        for(int i = 1; i < RED_max_arr.size() - 1; i++) {
            double energy_level = RED_energy_vec[RED_max_index_arr.get(i)];
            if(energy_level < RED_threshold) {
                red_max += RED_max_arr.get(i);
                red_max_len++;
            }
        }
        Log.d("Debug", "RED_max_arr.size() = " + Integer.toString(RED_max_arr.size()));
        Log.d("Debug", "red_max_len = " + Integer.toString(red_max_len));
        Log.d("Debug", "red_max sum = " + String.format("%.2f", red_max));
        Log.d("Debug", "red_max = " + String.format("%.2f", red_max / red_max_len));
        red_max /= red_max_len;
        for(int i = 1; i < RED_min_arr.size() - 1; i++) {
            double energy_level = RED_energy_vec[RED_min_index_arr.get(i)];
            if(energy_level < RED_threshold) {
                red_min += RED_min_arr.get(i);
                red_min_len++;
            }
        }
        Log.d("Debug", "RED_min_arr.size() = " + Integer.toString(IR_min_arr.size()));
        Log.d("Debug", "red_min_len = " + Integer.toString(red_min_len));
        Log.d("Debug", "red_min sum = " + String.format("%.2f", red_min));
        Log.d("Debug", "red_min = " + String.format("%.2f", red_min / red_min_len));
        red_min /= red_min_len;*/


        return getSO2((float) ir_max, (float) ir_min, (float) red_max, (float) red_min);
    }

    private double getSO2(float ir_max, float ir_min, float red_max, float red_min) {
        double Ros = Math.log((double) ir_max / (double) ir_min) / Math.log((double) red_max / (double) red_min);
        Log.d("Debug", "ir_max = " + String.format("%.2f", ir_max));
        Log.d("Debug", "ir_min = " + String.format("%.2f", ir_min));
        Log.d("Debug", "red_max = " + String.format("%.2f", red_max));
        Log.d("Debug", "red_min = " + String.format("%.2f", red_min));
        Log.d("Debug", "Ros = " + String.format("%.2f", Ros));
        double SO2 = 110 - 12 * Ros;
        return SO2;
    }

    private double calculateBPM(float[] dst) {
        double T = 0.015;
        double N = (double) (dst.length);
        double Total_time = T * N;

        return (60 * (double) IR_min_arr.size()) / Total_time;
    }

    private float[] testNFCTag(byte[] measurement, LightType ir_red) {
        float[] dst = new float[measurement.length / 2];
        for(int i = 0; i < measurement.length; i += 2) {
            int d = 0;
            d |= (measurement[i + 1] & 0xFF);
            d = d << 8;
            d |= (measurement[i] & 0xFF);
            dst[i / 2] = (float) d;
        }
        switch(ir_red) {
            case IR:
//                IR_raw = dst;
                break;
            case RED:
//                RD_raw = dst;
                break;
        }

        float[] convolved = filterFunction_3(dst, ir_red);

        return convolved;
    }

    private float[] filterFunction_1(float[] dst) {
        float[] convolved = new float[dst.length - (CONVOLVE_SIZE - 1)];
        Queue<Float> q = new LinkedList<Float>();
        float sum = 0;
        float max = Float.MIN_VALUE, min = Float.MAX_VALUE;
        for(int i = 0; i < CONVOLVE_SIZE; i++) {
            q.add(dst[i]);
            sum += dst[i];
        }
        for(int i = 0; i < convolved.length; i++) {
            convolved[i] = sum / ((float) CONVOLVE_SIZE);
            max = Math.max(max, convolved[i]);
            min = Math.min(min, convolved[i]);
            if (i + CONVOLVE_SIZE < dst.length) {
                sum -= q.poll();
                sum += dst[i + CONVOLVE_SIZE];
                q.add(dst[i + CONVOLVE_SIZE]);
            }
        }
        return convolved;
    }

    private float[] filterFunction_2(float[] dst, float smoothing) {
        float[] filtered = new float[dst.length];
        filtered[0] = dst[0];
        float value = dst[0];
        float max = Float.MIN_VALUE, min = Float.MAX_VALUE;
        for(int i = 1; i < dst.length; i++) {
            float cur = dst[i];
            value += (cur - value) / smoothing;
            filtered[i] = value;
            max = Math.max(max, value);
            min = Math.min(min, value);
        }
        return filtered;
    }

    private float[] filterFunction_3(float[] dst, LightType ir_red) {
        float[] filtered = new float[dst.length];
        Filter filter = new Filter(/*1000*/ 150 , dst.length, Filter.PassType.Lowpass, (float) 1);
        float max = Float.MIN_VALUE, min = Float.MAX_VALUE;
        for(int i = 0; i < dst.length; i++) {
            filter.Update(dst[i]);
            filtered[i] = filter.getValue();
            max = Math.max(max, filtered[i]);
            min = Math.min(min, filtered[i]);
        }

        filter = new Filter(40, dst.length, Filter.PassType.Highpass, (float) 1);
        for(int i = 0; i < filtered.length; i++) {
            filter.Update(filtered[i]);
            filtered[i] = filter.getValue();
            max = Math.max(max, filtered[i]);
            min = Math.min(min, filtered[i]);
        }
        switch (ir_red) {
            case IR:
                IR_min = min;
                break;
            case RED:
                RED_min = min;
                break;
        }
        Log.d("Debug", "min = " + Float.toString(min));
        //filtered = filterFunction_2(filtered, 10);
        return filtered;
    }
}

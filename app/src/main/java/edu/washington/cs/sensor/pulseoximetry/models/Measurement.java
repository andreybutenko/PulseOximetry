package edu.washington.cs.sensor.pulseoximetry.models;

import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.orm.SugarRecord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.washington.cs.sensor.pulseoximetry.util.EntryHelper;

/**
 * Created by Andrey on 1/15/2018.
 */

public class Measurement extends SugarRecord {
    String time; // creation date
    byte[] irMeasure;
    byte[] rdMeasure;
    String irValues;
    String rdValues;

    public Measurement() {
        // Required empty public constructor
    }

    public Measurement(List<Entry> irValues, List<Entry> rdValues) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        time = sdf.format(Calendar.getInstance().getTime());

        this.irValues = EntryHelper.deflateEntries(irValues);
        this.rdValues = EntryHelper.deflateEntries(rdValues);
    }

    public Measurement(byte[] irMeasure, byte[] rdMeasure, List<Entry> irValues, List<Entry> rdValues) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        time = sdf.format(Calendar.getInstance().getTime());

        this.irMeasure = irMeasure;
        this.rdMeasure = rdMeasure;
        this.irValues = EntryHelper.deflateEntries(irValues);
        this.rdValues = EntryHelper.deflateEntries(rdValues);
    }

    public String getRawTime() {
        return this.time;
    }

    public String getFormattedTime() {
        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat readableFormat = new SimpleDateFormat("MMMM dd, h:mm aa");

        try {
            Date date = parseFormat.parse(this.time);
            return readableFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return this.time;
    }

    public byte[] getIrMeasure() {
        return irMeasure;
    }

    public byte[] getRdMeasure() {
        return rdMeasure;
    }

    public List<Entry> getIrValues() {
        return EntryHelper.inflateEntries(irValues);
    }

    public float[] getIrValuesRaw() {
        return EntryHelper.csvToArray(irValues);
    }

    public List<Entry> getRdValues() {
        return EntryHelper.inflateEntries(rdValues);
    }

    public float[] getRdValuesRaw() {
        return EntryHelper.csvToArray(rdValues);
    }
}

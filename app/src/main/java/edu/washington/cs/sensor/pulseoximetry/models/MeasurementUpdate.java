package edu.washington.cs.sensor.pulseoximetry.models;

import java.text.DecimalFormat;

import edu.washington.cs.sensor.pulseoximetry.MeasurementAsyncTask;

/**
 * Created by Andrey on 2/27/2018.
 */

public class MeasurementUpdate {
    private String errorMsg;
    private boolean error = false;
    private boolean success = false;

    private float timeElapsed; // ms
    private float[] irData;
    private float[] rdData;

    public MeasurementUpdate(boolean success) {
        this.success = success;
    }

    public MeasurementUpdate(String errorMsg) {
        error = true;
        this.errorMsg = errorMsg;
    }

    public MeasurementUpdate(float timeElapsed, float[] irData, float[] rdData) {
        this.timeElapsed = timeElapsed;
        this.irData = irData;
        this.rdData = rdData;
    }

    public String getError() {
        return errorMsg;
    }

    public boolean isSuccess()  {
        return success;
    }

    public boolean isError() {
        return error;
    }

    public float getTimeElapsed() {
        return timeElapsed;
    }

    public float getTimeRemainingRaw() {
        return Math.max(MeasurementAsyncTask.MEASURE_TIME - timeElapsed, 0);
    }

    public String getTimeRemaining() {
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        return decimalFormat.format(getTimeRemainingRaw() / 1000f);
    }

    public float[] getIrData() {
        return irData;
    }

    public float[] getRdData() {
        return rdData;
    }
}

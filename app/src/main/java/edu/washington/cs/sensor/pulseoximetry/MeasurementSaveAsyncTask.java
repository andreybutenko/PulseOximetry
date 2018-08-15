package edu.washington.cs.sensor.pulseoximetry;

import android.os.AsyncTask;

import edu.washington.cs.sensor.pulseoximetry.models.Measurement;
import edu.washington.cs.sensor.pulseoximetry.models.MeasurementUpdate;

/**
 * Created by Andrey on 3/6/2018.
 */

public class MeasurementSaveAsyncTask extends AsyncTask<Void, Void, Void> {
    private MeasureFragment measureFragment;
    private Measurement measurement;

    public MeasurementSaveAsyncTask(MeasureFragment measureFragment, Measurement measurement) {
        this.measureFragment = measureFragment;
        this.measurement = measurement;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        measurement.save();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        measureFragment.showSaveSuccessDialog();
    }
}

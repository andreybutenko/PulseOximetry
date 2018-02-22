package edu.washington.cs.sensor.pulseoximetry;

import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Andrey on 1/15/2018.
 */

public class MeasurementAsyncTask extends AsyncTask<Void, Object, String> {
    private int PACKET_SIZE = 16;
    private static final long MEASURE_TIME = 20 * 1000; // milliseconds

    private byte[] sendData = new byte[PACKET_SIZE];
    private byte[] receiveData = new byte[PACKET_SIZE];

    private Tag tag;
    private MeasureFragment measureFragment;

    public MeasurementAsyncTask(MeasureFragment measureFragment, Tag tag) {
        this.measureFragment = measureFragment;
        this.tag = tag;
    }

    @Override
    protected String doInBackground(Void... voids) {
        boolean runLoop = true;
        ArrayList<Byte> irRawData = new ArrayList<>();
        ArrayList<Byte> redRawData = new ArrayList<>();
        IsoDep isoDep = IsoDep.get(tag);

        try {
            isoDep.connect();
            long endTime = System.currentTimeMillis() + MEASURE_TIME;

            while(runLoop && System.currentTimeMillis() <= endTime) {
                if(!isoDep.isConnected()) {
                    runLoop = false;
                    publishProgress("NFC tag lost :(");
                }

                float[] irData = new float[PACKET_SIZE / 4];
                float[] redData  = new float[PACKET_SIZE / 4];

                receiveData = isoDep.transceive(sendData);

                // The device sends 16 bytes at a time in 4 groups of 4 bytes each
                // This loop performs some bitwise operations to get two values from each group:
                // One is the reflection of red light, the other is the reflection of infrared light

                for(int j = 0; j < PACKET_SIZE; j += 4) {
                    // First two bytes: IR data
                    irRawData.add(receiveData[j]);
                    irRawData.add(receiveData[j + 1]);

                    int ir_s = 0;
                    ir_s = (receiveData[j + 1] & 0xFF);
                    ir_s = (ir_s << 8) | (receiveData[j] & 0xFF);

                    irData[j / 4] = (float) ir_s;

                    // Second two bytes: RED data
                    redRawData.add(receiveData[j + 2]);
                    redRawData.add(receiveData[j + 3]);



                    int rd_s = 0;
                    rd_s = (receiveData[j + 3] & 0xFF);
                    rd_s = (rd_s << 8) | (receiveData[j + 2] & 0xFF);


                    redData[j / 4] = (float) rd_s;
                }
                
//                String dataString = MeasurementDetailActivity.getByteArrayAsString(receiveData);
                String dataString = getTickDataAsString(irData, redData);
                Log.d("MEASURE", dataString);
                publishProgress(String.valueOf((endTime - System.currentTimeMillis()) / 1000f) + "s remaining...\n" + dataString, irData, redData, irRawData, redRawData);
            }

            publishProgress("Measurement complete!");
        } catch (IOException e) {
            e.printStackTrace();
            publishProgress("NFC tag lost :(");
        }
        return null;
    }

    private static String getTickDataAsString(float[] irData, float[] redData) {
        String result = "";

        result += "IR Data: ";
        for(float aIrData : irData) {
            result += aIrData + ",";
        }

        result += "\nRD Data: ";
        for(float aRedData : redData) {
            result += aRedData + ",";
        }

        return result;
    }

    @Override
    protected void onProgressUpdate(Object... objects) {
        String statusText = (String) objects[0];
        measureFragment.setStatus(statusText);

        if(objects.length > 1) {
            measureFragment.addIREntry((float[]) objects[1]);
            measureFragment.addRedEntry((float[]) objects[2]);

            measureFragment.setRawData((ArrayList<Byte>) objects[3], (ArrayList<Byte>) objects[4]);
        }
    }
}

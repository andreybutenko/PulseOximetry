package edu.washington.cs.sensor.pulseoximetry.util;

/**
 * Created by Andrey on 1/16/2018.
 * From https://stackoverflow.com/questions/28291582/implementing-a-high-pass-filter-to-an-audio-signal
 */

public class Filter {
    private float frequency;
    private float resonance;
    private PassType passType;
    private int sampleRate;

    public enum PassType {
        Highpass,
        Lowpass,
    }

    public float value;

    private float c, a1, a2, a3, b1, b2;

    // Array of input values; latest are in front
    private float[] inputHistory = new float[2];

    // Array of output values; latest are in front
    private float[] outputHistory = new float[3];

    // Declare filter parameters initially
    //  frequency: cutoff
    //  sampleRate: how many samples were collected per second (Hz)
    //  passType: either low pass or high pass
    //  resonance: some filter characteristic, between 0.1 and sqrt(2)
    public Filter(float frequency, int sampleRate, PassType passType, float resonance) {
        this.frequency = frequency;
        this.sampleRate = sampleRate;
        this.passType = passType;
        this.resonance = resonance;

        switch (passType) {
            case Lowpass:
                c = 1.0f / (float)Math.tan(Math.PI * frequency / sampleRate);
                a1 = 1.0f / (1.0f + resonance * c + c * c);
                a2 = 2f * a1;
                a3 = a1;
                b1 = 2.0f * (1.0f - c * c) * a1;
                b2 = (1.0f - resonance * c + c * c) * a1;
                break;
            case Highpass:
                c = (float)Math.tan(Math.PI * frequency / sampleRate);
                a1 = 1.0f / (1.0f + resonance * c + c * c);
                a2 = -2f * a1;
                a3 = a1;
                b1 = 2.0f * (c * c - 1.0f) * a1;
                b2 = (1.0f - resonance * c + c * c) * a1;
                break;
        }
    }

    // feed a new value as input
    public void Update(float newInput) {
        float newOutput = a1 * newInput + a2 * this.inputHistory[0] + a3 * this.inputHistory[1] - b1 * this.outputHistory[0] - b2 * this.outputHistory[1];

        this.inputHistory[1] = this.inputHistory[0];
        this.inputHistory[0] = newInput;

        this.outputHistory[2] = this.outputHistory[1];
        this.outputHistory[1] = this.outputHistory[0];
        this.outputHistory[0] = newOutput;
    }

    // get output value of latest input
    public float getValue() {
        return this.outputHistory[0];
    }
}

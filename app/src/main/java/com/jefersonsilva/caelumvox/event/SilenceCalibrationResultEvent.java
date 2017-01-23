package com.jefersonsilva.caelumvox.event;

/**
 * Created by jefrsilva on 27/12/16.
 */
public class SilenceCalibrationResultEvent {
    private double avgSilenceAmplitude;

    public SilenceCalibrationResultEvent(double avgSilenceAmplitude) {
        this.avgSilenceAmplitude = avgSilenceAmplitude;
    }

    public double getAverageSilenceAmplitude() {
        return avgSilenceAmplitude;
    }
}

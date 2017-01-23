package com.jefersonsilva.caelumvox.event;

/**
 * Created by jefrsilva on 30/12/16.
 */
public class CalibrationProgressEvent {
    private double progress;

    public CalibrationProgressEvent(double progress) {
        this.progress = progress;
    }

    public double getProgress() {
        return progress;
    }
}

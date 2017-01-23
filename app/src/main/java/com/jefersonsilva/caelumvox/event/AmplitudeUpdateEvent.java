package com.jefersonsilva.caelumvox.event;

/**
 * Created by jefrsilva on 27/12/16.
 */
public class AmplitudeUpdateEvent {
    private double amplitude;

    public AmplitudeUpdateEvent(double amplitude) {
        this.amplitude = amplitude;
    }

    public double getAmplitude() {
        return amplitude;
    }
}

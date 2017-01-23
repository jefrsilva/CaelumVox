package com.jefersonsilva.caelumvox.event;

/**
 * Created by jefrsilva on 27/12/16.
 */
public class VoiceHeardEvent {
    private long instant;
    private double amplitude;
    private float level;

    public VoiceHeardEvent(long instant, double amplitude, float level) {
        this.instant = instant;
        this.amplitude = amplitude;
        this.level = level;
    }

    public long getInstant() {
        return instant;
    }

    public double getAmplitude() {
        return amplitude;
    }

    public float getLevel() {
        return level;
    }
}

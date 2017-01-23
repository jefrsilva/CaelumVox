package com.jefersonsilva.caelumvox.state;

import com.jefersonsilva.caelumvox.event.SilenceAlertEvent;
import com.jefersonsilva.caelumvox.event.VoiceHeardEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by jefrsilva on 27/12/16.
 */

public class HearingState extends State {

    private final long startInstant;
    private double avgSilenceAmplitude;
    private long lastEventInstant = System.currentTimeMillis();

    private static final long MAX_SILENCE_TIME = 2500;
    private long currentScore = MAX_SILENCE_TIME;
    private double difficultyFactor;

    public static final int ALERT_INTERVAL = 5000;
    private long lastAlertInstant = System.currentTimeMillis();

    public HearingState(double avgSilenceAmplitude, double difficultyFactor) {
        this.avgSilenceAmplitude = avgSilenceAmplitude;
        this.difficultyFactor = difficultyFactor;
        this.startInstant = System.currentTimeMillis();
    }

    @Override
    public void update(double amplitude) {
        long currentInstant = System.currentTimeMillis();
        long interval = currentInstant - lastEventInstant;
        lastEventInstant = currentInstant;

        if (amplitude < (avgSilenceAmplitude + difficultyFactor)) {
            currentScore = Math.max(currentScore - interval, 0);
        } else {
            currentScore = Math.min(currentScore + interval, MAX_SILENCE_TIME);
        }

        float level = (float) currentScore / (float) MAX_SILENCE_TIME;
        long instant = currentInstant - startInstant;
        EventBus.getDefault().post(new VoiceHeardEvent(instant, amplitude, level));

        if (currentScore <= 0) {
            if (currentInstant - lastAlertInstant > ALERT_INTERVAL) {
                EventBus.getDefault().post(new SilenceAlertEvent());
                lastAlertInstant = currentInstant;
            }
        }
    }

    @Override
    public String getName() {
        return "HearingState";
    }

    public void setDifficultyFactor(double difficultyFactor) {
        this.difficultyFactor = difficultyFactor;
    }
}

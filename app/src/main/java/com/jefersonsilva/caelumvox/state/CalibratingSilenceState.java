package com.jefersonsilva.caelumvox.state;

import android.util.Log;

import com.jefersonsilva.caelumvox.event.CalibrationProgressEvent;
import com.jefersonsilva.caelumvox.event.SilenceCalibrationResultEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jefrsilva on 27/12/16.
 */

public class CalibratingSilenceState extends State {

    private long duration;
    private long calibrationStartInstant = System.currentTimeMillis();

    private List<Double> samples = new ArrayList<>();

    public CalibratingSilenceState(long duration) {
        this.duration = duration;
    }

    @Override
    public void update(double amplitude) {
        samples.add(amplitude);

        long currentUpdateInstant = System.currentTimeMillis();
        if ((currentUpdateInstant - calibrationStartInstant) > duration) {
            double avgSilenceAmplitude = 0.0;
            for (Double sample : samples) {
                avgSilenceAmplitude += sample;
            }
            avgSilenceAmplitude /= samples.size();

            EventBus.getDefault().post(new SilenceCalibrationResultEvent(avgSilenceAmplitude));
        } else {
            double progress = ((double) (currentUpdateInstant - calibrationStartInstant) / (double) duration) * 100.0;
            progress = Math.min(progress, 100.0);
            EventBus.getDefault().post(new CalibrationProgressEvent(progress));
        }
    }

    @Override
    public String getName() {
        return "CalibratingSilenceState";
    }

}

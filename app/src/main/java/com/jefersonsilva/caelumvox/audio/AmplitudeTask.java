package com.jefersonsilva.caelumvox.audio;

import com.jefersonsilva.caelumvox.event.AmplitudeUpdateEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.TimerTask;

/**
 * Created by jefrsilva on 27/12/16.
 */

public class AmplitudeTask extends TimerTask {
    private SoundMeter soundMeter;

    public AmplitudeTask(SoundMeter soundMeter) {
        this.soundMeter = soundMeter;
    }

    public void run() {
        EventBus.getDefault().post(new AmplitudeUpdateEvent(soundMeter.getAmplitude()));
    }
}

package com.jefersonsilva.caelumvox.activity;

import android.app.Application;

import com.jefersonsilva.caelumvox.event.VoiceHeardEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jefrsilva on 04/01/17.
 */

public class CaelumVoxApplication extends Application {

    private List<VoiceHeardEvent> sessionData;

    @Override
    public void onCreate() {
        super.onCreate();

        sessionData = new ArrayList<>();
    }

    public List<VoiceHeardEvent> getSessionData() {
        return sessionData;
    }
}

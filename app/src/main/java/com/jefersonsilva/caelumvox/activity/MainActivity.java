package com.jefersonsilva.caelumvox.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jefersonsilva.caelumvox.R;
import com.jefersonsilva.caelumvox.audio.AmplitudeTask;
import com.jefersonsilva.caelumvox.audio.SoundMeter;
import com.jefersonsilva.caelumvox.event.AmplitudeUpdateEvent;
import com.jefersonsilva.caelumvox.event.CalibrationProgressEvent;
import com.jefersonsilva.caelumvox.event.SilenceAlertEvent;
import com.jefersonsilva.caelumvox.event.SilenceCalibrationResultEvent;
import com.jefersonsilva.caelumvox.event.VoiceHeardEvent;
import com.jefersonsilva.caelumvox.state.CalibratingSilenceState;
import com.jefersonsilva.caelumvox.state.HearingState;
import com.jefersonsilva.caelumvox.state.State;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Timer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    @BindView(R.id.red_background)
    FrameLayout redBackground;

    @BindView(R.id.green_background)
    FrameLayout greenBackground;

    @BindView(R.id.message)
    TextView message;

    @BindView(R.id.progress)
    ProgressBar workingIndicator;

    @BindView(R.id.circle)
    ImageView voiceIndicator;

    @BindView(R.id.difficulty_factor)
    SeekBar difficultyFactorField;

    private SoundMeter soundMeter;
    private Timer timer;

    private State currentState;
    private double avgSilenceAmplitude;
    private MediaPlayer alertPlayer;
    private List<VoiceHeardEvent> lastSessionData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        alertPlayer = MediaPlayer.create(this, R.raw.deskbell);

        difficultyFactorField.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
                if (currentState.getName().equals("HearingState")) {
                    double difficultyFactor = value;

                    HearingState hearingState = (HearingState) currentState;
                    hearingState.setDifficultyFactor(difficultyFactor);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // do nothing
            }
        });

        CaelumVoxApplication app = (CaelumVoxApplication) getApplication();
        lastSessionData = app.getSessionData();

        resetGUI();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putDouble("avgSilenceAmplitude", avgSilenceAmplitude);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        avgSilenceAmplitude = savedInstanceState.getDouble("avgSilenceAmplitude");
    }

    private void resetGUI() {
        currentState = State.IDLE;
        workingIndicator.setVisibility(View.INVISIBLE);

        voiceIndicator.setScaleType(ImageView.ScaleType.FIT_CENTER);
        voiceIndicator.setScaleX(0.0f);
        voiceIndicator.setScaleY(0.0f);

        redBackground.setVisibility(View.INVISIBLE);
        greenBackground.setVisibility(View.INVISIBLE);

        message.setText(getResources().getString(R.string.app_name));

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (hasPermissions()) {
            startAudio();
        }

        EventBus.getDefault().register(this);
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startAudio();
            }
        }
    }

    private void startAudio() {
        soundMeter = new SoundMeter();
        soundMeter.start();

        timer = new Timer();
        timer.scheduleAtFixedRate(new AmplitudeTask(soundMeter), 0, 32);
    }


    private void stopAudio() {
        if (soundMeter != null) {
            soundMeter.stop();

            timer.cancel();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);

        stopAudio();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(AmplitudeUpdateEvent event) {
        currentState.update(event.getAmplitude());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void silenceCalibrationComplete(SilenceCalibrationResultEvent event) {
        avgSilenceAmplitude = event.getAverageSilenceAmplitude();
        changeState(State.IDLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateCalibrationProgress(CalibrationProgressEvent event) {
        message.setText(getResources().getString(R.string.calibrating) + String.format("%.0f", event.getProgress()) + "%");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void voiceHeard(VoiceHeardEvent event) {
        double difficultyFactor = getDifficultyFactor();
        double amplitude = event.getAmplitude();
        if (amplitude >= (avgSilenceAmplitude + difficultyFactor)) {
            float scale = (float) (amplitude / (Short.MAX_VALUE - (avgSilenceAmplitude + difficultyFactor)));
            scale = Math.min(scale, 1.0f);
            voiceIndicator.setScaleX(0.35f + 0.65f * scale);
            voiceIndicator.setScaleY(0.35f + 0.65f * scale);
        } else {
            amplitude = event.getAmplitude() - avgSilenceAmplitude;
            float scale = (float) (amplitude / (avgSilenceAmplitude + difficultyFactor - avgSilenceAmplitude));
            scale = Math.max(scale, 0.0f);
            voiceIndicator.setScaleX(0.35f * scale);
            voiceIndicator.setScaleY(0.35f * scale);
        }
        greenBackground.setAlpha(event.getLevel());

        lastSessionData.add(event);
    }

    private void changeState(State state) {
        if (currentState == State.IDLE && state != State.IDLE) {
            if (state.getName().equals("HearingState")) {
                lastSessionData.clear();
            }
            workingIndicator.setVisibility(View.VISIBLE);
            currentState = state;
        } else if (currentState != State.IDLE && state == State.IDLE) {
            resetGUI();
        } else if (currentState.equals(state)) {
            resetGUI();
        }

    }

    @OnClick(R.id.start_button)
    public void start() {
        if (hasPermissions()) {
            greenBackground.setVisibility(View.VISIBLE);
            greenBackground.setAlpha(1.0f);
            redBackground.setVisibility(View.VISIBLE);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            double difficultyFactor = getDifficultyFactor();
            changeState(new HearingState(avgSilenceAmplitude, difficultyFactor));
        } else {
            requestPermissions();
        }
    }

    @OnClick(R.id.calibrate_button)
    public void calibrateSilence() {
        if (hasPermissions()) {
            changeState(new CalibratingSilenceState(5000));
        } else {
            requestPermissions();
        }
    }

    @OnClick(R.id.results_button)
    public void viewResults() {
        Intent intent = new Intent(this, ResultsActivity.class);
        startActivity(intent);
    }

    private double getDifficultyFactor() {
        return difficultyFactorField.getProgress();
    }

    @Subscribe
    public void fireSilenceAlert(SilenceAlertEvent event) {
        alertPlayer.start();
    }
}

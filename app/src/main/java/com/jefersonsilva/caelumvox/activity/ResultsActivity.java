package com.jefersonsilva.caelumvox.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.jefersonsilva.caelumvox.R;
import com.jefersonsilva.caelumvox.event.VoiceHeardEvent;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.jefersonsilva.caelumvox.R.id.graph;

public class ResultsActivity extends AppCompatActivity {

    @BindView(graph)
    GraphView graphView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMaxY(1.0);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0.0);
        graphView.getViewport().setMaxX(15000.0);

        graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    return String.format("%.0fs", value / 1000);
                } else {
                    return "";
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        populateGraph();
    }

    private void populateGraph() {
        CaelumVoxApplication app = (CaelumVoxApplication) getApplication();
        List<VoiceHeardEvent> sessionData = app.getSessionData();

        DataPoint[] amplitudePoints = new DataPoint[sessionData.size()];
        DataPoint[] levelPoints = new DataPoint[sessionData.size()];
        for (int index = 0; index < sessionData.size(); index++) {
            VoiceHeardEvent event = sessionData.get(index);
            amplitudePoints[index] = new DataPoint(event.getInstant(), event.getAmplitude() / 32768.0);
            levelPoints[index] = new DataPoint(event.getInstant(), event.getLevel());
        }
        LineGraphSeries<DataPoint> amplitudeSeries = new LineGraphSeries<>(amplitudePoints);
        amplitudeSeries.setColor(ContextCompat.getColor(this, R.color.md_blue_500));

        LineGraphSeries<DataPoint> levelSeries = new LineGraphSeries<>(levelPoints);
        levelSeries.setColor(ContextCompat.getColor(this, R.color.md_red_500));
        levelSeries.setDrawBackground(true);
        levelSeries.setBackgroundColor(ContextCompat.getColor(this, R.color.md_red_100));

        graphView.removeAllSeries();
        graphView.addSeries(levelSeries);
        graphView.addSeries(amplitudeSeries);
    }
}

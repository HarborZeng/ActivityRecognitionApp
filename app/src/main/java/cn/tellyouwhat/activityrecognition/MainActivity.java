package cn.tellyouwhat.activityrecognition;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements SensorEventListener {

    private final int N_SAMPLES = 90;
    private static List<Float> x;
    private static List<Float> y;
    private static List<Float> z;
    private static List<Float> input_signal;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ActivityInference activityInference;

    private TextView downstairsTextView;
    private TextView joggingTextView;
    private TextView sittingTextView;
    private TextView standingTextView;
    private TextView upstairsTextView;
    private TextView walkingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        x = new ArrayList<>();
        y = new ArrayList<>();
        z = new ArrayList<>();
        input_signal = new ArrayList<>();

        downstairsTextView = findViewById(R.id.downstairs_prob);
        joggingTextView = findViewById(R.id.jogging_prob);
        sittingTextView = findViewById(R.id.sitting_prob);
        standingTextView = findViewById(R.id.standing_prob);
        upstairsTextView = findViewById(R.id.upstairs_prob);
        walkingTextView = findViewById(R.id.walking_prob);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager != null ? mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) : null;
        if (mSensorManager != null) {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        }
        activityInference = new ActivityInference(getApplicationContext());
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        activityPrediction();
        x.add(event.values[0]);
        y.add(event.values[1]);
        z.add(event.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void activityPrediction() {
        if (x.size() == N_SAMPLES && y.size() == N_SAMPLES && z.size() == N_SAMPLES) {
            normalize();

            input_signal.addAll(x);
            input_signal.addAll(y);
            input_signal.addAll(z);

            float[] results = activityInference.getActivityProb(toFloatArray(input_signal));

            downstairsTextView.setText(String.valueOf(results[0]));
            joggingTextView.setText(String.valueOf(results[1]));
            sittingTextView.setText(String.valueOf(results[2]));
            standingTextView.setText(String.valueOf(results[3]));
            upstairsTextView.setText(String.valueOf(results[4]));
            walkingTextView.setText(String.valueOf(results[5]));

            float maxProb = Float.MIN_VALUE;
            int maxIndex = -1;
            for (int i = 0; i < results.length; i++) {
                if (results[i] > maxProb) {
                    maxProb = results[i];
                    maxIndex = i;
                }
            }
            TextView probablyActivity = findViewById(R.id.tv_probably_activity);
            probablyActivity.setText("目前活动可能是" + maxIndex);
            x.clear();
            y.clear();
            z.clear();
            input_signal.clear();
        }
    }

    private float[] toFloatArray(List<Float> list) {
        int i = 0;
        float[] array = new float[list.size()];

        for (Float f : list) {
            array[i++] = (f != null ? f : Float.NaN);
        }
        return array;
    }

    private void normalize() {
        float x_m = 0.662868f;
        float y_m = 7.255639f;
        float z_m = 0.411062f;
        float x_s = 6.849058f;
        float y_s = 6.746204f;
        float z_s = 4.754109f;

        for (int i = 0; i < N_SAMPLES; i++) {
            x.set(i, ((x.get(i) - x_m) / x_s));
            y.set(i, ((y.get(i) - y_m) / y_s));
            z.set(i, ((z.get(i) - z_m) / z_s));
        }
    }

}


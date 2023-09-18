package com.zybooks.flipodoro;

import static java.lang.String.valueOf;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.os.CountDownTimer;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private static final String TAG = "MyActivity";
    private static int WORK_TIMER = 7000;
    private static int SHORT_BREAK_TIMER = 3000;
    private static int LONG_BREAK_TIMER = 10000;
    private static int COUNTDOWN_INTERVAL = 1000;

    private TextView mMainText;
    private TextView countDownTimer;

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    float accelerometerData;
    float accelUpdate;

    String state; // There will be 4 states: work, shortBreak, longBreak, idle
    int breakCounter = 0;
    int workCounter = 0;
    int workTimerCounter = 3000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mMainText = findViewById(R.id.MainText);
        countDownTimer = findViewById(R.id.countDownTimer);
        state = "idle";
    }

    @Override
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometerSensor);
    }

    // Check to see if the sensor value changed and display it to the screen. Changes everytime there is a more up to date reading.
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            accelerometerData = sensorEvent.values[1];

            viewAccelData(mMainText);

            // Since the onSensorChanged actually listens for more up to date data instead of changed data, I create a variable to hold the most current unchanged reading
            // and compare against that instead of the most up to date data which changes A TON.

            // If the accelerometerData value is not the same value as the accelUpdate value, set it equal and manage states appropriately.
            if (accelerometerData != accelUpdate) {
                accelUpdate = accelerometerData;

                // Debug
                Log.d(TAG, "State: " + state);
                Log.d(TAG, "breakCounter: " + breakCounter);
                Log.d(TAG, "workCounter: " + workCounter);

                // State manager
                if (accelUpdate < 0) {
                    state = "work";
                    workCounter += 1;
                    viewTimer(countDownTimer);
                } else if (accelUpdate > 1 && breakCounter <= 3 && workCounter >= 1) {
                    state = "shortBreak";
                    breakCounter += 1;
                    viewTimer(countDownTimer);
                } else if (accelUpdate > 1 && workCounter >= 4 && breakCounter > 3) {
                    state = "longBreak";
                    breakCounter = 0;
                    workCounter = 0;
                    viewTimer(countDownTimer);
                } else {
                    state = "idle";
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    // Function used to display sensor data to the screen.
    public void viewAccelData(View view){
        if (accelerometerData != -0.0){
            mMainText.setText(state);
        }

    }

    // Function used to display [5 second] timer on screen.
    public void viewTimer(View view){

        // Create the work timer and display it
        if (state.equals("work")){
            new CountDownTimer(WORK_TIMER, COUNTDOWN_INTERVAL){
                public void onTick(long millisUntilFinished){
                    countDownTimer.setText("seconds remaining: " + millisUntilFinished / 1000);
                    workTimerCounter = 0;
                }

                @Override
                public void onFinish() {
                    state = "idle";
                    countDownTimer.setText("Time for a break!");
                    workTimerCounter = 1;
                }
            }.start();

        // Create the shortBreak timer and display it
        } else if (state.equals("shortBreak")) {
            new CountDownTimer(SHORT_BREAK_TIMER, COUNTDOWN_INTERVAL){
                public void onTick(long millisUntilFinished){
                    countDownTimer.setText("seconds remaining: " + millisUntilFinished / 1000);
                }

                @Override
                public void onFinish() {
                    state = "idle";
                    countDownTimer.setText("Time to go back to work!");

                }
            }.start();

        // Create the longBreak timer and display it
        } else if (state.equals("longBreak")) {
            new CountDownTimer(LONG_BREAK_TIMER, COUNTDOWN_INTERVAL){
                public void onTick(long millisUntilFinished){
                    countDownTimer.setText("seconds remaining: " + millisUntilFinished / 1000);
                }

                @Override
                public void onFinish() {
                    state = "idle";
                    countDownTimer.setText("Time to go back to work!");

                }
            }.start();
        }

    }
}
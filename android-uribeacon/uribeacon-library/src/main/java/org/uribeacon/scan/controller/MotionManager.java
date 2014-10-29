/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uribeacon.scan.controller;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * Class MotionManager - an interface to the Android ACC sensor X Y Z, which 
 * provides a Significant Motion Detection and Timeout interface
 */
public class MotionManager implements SensorEventListener {
  private static final String TAG = MotionManager.class.getSimpleName();
  static final long IDLE_TIME_NANO = TimeUnit.SECONDS.toNanos(10);

  /** 
   * Interface for Motion Listener.
   */
  public interface MotionListener {
    void onMotion();
    void onMotionTimeout();
  }

  private final SensorManager mSensorManager;
  private final Sensor mAccelerometer;
  private MotionListener mMotionListener;

  private long mStopTimestamp = Long.MAX_VALUE;

  public MotionManager(Context context) {
    Log.i(TAG, "MotionManager Created");
    mSensorManager =
        (SensorManager) context.getApplicationContext().getSystemService(Activity.SENSOR_SERVICE);
    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
  }

  public void register(MotionListener ml) {
    if (mMotionListener != null) {
      return;
    }
    mMotionListener = ml;
    mStopTimestamp = Long.MAX_VALUE;
    // Note to change effective sample rate, chose another SensorManger parameter
    mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    Log.i(TAG, "ACC Motion Provider Listener Registered");
  }

  public void unregister() {
    mSensorManager.unregisterListener(this);
    Log.i(TAG, "ACC Motion Provider Listener Unregistered");
    mMotionListener = null;
  }


  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {}

  @Override
  public void onSensorChanged(SensorEvent event) {
    switch (event.sensor.getType()) {
      case (Sensor.TYPE_ACCELEROMETER):
        handleAccelChange(event);
      break;
    }
  }

  /**
   * Handle Accelerometer changes, sending them to the main Activity
   *
   * @param event
   */
  private void handleAccelChange(SensorEvent event) {
    // Because this implements a Retriggerable Monostable, always do the acc test
    float accX = event.values[0];
    float accY = event.values[1];
    float accZ = event.values[2];
    double vector = Math.pow(accX, 2) + Math.pow(accY, 2) + Math.pow(accZ, 2);
    // Measure if acceleration changes by +/- 0.4g where g ~= 9.8 m/s^2
    // Low acc(-0.4) = 9.4 and (9.4)^2 ~= 88.36
    // High acc(+0.4) = 10.2 and (10.2)^2 ~= 104.04
    if ((vector < 88) || (vector > 104)) {
      // Leave Motion flagged for 10 seconds before timeout
      // Only send event if state transition to MOTION
      if (mStopTimestamp == Long.MAX_VALUE) {
        mMotionListener.onMotion();
      }
      mStopTimestamp = event.timestamp + IDLE_TIME_NANO;
    } else if (event.timestamp > mStopTimestamp) {
      // Only send event if state transition to MOTION_TIMEOUT
      mStopTimestamp = Long.MAX_VALUE;
      mMotionListener.onMotionTimeout();
    }
  }
}

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

import org.uribeacon.scan.compat.BluetoothLeScannerCompat;
import org.uribeacon.scan.compat.BluetoothLeScannerCompatProvider;
import org.uribeacon.scan.compat.ScanCallback;
import org.uribeacon.scan.compat.ScanFilter;
import org.uribeacon.scan.compat.ScanSettings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class ScanController processes events and makes FSM transitions to decide on the lowest power
 * states
 */
public class ScanController implements MotionManager.MotionListener {
  private static final String TAG = ScanController.class.getSimpleName();
  private BluetoothLeScannerCompat mLeScanner;

  /**
   * ScanState Constants
   */
  public enum ScanState {
    NO_SCAN, SLOW_SCAN, FAST_SCAN;
  }

  /**
   * Event Constants
   */
  private enum Event {
    SCREEN_ON, SCREEN_OFF, MOTION, MOTION_TIMEOUT;
  }

  /**
   * Modifies the behavior of the controller.
   */
  public enum ScreenOffMode {
    NO_SCAN, SLOW_SCAN
  }

  private Map<ScanState, Map<Event, ScanState>> mScanStateTable;

  private class ControllerScanSettings {
    public ScanSettings mSettings;
    public List<ScanFilter> mFilters;
    public ScanCallback mCallback;
    ControllerScanSettings(ScanSettings settings, List<ScanFilter> filters,
        ScanCallback callback) {
      mSettings = settings;
      mFilters = filters;
      mCallback = callback;
    }

    ScanSettings setScanMode(int mode) {
      if (mode == mSettings.getScanMode()) {
        return mSettings;
      }
      ScanSettings.Builder builder = new ScanSettings.Builder();
      builder.setScanMode(mode)
          .setCallbackType(mSettings.getCallbackType())
          .setScanResultType(mSettings.getScanResultType())
          .setReportDelayMillis(mSettings.getReportDelayMillis());
      mSettings = builder.build();
      return mSettings;
    }
  }

  private Map<ScanSettings, ControllerScanSettings> mDeviceCallbacks;

  // System ScanState
  private final Context mContext;
  private final MotionManager mMotionManager;

  // Scan ScanState Variable (default value is the first state)
  private ScanState mScanState = ScanState.FAST_SCAN;

  // Listen for broadcast events that will effect the scan state
  private final BroadcastReceiver mScanEventListener = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      Log.d(TAG, "DEBUG onReceive");
      String action = intent.getAction();
      if (action.equals(Intent.ACTION_SCREEN_ON)) {
        stateEvent(Event.SCREEN_ON);
      } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
        stateEvent(Event.SCREEN_OFF);
      } else {
        Log.d(TAG, "Undefined Event: Intent Action=" + action);
      }
    }
  };

  public ScanController(Context context, ScreenOffMode scanMode) {
    mContext = context;
    mMotionManager = new MotionManager(mContext);
    init(scanMode);
  }

  public ScanController(Context context, MotionManager motionManager,
      ScreenOffMode scanMode) {
    mContext = context;
    mMotionManager = motionManager;
    init(scanMode);
  }

  public boolean startScan(ScanSettings settings, List<ScanFilter> filters,
        ScanCallback callback) {
    if (mDeviceCallbacks.containsKey(settings)) {
      stopScan(settings);
    }

    ControllerScanSettings scanSettings = new ControllerScanSettings(settings, filters, callback);
    mDeviceCallbacks.put(settings, scanSettings);

    // Use the current state to start the scan.
    if (mScanState != ScanState.NO_SCAN) {
      int mode = getModeFromScanState(mScanState);
      ScanSettings modeSettings = scanSettings.setScanMode(mode);
      return mLeScanner.startScan(filters, modeSettings, callback);
    }

    return true;
  }

  public void stopScan(ScanSettings settings) {
    if (mDeviceCallbacks.containsKey(settings)) {
      mLeScanner.stopScan(mDeviceCallbacks.get(settings).mCallback);
    } else {
      throw new RuntimeException("Asked to stop an unknown settings object callback");
    }
    mDeviceCallbacks.remove(settings);
  }

  public BluetoothLeScannerCompat getScanner() {
    return mLeScanner;
  }

  /**
   * Returns the current state of the controller.
   */
  public ScanState getScanState() {
    return mScanState;
  }

  public int getNumScanners() {
    return mDeviceCallbacks.size();
  }

  private void setState(ScanState toState) {
    if (toState == ScanState.NO_SCAN) {
      mMotionManager.unregister();
    } else if (mScanState == ScanState.NO_SCAN) {
      mMotionManager.register(this);
    }

    updateState(toState);
  }

  private void stateEvent(Event event) {
    if (!mScanStateTable.get(mScanState).containsKey(event)) {
      return;
    }
    Log.d(TAG, "STATE EVENT " + event.toString());
    setState(mScanStateTable.get(mScanState).get(event));
  }

  /**
   * Callback for significant motion event
   */
  @Override
  public void onMotion() {
    stateEvent(Event.MOTION);
  }

  /**
   * Callback for significant motion event timeout
   */
  @Override
  public void onMotionTimeout() {
    stateEvent(Event.MOTION_TIMEOUT);
  }

  private void updateState(ScanState state) {
    if (state == mScanState) {
      return;
    }
    mScanState = state;
    Log.d(TAG, "NEW STATE=" + mScanState.toString());
    for (Map.Entry<ScanSettings, ControllerScanSettings> entry : mDeviceCallbacks.entrySet()) {
      ControllerScanSettings scanSettings = entry.getValue();
      mLeScanner.stopScan(scanSettings.mCallback);
      if (state != ScanState.NO_SCAN) {
        int mode = getModeFromScanState(mScanState);
        ScanSettings settings = scanSettings.setScanMode(mode);
        mLeScanner.startScan(scanSettings.mFilters, settings, scanSettings.mCallback);
      }
    }
  }

  private int getModeFromScanState(ScanState state) {
    return (state == ScanState.SLOW_SCAN) ? ScanSettings.SCAN_MODE_LOW_POWER
        : ScanSettings.SCAN_MODE_LOW_LATENCY;
  }

  /**
   * Register broadcast listener with all Intent filters we need
   */
  public void init(ScreenOffMode scanMode) {
    mScanState = ScanState.NO_SCAN;

    if (scanMode == ScreenOffMode.NO_SCAN) {
      /*
       * NO_SCAN
       *    SCREEN_ON -> FAST_SCAN
       * SLOW_SCAN
       *    MOTION  ->  FAST_SCAN
       *    SCREEN_OFF -> NO_SCAN
       * FAST_SCAN
       *    MOTION_TIMEOUT -> SLOW_SCAN
       *    SCREEN_OFF -> NO_SCAN
       */
      mScanStateTable = new HashMap<ScanState, Map<Event, ScanState>>() {{
        put(ScanState.NO_SCAN, new HashMap<Event, ScanState>() {{
          put(Event.SCREEN_ON, ScanState.FAST_SCAN);
        }});

        put(ScanState.SLOW_SCAN, new HashMap<Event, ScanState>() {{
          put(Event.MOTION, ScanState.FAST_SCAN);
          put(Event.SCREEN_OFF, ScanState.NO_SCAN);
        }});

        put(ScanState.FAST_SCAN, new HashMap<Event, ScanState>() {{
          put(Event.MOTION_TIMEOUT, ScanState.SLOW_SCAN);
          put(Event.SCREEN_OFF, ScanState.NO_SCAN);
        }});
      }};
    } else if (scanMode == ScreenOffMode.SLOW_SCAN) {
      /*
       * NO_SCAN
       *    SCREEN_ON -> FAST_SCAN
       * SLOW_SCAN
       *    MOTION  ->  FAST_SCAN
       *    SCREEN_OFF -> SLOW_SCAN
       * FAST_SCAN
       *    MOTION_TIMEOUT -> SLOW_SCAN
       *    SCREEN_OFF -> SLOW_SCAN
       */
      mScanStateTable = new HashMap<ScanState, Map<Event, ScanState>>() {{
        put(ScanState.NO_SCAN, new HashMap<Event, ScanState>() {{
          put(Event.SCREEN_ON, ScanState.FAST_SCAN);
        }});

        put(ScanState.SLOW_SCAN, new HashMap<Event, ScanState>() {{
          put(Event.MOTION, ScanState.FAST_SCAN);
          put(Event.SCREEN_OFF, ScanState.SLOW_SCAN);
        }});

        put(ScanState.FAST_SCAN, new HashMap<Event, ScanState>() {{
          put(Event.MOTION_TIMEOUT, ScanState.SLOW_SCAN);
          put(Event.SCREEN_OFF, ScanState.SLOW_SCAN);
        }});
      }};
    }

    mDeviceCallbacks = new HashMap<ScanSettings, ControllerScanSettings>();
    mLeScanner = BluetoothLeScannerCompatProvider.getBluetoothLeScannerCompat(mContext);

    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(Intent.ACTION_SCREEN_ON);
    intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
    mContext.registerReceiver(mScanEventListener, intentFilter);

    // Only register the MotionProvider if the screen is already on
    PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);

    if (powerManager.isScreenOn()) {
      setState(ScanState.FAST_SCAN);
    }
  }

  /**
   * Unregister broadcast listener
   */
  public void unregister() {
    mContext.unregisterReceiver(mScanEventListener);
    mMotionManager.unregister();
  }
}

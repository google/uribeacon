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
package org.uribeacon.sample;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import org.uribeacon.beacon.UriBeacon;
import org.uribeacon.scan.compat.ScanRecord;
import org.uribeacon.scan.compat.ScanResult;
import org.uribeacon.scan.util.RangingUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class UriBeaconScanActivity extends ListActivity implements SwipeRefreshLayout.OnRefreshListener {

  private static final String TAG = "UriBeaconScan";
  private static final int REQUEST_ENABLE_BT = 1;

  private static final int DEVICE_LIFETIME_SECONDS = 30;
  private static final Handler mHandler = new Handler();
  private static final long SCAN_TIME_MILLIS = TimeUnit.SECONDS.toMillis(5);
  private final BluetoothAdapter.LeScanCallback mLeScanCallback = new LeScanCallback();
  private DeviceListAdapter mLeDeviceListAdapter;
  private BluetoothAdapter mBluetoothAdapter;
  private boolean mIsScanRunning;
  private boolean mShowAllDevices;
  private SwipeRefreshLayout mSwipeLayout;

  // Run when the SCAN_TIME_MILLIS has elapsed.
  private Runnable mScanTimeout = new Runnable() {
    @Override
    public void run() {
      scanLeDevice(false);
    }
  };

  private boolean leScanMatches(ScanRecord scanRecord) {
    List services = scanRecord.getServiceUuids();
    return (mShowAllDevices || (services != null && services.contains(UriBeacon.URI_SERVICE_UUID)));
  }

  /**
   * Start or stop scanning. Only scan for a limited amount of time defined by SCAN_TIME_MILLIS.
   *
   * @param enable Set to true to enable scanning, false to stop.
   */
  private void scanLeDevice(final boolean enable) {
    if (mIsScanRunning != enable) {
      mIsScanRunning = enable;
      if (enable) {
        // Stops scanning after the predefined scan time has elapsed.
        mHandler.postDelayed(mScanTimeout, SCAN_TIME_MILLIS);
        mLeDeviceListAdapter.clear();
        mSwipeLayout.setRefreshing(true);
        mBluetoothAdapter.startLeScan(mLeScanCallback);
      } else {
        // Cancel the scan timeout callback if still active or else it may fire later.
        mHandler.removeCallbacks(mScanTimeout);
        mSwipeLayout.setRefreshing(false);
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
      }
      // update the refresh/stop refresh menu
      invalidateOptionsMenu();
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.uribeacon_scan_layout);

    getActionBar().setTitle(R.string.title_devices);

    mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
    mSwipeLayout.setOnRefreshListener(this);
    mSwipeLayout.getViewTreeObserver().addOnGlobalLayoutListener(
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            mSwipeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            int height = mSwipeLayout.getHeight();
            mSwipeLayout.setProgressViewOffset(true, height / 3, height / 2);
          }
        });

    // Use this check to determine whether BLE is supported on the device. Then you can
    // selectively disable BLE-related features.
    if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
      Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
      finish();
    }

    // Initializes a Bluetooth adapter. For API version 18 and above,
    // get a reference to BluetoothAdapter through BluetoothManager.
    final BluetoothManager bluetoothManager =
        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
    mBluetoothAdapter = bluetoothManager.getAdapter();

    // Checks if Bluetooth is supported on the device.
    if (mBluetoothAdapter == null) {
      Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
      finish();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.findItem(R.id.menu_stop_refresh).setVisible(mIsScanRunning);
    menu.findItem(R.id.menu_refresh).setVisible(!mIsScanRunning);
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Intent intent;
    switch (item.getItemId()) {
      case R.id.menu_refresh:
        scanLeDevice(true);
        break;
      case R.id.menu_stop_refresh:
        scanLeDevice(false);
        break;
      case R.id.menu_settings:
        intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        break;
      case R.id.menu_config:
        intent = new Intent(this, ConfigListActivity.class);
        startActivity(intent);
        break;
    }
    return true;
  }

  @Override
  protected void onResume() {
    super.onResume();

    getActionBar().setTitle(getString(R.string.title_devices));

    // Initializes list view adapter.

    mLeDeviceListAdapter = new DeviceListAdapter(getLayoutInflater());
    setListAdapter(mLeDeviceListAdapter);

    mShowAllDevices = !getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_key_uribeacon), false);

    // Try to start scanning
    scanLeDevice(true);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // User chose not to enable Bluetooth.
    if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
      finish();
      return;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  protected void onPause() {
    super.onPause();
    // on Pause stop any scans that are running if not in background mode
    scanLeDevice(false);

  }

  private int getTxPowerLevel(ScanResult scanResult) {
    byte[] scanRecord = scanResult.getScanRecord().getBytes();
    UriBeacon uriBeacon =
        UriBeacon.parseFromBytes(scanRecord);

    if (uriBeacon != null) {
      int txPowerLevel = uriBeacon.getTxPowerLevel();
      return uriBeacon.getTxPowerLevel();
    }

    return RangingUtils.DEFAULT_TX_POWER_LEVEL;
  }

  @Override
  public void onRefresh() {
    scanLeDevice(true);
  }

  /**
   * Callback for LE scan results.
   */
  private class LeScanCallback implements BluetoothAdapter.LeScanCallback {
    @Override
    public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanBytes) {
      ScanRecord scanRecord = ScanRecord.parseFromBytes(scanBytes);
      if (leScanMatches(scanRecord)) {
        final ScanResult scanResult = new ScanResult(device, scanRecord, rssi, SystemClock.elapsedRealtimeNanos());
        final int txPower = getTxPowerLevel(scanResult);
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            mLeDeviceListAdapter.add(scanResult, txPower, DEVICE_LIFETIME_SECONDS);
          }
        });
      }
    }
  }
}

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

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.uribeacon.beacon.UriBeacon;
import org.uribeacon.config.ProtocolV1;
import org.uribeacon.config.ProtocolV2;
import org.uribeacon.scan.compat.ScanRecord;
import org.uribeacon.scan.compat.ScanResult;
import org.uribeacon.scan.util.RangingUtils;
import org.uribeacon.widget.ScanResultAdapter;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class UriBeaconScanActivity extends ListActivity implements SwipeRefreshLayout.OnRefreshListener {

  private static final String TAG = "UriBeaconScan";
  private static final int REQUEST_ENABLE_BT = 1;
  // Keep devices in adapter on screen forever.
  private static final int DEVICE_LIFETIME_SECONDS = Integer.MAX_VALUE;
  private static final Handler mHandler = new Handler();
  private final BluetoothAdapter.LeScanCallback mLeScanCallback = new LeScanCallback();
  private DeviceListAdapter mLeDeviceListAdapter;
  private BluetoothAdapter mBluetoothAdapter;
  private boolean mIsScanRunning;
  private SwipeRefreshLayout mSwipeWidget;
  private boolean mIsConfig;
  private Parcelable[] mScanFilterUuids;
  private long mScanTime;
  // Run when the SCAN_TIME_MILLIS has elapsed.
  private Runnable mScanTimeout = new Runnable() {
    @Override
    public void run() {
      scanLeDevice(false);
    }
  };

  private boolean leScanMatches(ScanRecord scanRecord) {
    if (mScanFilterUuids == null) {
      return true;
    }
    List services = scanRecord.getServiceUuids();
    if (services != null) {
      for (Parcelable uuid : mScanFilterUuids) {
        if (services.contains(uuid)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Start or stop scanning. Only scan for a limited amount of time defined by SCAN_TIME_MILLIS.
   *
   * @param enable Set to true to enable scanning, false to stop.
   */
  @SuppressWarnings("deprecation")
  private void scanLeDevice(final boolean enable) {
    if (mIsScanRunning != enable) {
      TextView view = (TextView) findViewById(android.R.id.empty);
      mIsScanRunning = enable;
      setProgressBarIndeterminateVisibility(enable);
      if (enable) {
        view.setText(mIsConfig ? R.string.empty_config_start : R.string.empty_scan_start);
        // Stops scanning after the predefined scan time has elapsed.
        mHandler.postDelayed(mScanTimeout, mScanTime);
        mLeDeviceListAdapter.clear();
        mBluetoothAdapter.startLeScan(mLeScanCallback);
      } else {
        // Cancel the scan timeout callback if still active or else it may fire later.
        mHandler.removeCallbacks(mScanTimeout);
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        view.setText(mIsConfig ? R.string.empty_config_end : R.string.empty_scan_end);
      }
      // update the refresh/stop refresh menu
      invalidateOptionsMenu();
    }
  }


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setContentView(R.layout.uribeacon_scan_layout);

    mSwipeWidget = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_widget);
    mSwipeWidget.setOnRefreshListener(this);

    // Initializes list view adapter.
    mLeDeviceListAdapter = new DeviceListAdapter(getLayoutInflater());
    setListAdapter(mLeDeviceListAdapter);

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
    menu.findItem(R.id.menu_config).setVisible(!mIsConfig);
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
      case R.id.menu_config: {
        // Start another copy of this activity with different filtering options.
        ParcelUuid services[] = {
            ProtocolV1.CONFIG_SERVICE_UUID, ProtocolV2.CONFIG_SERVICE_UUID};
        intent = new Intent(this, UriBeaconScanActivity.class);
        intent.putExtra(BluetoothDevice.EXTRA_UUID, services);
        startActivity(intent);
      }
      break;
    }
    return true;
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    ScanResultAdapter.DeviceSighting sighting = mLeDeviceListAdapter.getItem(position);
    // Only open configuration activity if the selected devices advertises configuration.
    if (mIsConfig) {
      startConfigureActivity(sighting.scanResult);
      // On exit from configuration, return to the main scan screen.
      finish();
    }
  }

  private void startConfigureActivity(ScanResult scanResult) {
    Intent intent = new Intent(this, ConfigActivity.class);
    intent.putExtra(ScanResult.class.getCanonicalName(), scanResult);
    startActivity(intent);
  }

  @Override
  protected void onResume() {
    super.onResume();
    Parcelable configServices[] = getIntent().getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
    mIsConfig = configServices != null;

    // If we are invoked with EXTRA_UUID then we are configuration mode so set title.
    getActionBar().setTitle(mIsConfig ? R.string.title_config : R.string.title_devices);

    // Set the scan filter to be one of three filtering modes: all beacons, UriBeacons,
    // UriBeacons in config mode.
    final String keyUriBeacon = getString(R.string.pref_key_uribeacon);
    final SharedPreferences prefs = getDefaultSharedPreferences(this);
    boolean filterUriBeacon = prefs.getBoolean(keyUriBeacon, false);
    String smoothFactorString = prefs.getString(getString(R.string.pref_key_smooth_factor), "0.5");
    mLeDeviceListAdapter.setSmoothFactor(Double.parseDouble(smoothFactorString));
    String scanTimeString = prefs.getString(getString(R.string.pref_key_scan_time), "10");
    mScanTime = TimeUnit.SECONDS.toMillis(Integer.parseInt(scanTimeString));

    if (mIsConfig) {
      mScanFilterUuids = configServices;
    } else if (filterUriBeacon) {
      mScanFilterUuids = new ParcelUuid[]{
          UriBeacon.URI_SERVICE_UUID, UriBeacon.TEST_SERVICE_UUID};
    } else {
      mScanFilterUuids = null;
    }
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
      return uriBeacon.getTxPowerLevel();
    }

    return RangingUtils.DEFAULT_TX_POWER_LEVEL;
  }

  @Override
  public void onRefresh() {
    // This obscures the contents, so keep in the action bar
    mSwipeWidget.setRefreshing(false);
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

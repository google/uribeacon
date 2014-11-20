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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.uribeacon.beacon.UriBeacon;
import org.uribeacon.scan.compat.BluetoothLeScannerCompat;
import org.uribeacon.scan.compat.BluetoothLeScannerCompatProvider;
import org.uribeacon.scan.compat.ScanCallback;
import org.uribeacon.scan.compat.ScanFilter;
import org.uribeacon.scan.compat.ScanResult;
import org.uribeacon.scan.compat.ScanSettings;
import org.uribeacon.scan.util.AdvertisingData;
import org.uribeacon.scan.util.RangingUtils;
import org.uribeacon.widget.ScanResultAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class UriBeaconScanActivity extends ListActivity {

  private static final String TAG = "UriBeaconScan";
  private static final int REQUEST_ENABLE_BT = 1;
  private static final SimpleDateFormat TIMESTAMP_FORMAT =
      new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS", Locale.US);
  private static final int DEVICE_LIFETIME_SECONDS = 30;
  static enum RunningScan {NONE, SERIAL}

  private LeDeviceListAdapter mLeDeviceListAdapter;
  private BluetoothAdapter mBluetoothAdapter;
  private RunningScan scanRunning;
  private boolean sortByDistance = true;

  private final ScanCallback mCallback = new ScanCallback() {
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
      if (callbackType == ScanSettings.CALLBACK_TYPE_ALL_MATCHES ||
          callbackType == ScanSettings.CALLBACK_TYPE_FIRST_MATCH) {
        handleScanMatch(result);
      } else {
        Log.e(TAG, "Unrecognized callback type received: " + callbackType);
      }
    }

    @Override
    public void onScanFailed(int errorCode) {
      handleScanFailed(errorCode);
    }
  };

  private void handleScanFailed(int err) {
    Log.e(TAG, "onScanFailed: " + err);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getActionBar().setTitle(R.string.title_devices);

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
    menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
    invalidateOptionsMenu();
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.findItem(R.id.menu_stop_serial).setVisible(scanRunning == RunningScan.SERIAL);
    menu.findItem(R.id.menu_scan).setVisible(scanRunning == RunningScan.NONE);
    menu.findItem(R.id.menu_refresh).setVisible(scanRunning != RunningScan.NONE);
    menu.findItem(R.id.menu_sort_dist).setVisible(sortByDistance);
    menu.findItem(R.id.menu_sort_addr).setVisible(!sortByDistance);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Intent intent;
    switch (item.getItemId()) {
      case R.id.menu_scan:
        startScanning();
        break;
      case R.id.menu_stop_serial:
        stopScanning();
        break;
      case R.id.menu_settings:
        intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        break;
      case R.id.menu_config:
        intent = new Intent(this, ConfigListActivity.class);
        startActivity(intent);
        break;
      case R.id.menu_sort_dist:
        sortByDistance = false;
        mLeDeviceListAdapter.notifyDataSetChanged();
        invalidateOptionsMenu();
        break;
      case R.id.menu_sort_addr:
        sortByDistance = true;
        mLeDeviceListAdapter.notifyDataSetChanged();
        invalidateOptionsMenu();
        break;
    }
    return true;
  }

  @Override
  protected void onResume() {
    super.onResume();

    getActionBar().setTitle(getString(R.string.title_devices));

    // Initializes list view adapter.
    mLeDeviceListAdapter = new LeDeviceListAdapter();
    setListAdapter(mLeDeviceListAdapter);

    // Try to start scanning
    startScanning();
  }

  private void startScanning() {
    Log.v(TAG, "startScanning() called");

    // Ensures Bluetooth is enabled on the device. If Bluetooth is not currently enabled,
    // fire an intent to display a dialog asking the user to grant permission to enable it.
    if (!mBluetoothAdapter.isEnabled()) {
      Log.v(TAG, "Bluetooth not enabled; asking user to start it");
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    } else {
      launchSerialScan();
    }
    invalidateOptionsMenu();
  }

  private void stopScanning() {
    Log.v(TAG, "stopScanning() called");

    getLeScanner().stopScan(mCallback);
    scanRunning = RunningScan.NONE;

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        invalidateOptionsMenu();
        mLeDeviceListAdapter.clear();
      }
    });
  }

  private List<ScanFilter> getFilters() {
    List<ScanFilter> filters = new ArrayList<>();
    if (getBooleanPreference(R.string.pref_key_uribeacon)) {
      ScanFilter filter = new ScanFilter.Builder()
          .setServiceData(UriBeacon.URI_SERVICE_UUID,
              new byte[]{},
              new byte[]{})
          .build();
      filters.add(filter);
    }
    return filters;
  }

  /**
   * Considers launching the standard Serial Scan using the given settings.
   */
  private void launchSerialScan() {
    Log.v(TAG, "attempting to start serial scan...");

    // Determine Serial Scan Mode Preference
    int scanMode = ScanSettings.SCAN_MODE_BALANCED;

    ScanSettings settings = new ScanSettings.Builder()
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setScanMode(scanMode)
        .build();
    boolean started = getLeScanner().startScan(getFilters(), settings, mCallback);
    Log.v(TAG, started ? "... scan started" : "... scan NOT started");
    scanRunning = started ? RunningScan.SERIAL : RunningScan.NONE;
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
  stopScanning();

  }

  private int getTxPowerLevel(ScanResult scanResult) {
    byte[] scanRecord = scanResult.getScanRecord().getBytes();
    UriBeacon uriBeacon =
        UriBeacon.parseFromBytes(scanRecord);

    if (uriBeacon != null) {
      int txPowerLevel = uriBeacon.getTxPowerLevel();
      Log.d(TAG, "Tx Power Level: " + txPowerLevel);
      return uriBeacon.getTxPowerLevel();
    }

    return RangingUtils.DEFAULT_TX_POWER_LEVEL;
  }

  private void handleScanMatch(final ScanResult scanResult) {
    Log.v(TAG, "Scan Match: " + scanResult.getDevice() + " rssi:" + scanResult.getRssi() + " sr:"
        + AdvertisingData.toHexString(scanResult.getScanRecord().getBytes()));
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        int txPower = getTxPowerLevel(scanResult);
        mLeDeviceListAdapter.add(scanResult, txPower, DEVICE_LIFETIME_SECONDS);
      }
    });
  }

  private BluetoothLeScannerCompat getLeScanner() {
    return BluetoothLeScannerCompatProvider.getBluetoothLeScannerCompat(this);
  }

  // Adapter for holding devices found through scanning.
  private class LeDeviceListAdapter extends ScanResultAdapter {
    public LeDeviceListAdapter() {
      super(UriBeaconScanActivity.this.getLayoutInflater());
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
      ViewHolder viewHolder;
      // General ListView optimization code.
      if (view == null) {
        view = mInflater.inflate(R.layout.listitem_device, null);
        viewHolder = new ViewHolder();
        viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
        viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
        view.setTag(viewHolder);
      } else {
        viewHolder = (ViewHolder) view.getTag();
      }

      String displayName = null, deviceAddress;
      StringBuilder deviceData = new StringBuilder();
      String nearest = mRegionResolver.getNearestAddress();
      DeviceSighting deviceSighting = getItem(i);
      ScanResult scanResult = deviceSighting.scanResult;
      UriBeacon beacon;
      byte txPowerLevel = 0;
      beacon = UriBeacon.parseFromBytes(scanResult.getScanRecord().getBytes());

      if (beacon != null) {
        displayName = beacon.getUriString();
        txPowerLevel = beacon.getTxPowerLevel();
      }
      if (displayName == null) {
        displayName = scanResult.getDevice().getName();
      }
      if (displayName == null) {
        displayName = "Unknown device";
      }

      deviceAddress = scanResult.getDevice().getAddress();

      long tsMillis = TimeUnit.NANOSECONDS.toMillis(scanResult.getTimestampNanos());
      String distance = String.format(Locale.US, "%.1f",
          mRegionResolver.getDistance(scanResult.getDevice().getAddress()));
      deviceData.append("\nTimestamp: ")
          .append(formatMillis(tsMillis))
          .append(" Tx: ")
          .append(txPowerLevel)
          .append(" RSSI: ")
          .append(scanResult.getRssi())
          .append(" Distance: ")
          .append(distance);

      // The stabilized region computed from the hysteresis.
      int region = mRegionResolver.getRegion(scanResult.getDevice().getAddress());

      if (scanResult.getDevice().getAddress().equals(nearest)) {
        deviceData.append(" Region: NEAREST");
        viewHolder.deviceName.setTextColor(0xff008800);
      } else if (region == RangingUtils.Region.NEAR) {
        deviceData.append(" Region: NEAR");
        viewHolder.deviceName.setTextColor(0xaa004400);
      } else if (region == RangingUtils.Region.MID) {
        deviceData.append(" Region: MID");
        viewHolder.deviceName.setTextColor(0xffaa6600);
      } else if (region == RangingUtils.Region.FAR) {
        deviceData.append(" Region: FAR");
        viewHolder.deviceName.setTextColor(0xff880000);
      } else {
        viewHolder.deviceName.setTextColor(0xff000000);
      }

      viewHolder.deviceName.setText(displayName
          + "  (" + String.format("%.1f", deviceSighting.latestDistance) + "m)");
      viewHolder.deviceAddress.setText(deviceAddress + deviceData.toString());

      return view;
    }
  }

  private static String formatMillis(long millis) {
    return TIMESTAMP_FORMAT.format(new Date(millis));
  }

  static class ViewHolder {
    TextView deviceName;
    TextView deviceAddress;
  }

  private String getStringPreference(int resId, int defaultResId) {
    return getDefaultSharedPreferences(this).getString(getString(resId), getString(defaultResId));
  }

  private boolean getBooleanPreference(int resId) {
    return getDefaultSharedPreferences(this).getBoolean(getString(resId), false);
  }
}

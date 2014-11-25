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
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import org.uribeacon.beacon.ConfigUriBeacon;
import org.uribeacon.config.ProtocolV1;
import org.uribeacon.config.ProtocolV2;
import org.uribeacon.config.UriBeaconConfig;
import org.uribeacon.config.UriBeaconConfig.UriBeaconCallback;
import org.uribeacon.scan.compat.ScanResult;

import java.net.URISyntaxException;
import java.util.List;

public class ConfigActivity extends Activity{
  private Spinner mSchema;
  private EditText mUriValue;
  private EditText mFlagsValue;
  private EditText mTxCal1;
  private EditText mTxCal2;
  private EditText mTxCal3;
  private EditText mTxCal4;
  private Spinner mTxPowerMode;
  private EditText mPeriod;
  private Switch mLock;

  private ProgressDialog mConnectionDialog = null;
  private static final byte DEFAULT_TX_POWER = -63;
  private final String TAG = "ConfigActivity";
  private UriBeaconConfig mUriBeaconConfig;

  private final UriBeaconCallback mUriBeaconCallback = new UriBeaconCallback() {
    @Override
    public void onUriBeaconRead(ConfigUriBeacon configUriBeacon, int status) {
      checkRequest(status);
      updateInputFields(configUriBeacon);
    }

    @Override
    public void onUriBeaconWrite(int status) {
      checkRequest(status);
      if (status == BluetoothGatt.GATT_SUCCESS) {
        mUriBeaconConfig.closeUriBeacon();
        finish();
      }
    }

    private void checkRequest(int status) {
      if (status == BluetoothGatt.GATT_FAILURE) {
        Toast.makeText(ConfigActivity.this, "Failed to update the beacon", Toast.LENGTH_SHORT)
            .show();
        finish();
      }
    }
  };



  public void saveConfigBeacon(MenuItem menu) {
    // block UI
    mUriValue.setEnabled(false);
    String uri = mUriValue.getText().toString();
    try {
      ConfigUriBeacon configUriBeacon = new ConfigUriBeacon.Builder()
          .uriString(uri)
          .txPowerLevel(DEFAULT_TX_POWER)
          .build();
      mUriBeaconConfig.writeUriBeacon(configUriBeacon);
    } catch (URISyntaxException e) {
      Toast.makeText(ConfigActivity.this, "Invalid Uri", Toast.LENGTH_LONG).show();
      mUriBeaconConfig.closeUriBeacon();
      finish();
    }
  }
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // set content view
    setContentView(R.layout.activity_configbeacon);

    initializeTextFields();

    // Get the device to connect to that was passed to us by the scan
    // results Activity.
    Intent intent = getIntent();
    if (intent.getExtras() != null) {
      ScanResult scanResult = intent.getExtras().getParcelable(ScanResult.class.getCanonicalName());
      BluetoothDevice device = scanResult.getDevice();
      if (device != null) {
        // start connection progress
        mConnectionDialog = new ProgressDialog(this);
        mConnectionDialog.setIndeterminate(true);
        mConnectionDialog.setMessage("Connecting to device...");
        mConnectionDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
            finish();
          }
        });
      }
      List<ParcelUuid> uuids = scanResult.getScanRecord().getServiceUuids();
      // Assuming the first uuid is the config uuid
      ParcelUuid uuid = uuids.get(0);
      mUriBeaconConfig = new UriBeaconConfig(this, mUriBeaconCallback, uuid);
      mUriBeaconConfig.connectUriBeacon(device);
    }
  }

  private void initializeTextFields() {
    mSchema = (Spinner) findViewById(R.id.spinner_uriProtocols);
    mUriValue = (EditText) findViewById(R.id.editText_uri);
    mFlagsValue = (EditText) findViewById(R.id.editText_flags);
    mPeriod = (EditText) findViewById(R.id.editText_beaconPeriod);
    mTxPowerMode = (Spinner) findViewById(R.id.spinner_powerMode);
    mTxCal1 = (EditText) findViewById(R.id.editText_txCal1);
    mTxCal2 = (EditText) findViewById(R.id.editText_txCal2);
    mTxCal3 = (EditText) findViewById(R.id.editText_txCal3);
    mTxCal4 = (EditText) findViewById(R.id.editText_txCal4);
    mLock = (Switch) findViewById(R.id.switch_lock);
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.config_menu, menu);
    return true;
  }
  public static void startConfigureActivity(Context context, ScanResult scanResult) {
    Intent intent = new Intent(context, ConfigActivity.class);
    intent.putExtra(ScanResult.class.getCanonicalName(), scanResult);
    context.startActivity(intent);
  }

  private void updateInputFields(ConfigUriBeacon configUriBeacon) {
    if (mUriValue != null && configUriBeacon != null) {
      mUriValue.setText(configUriBeacon.getUriString());
      if (mUriBeaconConfig.getVersion().equals(ProtocolV2.CONFIG_SERVICE_UUID)) {
        mFlagsValue.setText(byteToHexString(configUriBeacon.getFlags()));
        mPeriod.setText(Integer.toString(configUriBeacon.getPeriod()));
        mTxPowerMode.setSelection(configUriBeacon.getPowerMode());

        EditText[] txCalTable = {mTxCal1, mTxCal2, mTxCal3, mTxCal4};
        for (int i = 0; i < txCalTable.length; i++) {
          txCalTable[i].setText(Integer.toString(configUriBeacon.getPowerLevels()[i]));
        }

        mLock.setChecked(configUriBeacon.getLocked());
      }
      else if (mUriBeaconConfig.getVersion().equals(ProtocolV1.CONFIG_SERVICE_UUID)) {
        hideV2Fields();
      }
    }
    else {
      Toast.makeText(this, "Beacon Contains Invalid Data", Toast.LENGTH_SHORT).show();
    }
  }

  private String byteToHexString(byte theByte) {
    return String.format("%02X", theByte);
  }

  private void hideV2Fields(){
    findViewById(R.id.secondRow).setVisibility(View.GONE);
    findViewById(R.id.txCalRow).setVisibility(View.GONE);
    findViewById(R.id.lastRow).setVisibility(View.GONE);
  }
  @Override
  public void onDestroy() {
    // Close and release Bluetooth connection.
    mUriBeaconConfig.closeUriBeacon();
    Toast.makeText(this, "Disconnected from beacon", Toast.LENGTH_SHORT).show();
    super.onDestroy();
  }
}

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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.uribeacon.beacon.UriBeacon;
import org.uribeacon.config.BaseUriBeaconConfig;
import org.uribeacon.config.GattService;

import java.net.URISyntaxException;

public class ConfigActivity extends Activity implements OnClickListener {
  private TextView mBeaconValue = null;
  private TextView mBeaconNewValue = null;
  private Button mBeaconUpdateValue = null;
  private ProgressDialog mConnectionDialog = null;
  private final byte DEFAULT_TX_POWER = -63;
  private int mLength;
  private byte[] mData;
  private final String TAG = "ConfigActivity";
  private UriBeaconConfig mUriBeaconConfig;

  class UriBeaconConfig extends BaseUriBeaconConfig {
    public UriBeaconConfig(Context context) {
      super(context);
    }

    @Override
    public void onUriBeaconRead(byte[] scanRecord, int status) {
      checkRequest(status);
      setBeaconValue(scanRecord);
    }

    @Override
    public void onUriBeaconWrite(int status) {
      checkRequest(status);
      mUriBeaconConfig.closeUriBeacon();
      finish();
    }

    private void checkRequest(int status) {
      if (status == BluetoothGatt.GATT_FAILURE) {
        Toast.makeText(ConfigActivity.this, "Failed to update the beacon", Toast.LENGTH_SHORT).show();
        finish();
      }
    }
  }

  @Override
  public void onClick(View v) {
    // block UI
    mBeaconNewValue.setEnabled(false);
    mBeaconUpdateValue.setEnabled(false);
    String uri = mBeaconNewValue.getText().toString();
    try {
      UriBeacon uriBeacon = new UriBeacon.Builder().uriString(uri)
          .txPowerLevel(DEFAULT_TX_POWER).build();
      byte[] scanRecord = uriBeacon.toByteArray();
      mUriBeaconConfig.writeUriBeacon(scanRecord);
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

    // find the TextView to hold the current value
    mBeaconValue = (TextView) findViewById(R.id.beaconCurValue);
    mBeaconValue.setText(R.string.beacon_not_available);
    mBeaconNewValue = (TextView) findViewById(R.id.beaconNewValue);
    mBeaconUpdateValue = (Button) findViewById(R.id.writeNewValue);
    mBeaconUpdateValue.setOnClickListener(this);

    // Get the device to connect to that was passed to us by the scan
    // results Activity.
    Intent intent = getIntent();
    if (intent.getExtras() != null) {
      BluetoothDevice device = intent.getExtras().getParcelable(BluetoothDevice.EXTRA_DEVICE);
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
      mUriBeaconConfig = new UriBeaconConfig(this);
      mUriBeaconConfig.connectUriBeacon(device);
    }
  }

  public static void startConfigureActivity(Context context, BluetoothDevice deviceToConnect) {
    Intent intent = new Intent(context, ConfigActivity.class);
    intent.putExtra(BluetoothDevice.EXTRA_DEVICE, deviceToConnect);
    context.startActivity(intent);
  }

  private void setBeaconValue(byte[] scanRecord) {
    UriBeacon uriBeacon = UriBeacon.parseFromBytes(scanRecord);
    if (mBeaconValue != null && uriBeacon != null) {
      mBeaconValue.setText(uriBeacon.getUriString());
    }
  }

  @Override
  public void onDestroy() {
    // Close and release Bluetooth connection.
    mUriBeaconConfig.closeUriBeacon();
    Toast.makeText(this, "Disconnected from beacon", Toast.LENGTH_SHORT).show();
    super.onDestroy();
  }
}

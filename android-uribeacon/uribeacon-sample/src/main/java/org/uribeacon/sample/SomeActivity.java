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
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.uribeacon.beacon.UriBeacon;
import org.uribeacon.config.GattService;
import org.uribeacon.config.Util;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.UUID;

public class SomeActivity extends Activity implements OnClickListener {

  private static final UUID BEACON_SERVICE = UUID
      .fromString("b35d7da6-eed4-4d59-8f89-f6573edea967");
  private static final UUID DATA_ONE_CHARACTERISTIC = UUID
      .fromString("b35d7da7-eed4-4d59-8f89-f6573edea967");
  private static final UUID DATA_TWO_CHARACTERISTIC = UUID
      .fromString("b35d7da8-eed4-4d59-8f89-f6573edea967");
  private static final UUID DATA_SIZE_CHARACTERISTIC = UUID
      .fromString("b35d7da9-eed4-4d59-8f89-f6573edea967");
  private TextView mBeaconValue = null;
  private TextView mBeaconNewValue = null;
  private Button mBeaconUpdateValue = null;
  private ProgressDialog mConnectionDialog = null;
  private BluetoothDevice mDeviceToConnect = null;
  private GattService mService = null;
  private int mLength;
  private byte[] mData;
  private byte[] mDataWrite;
  private String TAG = "ConfigActivity";

  private BluetoothGattCallback mRequestCallback = new BluetoothGattCallback() {
    public void checkRequest(int status) {
      if (status == BluetoothGatt.GATT_FAILURE) {
        Toast.makeText(SomeActivity.this, "Failed to update the beacon", Toast.LENGTH_SHORT).show();
        // unblock the UI
        mBeaconNewValue.setEnabled(true);
        mBeaconUpdateValue.setEnabled(true);
        finish();
      }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
      Log.d("ConfigActivity", "onCharacteristicWrite " + Thread.currentThread());
      checkRequest(status);
      byte[] value = characteristic.getValue();
      Log.d(TAG, "onCharacteristicWrite length " + value.length);
      if (!Arrays.equals(value, mDataWrite)) {
        Log.e(TAG, "onCharacteristicWrite -- response is not equal " +
            Util.byteArrayToHexString(value) + " <> " +
            Util.byteArrayToHexString(mDataWrite));
      }
      if (mDataWrite.length <= 20 || DATA_TWO_CHARACTERISTIC.equals(characteristic.getUuid())) {
        mService.disconnect();
        finish();
      }
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, final int newState) {
      checkRequest(status);
      Log.d("ConfigActivity", "onConnectionStateChange " + newState);
      if (newState == BluetoothGatt.STATE_CONNECTED) {
        gatt.discoverServices();
      } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
        Log.d("ConfigActivity", "STATE_DISCONNECTED " + newState);
        // End this activity and go back to scan results view.
        finish();
      }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
      Log.d(TAG, "onServicesDiscovered request queue");
      checkRequest(status);
      mService.setService(BEACON_SERVICE);
      mService.readCharacteristic(DATA_SIZE_CHARACTERISTIC);
      mConnectionDialog.dismiss();
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
      checkRequest(status);
      UUID uuid = characteristic.getUuid();
      if (DATA_ONE_CHARACTERISTIC.equals(uuid)) {
        mData = characteristic.getValue();
        if (mLength <= 20) {
          setBeaconValue();
        }
      } else if (DATA_TWO_CHARACTERISTIC.equals(uuid)) {
        mData = Util.concatenate(mData, characteristic.getValue());
        setBeaconValue();
      } else if (DATA_SIZE_CHARACTERISTIC.equals(uuid)) {
        if (characteristic.getValue() == null) {
          Log.e(TAG, "Null value!");
        } else {
          mLength = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0);
          mService.readCharacteristic(DATA_ONE_CHARACTERISTIC);
          if (mLength > 20) {
            mService.readCharacteristic(DATA_TWO_CHARACTERISTIC);
          }
          Log.d(TAG, "Length is " + mLength);
        }
      }
      Log.d("ConfigActivity", "onCharacteristicRead done");
    }
  };

  /**
   * Callbacks for changes to the state of the connection to BtSmartService.
   */
  private ServiceConnection mServiceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder rawBinder) {
      mService = ((GattService.LocalBinder) rawBinder).getService();
      if (mService != null) {
        // We have a connection to BtSmartService so now we can connect
        // and register the device handler.
        mService.connect(SomeActivity.this, mDeviceToConnect, mRequestCallback);
        mConnectionDialog.show();
      }
    }

    @Override
    public void onServiceDisconnected(ComponentName classname) {
      mService = null;
    }
  };

  @Override
  public void onClick(View v) {
    // block UI
    mBeaconNewValue.setEnabled(false);
    mBeaconUpdateValue.setEnabled(false);
    String uri = mBeaconNewValue.getText().toString();
    try {
      UriBeacon uriBeacon = new UriBeacon.Builder().uriString(uri).build();
      mDataWrite = uriBeacon.toByteArray();
    } catch (URISyntaxException e) {
      Toast.makeText(SomeActivity.this, "Invalid Uri", Toast.LENGTH_LONG).show();
      return;
    }

    if (mDataWrite.length <= 20) {
      // write the value
      mService.writeCharacteristic(DATA_ONE_CHARACTERISTIC,
          mDataWrite);
    } else {
      byte[] buff = Arrays.copyOfRange(mDataWrite, 0, 20);
      Log.d(TAG, "Buffer length is " + buff.length);
      mService.writeCharacteristic(DATA_ONE_CHARACTERISTIC,
          buff);
      mService.writeCharacteristic(DATA_TWO_CHARACTERISTIC,
          Arrays.copyOfRange(mDataWrite, 20, mDataWrite.length));
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
      mDeviceToConnect = intent.getExtras().getParcelable(
          BluetoothDevice.EXTRA_DEVICE);
      if (mDeviceToConnect != null) {
        // Make a connection to BtSmartService to enable us to use its
        // services.
        Intent bindIntent = new Intent(this, GattService.class);
        bindService(bindIntent, mServiceConnection,
            Context.BIND_AUTO_CREATE);

        // start connection progress
        mConnectionDialog = new ProgressDialog(this);
        mConnectionDialog.setIndeterminate(true);
        mConnectionDialog.setMessage("Connecting to device...");
        mConnectionDialog
            .setOnCancelListener(new DialogInterface.OnCancelListener() {

              @Override
              public void onCancel(DialogInterface dialog) {
                finish();
              }
            });
      }
    }
  }

  public static void startConfigureActivity(Context context, BluetoothDevice deviceToConnect) {
    Intent intent = new Intent(context, SomeActivity.class);
    intent.putExtra(BluetoothDevice.EXTRA_DEVICE, deviceToConnect);
    context.startActivity(intent);
  }

  private void setBeaconValue() {
    UriBeacon uriBeacon = UriBeacon.parseFromBytes(mData);
    if (mBeaconValue != null) {
      mBeaconValue.setText(uriBeacon.getUriString());
    }
  }

  @Override
  public void onDestroy() {
    // Close and release Bluetooth connection.
    if (mService != null) {
      mService.close();
    }
    if (mServiceConnection != null) {
      unbindService(mServiceConnection);
    }
    Toast.makeText(this, "Disconnected from beacon", Toast.LENGTH_SHORT).show();
    super.onDestroy();
  }
}

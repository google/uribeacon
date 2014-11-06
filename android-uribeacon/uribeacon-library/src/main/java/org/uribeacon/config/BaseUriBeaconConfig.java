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

package org.uribeacon.config;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.Arrays;
import java.util.UUID;

/**
 * Common base class for configuring the UriBeacon. Provides simple methods and callbacks for
 * reading and writing the entire UriBeacon which may involve multiple GATT requests.
 */
public abstract class BaseUriBeaconConfig {
  private static String TAG = "BaseUriBeaconConfig";
  public static final ParcelUuid CONFIG_SERVICE_UUID = ParcelUuid.fromString("b35d7da6-eed4-4d59-8f89-f6573edea967");
  private static final UUID DATA_ONE = UUID.fromString("b35d7da7-eed4-4d59-8f89-f6573edea967");
  private static final UUID DATA_TWO = UUID.fromString("b35d7da8-eed4-4d59-8f89-f6573edea967");
  private static final UUID DATA_LENGTH = UUID.fromString("b35d7da9-eed4-4d59-8f89-f6573edea967");
  private static final int DATA_LENGTH_MAX = 20;
  private final Context mContext;
  private Integer mDataLength;
  private byte[] mData;
  private byte[] mDataWrite;
  private GattService mService;
  private BluetoothDevice mDevice;

  // Defines callbacks for service binding, passed to bindService()
  private ServiceConnection mServiceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
      GattService.LocalBinder binder = (GattService.LocalBinder) service;
      mService = binder.getService();
      // Initiate the connection and continue through callbacks.
      mService.connect(mContext, mDevice, mBeaconGattCallback_V1);
    }

    @Override
    public void onServiceDisconnected(ComponentName classname) {
      mService = null;
    }
  };

  // Defines callbacks to GATT requests to drive UriBeacon configuration
  private BluetoothGattCallback mBeaconGattCallback_V1 = new BluetoothGattCallback() {
    @Override
    public void onConnectionStateChange(android.bluetooth.BluetoothGatt gatt, int status, int newState) {
      super.onConnectionStateChange(gatt, status, newState);
      if (newState == BluetoothProfile.STATE_CONNECTED) {
        mService.discoverServices();
      }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
      Log.d(TAG, "onServicesDiscovered request queue");
      mService.setService(CONFIG_SERVICE_UUID.getUuid());
      mService.readCharacteristic(DATA_LENGTH);
    }

    @Override
    public void onCharacteristicRead(android.bluetooth.BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
      super.onCharacteristicRead(gatt, characteristic, status);
      // If the operation was successful
      if (status == BluetoothGatt.GATT_SUCCESS) {
        UUID uuid = characteristic.getUuid();
        if (DATA_LENGTH.equals(uuid)) {
          mDataLength = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0);
          mService.readCharacteristic(DATA_ONE);
        } else if (DATA_ONE.equals(uuid)) {
          mData = characteristic.getValue();
          if (mDataLength > DATA_LENGTH_MAX) {
            mService.readCharacteristic(DATA_TWO);
          } else {
            onUriBeaconRead(mData, status);
          }
        } else if (DATA_TWO.equals(uuid)) {
          mData = Util.concatenate(mData, characteristic.getValue());
          onUriBeaconRead(mData, status);
        }
      } else {
        onUriBeaconRead(null, status);
      }
    }

    @Override
    public void onCharacteristicWrite(android.bluetooth.BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
      super.onCharacteristicWrite(gatt, characteristic, status);
      if (mDataWrite.length <= 20 || DATA_TWO.equals(characteristic.getUuid())) {
        onUriBeaconWrite(status);
      }
    }
  };

  public BaseUriBeaconConfig(Context context) {
    mContext = context;
  }

  public abstract void onUriBeaconRead(byte[] scanRecord, int status);

  public abstract void onUriBeaconWrite(int status);

  public void connectUriBeacon(final BluetoothDevice device) {
    // Bind to LocalService
    Intent intent = new Intent(mContext, GattService.class);
    mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    mDevice = device;
  }

  public void writeUriBeacon(byte[] scanRecord) {
    mDataWrite = scanRecord;
    if (mDataWrite.length <= 20) {
      // write the value
      mService.writeCharacteristic(DATA_ONE, mDataWrite);
    } else {
      byte[] buff = Arrays.copyOfRange(mDataWrite, 0, 20);
      Log.d(TAG, "Buffer length is " + buff.length);
      mService.writeCharacteristic(DATA_ONE, buff);
      mService.writeCharacteristic(DATA_TWO,
          Arrays.copyOfRange(mDataWrite, 20, mDataWrite.length));
    }
  }

  public void closeUriBeacon() {
    // Close the GATT connection and unbind from the service
    if (mService != null) {
      mService.close();
      mContext.unbindService(mServiceConnection);
      mService = null;
    }
  }
}

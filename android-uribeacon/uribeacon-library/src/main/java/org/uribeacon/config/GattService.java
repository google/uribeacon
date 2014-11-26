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

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.uribeacon.config.GattRequestQueue.RequestType;

import java.util.UUID;

/**
 * Manages Gatt connections within a Service lifecycle.
 * <p/>
 * Because the Android BLE stack only allows one active request at a time
 * this class uses a request queue. Because callers want to update UI views,
 * this class delivers BluetoothGatt callbacks on the main UI Thread.
 */
public class GattService extends Service {
  private final IBinder mBinder = new LocalBinder();
  private GattRequestQueue mRequestQueue;
  private BluetoothGatt mBluetoothGatt;
  private BluetoothGattService mBluetoothGattService;
  private String TAG = "GattService";

  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }

  /**
   * Initialise the service.
   */
  @Override
  public void onCreate() {
  }

  /**
   * When the service is destroyed, make sure to close the Bluetooth connection.
   */
  @Override
  public void onDestroy() {
    disconnect();
    super.onDestroy();
  }

  private BluetoothGattCharacteristic initializeCharacteristic(UUID uuid) {
    BluetoothGattCharacteristic characteristic = mBluetoothGattService.getCharacteristic(uuid);
    // WriteType is WRITE_TYPE_NO_RESPONSE even though the one that requests a response
    // is called WRITE_TYPE_DEFAULT!
    if (characteristic.getWriteType() != BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) {
      Log.w(TAG, "writeCharacteristic default WriteType is being forced to WRITE_TYPE_DEFAULT");
      characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
    }
    return characteristic;
  }

  public void writeCharacteristic(UUID uuid, byte[] value) {
    BluetoothGattCharacteristic characteristic = initializeCharacteristic(uuid);
    characteristic.setValue(value);
    mRequestQueue.add(mBluetoothGatt, RequestType.WRITE_CHARACTERISTIC, characteristic);
  }

  public void writeCharacteristic(UUID uuid, int value, int formatType, int offset) {
    BluetoothGattCharacteristic characteristic = initializeCharacteristic(uuid);
    characteristic.setValue(value, formatType, offset);
    mRequestQueue.add(mBluetoothGatt, RequestType.WRITE_CHARACTERISTIC, characteristic);
  }

  public void readCharacteristic(UUID uuid) {
    BluetoothGattCharacteristic characteristic = mBluetoothGattService.getCharacteristic(uuid);
    mRequestQueue.add(mBluetoothGatt, RequestType.READ_CHARACTERISTIC, characteristic);
  }

  public void readDescriptor(UUID characteristicUuid, UUID descriptorUuid) {
    BluetoothGattCharacteristic characteristic = mBluetoothGattService.getCharacteristic(characteristicUuid);
    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descriptorUuid);
    mRequestQueue.add(mBluetoothGatt, RequestType.READ_DESCRIPTOR, descriptor);
  }

  /**
   * Connect to a remote Bluetooth Smart device. Callbacks are delivered on the UI Thread.
   */
  public void connect(Context context, BluetoothDevice device, BluetoothGattCallback callback) {
    mRequestQueue = new GattRequestQueue();
    mBluetoothGatt = device.connectGatt(context, false,
        mRequestQueue.newGattCallbackOnUiThread(callback));
  }

  public void discoverServices() {
    mBluetoothGatt.discoverServices();
  }
  /*
  * Once close() is called we are done. If you want to re-connect you will have to call connectGatt()
  * on the BluetoothDevice again; close() will release resources held by BluetoothGatt.
  */
  public void close() {
    if (mBluetoothGatt != null) {
      mBluetoothGatt.close();
      mBluetoothGatt = null;
      mRequestQueue = null;
    }
  }

  /**
   * With disconnect() you can later call connect() and continue with that cycle.
   */
  public void disconnect() {
    if (mBluetoothGatt != null) {
      mBluetoothGatt.disconnect();
    }
  }

  /**
   * Set the service UUID for subsequent GATT calls.
   */
  public boolean setService(UUID uuid) {
    mBluetoothGattService = mBluetoothGatt.getService(uuid);
    if (mBluetoothGattService == null) {
      Log.e(TAG, "setService not found: " + uuid);
    }
    return mBluetoothGattService != null;
  }

  /**
   * Class used for the client Binder. Because we know this service always runs in the same process as its clients, we
   * don't need to deal with IPC.
   */
  public class LocalBinder extends Binder {
    public GattService getService() {
      // Return this instance of GattService so clients can call public methods.
      return GattService.this;
    }
  }
}

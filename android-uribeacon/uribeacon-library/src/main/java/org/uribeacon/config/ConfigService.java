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
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.ParcelUuid;

import java.util.UUID;

public abstract class ConfigService {
  private static String TAG = "UriBeaconConfig";

  public static final ParcelUuid CONFIG_SERVICE_UUID = ParcelUuid.fromString("b35d7da6-eed4-4d59-8f89-f6573edea967");
  private static final UUID DATA_ONE = UUID.fromString("b35d7da7-eed4-4d59-8f89-f6573edea967");
  private static final UUID DATA_TWO = UUID.fromString("b35d7da8-eed4-4d59-8f89-f6573edea967");
  private static final UUID DATA_LENGTH = UUID.fromString("b35d7da9-eed4-4d59-8f89-f6573edea967");
  private static final int DATA_LENGTH_MAX = 20;
  private final Context mContext;

  private BluetoothGatt mBluetoothGatt;
  private Integer mDataLength;
  private byte[] mData;

  private BluetoothGattService mBeaconBluetoothGattService = null;

  private class UriBeaconGattCallback extends BluetoothGattCallback {
    @Override
    public void onConnectionStateChange(android.bluetooth.BluetoothGatt gatt, int status, int newState) {
      super.onConnectionStateChange(gatt, status, newState);
      if (newState == BluetoothProfile.STATE_CONNECTED) {
        ConfigService.this.handleStateConnected(gatt);
      }
    }

    @Override
    public void onCharacteristicRead(android.bluetooth.BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
      super.onCharacteristicRead(gatt, characteristic, status);
      ConfigService.this.handleCharacteristicRead(gatt, characteristic, status);
    }

    @Override
    public void onCharacteristicWrite(android.bluetooth.BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
      super.onCharacteristicWrite(gatt, characteristic, status);
      ConfigService.this.handleCharacteristicWrite(gatt, characteristic, status);
    }
  }

  public ConfigService(Context context) {
    mContext = context;
  }

  public abstract void onUriBeaconRead(byte[] scanRecord, int status);
  public abstract void onUriBeaconWrite(int status);


  public void connectUriBeacon(BluetoothDevice device) {
    mBluetoothGatt = device.connectGatt(mContext, true, new UriBeaconGattCallback());
  }

  public void readUriBeacon() {
    mData = null;
    mDataLength = null;
    readCharacteristic(DATA_LENGTH);
  }

  public void writeUriBeacon(byte[] scanRecord) {
    writeCharacteristicIntValue(DATA_LENGTH, scanRecord.length);
  }

  private void handleStateConnected(BluetoothGatt gatt) {
    // Store a reference to the GATT service
    mBeaconBluetoothGattService = mBluetoothGatt.getService(CONFIG_SERVICE_UUID.getUuid());
    // Start the operation that will read the beacon's advertising packet
    readCharacteristic(DATA_LENGTH);
  }

  /**
   * Called when a characteristic read operation has occurred
   */
  private void handleCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    // If the operation was successful
    if (status == BluetoothGatt.GATT_SUCCESS) {
      UUID uuid = characteristic.getUuid();
      if (DATA_LENGTH.equals(uuid)) {
        mDataLength = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0);
        readCharacteristic(DATA_ONE);
      } else if (DATA_ONE.equals(uuid)) {
        mData = characteristic.getValue();
        if (mDataLength > DATA_LENGTH_MAX) {
          readCharacteristic(DATA_TWO);
        } else {
          onUriBeaconRead(mData, status);
        }
      } else if (DATA_TWO.equals(uuid)) {
        mData = concatenate(mData, characteristic.getValue());
        onUriBeaconRead(mData, status);
      }
    } else {
      onUriBeaconRead(null, status);
    }
  }

  private void handleCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
  }

  private void readCharacteristic(UUID uuid) {
    BluetoothGattCharacteristic characteristic = mBeaconBluetoothGattService.getCharacteristic(uuid);
    mBluetoothGatt.readCharacteristic(characteristic);
  }

  private void writeCharacteristicIntValue(UUID uuid, Integer value) {
    BluetoothGattCharacteristic characteristic = mBeaconBluetoothGattService.getCharacteristic(uuid);
    characteristic.setValue(value, BluetoothGattCharacteristic.FORMAT_SINT8, 0);
    mBluetoothGatt.writeCharacteristic(characteristic);
  }

  private void writeCharacteristic(UUID uuid, byte[] data) {
    BluetoothGattCharacteristic characteristic = mBeaconBluetoothGattService.getCharacteristic(uuid);
    characteristic.setValue(data);
    mBluetoothGatt.writeCharacteristic(characteristic);
  }

  public void closeUriBeacon() {
    if (mBluetoothGatt != null) {
      mBluetoothGatt.close();
      mBluetoothGatt = null;
    }
  }

  /**
   * Concatenates two byte arrays.
   *
   * @param a the first array.
   * @param b the second array.
   * @return the concatenated array.
   */
  private static byte[] concatenate(byte[] a, byte[] b) {
    byte[] result = new byte[a.length + b.length];
    System.arraycopy(a, 0, result, 0, a.length);
    System.arraycopy(b, 0, result, a.length, b.length);
    return result;
  }
}

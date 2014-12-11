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

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.ParcelUuid;

import org.uribeacon.beacon.ConfigUriBeacon;

import java.net.URISyntaxException;

/**
 * Common base class for configuring the ConfigUriBeacon. Provides simple methods and callbacks for
 * reading and writing the entire ConfigUriBeacon which may involve multiple GATT requests.
 */
abstract class BaseProtocol extends BluetoothGattCallback {

  /**
   * Write the ConfigUriBeacon provided to the actual beacon.
   *
   * @param configUriBeacon ConfigUriBeacon containing all the data of the new beacon.
   */
  public abstract void writeUriBeacon(ConfigUriBeacon configUriBeacon) throws URISyntaxException;

  /**
   * @return The version of the Uri Beacon
   */
  public abstract ParcelUuid getVersion();

  public abstract void onConnectionStateChange(BluetoothGatt gatt, int status, int newState);

  public abstract void onServicesDiscovered(BluetoothGatt gatt, int status);

  public abstract void onCharacteristicRead(BluetoothGatt gatt,
      BluetoothGattCharacteristic characteristic, int status);

  public abstract void onCharacteristicWrite(android.bluetooth.BluetoothGatt gatt,
      BluetoothGattCharacteristic characteristic, int status);
}

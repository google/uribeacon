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
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.os.ParcelUuid;
import android.util.Log;

import org.uribeacon.beacon.ConfigUriBeacon;
import org.uribeacon.beacon.ConfigUriBeacon.Builder;
import org.uribeacon.config.UriBeaconConfig.UriBeaconCallback;

import java.net.URISyntaxException;
import java.util.UUID;

public class ProtocolV2 extends BaseProtocol {

  private static final String TAG = ProtocolV2.class.getCanonicalName();

  public static final ParcelUuid CONFIG_SERVICE_UUID = ParcelUuid.fromString("ee0c2080-8786-40ba-ab96-99b91ac981d8");
  private static final UUID LOCK_STATE                     = UUID.fromString("ee0c2081-8786-40ba-ab96-99b91ac981d8");
  private static final UUID DATA                           = UUID.fromString("ee0c2084-8786-40ba-ab96-99b91ac981d8");
  private static final UUID FLAGS                          = UUID.fromString("ee0c2085-8786-40ba-ab96-99b91ac981d8");
  private static final UUID POWER_LEVELS                   = UUID.fromString("ee0c2086-8786-40ba-ab96-99b91ac981d8");
  private static final UUID POWER_MODE                     = UUID.fromString("ee0c2087-8786-40ba-ab96-99b91ac981d8");
  private static final UUID PERIOD                         = UUID.fromString("ee0c2088-8786-40ba-ab96-99b91ac981d8");


  private final GattService mService;
  private final UriBeaconCallback mUriBeaconCallback;
  private UUID mLastUUID;
  private ConfigUriBeacon mConfigUriBeacon;
  private ConfigUriBeacon.Builder mBuilder;

  public ProtocolV2(GattService serviceConnection,
      UriBeaconCallback beaconCallback) {
    mService = serviceConnection;
    mUriBeaconCallback = beaconCallback;
  }

  public ParcelUuid getVersion() {
    return CONFIG_SERVICE_UUID;
  }


  public void writeUriBeacon(ConfigUriBeacon configUriBeacon) throws URISyntaxException{
    // If the characteristic is different write it to the beacon.
    // Before starting the calls define last call
    if (mConfigUriBeacon != null
        && !configUriBeacon.getUriString().equals(mConfigUriBeacon.getUriString())) {
      mLastUUID = DATA;
    }
    // Once the last call has been defined start enqueuing the writes
    if (mConfigUriBeacon != null
        && !configUriBeacon.getUriString().equals(mConfigUriBeacon.getUriString())) {
      mService.writeCharacteristic(DATA, configUriBeacon.getUriBytes());
    }
    //TODO(g-ortuno): Add the rest of V2 characteristics
  }

  @Override
  public void onConnectionStateChange(android.bluetooth.BluetoothGatt gatt, int status,
      int newState) {
    if (newState == BluetoothProfile.STATE_CONNECTED) {
      mService.discoverServices();
    }
  }
  @Override
  public void onServicesDiscovered(BluetoothGatt gatt, int status) {
    Log.d(TAG, "onServicesDiscovered request queue");
    mBuilder = new Builder();
    mService.setService(CONFIG_SERVICE_UUID.getUuid());
    mService.readCharacteristic(LOCK_STATE);
    mService.readCharacteristic(DATA);
    mService.readCharacteristic(FLAGS);
    mService.readCharacteristic(POWER_LEVELS);
    mService.readCharacteristic(POWER_MODE);
    mService.readCharacteristic(PERIOD);
  }

  @Override
  public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
      int status) {
    // If the operation was successful
    if (status == BluetoothGatt.GATT_SUCCESS) {
      UUID uuid = characteristic.getUuid();
      try {
        if (LOCK_STATE.equals(uuid)) {
          //0 unlocked; 1 locked
          mBuilder.locked(characteristic.getIntValue
              (BluetoothGattCharacteristic.FORMAT_UINT8, 0) != 1);
        } else if (DATA.equals(uuid)) {
          mBuilder.uriString(characteristic.getValue());
        } else if (FLAGS.equals(uuid)) {
          mBuilder.flags(characteristic.getValue()[0]);
        } else if (POWER_LEVELS.equals(uuid)) {
          int[] tempValues = new int[4];
          for (int i = 0; i < tempValues.length; i++) {
            tempValues[i] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, i);
          }
          mBuilder.powerLevels(tempValues);
        } else if (POWER_MODE.equals(uuid)) {
          mBuilder
              .powerMode(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
        } else if (PERIOD.equals(uuid)) {
          mBuilder.period(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0));
          mConfigUriBeacon = mBuilder.build();
          mUriBeaconCallback.onUriBeaconRead(mConfigUriBeacon, status);
        }
      } catch (URISyntaxException | IllegalArgumentException e) {
        mUriBeaconCallback.onUriBeaconRead(null, status);
      }
    } else {
      mUriBeaconCallback.onUriBeaconRead(null, status);
    }
  }

  @Override
  public void onCharacteristicWrite(android.bluetooth.BluetoothGatt gatt,
      BluetoothGattCharacteristic characteristic, int status) {
    // If the operation was successful
    if (status == BluetoothGatt.GATT_SUCCESS) {
      if (mLastUUID.equals(characteristic.getUuid())) {
        mUriBeaconCallback.onUriBeaconWrite(status);
      }
    } else {
      mUriBeaconCallback.onUriBeaconWrite(status);
    }
  }
}

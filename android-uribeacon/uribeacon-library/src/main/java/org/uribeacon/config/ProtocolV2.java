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
import org.uribeacon.config.UriBeaconConfig.UriBeaconCallback;

import java.net.URISyntaxException;
import java.util.UUID;

public class ProtocolV2 extends BaseProtocol {

  private static final String TAG = ProtocolV2.class.getCanonicalName();

  public static final ParcelUuid CONFIG_SERVICE_UUID = ParcelUuid
      .fromString("ee0c2087-8786-40ba-ab96-99b91ac981d8");
  //TODO(g-ortuno): Add the rest of the V2 characteristics
  private static final UUID DATA = UUID.fromString("ee0c208a-8786-40ba-ab96-99b91ac981d8");

  private final GattService mService;
  private final UriBeaconCallback mUriBeaconCallback;
  private UUID mLastUUID;
  private ConfigUriBeacon mConfigUriBeacon;

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
    mService.setService(CONFIG_SERVICE_UUID.getUuid());
    mService.readCharacteristic(DATA);
  }

  @Override
  public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
      int status) {
    // If the operation was successful
    if (status == BluetoothGatt.GATT_SUCCESS) {
      UUID uuid = characteristic.getUuid();
      try {
        //TODO(g-ortuno): Add the rest of V2 characteristics
        if (DATA.equals(uuid)) {
          mConfigUriBeacon = new ConfigUriBeacon.Builder()
              .uriString(characteristic.getValue())
              .build();
        }
      } catch (URISyntaxException e) {
        mUriBeaconCallback.onUriBeaconRead(null, status);
      }
      mUriBeaconCallback.onUriBeaconRead(mConfigUriBeacon, status);
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

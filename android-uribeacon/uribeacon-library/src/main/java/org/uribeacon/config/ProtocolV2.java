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
import java.util.Arrays;
import java.util.UUID;

public class ProtocolV2 extends BaseProtocol {

  private static final String TAG = ProtocolV2.class.getCanonicalName();

  public static final ParcelUuid CONFIG_SERVICE_UUID = ParcelUuid.fromString("ee0c2080-8786-40ba-ab96-99b91ac981d8");
  private static final UUID LOCK_STATE                     = UUID.fromString("ee0c2081-8786-40ba-ab96-99b91ac981d8");
  private static final UUID LOCK                           = UUID.fromString("ee0c2082-8786-40ba-ab96-99b91ac981d8");
  private static final UUID UNLOCK                         = UUID.fromString("ee0c2083-8786-40ba-ab96-99b91ac981d8");
  private static final UUID DATA                           = UUID.fromString("ee0c2084-8786-40ba-ab96-99b91ac981d8");
  private static final UUID FLAGS                          = UUID.fromString("ee0c2085-8786-40ba-ab96-99b91ac981d8");
  private static final UUID POWER_LEVELS                   = UUID.fromString("ee0c2086-8786-40ba-ab96-99b91ac981d8");
  private static final UUID POWER_MODE                     = UUID.fromString("ee0c2087-8786-40ba-ab96-99b91ac981d8");
  private static final UUID PERIOD                         = UUID.fromString("ee0c2088-8786-40ba-ab96-99b91ac981d8");
  private static final UUID RESET                          = UUID.fromString("ee0c2089-8786-40ba-ab96-99b91ac981d8");
  private static final int LOCK_FORMAT = BluetoothGattCharacteristic.FORMAT_UINT8;
  private static final int PERIOD_FORMAT = BluetoothGattCharacteristic.FORMAT_UINT16;

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
    //TODO: If beacon has invalid data initialize a beacon with RESET values
    if (mConfigUriBeacon.getLockState()) {
      if (configUriBeacon.getKey() == null) {
        mUriBeaconCallback.onUriBeaconWrite(ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION);
      }
      mService.writeCharacteristic(UNLOCK, configUriBeacon.getKey());
    }
    if (configUriBeacon.getReset()) {
      mLastUUID = RESET;
      mService.writeCharacteristic(RESET, new byte[]{1});
    } else {
      // Define last call
      if (configUriBeacon.getUriString() != null
          && !configUriBeacon.getUriString().equals(mConfigUriBeacon.getUriString())) {
        mLastUUID = DATA;
      }
      if (configUriBeacon.getFlags() != mConfigUriBeacon.getFlags()) {
        mLastUUID = FLAGS;
      }
      if (configUriBeacon.getAdvertisedTxPowerLevels() != null
          && !Arrays.equals(configUriBeacon.getAdvertisedTxPowerLevels(),
          mConfigUriBeacon.getAdvertisedTxPowerLevels())) {
        mLastUUID = POWER_LEVELS;
      }
      if (configUriBeacon.getTxPowerMode() != ConfigUriBeacon.POWER_MODE_NONE
          && configUriBeacon.getTxPowerMode() != mConfigUriBeacon.getTxPowerMode()) {
        mLastUUID = POWER_MODE;
      }
      if (configUriBeacon.getBeaconPeriod() != ConfigUriBeacon.PERIOD_NONE
          && configUriBeacon.getBeaconPeriod() != mConfigUriBeacon.getBeaconPeriod()) {
        mLastUUID = PERIOD;
      }
      if (configUriBeacon.getLockState()) {
        if (configUriBeacon.getKey() == null) {
          mUriBeaconCallback.onUriBeaconWrite(ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION);
        }
        mLastUUID = LOCK;
      }
      // If there are no changes or the only change is that the beacon was unlocked, return.
      if (mLastUUID == null) {
        mUriBeaconCallback.onUriBeaconWrite(BluetoothGatt.GATT_SUCCESS);
      }
      // Start enqueing writes
      if (configUriBeacon.getUriString() != null
          && !configUriBeacon.getUriString().equals(mConfigUriBeacon.getUriString())) {
        mService.writeCharacteristic(DATA, configUriBeacon.getUriBytes());
      }
      if (configUriBeacon.getFlags() != mConfigUriBeacon.getFlags()) {
        mService.writeCharacteristic(FLAGS, new byte[]{configUriBeacon.getFlags()});
      }
      if (configUriBeacon.getAdvertisedTxPowerLevels() != null
          && !Arrays.equals(configUriBeacon.getAdvertisedTxPowerLevels(),
          mConfigUriBeacon.getAdvertisedTxPowerLevels())) {
        mService.writeCharacteristic(POWER_LEVELS, configUriBeacon.getAdvertisedTxPowerLevels());
      }
      if (configUriBeacon.getTxPowerMode() != ConfigUriBeacon.POWER_MODE_NONE
          && configUriBeacon.getTxPowerMode() != mConfigUriBeacon.getTxPowerMode()) {
        mService.writeCharacteristic(POWER_MODE, new byte[]{configUriBeacon.getTxPowerMode()});
      }
      if (configUriBeacon.getBeaconPeriod() != ConfigUriBeacon.PERIOD_NONE
          && configUriBeacon.getBeaconPeriod() != mConfigUriBeacon.getBeaconPeriod()) {
        mService.writeCharacteristic(PERIOD, configUriBeacon.getBeaconPeriod(), PERIOD_FORMAT, 0);
      }
      if (configUriBeacon.getLockState()) {
        mService.writeCharacteristic(LOCK, configUriBeacon.getKey());
      }
    }
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
          mBuilder.lockState(characteristic.getIntValue(LOCK_FORMAT, 0) != 0);
        } else if (DATA.equals(uuid)) {
          mBuilder.uriString(characteristic.getValue());
        } else if (FLAGS.equals(uuid)) {
          mBuilder.flags(characteristic.getValue()[0]);
        } else if (POWER_LEVELS.equals(uuid)) {
          mBuilder.advertisedTxPowerLevels(characteristic.getValue());
        } else if (POWER_MODE.equals(uuid)) {
          mBuilder.txPowerMode(characteristic.getValue()[0]);
        } else if (PERIOD.equals(uuid)) {
          mBuilder.beaconPeriod(characteristic.getIntValue(PERIOD_FORMAT, 0));
          mConfigUriBeacon = mBuilder.build();
          mUriBeaconCallback.onUriBeaconRead(mConfigUriBeacon, status);
        }
      } catch (URISyntaxException | IllegalArgumentException e) {
        e.printStackTrace();
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

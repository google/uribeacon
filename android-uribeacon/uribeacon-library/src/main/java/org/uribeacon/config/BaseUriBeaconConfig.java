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

import org.uribeacon.beacon.UriBeacon;

/**
 * Common base class for configuring the UriBeacon. Provides simple methods and callbacks for
 * reading and writing the entire UriBeacon which may involve multiple GATT requests.
 */
public abstract class BaseUriBeaconConfig extends BluetoothGattCallback {

  private static final String TAG = "BaseUriBeaconConfig";

  private final Context mContext;
  private GattService mService;
  private BluetoothDevice mDevice;
  private BaseUriBeaconCallback mBluetoothCallback;
  // Defines callbacks for service binding, passed to bindService()
  private final ServiceConnection mServiceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
      GattService.LocalBinder binder = (GattService.LocalBinder) service;
      mService = binder.getService();
      // Initiate the connection and continue through callbacks.
      mService.connect(mContext, mDevice, BaseUriBeaconConfig.this);
    }

    @Override
    public void onServiceDisconnected(ComponentName classname) {
      mService = null;
    }
  };

  public BaseUriBeaconConfig(Context context) {
    mContext = context;
  }

  /**
   * Called when the data has been read from the beacon.
   *
   * @param beacon UriBeacon with all the fields read
   * @param status Status code from the gatt request
   */
  public abstract void onUriBeaconRead(UriBeacon beacon, int status);

  /**
   * Called when the data has been written to the beacon.
   *
   * @param status Status code from the gatt request.
   */
  public abstract void onUriBeaconWrite(int status);

  /**
   * Initiate the Gatt connection to the beacon
   *
   * @param device The device that you're going to connect to.
   */
  public void connectUriBeacon(final BluetoothDevice device) {
    // Bind to LocalService
    Intent intent = new Intent(mContext, GattService.class);
    mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    mDevice = device;
  }

  /**
   * Write the UriBeacon provided to the actual beacon.
   *
   * @param uriBeacon UriBeacon containing all the data of the new beacon.
   */
  public void writeUriBeacon(UriBeacon uriBeacon) {
    // Different functions depending on beacon version
    mBluetoothCallback.startWriting(uriBeacon);
  }

  /**
   * Close connection to the beacon.
   */
  public void closeUriBeacon() {
    // Close the GATT connection and unbind from the service
    if (mService != null) {
      mService.close();
      mContext.unbindService(mServiceConnection);
      mService = null;
    }
  }

  /**
   * @return The version of the Uri Beacon
   */
  public ParcelUuid getVersion() {
    return mBluetoothCallback.getVersion();
  }

  /////////////////////////////////////////////
  ///// Bluetooth Gatt Callback functions /////
  /////////////////////////////////////////////

  @Override
  public void onConnectionStateChange(android.bluetooth.BluetoothGatt gatt, int status,
      int newState) {
    super.onConnectionStateChange(gatt, status, newState);
    if (newState == BluetoothProfile.STATE_CONNECTED) {
      mService.discoverServices();
    }
  }

  @Override
  public void onServicesDiscovered(BluetoothGatt gatt, int status) {
    Log.d(TAG, "onServicesDiscovered request queue");
    if (mService.setService(UriBeaconCallbackV2.CONFIG_SERVICE_UUID.getUuid())) {
      mBluetoothCallback = new UriBeaconCallbackV2(mService, this);
      mBluetoothCallback.startReading();
    } else if (mService.setService(UriBeaconCallbackV1.CONFIG_SERVICE_UUID.getUuid())) {
      mBluetoothCallback = new UriBeaconCallbackV1(mService, this);
      mBluetoothCallback.startReading();
    }
  }

  @Override
  public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
      int status) {
    super.onCharacteristicRead(gatt, characteristic, status);
    // If the operation was successful
    if (status == BluetoothGatt.GATT_SUCCESS) {
      mBluetoothCallback.onCharacteristicRead(gatt, characteristic, status);
    } else {
      onUriBeaconRead(null, status);
    }
  }

  @Override
  public void onCharacteristicWrite(android.bluetooth.BluetoothGatt gatt,
      BluetoothGattCharacteristic characteristic, int status) {
    super.onCharacteristicWrite(gatt, characteristic, status);
    // If the operation was successful
    if (status == BluetoothGatt.GATT_SUCCESS) {
      mBluetoothCallback.onCharacteristicWrite(gatt, characteristic, status);
    } else {
      onUriBeaconWrite(status);
    }
  }
}

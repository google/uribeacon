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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.ParcelUuid;

import org.uribeacon.beacon.ConfigUriBeacon;

import java.net.URISyntaxException;
import java.util.UUID;

public class UriBeaconConfig {

  private static final String TAG = UriBeaconConfig.class.getCanonicalName();
  private Context mContext;
  private BluetoothDevice mDevice;
  private UriBeaconCallback mUriBeaconCallback;
  private BaseProtocol mBaseProtocol;
  private GattService mService;
  private UUID mUuid;
  private ServiceConnection mServiceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
      GattService.LocalBinder binder = (GattService.LocalBinder) service;
      mService = binder.getService();
      if (ProtocolV2.CONFIG_SERVICE_UUID.getUuid().equals(mUuid)) {
        mBaseProtocol = new ProtocolV2(mService, mUriBeaconCallback);
      } else if (ProtocolV1.CONFIG_SERVICE_UUID.getUuid().equals(mUuid)) {
        mBaseProtocol = new ProtocolV1(mService, mUriBeaconCallback);
      }
      mService.connect(mContext, mDevice, mBaseProtocol);
    }

    @Override
    public void onServiceDisconnected(ComponentName classname) {
      mService = null;
    }
  };

  public UriBeaconConfig(Context context, UriBeaconCallback uriBeaconCallback,
      ParcelUuid uuid) {
    mContext = context;
    mUriBeaconCallback = uriBeaconCallback;
    mUuid = uuid.getUuid();
  }

  public ParcelUuid getVersion() {
    return mBaseProtocol.getVersion();
  }

  /**
   * Initiate the Gatt connection to the beacon
   *
   * @param device The device that you're going to connect to.
   */
  public void connectUriBeacon(final BluetoothDevice device) {
    // Bind to LocalService
    Intent intent = new Intent(mContext, GattService.class);
    mDevice = device;
    mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
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

  public void writeUriBeacon(ConfigUriBeacon configUriBeacon) throws URISyntaxException {
    mBaseProtocol.writeUriBeacon(configUriBeacon);
  }


  public interface UriBeaconCallback {

    /**
     * Called when the data has been read from the beacon.
     *
     * @param configUriBeacon ConfigUriBeacon with all the fields read
     * @param status Status code from the gatt request
     */
    public void onUriBeaconRead(ConfigUriBeacon configUriBeacon, int status);

    /**
     * Called when the data has been written to the beacon.
     *
     * @param status Status code from the gatt request.
     */
    public void onUriBeaconWrite(int status);
  }
}
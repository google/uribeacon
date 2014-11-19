package org.uribeacon.config;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.ParcelUuid;

import org.uribeacon.beacon.UriBeacon;

public abstract class BaseUriBeaconCallback {

  public abstract ParcelUuid getVersion();

  public abstract void startReading();

  public abstract void onCharacteristicRead(BluetoothGatt gatt,
      BluetoothGattCharacteristic characteristic, int status);

  public abstract void startWriting(UriBeacon uriBeacon);

  public abstract void onCharacteristicWrite(BluetoothGatt gatt,
      BluetoothGattCharacteristic characteristic, int status);
}

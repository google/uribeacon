package org.uribeacon.config;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.ParcelUuid;
import android.util.Log;

import org.uribeacon.beacon.UriBeacon;

import java.util.Arrays;
import java.util.UUID;

public class UriBeaconCallbackV1 extends BaseUriBeaconCallback {

  public static final ParcelUuid CONFIG_SERVICE_UUID = ParcelUuid
      .fromString("b35d7da6-eed4-4d59-8f89-f6573edea967");
  private static final String TAG = "callback v1";
  private static final UUID DATA_ONE = UUID.fromString("b35d7da7-eed4-4d59-8f89-f6573edea967");
  private static final UUID DATA_TWO = UUID.fromString("b35d7da8-eed4-4d59-8f89-f6573edea967");
  private static final UUID DATA_LENGTH = UUID.fromString("b35d7da9-eed4-4d59-8f89-f6573edea967");
  private static final int DATA_LENGTH_MAX = 20;
  private final GattService mService;
  private final BaseUriBeaconConfig mBeaconConfig;
  private Integer mDataLength;
  private byte[] mData;
  private UUID mLastUUID;

  public UriBeaconCallbackV1(GattService serviceConnection, BaseUriBeaconConfig beaconConfig) {
    mService = serviceConnection;
    mBeaconConfig = beaconConfig;
  }

  @Override
  public ParcelUuid getVersion() {
    return CONFIG_SERVICE_UUID;
  }

  @Override
  public void startReading() {
    mService.readCharacteristic(DATA_LENGTH);
  }


  public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
      int status) {
    UUID uuid = characteristic.getUuid();
    if (DATA_LENGTH.equals(uuid)) {
      mDataLength = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0);
      mService.readCharacteristic(DATA_ONE);
    } else if (DATA_ONE.equals(uuid)) {
      mData = characteristic.getValue();
      if (mDataLength > DATA_LENGTH_MAX) {
        mService.readCharacteristic(DATA_TWO);
      } else {
        mBeaconConfig.onUriBeaconRead(UriBeacon.parseFromBytes(mData), status);
      }
    } else if (DATA_TWO.equals(uuid)) {
      mData = Util.concatenate(mData, characteristic.getValue());
      mBeaconConfig.onUriBeaconRead(UriBeacon.parseFromBytes(mData), status);
    }
  }

  @Override
  public void startWriting(UriBeacon uriBeacon) {

    byte[] dataWrite = uriBeacon.toByteArray();
    if (dataWrite.length <= 20) {
      mLastUUID = DATA_ONE;
      // write the value
      mService.writeCharacteristic(DATA_ONE, dataWrite);
    } else {
      mLastUUID = DATA_TWO;
      byte[] buff = Arrays.copyOfRange(dataWrite, 0, 20);
      Log.d(TAG, "Buffer length is " + buff.length);
      mService.writeCharacteristic(DATA_ONE, buff);
      mService.writeCharacteristic(DATA_TWO,
          Arrays.copyOfRange(dataWrite, 20, dataWrite.length));
    }
  }

  @Override
  public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
      int status) {
    if (mLastUUID.equals(characteristic.getUuid())) {
      mBeaconConfig.onUriBeaconWrite(status);
    }
  }


}


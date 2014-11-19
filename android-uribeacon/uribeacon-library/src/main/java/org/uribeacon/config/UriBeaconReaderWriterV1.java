package org.uribeacon.config;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.ParcelUuid;
import android.util.Log;

import org.uribeacon.beacon.UriBeacon;
import org.uribeacon.config.UriBeaconConfigDispatcher.UriBeaconCallback;

import java.util.Arrays;
import java.util.UUID;

public class UriBeaconReaderWriterV1 implements BaseUriBeaconReaderWriter {

  public static final ParcelUuid CONFIG_SERVICE_UUID = ParcelUuid
      .fromString("b35d7da6-eed4-4d59-8f89-f6573edea967");
  private static final String TAG = "callback v1";
  private static final UUID DATA_ONE = UUID.fromString("b35d7da7-eed4-4d59-8f89-f6573edea967");
  private static final UUID DATA_TWO = UUID.fromString("b35d7da8-eed4-4d59-8f89-f6573edea967");
  private static final UUID DATA_LENGTH = UUID.fromString("b35d7da9-eed4-4d59-8f89-f6573edea967");
  private static final int DATA_LENGTH_MAX = 20;
  private final GattService mService;
  private final UriBeaconCallback mUriBeaconCallback;
  private Integer mDataLength;
  private byte[] mData;
  private byte[] mDataWrite;

  public UriBeaconReaderWriterV1(GattService serviceConnection,
      UriBeaconCallback uriBeaconCallback) {
    mService = serviceConnection;
    mUriBeaconCallback = uriBeaconCallback;
  }

  public ParcelUuid getVersion() {
    return CONFIG_SERVICE_UUID;
  }

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
        mUriBeaconCallback.onUriBeaconRead(UriBeacon.parseFromBytes(mData), status);
      }
    } else if (DATA_TWO.equals(uuid)) {
      mData = Util.concatenate(mData, characteristic.getValue());
      mUriBeaconCallback.onUriBeaconRead(UriBeacon.parseFromBytes(mData), status);
    }
  }

  public void startWriting(UriBeacon uriBeacon) {

    mDataWrite = uriBeacon.toByteArray();
    if (mDataWrite.length <= 20) {
      // write the value
      mService.writeCharacteristic(DATA_ONE, mDataWrite);
    } else {
      byte[] buff = Arrays.copyOfRange(mDataWrite, 0, 20);
      Log.d(TAG, "Buffer length is " + buff.length);
      mService.writeCharacteristic(DATA_ONE, buff);
      mService.writeCharacteristic(DATA_TWO,
          Arrays.copyOfRange(mDataWrite, 20, mDataWrite.length));
    }
  }

  @Override
  public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
      int status) {
    if (mDataWrite.length <= 20 || DATA_TWO.equals(characteristic.getUuid())) {
      mUriBeaconCallback.onUriBeaconWrite(status);
    }
  }


}


package org.uribeacon.config;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.os.ParcelUuid;
import android.util.Log;

import org.uribeacon.beacon.ConfigUriBeacon;
import org.uribeacon.config.UriBeaconConfig.UriBeaconCallback;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.UUID;

public class ProtocolV1 extends BaseProtocol {

  private static final String TAG = ProtocolV1.class.getCanonicalName();
  public static final ParcelUuid CONFIG_SERVICE_UUID = ParcelUuid
      .fromString("b35d7da6-eed4-4d59-8f89-f6573edea967");
  private static final UUID DATA_ONE = UUID.fromString("b35d7da7-eed4-4d59-8f89-f6573edea967");
  private static final UUID DATA_TWO = UUID.fromString("b35d7da8-eed4-4d59-8f89-f6573edea967");
  private static final UUID DATA_LENGTH = UUID.fromString("b35d7da9-eed4-4d59-8f89-f6573edea967");
  private static final int DATA_LENGTH_MAX = 20;
  private final GattService mService;
  private final UriBeaconCallback mUriBeaconCallback;
  private Integer mDataLength;
  private byte[] mData;
  private byte[] mDataWrite;

  public ProtocolV1(GattService serviceConnection,
      UriBeaconCallback uriBeaconCallback) {
    mService = serviceConnection;
    mUriBeaconCallback = uriBeaconCallback;
  }

  public ParcelUuid getVersion() {
    return CONFIG_SERVICE_UUID;
  }


  public void writeUriBeacon(ConfigUriBeacon configUriBeacon) throws URISyntaxException {

    mDataWrite = configUriBeacon.toByteArray();
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
    mService.readCharacteristic(DATA_LENGTH);
  }

  @Override
  public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
      int status) {
    // If the operation was successful
    UUID uuid = characteristic.getUuid();
    if (status == BluetoothGatt.GATT_SUCCESS) {
      if (DATA_LENGTH.equals(uuid)) {
        mDataLength = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0);
        mService.readCharacteristic(DATA_ONE);
      } else if (DATA_ONE.equals(uuid)) {
        mData = characteristic.getValue();
        if (mDataLength > DATA_LENGTH_MAX) {
          mService.readCharacteristic(DATA_TWO);
        } else {
          mUriBeaconCallback.onUriBeaconRead(ConfigUriBeacon.parseFromBytes(mData), status);
        }
      } else if (DATA_TWO.equals(uuid)) {
        mData = Util.concatenate(mData, characteristic.getValue());
        mUriBeaconCallback.onUriBeaconRead(ConfigUriBeacon.parseFromBytes(mData), status);
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
      if (mDataWrite.length <= 20 || DATA_TWO.equals(characteristic.getUuid())) {
        mUriBeaconCallback.onUriBeaconWrite(status);
      }
    } else {
      mUriBeaconCallback.onUriBeaconWrite(status);
    }
  }

}


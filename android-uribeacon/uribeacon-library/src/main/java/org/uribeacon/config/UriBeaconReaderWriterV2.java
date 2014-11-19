package org.uribeacon.config;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.ParcelUuid;

import org.uribeacon.beacon.UriBeacon;
import org.uribeacon.config.UriBeaconConfigDispatcher.UriBeaconCallback;

import java.util.UUID;

public class UriBeaconReaderWriterV2 implements BaseUriBeaconReaderWriter {

  public static final ParcelUuid CONFIG_SERVICE_UUID = ParcelUuid
      .fromString("ee0c2087-8786-40ba-ab96-99b91ac981d8");
  //TODO(g-ortuno): Add the rest of the V2 characteristics
  private static final UUID DATA = UUID.fromString("ee0c208a-8786-40ba-ab96-99b91ac981d8");

  private final GattService mService;
  private final UriBeaconCallback mUriBeaconCallback;
  private UUID mLastUUID;
  private UriBeacon mUriBeacon;

  public UriBeaconReaderWriterV2(GattService serviceConnection,
      UriBeaconCallback beaconCallback) {
    mService = serviceConnection;
    mUriBeaconCallback = beaconCallback;
  }

  public ParcelUuid getVersion() {
    return CONFIG_SERVICE_UUID;
  }

  public void startReading() {
    mService.readCharacteristic(DATA);
  }

  @Override
  public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
      int status) {
    UUID uuid = characteristic.getUuid();
    if (DATA.equals(uuid)) {
      mUriBeacon = new UriBeacon.Builder()
          .uriString(characteristic.getValue())
          .buildExisting();
      mUriBeaconCallback.onUriBeaconRead(mUriBeacon, status);
    }
    //TODO(g-ortuno): Add the rest of V2 characteristics
  }

  public void startWriting(UriBeacon uriBeacon) {
    // If the characteristic is different write it to the beacon.
    // Before starting the calls define last call
    if (!uriBeacon.getUriString().equals(mUriBeacon.getUriString())) {
      mLastUUID = DATA;
    }
    // Once the last call has been defined start enqueuing the writes
    if (uriBeacon.getUriString() != null) {
      mService.writeCharacteristic(DATA, uriBeacon.getUriBytes());
    }
    //TODO(g-ortuno): Add the rest of V2 characteristics
  }


  @Override
  public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
      int status) {
    if (mLastUUID.equals(characteristic.getUuid())) {
      mUriBeaconCallback.onUriBeaconWrite(status);
    }
  }
}

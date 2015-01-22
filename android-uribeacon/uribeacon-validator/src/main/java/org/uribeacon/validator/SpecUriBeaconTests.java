package org.uribeacon.validator;

import android.bluetooth.BluetoothGatt;
import android.content.Context;

import org.uribeacon.beacon.ConfigUriBeacon;
import org.uribeacon.config.ProtocolV2;
import org.uribeacon.validator.TestHelper.Builder;
import org.uribeacon.validator.TestHelper.TestCallback;

import java.util.ArrayList;

public class SpecUriBeaconTests {
  public static final String TEST_NAME = "Spec Tests";
  public static ArrayList<TestHelper> initializeTests(Context context, TestCallback testCallback, boolean optional) {
    ArrayList<Builder> specUriBeaconTestsBuilder = new ArrayList<>();
    specUriBeaconTestsBuilder.add(
        new Builder()
        .name("Connecting...")
        .connect()
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Long UriData")
            .write(ProtocolV2.DATA, TestData.LONG_URI, BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Short Flags")
            .write(ProtocolV2.FLAGS, TestData.SHORT_FLAGS,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Long Flags")
            .write(ProtocolV2.FLAGS, TestData.LONG_FLAGS,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Short Advertised Tx Power Levels")
            .write(ProtocolV2.POWER_LEVELS, TestData.SHORT_TX_POWER_LEVELS,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Long Advertised Tx Power Levels")
            .write(ProtocolV2.POWER_LEVELS, TestData.LONG_TX_POWER_LEVELS,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );

    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Short Power Mode")
            .write(ProtocolV2.POWER_MODE, TestData.SHORT_POWER_MODE,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Long Power Mode")
            .write(ProtocolV2.POWER_MODE, TestData.LONG_POWER_MODE,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );

    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Short Period")
            .write(ProtocolV2.PERIOD, TestData.SHORT_PERIOD,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );

    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Long Period")
            .write(ProtocolV2.PERIOD, TestData.LONG_PERIOD,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );

    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Short Reset")
            .write(ProtocolV2.RESET, TestData.SHORT_RESET,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Long Reset")
            .write(ProtocolV2.RESET, TestData.LONG_RESET,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );
    if (optional) {
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Lock with Short Key")
              .write(ProtocolV2.LOCK, TestData.SHORT_LOCK_KEY,
                  BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Lock with Long Key")
              .write(ProtocolV2.LOCK, TestData.LONG_LOCK_KEY,
                  BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locking beacon...")
              .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Try lock with locked beacon and short key")
              .write(ProtocolV2.LOCK, TestData.SHORT_LOCK_KEY,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Try lock with locked beacon and valid key")
              .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Try lock with locked beacon and long key")
              .write(ProtocolV2.LOCK, TestData.LONG_LOCK_KEY,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Unlocking beacon...")
              .write(ProtocolV2.UNLOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Unlock with short key")
              .write(ProtocolV2.UNLOCK, TestData.SHORT_LOCK_KEY,
                  BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Unlock with valid key")
              .write(ProtocolV2.UNLOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Unlock with long key")
              .write(ProtocolV2.UNLOCK, TestData.LONG_LOCK_KEY, BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locking beacon...")
              .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Try to unlock with short key")
              .write(ProtocolV2.UNLOCK, TestData.SHORT_LOCK_KEY, BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Try to unlock with long key")
              .write(ProtocolV2.UNLOCK, TestData.LONG_LOCK_KEY, BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Try to unlock with valid key")
              .write(ProtocolV2.UNLOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
      );
    }
    specUriBeaconTestsBuilder.add(
        new Builder()
        .name("Disconnecting...")
        .disconnect()
    );
    ArrayList<TestHelper> specUriBeaconTests = new ArrayList<>();
    for (Builder builder : specUriBeaconTestsBuilder) {
      specUriBeaconTests.add(builder
          .setUp(context, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
          .build());
    }
    return specUriBeaconTests;
  }
}

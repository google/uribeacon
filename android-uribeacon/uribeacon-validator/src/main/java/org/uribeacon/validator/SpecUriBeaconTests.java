package org.uribeacon.validator;

import android.bluetooth.BluetoothGatt;
import android.content.Context;

import org.uribeacon.beacon.ConfigUriBeacon;
import org.uribeacon.config.ProtocolV2;
import org.uribeacon.validator.TestHelper.Builder;
import org.uribeacon.validator.TestHelper.TestCallback;

import java.util.ArrayList;

class SpecUriBeaconTests {

  public static final String TEST_NAME = "Spec UriBeacon Tests";

  public static ArrayList<TestHelper> initializeTests(Context context, TestCallback testCallback,
      boolean optional) {
    ArrayList<Builder> specUriBeaconTestsBuilder = new ArrayList<>();
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Connecting...")
            .connect()
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Try Long UriData")
            .write(ProtocolV2.DATA, TestData.LONG_URI, BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Try Short Flags")
            .write(ProtocolV2.FLAGS, TestData.SHORT_FLAGS,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Try Long Flags")
            .write(ProtocolV2.FLAGS, TestData.LONG_FLAGS,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Try Short Advertised Tx Power Levels")
            .write(ProtocolV2.POWER_LEVELS, TestData.SHORT_TX_POWER_LEVELS,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Try Long Advertised Tx Power Levels")
            .write(ProtocolV2.POWER_LEVELS, TestData.LONG_TX_POWER_LEVELS,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );

    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Try Short Power Mode")
            .write(ProtocolV2.POWER_MODE, TestData.SHORT_POWER_MODE,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Try Long Power Mode")
            .write(ProtocolV2.POWER_MODE, TestData.LONG_POWER_MODE,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Invalid Power Mode")
            .write(ProtocolV2.POWER_MODE, TestData.INVALID_POWER_MODE, BluetoothGatt.GATT_WRITE_NOT_PERMITTED)
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Try Short Period")
            .write(ProtocolV2.PERIOD, TestData.SHORT_PERIOD,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Try Long Period")
            .write(ProtocolV2.PERIOD, TestData.LONG_PERIOD,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Try Short Reset")
            .write(ProtocolV2.RESET, TestData.SHORT_RESET,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Try Long Reset")
            .write(ProtocolV2.RESET, TestData.LONG_RESET,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
    );
    if (optional) {
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Try Lock with Short Key")
              .write(ProtocolV2.LOCK, TestData.SHORT_LOCK_KEY,
                  BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Try Lock with Long Key")
              .write(ProtocolV2.LOCK, TestData.LONG_LOCK_KEY,
                  BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locking beacon...")
              .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
              .assertEquals(ProtocolV2.LOCK_STATE, TestData.LOCKED_STATE,
                  BluetoothGatt.GATT_SUCCESS)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locked: Try Lock with Short Key")
              .write(ProtocolV2.LOCK, TestData.SHORT_LOCK_KEY,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locked: Try Lock with Valid Key")
              .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locked: Try Lock with Long Key")
              .write(ProtocolV2.LOCK, TestData.LONG_LOCK_KEY,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Unlocking beacon...")
              .write(ProtocolV2.UNLOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
              .assertEquals(ProtocolV2.LOCK_STATE, TestData.UNLOCKED_STATE,
                  BluetoothGatt.GATT_SUCCESS)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Try Unlock with Short Key")
              .write(ProtocolV2.UNLOCK, TestData.SHORT_LOCK_KEY,
                  BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Try Unlock with Valid Key")
              .write(ProtocolV2.UNLOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Try Unlock with Long Key")
              .write(ProtocolV2.UNLOCK, TestData.LONG_LOCK_KEY,
                  BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locking beacon...")
              .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
              .assertEquals(ProtocolV2.LOCK_STATE, TestData.LOCKED_STATE,
                  BluetoothGatt.GATT_SUCCESS)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Try to Unlock with Short Key")
              .write(ProtocolV2.UNLOCK, TestData.SHORT_LOCK_KEY,
                  BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Try Unlock with Long Key")
              .write(ProtocolV2.UNLOCK, TestData.LONG_LOCK_KEY,
                  BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Unlocking...")
              .write(ProtocolV2.UNLOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
              .assertEquals(ProtocolV2.LOCK_STATE, TestData.UNLOCKED_STATE,
                  BluetoothGatt.GATT_SUCCESS)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locking...")
              .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
              .assertEquals(ProtocolV2.LOCK_STATE, TestData.LOCKED_STATE,
                  BluetoothGatt.GATT_SUCCESS)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locked: Try Long Data")
              .write(ProtocolV2.DATA, TestData.LONG_URI, ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locked: Try Short Flags")
              .write(ProtocolV2.FLAGS, TestData.SHORT_FLAGS,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locked: Try Valid Flags")
              .write(ProtocolV2.FLAGS, TestData.BASIC_GENERAL_DATA,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locked: Try Long Flags")
              .write(ProtocolV2.FLAGS, TestData.LONG_FLAGS,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locked: Try Short Advertised Tx Power Levels")
              .write(ProtocolV2.POWER_LEVELS, TestData.SHORT_TX_POWER_LEVELS,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locked: Try Valid Advertised Tx Power Levels")
              .write(ProtocolV2.POWER_LEVELS, TestData.BASIC_TX_POWER_LEVELS,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locked: Try Long Advertised Tx Power Levels")
              .write(ProtocolV2.POWER_LEVELS, TestData.LONG_TX_POWER_LEVELS,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locked: Try Short Power Mode")
              .write(ProtocolV2.POWER_MODE, TestData.SHORT_POWER_MODE,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locked: Try Valid Power Mode")
              .write(ProtocolV2.POWER_MODE, TestData.BASIC_GENERAL_DATA,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locked: Try Long Power Mode")
              .write(ProtocolV2.POWER_MODE, TestData.BASIC_GENERAL_DATA,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locked: Try Short Period")
              .write(ProtocolV2.PERIOD, TestData.SHORT_PERIOD,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locked: Try Valid Period")
              .write(ProtocolV2.PERIOD, TestData.BASIC_PERIOD,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locked: Try Long Period")
              .write(ProtocolV2.PERIOD, TestData.LONG_PERIOD,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locked: Try Short Reset")
              .write(ProtocolV2.RESET, TestData.SHORT_RESET,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locked: Try Valid Reset")
              .write(ProtocolV2.RESET, TestData.BASIC_GENERAL_DATA,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Locked: Try Long Reset")
              .write(ProtocolV2.RESET, TestData.LONG_RESET,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Unlocking...")
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

package org.uribeacon.validator;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;

import org.uribeacon.beacon.ConfigUriBeacon;
import org.uribeacon.config.ProtocolV2;
import org.uribeacon.validator.TestHelper.Builder;
import org.uribeacon.validator.TestHelper.TestCallback;

import java.util.ArrayList;

public class SpecUriBeaconTests {
  public static final String TEST_NAME = "Spec Tests";
  public static ArrayList<TestHelper> initializeTests(Context context,
      BluetoothDevice bluetoothDevice, TestCallback testCallback, boolean optional) {
    ArrayList<Builder> specUriBeaconTestsBuilder = new ArrayList<>();

    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("UriData")
            .connect()
            .write(ProtocolV2.DATA, TestData.LONG_URI, BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
            .disconnect()
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("UriFlags")
            .connect()
            .write(ProtocolV2.FLAGS, TestData.LONG_FLAGS,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
            .disconnect()
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Advertised Tx Power Levels")
            .connect()
            .write(ProtocolV2.POWER_LEVELS, TestData.SHORT_TX_POWER_LEVELS,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
            .write(ProtocolV2.POWER_LEVELS, TestData.LONG_TX_POWER_LEVELS,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
            .disconnect()
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Power Mode")
            .connect()
            .write(ProtocolV2.POWER_MODE, TestData.SHORT_POWER_MODE,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
            .write(ProtocolV2.POWER_MODE, TestData.LONG_POWER_MODE,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
            .disconnect()
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Period")
            .connect()
            .write(ProtocolV2.PERIOD, TestData.SHORT_PERIOD,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
            .write(ProtocolV2.PERIOD, TestData.LONG_PERIOD,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
            .disconnect()
    );
    specUriBeaconTestsBuilder.add(
        new Builder()
            .name("Reset")
            .connect()
            .write(ProtocolV2.RESET, TestData.SHORT_RESET,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
            .write(ProtocolV2.RESET, TestData.LONG_RESET,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
            .disconnect()
    );
    if (optional) {
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Lock")
              .connect()
              .write(ProtocolV2.LOCK, TestData.SHORT_LOCK_KEY,
                  BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
              .write(ProtocolV2.LOCK, TestData.LONG_LOCK_KEY,
                  BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
              .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
              .write(ProtocolV2.LOCK, TestData.SHORT_LOCK_KEY,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
              .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
              .write(ProtocolV2.LOCK, TestData.LONG_LOCK_KEY,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
              .write(ProtocolV2.UNLOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
              .disconnect()
      );
      specUriBeaconTestsBuilder.add(
          new Builder()
              .name("Unlock")
              .connect()
              .write(ProtocolV2.UNLOCK, TestData.SHORT_LOCK_KEY,
                  BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
              .write(ProtocolV2.UNLOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
              .write(ProtocolV2.UNLOCK, TestData.LONG_LOCK_KEY, BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
              .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
              .write(ProtocolV2.UNLOCK, TestData.SHORT_LOCK_KEY, BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
              .write(ProtocolV2.UNLOCK, TestData.LONG_LOCK_KEY, BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
              .write(ProtocolV2.UNLOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
              .disconnect()
      );
    }
    ArrayList<TestHelper> specUriBeaconTests = new ArrayList<>();
    for (Builder builder : specUriBeaconTestsBuilder) {
      specUriBeaconTests.add(builder
          .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
          .build());
    }
    return specUriBeaconTests;
  }
}

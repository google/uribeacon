package org.uribeacon.validator;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;

import org.uribeacon.beacon.ConfigUriBeacon;
import org.uribeacon.config.ProtocolV2;
import org.uribeacon.validator.TestHelper.Builder;
import org.uribeacon.validator.TestHelper.TestCallback;

import java.util.ArrayList;

public class BasicUriBeaconTests {
  public static final String TEST_NAME = "Basic Tests";
  public static ArrayList<TestHelper> initializeTests(Context context,
      BluetoothDevice bluetoothDevice, TestCallback testCallback, boolean optional) {
    ArrayList<Builder> basicTestsBuilder = new ArrayList<>();
    basicTestsBuilder.add(
        new Builder()
            .name("Write and read mandatory Characteristics")
            .connect()
            .insertActions(writeAndReadMandatoryCharacteristics())
            .disconnect()
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Has valid Advertisement Packet")
            .checkAdvPacket()
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Values that are written are advertised")
            .assertAdvFlags(TestData.BASIC_GENERAL_DATA[0])
            .assertAdvTxPower(TestData.BASIC_TX_POWER_LEVELS[1])
            .assertAdvUri(TestData.BASIC_GENERAL_DATA)
    );
    if (optional) {
      basicTestsBuilder.add(
          new Builder()
              .name("Lock/Unlock Beacon")
              .connect()
              .assertEquals(ProtocolV2.LOCK_STATE, TestData.UNLOCKED_STATE,
                  BluetoothGatt.GATT_SUCCESS)
              .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
              .assertEquals(ProtocolV2.LOCK_STATE, TestData.LOCKED_STATE,
                  BluetoothGatt.GATT_SUCCESS)
              .write(ProtocolV2.UNLOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
              .assertEquals(ProtocolV2.LOCK_STATE, TestData.UNLOCKED_STATE,
                  BluetoothGatt.GATT_SUCCESS)
              .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
              .write(ProtocolV2.UNLOCK, TestData.WRONG_LOCK_KEY,
                  ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
              .write(ProtocolV2.UNLOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
              .disconnect()
      );
    }
    ArrayList<TestHelper> basicTests = new ArrayList<>();
    for(Builder builder : basicTestsBuilder) {
      basicTests.add(builder
          .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
          .build());
    }
    return basicTests;
  }
  private static Builder writeAndReadMandatoryCharacteristics() {
    return new Builder()
        .write(ProtocolV2.RESET, TestData.BASIC_GENERAL_DATA, BluetoothGatt.GATT_SUCCESS)
        .assertEquals(ProtocolV2.LOCK_STATE, TestData.UNLOCKED_STATE, BluetoothGatt.GATT_SUCCESS)
        .writeAndRead(ProtocolV2.DATA, TestData.BASIC_GENERAL_DATA)
        .writeAndRead(ProtocolV2.FLAGS, TestData.BASIC_GENERAL_DATA)
        .writeAndRead(ProtocolV2.POWER_LEVELS, TestData.BASIC_TX_POWER_LEVELS)
        .writeAndRead(ProtocolV2.POWER_MODE, TestData.BASIC_GENERAL_DATA)
        .writeAndRead(ProtocolV2.PERIOD, TestData.BASIC_PERIOD);
  }
}
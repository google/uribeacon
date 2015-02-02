package org.uribeacon.validator;

import android.bluetooth.BluetoothGatt;
import android.content.Context;

import org.uribeacon.beacon.ConfigUriBeacon;
import org.uribeacon.config.ProtocolV2;
import org.uribeacon.validator.TestHelper.Builder;
import org.uribeacon.validator.TestHelper.TestCallback;

import java.util.ArrayList;

class CoreUriBeaconTests {

  public static final String TEST_NAME = "Core UriBeacon Tests";

  public static ArrayList<TestHelper> initializeTests(Context context, TestCallback testCallback,
      boolean optional) {
    ArrayList<Builder> basicTestsBuilder = new ArrayList<>();
    basicTestsBuilder.add(
        new Builder()
            .name("Connect to UriBeacon")
            .connect()
    );
    if (optional) {
      addLockUnlockTests(basicTestsBuilder);
    }
    basicTestsBuilder.add(
        new Builder()
            .name("Read Lock State")
            .assertEquals(ProtocolV2.LOCK_STATE, TestData.UNLOCKED_STATE,
                BluetoothGatt.GATT_SUCCESS)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Write Reset")
            .write(ProtocolV2.RESET, TestData.BASIC_GENERAL_DATA, BluetoothGatt.GATT_SUCCESS)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Write and Read Data")
            .writeAndRead(ProtocolV2.DATA, TestData.MULTIPLE_GENERAL_DATA)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Write and Read Flags")
            .writeAndRead(ProtocolV2.FLAGS, TestData.MULTIPLE_GENERAL_DATA)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Write and Read Tx Power Levels")
            .writeAndRead(ProtocolV2.POWER_LEVELS, TestData.MULTIPLE_TX_POWER_LEVELS)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Write and read Tx Power Mode")
            .writeAndRead(ProtocolV2.POWER_MODE, TestData.MULTIPLE_GENERAL_DATA)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Write and read period")
            .writeAndRead(ProtocolV2.PERIOD, TestData.MULTIPLE_BASIC_PERIOD)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Disable Beacon using period = 0")
            .writeAndRead(ProtocolV2.PERIOD, TestData.ZERO_PERIOD)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Floor period")
            .write(ProtocolV2.PERIOD, TestData.LOW_PERIOD, BluetoothGatt.GATT_SUCCESS)
            .assertNotEquals(ProtocolV2.PERIOD, TestData.LOW_PERIOD, BluetoothGatt.GATT_SUCCESS)
            .assertNotEquals(ProtocolV2.PERIOD, TestData.ZERO_PERIOD, BluetoothGatt.GATT_SUCCESS)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Enable beacon again")
            .writeAndRead(ProtocolV2.PERIOD, TestData.BASIC_PERIOD)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Disconnecting")
            .disconnect()
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Has Valid Advertisement Packet")
            .checkAdvPacket()
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Flag Written are Broadcasted")
            .assertAdvFlags(TestData.BASIC_GENERAL_DATA[0])
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Tx Power that is written is being broadcasted")
            .assertAdvTxPower(TestData.BASIC_TX_POWER_LEVELS[1])
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Uri written is being broadcasted")
            .assertAdvUri(TestData.BASIC_GENERAL_DATA)
    );
    return setUpTests(basicTestsBuilder, context, testCallback);
  }

  private static ArrayList<TestHelper> setUpTests(ArrayList<Builder> basicTestsBuilder,
      Context context, TestCallback testCallback) {
    ArrayList<TestHelper> basicTests = new ArrayList<>();
    for (Builder builder : basicTestsBuilder) {
      basicTests.add(builder
          .setUp(context, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
          .build());
    }
    return basicTests;
  }

  private static void addLockUnlockTests(ArrayList<Builder> basicTestsBuilder) {
    basicTestsBuilder.add(
        new Builder()
            .name("Lock Beacon")
            .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
            .assertEquals(ProtocolV2.LOCK_STATE, TestData.LOCKED_STATE,
                BluetoothGatt.GATT_SUCCESS)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Unlock Beacon")
            .write(ProtocolV2.UNLOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
            .assertEquals(ProtocolV2.LOCK_STATE, TestData.UNLOCKED_STATE,
                BluetoothGatt.GATT_SUCCESS)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Locking...")
            .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
            .assertEquals(ProtocolV2.LOCK_STATE, TestData.LOCKED_STATE, BluetoothGatt.GATT_SUCCESS)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Try to unlock with wrong key")
            .write(ProtocolV2.UNLOCK, TestData.WRONG_LOCK_KEY,
                ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Unlocking...")
            .write(ProtocolV2.UNLOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
            .assertEquals(ProtocolV2.LOCK_STATE, TestData.UNLOCKED_STATE,
                BluetoothGatt.GATT_SUCCESS)
    );
  }
}
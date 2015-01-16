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

  public static ArrayList<TestHelper> initializeTests(Context context,
      BluetoothDevice bluetoothDevice, TestCallback testCallback, boolean optional) {
    ArrayList<TestHelper> basicTests = new ArrayList<>();
    basicTests.add(
        new Builder()
            .name("Write and read mandatory Characteristics")
            .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .insertActions(writeAndReadMandatoryCharacteristics())
            .disconnect()
            .build()
    );
    basicTests.add(
        new Builder()
            .name("Has valid Advertisement Packet")
            .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .checkAdvPacket()
            .build()
    );
    basicTests.add(
        new Builder()
            .name("Values that are written are advertised")
            .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .assertAdvFlags(TestData.BASIC_GENERAL_DATA[0])
            .assertAdvTxPower(TestData.BASIC_TX_POWER[1])
            .assertAdvUri(TestData.BASIC_GENERAL_DATA)
            .build()
    );
    if (optional) {
      basicTests.add(
          new Builder()
              .name("Lock/Unlock Beacon")
              .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
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
              .write(ProtocolV2.UNLOCK, TestData.WRONG_LOCK_KEY, ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
              .write(ProtocolV2.UNLOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
              .disconnect()
              .build()
      );
    }
    return basicTests;
  }
  private static Builder writeAndReadMandatoryCharacteristics() {
    return new Builder()
        .write(ProtocolV2.RESET, TestData.BASIC_GENERAL_DATA, BluetoothGatt.GATT_SUCCESS)
        .assertEquals(ProtocolV2.LOCK_STATE, TestData.UNLOCKED_STATE, BluetoothGatt.GATT_SUCCESS)
        .writeAndRead(ProtocolV2.DATA, TestData.BASIC_GENERAL_DATA)
        .writeAndRead(ProtocolV2.FLAGS, TestData.BASIC_GENERAL_DATA)
        .writeAndRead(ProtocolV2.POWER_LEVELS, TestData.BASIC_TX_POWER)
        .writeAndRead(ProtocolV2.POWER_MODE, TestData.BASIC_GENERAL_DATA)
        .writeAndRead(ProtocolV2.PERIOD, TestData.BASIC_PERIOD);
  }
}
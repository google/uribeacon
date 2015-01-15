/*
 * Copyright 2015 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uribeacon.validator;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;

import org.uribeacon.beacon.ConfigUriBeacon;
import org.uribeacon.config.ProtocolV2;
import org.uribeacon.validator.TestHelper.Builder;
import org.uribeacon.validator.TestHelper.TestCallback;

import java.util.ArrayList;

public class UriBeaconTests {

  private static final String TAG = UriBeaconTests.class.getCanonicalName();
  public static ArrayList<TestHelper> initializeTests(Context context, BluetoothDevice bluetoothDevice, TestCallback testCallback) {

    ArrayList<TestHelper> tests = new ArrayList<>();

    tests.addAll(basicTests(context, bluetoothDevice, testCallback));
    tests.addAll(dataTests(context, bluetoothDevice, testCallback));
    tests.addAll(flagsTests(context, bluetoothDevice, testCallback));
    tests.addAll(lockStateTests(context, bluetoothDevice, testCallback));
    tests.addAll(lockTests(context, bluetoothDevice, testCallback));
    // TODO: unlock tests
    tests.add(
        new Builder()
            .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .name("Reset")
            .connect()
            .write(ProtocolV2.RESET, TestData.BASIC_GENERAL_DATA, BluetoothGatt.GATT_SUCCESS)
            .disconnect()
            .build()
    );
    return tests;
  }

  ///////////////////
  ////// Tests //////
  ///////////////////
  private static ArrayList<TestHelper> basicTests(Context context, BluetoothDevice bluetoothDevice,
      TestCallback testCallback) {
    ArrayList<TestHelper> basicTests = new ArrayList<>();
    basicTests.add(
        new Builder()
            .name("Has valid Advertisement Packet")
            .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .disconnect()
            .checkAdvPacket()
            .build()
    );
    return basicTests;
  }
  private static ArrayList<TestHelper> lockStateTests(Context context,
      BluetoothDevice bluetoothDevice, TestCallback testCallback) {
    ArrayList<TestHelper> lockTests = new ArrayList<>();
    lockTests.add(
        new Builder()
            .name("Lock State: Read when unlocked")
            .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .write(ProtocolV2.UNLOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
            .assertEquals(ProtocolV2.LOCK_STATE, TestData.UNLOCKED_STATE,
                BluetoothGatt.GATT_SUCCESS)
            .disconnect()
            .connect()
            .assertEquals(ProtocolV2.LOCK_STATE, TestData.UNLOCKED_STATE,
                BluetoothGatt.GATT_SUCCESS)
            .disconnect()
            .build()
    );
    lockTests.add(
        new Builder()
            .name("Lock State: Read when locked")
            .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
            .assertEquals(ProtocolV2.LOCK_STATE, TestData.LOCKED_STATE, BluetoothGatt.GATT_SUCCESS)
            .disconnect()
            .connect()
            .assertEquals(ProtocolV2.LOCK_STATE, TestData.LOCKED_STATE, BluetoothGatt.GATT_SUCCESS)
            .write(ProtocolV2.UNLOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
            .disconnect()
            .build()
    );
    return lockTests;
  }

  private static ArrayList<TestHelper> lockTests(Context context,
      BluetoothDevice bluetoothDevice, TestCallback testCallback) {
    ArrayList<TestHelper> lockTests = new ArrayList<>();
    lockTests.add(
        new Builder()
            .name("Lock: Write Short Key")
            .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .write(ProtocolV2.LOCK, TestData.SHORT_LOCK_KEY,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
            .insertActions(lockUnlockCheck())
            .insertActions(writeAndReadAllValueCharacteristics())
            .disconnect()
            .connect()
            .insertActions(writeAndReadAllValueCharacteristics())
            .disconnect()
            .build()
    );
    lockTests.add(
        new Builder()
            .name("Lock: Write basic key")
            .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
            .assertEquals(ProtocolV2.LOCK_STATE, TestData.LOCKED_STATE, BluetoothGatt.GATT_SUCCESS)
            .insertActions(writeAllCharacteristics(ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION))
            .disconnect()
            .connect()
            .assertEquals(ProtocolV2.LOCK_STATE, TestData.LOCKED_STATE, BluetoothGatt.GATT_SUCCESS)
            .insertActions(writeAllCharacteristics(ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION))
            .write(ProtocolV2.UNLOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
            .disconnect()
            .build()
    );
    lockTests.add(
        new Builder()
            .name("Lock: Write Long Key")
            .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .write(ProtocolV2.LOCK, TestData.LONG_LOCK_KEY,
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
            .assertEquals(ProtocolV2.LOCK_STATE, TestData.UNLOCKED_STATE,
                BluetoothGatt.GATT_SUCCESS)
            .insertActions(writeAndReadAllValueCharacteristics())
            .disconnect()
            .connect()
            .assertEquals(ProtocolV2.LOCK_STATE, TestData.UNLOCKED_STATE,
                BluetoothGatt.GATT_SUCCESS)
            .insertActions(writeAndReadAllValueCharacteristics())
            .disconnect()
            .build()
    );
    lockTests.add(
        new Builder()
            .name("Lock: Write correct key when locked")
            .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
            .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY,
                ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
            .assertEquals(ProtocolV2.LOCK_STATE, TestData.LOCKED_STATE, BluetoothGatt.GATT_SUCCESS)
            .insertActions(writeAllCharacteristics(ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION))
            .disconnect()
            .connect()
            .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY,
                ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION)
            .assertEquals(ProtocolV2.LOCK_STATE, TestData.LOCKED_STATE, BluetoothGatt.GATT_SUCCESS)
            .insertActions(writeAllCharacteristics(ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION))
            .write(ProtocolV2.UNLOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
            .disconnect()
            .build()
    );
    return lockTests;
  }

  private static ArrayList<TestHelper> dataTests(Context context,
      BluetoothDevice bluetoothDevice, TestCallback testCallback) {
    ArrayList<TestHelper> dataTests = new ArrayList<>();
    Builder dataRangeTest = new Builder()
        .name("UriData: Write <= 18 bytes")
        .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
        .connect();
    for (int i = 0; i <= ConfigUriBeacon.MAX_URI_LENGTH; i++) {
      byte[] testBytes = new byte[i];
      dataRangeTest
          .write(ProtocolV2.DATA, testBytes, BluetoothGatt.GATT_SUCCESS)
          .assertEquals(ProtocolV2.DATA, testBytes, BluetoothGatt.GATT_SUCCESS);
    }
    dataRangeTest.disconnect();
    dataTests.add(dataRangeTest.build());
    dataTests.add(
        new Builder()
            .name("UriData: Write > 18 bytes")
            .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .write(ProtocolV2.DATA, TestData.LONG_URI, BluetoothGatt.GATT_SUCCESS)
            .disconnect()
            .build()
    );
    return dataTests;
  }

  private static ArrayList<TestHelper> flagsTests(Context context, BluetoothDevice bluetoothDevice,
      TestCallback testCallback) {
    ArrayList<TestHelper> flagsTests = new ArrayList<>();
    flagsTests.add(
        new Builder()
            .name("Flags: Write 0 bytes")
            .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .write(ProtocolV2.FLAGS, new byte[]{}, BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
            .disconnect()
            .build()
    );
    flagsTests.add(
        new Builder()
            .name("Flags: Write 1 byte")
            .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .writeAndRead(ProtocolV2.FLAGS, TestData.BASIC_GENERAL_DATA)
            .disconnect()
            .build()
    );
    flagsTests.add(
        new Builder()
            .name("Flags: Write 2 bytes")
            .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .write(ProtocolV2.FLAGS, new byte[2], BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
            .disconnect()
            .build()
    );
    return flagsTests;
  }
  //////////////////////////////
  ////// Reusable actions //////
  //////////////////////////////

  private static Builder writeAndReadAllValueCharacteristics() {
    return new Builder()
        .writeAndRead(ProtocolV2.DATA, TestData.BASIC_GENERAL_DATA)
        .writeAndRead(ProtocolV2.FLAGS, TestData.BASIC_GENERAL_DATA)
        .writeAndRead(ProtocolV2.POWER_LEVELS, TestData.BASIC_TX_POWER)
        .writeAndRead(ProtocolV2.POWER_MODE, TestData.BASIC_GENERAL_DATA)
        .writeAndRead(ProtocolV2.PERIOD, TestData.BASIC_PERIOD);
  }

  private static Builder lockUnlockCheck() {
    return new Builder()
        .assertEquals(ProtocolV2.LOCK_STATE, TestData.UNLOCKED_STATE, BluetoothGatt.GATT_SUCCESS)
        .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
        .assertEquals(ProtocolV2.LOCK_STATE, TestData.LOCKED_STATE, BluetoothGatt.GATT_SUCCESS)
        .write(ProtocolV2.UNLOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
        .assertEquals(ProtocolV2.LOCK_STATE, TestData.UNLOCKED_STATE, BluetoothGatt.GATT_SUCCESS);
  }

  private static Builder writeAllCharacteristics(int expectedReturnCode) {
    return new Builder()
        .write(ProtocolV2.DATA, TestData.BASIC_GENERAL_DATA, expectedReturnCode)
        .write(ProtocolV2.FLAGS, TestData.BASIC_GENERAL_DATA, expectedReturnCode)
        .write(ProtocolV2.POWER_LEVELS, TestData.BASIC_TX_POWER, expectedReturnCode)
        .write(ProtocolV2.POWER_MODE, TestData.BASIC_GENERAL_DATA, expectedReturnCode)
        .write(ProtocolV2.PERIOD, TestData.BASIC_PERIOD, expectedReturnCode)
        .write(ProtocolV2.RESET, TestData.BASIC_GENERAL_DATA, expectedReturnCode);
  }
}

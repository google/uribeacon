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
            .reference("https://github.com/google/uribeacon/blob/master/specification/ConfigService.md")
            .connect()
    );
    if (optional) {
      addLockUnlockTests(basicTestsBuilder);
    }
    basicTestsBuilder.add(
        new Builder()
            .name("Read Lock State")
            .reference("https://github.com/google/uribeacon/blob/master/specification/ConfigService.md#31-lock-state")
            .assertEquals(ProtocolV2.LOCK_STATE, TestData.UNLOCKED_STATE,
                BluetoothGatt.GATT_SUCCESS)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Write Reset")
            .reference("https://github.com/google/uribeacon/blob/master/specification/ConfigService.md#39-reset")
            .write(ProtocolV2.RESET, TestData.BASIC_GENERAL_DATA, BluetoothGatt.GATT_SUCCESS)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Write and Read Data")
            .reference("https://github.com/google/uribeacon/blob/master/specification/ConfigService.md#34-uri-data")
            .writeAndRead(ProtocolV2.DATA, TestData.MULTIPLE_GENERAL_DATA)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Write and Read Flags")
            .reference("https://github.com/google/uribeacon/blob/master/specification/ConfigService.md#35-flags")
            .writeAndRead(ProtocolV2.FLAGS, TestData.MULTIPLE_GENERAL_DATA)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Write and Read Tx Power Levels")
            .reference("https://github.com/google/uribeacon/blob/master/specification/ConfigService.md#36-advertised-tx-power-levels")
            .writeAndRead(ProtocolV2.POWER_LEVELS, TestData.MULTIPLE_TX_POWER_LEVELS)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Write and read Tx Power Mode")
            .reference("https://github.com/google/uribeacon/blob/master/specification/ConfigService.md#37-tx-power-mode")
            .writeAndRead(ProtocolV2.POWER_MODE, TestData.MULTIPLE_GENERAL_DATA)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Write and read period")
            .reference("https://github.com/google/uribeacon/blob/master/specification/ConfigService.md#38-beacon-period")
            .writeAndRead(ProtocolV2.PERIOD, TestData.MULTIPLE_BASIC_PERIOD)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Disable Beacon using period = 0")
            .reference("https://github.com/google/uribeacon/blob/master/specification/ConfigService.md#38-beacon-period")
            .writeAndRead(ProtocolV2.PERIOD, TestData.ZERO_PERIOD)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Floor period")
            .reference("https://github.com/google/uribeacon/blob/master/specification/ConfigService.md#38-beacon-period")
            .write(ProtocolV2.PERIOD, TestData.LOW_PERIOD, BluetoothGatt.GATT_SUCCESS)
            .assertNotEquals(ProtocolV2.PERIOD, TestData.LOW_PERIOD, BluetoothGatt.GATT_SUCCESS)
            .assertNotEquals(ProtocolV2.PERIOD, TestData.ZERO_PERIOD, BluetoothGatt.GATT_SUCCESS)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Enable beacon again")
            .reference("https://github.com/google/uribeacon/blob/master/specification/ConfigService.md#38-beacon-period")
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
            .reference("https://github.com/google/uribeacon/tree/master/specification#uribeacon-advertising-data")
            .checkAdvPacket()
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Flag Written are Broadcasted")
            .reference("https://github.com/google/uribeacon/tree/master/specification#uribeacon-flags")
            .assertAdvFlags(TestData.BASIC_GENERAL_DATA[0])
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Tx Power that is written is being broadcasted")
            .reference("https://github.com/google/uribeacon/tree/master/specification#uribeacon-tx-power-level")
            .assertAdvTxPower(TestData.BASIC_TX_POWER_LEVELS[1])
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Uri written is being broadcasted")
            .reference("https://github.com/google/uribeacon/tree/master/specification#uribeacon-uri-scheme-prefix")
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
            .reference("https://github.com/google/uribeacon/blob/master/specification/ConfigService.md#32-lock")
            .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
            .assertEquals(ProtocolV2.LOCK_STATE, TestData.LOCKED_STATE,
                BluetoothGatt.GATT_SUCCESS)
    );
    basicTestsBuilder.add(
        new Builder()
            .name("Unlock Beacon")
            .reference("https://github.com/google/uribeacon/blob/master/specification/ConfigService.md#33-unlock")
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
            .reference("https://github.com/google/uribeacon/blob/master/specification/ConfigService.md#33-unlock")
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